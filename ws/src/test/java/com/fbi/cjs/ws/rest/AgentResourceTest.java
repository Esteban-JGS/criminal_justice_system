package com.fbi.cjs.ws.rest;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fbi.cjs.shared.api.ApiPaths;
import com.fbi.cjs.shared.enums.Role;
import com.fbi.cjs.ws.security.AllowedRoles;
import com.fbi.cjs.ws.security.Secured;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * El contrato HTTP del recurso vive en sus anotaciones, y las anotaciones se
 * pueden leer por reflexion. Estas pruebas comprueban ese contrato sin
 * necesidad de desplegar en Payara.
 */
class AgentResourceTest {

	@Test
	@DisplayName("el recurso se publica en la ruta /agents")
	void rutaDelRecurso() {
		Path path = AgentResource.class.getAnnotation(Path.class);

		assertTrue(path != null, "AgentResource debe estar anotado con @Path");
		assertEquals(ApiPaths.AGENTS, path.value(),
				"la ruta debe salir de ApiPaths para no desincronizarse con el cliente");
	}

	@Test
	@DisplayName("el recurso exige token en todos sus metodos")
	void recursoProtegido() {
		assertTrue(AgentResource.class.isAnnotationPresent(Secured.class),
				"sin @Secured cualquiera consulta los agentes sin autenticarse");
	}

	@Test
	@DisplayName("el recurso produce y consume JSON")
	void tipoDeContenido() {
		Produces produces = AgentResource.class.getAnnotation(Produces.class);
		Consumes consumes = AgentResource.class.getAnnotation(Consumes.class);

		assertTrue(produces != null && Arrays.asList(produces.value()).contains(MediaType.APPLICATION_JSON),
				"falta @Produces(MediaType.APPLICATION_JSON)");
		assertTrue(consumes != null && Arrays.asList(consumes.value()).contains(MediaType.APPLICATION_JSON),
				"sin @Consumes el servidor responde 415 al recibir JSON");
	}

	@Test
	@DisplayName("los verbos HTTP estan en el metodo que corresponde")
	void verbosHttp() {
		assertAnnotated(method("list"), GET.class);
		assertAnnotated(method("findById"), GET.class);
		assertAnnotated(method("create"), POST.class);
		assertAnnotated(method("update"), PUT.class);
		assertAnnotated(method("delete"), DELETE.class);
	}

	@Test
	@DisplayName("crear devuelve Response para poder responder 201 con la cabecera Location")
	void createDevuelveResponse() {
		assertEquals(Response.class, method("create").getReturnType(),
				"un POST que crea algo responde 201 y el header Location; eso necesita construir un Response");
	}

	@Test
	@DisplayName("el cuerpo que entra se valida")
	void cuerpoValidado() {
		assertTrue(tieneValid(method("create")), "falta @Valid en el parametro de create");
		assertTrue(tieneValid(method("update")), "falta @Valid en el parametro de update");
	}

	@Test
	@DisplayName("solo supervisores y jefes registran o editan agentes")
	void permisosDeEscritura() {
		assertArrayEquals(new Role[]{Role.SUPERVISOR, Role.JEFE_FBI}, roles(method("create")));
		assertArrayEquals(new Role[]{Role.SUPERVISOR, Role.JEFE_FBI}, roles(method("update")));
	}

	@Test
	@DisplayName("solo el jefe elimina agentes")
	void permisoDeBorrado() {
		assertArrayEquals(new Role[]{Role.JEFE_FBI}, roles(method("delete")));
	}

	private Method method(String name) {
		return Arrays.stream(AgentResource.class.getDeclaredMethods()).filter(m -> m.getName().equals(name)).findFirst()
				.orElseThrow(() -> new AssertionError("AgentResource no tiene el metodo " + name));
	}

	private void assertAnnotated(Method method, Class<? extends Annotation> annotation) {
		assertTrue(method.isAnnotationPresent(annotation),
				method.getName() + " debe estar anotado con @" + annotation.getSimpleName());
	}

	private boolean tieneValid(Method method) {
		return Arrays.stream(method.getParameterAnnotations())
				.anyMatch(annotations -> Arrays.stream(annotations).anyMatch(a -> a instanceof Valid));
	}

	private Role[] roles(Method method) {
		AllowedRoles allowedRoles = method.getAnnotation(AllowedRoles.class);
		assertTrue(allowedRoles != null, "falta @AllowedRoles en " + method.getName());
		return allowedRoles.value();
	}
}
