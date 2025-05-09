package com.snowhub.server.dummy.domain.board.entity;

import java.sql.Timestamp;

import com.snowhub.server.dummy.common.condition.State;
import com.snowhub.server.dummy.domain.user.infrastructure.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class BoardEntity {
	private int id; // 게시글 번호
	private User user; // 사용자
	private String title; // 제목
	private String content; // 내용
	private String category; // 카테고리
	private Timestamp createDate; // 작성일
	private String writer;
	private int count; // 게시글 조회
	private State state;
}
