package com.fbi.cjs.ws.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fbi.cjs.shared.dto.UserDTO;
import com.fbi.cjs.shared.enums.Role;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Emisión, validación y revocación de tokens. */
class TokenStoreTest {

	private TokenStore tokenStore;
	private UserDTO user;

	@BeforeEach
	void setUp() {
		tokenStore = new TokenStore();
		user = new UserDTO(1L, "Horacio Perez", "jefe01", null, Role.JEFE_FBI, true);
	}

	@Test
	@DisplayName("un token recien emitido vale y trae a su usuario")
	void emitirYValidar() {
		TokenStore.IssuedToken issued = tokenStore.issue(user);

		assertEquals("jefe01", tokenStore.validate(issued.token()).orElseThrow().getUsername());
		assertTrue(issued.session().expiresAt().isAfter(Instant.now()));
	}

	@Test
	@DisplayName("cada emision genera un token distinto")
	void tokensDistintos() {
		assertNotEquals(tokenStore.issue(user).token(), tokenStore.issue(user).token());
	}

	@Test
	@DisplayName("un token desconocido, nulo o vacio no vale")
	void tokensInvalidos() {
		assertTrue(tokenStore.validate("no-existe").isEmpty());
		assertTrue(tokenStore.validate(null).isEmpty());
		assertTrue(tokenStore.validate("   ").isEmpty());
	}

	@Test
	@DisplayName("revocar invalida el token y no afecta a los demas")
	void revocar() {
		String primero = tokenStore.issue(user).token();
		String segundo = tokenStore.issue(user).token();

		tokenStore.revoke(primero);

		assertTrue(tokenStore.validate(primero).isEmpty());
		assertTrue(tokenStore.validate(segundo).isPresent());
	}

	@Test
	@DisplayName("una sesion vencida se considera invalida")
	void sesionVencida() {
		TokenStore.Session vencida = new TokenStore.Session(user, Instant.now().minusSeconds(1));

		assertTrue(vencida.isExpired());
		assertFalse(new TokenStore.Session(user, Instant.now().plusSeconds(60)).isExpired());
	}
}
