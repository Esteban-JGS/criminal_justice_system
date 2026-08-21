// AuthService.java
package com.fbi.criminal_justice_system.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fbi.cjs.shared.api.ApiResponse;
import com.fbi.cjs.shared.dto.LoginRequestDTO;
import com.fbi.cjs.shared.dto.LoginResponseDTO;
import com.fbi.cjs.shared.dto.UserDTO;
import com.fbi.criminal_justice_system.utils.ApiClient;
import com.fbi.criminal_justice_system.utils.Session;

/**
 * Autenticación contra el web service.
 *
 * <p>
 * Antes había una lista de usuarios quemada acá. Ahora valida el WS: una
 * comprobación que vive en el cliente se salta editando el cliente.
 *
 * <p>
 * Métodos bloqueantes: usar dentro de {@code BackgroundTask}.
 */
public class AuthService {

	private final ApiClient apiClient = ApiClient.getInstance();

	/**
	 * Inicia sesión y deja el token guardado en {@link Session}. A partir de ahí,
	 * {@code ApiClient} lo adjunta solo en cada petición.
	 *
	 * @return el usuario autenticado
	 * @throws com.fbi.criminal_justice_system.utils.ApiException
	 *             si las credenciales son inválidas (401) o el servidor no responde
	 */
	public UserDTO login(String username, String password) {
		LoginResponseDTO response = apiClient.post("/auth/login", new LoginRequestDTO(username, password),
				new TypeReference<ApiResponse<LoginResponseDTO>>() {
				});

		Session.start(response.getToken(), response.getUser());
		return response.getUser();
	}

	/**
	 * Cierra la sesión.
	 *
	 * <p>
	 * La sesión local se limpia pase lo que pase: si el servidor no contesta, el
	 * usuario igual tiene que quedar deslogueado en la aplicación.
	 */
	public void logout() {
		try {
			apiClient.post("/auth/logout", null, new TypeReference<ApiResponse<Void>>() {
			});
		} catch (RuntimeException ignored) {
			// El token vence solo en el servidor; no vale la pena molestar al usuario.
		} finally {
			Session.clear();
		}
	}
}
