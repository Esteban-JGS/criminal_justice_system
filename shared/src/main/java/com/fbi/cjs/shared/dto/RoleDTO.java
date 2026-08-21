package com.fbi.cjs.shared.dto;

import com.fbi.cjs.shared.enums.Role;

/**
 * Rol como elemento de catálogo: el valor que se envía a la API y el texto que
 * se muestra en un ComboBox del front.
 */
public class RoleDTO {

	private Role value;
	private String label;

	public RoleDTO() {
	}

	public RoleDTO(Role value) {
		this.value = value;
		this.label = value.getLabel();
	}

	public Role getValue() {
		return value;
	}

	public void setValue(Role value) {
		this.value = value;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public String toString() {
		return label;
	}
}
