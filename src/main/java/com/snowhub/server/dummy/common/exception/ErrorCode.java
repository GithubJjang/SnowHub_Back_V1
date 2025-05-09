package com.snowhub.server.dummy.common.exception;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode implements CodeInterface{

	// API 요청 에러
	SUCCESS(200, HttpStatus.OK, "SUCCESS"),

	BAD_REQUEST(400, HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),

	// USER 관련 에러
	USER_NOT_FOUND(401,HttpStatus.NOT_FOUND,"해당 사용자는 존재하지 않습니다."),
	USER_EMAIL_NOT_FOUND(401,HttpStatus.NOT_FOUND,"해당 사용자의 email 정보가 불일치 합니다."),

	// BOARD 관련 에러
	BOARD_NOT_FOUND(404,HttpStatus.NOT_FOUND,"해당 게시글은 존재하지 않습니다"),

	// REPLY 관련 에러
	REPLY_NOT_FOUND(404,HttpStatus.NOT_FOUND,"하당 댓글은 존재하지 않습니다.");


	private final Integer code;
	private final HttpStatus httpStatus;
	private final String message;
}
