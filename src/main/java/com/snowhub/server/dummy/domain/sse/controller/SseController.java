package com.snowhub.server.dummy.domain.sse.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.snowhub.server.dummy.domain.sse.service.EmitterService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
public class SseController {

	private final EmitterService emitterService;

	// - 게시글 불러오기에 SSE 실시간 통신
	@GetMapping(value = "/board/list/streams",produces = "text/event-stream")
	public SseEmitter fetchBoardList(
		//,@RequestHeader(value = "Last-Event-ID", required = false, defaultValue = "") String lastEventId),
		@AuthenticationPrincipal UserDetails principal
	){
		// id는 userId
		return emitterService.getMessageQueue(principal.getUsername());
	}

	// - 게시글 SSE 실시간으로 데이터 받기
	@GetMapping("/publish")
	public String publish(
		@RequestParam(name = "userId")String userId,
		@RequestParam(name = "type")String resort){
		emitterService.pubAllEmitter(userId,resort);
		return "Successfully publish";

	}
}
