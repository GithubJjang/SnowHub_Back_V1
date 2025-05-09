package com.snowhub.server.dummy.domain.sse.service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.snowhub.server.dummy.domain.sse.model.response.QueueResponse;
import com.snowhub.server.dummy.domain.sse.infrastructure.EmitterRepositoryImpl;

import io.lettuce.core.api.reactive.RedisReactiveCommands;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class EmitterService {

	private final EmitterRepositoryImpl emitterRepository;

	@Qualifier("Master_redisTemplate")
	private final RedisTemplate<String,String> masterRedisTemplate;

	private final RedisReactiveCommands<String, String> slaveRedisReactiveCommands;

	private static final Long DEFAULT_TIMEOUT = 60L*1000*60;
	public SseEmitter getMessageQueue(String id)  {

		SseEmitter emitter =
			emitterRepository.save(id,new SseEmitter(DEFAULT_TIMEOUT));

		// 시간 초과, 비동기 요청 실패시 작동
		emitter.onCompletion(()->emitterRepository.deleteById(id));
		emitter.onTimeout(()->emitterRepository.deleteById(id));

		// 더미데이터 던져야 함.(수정)
		// 이 부분에서 Redis에서 작업 큐를 찾는다.
		// 있다면 작업 큐에서 반환하고,
		// 없으면 새로 생성을 한다.
		ListOperations<String,String> enrollMyQueueTemplate = masterRedisTemplate.opsForList();

		// 1.자신의 고유한 메시지 큐를 조회한다.
		Mono<List<String>> findMyQueue = slaveRedisReactiveCommands.lrange(id+"MessageQueue",0,-1)
		.collectList().defaultIfEmpty(Collections.emptyList());// 없는 경우 빈 리스트를 집어 넣는다.

		// 비었으면, 신규 사용자임. -> 새로 메시지 큐를 생성하자
		findMyQueue.doOnNext(list -> {
			if (list.isEmpty()) {
				enrollMyQueueTemplate.leftPush(id +"MessageQueue", "");
				enrollMyQueueTemplate.leftPush(id +"SubscribeQueue", "");
			}
		}).subscribe();

		// 가져온 메시지 큐 반환하기
		Mono<QueueResponse> queueResponse = findMyQueue.map(QueueResponse::new);
		setEmitter(emitter,id,queueResponse);

		return emitter;

	}

	private void setEmitter(SseEmitter emitter,String id,Mono<QueueResponse> queueResponse){

		queueResponse.subscribe(response -> {
			try {
				emitter.send(
					SseEmitter.event()
						.id(id)
						.data(response)  // JSON 직렬화 가능
				);
			} catch (IOException e) {
				emitterRepository.deleteById(id);
				e.printStackTrace();
			}
		});
	}

	// resort 구독하기
	public void subscribe(Long userId, String resort){
		ListOperations<String,String> enrollMyQueueTemplate = masterRedisTemplate.opsForList();
		enrollMyQueueTemplate.leftPush(userId+"SubscribeQueue",resort); // subscribe 큐에 추가한다
		enrollMyQueueTemplate.leftPush(resort,userId.toString()); // resort 큐에도 추가 한다.

	}


	public void pubAllEmitter(String userId,String resort){
		//Map<String,SseEmitter> emitters = emitterRepository.finaAllEmitter();
		//Set<String> emitterKeys = emitters.keySet();

		try {

			SseEmitter emitter = emitterRepository.findSingleEmitter(userId);
			if(emitter==null){
				// 해당 유저는 접속하지 않은 상태이므로 SSE통신을 하지 않는다.
				return;
			}
			emitter.send(resort+" new alert!");
				/*
				for(String s:emitterKeys) {
					SseEmitter emitter = emitters.get(s);
					emitter.send(
						SseEmitter.event()
							.data("publish")

					);
				}

				 */
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
			throw new RuntimeException(e.toString());
		}
	}
}
