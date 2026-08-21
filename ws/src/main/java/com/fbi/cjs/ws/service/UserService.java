package com.fbi.cjs.ws.service;

import com.fbi.cjs.shared.api.ResponseCode;
import com.fbi.cjs.shared.dto.UserDTO;
import com.fbi.cjs.ws.exception.BusinessRuleException;
import com.fbi.cjs.ws.exception.ResourceNotFoundException;
import com.fbi.cjs.ws.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Optional;

/**
 * Reglas de negocio de usuarios.
 *
 * <p>
 * Dos que no puede expresar una anotación de validación, porque necesitan mirar
 * los datos: el {@code username} es único, y nadie puede quitarse a sí mismo el
 * acceso —cambiarse el rol, desactivarse o borrarse— porque el sistema podría
 * quedarse sin ningún jefe.
 */
@ApplicationScoped
public class UserService {

	/**
	 * Sin {@code private} a propósito: así las pruebas del mismo paquete le pueden
	 * poner un repositorio sin levantar CDI.
	 */
	@Inject
	UserRepository userRepository;

	public List<UserDTO> findAll() {
		return userRepository.findAll();
	}

	public UserDTO findById(Long id) {
		return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Usuario", id));
	}

	public UserDTO create(UserDTO user) {
		user.setId(null);
		user.setUsername(trim(user.getUsername()));
		user.setName(trim(user.getName()));

		if (user.getPassword() == null || user.getPassword().isBlank()) {
			throw new BusinessRuleException(ResponseCode.UNPROCESSABLE_CONTENT,
					"La contraseña es obligatoria al crear un usuario.");
		}
		if (userRepository.existsByUsername(user.getUsername())) {
			throw new BusinessRuleException(ResponseCode.CONFLICT,
					"Ya existe un usuario con el nombre de usuario '" + user.getUsername() + "'.");
		}
		if (user.getActive() == null) {
			user.setActive(Boolean.TRUE);
		}
		return userRepository.create(user);
	}

	/**
	 * @param actingUserId
	 *            quién está haciendo el cambio, para impedir que se quite a sí
	 *            mismo el acceso
	 */
	public UserDTO update(Long id, UserDTO user, Long actingUserId) {
		UserDTO current = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Usuario", id));

		user.setUsername(trim(user.getUsername()));
		user.setName(trim(user.getName()));

		// El username puede repetirse solo si el dueño es este mismo usuario.
		Optional<UserDTO> owner = userRepository.findByUsername(user.getUsername());
		if (owner.isPresent() && !owner.get().getId().equals(id)) {
			throw new BusinessRuleException(ResponseCode.CONFLICT,
					"El nombre de usuario '" + user.getUsername() + "' ya está tomado.");
		}

		if (id.equals(actingUserId)) {
			if (user.getRole() != null && user.getRole() != current.getRole()) {
				throw new BusinessRuleException(ResponseCode.CONFLICT, "No podés cambiar tu propio rol.");
			}
			if (Boolean.FALSE.equals(user.getActive())) {
				throw new BusinessRuleException(ResponseCode.CONFLICT, "No podés desactivar tu propio usuario.");
			}
		}

		return userRepository.update(id, user).orElseThrow(() -> new ResourceNotFoundException("Usuario", id));
	}

	public void delete(Long id, Long actingUserId) {
		if (id.equals(actingUserId)) {
			throw new BusinessRuleException(ResponseCode.CONFLICT, "No podés eliminar tu propio usuario.");
		}
		if (!userRepository.deleteById(id)) {
			throw new ResourceNotFoundException("Usuario", id);
		}
	}

	private String trim(String value) {
		return value == null ? null : value.trim();
	}
}
