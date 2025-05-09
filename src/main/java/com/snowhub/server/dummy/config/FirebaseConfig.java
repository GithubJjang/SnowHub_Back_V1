package com.snowhub.server.dummy.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.snowhub.server.dummy.domain.oauth.firebase.FirebaseAuthTokenManager;
import com.snowhub.server.dummy.domain.user.service.UserService;
import com.snowhub.server.dummy.security.service.CustomUserDetailsService;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.io.InputStream;
@RequiredArgsConstructor
@Configuration
public class FirebaseConfig {

    // 생성자 주입을 통하서 Null이 들어가는 것을 방지한다.
    private final CustomUserDetailsService customUserDetailsService;
    private final UserService userService;
    private final WebClient webClient;

    // 1. Firebase SDK를 사용하기 위한 설정
    @Bean
    public FirebaseAuth firebaseAuth(){
        return FirebaseAuth.getInstance();
    }

    @PostConstruct
    public void init() throws IOException {
        InputStream serviceAccount = getClass().getClassLoader().getResourceAsStream("serviceAccountKey.json");

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                //.setCredentials(GoogleCredentials.getApplicationDefault())
                .setServiceAccountId("firebase-adminsdk-r7r96@authfirewithspringboot.iam.gserviceaccount.com")
                .build();

        FirebaseApp.initializeApp(options);

    }

    // 2. firebase 토큰 관리 빈
    @Bean
    public FirebaseAuthTokenManager firebaseAuthTokenManager(){
        return new FirebaseAuthTokenManager(customUserDetailsService,userService,firebaseAuth(),webClient);
    }

}
