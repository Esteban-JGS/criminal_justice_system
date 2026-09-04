package com.fbi.cjs.ws.service;

import com.fbi.cjs.shared.api.ResponseCode;
import com.fbi.cjs.shared.dto.AgentDTO;
import com.fbi.cjs.shared.enums.AgentStatus;
import com.fbi.cjs.ws.exception.BusinessRuleException;
import com.fbi.cjs.ws.exception.ResourceNotFoundException;
import com.fbi.cjs.ws.repository.AgentRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class AgentService {

	@Inject
	AgentRepository agentRepository;

	public List<AgentDTO> search(String text, AgentStatus status) {
		if (isBlank(text) && status == null) {
			return agentRepository.findAll();
		}
		return agentRepository.search(text, status);
	}

	public AgentDTO findById(Long id) {
		return agentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Agente", id));
	}

	public AgentDTO create(AgentDTO agent) {
		agent.setId(null); // el id lo asigna la base de datos, nunca el cliente
		normalize(agent);

		if (agentRepository.existsByBadgeNumber(agent.getBadgeNumber())) {
			throw new BusinessRuleException(ResponseCode.CONFLICT,
					"Ya existe un agente con la placa '" + agent.getBadgeNumber() + "'.");
		}
		return agentRepository.create(agent);
	}

	public AgentDTO update(Long id, AgentDTO agent) {
		normalize(agent);

		// La placa puede repetirse solo si el dueño es este mismo agente.
		Optional<AgentDTO> owner = agentRepository.findByBadgeNumber(agent.getBadgeNumber());
		if (owner.isPresent() && !owner.get().getId().equals(id)) {
			throw new BusinessRuleException(ResponseCode.CONFLICT,
					"La placa '" + agent.getBadgeNumber() + "' ya esta asignada a otro agente.");
		}

		return agentRepository.update(id, agent).orElseThrow(() -> new ResourceNotFoundException("Agente", id));
	}

	public void delete(Long id) {
		if (!agentRepository.deleteById(id)) {
			throw new ResourceNotFoundException("Agente", id);
		}
	}

	private void normalize(AgentDTO agent) {
		agent.setBadgeNumber(trim(agent.getBadgeNumber()));
		agent.setName(trim(agent.getName()));
		agent.setDivision(trim(agent.getDivision()));
	}

	private String trim(String value) {
		return value == null ? null : value.trim();
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}
}
