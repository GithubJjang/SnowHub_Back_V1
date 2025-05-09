package com.snowhub.server.dummy.domain.user.infrastructure;

import jakarta.persistence.*;
import lombok.*;

import com.snowhub.server.dummy.domain.role.RoleType;
import com.snowhub.server.dummy.domain.user.entity.UserEntity;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user")
@Entity
public class User {

    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    // 이메일 = 식별가능한 이름. 어차피 중복x
    private String  email;

    // Authentication을 만들기 위함 <- security에서 필수, firebase 등록을 위해서 필수
    private String password;

    //private String phoneNumber;

    @Column(name = "username")
    private String displayName;

    // enum => String으로 자동변환
    // 사용자 역할 식별을 위해서 필요할 듯.
    @Enumerated(EnumType.STRING)
    private RoleType roleType;

    // 연속적인 서비스 이용을 위해서 필수!
    @Column(length = 1000)
    private String refreshToken;

    private String uid;// 로그아웃시 RefeshToken을 만료시키기 위해서 로컬 DB에 따로 저장. 나중에 회원탈퇴시 필요할 듯 firebase에.

    public static User from(UserEntity userEntity){
        return User.builder()
            .email(userEntity.getEmail())
            .password(userEntity.getPassword())
            .displayName(userEntity.getDisplayName())
            .roleType(userEntity.getRoleType())
            .build()
            ;
    }

}
