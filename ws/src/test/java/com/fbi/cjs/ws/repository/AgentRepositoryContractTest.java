package com.fbi.cjs.ws.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fbi.cjs.shared.dto.AgentDTO;
import com.fbi.cjs.shared.enums.AgentStatus;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public abstract class AgentRepositoryContractTest {

	protected AgentRepository repository;

	protected abstract AgentRepository newRepository();

	@BeforeEach
	void setUp() {
		repository = newRepository();
	}

	@Test
	@DisplayName("findAll devuelve los cinco agentes ordenados por id")
	void findAllOrdenadoPorId() {
		List<AgentDTO> agents = repository.findAll();

		assertEquals(5, agents.size());
		for (int i = 1; i < agents.size(); i++) {
			assertTrue(agents.get(i - 1).getId() < agents.get(i).getId());
		}
	}

	@Test
	@DisplayName("findById devuelve vacio cuando el id no existe")
	void findByIdInexistente() {
		assertTrue(repository.findById(9999L).isEmpty());
	}

	@Test
	@DisplayName("modificar lo devuelto no toca lo almacenado")
	void devuelveCopias() {
		AgentDTO primero = repository.findById(1L).orElseThrow();
		primero.setName("Nombre alterado");

		AgentDTO recargado = repository.findById(1L).orElseThrow();
		assertNotSame(primero, recargado);
		assertEquals("Dana Scully", recargado.getName());
	}

	@Test
	@DisplayName("search por texto busca en nombre, placa y division")
	void searchPorTexto() {
		assertEquals(1, repository.search("scully", null).size(), "por nombre");
		assertEquals(1, repository.search("FBI-1002", null).size(), "por placa");
		assertEquals(1, repository.search("cibercrimen", null).size(), "por division");
		assertTrue(repository.search("no-existe-nada", null).isEmpty());
	}

	@Test
	@DisplayName("search combina texto y estado con AND")
	void searchCombinaFiltros() {
		assertEquals(3, repository.search(null, AgentStatus.ACTIVO).size());
		assertTrue(repository.search("scully", AgentStatus.RETIRADO).isEmpty(),
				"el texto coincide pero el estado no: no debe devolver nada");
	}

	@Test
	@DisplayName("la placa no distingue mayusculas")
	void placaSinDistinguirMayusculas() {
		assertTrue(repository.findByBadgeNumber("fbi-1001").isPresent());
		assertTrue(repository.existsByBadgeNumber("FBI-1001"));
		assertFalse(repository.existsByBadgeNumber("FBI-9999"));
	}

	@Test
	@DisplayName("create asigna un id nuevo sin pisar los existentes")
	void createAsignaId() {
		AgentDTO creado = repository
				.create(new AgentDTO(null, "FBI-2001", "Nuevo Agente", "Antiterrorismo", AgentStatus.ACTIVO));

		assertNotNull(creado.getId());
		assertTrue(creado.getId() > 5, "el id debe continuar la secuencia");
		assertEquals(6, repository.findAll().size());
	}

	@Test
	@DisplayName("update conserva el id de la URL aunque el cuerpo traiga otro")
	void updateConservaIdDeLaUrl() {
		AgentDTO cambios = new AgentDTO(777L, "FBI-1002", "Fox Mulder", "Cibercrimen", AgentStatus.SUSPENDIDO);

		AgentDTO actualizado = repository.update(2L, cambios).orElseThrow();

		assertEquals(2L, actualizado.getId());
		assertEquals("Cibercrimen", actualizado.getDivision());
		assertTrue(repository.findById(777L).isEmpty());
	}

	@Test
	@DisplayName("update de un id inexistente devuelve vacio y no crea nada")
	void updateInexistente() {
		assertTrue(repository.update(9999L, new AgentDTO(null, "FBI-3001", "X", "X", AgentStatus.ACTIVO)).isEmpty());
		assertEquals(5, repository.findAll().size());
	}

	@Test
	@DisplayName("deleteById informa si borro algo")
	void deleteById() {
		assertTrue(repository.deleteById(3L));
		assertTrue(repository.findById(3L).isEmpty());
		assertFalse(repository.deleteById(3L));
	}
}
