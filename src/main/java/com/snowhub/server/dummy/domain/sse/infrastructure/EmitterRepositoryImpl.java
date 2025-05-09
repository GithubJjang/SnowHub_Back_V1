package com.snowhub.server.dummy.domain.sse.infrastructure;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.snowhub.server.dummy.domain.sse.infrastructure.EmitterRepository;

import lombok.NoArgsConstructor;

@Repository
@NoArgsConstructor
public class EmitterRepositoryImpl implements EmitterRepository {

	private final Map<String,SseEmitter> emitters = new ConcurrentHashMap<>();
	
	// 연결이 끊어진 emitter들을 삭제하기 위한 자료구조
	private final List<String> emittersToRemove = new ArrayList<>();


	@Override
	public SseEmitter save(String emitterId, SseEmitter sseEmitter) {
		emitters.put(emitterId, sseEmitter);
		return sseEmitter;
	}

	@Override
	public void saveEventCache(String emitterId, Object event) {

	}

	public SseEmitter findSingleEmitter(String userId){
		return emitters.get(userId);
	}

	public Map<String,SseEmitter> finaAllEmitter(){
		return emitters;
	}
	@Override
	public Map<String, SseEmitter> findAllEmitterStartWithByMemberId(String memberId) {
		return null;
	}

	@Override
	public Map<String, Object> findAllEventCacheStartWithByMemberId(String memberId) {
		return null;
	}

	@Override
	public void deleteById(String id) {
		emitters.remove(id);

	}

	@Override
	public void deleteAllEmitterStartWithId(String memberId) {

	}

	@Override
	public void deleteAllEventCacheStartWithId(String memberId) {

	}

	// Sse도 결국 HTTP이고, stateless하기 때문에 연결이 끊어지는 문제가 발생한 것으로 보인다.
	// 클라이언트는 패킷을 전송할 수 없기에, 서버에서 패킷을 전송해서 keepAlive를 유지한다.
	public void sseKeepAlive(){


		Set<String> emitterNames = emitters.keySet();

		// 아무도 연결을 하지 않으면, 보낼 필요도 없다.
		if(emitterNames.isEmpty()){
			System.out.println("emitter에 등록된 사용자가 없습니다.");
			return;
		}

		System.out.println("sseKeepAlive Active & size : "+emitters.size());

		for(String s : emitterNames){

			SseEmitter emitter = emitters.get(s);

			try {
				emitter.send(
					SseEmitter.event()
						.data("") // 단순 빈 패킷만 전송함으로써, 연결 상태만 유지한다.
				)
				;
			} catch (IOException e) {
				// 여기는 브라우저와 연결이 끊어진 emitter들이 발생시키는 오류로, finally 절에서 emitters에 매칭되는 것들을 삭제한다.
				// 나중에 로그를 찍든가 하자.
				emittersToRemove.add(s);
			}

		}

		// 연결이 끊어진 ZombieEmitter가 있다 -> kill (리소스 낭비만 한다.)
		if(!emittersToRemove.isEmpty()){
			for(String s : emittersToRemove){
				emitters.remove(s);
			}
		}


	}
}