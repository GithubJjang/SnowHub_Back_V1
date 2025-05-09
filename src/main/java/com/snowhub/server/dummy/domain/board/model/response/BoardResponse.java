package com.snowhub.server.dummy.domain.board.model.response;

import java.sql.Timestamp;

import com.snowhub.server.dummy.common.condition.State;
import com.snowhub.server.dummy.domain.user.infrastructure.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BoardResponse {

	// 명시적으로 선언을 안하니 @Builder가 안먹힘.
	private Integer id; // 게시글 번호
	private User user; // 사용자
	private String title; // 제목
	private String content; // 내용
	private String category; // 카테고리
	private Timestamp createDate; // 작성일
	private Integer count; // 게시글 조회
	private State state;
	private String writer;

	@Override
	public String toString() {
		return "Board{" +
			"id=" + id +
			", user=" + (user != null ? user.getId() : null) + // 또는 user.getUsername()
			", title='" + title + '\'' +
			", content='" + content  + '\'' +
			", category='" + category + '\'' +
			", createDate=" + createDate +
			", writer='" + writer + '\'' +
			", count=" + count +
			", state=" + state +
			'}';
	}
}
