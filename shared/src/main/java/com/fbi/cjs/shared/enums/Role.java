package com.fbi.cjs.shared.enums;

public enum Role {
	AGENTE("Agente"), SUPERVISOR("Supervisor"), JEFE_FBI("Jefe FBI");

	private final String label;

	Role(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}
