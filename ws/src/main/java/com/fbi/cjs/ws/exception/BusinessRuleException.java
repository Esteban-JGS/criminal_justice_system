package com.fbi.cjs.ws.exception;

import com.fbi.cjs.shared.api.ResponseCode;

/**
 * Se rompió una regla de negocio (por ejemplo: username duplicado).
 *
 * <p>
 * Lleva su propio {@link ResponseCode} porque no todas las reglas significan lo
 * mismo: un duplicado es 409 CONFLICT, un dato inconsistente es 422.
 */
public class BusinessRuleException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final ResponseCode code;

	public BusinessRuleException(ResponseCode code, String message) {
		super(message);
		this.code = code;
	}

	public ResponseCode getCode() {
		return code;
	}
}
