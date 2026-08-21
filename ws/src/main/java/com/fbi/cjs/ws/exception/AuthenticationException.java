package com.fbi.cjs.ws.exception;

/** Credenciales inválidas o token vencido. Se traduce a HTTP 401. */
public class AuthenticationException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public AuthenticationException(String message) {
		super(message);
	}
}
