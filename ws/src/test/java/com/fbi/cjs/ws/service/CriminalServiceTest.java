package com.fbi.cjs.ws.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fbi.cjs.shared.dto.CriminalDTO;
import com.fbi.cjs.shared.enums.CriminalStatus;
import com.fbi.cjs.shared.enums.DangerLevel;
import com.fbi.cjs.ws.exception.ResourceNotFoundException;
import com.fbi.cjs.ws.mocks.MockCriminalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CriminalServiceTest {

	private CriminalService service;

	@BeforeEach
	void setUp() {
		MockCriminalRepository repository = new MockCriminalRepository();
		repository.seed();

		service = new CriminalService();
		service.criminalRepository = repository;
	}

	@Test
	@DisplayName("search sin filtros devuelve todo")
	void searchSinFiltros() {
		assertEquals(10, service.search(null, null, null).size());
		assertEquals(10, service.search("   ", null, null).size(), "un texto en blanco no es un filtro");
	}

	@Test
	@DisplayName("search con filtros delega en el repositorio")
	void searchConFiltros() {
		assertEquals(1, service.search("cobra", null, null).size());
		assertEquals(2, service.search(null, CriminalStatus.CAPTURADO, null).size());
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
		CriminalDTO creado = service
				.create(new CriminalDTO(500L, "Nuevo", "Alias", "Delito", DangerLevel.BAJO, CriminalStatus.ACTIVO));

		assertNotEquals(500L, creado.getId(), "el id lo asigna la base de datos, no el cliente");
	}

	@Test
	@DisplayName("create recorta los espacios sobrantes")
	void createNormalizaTextos() {
		CriminalDTO creado = service.create(new CriminalDTO(null, "  Victor  ", "  El Fantasma  ", "  Fraude  ",
				DangerLevel.ALTO, CriminalStatus.ACTIVO));

		assertEquals("Victor", creado.getName());
		assertEquals("El Fantasma", creado.getAlias());
		assertEquals("Fraude", creado.getCrime());
	}

	@Test
	@DisplayName("update tambien normaliza y lanza 404 si no existe")
	void update() {
		CriminalDTO actualizado = service.update(1L,
				new CriminalDTO(null, "  Editado  ", "Alias", "Delito", DangerLevel.MEDIO, CriminalStatus.CAPTURADO));

		assertEquals("Editado", actualizado.getName());
		assertEquals(1L, actualizado.getId());

		assertThrows(ResourceNotFoundException.class, () -> service.update(9999L,
				new CriminalDTO(null, "X", "X", "X", DangerLevel.BAJO, CriminalStatus.ACTIVO)));
	}

	@Test
	@DisplayName("delete lanza 404 cuando el id no existe")
	void delete() {
		service.delete(1L);
		assertThrows(ResourceNotFoundException.class, () -> service.delete(1L));
	}
}
