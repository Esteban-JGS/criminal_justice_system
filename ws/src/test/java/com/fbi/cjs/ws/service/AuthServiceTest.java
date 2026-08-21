package com.fbi.cjs.ws.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fbi.cjs.shared.dto.LoginRequestDTO;
import com.fbi.cjs.shared.dto.LoginResponseDTO;
import com.fbi.cjs.shared.dto.UserDTO;
import com.fbi.cjs.shared.enums.Role;
import com.fbi.cjs.ws.exception.AuthenticationException;
import com.fbi.cjs.ws.mocks.MockUserRepository;
import com.fbi.cjs.ws.security.TokenStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AuthServiceTest {

	private AuthService service;
	private TokenStore tokenStore;
	private MockUserRepository repository;

	@BeforeEach
	void setUp() {
		repository = new MockUserRepository();
		repository.seed();
		tokenStore = new TokenStore();

		service = new AuthService();
		service.userRepository = repository;
		service.tokenStore = tokenStore;
	}

	@Test
	@DisplayName("login correcto devuelve token y usuario sin contrasena")
	void loginCorrecto() {
		LoginResponseDTO response = service.login(new LoginRequestDTO("jefe01", "1234"));

		assertNotNull(response.getToken());
		assertNotNull(response.getExpiresAt());
		assertEquals(Role.JEFE_FBI, response.getUser().getRole());
		assertNull(response.getUser().getPassword(), "la contrasena no puede viajar en la respuesta");
		assertTrue(tokenStore.validate(response.getToken()).isPresent());
	}

	@Test
	@DisplayName("el mensaje es el mismo si el usuario no existe o la clave esta mal")
	void loginInvalidoNoRevelaSiElUsuarioExiste() {
		String claveMala = assertThrows(AuthenticationException.class,
				() -> service.login(new LoginRequestDTO("jefe01", "incorrecta"))).getMessage();

		String usuarioInexistente = assertThrows(AuthenticationException.class,
				() -> service.login(new LoginRequestDTO("no-existe", "1234"))).getMessage();

		assertEquals(claveMala, usuarioInexistente);
	}

	@Test
	@DisplayName("un usuario inactivo no puede entrar")
	void loginUsuarioInactivo() {
		repository.update(1L, new UserDTO(1L, "Gustabo Garcia", "agente01", null, Role.AGENTE, false));

		AuthenticationException error = assertThrows(AuthenticationException.class,
				() -> service.login(new LoginRequestDTO("agente01", "1234")));

		assertTrue(error.getMessage().toLowerCase().contains("inactivo"));
	}

	@Test
	@DisplayName("logout invalida el token")
	void logout() {
		String token = service.login(new LoginRequestDTO("agente01", "1234")).getToken();

		service.logout(token);

		assertTrue(tokenStore.validate(token).isEmpty());
	}
}
