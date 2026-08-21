package com.fbi.cjs.ws.security;

import com.fbi.cjs.shared.dto.UserDTO;
import jakarta.enterprise.context.ApplicationScoped;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Emisor y validador de tokens de sesión, en memoria.
 *
 * <p>
 * No es JWT: es un token opaco y aleatorio guardado en un mapa. Se pierde al
 * reiniciar Payara y no serviría con varias instancias del servidor. Para
 * producción, emitir un JWT firmado o mover las sesiones a una tabla; en ambos
 * casos solo cambia esta clase.
 */
@ApplicationScoped
public class TokenStore {

	private static final long TTL_HOURS = 8;

	private final Map<String, Session> sessions = new ConcurrentHashMap<>();

	public record Session(UserDTO user, Instant expiresAt) {
		public boolean isExpired() {
			return Instant.now().isAfter(expiresAt);
		}
	}

	public record IssuedToken(String token, Session session) {
	}

	public IssuedToken issue(UserDTO user) {
		String token = Base64.getUrlEncoder().withoutPadding()
				.encodeToString(UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));

		Session session = new Session(user, Instant.now().plus(TTL_HOURS, ChronoUnit.HOURS));
		sessions.put(token, session);
		return new IssuedToken(token, session);
	}

	/**
	 * Los tokens vencidos se descartan al consultarlos, así no hace falta un hilo
	 * barriendo el mapa.
	 */
	public Optional<UserDTO> validate(String token) {
		if (token == null || token.isBlank()) {
			return Optional.empty();
		}
		Session session = sessions.get(token);
		if (session == null) {
			return Optional.empty();
		}
		if (session.isExpired()) {
			sessions.remove(token);
			return Optional.empty();
		}
		return Optional.of(session.user());
	}

	public void revoke(String token) {
		if (token != null) {
			sessions.remove(token);
		}
	}
}
