package com.snowhub.server.dummy.domain.reply.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReplyRequest {

    // 게시글 고유 넘버.
    private String boardId;

    @NotBlank(message = "The reply is Empty")
    private String reply;
}
