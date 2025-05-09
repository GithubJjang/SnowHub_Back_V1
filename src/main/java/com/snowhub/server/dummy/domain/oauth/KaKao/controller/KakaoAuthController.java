package com.snowhub.server.dummy.domain.oauth.KaKao.controller;

import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.snowhub.server.dummy.config.SupplierConfig.KakaoConfig;
import com.snowhub.server.dummy.domain.oauth.KaKao.dto.KaKaoDetails;
import com.snowhub.server.dummy.domain.user.model.request.UserRequest;
import com.snowhub.server.dummy.domain.user.infrastructure.User;
//import com.snowhub.server.dummy.domain.oauth.firebase.dummy.CustomHttpClient;
import com.snowhub.server.dummy.domain.user.service.UserService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.view.RedirectView;

@Slf4j
@AllArgsConstructor
@Controller
public class KakaoAuthController {

    private final KakaoConfig kakaoConfig;
    private final UserService userService;
    private final WebClient webClient;

    @GetMapping("/auth/kakao")
    public RedirectView redirect(@RequestParam(name = "code") String code, HttpServletResponse res){



        // Payload
        MultiValueMap<String, String> payload = new LinkedMultiValueMap<>();
        payload.add("grant_type","authorization_code");
        payload.add("client_id",kakaoConfig.getRestApiKey());
        payload.add("client_secret",kakaoConfig.getClientSecret());
        payload.add("redirect_uri",kakaoConfig.getRedirectUri());
        payload.add("code",code);

        // CustomHttpClient
        KaKaoDetails.Token getTokenFromKaKao = webClient.mutate()
                .build()
                .post()
                .uri("https://kauth.kakao.com/oauth/token")
                .header("Content-type", "application/x-www-form-urlencoded;charset=utf-8")
                .body(BodyInserters.fromFormData(payload))
                .retrieve()
                .bodyToMono(KaKaoDetails.Token.class)
                .block();

        //String tokenType = getTokenFromKaKao.getToken_type();
        String accessToken = getTokenFromKaKao.getAccess_token();
        //String idToken = getTokenFromKaKao.getId_token();
        //String refreshToken = getTokenFromKaKao.getRefresh_token();



        // 2. firebase의 Authentication에 등록

        // Payloads
        payload.clear();
        payload.add("property_keys", "[\"kakao_account.profile\",\"kakao_account.name\",\"kakao_account.email\"]");
        //

        KaKaoDetails.User getUserFromKaKao = webClient.mutate()
                .build()
                .post()
                .uri("https://kapi.kakao.com/v2/user/me")
                .header("Authorization","Bearer "+accessToken)
                .body(BodyInserters.fromFormData(payload))
                .retrieve()
                .bodyToMono(KaKaoDetails.User.class)
                .block();

        String email = getUserFromKaKao.getKakao_account().getEmail();// email
        String id = getUserFromKaKao.getId()+"";// password (고민)
        String name = getUserFromKaKao.getKakao_account().getProfile().getNickname();// username

        // 1. 신규 회원등록 로직
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        User getUser = userService.findUserByEmail(email);// email은 고유 식별자
        if (getUser ==null){// 신규 회원등록 로직. 처음에만  save하고, 그 이후는 PASS
            try {
                log.info("Enroll new user!!!");
                // 신규 User를 추가.
                UserRequest userDTO = UserRequest.builder()
                        .email(email)
                        .password(id)
                        .displayName(name)
                        .build();

                UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                        .setEmail(userDTO.getEmail())
                        .setPassword(userDTO.getPassword())
                        .setDisplayName(userDTO.getDisplayName())// username
                        //.setEmailVerified(false)// 수정
                        //.setPhoneNumber(user.getPhoneNumber())
                        ;
                firebaseAuth.createUser(request);// 이미 등록된 사용자면 Exception발생
                userService.saveUser(userDTO);
            } catch (FirebaseAuthException e) {
                log.error("enroll original user double. this must not activated. if so, check here GoogleAuthController!!!");
                throw new RuntimeException(e.getMessage());
            }
        }


        // 2. idToken과 RefreshToken을 firebase로 부터 가져오기.

        // WebClient URL, firebase로 API요청을 보내기 위해서.
        WebClient webClient = WebClient.create();

        // Body elements
        String returnSecureToken = "true";

        // Body Payload
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("email", email);
        formData.add("password", id);
        formData.add("returnSecureToken", returnSecureToken);

        // Firebase API를 이용해서, 로그인을 진행한다. <- 검증을 한다.
        // 그리고, idToken, refreshToken 받아오기.
        String getUserInfo = webClient.post()
                .uri("https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=AIzaSyBTLAv6wGA--ago8nUor445hdho3eIvqnA")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        // ObjectMapper 생성
        ObjectMapper objectMapper = new ObjectMapper();

        // JSON 문자열을 Map으로 변환

		Map<String, String> tokens = null;
		try {
			tokens = objectMapper.readValue(getUserInfo, Map.class);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}

		// idToken과 refreshToken 추출
        String username = tokens.get("displayName");
        String idToken = tokens.get("idToken");
        String refreshToken = tokens.get("refreshToken");

        userService.updateRefreshToken(refreshToken,email);

        // Uid 재 업데이트
        try {
            FirebaseToken decodedToken = firebaseAuth.verifyIdToken(idToken);
            String uid = decodedToken.getUid();
            userService.updateUid(email,uid);
        } catch (FirebaseAuthException e) {
            throw new RuntimeException(e);
        }

        // 5. Token이 담긴 Cookie를 사용자에게 반환
        // setMaxAge를 할 경우, 브라우저를 종료해도 쿠키는 남아있는 상태가 된다.


        Cookie idCookie = new Cookie("IdTokenCookie",idToken);
        idCookie.setDomain(""); // localhost
        idCookie.setPath("/");
        //idCookie.setMaxAge(3600); <- 브라우저 끄면, 같이 쿠키도 사라지는 설정.
        idCookie.setSecure(false);

        // 테스트

        Cookie refreshCookie = new Cookie("refreshTokenCookie",refreshToken);
        refreshCookie.setDomain(""); // localhost
        refreshCookie.setPath("/");
        //refreshCookie.setMaxAge(3600); <- 브라우저 끄면, 같이 쿠키도 사라지는 설정.
        refreshCookie.setSecure(false);


        res.addCookie(idCookie);
        res.addCookie(refreshCookie);

        RedirectView redirectView = new RedirectView();
        //redirectView.setUrl("http://localhost:3000/");
        //redirectView.setUrl("http://192.168.55.203:3000/login");  // Test
        redirectView.setUrl("https://192.168.55.203:443/login");


        /*
        Cookie idCookie = new Cookie("IdTokenCookie",idToken);
        idCookie.setDomain("localhost");
        idCookie.setPath("/");
        //idCookie.setMaxAge(3600);
        idCookie.setSecure(false);


        Cookie refreshCookie = new Cookie("refreshTokenCookie",refreshToken);
        refreshCookie.setDomain("localhost");
        refreshCookie.setPath("/");
        //refreshCookie.setMaxAge(3600);
        refreshCookie.setSecure(false);

        res.addCookie(idCookie);
        res.addCookie(refreshCookie);

        RedirectView redirectView = new RedirectView();
        redirectView.setUrl("http://localhost:3000/");
        return redirectView;

         */

        return redirectView;
    }
}
