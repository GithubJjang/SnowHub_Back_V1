package com.snowhub.server.dummy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.reactive.function.client.WebClient;

import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;

@Configuration
public class LibraryBeanConfig {

	@Bean
	public WebClient webClient(){
		return WebClient.create();
	}

	@Bean
	public PasswordEncoder PasswordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public JPAQueryFactory jpaQueryFactory(EntityManager em){ return new JPAQueryFactory(em);}

}
