package com.venky.validationservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtUtil jwtUtil;
	private final AntPathMatcher pathMatcher;
	
	@Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        
        // If the URL starts with /api/v1/auth/, tell the Bouncer to skip this entire filter!
        return pathMatcher.match("/api/v1/auth/**", path);
    }

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		// 1. Extract the Cookie from Tomcat's "Order" (Request)
		String token = null;
		if (request.getCookies() != null) {
			token = Arrays.stream(request.getCookies()).filter(cookie -> "access_token".equals(cookie.getName()))
					.map(Cookie::getValue).findFirst().orElse(null);
		}

		// 2. If no token found, just move to the next filter (Spring will block it
		// later if needed)
		if (token == null) {
			String authHeader = request.getHeader("Authorization");
			// Standard OAuth2 format expects the header to look like: "Bearer eyJhbGci..."
			if (authHeader != null && authHeader.startsWith("Bearer ")) {
				// Extract the token by cutting off the first 7 characters ("Bearer ")
				token = authHeader.substring(7);
			} else {
				filterChain.doFilter(request, response);
				return;
			}
		}

		try {
			// 3. Verify the "Wax Seal" and Expiry
			if (!jwtUtil.isTokenExpired(token)) {
				String username = jwtUtil.extractUsername(token);

				// 4. Extract Roles from the Payload
				Claims claims = jwtUtil.extractAllClaims(token);
				String rolesString = claims.get("roles", String.class);

				// Convert "ROLE_ADMIN,ROLE_USER" back into Spring Authorities
				List<SimpleGrantedAuthority> authorities = Arrays.stream(rolesString.split(","))
						.map(SimpleGrantedAuthority::new).collect(Collectors.toList());

				// 5. Tell Spring Security: "This user is valid!"
				// We put the user into the 'SecurityContextHolder' - this is the VIP List for
				// this request
				UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, null,
						authorities);

				SecurityContextHolder.getContext().setAuthentication(authToken);
			}
		} catch (ExpiredJwtException e) {
			// The "Time Bomb" went off!
			log.error("Token has expired: {}", e.getMessage());
			handleException(response, "Token has expired. Please login again.", HttpServletResponse.SC_UNAUTHORIZED);
			return; // STOP the request here. Do not let it reach the controller.
		} catch (SignatureException | MalformedJwtException e) {
			// Someone tried to forge the wristband!
			log.error("Invalid token signature: {}", e.getMessage());
			handleException(response, "Invalid token. Access denied.", HttpServletResponse.SC_UNAUTHORIZED);
			return;
		} catch (Exception e) {
			log.error("Internal security error: {}", e.getMessage());
			handleException(response, "Authentication failed.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}
		filterChain.doFilter(request, response);
	}

	// Helper method to write JSON directly to Tomcat's "Tray"
	private void handleException(HttpServletResponse response, String message, int status) throws IOException {
		response.setStatus(status);
		response.setContentType("application/json");
		// Directly write the JSON string to the response body
		String jsonResponse = String.format("{\"error\": \"%s\"}", message);
		response.getWriter().write(jsonResponse);
	}
}
