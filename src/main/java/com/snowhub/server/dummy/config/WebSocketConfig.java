package com.snowhub.server.dummy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		// /wss 경로로 WebSocket 핸들러 연결
		registry.addHandler(webSocketHandler(), "/ws") // /wss 경로로 WebSocket 핸들러 연결
			.addInterceptors(new HttpSessionHandshakeInterceptor()) // 세션을 핸들링하는 인터셉터
			.setAllowedOrigins("*"); // 모든 출처에서 접근을 허용
	}

	@Bean
	public WebSocketHandler webSocketHandler() {
		return new WebSocketHandlerImpl(); // 사용자 정의 WebSocketHandler
	}
}
