package com.snowhub.server.dummy.domain.user.model.request;

import com.snowhub.server.dummy.domain.board.infrastructure.Board;
import com.snowhub.server.dummy.domain.role.RoleType;
import com.snowhub.server.dummy.domain.tmpboard.infrastructure.TmpBoard;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequest {


    private int id;
    private String  email;
    private String password;
    private String displayName;
    private RoleType roleType;
    private String refreshToken;
    private String uid;// 로그아웃시 RefeshToken을 만료시키기 위해서 로컬 DB에 따로 저장. 나중에 회원탈퇴시 필요할 듯 firebase에.
    private List<Board> boardList;
    //private String phoneNumber;
    private TmpBoard tmpBoard;
}
