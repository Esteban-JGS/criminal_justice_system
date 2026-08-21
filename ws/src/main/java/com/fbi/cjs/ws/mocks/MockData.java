package com.fbi.cjs.ws.mocks;

import com.fbi.cjs.shared.dto.CriminalDTO;
import com.fbi.cjs.shared.dto.UserDTO;
import com.fbi.cjs.shared.enums.CriminalStatus;
import com.fbi.cjs.shared.enums.DangerLevel;
import com.fbi.cjs.shared.enums.Role;
import java.util.ArrayList;
import java.util.List;

/** Datos iniciales; el equivalente a un script de INSERT de arranque. */
public final class MockData {

	/** Contraseña de todos los usuarios de prueba. */
	public static final String DEFAULT_PASSWORD = "1234";

	private MockData() {
	}

	public static List<UserDTO> users() {
		List<UserDTO> users = new ArrayList<>();
		users.add(new UserDTO(1L, "Gustabo Garcia", "agente01", DEFAULT_PASSWORD, Role.AGENTE, true));
		users.add(new UserDTO(2L, "Jack Conway", "supervisor01", DEFAULT_PASSWORD, Role.SUPERVISOR, true));
		users.add(new UserDTO(3L, "Horacio Perez", "jefe01", DEFAULT_PASSWORD, Role.JEFE_FBI, true));
		return users;
	}

	public static List<CriminalDTO> criminals() {
		List<CriminalDTO> criminals = new ArrayList<>();
		criminals.add(new CriminalDTO(1L, "Victor Salazar", "El Fantasma", "Lavado de dinero", DangerLevel.ALTO,
				CriminalStatus.ACTIVO));
		criminals.add(new CriminalDTO(2L, "Ramón Oviedo", "El Toro", "Tráfico de armas", DangerLevel.ALTO,
				CriminalStatus.ACTIVO));
		criminals.add(new CriminalDTO(3L, "Sandra Quirós", "La Araña", "Fraude bancario", DangerLevel.MEDIO,
				CriminalStatus.ACTIVO));
		criminals.add(new CriminalDTO(4L, "Diego Montero", "El Sombra", "Extorsión", DangerLevel.MEDIO,
				CriminalStatus.CAPTURADO));
		criminals.add(new CriminalDTO(5L, "Luisa Fernández", "La Cobra", "Narcotráfico", DangerLevel.ALTO,
				CriminalStatus.ACTIVO));
		criminals.add(new CriminalDTO(6L, "Andrés Villalobos", "El Ciclón", "Robo a mano armada", DangerLevel.BAJO,
				CriminalStatus.CAPTURADO));
		criminals.add(new CriminalDTO(7L, "Patricia Mora", "La Sombra", "Espionaje corporativo", DangerLevel.MEDIO,
				CriminalStatus.ACTIVO));
		criminals.add(new CriminalDTO(8L, "Carlos Ureña", "El Escorpión", "Tráfico de personas", DangerLevel.ALTO,
				CriminalStatus.ACTIVO));
		criminals.add(new CriminalDTO(9L, "Miguel Bonilla", "El Rayo", "Hackeo de sistemas", DangerLevel.MEDIO,
				CriminalStatus.ACTIVO));
		criminals.add(new CriminalDTO(10L, "Gabriela Solano", "La Mantis", "Falsificación", DangerLevel.BAJO,
				CriminalStatus.FALLECIDO));
		return criminals;
	}
}
