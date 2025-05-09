package com.snowhub.server.dummy.domain.comment.controller;

import com.snowhub.server.dummy.common.response.ApiResult;
import com.snowhub.server.dummy.domain.comment.model.request.CommentRequest;
import com.snowhub.server.dummy.domain.comment.model.response.CommentResponse;
import com.snowhub.server.dummy.domain.reply.infrastructure.Reply;
import com.snowhub.server.dummy.domain.comment.service.CommentService;
import com.snowhub.server.dummy.domain.reply.infrastructure.ReplyRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@AllArgsConstructor
@RestController
@Tag(name = "답글 관련 API", description = "/board/detail 이후, 사용자가 현재 보고있는 게시글의 댓글에 대해서 답글 작성/불러오기/")
public class CommentController {


    private final ReplyRepository replyRepository;// 런타임 전에 오류를 잡기 위해서, 생성자 의존성 주입을 한다.
    private final CommentService commentService;

    @Operation(summary = "답글 작성하기", description = "하나의 댓글에 대한 하나의 답글을 작성합니다.")
    @PostMapping("/api/v1/board/comment")
    public ApiResult<Object> getComment(@RequestParam(name = "id") int replyId,
                                        @RequestBody CommentRequest commentRequest,
                                        @AuthenticationPrincipal UserDetails principal){

        String username = principal.getUsername();

        return commentService.save(commentRequest,replyId,username);

    }

    @Operation(summary = "답글 불러오기", description = "하나의 댓글에 대한 모든 답글을 불러옵니다.")
    @GetMapping("/api/v1/board/reply/comment")
    public ApiResult<List<CommentResponse>> getComment(
        @RequestParam(name = "id")int replyId
    ){
        Reply reply = replyRepository.findById(replyId);

        return commentService.getComment(reply);
    }

}
