package com.tictactoe.auth.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.tictactoe.auth.service.TokenBlocklistService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtUtil jwtUtil;
	private final TokenBlocklistService tokenBlocklistService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		final String authHeader = request.getHeader("Authorization");

		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}

		final String token = authHeader.substring(7);

		if (!jwtUtil.validateToken(token)) {
			log.debug("Invalid JWT token for request: {}", request.getRequestURI());
			filterChain.doFilter(request, response);
			return;
		}

		String jti = jwtUtil.extractJti(token);
		if (tokenBlocklistService.isTokenBlocked(jti)) {
			log.debug("Blocklisted token used: jti={}", jti);
			filterChain.doFilter(request, response);
			return;
		}

		final String username = jwtUtil.extractUsername(token);
		final String role = jwtUtil.extractRole(token);
		final String tokenType = jwtUtil.extractTokenType(token);

		if (!"access".equals(tokenType)) {
			log.warn("Refresh token used as access token for request: {}", request.getRequestURI());
			filterChain.doFilter(request, response);
			return;
		}

		if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
			List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));

			UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username, null,
					authorities);

			authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

			SecurityContextHolder.getContext().setAuthentication(authentication);
		}

		filterChain.doFilter(request, response);
	}
}
