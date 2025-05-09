package com.snowhub.server.dummy.domain.tmpboard.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TmpBoardRequest {
    private int id;
    private String title;
    private String content;
    private String category;
}
