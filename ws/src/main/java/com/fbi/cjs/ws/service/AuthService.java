package com.fbi.cjs.ws.service;

import com.fbi.cjs.shared.dto.LoginRequestDTO;
import com.fbi.cjs.shared.dto.LoginResponseDTO;
import com.fbi.cjs.shared.dto.UserDTO;
import com.fbi.cjs.ws.exception.AuthenticationException;
import com.fbi.cjs.ws.repository.UserRepository;
import com.fbi.cjs.ws.security.TokenStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Autenticación de usuarios.
 *
 * <p>
 * El mensaje de error es el mismo si el usuario no existe o si la contraseña
 * está mal: distinguirlos le confirma a un atacante qué usuarios son válidos.
 *
 * <p>
 * La comparación de contraseñas es literal porque los datos son mocks; con
 * Oracle se compara el hash.
 */
@ApplicationScoped
public class AuthService {

	@Inject
	UserRepository userRepository;

	@Inject
	TokenStore tokenStore;

	public LoginResponseDTO login(LoginRequestDTO request) {
		UserDTO user = userRepository.findByUsername(request.getUsername())
				.orElseThrow(() -> new AuthenticationException("Usuario o contraseña incorrectos."));

		if (!user.getPassword().equals(request.getPassword())) {
			throw new AuthenticationException("Usuario o contraseña incorrectos.");
		}
		if (Boolean.FALSE.equals(user.getActive())) {
			throw new AuthenticationException("El usuario está inactivo. Contactá al administrador.");
		}

		// A partir de aquí el usuario ya no lleva contraseña a ningún lado.
		user.setPassword(null);

		TokenStore.IssuedToken issued = tokenStore.issue(user);
		return new LoginResponseDTO(issued.token(), issued.session().expiresAt().toString(), user);
	}

	public void logout(String token) {
		tokenStore.revoke(token);
	}
}
