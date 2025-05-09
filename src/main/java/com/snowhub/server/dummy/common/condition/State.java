package com.snowhub.server.dummy.common.condition;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 게시글의 상태
@Getter
@AllArgsConstructor
public enum State {
	
	Live("live"),Dead("dead");

	private final String description;
}
