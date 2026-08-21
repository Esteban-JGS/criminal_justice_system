package com.fbi.cjs.ws.service;

import com.fbi.cjs.shared.dto.CriminalDTO;
import com.fbi.cjs.shared.enums.CriminalStatus;
import com.fbi.cjs.shared.enums.DangerLevel;
import com.fbi.cjs.ws.exception.ResourceNotFoundException;
import com.fbi.cjs.ws.repository.CriminalRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;

@ApplicationScoped
public class CriminalService {

	@Inject
	CriminalRepository criminalRepository;

	public List<CriminalDTO> search(String text, CriminalStatus status, DangerLevel dangerLevel) {
		if (isBlank(text) && status == null && dangerLevel == null) {
			return criminalRepository.findAll();
		}
		return criminalRepository.search(text, status, dangerLevel);
	}

	public CriminalDTO findById(Long id) {
		return criminalRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Criminal", id));
	}

	public CriminalDTO create(CriminalDTO criminal) {
		criminal.setId(null); // el id lo asigna la base de datos, nunca el cliente
		normalize(criminal);
		return criminalRepository.create(criminal);
	}

	public CriminalDTO update(Long id, CriminalDTO criminal) {
		normalize(criminal);
		return criminalRepository.update(id, criminal).orElseThrow(() -> new ResourceNotFoundException("Criminal", id));
	}

	public void delete(Long id) {
		if (!criminalRepository.deleteById(id)) {
			throw new ResourceNotFoundException("Criminal", id);
		}
	}

	private void normalize(CriminalDTO criminal) {
		criminal.setName(trim(criminal.getName()));
		criminal.setAlias(trim(criminal.getAlias()));
		criminal.setCrime(trim(criminal.getCrime()));
	}

	private String trim(String value) {
		return value == null ? null : value.trim();
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}
}
