package com.fbi.cjs.ws.security;

import com.fbi.cjs.shared.dto.UserDTO;
import com.fbi.cjs.ws.exception.AuthenticationException;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;
import java.security.Principal;

@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {

	private static final String BEARER_PREFIX = "Bearer ";

	@Inject
	private TokenStore tokenStore;

	@Override
	public void filter(ContainerRequestContext requestContext) {
		String token = extractToken(requestContext);

		UserDTO user = tokenStore.validate(token)
				.orElseThrow(() -> new AuthenticationException("Token ausente, inválido o expirado."));

		RequestUser.store(requestContext, user, token);

		boolean secure = "https".equalsIgnoreCase(requestContext.getUriInfo().getRequestUri().getScheme());
		requestContext.setSecurityContext(new TokenSecurityContext(user, secure));
	}

	private String extractToken(ContainerRequestContext requestContext) {
		String header = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
		if (header == null || !header.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
			return null;
		}
		return header.substring(BEARER_PREFIX.length()).trim();
	}

	private record TokenSecurityContext(UserDTO user, boolean secure) implements SecurityContext {

		@Override
		public Principal getUserPrincipal() {
			return user::getUsername;
		}

		@Override
		public boolean isUserInRole(String role) {
			return user.getRole() != null && user.getRole().name().equals(role);
		}

		@Override
		public boolean isSecure() {
			return secure;
		}

		@Override
		public String getAuthenticationScheme() {
			return "Bearer";
		}
	}
}
