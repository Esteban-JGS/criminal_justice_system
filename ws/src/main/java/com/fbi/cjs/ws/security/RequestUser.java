package com.fbi.cjs.ws.security;

import com.fbi.cjs.shared.dto.UserDTO;
import jakarta.ws.rs.container.ContainerRequestContext;

/**
 * Usuario autenticado de la petición en curso.
 *
 * <p>
 * {@link AuthenticationFilter} lo deja acá tras validar el token y los recursos
 * lo leen desde su {@code ContainerRequestContext}.
 *
 * <p>
 * No se lee del {@code SecurityContext} inyectado en un recurso: JAX-RS inyecta
 * un proxy que delega por petición, así que castearlo al tipo concreto falla en
 * tiempo de ejecución.
 */
public final class RequestUser {

	private static final String USER_PROPERTY = "cjs.auth.user";
	private static final String TOKEN_PROPERTY = "cjs.auth.token";

	private RequestUser() {
	}

	static void store(ContainerRequestContext requestContext, UserDTO user, String token) {
		requestContext.setProperty(USER_PROPERTY, user);
		requestContext.setProperty(TOKEN_PROPERTY, token);
	}

	/**
	 * Nunca es null en un recurso {@link Secured}: sin token el filtro corta antes.
	 */
	public static UserDTO of(ContainerRequestContext requestContext) {
		return (UserDTO) requestContext.getProperty(USER_PROPERTY);
	}

	public static String tokenOf(ContainerRequestContext requestContext) {
		return (String) requestContext.getProperty(TOKEN_PROPERTY);
	}
}
