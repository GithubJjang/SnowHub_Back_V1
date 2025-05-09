package com.snowhub.server.dummy.domain.comment.entity;

import java.util.List;

import com.snowhub.server.dummy.domain.comment.infrastructure.Comment;
import com.snowhub.server.dummy.domain.comment.model.response.CommentResponse;

public class CommentManager {
	
	// Jpa에 의존적
	public List<CommentResponse> getCommentsByReply(List<Comment> commentJpaEntities){
		return commentJpaEntities.stream()
			.map((e)->

				CommentResponse.builder()
					.id(e.getId())
					.content(e.getContent())
					.writer(e.getWriter())
					.state(e.getState().toString())
					.createDate(e.getCreateDate())
					.build()

			)
			.toList()
		;

	}
}
