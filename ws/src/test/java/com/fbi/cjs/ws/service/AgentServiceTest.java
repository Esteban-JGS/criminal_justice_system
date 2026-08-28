package com.fbi.cjs.ws.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fbi.cjs.shared.api.ResponseCode;
import com.fbi.cjs.shared.dto.AgentDTO;
import com.fbi.cjs.shared.enums.AgentStatus;
import com.fbi.cjs.ws.exception.BusinessRuleException;
import com.fbi.cjs.ws.exception.ResourceNotFoundException;
import com.fbi.cjs.ws.mocks.MockAgentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AgentServiceTest {

	private AgentService service;

	@BeforeEach
	void setUp() {
		MockAgentRepository repository = new MockAgentRepository();
		repository.seed();

		service = new AgentService();
		service.agentRepository = repository;
	}

	@Test
	@DisplayName("search sin filtros devuelve todo")
	void searchSinFiltros() {
		assertEquals(5, service.search(null, null).size());
		assertEquals(5, service.search("   ", null).size(), "un texto en blanco no es un filtro");
	}

	@Test
	@DisplayName("search con filtros delega en el repositorio")
	void searchConFiltros() {
		assertEquals(1, service.search("mulder", null).size());
		assertEquals(1, service.search(null, AgentStatus.SUSPENDIDO).size());
	}

	@Test
	@DisplayName("findById lanza 404 cuando no existe")
	void findByIdInexistente() {
		ResourceNotFoundException error = assertThrows(ResourceNotFoundException.class, () -> service.findById(9999L));

		assertTrue(error.getMessage().contains("9999"));
	}

	@Test
	@DisplayName("create ignora el id que mande el cliente")
	void createIgnoraElIdDelCliente() {
		AgentDTO creado = service
				.create(new AgentDTO(500L, "FBI-2001", "Nuevo Agente", "Antiterrorismo", AgentStatus.ACTIVO));

		assertNotEquals(500L, creado.getId());
	}

	@Test
	@DisplayName("create recorta los espacios sobrantes")
	void createNormalizaTextos() {
		AgentDTO creado = service
				.create(new AgentDTO(null, "  FBI-2002  ", "  Nuevo Agente  ", "  Cibercrimen  ", AgentStatus.ACTIVO));

		assertEquals("FBI-2002", creado.getBadgeNumber());
		assertEquals("Nuevo Agente", creado.getName());
		assertEquals("Cibercrimen", creado.getDivision());
	}

	@Test
	@DisplayName("create rechaza una placa repetida")
	void createPlacaDuplicada() {
		BusinessRuleException error = assertThrows(BusinessRuleException.class,
				() -> service.create(new AgentDTO(null, "FBI-1001", "Otro Agente", "Cibercrimen", AgentStatus.ACTIVO)));

		assertEquals(ResponseCode.CONFLICT, error.getCode());
	}

	@Test
	@DisplayName("update rechaza tomar la placa de otro agente")
	void updatePlacaDeOtro() {
		BusinessRuleException error = assertThrows(BusinessRuleException.class, () -> service.update(1L,
				new AgentDTO(null, "FBI-1002", "Dana Scully", "Ciencias Forenses", AgentStatus.ACTIVO)));

		assertEquals(ResponseCode.CONFLICT, error.getCode());
	}

	@Test
	@DisplayName("update permite que el agente conserve su propia placa")
	void updateConservaSuPropiaPlaca() {
		AgentDTO actualizado = service.update(1L,
				new AgentDTO(null, "FBI-1001", "Dana Scully", "Cibercrimen", AgentStatus.ACTIVO));

		assertEquals("Cibercrimen", actualizado.getDivision());
	}

	@Test
	@DisplayName("update de un id inexistente es 404")
	void updateInexistente() {
		assertThrows(ResourceNotFoundException.class,
				() -> service.update(9999L, new AgentDTO(null, "FBI-3001", "X", "X", AgentStatus.ACTIVO)));
	}

	@Test
	@DisplayName("delete lanza 404 cuando el id no existe")
	void delete() {
		service.delete(1L);
		assertThrows(ResourceNotFoundException.class, () -> service.delete(1L));
	}
}
