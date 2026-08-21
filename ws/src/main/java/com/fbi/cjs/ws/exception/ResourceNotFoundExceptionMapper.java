package com.fbi.cjs.ws.exception;

import com.fbi.cjs.shared.api.ApiResponse;
import com.fbi.cjs.shared.api.ResponseCode;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ResourceNotFoundExceptionMapper implements ExceptionMapper<ResourceNotFoundException> {

	@Override
	public Response toResponse(ResourceNotFoundException exception) {
		ApiResponse<Void> body = ApiResponse.error(ResponseCode.NOT_FOUND, exception.getMessage());
		return Response.status(Response.Status.NOT_FOUND).entity(body).type(MediaType.APPLICATION_JSON).build();
	}
}
