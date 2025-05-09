package com.snowhub.server.dummy.domain.oauth.firebase;

import java.io.IOException;
import java.rmi.ServerException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
//import com.snowhub.server.dummy.domain.oauth.firebase.dummy.CustomHttpClient;
import com.snowhub.server.dummy.domain.oauth.firebase.dto.FireBaseDetails;
import com.snowhub.server.dummy.domain.user.infrastructure.User;
import com.snowhub.server.dummy.domain.user.service.UserService;
import com.snowhub.server.dummy.security.service.CustomUserDetailsService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class FirebaseAuthTokenManager {
	
	private final CustomUserDetailsService customUserDetailsService;
	private final UserService userService;
	private final FirebaseAuth firebaseAuth;
	private final WebClient webClient;
	FirebaseToken decode;

	// 스레드 safe하기 떄문에 이게 좋을듯.
	private final MultiValueMap<String,String> payload = new LinkedMultiValueMap<>();

	// 1. 토큰 검증 메서드.
	public void verifyToken(HttpServletRequest request,
		HttpServletResponse response,
		String accessToken, String refreshToken,
		FilterChain filterChain) throws IOException {
		log.info("Token 검증 메소드 시작");

		//FirebaseToken decode = null;// 변수 <- public final class를 매번 이런식으로 할당하는 게 맞나? <- 수정

		 // accessToken은 필수 -> null은 else로 처리하면 된다.
		if(accessToken!=null && refreshToken==null){ // accessToken만 있는 경우
			// 1. accessToken만 검증하자.
			try {
				// 1. accessToken을 받은 경우
				decode = firebaseAuth.verifyIdToken(accessToken);

				String email = decode.getEmail();// 회원을 구분짓는 username 대신의 식별자.

				// Authentication 등록(변수)
				UserDetails getUserDetails = customUserDetailsService.loadUserByEmail(email);
				Authentication authentication =
					new UsernamePasswordAuthenticationToken(getUserDetails,null,getUserDetails.getAuthorities());
				SecurityContextHolder.getContext().setAuthentication(authentication);

				filterChain.doFilter(request,response);

			}
			catch (FirebaseAuthException e) { //Firebase 오류
				throw new RuntimeException("Firebase Exception : "+e.getMessage());
			}
			catch (ServletException e){ //Servlet 오류
				throw new ServerException("Servlet Exception : "+e.getMessage());
			}
			catch (Exception e ){ // 나머지 기타 오류
				throw new RuntimeException("Exception : "+e.getMessage());
			}

		}
		else if(accessToken != null){ // accessToken, refreshToken 둘다 있는 경우

			try {
				// Payload
				// stateless한데, 굳이 매번 새로운 객체를 만들어야 할까?
				//MultiValueMap<String,String> payload = new LinkedMultiValueMap<>();
				payload.add("grant_type","refresh_token");
				payload.add("refresh_token",refreshToken);

				// accessToken을 재발급을 받기 위한 절차
				//  CustomHttpClient.getInstance()
				FireBaseDetails.ReissuanceToken reissuanceToken = webClient.mutate()
					.build()
					.post()
					.uri("https://securetoken.googleapis.com/v1/token?key=AIzaSyBTLAv6wGA--ago8nUor445hdho3eIvqnA")
					.header("Content-type", "application/x-www-form-urlencoded;charset=utf-8")
					.body(BodyInserters.fromFormData(payload))
					.retrieve()
					.bodyToMono(FireBaseDetails.ReissuanceToken.class)
					.block();

				// RefreshToken은 항상 로그인을 할때 마다 발급이 되므로, 굳이 갱신(reissuanceToken.getRefresh_token())할 필요가 없다.
				String idToken = reissuanceToken.getId_token();

				User findUser = userService.findRefreshToken(refreshToken);// refreshToken으로 User찾기.
				String email = findUser.getEmail();

				UserDetails getUserDetails = customUserDetailsService.loadUserByEmail(email);
				Authentication authentication =
					new UsernamePasswordAuthenticationToken(getUserDetails,null,getUserDetails.getAuthorities());
				SecurityContextHolder.getContext().setAuthentication(authentication);

				System.out.println("idToken:"+idToken);

				response.addHeader("IdTokenCookie",idToken);
				response.addHeader("refreshTokenCookie",refreshToken);
				filterChain.doFilter(request,response);
			} catch (ServletException e) {
				throw new RuntimeException(e);
			} catch (NullPointerException e){
				throw new NullPointerException(e.getMessage());
			} finally {
				payload.clear();
			}

		}
		else{ // accessToken이 없는 경우
			throw new RuntimeException("AccessToken이 없습니다.");
			
		}
		log.info("Token 검증 메소드 완료");

	}

	// 2. 예외 처리 메서드
	private void handleException(HttpServletResponse response,Exception e) throws IOException {

		response.setStatus(HttpStatus.BAD_REQUEST.value());
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		Map<String,String> responseBody = new HashMap<>();
		responseBody.put("error",e.getMessage());
		responseBody.put("status",HttpStatus.BAD_REQUEST.value()+"");

		ObjectMapper objectMapper = new ObjectMapper();
		String json = objectMapper.writeValueAsString(responseBody);

		response.getWriter().write(json);

	}

	// 3. 이메일 추출 메서드
	public String getEmail(HttpServletRequest request){

		String getBearerToken = request.getHeader("Authorization");
		String accesstoken=null;

		if (getBearerToken != null && getBearerToken.startsWith("Bearer ")) {
			accesstoken = getBearerToken.substring(7); // Extracting the token after "Bearer "
		}

		else{
			// Bearer로 시작하는 토큰이 아닌 경우 -> 오류발생
			throw new RuntimeException("It is not Bearer Token");
		}

		FirebaseToken decode = null;
		// Throw를 할 경우, boardService를 쓰는 모든 컴포넌트가 throw FirebaseException을 throw 해야함.
		try {
			decode =  firebaseAuth.verifyIdToken(accesstoken);
		} catch (FirebaseAuthException e) {
			throw new RuntimeException(e);
		}

		return decode.getEmail();

	}
	
}

			/*
				브라우저로 에러 던짐.
				handleException(response,e);
				return;
			 */

