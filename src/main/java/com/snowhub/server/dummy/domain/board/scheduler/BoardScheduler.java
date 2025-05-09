package com.snowhub.server.dummy.domain.board.scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.snowhub.server.dummy.domain.board.model.response.BoardResponse;

import lombok.RequiredArgsConstructor;

// 이거는 분리해 COUNT는 5분마다 USER는 24시간마다 FLUSH를 해줘야한다.
@RequiredArgsConstructor
@Component
public class BoardScheduler {
	
	// read-Only
	private final SetOperations<String,String> readOnlySetOperations;
	private final ListOperations<String,String> readOnlyListOperations;
	private final HashOperations<String,String,String> readOnlyHashOperations;



	// write-Only
	private final RedisTemplate<String, String> masterRedisTemplate;

	//
	private final HashOperations<String,String,String> writeOnlyHashOperations;

	//
	private final ObjectMapper objectMapper;


	// 24시간마다 중복된 user를 0으로 refresh하되, 80개에 해당하는 캐싱된 데이터만 살린다.
	@Scheduled(fixedDelay = 60000)
	public void refreshUserCount() {

		System.out.println("Board Scheduler operate");

		// 1.캐싱된 boardId 가져오기
		Set<String> cachedBoardIds = readOnlySetOperations.members("cachedBoardId");


		// 2. caching된 boardlist 가져오기
		List<String> cachedBoardLists = readOnlyListOperations.range("board",0,-1);

		// 이걸로 다시 4.에 채워넣는다.
		List<String> hashKeyList = new ArrayList<>();
		List<String> hashKeyCountList = new ArrayList<>();

		// 반복문을 돌면서
		try{
			if(!cachedBoardLists.isEmpty()) {
				for (String s : cachedBoardLists) {
					BoardResponse boardResponse = objectMapper.readValue(s, BoardResponse.class); // Redis에서 가져온다.

					String boardId = boardResponse.getId().toString();

					// 1.
					String hashKey = new StringBuilder()
						.append("board:")
						.append(boardId)
						.toString();

					// 80개 hashkey를 따로 저장한다. 추후 hashkey:{count : 100} 이런식으로 보존하기 위해서.(캐싱 boardList 전용)
					hashKeyList.add(hashKey);

					// 2.
					String boardCount = readOnlyHashOperations.get(hashKey, "count");
					hashKeyCountList.add(boardCount);

					// board:1에 해당하는 count를 가져와서 "순서대로" 저장을 한다.
				}

				// 3. caching된 hash 전부 flushall( 사용자 전부 refresh하기 위해서. )
				for (String s : cachedBoardIds) {
					masterRedisTemplate.delete(s);
				}

				// 4. 캐싱된 80개 게시글의 hash 다시 생성하기.( user를 포함해서 통째로 날려서 다시 생성을 해야 count를 가져올 수 있다.)

				int size = hashKeyList.size();

				for (int i = 0; i < size; i++) {
					String hashKey = hashKeyList.get(i);
					String count = hashKeyCountList.get(i);

					writeOnlyHashOperations.put(hashKey, "count", count);

				}
				// 5. 정상적으로 위 과정이 완료되면, cachedBoardId 싹다 날림
				masterRedisTemplate.delete("cachedBoardId");
			}
			
			// 사람들이 조회를 하지 않아서, 캐싱된 것이 없다면 그냥 넘어가자

		}
		catch (NullPointerException e){
			throw new NullPointerException(e.getMessage());

		}
		catch (JsonProcessingException e){
			throw new RuntimeException(e.getMessage());
		}
		catch (Exception e){
			// 기타 에러들
			throw new RuntimeException(e);
		}


	}




}
