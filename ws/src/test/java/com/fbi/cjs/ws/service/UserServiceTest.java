package com.fbi.cjs.ws.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fbi.cjs.shared.api.ResponseCode;
import com.fbi.cjs.shared.dto.UserDTO;
import com.fbi.cjs.shared.enums.Role;
import com.fbi.cjs.ws.exception.BusinessRuleException;
import com.fbi.cjs.ws.exception.ResourceNotFoundException;
import com.fbi.cjs.ws.mocks.MockUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserServiceTest {

	private static final long AGENTE = 1L;
	private static final long JEFE = 3L;

	private UserService service;

	@BeforeEach
	void setUp() {
		MockUserRepository repository = new MockUserRepository();
		repository.seed();

		service = new UserService();
		service.userRepository = repository;
	}

	@Test
	@DisplayName("create exige contrasena")
	void createSinContrasena() {
		BusinessRuleException error = assertThrows(BusinessRuleException.class,
				() -> service.create(new UserDTO(null, "Ana", "agente99", "  ", Role.AGENTE, true)));

		assertEquals(ResponseCode.UNPROCESSABLE_CONTENT, error.getCode());
	}

	@Test
	@DisplayName("create rechaza un nombre de usuario repetido")
	void createUsuarioDuplicado() {
		BusinessRuleException error = assertThrows(BusinessRuleException.class,
				() -> service.create(new UserDTO(null, "Otro", "jefe01", "1234", Role.AGENTE, true)));

		assertEquals(ResponseCode.CONFLICT, error.getCode());
	}

	@Test
	@DisplayName("create deja el usuario activo por defecto y sin devolver la contrasena")
	void createPorDefectoActivo() {
		UserDTO creado = service.create(new UserDTO(null, "  Ana Rojas  ", "  agente99  ", "1234", Role.AGENTE, null));

		assertEquals("Ana Rojas", creado.getName());
		assertEquals("agente99", creado.getUsername());
		assertTrue(creado.getActive());
		assertNull(creado.getPassword());
	}

	@Test
	@DisplayName("update rechaza tomar el nombre de usuario de otro")
	void updateUsuarioTomado() {
		BusinessRuleException error = assertThrows(BusinessRuleException.class,
				() -> service.update(AGENTE, new UserDTO(null, "Gustabo", "jefe01", null, Role.AGENTE, true), JEFE));

		assertEquals(ResponseCode.CONFLICT, error.getCode());
	}

	@Test
	@DisplayName("update permite conservar el propio nombre de usuario")
	void updateConservaSuPropioUsuario() {
		UserDTO actualizado = service.update(AGENTE,
				new UserDTO(null, "Gustabo Garcia Mora", "agente01", null, Role.AGENTE, true), JEFE);

		assertEquals("Gustabo Garcia Mora", actualizado.getName());
	}

	@Test
	@DisplayName("nadie puede cambiarse el rol a si mismo")
	void updateNoPuedeCambiarseElRol() {
		BusinessRuleException error = assertThrows(BusinessRuleException.class, () -> service.update(JEFE,
				new UserDTO(null, "Horacio Perez", "jefe01", null, Role.AGENTE, true), JEFE));

		assertEquals(ResponseCode.CONFLICT, error.getCode());
		assertTrue(error.getMessage().toLowerCase().contains("rol"));
	}

	@Test
	@DisplayName("nadie puede desactivarse a si mismo")
	void updateNoPuedeDesactivarse() {
		assertThrows(BusinessRuleException.class, () -> service.update(JEFE,
				new UserDTO(null, "Horacio Perez", "jefe01", null, Role.JEFE_FBI, false), JEFE));
	}

	@Test
	@DisplayName("si puede cambiarle el rol a otro usuario")
	void updateCambiaElRolDeOtro() {
		UserDTO actualizado = service.update(AGENTE,
				new UserDTO(null, "Gustabo Garcia", "agente01", null, Role.SUPERVISOR, true), JEFE);

		assertEquals(Role.SUPERVISOR, actualizado.getRole());
	}

	@Test
	@DisplayName("update de un id inexistente es 404")
	void updateInexistente() {
		assertThrows(ResourceNotFoundException.class,
				() -> service.update(9999L, new UserDTO(null, "X", "xxxx", null, Role.AGENTE, true), JEFE));
	}

	@Test
	@DisplayName("nadie puede eliminarse a si mismo")
	void deleteNoPuedeBorrarseASiMismo() {
		BusinessRuleException error = assertThrows(BusinessRuleException.class, () -> service.delete(JEFE, JEFE));

		assertEquals(ResponseCode.CONFLICT, error.getCode());
	}

	@Test
	@DisplayName("delete de otro usuario funciona; de un id inexistente es 404")
	void delete() {
		service.delete(AGENTE, JEFE);

		assertEquals(2, service.findAll().size());
		assertThrows(ResourceNotFoundException.class, () -> service.delete(AGENTE, JEFE));
	}
}
