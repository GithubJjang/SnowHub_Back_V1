package com.snowhub.server.dummy.domain.sse.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.snowhub.server.dummy.domain.sse.infrastructure.EmitterRepositoryImpl;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class Scheduler {

	private final EmitterRepositoryImpl emitterRepository;
	
	@Scheduled(fixedDelay = 30000)
	public void automatedExternalDefibrillatorForSSE(){
		System.out.println("심장 충격기 작동");
		emitterRepository.sseKeepAlive();
	}
}
