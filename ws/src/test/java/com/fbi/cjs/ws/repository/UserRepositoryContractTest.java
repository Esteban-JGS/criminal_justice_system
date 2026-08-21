package com.fbi.cjs.ws.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fbi.cjs.shared.dto.UserDTO;
import com.fbi.cjs.shared.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Comportamiento que debe cumplir cualquier {@link UserRepository}.
 *
 * <p>
 * Lo más importante que verifica: la contraseña solo sale por
 * {@code findByUsername}, que es lo que usa el login. Si una implementación
 * futura la devolviera en {@code findAll}, terminaría viajando al cliente.
 */
public abstract class UserRepositoryContractTest {

	protected UserRepository repository;

	protected abstract UserRepository newRepository();

	@BeforeEach
	void setUp() {
		repository = newRepository();
	}

	@Test
	@DisplayName("findAll nunca devuelve contrasenas")
	void findAllSinContrasenas() {
		assertEquals(3, repository.findAll().size());
		assertTrue(repository.findAll().stream().allMatch(user -> user.getPassword() == null));
	}

	@Test
	@DisplayName("findById tampoco devuelve la contrasena")
	void findByIdSinContrasena() {
		UserDTO user = repository.findById(1L).orElseThrow();

		assertEquals("agente01", user.getUsername());
		assertNull(user.getPassword());
	}

	@Test
	@DisplayName("findByUsername si trae la contrasena, porque la usa el login")
	void findByUsernameConContrasena() {
		UserDTO user = repository.findByUsername("jefe01").orElseThrow();

		assertNotNull(user.getPassword());
		assertEquals(Role.JEFE_FBI, user.getRole());
	}

	@Test
	@DisplayName("el nombre de usuario no distingue mayusculas")
	void usernameSinDistinguirMayusculas() {
		assertTrue(repository.findByUsername("JEFE01").isPresent());
		assertTrue(repository.existsByUsername("Agente01"));
		assertFalse(repository.existsByUsername("no-existe"));
		assertFalse(repository.findByUsername(null).isPresent());
	}

	@Test
	@DisplayName("create asigna id y no devuelve la contrasena")
	void create() {
		UserDTO creado = repository.create(new UserDTO(null, "Ana Rojas", "agente99", "clave", Role.AGENTE, true));

		assertNotNull(creado.getId());
		assertNull(creado.getPassword());
		assertTrue(repository.findByUsername("agente99").isPresent());
	}

	@Test
	@DisplayName("update con contrasena vacia conserva la anterior")
	void updateSinContrasenaConservaLaActual() {
		String original = repository.findByUsername("agente01").orElseThrow().getPassword();

		repository.update(1L, new UserDTO(1L, "Gustabo Garcia", "agente01", null, Role.SUPERVISOR, true));

		UserDTO actualizado = repository.findByUsername("agente01").orElseThrow();
		assertEquals(original, actualizado.getPassword());
		assertEquals(Role.SUPERVISOR, actualizado.getRole(), "el resto de los campos si se actualiza");
	}

	@Test
	@DisplayName("update con contrasena nueva la reemplaza")
	void updateConContrasenaNueva() {
		repository.update(1L, new UserDTO(1L, "Gustabo Garcia", "agente01", "nueva-clave", Role.AGENTE, true));

		assertEquals("nueva-clave", repository.findByUsername("agente01").orElseThrow().getPassword());
	}

	@Test
	@DisplayName("update de un id inexistente devuelve vacio")
	void updateInexistente() {
		assertTrue(repository.update(9999L, new UserDTO(null, "X", "xxxx", "clave", Role.AGENTE, true)).isEmpty());
	}

	@Test
	@DisplayName("deleteById informa si borro algo")
	void deleteById() {
		assertTrue(repository.deleteById(1L));
		assertTrue(repository.findById(1L).isEmpty());
		assertFalse(repository.deleteById(1L));
	}
}
