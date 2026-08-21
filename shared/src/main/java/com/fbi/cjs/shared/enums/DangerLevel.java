package com.fbi.cjs.shared.enums;

/** Nivel de peligrosidad de un criminal. */
public enum DangerLevel {
	BAJO("Bajo"), MEDIO("Medio"), ALTO("Alto");

	private final String label;

	DangerLevel(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}
