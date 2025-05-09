package com.snowhub.server.dummy.domain.board.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import com.snowhub.server.dummy.common.condition.State;
import com.snowhub.server.dummy.common.exception.CustomException;
import com.snowhub.server.dummy.common.exception.ErrorCode;
import com.snowhub.server.dummy.common.response.ApiResult;
import com.snowhub.server.dummy.config.redis.manager.BoardRedisManager;
import com.snowhub.server.dummy.domain.board.entity.BoardEntity;
import com.snowhub.server.dummy.domain.board.entity.BoardManager;
import com.snowhub.server.dummy.domain.board.infrastructure.Board;
import com.snowhub.server.dummy.domain.board.infrastructure.BoardRepository;
import com.snowhub.server.dummy.domain.board.model.request.BoardRequest;
import com.snowhub.server.dummy.domain.board.model.request.BoardUpdateDto;
import com.snowhub.server.dummy.domain.oauth.firebase.FirebaseAuthTokenManager;
import com.snowhub.server.dummy.domain.board.model.response.BoardResponseWithReplies;
import com.snowhub.server.dummy.domain.board.model.response.BoardResponse;

import com.snowhub.server.dummy.domain.reply.model.response.ReplyResponse;
import com.snowhub.server.dummy.domain.user.infrastructure.User;
import com.snowhub.server.dummy.domain.user.infrastructure.UserJpaRepository;

import com.snowhub.server.dummy.domain.reply.service.ReplyService;
import com.snowhub.server.dummy.domain.user.infrastructure.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import java.util.List;
import java.util.Optional;

import static com.snowhub.server.dummy.domain.board.infrastructure.QBoard.*;
import static com.snowhub.server.dummy.domain.user.infrastructure.QUser.user;

@Slf4j
@RequiredArgsConstructor
@Service
public class BoardService {


    private final FirebaseAuthTokenManager firebaseAuthTokenManager;

    private final BoardRedisManager boardRedisManager;

    private final BoardRepository boardRepository;

    private final UserRepository userRepository; // userRepo는 외부 빈 -> 순환참조 x

    private final JPAQueryFactory jpaQueryFactory;

    private final ReplyService replyService;

    private final ObjectMapper objectMapper;


    @Transactional
    public ApiResult<Object> save(BoardRequest boardRequest, HttpServletRequest request) {

        // 1. 토큰에서 추출한 이메일로 사용자를 찾는다.
        String userEmail = firebaseAuthTokenManager.getEmail(request);

        User user = Optional.ofNullable(userRepository.findByEmail(userEmail)).orElseThrow(
            () -> new CustomException(ErrorCode.USER_NOT_FOUND) // //new NullPointerException("The user is not registered!")
        );

        // 2. Board DB에 등록하기.(이렇게 해야지 board의 id를 가져올 수 있음.)
        Board board = Board.from(user,boardRequest);
        boardRepository.save(board); // 여기서 트랜잭션을 실패할 경우 롤백을 한다.

        // Board의 데이터 형식
        // {id=752, user=1, title='0411', content='<p>이건 진짜 최신글 입니다.</p>', category='Review', createDate=2025-04-11 19:27:55.389, writer='Son', count=0, state=Live}

        // 3. Redis에 캐싱하기.
        boardRedisManager.save(board);

        return ApiResult.builder()
            .status(200)
            .code(ErrorCode.SUCCESS)
            .message("게시글을 성공적으로 작성했습니다.")
            .build();

    }

    // 게시글 삭제 -> 더티 체킹으로 soft-delete를 하자!
    // Dirty-Checking
    @Transactional
    public ApiResult<Object> deleteBoard(String userId,int boardId){
        User user = userRepository.findById(Integer.valueOf(userId));

        System.out.println("userId: "+user.getId());
        // board가 없는 것에 대한 예외 처리
        Board board = boardRepository.findSpecificBoard(user.getId(),boardId);
        System.out.println(board.getContent());

        board.setState(State.Dead);

        return ApiResult.builder()
            .status(200)
            .code(ErrorCode.SUCCESS)
            .message("게시글을 성공적으로 삭제했습니다.")
            .build();
    }

