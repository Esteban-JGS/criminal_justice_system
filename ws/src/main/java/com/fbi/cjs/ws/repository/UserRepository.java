package com.fbi.cjs.ws.repository;

import com.fbi.cjs.shared.dto.UserDTO;
import java.util.List;
import java.util.Optional;

public interface UserRepository {

	List<UserDTO> findAll();

	Optional<UserDTO> findById(Long id);

	Optional<UserDTO> findByUsername(String username);

	boolean existsByUsername(String username);

	UserDTO create(UserDTO user);

	Optional<UserDTO> update(Long id, UserDTO user);

	boolean deleteById(Long id);
}
