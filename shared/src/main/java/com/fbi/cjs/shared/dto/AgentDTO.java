package com.fbi.cjs.shared.dto;

import com.fbi.cjs.shared.enums.AgentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AgentDTO {

	private Long id;

	@NotBlank(message = "El numero de placa es obligatorio")
	@Pattern(regexp = "FBI-[0-9]{4}", message = "La placa debe tener el formato FBI-0000")
	private String badgeNumber;

	@NotBlank(message = "El nombre es obligatorio")
	@Size(max = 120, message = "El nombre no puede exceder 120 caracteres")
	private String name;

	@NotBlank(message = "La division es obligatoria")
	@Size(max = 80, message = "La division no puede exceder 80 caracteres")
	private String division;

	@NotNull(message = "El estado es obligatorio")
	private AgentStatus status;

	public AgentDTO(Long id, String badgeNumber, String name, String division, AgentStatus status) {
		this.id = id;
		this.badgeNumber = badgeNumber;
		this.name = name;
		this.division = division;
		this.status = status;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getBadgeNumber() {
		return badgeNumber;
	}

	public void setBadgeNumber(String badgeNumber) {
		this.badgeNumber = badgeNumber;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDivision() {
		return division;
	}

	public void setDivision(String division) {
		this.division = division;
	}

	public AgentStatus getStatus() {
		return status;
	}

	public void setStatus(AgentStatus status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return badgeNumber + " - " + name;
	}
}
