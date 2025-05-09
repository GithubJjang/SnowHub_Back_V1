package com.snowhub.server.dummy.domain.board.model.response;

import com.snowhub.server.dummy.domain.board.infrastructure.Board;
import com.snowhub.server.dummy.domain.reply.model.response.ReplyResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class BoardResponseWithReplies {
    private Board board;
    private List<ReplyResponse> replylist;

}
