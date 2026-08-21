package com.fbi.criminal_justice_system.utils;

import java.util.List;

public class ApiException extends RuntimeException {

	private static final long serialVersionUID = 1L;

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

	public boolean isConnectionProblem() {
		return status == 0;
	}

	public String getDisplayMessage() {
		if (errors.isEmpty()) {
			return getMessage();
		}
		return getMessage() + "\n- " + String.join("\n- ", errors);
	}
}
