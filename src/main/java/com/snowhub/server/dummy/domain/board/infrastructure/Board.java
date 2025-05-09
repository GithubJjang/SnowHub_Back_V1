package com.snowhub.server.dummy.domain.board.infrastructure;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

import com.snowhub.server.dummy.common.condition.State;
import com.snowhub.server.dummy.domain.board.entity.BoardEntity;
import com.snowhub.server.dummy.domain.board.model.request.BoardRequest;
import com.snowhub.server.dummy.domain.user.infrastructure.User;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "board")
@Builder
public class Board {
    @Id @GeneratedValue(strategy = GenerationType.AUTO) //IDENTITY로 명시적 선언할 것
    private int id; // 게시글 번호

    // 한명의 user가 여러개의 Board 작성 가능하다.
    @ManyToOne(fetch = FetchType.LAZY) // LAZY LOADING으로 필요할 때만 가져오자
    private User user; // 사용자

    private String title; // 제목
    @Column(columnDefinition = "LONGTEXT" )
    private String content; // 내용

    private String category; // 카테고리

    //@CreationTimestamp
    private Timestamp createDate; // 작성일

    private String writer;

    private int count; // 게시글 조회

    @Enumerated(EnumType.STRING)
    private State state;

    // 1. JpaEntity 만들기
    public static Board from(User user, BoardRequest boardRequest){

        Timestamp now = new Timestamp(System.currentTimeMillis());// 현재시간 밀리초까지 포함

        String username = user.getDisplayName();

        return Board.builder()
            .user(user)
            .title(boardRequest.getTitle())
            .content(boardRequest.getContent())
            .category(boardRequest.getCategory())
            .writer(username)
            .count(0)
            .state(State.Live)
            .createDate(now)
            .build()
            ;
    }

    // 2. BoardEntity 만들기
    public BoardEntity toModel(Board board){
        return BoardEntity.builder()
            .user(user)
            .title(board.getTitle())
            .content(board.getContent())
            .category(board.getCategory())
            .writer(board.user.getDisplayName())
            .count(0)
            .state(State.Live)
            .createDate(board.getCreateDate())
            .build()
            ;

    }

}
