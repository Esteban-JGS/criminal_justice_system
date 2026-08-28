package com.fbi.cjs.shared.enums;

public enum AgentStatus {
	ACTIVO("Activo"), SUSPENDIDO("Suspendido"), RETIRADO("Retirado");

	private final String label;

	AgentStatus(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}
