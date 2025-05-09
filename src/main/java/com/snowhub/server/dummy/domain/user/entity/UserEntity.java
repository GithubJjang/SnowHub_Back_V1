package com.snowhub.server.dummy.domain.user.entity;

import com.snowhub.server.dummy.domain.role.RoleType;
import com.snowhub.server.dummy.domain.user.model.request.UserRequest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class UserEntity {
	private int id;
	private String  email;
	private String password;
	private String displayName;
	private RoleType roleType;
	private String refreshToken;
	private String uid;// 로그아웃시 RefeshToken을 만료시키기 위해서 로컬 DB에 따로 저장. 나중에 회원탈퇴시 필요할 듯 firebase에.

	// UserEntity 생성 로직을 여기서 처리한다.
	public static UserEntity toEntity(UserRequest userRequest){
		return UserEntity.builder()
			.email(userRequest.getEmail())
			.password(userRequest.getPassword())
			.displayName(userRequest.getDisplayName())
			.roleType(userRequest.getRoleType())
			.build()
			;


	}
}
