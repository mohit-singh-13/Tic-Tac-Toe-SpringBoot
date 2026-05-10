package com.tictactoe.auth.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.tictactoe.auth.dto.AuthResponse;
import com.tictactoe.auth.dto.LoginRequest;
import com.tictactoe.auth.dto.RegisterRequest;
import com.tictactoe.auth.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AuthController {

	private static final String REFRESH_TOKEN_COOKIE = "refresh_token";
	private static final int REFRESH_TOKEN_COOKIE_MAX_AGE = 604800;

	private final AuthService authService;

	private ResponseCookie buildResponseCookie(String refreshToken) {
		return ResponseCookie.from(REFRESH_TOKEN_COOKIE, refreshToken).httpOnly(true).secure(true).sameSite("Strict")
				.path("/auth/refresh").maxAge(REFRESH_TOKEN_COOKIE_MAX_AGE).build();
	}

	@PostMapping("/register")
	public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
		AuthResponse authResponse = authService.register(request);

		return ResponseEntity.status(HttpStatus.CREATED)
				.header(HttpHeaders.SET_COOKIE, buildResponseCookie(authResponse.getRefreshToken()).toString())
				.body(authResponse);
	}

	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
		AuthResponse authResponse = authService.login(request);

		return ResponseEntity.status(HttpStatus.OK)
				.header(HttpHeaders.SET_COOKIE, buildResponseCookie(authResponse.getRefreshToken()).toString())
				.body(authResponse);
	}

	@PostMapping("/refresh")
	public ResponseEntity<AuthResponse> refresh(
			@CookieValue(name = REFRESH_TOKEN_COOKIE, required = false) String refreshToken) {
		if (refreshToken == null) {
			return ResponseEntity.badRequest().build();
		}

		AuthResponse authResponse = authService.refresh(refreshToken);

		return ResponseEntity.status(HttpStatus.OK)
				.header(HttpHeaders.SET_COOKIE, buildResponseCookie(authResponse.getRefreshToken()).toString())
				.body(authResponse);
	}

	@PostMapping("/logout")
	public ResponseEntity<Void> logout(
			@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			String accessToken = authHeader.substring(7);
			authService.logout(accessToken);
		}

		ResponseCookie clearCookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE, "").httpOnly(true).secure(true)
				.path("/auth/refresh").maxAge(0).build();

		return ResponseEntity.status(HttpStatus.OK).header(HttpHeaders.SET_COOKIE, clearCookie.toString()).build();
	}
}
