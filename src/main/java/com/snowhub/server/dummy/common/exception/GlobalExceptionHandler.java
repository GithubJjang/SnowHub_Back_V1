package com.snowhub.server.dummy.common.exception;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.snowhub.server.dummy.common.response.ErrorResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler{

    // validation 오류
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {

        log.error("Validation error occurred", e);

        // message detail 생성
        String detailMessage = createDetailMessage(e.getBindingResult().getFieldErrors());

        ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.BAD_REQUEST, detailMessage);

        return new ResponseEntity<>(errorResponse, ErrorCode.BAD_REQUEST.getHttpStatus());
    }

    // CustomException 발생 시 처리
    @ExceptionHandler(CustomException.class)
    protected ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {

        log.error("CustomException occurred. ErrorCode: {}, Message: {}",
            e.getCodeInterface().getCode(),
            e.getMessage(),
            e);

        ErrorCode errorCode = (ErrorCode)e.getCodeInterface();

        return createErrorResponseEntity(errorCode);
    }
    private ResponseEntity<ErrorResponse> createErrorResponseEntity(ErrorCode errorCode) {

        return new ResponseEntity<>(
            ErrorResponse.of(errorCode),
            errorCode.getHttpStatus());

    }

    private String createDetailMessage(List<FieldError> fieldErrors) {
        return fieldErrors.stream()
            .map(error -> String.format("%s", error.getDefaultMessage()))
            .collect(Collectors.joining(", "));
    }

}
