package com.snowhub.server.dummy.domain.board.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoardUpdateDto {

	@NotNull(message = "id는 널이어서는 안됩니다.")
	private Integer id;
	// 오류 감지기
	@NotBlank(message = "Title is Empty.")
	private String title;
	@NotBlank(message = "Content is Empty.")
	private String content;
	@NotBlank(message = "Choose Category.")
	private String category;
}
