package com.fbi.cjs.ws.exception;

import com.fbi.cjs.shared.api.ApiResponse;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class BusinessRuleExceptionMapper implements ExceptionMapper<BusinessRuleException> {

	@Override
	public Response toResponse(BusinessRuleException exception) {
		ApiResponse<Void> body = ApiResponse.error(exception.getCode(), exception.getMessage());
		return Response.status(exception.getCode().getHttpStatus()).entity(body).type(MediaType.APPLICATION_JSON)
				.build();
	}
}
