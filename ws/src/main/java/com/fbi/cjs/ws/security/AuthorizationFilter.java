package com.fbi.cjs.ws.security;

import com.fbi.cjs.shared.api.ApiResponse;
import com.fbi.cjs.shared.api.ResponseCode;
import com.fbi.cjs.shared.enums.Role;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Aplica {@link AllowedRoles} sobre los recursos {@link Secured}.
 *
 * <p>
 * Corre después de {@link AuthenticationFilter}, así que el usuario ya está
 * identificado. Separar los dos filtros mantiene la distinción de siempre:
 * autenticación es 401 (quién sos), autorización es 403 (podés hacer esto).
 */
@Secured
@Provider
@Priority(Priorities.AUTHORIZATION)
public class AuthorizationFilter implements ContainerRequestFilter {

	@Context
	private ResourceInfo resourceInfo;

	@Override
	public void filter(ContainerRequestContext requestContext) {
		AllowedRoles allowedRoles = findAllowedRoles();
		if (allowedRoles == null) {
			return; // basta con estar autenticado
		}

		SecurityContext securityContext = requestContext.getSecurityContext();
		boolean allowed = Arrays.stream(allowedRoles.value())
				.anyMatch(role -> securityContext.isUserInRole(role.name()));

		if (!allowed) {
			requestContext.abortWith(forbidden(allowedRoles));
		}
	}

	private Response forbidden(AllowedRoles allowedRoles) {
		String roles = Arrays.stream(allowedRoles.value()).map(Role::name).collect(Collectors.joining(", "));

		ApiResponse<Void> body = ApiResponse.error(ResponseCode.FORBIDDEN,
				"Tu rol no tiene permiso para esta operación. Roles permitidos: " + roles);

		return Response.status(Response.Status.FORBIDDEN).entity(body).type(MediaType.APPLICATION_JSON).build();
	}

	private AllowedRoles findAllowedRoles() {
		Method method = resourceInfo.getResourceMethod();
		if (method != null && method.isAnnotationPresent(AllowedRoles.class)) {
			return method.getAnnotation(AllowedRoles.class);
		}
		Class<?> resourceClass = resourceInfo.getResourceClass();
		return resourceClass == null ? null : resourceClass.getAnnotation(AllowedRoles.class);
	}
}
