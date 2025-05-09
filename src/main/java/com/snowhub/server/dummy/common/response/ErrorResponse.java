package com.snowhub.server.dummy.common.response;

import com.snowhub.server.dummy.common.exception.ErrorCode;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ErrorResponse {

	private int status;
	private ErrorCode code;
	private String message;

	public static ErrorResponse of(ErrorCode code) {
		return new ErrorResponse(code.getHttpStatus().value(), code, code.getMessage());
	}

	public static ErrorResponse of(ErrorCode code, String message) {
		return new ErrorResponse(code.getHttpStatus().value(), code, message);
	}

}
