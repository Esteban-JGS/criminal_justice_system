package com.fbi.cjs.ws.mocks;

import com.fbi.cjs.shared.dto.AgentDTO;
import com.fbi.cjs.shared.enums.AgentStatus;
import com.fbi.cjs.ws.repository.AgentRepository;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class MockAgentRepository implements AgentRepository {

	private final Map<Long, AgentDTO> store = new ConcurrentHashMap<>();

	private final AtomicLong sequence = new AtomicLong(0);

	@PostConstruct
	public void seed() {
		for (AgentDTO agent : MockData.agents()) {
			store.put(agent.getId(), agent);
			sequence.updateAndGet(current -> Math.max(current, agent.getId()));
		}
	}

	@Override
	public List<AgentDTO> findAll() {
		return store.values().stream().sorted(Comparator.comparing(AgentDTO::getId)).map(this::copy).toList();
	}

	@Override
	public List<AgentDTO> search(String text, AgentStatus status) {
		String needle = text == null ? null : text.toLowerCase(Locale.ROOT).trim();

		List<AgentDTO> result = new ArrayList<>();
		for (AgentDTO agent : findAll()) {
			if (status != null && agent.getStatus() != status) {
				continue;
			}
			if (needle != null && !needle.isEmpty() && !matchesText(agent, needle)) {
				continue;
			}
			result.add(agent);
		}
		return result;
	}

	@Override
	public Optional<AgentDTO> findById(Long id) {
		return Optional.ofNullable(store.get(id)).map(this::copy);
	}

	@Override
	public Optional<AgentDTO> findByBadgeNumber(String badgeNumber) {
		if (badgeNumber == null) {
			return Optional.empty();
		}
		return store.values().stream().filter(agent -> badgeNumber.equalsIgnoreCase(agent.getBadgeNumber())).findFirst()
				.map(this::copy);
	}

	@Override
	public boolean existsByBadgeNumber(String badgeNumber) {
		return findByBadgeNumber(badgeNumber).isPresent();
	}

	@Override
	public AgentDTO create(AgentDTO agent) {
		AgentDTO stored = copy(agent);
		stored.setId(sequence.incrementAndGet());
		store.put(stored.getId(), stored);
		return copy(stored);
	}

	@Override
	public Optional<AgentDTO> update(Long id, AgentDTO agent) {
		if (!store.containsKey(id)) {
			return Optional.empty();
		}
		AgentDTO stored = copy(agent);
		stored.setId(id);
		store.put(id, stored);
		return Optional.of(copy(stored));
	}

	private boolean matchesText(AgentDTO agent, String needle) {
		return contains(agent.getName(), needle) || contains(agent.getBadgeNumber(), needle)
				|| contains(agent.getDivision(), needle);
	}

	private boolean contains(String value, String needle) {
		return value != null && value.toLowerCase(Locale.ROOT).contains(needle);
	}

	private AgentDTO copy(AgentDTO source) {
		return new AgentDTO(source.getId(), source.getBadgeNumber(), source.getName(), source.getDivision(),
				source.getStatus());
	}
}
