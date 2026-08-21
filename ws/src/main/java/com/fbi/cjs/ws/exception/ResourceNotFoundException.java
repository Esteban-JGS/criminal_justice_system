package com.fbi.cjs.ws.exception;

public class ResourceNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ResourceNotFoundException(String message) {
		super(message);
	}

	public ResourceNotFoundException(String resource, Object id) {
		super(resource + " con id " + id + " no encontrado.");
	}
}
