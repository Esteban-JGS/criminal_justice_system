package com.fbi.cjs.ws.exception;

import com.fbi.cjs.shared.api.ResponseCode;

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
