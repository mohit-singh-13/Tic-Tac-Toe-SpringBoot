package com.tictactoe.auth.service;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlocklistService {

	private static final String BLOCKLIST_PREFIX = "blocklist:";

	private final RedisTemplate<String, String> redisTemplate;

	public void blockToken(String jti, long ttlMillis) {
		String key = BLOCKLIST_PREFIX + jti;

		redisTemplate.opsForValue().set(key, "true", ttlMillis, TimeUnit.MILLISECONDS);
		log.debug("Token blocked: jti={}, ttl={}ms", jti, ttlMillis);
	}

	public boolean isTokenBlocked(String jti) {
		return Boolean.TRUE.equals(redisTemplate.hasKey(BLOCKLIST_PREFIX + jti));
	}
}
