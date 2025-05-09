package com.snowhub.server.dummy.config;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.web.method.HandlerMethod;

import com.snowhub.server.dummy.common.exception.ErrorCode;
import com.snowhub.server.dummy.common.response.ErrorResponse;
import com.snowhub.server.dummy.common.swagger.ApiErrorCode;
import com.snowhub.server.dummy.common.swagger.ExampleHolder;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;


@Configuration
public class SwaggerConfig {

	@Bean
	public OpenAPI openAPI() {
		String jwt = "JWT";
		SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwt);
		Components components = new Components().addSecuritySchemes(jwt, new SecurityScheme()
			.name(jwt)
			.type(SecurityScheme.Type.HTTP)
			.scheme("bearer")
			.bearerFormat("JWT")
		);

		Info customInfo = new Info()
			.title("SnowHub API DOC")
			.version("V1.0")
			.description("SnowHub API 문서");

		return new OpenAPI()
			.components(components)
			.addSecurityItem(securityRequirement)
			.info(customInfo);
	}

	@Bean
	public OperationCustomizer customize() {

		return (Operation operation, HandlerMethod handlerMethod) -> {
			ApiErrorCode apiErrorCode = handlerMethod.getMethodAnnotation(
				ApiErrorCode.class
			);

			// @ApiErrorCode 어노테이션이 붙어있다면
			if (apiErrorCode != null) {
				generateErrorCodeResponseExample(operation, apiErrorCode.value());
			}

			return operation;
		};
	}

	// 여러 개의 에러 응답값 추가
	private void generateErrorCodeResponseExample(Operation operation, ErrorCode[] errorCodes) {

		ApiResponses responses = operation.getResponses();

		// ExampleHolder(에러 응답값) 객체를 만들고 에러 코드별로 그룹화
		Map<Integer, List<ExampleHolder>> statusWithExampleHolders = Arrays.stream(errorCodes)
			.map(
				errorCode -> ExampleHolder.builder()
					.holder(getSwaggerExample(errorCode))
					.code(errorCode.getHttpStatus().value())
					.name(errorCode.name())
					.build()
			)
			.collect(Collectors.groupingBy(ExampleHolder::getCode));

		// ExampleHolders를 ApiResponses에 추가
		addExamplesToResponses(responses, statusWithExampleHolders);
	}

	// ErrorResponse 형태의 예시 객체 생성
	private Example getSwaggerExample(ErrorCode errorCode) {

		ErrorResponse errorResponseDto = ErrorResponse.of(errorCode);
		Example example = new Example();
		example.setValue(errorResponseDto);

		return example;
	}

	// exampleHolder를 ApiResponses에 추가
	private void addExamplesToResponses(
		ApiResponses responses,
		Map<Integer, List<ExampleHolder>> statusWithExampleHolders
	) {

		statusWithExampleHolders.forEach(
			(status, v) -> {
				Content content = new Content();
				MediaType mediaType = new MediaType();
				ApiResponse apiResponse = new ApiResponse();

				v.forEach(
					exampleHolder -> mediaType.addExamples(
						exampleHolder.getName(),
						exampleHolder.getHolder()
					)
				);
				content.addMediaType("application/json", mediaType);
				apiResponse.setContent(content);
				responses.addApiResponse(String.valueOf(status), apiResponse);
			}
		);
	}
}
