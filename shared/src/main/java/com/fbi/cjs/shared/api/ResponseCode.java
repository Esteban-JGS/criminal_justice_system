package com.fbi.cjs.shared.api;

/**
 * Códigos de respuesta de la API con su equivalente HTTP. Viajan también dentro
 * del cuerpo JSON, así el cliente decide leyendo un solo campo.
 */
public enum ResponseCode {
	OK(200), CREATED(201), NO_CONTENT(204), BAD_REQUEST(400), UNAUTHORIZED(401), FORBIDDEN(403), NOT_FOUND(
			404), CONFLICT(409), UNPROCESSABLE_CONTENT(422), INTERNAL_SERVER_ERROR(500), SERVICE_UNAVAILABLE(503);

	private final int httpStatus;

	ResponseCode(int httpStatus) {
		this.httpStatus = httpStatus;
	}

	public int getHttpStatus() {
		return httpStatus;
	}
}
