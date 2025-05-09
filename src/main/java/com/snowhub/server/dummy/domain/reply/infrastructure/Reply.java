package com.snowhub.server.dummy.domain.reply.infrastructure;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import com.snowhub.server.dummy.common.condition.State;
import com.snowhub.server.dummy.domain.board.infrastructure.Board;
import com.snowhub.server.dummy.domain.reply.model.request.ReplyRequest;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "reply")
@Builder
public class Reply { // Reply : Board = N : 1

    // 최신순 = 2024-05-22
    // 등록순 = 2024-03-13
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY) // 수정
    private Board board;

    private String content;

    private String writer;

    private Timestamp createDate; // 작성일

    @Enumerated(EnumType.STRING)
    private State state;

    // 1.ReplyJpaEntity 만들기
    public static Reply from(ReplyRequest replyRequest, Board board, String username){

        // 현재 시간
        LocalDateTime localDateTime = LocalDateTime.now();
        Timestamp now = Timestamp.valueOf(localDateTime);

        // username
        String userName = new StringBuilder()
            .append("user")
            .append(username)
            .toString()
            ;

		return Reply.builder()
            .board(board)
            .content(replyRequest.getReply())
            .writer(userName)
            .state(State.Live)
            .createDate(now)
            .build();
    }

}
