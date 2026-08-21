package com.fbi.cjs.shared.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * El sobre de respuesta es el contrato entre el WS y el front: si cambia, los
 * dos lados se rompen a la vez.
 */
class ApiResponseTest {

	@Test
	@DisplayName("ok arma un 200 con datos y sin errores")
	void ok() {
		ApiResponse<String> response = ApiResponse.ok("Todo bien", "contenido");

		assertEquals(200, response.getStatus());
		assertEquals(ResponseCode.OK, response.getCode());
		assertEquals("contenido", response.getData());
		assertNull(response.getErrors());
		assertTrue(response.isSuccess());
	}

	@Test
	@DisplayName("created arma un 201")
	void created() {
		ApiResponse<String> response = ApiResponse.created("Creado", "contenido");

		assertEquals(201, response.getStatus());
		assertTrue(response.isSuccess());
	}

	@Test
	@DisplayName("error no trae datos y no es exitoso")
	void error() {
		ApiResponse<Void> response = ApiResponse.error(ResponseCode.NOT_FOUND, "No existe");

		assertEquals(404, response.getStatus());
		assertNull(response.getData());
		assertFalse(response.isSuccess());
	}

	@Test
	@DisplayName("los errores de validacion viajan en la lista de errors")
	void erroresDeValidacion() {
		ApiResponse<Void> response = ApiResponse.error(ResponseCode.UNPROCESSABLE_CONTENT, "Datos invalidos",
				List.of("name: obligatorio"));

		assertEquals(422, response.getStatus());
		assertEquals(1, response.getErrors().size());
	}

	@Test
	@DisplayName("el status siempre coincide con el codigo HTTP del enum")
	void statusCoincideConElCodigo() {
		for (ResponseCode code : ResponseCode.values()) {
			assertEquals(code.getHttpStatus(), ApiResponse.error(code, "x").getStatus(), code.name());
		}
	}
}
