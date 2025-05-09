package com.snowhub.server.dummy.domain.comment.service;


import com.snowhub.server.dummy.common.exception.CustomException;
import com.snowhub.server.dummy.common.exception.ErrorCode;
import com.snowhub.server.dummy.common.response.ApiResult;
import com.snowhub.server.dummy.domain.comment.entity.CommentManager;
import com.snowhub.server.dummy.domain.comment.infrastructure.Comment;
import com.snowhub.server.dummy.domain.comment.infrastructure.CommentRepository;
import com.snowhub.server.dummy.domain.comment.model.request.CommentRequest;
import com.snowhub.server.dummy.domain.comment.model.response.CommentResponse;
import com.snowhub.server.dummy.domain.reply.infrastructure.Reply;

import com.snowhub.server.dummy.domain.reply.infrastructure.ReplyJpaRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class CommentService {
    private final ReplyJpaRepository replyJpaRepository;
    private final CommentRepository commentRepository;

    // 1.Comment등록하기
    @Transactional
    public ApiResult<Object> save(
        CommentRequest commentRequest,
        int replyId,
        String username){
        // save comment, dirty checking about reply

        // Reply 찾기
        Reply reply = replyJpaRepository.findById(replyId).orElseThrow(
                ()-> new CustomException(ErrorCode.REPLY_NOT_FOUND)
        );

        // CommentJpaEntity만들기
        Comment comment = Comment.from(reply,commentRequest,username);
        System.out.println("comment 작성");
        commentRepository.save(comment);

        return ApiResult.builder()
            .status(200)
            .code(ErrorCode.SUCCESS)
            .message("답글을 성공적으로 작성했습니다.")
            .build();

    }

    // 2. 특정 reply에 대한 comment 가져오기
    public ApiResult<List<CommentResponse>> getComment(Reply reply){

        CommentManager commentManager = new CommentManager();

        List<Comment> commentJpaEntities = commentRepository.findByReplyJpaEntity(reply);

        // Jpa에 의존적이다.
        List<CommentResponse> commentResponses = commentManager.getCommentsByReply(commentJpaEntities);

        return ApiResult.<List<CommentResponse>>builder()
            .status(200)
            .code(ErrorCode.SUCCESS)
            .data(commentResponses)
            .message("답글을 성공적으로 작성했습니다.")
            .build();


    }
}
