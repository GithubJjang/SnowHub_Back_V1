package com.snowhub.server.dummy.common.swagger;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.snowhub.server.dummy.common.exception.ErrorCode;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiErrorCode {

	ErrorCode[] value();
}