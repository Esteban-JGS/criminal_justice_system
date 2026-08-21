package com.fbi.cjs.shared.api;

/**
 * Rutas de la API en un solo lugar: el servidor las usa en {@code @Path} y el
 * cliente para armar las URLs.
 *
 * <p>
 * Son constantes literales porque las anotaciones solo aceptan constantes de
 * compilación.
 */
public final class ApiPaths {

	/** Prefijo de la aplicación JAX-RS: {@code @ApplicationPath("/api/v1")}. */
	public static final String API_ROOT = "/api/v1";

	public static final String AUTH = "auth";
	public static final String CRIMINALS = "criminals";
	public static final String USERS = "users";
	public static final String ROLES = "roles";
	public static final String HEALTH = "health";

	private ApiPaths() {
	}
}
