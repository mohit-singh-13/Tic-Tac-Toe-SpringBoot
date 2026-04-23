package com.tictactoe.auth.service;

import java.util.Date;
import java.util.List;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tictactoe.auth.dto.AuthResponse;
import com.tictactoe.auth.dto.LoginRequest;
import com.tictactoe.auth.dto.RegisterRequest;
import com.tictactoe.auth.entity.User;
import com.tictactoe.auth.mapper.UserMapper;
import com.tictactoe.auth.repository.UserRepository;
import com.tictactoe.auth.security.JwtUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService implements UserDetailsService {

	private final AuthenticationManager authenticationManager;
	private final UserRepository userRepository;
	private final UserMapper userMapper;
	private final PasswordEncoder passwordEncoder;
	private final JwtUtil jwtUtil;
	private final TokenBlocklistService tokenBlocklistService;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

		return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(),
				List.of(new SimpleGrantedAuthority(user.getRole().name())));
	}

	private AuthResponse buildAuthResponse(String username, String role) {
		String accessToken = jwtUtil.generateAccessToken(username, role);
		String refreshToken = jwtUtil.generateRefreshToken(username);

		return AuthResponse.builder().accessToken(accessToken).refreshToken(refreshToken).username(username).role(role)
				.expiresIn(900000L).build();
	}

	@Transactional
	public AuthResponse register(RegisterRequest request) {
		if (userRepository.existsByUsername(request.getUsername())) {
			throw new IllegalArgumentException("Username already taken: " + request.getUsername());
		}

		if (userRepository.existsByEmail(request.getEmail())) {
			throw new IllegalArgumentException("Email already registered: " + request.getEmail());
		}

		User user = userMapper.toEntity(request);
		user.setPassword(passwordEncoder.encode(request.getPassword()));

		userRepository.save(user);
		log.info("New user registered: {}", user.getUsername());

		return buildAuthResponse(user.getUsername(), user.getRole().name());
	}

	public AuthResponse login(LoginRequest request) {
		authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

		User user = userRepository.findByUsername(request.getUsername())
				.orElseThrow(() -> new UsernameNotFoundException(request.getUsername()));

		log.info("User logged in: {}", user.getUsername());
		return buildAuthResponse(user.getUsername(), user.getRole().name());
	}

	public AuthResponse refresh(String refreshToken) {
		if (!jwtUtil.validateToken(refreshToken)) {
			throw new IllegalArgumentException("Invalid or expired refresh token");
		}

		String tokenType = jwtUtil.extractTokenType(refreshToken);
		if (!"refresh".equals(tokenType)) {
			throw new IllegalArgumentException("Token is not a refresh token");
		}

		String jti = jwtUtil.extractJti(refreshToken);
		if (tokenBlocklistService.isTokenBlocked(jti)) {
			throw new IllegalArgumentException("Refresh token has been invalidated");
		}

		String username = jwtUtil.extractUsername(refreshToken);
		User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));

		log.info("Access token refreshed for user: {}", username);

		String newAccessToken = jwtUtil.generateAccessToken(username, user.getRole().name());

		return AuthResponse.builder().accessToken(newAccessToken).refreshToken(refreshToken).username(username)
				.role(user.getRole().name()).expiresIn(900000L).build();
	}

	public void logout(String accessToken) {
		if (!jwtUtil.validateToken(accessToken)) {
			return;
		}

		String jti = jwtUtil.extractJti(accessToken);
		Date expiry = jwtUtil.extractExpiration(accessToken);

		long ttlMillis = expiry.getTime() - System.currentTimeMillis();

		if (ttlMillis > 0) {
			tokenBlocklistService.blockToken(jti, ttlMillis);
		}

		log.info("User logged out, token blocklisted: jti={}", jti);
	}
}
