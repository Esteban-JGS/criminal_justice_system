package com.fbi.cjs.shared.dto;

import com.fbi.cjs.shared.enums.CriminalStatus;
import com.fbi.cjs.shared.enums.DangerLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Criminal tal como viaja por la red.
 *
 * <p>
 * Un DTO no es una entidad: no tiene anotaciones de JPA ni lógica. Al conectar
 * Oracle habrá además una entidad {@code Criminal} y un mapper entre ambas; el
 * front nunca ve la entidad.
 */
public class CriminalDTO {

	private Long id;

	@NotBlank(message = "El nombre es obligatorio")
	@Size(max = 120, message = "El nombre no puede exceder 120 caracteres")
	private String name;

	@Size(max = 60, message = "El alias no puede exceder 60 caracteres")
	private String alias;

	@NotBlank(message = "El delito es obligatorio")
	@Size(max = 200, message = "El delito no puede exceder 200 caracteres")
	private String crime;

	@NotNull(message = "El nivel de peligrosidad es obligatorio")
	private DangerLevel dangerLevel;

	@NotNull(message = "El estado es obligatorio")
	private CriminalStatus status;

	public CriminalDTO() {
	}

	public CriminalDTO(Long id, String name, String alias, String crime, DangerLevel dangerLevel,
			CriminalStatus status) {
		this.id = id;
		this.name = name;
		this.alias = alias;
		this.crime = crime;
		this.dangerLevel = dangerLevel;
		this.status = status;
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

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getCrime() {
		return crime;
	}

	public void setCrime(String crime) {
		this.crime = crime;
	}

	public DangerLevel getDangerLevel() {
		return dangerLevel;
	}

	public void setDangerLevel(DangerLevel dangerLevel) {
		this.dangerLevel = dangerLevel;
	}

	public CriminalStatus getStatus() {
		return status;
	}

	public void setStatus(CriminalStatus status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return name + (alias != null && !alias.isBlank() ? " (" + alias + ")" : "");
	}
}
