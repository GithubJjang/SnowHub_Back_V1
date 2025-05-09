package com.snowhub.server.dummy.config.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.reactive.RedisReactiveCommands;

@Configuration
public class ReactiveRedisConfig {
	@Primary
	@Bean
	public RedisReactiveCommands<String, String> masterRedisReactiveCommands() {
		// Redis 서버 연결 (예: localhost:6379)
		RedisClient redisClient = RedisClient.create("redis://localhost:6379");

		// 비동기 연결 생성
		StatefulRedisConnection<String, String> connection = redisClient.connect();

		// RedisReactiveCommands 객체 반환
		return connection.reactive();
	}

	@Bean
	public RedisReactiveCommands<String, String> slaveRedisReactiveCommands() {
		// Redis 서버 연결 (예: localhost:6379)
		RedisClient redisClient = RedisClient.create("redis://localhost:6380");

		// 비동기 연결 생성
		StatefulRedisConnection<String, String> connection = redisClient.connect();

		// RedisReactiveCommands 객체 반환
		return connection.reactive();
	}



}
