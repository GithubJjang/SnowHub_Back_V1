package com.snowhub.server.dummy.domain.tmpboard.service;

import com.snowhub.server.dummy.common.condition.State;
import com.snowhub.server.dummy.common.exception.CustomException;
import com.snowhub.server.dummy.common.exception.ErrorCode;
import com.snowhub.server.dummy.common.response.ApiResult;
import com.snowhub.server.dummy.domain.oauth.firebase.FirebaseAuthTokenManager;
import com.snowhub.server.dummy.domain.tmpboard.infrastructure.TmpBoard;
import com.snowhub.server.dummy.domain.tmpboard.entity.TmpBoardMananger;
import com.snowhub.server.dummy.domain.tmpboard.entity.TmpBoardEntity;
import com.snowhub.server.dummy.domain.tmpboard.infrastructure.TmpBoardRepository;
import com.snowhub.server.dummy.domain.tmpboard.model.request.TmpBoardRequest;
import com.snowhub.server.dummy.domain.tmpboard.model.response.TmpBoardResponse;
import com.snowhub.server.dummy.domain.user.infrastructure.User;
//import com.snowhub.server.dummy.domain.oauth.firebase.dummy.Firebase;
import com.snowhub.server.dummy.domain.user.infrastructure.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
public class TmpBoardService {

    private final FirebaseAuthTokenManager firebaseAuthTokenManager;

    private final UserRepository userRepository;
    private final TmpBoardRepository tmpBoardRepository; // 생성자 주입을 해야하는 이유: 컴파일때 오류를 찾을 수 있음.

    @Transactional
    public ApiResult<Object> save(TmpBoardRequest tmpBoardRequest, HttpServletRequest request){ // 임시 게시글 저장을 한다.

        String email = firebaseAuthTokenManager.getEmail(request);

        User user = Optional.ofNullable(userRepository.findByEmail(email)).orElseThrow(
                ()-> new CustomException(ErrorCode.USER_NOT_FOUND)
        );

        TmpBoardEntity tmpBoardEntity = TmpBoardEntity.toEntity(tmpBoardRequest, user);

        // 1. 새로 작성된 임시 게시글
        TmpBoard tmpBoard = TmpBoard.from(tmpBoardEntity);

        tmpBoardRepository.save(tmpBoard);

        return ApiResult.builder()
            .status(200)
            .code(ErrorCode.SUCCESS)
            .message("현재 작성중인 게시글을 임시저장 했습니다.")
            .build();
    }

    @Transactional
    public ApiResult<TmpBoard> getTmpBoard(HttpServletRequest request){

        String email = firebaseAuthTokenManager.getEmail(request);

        User user = Optional.ofNullable(userRepository.findByEmail(email)).orElseThrow(
                ()-> new CustomException(ErrorCode.USER_NOT_FOUND)
        );

        TmpBoard tmpBoard = tmpBoardRepository.findLatestByUserId(user.getId());

		return ApiResult.<TmpBoard>builder()
            .status(200)
            .code(ErrorCode.SUCCESS)
            .data(tmpBoard)
            .message("최신 임시 저장글을 불러왔습니다.")
            .build()
            ;
    }

    public List<TmpBoardResponse> getMyTmpBoards(int userId,Integer cursorId){

        List<Object[]> tmpBoards;


        if(cursorId==null){
            // 최초 요청
            tmpBoards = tmpBoardRepository.findTop10ByOrderByIdDesc(userId);
        }
        else {
            tmpBoards =  tmpBoardRepository.findNext10ByIdLessThan(userId,cursorId);
        }

        // DTO로 변경하기.
        TmpBoardMananger tmpBoardMananger = new TmpBoardMananger();

		return tmpBoardMananger.converToDto(tmpBoards);

    }

    // 더티체킹을 통한 update 수정이 요구됨.
    @Transactional
    public ResponseEntity<Object> updateMyTmpBoards(TmpBoardRequest tmpBoardRequest){

        int boardId = tmpBoardRequest.getId();

        // JPA 전용 트랜잭션 스크립트 - 더티 체킹
        TmpBoard tmpBoard = tmpBoardRepository.findById(boardId);

        tmpBoard.setTitle(tmpBoardRequest.getTitle());
        tmpBoard.setContent(tmpBoardRequest.getContent());
        tmpBoard.setCategory(tmpBoardRequest.getCategory());

        return ResponseEntity.ok("성공적으로 임시 게시글을 저장했습니다.");

    }

    // 더티 체킹을 통한 soft delete
    @Transactional
    public ApiResult<Object> deleteTmpBoard(int boardId){

        // board가 없는 것에 대한 예외 처리
        TmpBoard tmpBoard = tmpBoardRepository.findById(boardId);

        // JPA 전용 트랜잭션 스크립트 - 더티 체킹ㄴ
        tmpBoard.setState(State.Dead);
        return ApiResult.builder()
            .status(200)
            .code(ErrorCode.SUCCESS)
            .message("게시글을 성공적으로 삭제했습니다.")
            .build();
    }

    public ResponseEntity<Object> getMyTmpBoard(int boardId){
        TmpBoard tmpBoard = tmpBoardRepository.findById(boardId);

        TmpBoardMananger tmpBoardMananger = new TmpBoardMananger();

        TmpBoardResponse tmpBoardResponse = tmpBoardMananger.converToDto(tmpBoard);
        return ResponseEntity.ok(tmpBoardResponse);
    }
}
