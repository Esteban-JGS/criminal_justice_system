package com.fbi.cjs.ws.rest;

import com.fbi.cjs.shared.api.ApiPaths;
import com.fbi.cjs.shared.api.ApiResponse;
import com.fbi.cjs.shared.dto.RoleDTO;
import com.fbi.cjs.shared.enums.Role;
import com.fbi.cjs.ws.security.Secured;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.List;

/**
 * Catálogo de roles: {@code GET /api/v1/roles}.
 *
 * <p>
 * Es de solo lectura porque los roles son un enum del código, no una tabla
 * editable. Sirve para llenar un ComboBox en el front sin que este tenga que
 * hardcodear la lista.
 *
 * <p>
 * Si algún día los roles se administran desde la aplicación, este recurso pasa
 * a tener POST/PUT/DELETE y el enum se convierte en tabla; la URL no cambia.
 */
@Path(ApiPaths.ROLES)
@Secured
@Produces(MediaType.APPLICATION_JSON)
public class RoleResource {

	@GET
	public ApiResponse<List<RoleDTO>> list() {
		List<RoleDTO> roles = Arrays.stream(Role.values()).map(RoleDTO::new).toList();
		return ApiResponse.ok("Catálogo de roles.", roles);
	}
}
