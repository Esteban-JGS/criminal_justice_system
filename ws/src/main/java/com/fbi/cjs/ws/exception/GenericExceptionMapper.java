package com.fbi.cjs.ws.exception;

import com.fbi.cjs.shared.api.ApiResponse;
import com.fbi.cjs.shared.api.ResponseCode;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Red de seguridad: cualquier excepción no contemplada termina aquí.
 *
 * <p>
 * Dos cosas importantes:
 * <ul>
 * <li>Las {@link WebApplicationException} (404 de ruta inexistente, 405 de
 * método no permitido...) conservan su código; si no, todo se vería como 500.
 * <li>Al cliente se le manda un mensaje genérico. El stack trace va al log de
 * Payara, nunca en la respuesta: filtrar detalles internos es parte de la
 * seguridad de una API.
 * </ul>
 */
@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {

	private static final Logger LOGGER = Logger.getLogger(GenericExceptionMapper.class.getName());

	@Override
	public Response toResponse(Throwable exception) {
		if (exception instanceof WebApplicationException webException) {
			Response original = webException.getResponse();
			ResponseCode code = fromStatus(original.getStatus());
			ApiResponse<Void> body = ApiResponse.error(code,
					webException.getMessage() == null ? code.name() : webException.getMessage());
			return Response.status(original.getStatus()).entity(body).type(MediaType.APPLICATION_JSON).build();
		}

		LOGGER.log(Level.SEVERE, "Error no controlado en la API", exception);

		ApiResponse<Void> body = ApiResponse.error(ResponseCode.INTERNAL_SERVER_ERROR,
				"Ocurrió un error interno. Revisá el log del servidor.");
		return Response.serverError().entity(body).type(MediaType.APPLICATION_JSON).build();
	}

	private ResponseCode fromStatus(int status) {
		for (ResponseCode code : ResponseCode.values()) {
			if (code.getHttpStatus() == status) {
				return code;
			}
		}
		return ResponseCode.INTERNAL_SERVER_ERROR;
	}
}
