package com.fbi.cjs.ws.rest;

import com.fbi.cjs.shared.api.ApiPaths;
import com.fbi.cjs.shared.api.ApiResponse;
import com.fbi.cjs.shared.dto.UserDTO;
import com.fbi.cjs.shared.enums.Role;
import com.fbi.cjs.ws.security.AllowedRoles;
import com.fbi.cjs.ws.security.Secured;
import com.fbi.cjs.ws.service.UserService;
import com.fbi.cjs.ws.security.RequestUser;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.util.List;

/**
 * Usuarios. Consultar es de supervisores y jefes; administrar, solo del jefe.
 *
 * <p>
 * La contraseña nunca sale en las respuestas. En el PUT es opcional: si no
 * viene, se conserva la actual.
 */
@Path(ApiPaths.USERS)
@Secured
@AllowedRoles({Role.SUPERVISOR, Role.JEFE_FBI})
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

	@Inject
	private UserService userService;

	@GET
	public ApiResponse<List<UserDTO>> list() {
		List<UserDTO> users = userService.findAll();
		return ApiResponse.ok("Se encontraron " + users.size() + " usuarios.", users);
	}

	@GET
	@Path("{id}")
	public ApiResponse<UserDTO> findById(@PathParam("id") Long id) {
		return ApiResponse.ok("Usuario encontrado.", userService.findById(id));
	}

	@POST
	@AllowedRoles(Role.JEFE_FBI)
	public Response create(@Valid UserDTO user, @Context UriInfo uriInfo) {
		UserDTO created = userService.create(user);

		return Response.created(uriInfo.getAbsolutePathBuilder().path(String.valueOf(created.getId())).build())
				.entity(ApiResponse.created("Usuario creado correctamente.", created)).build();
	}

	@PUT
	@Path("{id}")
	@AllowedRoles(Role.JEFE_FBI)
	public ApiResponse<UserDTO> update(@PathParam("id") Long id, @Valid UserDTO user,
			@Context ContainerRequestContext requestContext) {

		UserDTO actingUser = RequestUser.of(requestContext);
		return ApiResponse.ok("Usuario actualizado correctamente.", userService.update(id, user, actingUser.getId()));
	}

	@DELETE
	@Path("{id}")
	@AllowedRoles(Role.JEFE_FBI)
	public ApiResponse<Void> delete(@PathParam("id") Long id, @Context ContainerRequestContext requestContext) {
		userService.delete(id, RequestUser.of(requestContext).getId());
		return ApiResponse.ok("Usuario eliminado correctamente.", null);
	}
}
