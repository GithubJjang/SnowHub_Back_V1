package com.snowhub.server.dummy.domain.comment.infrastructure;

import java.sql.Timestamp;

import org.hibernate.annotations.CreationTimestamp;

import com.snowhub.server.dummy.common.condition.State;
import com.snowhub.server.dummy.domain.comment.model.request.CommentRequest;
import com.snowhub.server.dummy.domain.reply.infrastructure.Reply;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "comment")
@Entity
public class Comment {

    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @ManyToOne // Comment:reply = N:1
    private Reply reply;

    @Column(columnDefinition = "LONGTEXT") // DB에 글자 충분히 저장할 수 있게.
    private String content;

    private String writer;

    @Enumerated(EnumType.STRING)
    private State state;

    @CreationTimestamp
    private Timestamp createDate; // 작성일

    // 1. CommentJpaEntity 생성
    public static Comment from(
        Reply reply,
        CommentRequest commentRequest,
        String username
        ){

        return Comment.builder()
            .reply(reply)
            .content(commentRequest.getContent())
            .writer("user"+username)
            .state(State.Live)
            .build()
            ;

    }

}
