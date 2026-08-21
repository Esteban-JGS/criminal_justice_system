package com.fbi.criminal_justice_system.utils;

import java.util.List;

/**
 * Error devuelto por el web service, o fallo al comunicarse con él. Incluye el
 * código HTTP y, si fue un error de validación, los campos que fallaron.
 */
public class ApiException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/** 0 cuando ni siquiera se logró contactar al servidor. */
	private final int status;

	private final List<String> errors;

	public ApiException(int status, String message, List<String> errors) {
		super(message);
		this.status = status;
		this.errors = errors == null ? List.of() : errors;
	}

	public ApiException(String message, Throwable cause) {
		super(message, cause);
		this.status = 0;
		this.errors = List.of();
	}

	public int getStatus() {
		return status;
	}

	public List<String> getErrors() {
		return errors;
	}

	/** {@code true} si el servidor no respondió (apagado, URL mala, sin red). */
	public boolean isConnectionProblem() {
		return status == 0;
	}

	/**
	 * Mensaje listo para mostrarle al usuario, con el detalle de validación si lo
	 * hay.
	 */
	public String getDisplayMessage() {
		if (errors.isEmpty()) {
			return getMessage();
		}
		return getMessage() + "\n- " + String.join("\n- ", errors);
	}
}
