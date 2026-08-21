package com.fbi.cjs.ws.rest;

import com.fbi.cjs.shared.api.ApiPaths;
import com.fbi.cjs.shared.api.ApiResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Endpoint público de diagnóstico: {@code GET /api/v1/health}.
 *
 * <p>
 * Primer lugar donde mirar cuando "no funciona": si esto responde, el WAR está
 * desplegado y JAX-RS arrancó bien, así que el problema está en la petición, no
 * en el servidor.
 *
 * <p>
 * {@code dataSource: MOCK} recuerda que los datos son de mentira. Cuando se
 * conecte Oracle, ese valor debe pasar a {@code ORACLE}.
 */
@Path(ApiPaths.HEALTH)
@Produces(MediaType.APPLICATION_JSON)
public class HealthResource {

	@GET
	public ApiResponse<Map<String, String>> health() {
		Map<String, String> info = new LinkedHashMap<>();
		info.put("application", "criminal-justice-system-ws");
		info.put("apiVersion", ApiPaths.API_ROOT);
		info.put("dataSource", "MOCK");
		info.put("serverTime", OffsetDateTime.now().toString());
		info.put("javaVersion", System.getProperty("java.version"));

		return ApiResponse.ok("El servicio está arriba.", info);
	}
}
