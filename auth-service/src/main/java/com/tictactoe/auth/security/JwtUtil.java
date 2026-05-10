package com.tictactoe.auth.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import com.tictactoe.auth.config.JwtProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtUtil {

	private final JwtProperties jwtProperties;

	private SecretKey getSigningKey() {
		byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
		return Keys.hmacShaKeyFor(keyBytes);
	}

	public String generateAccessToken(String username, String role) {
		Date now = new Date();
		Date expiry = new Date(now.getTime() + jwtProperties.getAccessTokenExpiration());

		return Jwts.builder().subject(username).id(UUID.randomUUID().toString()).claim("role", role)
				.claim("type", "access").issuedAt(now).expiration(expiry).signWith(getSigningKey()).compact();
	}

	public String generateRefreshToken(String username) {
		Date now = new Date();
		Date expiry = new Date(now.getTime() + jwtProperties.getRefreshTokenExpiration());

		return Jwts.builder().subject(username).id(UUID.randomUUID().toString()).claim("type", "refresh").issuedAt(now)
				.expiration(expiry).signWith(getSigningKey()).compact();
	}

	public boolean validateToken(String token) {
		try {
			Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
			return true;
		} catch (JwtException e) {
			log.warn("JWT validation failed: {}", e.getMessage());
			return false;
		} catch (IllegalArgumentException e) {
			log.warn("JWT token is null or empty: {}", e.getMessage());
			return false;
		}
	}

	private Claims extractAllClaims(String token) {
		return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
	}

	public String extractUsername(String token) {
		return extractAllClaims(token).getSubject();
	}

	public String extractJti(String token) {
		return extractAllClaims(token).getId();
	}

	public String extractRole(String token) {
		return extractAllClaims(token).get("role", String.class);
	}

	public String extractTokenType(String token) {
		return extractAllClaims(token).get("type", String.class);
	}

	public Date extractExpiration(String token) {
		return extractAllClaims(token).getExpiration();
	}
}
