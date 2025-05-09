package com.snowhub.server.dummy.config.redis;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

	@Value("${spring.data.redis.host}")
	private String redisHost;
	@Value("${spring.data.redis.port}")
	private int redisPort;

	private final int redisSlavePort = 6380 ;

	// 1. Host꺼
	@Bean
	@Qualifier("Master_redisConnectionFactory")
	public RedisConnectionFactory redisConnectionFactory(){
		return new LettuceConnectionFactory(redisHost, redisPort);
	}
	@Bean
	@Primary
	@Qualifier("Master_redisTemplate")
	public RedisTemplate<String, String> masterRedisTemplate(@Qualifier("Master_redisConnectionFactory") RedisConnectionFactory connectionFactory) {


		RedisTemplate<String, String> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);

		// 모든 경우
		template.setDefaultSerializer(new StringRedisSerializer());
		return template;


		//return initRedis(connectionFactory);
	}


	// 2. Slave꺼(Read-Only)

	@Bean
	@Qualifier("Slave_redisConnectionFactory")
	public RedisConnectionFactory redisConnectionFactory_Slave(){
		return new LettuceConnectionFactory(redisHost, redisSlavePort);
	}
	@Bean
	@Qualifier("Slave_redisTemplate")
	public RedisTemplate<String, String> slaveRedisTemplate(@Qualifier("Slave_redisConnectionFactory")RedisConnectionFactory connectionFactory) {


		RedisTemplate<String, String> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);

		// 모든 경우
		template.setDefaultSerializer(new StringRedisSerializer());
		return template;


		//return initRedis(connectionFactory);
	}

	// Redis 객체들
	
	// Read-only 빈
	@Bean
	public ListOperations<String, String> readOnlyListOperations(){
		RedisTemplate<String, String> slaveRedisTemplate = slaveRedisTemplate(redisConnectionFactory_Slave());

		return slaveRedisTemplate.opsForList();
	}

	@Bean
	public HashOperations<String,String,String> readOnlyHashOperations(){
		RedisTemplate<String, String> slaveRedisTemplate = slaveRedisTemplate(redisConnectionFactory_Slave());

		return slaveRedisTemplate.opsForHash();
	}

	@Bean
	public SetOperations<String,String> readOnlySetOperations(){
		RedisTemplate<String, String> slaveRedisTemplate = slaveRedisTemplate(redisConnectionFactory_Slave());

		return slaveRedisTemplate.opsForSet();
	}
	
	// Write-only 빈

	@Bean
	@Primary
	public ListOperations<String, String> writeOnlyListOperations(){
		RedisTemplate<String, String> masterRedisTemplate = masterRedisTemplate(redisConnectionFactory());

		return masterRedisTemplate.opsForList();
	}

	@Bean
	@Primary
	public HashOperations<String,String,String> writeOnlyHashOperations(){
		RedisTemplate<String, String> masterRedisTemplate = masterRedisTemplate(redisConnectionFactory());

		return masterRedisTemplate.opsForHash();
	}

	@Bean
	@Primary
	public SetOperations<String,String> writeOnlySetOperations(){
		RedisTemplate<String, String> masterRedisTemplate = masterRedisTemplate(redisConnectionFactory());

		return masterRedisTemplate.opsForSet();
	}

}
/*
/*
	@Bean
	public RedisTemplate<String, String> redisTemplate() {
		RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(redisConnectionFactory());

		// 일반적인 key:value의 경우 시리얼라이저
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(new StringRedisSerializer());

		// Hash를 사용할 경우 시리얼라이저
		redisTemplate.setHashKeySerializer(new StringRedisSerializer());
		redisTemplate.setHashValueSerializer(new StringRedisSerializer());

		// 모든 경우
		redisTemplate.setDefaultSerializer(new StringRedisSerializer());
		return redisTemplate;
	}

*/
