package com.snowhub.server.dummy.config;

import com.snowhub.server.dummy.config.SupplierConfig.CustomAccessDeniedHandler;
import com.snowhub.server.dummy.domain.oauth.firebase.FirebaseAuthTokenManager;
import com.snowhub.server.dummy.security.filter.VerifyTokenFilter;

import jakarta.servlet.DispatcherType;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.CorsUtils;

@AllArgsConstructor
@Configuration
@EnableWebSecurity //(debug = true)
public class SecurityConfig {

   private final CustomAccessDeniedHandler customAccessDeniedHandler;
   private final CorsConfigurationSource corsConfigurationSource;
   private final FirebaseAuthTokenManager firebaseAuthTokenManager;

    public static final String[] PUBLIC_URLS = {
        "/swagger-ui/**",
        "/v3/**",
        "/actuator/**",
        "/publish/**",
        "/logout",
        "/auth/**",
        "/ws"
    };


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .httpBasic(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)//Csrf 안씀.
                .sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))// Session 안씀.
                .formLogin(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize ->
                        authorize
                                .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll() // 제일 의심가는 부분. <- 해결
                                .requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
                                .anyRequest().authenticated()
                        // ADMIN페이지는 따로 시큐리티 Authority를 부여. <- 토큰 로그인이 X
                )
                .addFilterBefore(new VerifyTokenFilter(firebaseAuthTokenManager), UsernamePasswordAuthenticationFilter.class
                );

        http
                .exceptionHandling(exception -> exception.accessDeniedHandler(customAccessDeniedHandler)// 403에러
                );


        return http.build();
    }


    @Bean WebSecurityCustomizer webSecurityCustomizer(){
        return web -> web.ignoring()
                .requestMatchers(PUBLIC_URLS)
                ;
    }




}

