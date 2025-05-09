package com.snowhub.server.dummy.domain.tmpboard.infrastructure;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import com.snowhub.server.dummy.common.condition.State;
import com.snowhub.server.dummy.domain.tmpboard.entity.TmpBoardEntity;
import com.snowhub.server.dummy.domain.user.infrastructure.User;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


// User만 조회했는데 자꾸 쓸데없이 같이 조회됨 <- 지연로딩할 필요가 있다.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "tmpboard")
@Entity
public class TmpBoard {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @ManyToOne
    private User user;

    private String title;

    @Column(columnDefinition = "LONGTEXT")
    private String content;

    private String category;

    @Enumerated(EnumType.STRING)
    private State state;

    private Timestamp createDate; // 작성일

    public static TmpBoard from(TmpBoardEntity tmpBoardEntity){

        // 현재 시간
        LocalDateTime localDateTime = LocalDateTime.now();
        Timestamp now = Timestamp.valueOf(localDateTime);

        return TmpBoard.builder()
            .user(tmpBoardEntity.getUser())
            .title(tmpBoardEntity.getTitle())
            .content(tmpBoardEntity.getContent())
            .category(tmpBoardEntity.getCategory())
            .state(tmpBoardEntity.getState())
            .createDate(now)
            .build()
            ;
    }

}
