package com.fbi.cjs.ws;

import com.fbi.cjs.shared.api.ApiPaths;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * Arranque de JAX-RS. La anotación define el prefijo de todas las rutas y el
 * servidor descubre solo las clases con {@code @Path} y {@code @Provider}, sin
 * necesidad de web.xml.
 *
 * <pre>
 * http://localhost:8080/criminal_justice_ws/api/v1/criminals
 *                      context-root         prefijo  @Path
 * </pre>
 */
@ApplicationPath(ApiPaths.API_ROOT)
public class JaxRsApplication extends Application {
}
