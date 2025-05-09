package com.snowhub.server.dummy.domain.tmpboard.entity;

import java.sql.Timestamp;

import com.snowhub.server.dummy.common.condition.State;
import com.snowhub.server.dummy.domain.tmpboard.model.request.TmpBoardRequest;
import com.snowhub.server.dummy.domain.user.infrastructure.User;

import io.swagger.v3.oas.annotations.servers.Server;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Server
@AllArgsConstructor
@Builder
public class TmpBoardEntity {
	private int id;
	private User user;
	private String title;
	private String content;
	private String category;
	private State state;
	private Timestamp createDate; // 작성일

	// 1. TmpBoardRequest - > TmpBoard
	public static TmpBoardEntity toEntity(
		TmpBoardRequest tmpBoardRequest,
		User user){
		// 1. 새로 작성된 임시 게시글
		return TmpBoardEntity.builder()
			.user(user)
			.title(tmpBoardRequest.getTitle())
			.content(tmpBoardRequest.getContent())
			.category(tmpBoardRequest.getCategory())
			.state(State.Live)
			.build()
			;
	}

	// 2. TmpBoardUpdate

}
