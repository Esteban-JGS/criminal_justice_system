package com.fbi.cjs.shared.dto;

import com.fbi.cjs.shared.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UserDTO {

	private Long id;

	@NotBlank(message = "El nombre es obligatorio")
	@Size(max = 120, message = "El nombre no puede exceder 120 caracteres")
	private String name;

	@NotBlank(message = "El nombre de usuario es obligatorio")
	@Size(min = 4, max = 40, message = "El usuario debe tener entre 4 y 40 caracteres")
	private String username;

	@Size(min = 4, max = 60, message = "La contraseña debe tener entre 4 y 60 caracteres")
	private String password;

	@NotNull(message = "El rol es obligatorio")
	private Role role;

	private Boolean active = Boolean.TRUE;

	public UserDTO() {
	}

	public UserDTO(Long id, String name, String username, String password, Role role, Boolean active) {
		this.id = id;
		this.name = name;
		this.username = username;
		this.password = password;
		this.role = role;
		this.active = active;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	@Override
	public String toString() {
		return name;
	}
}
