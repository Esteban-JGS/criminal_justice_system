package com.fbi.cjs.ws.mocks;

import com.fbi.cjs.shared.dto.UserDTO;
import com.fbi.cjs.ws.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * {@link UserRepository} en memoria.
 *
 * <p>
 * Las contraseñas se guardan en texto plano porque son datos de prueba. Con
 * Oracle hay que guardar un hash (BCrypt/Argon2) y comparar hashes.
 */
@ApplicationScoped
public class MockUserRepository implements UserRepository {

	private final Map<Long, UserDTO> store = new ConcurrentHashMap<>();
	private final AtomicLong sequence = new AtomicLong(0);

	@PostConstruct
	void seed() {
		for (UserDTO user : MockData.users()) {
			store.put(user.getId(), user);
			sequence.updateAndGet(current -> Math.max(current, user.getId()));
		}
	}

	@Override
	public List<UserDTO> findAll() {
		return store.values().stream().sorted(Comparator.comparing(UserDTO::getId)).map(this::copyWithoutPassword)
				.toList();
	}

	@Override
	public Optional<UserDTO> findById(Long id) {
		return Optional.ofNullable(store.get(id)).map(this::copyWithoutPassword);
	}

	@Override
	public Optional<UserDTO> findByUsername(String username) {
		if (username == null) {
			return Optional.empty();
		}
		return store.values().stream().filter(user -> username.equalsIgnoreCase(user.getUsername())).findFirst()
				.map(this::copy);
	}

	@Override
	public boolean existsByUsername(String username) {
		return findByUsername(username).isPresent();
	}

	@Override
	public UserDTO create(UserDTO user) {
		UserDTO stored = copy(user);
		stored.setId(sequence.incrementAndGet());
		store.put(stored.getId(), stored);
		return copyWithoutPassword(stored);
	}

	@Override
	public Optional<UserDTO> update(Long id, UserDTO user) {
		UserDTO current = store.get(id);
		if (current == null) {
			return Optional.empty();
		}
		UserDTO stored = copy(user);
		stored.setId(id);
		// Si el cliente no envía contraseña, se conserva la que ya tenía.
		if (stored.getPassword() == null || stored.getPassword().isBlank()) {
			stored.setPassword(current.getPassword());
		}
		store.put(id, stored);
		return Optional.of(copyWithoutPassword(stored));
	}

	@Override
	public boolean deleteById(Long id) {
		return store.remove(id) != null;
	}

	private UserDTO copy(UserDTO source) {
		return new UserDTO(source.getId(), source.getName(), source.getUsername(), source.getPassword(),
				source.getRole(), source.getActive());
	}

	private UserDTO copyWithoutPassword(UserDTO source) {
		UserDTO copy = copy(source);
		copy.setPassword(null);
		return copy;
	}
}
