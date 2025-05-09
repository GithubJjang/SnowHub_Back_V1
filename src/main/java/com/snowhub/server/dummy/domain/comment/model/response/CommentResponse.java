package com.snowhub.server.dummy.domain.comment.model.response;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class CommentResponse {

	private int id;
	private String content;
	private String writer;
	private String state;
	private Timestamp createDate; // 작성일
}
