package com.fbi.cjs.shared.dto;

import jakarta.validation.constraints.NotBlank;

/** Cuerpo del POST /auth/login. */
public class LoginRequestDTO {

	@NotBlank(message = "El usuario es obligatorio")
	private String username;

	@NotBlank(message = "La contraseña es obligatoria")
	private String password;

	public LoginRequestDTO() {
	}

	public LoginRequestDTO(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
