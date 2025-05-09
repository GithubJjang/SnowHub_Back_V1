package com.snowhub.server.dummy.config.redis.manager;


import java.util.Collections;
import java.util.List;

import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.snowhub.server.dummy.domain.board.infrastructure.Board;
import com.snowhub.server.dummy.domain.board.model.response.BoardResponse;

import io.lettuce.core.api.reactive.RedisReactiveCommands;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class BoardRedisManager {

	// Block과 Non Block 혼용해서 사용하는 이유
	// Netty의 이벤트 스레드는 12(6*2)개 밖에 없음. 따라서 connection이 집중된 곳에 사용할 것
	// 그 외 connection이 적은 곳은 block으로 처리해서, 부하가 큰 곳에만 비동기 처리를 한다.

	// 1. Block I/O
	// Write-Only
	private final ListOperations<String, String> writeOnlyListOperations;
	private final HashOperations<String, String, String> writeOnlyHashOperations;
	private final SetOperations<String, String> writeOnlySetOperations;

	// Read-Only
	private final ListOperations<String, String> readOnlyListOperations;
	private final HashOperations<String, String, String> readOnlyHashOperations;


	// 2. Non Block I/O
	private final RedisReactiveCommands<String, String> slaveRedisReactiveCommands;

	// master
	private final RedisTemplate<String, String> masterRedisTemplate;

	// 3. etc
	// objectMapper
	private final ObjectMapper objectMapper;

	// final field
	private final String key = "board";
	private final int boardFetchSize = 16;


	// 1. 게시글 저장하기
	public void save(Board board){
		// 3. Redis에 캐싱하기.
		// 80을 넘어서면 맨 뒤에꺼 삭제. 아니면 계속 추가.
		try {
			String json = objectMapper.writeValueAsString(board);

			ListOperations<String, String> listOperations = writeOnlyListOperations;//masterRedisTemplate.opsForList();

			listOperations.leftPush(key, json);//최신 데이터는 항상 맨 앞쪽에 집어 넣는다. 맨 뒤에 +1(RPUSH), 맨 앞에 -1(LPOP)

			// board의 id값을 캐싱한다
			if (listOperations.size(key) == 81) {
				// Redis의 list.size가 80보다 크면,
				// INDEX 0 ~ 79번까지 데이터만 살려둔다. = 맨 뒤 오래된 데이터는 제거한다.
				//listOperations.trim(key, 0, 79);

				// 아래는 도퇴가 되어버린 boardlist

				// boardlist 캐싱에서도 비워주고
				String s = listOperations.rightPop(key);
				BoardResponse boardResponse = objectMapper.readValue(s, BoardResponse.class);

				// board의 count를 저장한 것도 날려준다.
				Integer boardId = boardResponse.getId();


				writeOnlyHashOperations.put("board:","count","-1");

				masterRedisTemplate.delete(boardId.toString()); // 80개에 포함되지 않는 해시키를 탈락시킨다.
			}

            /*
                board:1
                  { "user:111" : "111"}
                  { "user:222" : "222"}
                  { "count" : 12 }
                  여기서 먼저 count하고 만들어 준다. 왜냐하면 cachedBoard는 json으로 저장되어 있다.
                  이거 기존 json에 오버라이드 하는 것은 비효율적으로 보인다.
                  count는 따로 부르자.
             */

			// hash를 이용해서 저장
			HashOperations<String, String, String> hashOperations = writeOnlyHashOperations;//masterRedisTemplate.opsForHash();

			// hashKey 만들기
			StringBuilder hashKeyBuilder = new StringBuilder();
			hashKeyBuilder.append("board:");
			hashKeyBuilder.append(board.getId()); // 형변환을 하긴 하는데 신경쓰임.

			String hashKey = hashKeyBuilder.toString();

			hashOperations.put(hashKey, "count", "0");


		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	// 2-1. 0~4페이지에 해당하는 게시글 불러오기
	public Mono<List<BoardResponse>> getBoards(int page){

		if(page<=4){
			// 이 부분만 비동기로 빠르기 처리를 하자.
			return slaveRedisReactiveCommands.lrange(key,0,-1).count() // 비동기
				.flatMap(size ->{

					int start = boardFetchSize * page;
					// if (page <= 4 && size != 0)
					if (size != 0) {

						Mono<List<BoardResponse>> cachedBoards = slaveRedisReactiveCommands.lrange(key, // 비동기
								start, start + boardFetchSize - 1)
							.flatMap(s -> {
								try {
									BoardResponse boardResponse = objectMapper.readValue(s, BoardResponse.class);

									String hashKey = new StringBuilder()
										.append("board:")
										.append(boardResponse.getId())
										.toString();

									// Redis에서 count 비동기 조회 (리액티브)
									return slaveRedisReactiveCommands.hget(hashKey, "count") // 비동기
										//.defaultIfEmpty("0") // count가 없을 경우 기본값
										.flatMap(countStr -> {
											boardResponse.setCount(Integer.parseInt(countStr));
											return Mono.just(boardResponse);
										});

								} catch (JsonProcessingException | NullPointerException e) {
									return Mono.error(e);
								}
							})
							.collectList()
							.map(boardList -> { // 단순 값 변환 -> 동기 ok
								// pageSize 계산 후 마지막 항목 추가
								int getPageSize = Math.toIntExact(80 / 16) + 1;

								BoardResponse pageSizeEntity = new BoardResponse();
								pageSizeEntity.setId(getPageSize);
								pageSizeEntity.setTitle(String.valueOf(getPageSize));

								boardList.add(pageSizeEntity);

								return boardList;
							});

						return cachedBoards;

					}
					else {
						return Mono.just(Collections.emptyList());
					}


				})
				;

		}

		// null을 반환 하면
		throw new RuntimeException("BoardRedisManager.getBoardsFromRedis - emptyList 반환해야 하는데 이거 실행되면 절대 안됨.");
	}

	// 3. 게시글 조회시 방문여부 확인하기.
	public String getUserIdFromBoardHash(
		String hashKey,
		String userKey){
		return writeOnlyHashOperations.get(hashKey, userKey);
	}

	// 4. Redis Set에 board Id 기록하기
	public void recordBoardIdOnce(String boardKey){
		writeOnlySetOperations.add("cachedBoardId",boardKey);
	}

	// 5. User를 board hash에 방문기록 남기기
	public void recordUserInBoardHash(String boardKey, String userKey, String userId){
		writeOnlyHashOperations.put(boardKey, userKey, userId);

	}

	// 6. 캐싱된 80개 싹다 가져오기
	public List<String> getAllCachedBoards(){
		return readOnlyListOperations.range(key, 0, -1);
	}

	// 7. board의 count 가져오기
	public String getBoardCount(int boardId){
		return readOnlyHashOperations.get("board:"+boardId,"count");
	}

	// 8. board의 count 업데이트하기
	public void updateBoardCount(String boardKey, String count){
		writeOnlyHashOperations.put(boardKey, "count", count.toString());// Redis count 업데이트
	}

}
