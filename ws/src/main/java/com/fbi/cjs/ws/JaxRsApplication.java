package com.fbi.cjs.ws;

import com.fbi.cjs.shared.api.ApiPaths;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath(ApiPaths.API_ROOT)
public class JaxRsApplication extends Application {
}
