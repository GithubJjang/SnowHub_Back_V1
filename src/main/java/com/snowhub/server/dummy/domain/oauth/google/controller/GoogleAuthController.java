package com.snowhub.server.dummy.domain.oauth.google.controller;

import com.snowhub.server.dummy.config.SupplierConfig.GoogleConfig;
import com.snowhub.server.dummy.domain.oauth.firebase.dto.FireBaseDetails;
import com.snowhub.server.dummy.domain.oauth.google.dto.GoogleDetails;
import com.snowhub.server.dummy.domain.user.infrastructure.User;
import com.snowhub.server.dummy.domain.user.model.request.UserRequest;
//import com.snowhub.server.dummy.domain.oauth.firebase.dummy.CustomHttpClient;
import com.snowhub.server.dummy.domain.user.service.UserService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
@Controller
public class GoogleAuthController {

    private final GoogleConfig googleConfig;
    private final UserService userService;
    private final WebClient webClient;


    // - 이쪽으로 redirect가 됨. 여기서 accessToken,idToken 등 필요한 데이터를 가져온다.
    @GetMapping("/auth/google")
    public RedirectView reDirect(@RequestParam(name = "code") String code, HttpServletResponse res){
        long startTime = System.currentTimeMillis();

        // 1. Google에서 토큰 발급받기.

        // - Header <- WebClient에 직접 세팅
        // - Payload
        MultiValueMap<String, String> payload = new LinkedMultiValueMap<>();
        payload.add("code", code);
        payload.add("client_id", googleConfig.getClientId());
        payload.add("client_secret", googleConfig.getClientSecret());
        payload.add("redirect_uri", googleConfig.getRedirectUri());
        payload.add("grant_type", "authorization_code");

        //  기존 유저인 경우, DB에서 찾은 후 update


        GoogleDetails.Token getTokenFromGoogle = webClient.mutate()
            .build()
            .post()
            .uri("https://oauth2.googleapis.com/token")
            .header("Content-type", "application/x-www-form-urlencoded;charset=utf-8")
            .body(BodyInserters.fromFormData(payload))
            .retrieve()
            .bodyToMono(GoogleDetails.Token.class)
            .block();




        String accessToken = getTokenFromGoogle.getAccess_token();// null검사 안함 <- null검사하는 과정 전부다 비용

        // 2, Google에서 사용자 정보 가져오기.
        //https://www.googleapis.com/oauth2/v2/userinfo <- id,이름,사진주소,국적
        GoogleDetails.User userInfoFromGoogle = webClient.mutate()
                .build()
                .get()
                .uri("https://www.googleapis.com/oauth2/v2/userinfo")
                .header("Authorization","Bearer " + accessToken)
                .retrieve()
                .bodyToMono(GoogleDetails.User.class)
                .block();


        String id = userInfoFromGoogle.getId();// password (고민)
        String name = userInfoFromGoogle.getName();// username
        String email = userInfoFromGoogle.getEmail();// email

        // 3. 신규 회원등록 로직( Firebase에 회원등록 )
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        User getUser = userService.findUserByEmail(email);// email은 고유 식별자
        if (getUser ==null){// 신규 회원등록 로직. 처음에만  save하고, 그 이후는 PASS
            try {
                log.info("Enroll new user!!!");

                UserRequest userDTO = UserRequest.builder()
                        .email(email)
                        .password(id)
                        .displayName(name)
                        .build();

                UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                        .setEmail(userDTO.getEmail())
                        .setPassword(userDTO.getPassword()) // 필수! 안하면 BAD_REQUEST
                        .setDisplayName(userDTO.getDisplayName())// username
                        //.setEmailVerified(false)// 수정
                        //.setPhoneNumber(user.getPhoneNumber())
                        ;
                firebaseAuth.createUser(request);// 이미 등록된 사용자면 Exception발생
                userService.saveUser(userDTO);
            } catch (FirebaseAuthException e) {
                log.error("API:/auth/google - Enroll original user double. ");
                throw new RuntimeException(e.getMessage());
            }
        }

        // 4. Get Tokens from Firebase
        // 사용자를 먼저 Firebase에 등록을 해야만 Token을 발급 받을 수 있다.

        // Body Payload
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("email", email);
        formData.add("password", id);
        formData.add("returnSecureToken", "true"); // String returnSecureToken = "true" -> 토큰 발급이 가능하다.

        FireBaseDetails.Token firebaseTokens = webClient.mutate()
                .build()
                .post()
                .uri("https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=AIzaSyBTLAv6wGA--ago8nUor445hdho3eIvqnA")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(FireBaseDetails.Token.class)
                .block();

        String idToken = firebaseTokens.getIdToken();
        String refreshToken = firebaseTokens.getRefreshToken();

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


        return redirectView;



    }

}
