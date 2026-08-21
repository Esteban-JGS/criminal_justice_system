package com.fbi.cjs.ws.rest;

import com.fbi.cjs.shared.api.ApiPaths;
import com.fbi.cjs.shared.api.ApiResponse;
import com.fbi.cjs.shared.dto.LoginRequestDTO;
import com.fbi.cjs.shared.dto.LoginResponseDTO;
import com.fbi.cjs.shared.dto.UserDTO;
import com.fbi.cjs.ws.security.RequestUser;
import com.fbi.cjs.ws.security.Secured;
import com.fbi.cjs.ws.service.AuthService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;

@Path(ApiPaths.AUTH)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

	@Inject
	private AuthService authService;

	@POST
	@Path("login")
	public ApiResponse<LoginResponseDTO> login(@Valid LoginRequestDTO request) {
		LoginResponseDTO response = authService.login(request);
		return ApiResponse.ok("Bienvenido, " + response.getUser().getName() + ".", response);
	}

	@POST
	@Path("logout")
	@Secured
	public ApiResponse<Void> logout(@Context ContainerRequestContext requestContext) {
		authService.logout(RequestUser.tokenOf(requestContext));
		return ApiResponse.ok("Sesión cerrada.", null);
	}

	@GET
	@Path("me")
	@Secured
	public ApiResponse<UserDTO> me(@Context ContainerRequestContext requestContext) {
		return ApiResponse.ok("Sesión activa.", RequestUser.of(requestContext));
	}
}
