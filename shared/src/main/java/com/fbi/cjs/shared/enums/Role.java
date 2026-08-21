package com.fbi.cjs.shared.enums;

/**
 * Roles de un usuario del sistema.
 *
 * <p>
 * Viaja como su nombre ({@code "SUPERVISOR"}), que es el valor por defecto
 * tanto en JSON-B (servidor) como en Jackson (cliente). {@link #getLabel()} es
 * solo para mostrar en pantalla.
 */
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
