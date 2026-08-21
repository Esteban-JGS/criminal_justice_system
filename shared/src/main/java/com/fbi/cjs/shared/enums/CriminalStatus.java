package com.fbi.cjs.shared.enums;

public enum CriminalStatus {
	ACTIVO("Activo"), CAPTURADO("Capturado"), FALLECIDO("Fallecido");

	private final String label;

	CriminalStatus(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}
