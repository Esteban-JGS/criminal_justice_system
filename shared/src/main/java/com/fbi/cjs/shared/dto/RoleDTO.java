package com.fbi.cjs.shared.dto;

import com.fbi.cjs.shared.enums.Role;

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
