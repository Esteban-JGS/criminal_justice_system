package com.fbi.cjs.ws.exception;

import com.fbi.cjs.shared.api.ApiResponse;
import com.fbi.cjs.shared.api.ResponseCode;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class AuthenticationExceptionMapper implements ExceptionMapper<AuthenticationException> {

	@Override
	public Response toResponse(AuthenticationException exception) {
		ApiResponse<Void> body = ApiResponse.error(ResponseCode.UNAUTHORIZED, exception.getMessage());
		return Response.status(Response.Status.UNAUTHORIZED)
				.header(HttpHeaders.WWW_AUTHENTICATE, "Bearer realm=\"criminal-justice-system\"").entity(body)
				.type(MediaType.APPLICATION_JSON).build();
	}
}
