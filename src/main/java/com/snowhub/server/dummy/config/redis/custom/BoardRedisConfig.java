package com.snowhub.server.dummy.config.redis.custom;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.snowhub.server.dummy.config.redis.manager.BoardRedisManager;

import io.lettuce.core.api.reactive.RedisReactiveCommands;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Configuration
public class BoardRedisConfig {

	// 1. Block I/O
	// Write-Only
	private final ListOperations<String, String> writeOnlyListOperations;
	private final HashOperations<String, String, String> writeOnlyHashOperations;
	private final SetOperations<String, String> writeOnlySetOperations;

	// Read-Only
	private final ListOperations<String, String> readOnlyListOperations;
	private final HashOperations<String, String, String> readOnlyHashOperations;


	// 2. Non Block I/O
	private final RedisReactiveCommands<String, String> slaveRedisReactiveCommands;

	// master
	private final RedisTemplate<String, String> masterRedisTemplate;

	// 3. etc
	// objectMapper
	private final ObjectMapper objectMapper;

	@Bean
	public BoardRedisManager boardRedisManager(){

		return new BoardRedisManager(
			writeOnlyListOperations,
			writeOnlyHashOperations,
			writeOnlySetOperations,
			readOnlyListOperations,
			readOnlyHashOperations,
			slaveRedisReactiveCommands,
			masterRedisTemplate,
			objectMapper
		);

	}
}
