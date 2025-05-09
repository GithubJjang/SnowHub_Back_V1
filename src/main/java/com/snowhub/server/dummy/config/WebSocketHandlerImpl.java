package com.snowhub.server.dummy.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.databind.ObjectMapper;

public class WebSocketHandlerImpl implements WebSocketHandler {

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {

		ObjectMapper mapper = new ObjectMapper();

		// 예: Map 객체를 JSON으로
		Map<String, String> message = new HashMap<>();
		message.put("status", "connected");

		String json = mapper.writeValueAsString(message);  // {"status":"connected"}

		// WebSocket 연결이 성공적으로 이루어졌을 때 호출됩니다.
		//System.out.println("WebSocket 연결 성공: " + session.getId());
		// 클라이언트에 연결 성공 메시지 전송
		session.sendMessage(new TextMessage(json));
	}

	@Override
	public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
		// 클라이언트에서 보낸 메시지를 처리합니다.
		//System.out.println("메시지 수신: " + message.getPayload());
		// 클라이언트로 메시지를 다시 보냄
		session.sendMessage(new TextMessage("서버에서 받은 메시지: " + message.getPayload()));
	}

	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
		// WebSocket 연결 중 오류가 발생하면 호출됩니다.
		System.err.println("WebSocket 오류 발생: " + exception.getMessage());
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		// WebSocket 연결이 종료된 후 호출됩니다.
		System.out.println("WebSocket 연결 종료: " + session.getId());
	}

	@Override
	public boolean supportsPartialMessages() {
		// WebSocket 메시지의 부분 지원 여부를 정의합니다.
		return false;
	}
}
