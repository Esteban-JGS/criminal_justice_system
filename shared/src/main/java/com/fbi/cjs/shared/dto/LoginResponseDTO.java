package com.fbi.cjs.shared.dto;

/**
 * Respuesta de un login exitoso: el token que hay que mandar en el header
 * {@code Authorization: Bearer <token>} y los datos del usuario autenticado.
 */
public class LoginResponseDTO {

	private String token;
	private String expiresAt;
	private UserDTO user;

	public LoginResponseDTO() {
	}

	public LoginResponseDTO(String token, String expiresAt, UserDTO user) {
		this.token = token;
		this.expiresAt = expiresAt;
		this.user = user;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getExpiresAt() {
		return expiresAt;
	}

	public void setExpiresAt(String expiresAt) {
		this.expiresAt = expiresAt;
	}

	public UserDTO getUser() {
		return user;
	}

	public void setUser(UserDTO user) {
		this.user = user;
	}
}
