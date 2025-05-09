package com.snowhub.server.dummy.domain.reply.entity;

import java.util.List;

import com.snowhub.server.dummy.domain.reply.infrastructure.Reply;
import com.snowhub.server.dummy.domain.reply.model.response.ReplyResponse;

public class ReplyManager {

	// Jpa에 의존적 추후 수정을 원한다면, 데이터 타입을 List<Object[]>로 바꿔야 한다.
	public List<ReplyResponse> convertToReplyResponses(List<Reply> replyJpaEntities){

		return replyJpaEntities.stream()
			.map((e)->
				ReplyResponse.builder()
					.id(e.getId())
					.content(e.getContent())
					.writer(e.getWriter())
					.createDate(e.getCreateDate())
					.state(e.getState().getDescription())
					.build()
			)
			.toList()
			;


	}
}