// 토큰 검증 로직
		/*
		try {
			// 1. accessToken 검증로직
			log.info("1. Decode AccessToken");
			System.out.println("accessToken:"+accessToken);
			FirebaseToken decode = null;

			try {
				decode = firebaseAuth.verifyIdToken(accessToken);
			} catch (FirebaseAuthException e) {
				throw new RuntimeException(e);
			}

			String email = decode.getEmail();// 회원을 구분짓는 username 대신의 식별자.
			//String username =decode.getName();
			log.info("2. Make Authentication & Save at ContextHolder");


			UserDetails getUserDetails = customUserDetailsService.loadUserByEmail(email);
			Authentication authentication =
				new UsernamePasswordAuthenticationToken(getUserDetails,null,getUserDetails.getAuthorities());
			SecurityContextHolder.getContext().setAuthentication(authentication);

			log.info("3. PASS Filter");

			filterChain.doFilter(request,response);
			return;

		} catch (IOException | ServletException e) { // 강제로 예외 발생안시키고, 다음꺼 실행
			// 2. 토큰 오류 처리와 로직

			// Token_Expiration + RefreshToken!=null -> accessToken이 만료되었지만, RefreshToken은 가지고 있는 상태라서,
			// 다음 과정(RefreshToken)으로 이동을 해도 괜찮을 듯. <- 검증된 사용자니까

			if(e.getMessage().equals(ErrorType.Token_Expiration.getValue()) && RefreshToken!=null ){
				// RefreshToken 로직으로 넘어가기 위해서 일부러 비워둠.

			}
			else{
				// 브라우저로 에러 던짐.
				handleException(response,e);
				return;

			}

		}

		// NOT_FireBaseRefreshToken인 경우 -> 무조건 로그인 페이지로 redirect

		// 2. Firebase의 refeshToken인지 검증. 실패시 status는 false로 종료.
		try{
			log.info("3. Check RefreshToken");

			// Payload
			MultiValueMap<String,String> payload = new LinkedMultiValueMap<>();
			payload.add("grant_type","refresh_token");
			payload.add("refresh_token",RefreshToken);

			FireBaseDetails.ReissuanceToken reissuanceToken = CustomHttpClient.getInstance().mutate()
				.build()
				.post()
				.uri("https://securetoken.googleapis.com/v1/token?key=AIzaSyBTLAv6wGA--ago8nUor445hdho3eIvqnA")
				.header("Content-type", "application/x-www-form-urlencoded;charset=utf-8")
				.body(BodyInserters.fromFormData(payload))
				.retrieve()
				.bodyToMono(FireBaseDetails.ReissuanceToken.class)
				.block();

			String idToken = reissuanceToken.getId_token();
			//String refreshToken = reissuanceToken.getRefresh_token();

			// RefreshToken은 항상 로그인을 할때 마다 발급이 되므로, 굳이 갱신할 필요가 없다.
			// 그리고 위에서 재발급 받은 토큰 = 로그인 시 발급 받은 토큰




			// Authentication을 등록한다.
			User findUser = userService.findRefreshToken(RefreshToken);// refreshToken으로 User찾기.
			String email = findUser.getEmail();

			UserDetails getUserDetails = customUserDetailsService.loadUserByEmail(email);
			Authentication authentication =
				new UsernamePasswordAuthenticationToken(getUserDetails,null,getUserDetails.getAuthorities());
			SecurityContextHolder.getContext().setAuthentication(authentication);


			//굳이 request에 실어서 보내지 말고, 여기서 response의 header에 담아서 처리하자.

			response.addHeader("IdTokenCookie",idToken);
			response.addHeader("refreshTokenCookie",RefreshToken);
			log.info("4. RefreshToken Check Completed");
			filterChain.doFilter(request,response);

		}
		catch (Exception e){
			handleException(response,e);
		}

		 */