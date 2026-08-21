package com.fbi.cjs.ws.security;

import com.fbi.cjs.shared.dto.UserDTO;
import jakarta.ws.rs.container.ContainerRequestContext;

public final class RequestUser {

	private static final String USER_PROPERTY = "cjs.auth.user";
	private static final String TOKEN_PROPERTY = "cjs.auth.token";

	private RequestUser() {
	}

	static void store(ContainerRequestContext requestContext, UserDTO user, String token) {
		requestContext.setProperty(USER_PROPERTY, user);
		requestContext.setProperty(TOKEN_PROPERTY, token);
	}

	public static UserDTO of(ContainerRequestContext requestContext) {
		return (UserDTO) requestContext.getProperty(USER_PROPERTY);
	}

	public static String tokenOf(ContainerRequestContext requestContext) {
		return (String) requestContext.getProperty(TOKEN_PROPERTY);
	}
}
