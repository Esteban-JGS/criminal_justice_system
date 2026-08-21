package com.fbi.criminal_justice_system.utils;

/**
 * Descarta las respuestas que llegan tarde.
 *
 * <p>
 * Con una búsqueda que consulta al servidor mientras el usuario escribe puede
 * haber varias peticiones en vuelo. Si la primera es más lenta que la segunda,
 * su respuesta llega después y deja la tabla mostrando resultados que ya no
 * corresponden al texto escrito.
 *
 * <p>
 * Cada consulta pide un turno con {@link #next()} y, al responder, solo se
 * pinta si sigue siendo el último ({@link #isCurrent(long)}).
 *
 * <pre>
 * long ticket = guard.next();
 * BackgroundTask.run(() -&gt; service.buscar(texto), resultado -&gt; {
 * 	if (guard.isCurrent(ticket)) {
 * 		tabla.setItems(resultado);
 * 	}
 * }, error -&gt; ...);
 * </pre>
 *
 * <p>
 * No necesita sincronización: se usa siempre desde el hilo de JavaFX, tanto al
 * pedir el turno como al recibir la respuesta.
 */
public final class RequestGuard {

	private long lastIssued;

	/** Turno para una consulta nueva; invalida las anteriores. */
	public long next() {
		return ++lastIssued;
	}

	/** {@code true} si este turno sigue siendo el más reciente. */
	public boolean isCurrent(long ticket) {
		return ticket == lastIssued;
	}
}
