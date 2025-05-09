package com.snowhub.server.dummy.domain.comment.model.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentRequest {
    private int id;
    private String content;
}
