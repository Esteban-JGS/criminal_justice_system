package com.fbi.cjs.ws.mocks;

import com.fbi.cjs.shared.dto.CriminalDTO;
import com.fbi.cjs.shared.enums.CriminalStatus;
import com.fbi.cjs.shared.enums.DangerLevel;
import com.fbi.cjs.ws.repository.CriminalRepository;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * {@link CriminalRepository} en memoria.
 *
 * <p>
 * Al ser {@code @ApplicationScoped} hay una sola instancia para toda la
 * aplicación y varias peticiones pueden entrar a la vez, de ahí las estructuras
 * concurrentes. Lo que entra y sale se copia: devolver la instancia guardada
 * permitiría modificar los datos sin pasar por el repositorio.
 */
@ApplicationScoped
public class MockCriminalRepository implements CriminalRepository {

	private final Map<Long, CriminalDTO> store = new ConcurrentHashMap<>();

	/** Hace las veces de la secuencia de Oracle. */
	private final AtomicLong sequence = new AtomicLong(0);

	/** Público para que las pruebas puedan sembrar los datos sin levantar CDI. */
	@PostConstruct
	public void seed() {
		for (CriminalDTO criminal : MockData.criminals()) {
			store.put(criminal.getId(), criminal);
			sequence.updateAndGet(current -> Math.max(current, criminal.getId()));
		}
	}

	@Override
	public List<CriminalDTO> findAll() {
		return store.values().stream().sorted(Comparator.comparing(CriminalDTO::getId)).map(this::copy).toList();
	}

	@Override
	public List<CriminalDTO> search(String text, CriminalStatus status, DangerLevel dangerLevel) {
		String needle = text == null ? null : text.toLowerCase(Locale.ROOT).trim();

		List<CriminalDTO> result = new ArrayList<>();
		for (CriminalDTO criminal : findAll()) {
			if (status != null && criminal.getStatus() != status) {
				continue;
			}
			if (dangerLevel != null && criminal.getDangerLevel() != dangerLevel) {
				continue;
			}
			if (needle != null && !needle.isEmpty() && !matchesText(criminal, needle)) {
				continue;
			}
			result.add(criminal);
		}
		return result;
	}

	@Override
	public Optional<CriminalDTO> findById(Long id) {
		return Optional.ofNullable(store.get(id)).map(this::copy);
	}

	@Override
	public CriminalDTO create(CriminalDTO criminal) {
		CriminalDTO stored = copy(criminal);
		stored.setId(sequence.incrementAndGet());
		store.put(stored.getId(), stored);
		return copy(stored);
	}

	@Override
	public Optional<CriminalDTO> update(Long id, CriminalDTO criminal) {
		if (!store.containsKey(id)) {
			return Optional.empty();
		}
		CriminalDTO stored = copy(criminal);
		stored.setId(id); // el id lo manda la URL, no el cuerpo
		store.put(id, stored);
		return Optional.of(copy(stored));
	}

	@Override
	public boolean deleteById(Long id) {
		return store.remove(id) != null;
	}

	private boolean matchesText(CriminalDTO criminal, String needle) {
		return contains(criminal.getName(), needle) || contains(criminal.getAlias(), needle)
				|| contains(criminal.getCrime(), needle);
	}

	private boolean contains(String value, String needle) {
		return value != null && value.toLowerCase(Locale.ROOT).contains(needle);
	}

	private CriminalDTO copy(CriminalDTO source) {
		return new CriminalDTO(source.getId(), source.getName(), source.getAlias(), source.getCrime(),
				source.getDangerLevel(), source.getStatus());
	}
}
