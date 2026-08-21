package com.fbi.cjs.ws.repository;

import com.fbi.cjs.shared.dto.CriminalDTO;
import com.fbi.cjs.shared.enums.CriminalStatus;
import com.fbi.cjs.shared.enums.DangerLevel;
import java.util.List;
import java.util.Optional;

public interface CriminalRepository {

	List<CriminalDTO> findAll();

	List<CriminalDTO> search(String text, CriminalStatus status, DangerLevel dangerLevel);

	Optional<CriminalDTO> findById(Long id);

	CriminalDTO create(CriminalDTO criminal);

	Optional<CriminalDTO> update(Long id, CriminalDTO criminal);

	boolean deleteById(Long id);
}
