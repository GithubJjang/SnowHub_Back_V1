package com.snowhub.server.dummy.domain.reply.service;

import com.snowhub.server.dummy.common.exception.CustomException;
import com.snowhub.server.dummy.common.exception.ErrorCode;
import com.snowhub.server.dummy.common.response.ApiResult;
import com.snowhub.server.dummy.domain.board.infrastructure.Board;
import com.snowhub.server.dummy.domain.reply.entity.ReplyManager;
import com.snowhub.server.dummy.domain.reply.infrastructure.Reply;
import com.snowhub.server.dummy.domain.reply.infrastructure.ReplyRepository;
import com.snowhub.server.dummy.domain.reply.model.request.ReplyRequest;
import com.snowhub.server.dummy.domain.reply.model.response.ReplyResponse;
import com.snowhub.server.dummy.domain.board.infrastructure.BoardJpaRepository;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@AllArgsConstructor
@Service
public class ReplyService {

    private final ReplyRepository replyRepository;
    private final BoardJpaRepository boardJpaRepository;

    // 1. 댓글 등록
    @Transactional
    public ApiResult<Object> save(ReplyRequest replyRequest,String userName){

        // replyDTO 이용해서 Board 찾기 -> reply 인스턴스에 초기화 -> Reply 등록
        int boardId = Integer.parseInt(replyRequest.getBoardId());

        Board board = boardJpaRepository.findById(boardId).orElseThrow(
                ()-> new CustomException(ErrorCode.BOARD_NOT_FOUND)
        );

        // 1. reply 저장하기
        Reply reply = Reply.from(replyRequest, board,userName);
        replyRepository.save(reply);
        System.out.println("댓글 작성된거 맞아");

        return ApiResult.builder()
            .status(200)
            .code(ErrorCode.SUCCESS)
            .message("댓글을 성공적으로 작성했습니다.")
            .build();
    }

    // 2. Board Id 기반으로 Reply 가져오기
    @Transactional
    public List<ReplyResponse> getReplys(int boardId){

        Board board = boardJpaRepository.findById(boardId).orElseThrow(
                ()-> new NullPointerException("Error:getReply : "+boardId)
        );

        // 댓글이 안달릴수도 있으니까, Optional Null은 제외.
        // board.id를 이용해서 해당 값의 레코드를 찾는 것인데, 인덱스를 쓰는 것보단 풀스캔을 하자.
        // 어차피 정렬하는 것 자체가 불가능하고, 하면 쌉손해.
        ReplyManager replyManager = new ReplyManager();
        List<Reply> results = replyRepository.findRepliesByBoard(board);

        return replyManager.convertToReplyResponses(results);

    }
}

