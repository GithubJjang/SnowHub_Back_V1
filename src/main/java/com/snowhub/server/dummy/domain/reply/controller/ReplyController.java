package com.snowhub.server.dummy.domain.reply.controller;

import com.snowhub.server.dummy.common.response.ApiResult;
import com.snowhub.server.dummy.domain.reply.model.request.ReplyRequest;
import com.snowhub.server.dummy.domain.reply.service.ReplyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@Tag(name = "댓글 관련 API", description = "/board/detail 이후, 사용자가 현재 보고있는 게시글에 대해서 댓글 작성/")
public class ReplyController {

    private final ReplyService replyService;

    // 1. Reply 등록하기.
    @Operation(summary = "댓글 작성하기", description = "/board/detail 이후, 사용자가 현재 보고있는 게시글에 대한 댓글을 작성/")
    @PostMapping("/api/v1/board/reply")
    public ApiResult<Object> saveReply(
        @RequestBody ReplyRequest replyRequest,
        @AuthenticationPrincipal UserDetails principal){
        System.out.println("댓글 작성하기 api 온거 맞나");

        String userName = principal.getUsername();

        return replyService.save(replyRequest,userName);
    }

}