    // 최신글을 맨 뒤에 넣는 경우
    //result = listOperations.range(key,
    //(cacheSize - 1) - page * dataFetchSize - (dataFetchSize - 1), (cacheSize - 1) - page * dataFetchSize);

    // 전체 게시글 불러오기
    public Mono<List<BoardResponse>> getBoardsByPage(String category, int page) {


        Mono<List<BoardResponse>> boards = null;


        if(page<=4){
            boards = boardRedisManager.getBoards(page);

            boards = boards
                .filter(list -> !list.isEmpty()) // 리스트가 비어 있지 않으면 진행
                .switchIfEmpty(Mono.defer(() -> {
                    // 리스트가 비어있을 경우 처리
                    return null;
                }));

        }


        if(boards!=null){
            return boards;

        }

        // page가 4보다 크거나 boards가 emptyList인 경우 DB에서 가져온다.
        List<BoardEntity> boardEntities = //boardRepository.pagination(category, page)
            boardRepository.findBoardsPaged(page)
            .stream()
            .map(
                e -> e.toModel(e)
            )
            .toList();

        BoardManager boardManager = new BoardManager();
        List<BoardResponse> fetchedBoards = boardManager.convertToBoardResponses(boardEntities);


        // JPA에 의존적인 코드
        // Querydsl을 이용한 DTO로 값을 받는 경우, user엔티티의 username 추출하기가 매우 번거롭다. <- 추후 고려
        /*
        long getBoardSize = jpaQueryFactory
            .select(board.id.count())
            .from(board)
            .where(categoryEq(category))
            .fetchOne();

         */
        Long getBoardSize = 0L;//boardRepository.countByCategory(category);

        if(category.equals("all")){
            getBoardSize = boardRepository.countAll();
        }
        else{
            getBoardSize = boardRepository.countByCategory(category);
        }

        int getPageSize = Math.toIntExact(getBoardSize / 16) + 1;

        BoardResponse pageSizeEntity = new BoardResponse();
        pageSizeEntity.setId(Math.toIntExact(getPageSize));
        pageSizeEntity.setTitle(getPageSize + "");

        fetchedBoards.add(pageSizeEntity);

        return Mono.just(fetchedBoards);

    }

    private BooleanExpression categoryEq(String categoryCond) {

        return categoryCond.equals("all") ? null : board.category.eq(categoryCond);
    }

