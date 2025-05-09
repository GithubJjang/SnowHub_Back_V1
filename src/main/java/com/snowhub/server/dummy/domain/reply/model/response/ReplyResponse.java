package com.snowhub.server.dummy.domain.reply.model.response;

import java.sql.Timestamp;

import org.hibernate.annotations.CreationTimestamp;

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
public class ReplyResponse {
	
	private int id;
	private String content;
	private String writer;
	private String state; // live(유지) or dead(삭제)
	
	@CreationTimestamp
	private Timestamp createDate; // 작성일


}
