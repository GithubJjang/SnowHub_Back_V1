package com.snowhub.server.dummy.domain.tmpboard.model.response;

import java.sql.Timestamp;

import com.snowhub.server.dummy.common.condition.State;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class TmpBoardResponse {

    private int id;
    private String title;
    private String content;
    private String category;
    private State state;
    private Timestamp createDate;
}