    public BoardResponseWithReplies getBoard(int boardId, HttpServletRequest request) { // 리팩토링 Integer boardId -> int

        // 1. 게시글 가져오기
        // board를 1건으로 줄인 후, user와 join
        Board findBoard = jpaQueryFactory.selectFrom(board) // select board 쿼리
            .join(board.user, user).fetchJoin()
            .where(board.id.eq(boardId))
            .fetchOne();

        // 2.
        String email = firebaseAuthTokenManager.getEmail(request);

        User findUser = Optional.ofNullable(userRepository.findByEmail(email)).orElseThrow( // select user 쿼리
            () -> new NullPointerException("The user is not registered!")
        );

        Integer userId = findUser.getId();// 사용자 id, int처럼 원시타입을 쓸 경우 toString을 할 수 없다. 원시타입을 쓰는 이유는?

        // hashKey 만들기
		String boardKey =
            "board:"
			+ boardId // 형변환을 하긴 하는데 신경쓰임.
			;

        // userKey 만들기
		String userKey =
            "user:"
			+ userId
            ;

        // Redis에 user가 캐싱되었는지 확인해보기
        String cachedUserId = boardRedisManager.getUserIdFromBoardHash(boardKey,userKey);

        // 추가, Redis에 게시글 번호 Set으로 등록하기. 왜냐하면, 나중에 사용자 refresh를 하기 위함임.
        //writeOnlySetOperations.add("cachedBoardId",boardKey);
        boardRedisManager.recordBoardIdOnce(boardKey);

        Integer count = 0;
        /*
        board:1
          { "user:홍길동",
            "user:고길동",
            "count":12 }
          count는 1분마다 flush를 한다. 왜냐하면 DB에 직접 자주 업데이트를 할 경수 DISK I/O가 자주 발생한다.
          ...
         */

        if (cachedUserId == null) {// 처음 게시글에 방문을 한다. -> 중복 조회를 방지하기 위해서 Redis에 등록을 한다.
                // 사용자 캐싱하기(공통)
                //hashOperations.put(boardKey, userKey, userId.toString());
                boardRedisManager.recordUserInBoardHash(boardKey,userKey,userId.toString());

                // 80개 귀속되나 확인
                //ListOperations<String, String> listOperations = readOnlyListOperations;

                // 이부분 조금 더 빨리 가져오기
                //List<String> fetchedBoardByRedis = listOperations.range(key, 0, -1);
                List<String> fetchedBoardByRedis = boardRedisManager.getAllCachedBoards();

                boolean isCached = false;

                try {
                    // 1. 만약 80개랑 매칭이 되면
                    for (String s : fetchedBoardByRedis) {
                        BoardResponse boardResponse = objectMapper.readValue(s, BoardResponse.class);
                        if (boardResponse.getId() == boardId) { // 만약 80개랑 매칭이 되면, redis에 기록한다.
                            isCached = true;

                            //String countString = readOnlyHashOperations.get("board:"+boardId,"count");
                            String countString = boardRedisManager.getBoardCount(boardId);

                            count = Integer.valueOf(countString) + 1 ;
                            
                            updateCount(boardId, count);// DB에 count 업데이트

                            //hashOperations.put(boardKey, "count", count.toString());// Redis count 업데이트
                            boardRedisManager.updateBoardCount(boardKey,count.toString());
                            break;
                        }
                    }

                    // DB에 업데이트
                } catch (Exception e) {
                    // 이 부분은 해결 불가능하며, 운영자에게 필요한 에러이다. 따라서 slack이나 discord로 알림 메시지를 전송한다.
                    throw new RuntimeException(e);
                }

                // 2. 만약 80개 안에 안 들어가는 글이더라 -> count + 1. DB만 업데이트
                if (!isCached) {
                    updateCount(boardId, findBoard.getCount()+1);
                }
        }

        List<ReplyResponse> getReplies = replyService.getReplys(boardId); // // select reply 쿼리

        // Board와 Reply를 동시에 반환을 한다.
        return BoardResponseWithReplies.builder()
            .board(findBoard)
            .replylist(getReplies)
            .build();
    }

    @Transactional
    public void updateCount(int boardId,int count){
        // board 가져오기 -> 원본을 변경 -> orig와 변경된 board 서로 compare -> Dirty Checking에 의해서 자동 update
        Board board = boardRepository.findById(boardId);
        board.setCount(count);
    }

    public List<Board> getMyBoards(int userId,Integer cursorId){

        List<Board> boardJpaEntities = null;

        if(cursorId==null){
            // 최초 요청
            boardJpaEntities = boardRepository.findTop10ByOrderByIdDesc(userId);
        }
        else {
            boardJpaEntities =  boardRepository.findNext10ByIdLessThan(userId,cursorId);
        }

        return boardJpaEntities;

    }

    // update를 위해서 내가 작성한 게시글 가져오기
    public ResponseEntity<Object> updateMyBoard(int boardId){
        Board board = boardRepository.findById(boardId);
        return ResponseEntity.ok(board);
    }

    // update를 위해서 내가 작성한 게시글 가져오기
    @Transactional
    public ResponseEntity<Object> dirtyChecking(BoardUpdateDto boardUpdateDto){
        int boardId = boardUpdateDto.getId();

        Board board = boardRepository.findById(boardId);

        LocalDateTime now = LocalDateTime.now();
        Timestamp timestamp = Timestamp.valueOf(now);

        board.setTitle(boardUpdateDto.getTitle());
        board.setContent(boardUpdateDto.getContent());
        board.setCategory(boardUpdateDto.getCategory());
        board.setCreateDate(timestamp);

        return ResponseEntity.ok("게시글을 성공적으로 수정했습니다.");
    }
}
