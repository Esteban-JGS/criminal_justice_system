package com.fbi.cjs.ws.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fbi.cjs.shared.dto.CriminalDTO;
import com.fbi.cjs.shared.enums.CriminalStatus;
import com.fbi.cjs.shared.enums.DangerLevel;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public abstract class CriminalRepositoryContractTest {

	protected CriminalRepository repository;

	protected abstract CriminalRepository newRepository();

	@BeforeEach
	void setUp() {
		repository = newRepository();
	}

	@Test
	@DisplayName("findAll devuelve los registros ordenados por id")
	void findAllOrdenadoPorId() {
		List<CriminalDTO> criminals = repository.findAll();

		assertEquals(10, criminals.size());
		for (int i = 1; i < criminals.size(); i++) {
			assertTrue(criminals.get(i - 1).getId() < criminals.get(i).getId(),
					"los ids deben venir en orden ascendente");
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
		CriminalDTO primero = repository.findById(1L).orElseThrow();
		primero.setName("Nombre alterado");

		CriminalDTO recargado = repository.findById(1L).orElseThrow();
		assertNotSame(primero, recargado);
		assertEquals("Victor Salazar", recargado.getName());
	}

	@Test
	@DisplayName("search por texto busca en nombre, alias y delito, sin distinguir mayusculas")
	void searchPorTexto() {
		assertEquals(1, repository.search("cobra", null, null).size(), "por alias");
		assertEquals(1, repository.search("VICTOR", null, null).size(), "por nombre en mayusculas");
		assertEquals(1, repository.search("bancario", null, null).size(), "por delito");
		assertTrue(repository.search("no-existe-nada", null, null).isEmpty());
	}

	@Test
	@DisplayName("search combina los filtros con AND")
	void searchCombinaFiltros() {
		List<CriminalDTO> activosAltos = repository.search(null, CriminalStatus.ACTIVO, DangerLevel.ALTO);

		assertFalse(activosAltos.isEmpty());
		assertTrue(activosAltos.stream()
				.allMatch(c -> c.getStatus() == CriminalStatus.ACTIVO && c.getDangerLevel() == DangerLevel.ALTO));

		assertTrue(repository.search("cobra", CriminalStatus.FALLECIDO, null).isEmpty(),
				"el texto coincide pero el estado no: no debe devolver nada");
	}

	@Test
	@DisplayName("create asigna un id nuevo sin pisar los existentes")
	void createAsignaId() {
		CriminalDTO creado = repository.create(
				new CriminalDTO(null, "Nuevo", "El Nuevo", "Contrabando", DangerLevel.BAJO, CriminalStatus.ACTIVO));

		assertNotNull(creado.getId());
		assertTrue(creado.getId() > 10, "el id debe continuar la secuencia");
		assertEquals(11, repository.findAll().size());
		assertEquals("Nuevo", repository.findById(creado.getId()).orElseThrow().getName());
	}

	@Test
	@DisplayName("update conserva el id de la URL aunque el cuerpo traiga otro")
	void updateConservaIdDeLaUrl() {
		CriminalDTO cambios = new CriminalDTO(777L, "Editado", "Alias", "Otro delito", DangerLevel.MEDIO,
				CriminalStatus.CAPTURADO);

		CriminalDTO actualizado = repository.update(2L, cambios).orElseThrow();

		assertEquals(2L, actualizado.getId());
		assertEquals("Editado", actualizado.getName());
		assertTrue(repository.findById(777L).isEmpty(), "no debe haber creado un registro con el id del cuerpo");
	}

	@Test
	@DisplayName("update de un id inexistente devuelve vacio y no crea nada")
	void updateInexistente() {
		Optional<CriminalDTO> resultado = repository.update(9999L,
				new CriminalDTO(null, "X", "X", "X", DangerLevel.BAJO, CriminalStatus.ACTIVO));

		assertTrue(resultado.isEmpty());
		assertEquals(10, repository.findAll().size());
	}

	@Test
	@DisplayName("deleteById informa si borro algo")
	void deleteById() {
		assertTrue(repository.deleteById(3L));
		assertTrue(repository.findById(3L).isEmpty());
		assertFalse(repository.deleteById(3L), "borrar dos veces el mismo id debe devolver false");
	}
}
