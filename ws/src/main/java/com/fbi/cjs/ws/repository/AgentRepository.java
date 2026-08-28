package com.fbi.cjs.ws.repository;

import com.fbi.cjs.shared.dto.AgentDTO;
import com.fbi.cjs.shared.enums.AgentStatus;
import java.util.List;
import java.util.Optional;

public interface AgentRepository {

	List<AgentDTO> findAll();

	List<AgentDTO> search(String text, AgentStatus status);

	Optional<AgentDTO> findById(Long id);

	Optional<AgentDTO> findByBadgeNumber(String badgeNumber);

	boolean existsByBadgeNumber(String badgeNumber);

	AgentDTO create(AgentDTO agent);

	Optional<AgentDTO> update(Long id, AgentDTO agent);

	boolean deleteById(Long id);
}
