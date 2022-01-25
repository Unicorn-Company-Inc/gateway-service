package com.shopicorn.gateway;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import reactor.core.publisher.Mono;

@Configuration
public class RedisRateLimiterConfig {

	@Bean
	public KeyResolver remoteAddrKeyResolver() {
		return exchange -> Mono.just(exchange.getRequest().getRemoteAddress().getHostString());
	}

	@Bean("redisRateLimiter")
	public RedisRateLimiter redisLimiter() {
		return new RedisRateLimiter(2, 10);
	}
}