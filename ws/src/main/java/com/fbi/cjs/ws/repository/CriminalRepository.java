package com.fbi.cjs.ws.repository;

import com.fbi.cjs.shared.dto.CriminalDTO;
import com.fbi.cjs.shared.enums.CriminalStatus;
import com.fbi.cjs.shared.enums.DangerLevel;
import java.util.List;
import java.util.Optional;

/**
 * Acceso a datos de criminales.
 *
 * <p>
 * Los servicios dependen de esta interfaz y nunca de una implementación
 * concreta: hoy la implementa {@code MockCriminalRepository} (memoria) y mañana
 * una versión con JPA sobre Oracle, sin tocar servicios ni recursos REST. Ver
 * {@code ws/docs/oracle-migration.md}.
 */
public interface CriminalRepository {

	List<CriminalDTO> findAll();

	/** Filtros opcionales: los parámetros en {@code null} se ignoran. */
	List<CriminalDTO> search(String text, CriminalStatus status, DangerLevel dangerLevel);

	Optional<CriminalDTO> findById(Long id);

	/** Inserta y devuelve el criminal ya con su id asignado. */
	CriminalDTO create(CriminalDTO criminal);

	/** {@code Optional.empty()} si el id no existe. */
	Optional<CriminalDTO> update(Long id, CriminalDTO criminal);

	/** @return {@code true} si borró algo. */
	boolean deleteById(Long id);
}
