package com.fbi.cjs.ws.rest;

import com.fbi.cjs.shared.api.ApiPaths;
import com.fbi.cjs.shared.api.ApiResponse;
import com.fbi.cjs.shared.api.ResponseCode;
import com.fbi.cjs.shared.dto.CriminalDTO;
import com.fbi.cjs.shared.enums.CriminalStatus;
import com.fbi.cjs.shared.enums.DangerLevel;
import com.fbi.cjs.shared.enums.Role;
import com.fbi.cjs.ws.exception.BusinessRuleException;
import com.fbi.cjs.ws.security.AllowedRoles;
import com.fbi.cjs.ws.security.Secured;
import com.fbi.cjs.ws.service.CriminalService;
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
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Criminales.
 *
 * <pre>
 * GET    /criminals        listar (filtros: search, status, dangerLevel)
 * GET    /criminals/{id}   obtener uno
 * POST   /criminals        crear
 * PUT    /criminals/{id}   actualizar
 * DELETE /criminals/{id}   eliminar
 * </pre>
 *
 * <p>
 * {@code @Secured} exige token en toda la clase; {@link AllowedRoles} restringe
 * además por rol lo que modifica datos.
 */
@Path(ApiPaths.CRIMINALS)
@Secured
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CriminalResource {

	@Inject
	private CriminalService criminalService;

	/**
	 * Los filtros van como query params porque no identifican otro recurso: son la
	 * misma colección, filtrada.
	 */
	@GET
	public ApiResponse<List<CriminalDTO>> list(@QueryParam("search") String search, @QueryParam("status") String status,
			@QueryParam("dangerLevel") String dangerLevel) {

		List<CriminalDTO> criminals = criminalService.search(search, parseEnum(CriminalStatus.class, status, "status"),
				parseEnum(DangerLevel.class, dangerLevel, "dangerLevel"));

		return ApiResponse.ok("Se encontraron " + criminals.size() + " criminales.", criminals);
	}

	@GET
	@Path("{id}")
	public ApiResponse<CriminalDTO> findById(@PathParam("id") Long id) {
		return ApiResponse.ok("Criminal encontrado.", criminalService.findById(id));
	}

	/**
	 * {@code @Valid} dispara Bean Validation: si el DTO no cumple, la petición no
	 * llega al servicio y se responde 422.
	 *
	 * <p>
	 * Devuelve 201 con el header {@code Location} del recurso creado, como espera
	 * el estándar HTTP.
	 */
	@POST
	@AllowedRoles({Role.SUPERVISOR, Role.JEFE_FBI})
	public Response create(@Valid CriminalDTO criminal, @Context UriInfo uriInfo) {
		CriminalDTO created = criminalService.create(criminal);

		return Response.created(uriInfo.getAbsolutePathBuilder().path(String.valueOf(created.getId())).build())
				.entity(ApiResponse.created("Criminal registrado correctamente.", created)).build();
	}

	@PUT
	@Path("{id}")
	@AllowedRoles({Role.SUPERVISOR, Role.JEFE_FBI})
	public ApiResponse<CriminalDTO> update(@PathParam("id") Long id, @Valid CriminalDTO criminal) {
		return ApiResponse.ok("Criminal actualizado correctamente.", criminalService.update(id, criminal));
	}

	@DELETE
	@Path("{id}")
	@AllowedRoles(Role.JEFE_FBI)
	public ApiResponse<Void> delete(@PathParam("id") Long id) {
		criminalService.delete(id);
		return ApiResponse.ok("Criminal eliminado correctamente.", null);
	}

	/**
	 * Convierte el filtro a enum. Se hace a mano porque declarar el parámetro como
	 * enum haría que JAX-RS respondiera 404 ante un valor desconocido, y el recurso
	 * sí existe: lo inválido es el filtro.
	 */
	private <E extends Enum<E>> E parseEnum(Class<E> type, String value, String paramName) {
		if (value == null || value.isBlank()) {
			return null;
		}
		try {
			return Enum.valueOf(type, value.trim().toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException ex) {
			throw new BusinessRuleException(ResponseCode.UNPROCESSABLE_CONTENT, "Valor inválido para '" + paramName
					+ "': " + value + ". Valores válidos: " + Arrays.toString(type.getEnumConstants()));
		}
	}
}
