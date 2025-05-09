package com.snowhub.server.dummy.common.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

	private final CodeInterface codeInterface;

	public CustomException(CodeInterface errorCode) {
		this.codeInterface = errorCode;
	}

	public CustomException(CodeInterface errorCode,String message ) {
		super(message);
		this.codeInterface = errorCode;
	}

}
