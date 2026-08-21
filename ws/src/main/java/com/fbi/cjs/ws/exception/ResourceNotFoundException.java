package com.fbi.cjs.ws.exception;

/**
 * El recurso pedido no existe. Se traduce a HTTP 404 en
 * {@link ResourceNotFoundExceptionMapper}.
 *
 * <p>
 * Los servicios lanzan esto en vez de devolver {@code null}: así el recurso
 * REST no tiene que preguntar {@code if (x == null)} en cada método.
 */
public class ResourceNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ResourceNotFoundException(String message) {
		super(message);
	}

	public ResourceNotFoundException(String resource, Object id) {
		super(resource + " con id " + id + " no encontrado.");
	}
}
