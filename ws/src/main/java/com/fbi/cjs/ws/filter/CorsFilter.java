package com.fbi.cjs.ws.filter;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

/**
 * Habilita CORS para clientes que corran en un navegador. El cliente JavaFX no
 * lo necesita —CORS es una política del browser—, pero sí Swagger UI o una
 * pantalla web.
 *
 * <p>
 * El origen {@code *} vale en desarrollo; en producción se lista el dominio
 * exacto del cliente.
 */
@Provider
public class CorsFilter implements ContainerResponseFilter {

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
		responseContext.getHeaders().putSingle("Access-Control-Allow-Origin", "*");
		responseContext.getHeaders().putSingle("Access-Control-Allow-Headers",
				"origin, content-type, accept, authorization");
		responseContext.getHeaders().putSingle("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
		responseContext.getHeaders().putSingle("Access-Control-Max-Age", "3600");
	}
}
