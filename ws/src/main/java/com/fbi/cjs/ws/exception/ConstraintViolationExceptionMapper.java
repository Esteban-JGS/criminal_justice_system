package com.fbi.cjs.ws.exception;

import com.fbi.cjs.shared.api.ApiResponse;
import com.fbi.cjs.shared.api.ResponseCode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.List;

/**
 * Convierte los errores de Bean Validation en un 422 con la lista de campos que
 * fallaron. Sin esto, un {@code @NotBlank} incumplido devuelve un cuerpo enorme
 * y dependiente del servidor.
 */
@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

	@Override
	public Response toResponse(ConstraintViolationException exception) {
		List<String> errors = exception.getConstraintViolations().stream().map(this::describe).sorted().toList();

		ApiResponse<Void> body = ApiResponse.error(ResponseCode.UNPROCESSABLE_CONTENT,
				"Los datos enviados no son válidos.", errors);

		return Response.status(ResponseCode.UNPROCESSABLE_CONTENT.getHttpStatus()).entity(body)
				.type(MediaType.APPLICATION_JSON).build();
	}

	/** Deja "createCriminal.arg0.name" como "name: El nombre es obligatorio". */
	private String describe(ConstraintViolation<?> violation) {
		String path = violation.getPropertyPath().toString();
		int lastDot = path.lastIndexOf('.');
		String field = lastDot >= 0 ? path.substring(lastDot + 1) : path;
		return field + ": " + violation.getMessage();
	}
}
