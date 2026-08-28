package com.fbi.cjs.ws.rest;

import com.fbi.cjs.shared.api.ApiResponse;
import com.fbi.cjs.shared.api.ResponseCode;
import com.fbi.cjs.shared.dto.AgentDTO;
import com.fbi.cjs.shared.enums.AgentStatus;
import com.fbi.cjs.shared.enums.Role;
import com.fbi.cjs.ws.exception.BusinessRuleException;
import com.fbi.cjs.ws.security.AllowedRoles;
import com.fbi.cjs.ws.service.AgentService;
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
import jakarta.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Path("agent")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.TEXT_PLAIN)
public class AgentResource {

	@Inject
	AgentService agentService;

	@GET
	public ApiResponse<List<AgentDTO>> list(@QueryParam("search") String search, @QueryParam("status") String status) {
		List<AgentDTO> agents = agentService.search(search, parseStatus(status));

		return ApiResponse.ok("Se encontraron " + agents.size() + " agentes.", agents);
	}

	@GET
	@Path("{id}")
	public ApiResponse<AgentDTO> findById(@PathParam("id") Long id) {
		return ApiResponse.ok("Agente encontrado.", agentService.findById(id));
	}

	@POST
	@AllowedRoles({Role.SUPERVISOR, Role.JEFE_FBI})
	public ApiResponse<AgentDTO> create(@Valid AgentDTO agent) {
		return ApiResponse.created("Agente registrado correctamente.", agentService.create(agent));
	}

	@PUT
	@Path("{id}")
	@AllowedRoles({Role.SUPERVISOR, Role.JEFE_FBI})
	public ApiResponse<AgentDTO> update(@PathParam("id") Long id, @Valid AgentDTO agent) {
		return ApiResponse.ok("Agente actualizado correctamente.", agentService.update(id, agent));
	}

	@DELETE
	@Path("{id}")
	@AllowedRoles(Role.JEFE_FBI)
	public ApiResponse<Void> delete(@PathParam("id") Long id) {
		agentService.delete(id);
		return ApiResponse.ok("Agente eliminado correctamente.", null);
	}

	private AgentStatus parseStatus(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		try {
			return AgentStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException ex) {
			throw new BusinessRuleException(ResponseCode.UNPROCESSABLE_CONTENT, "Valor invalido para 'status': " + value
					+ ". Valores validos: " + Arrays.toString(AgentStatus.values()));
		}
	}
}
