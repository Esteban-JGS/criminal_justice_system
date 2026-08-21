package com.fbi.criminal_justice_system.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fbi.cjs.shared.api.ApiResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ApiClient {

	private static final Logger LOGGER = Logger.getLogger(ApiClient.class.getName());

	private static ApiClient INSTANCE;

	private final HttpClient httpClient;
	private final ObjectMapper objectMapper;
	private final String baseUrl;

	private ApiClient() {
		Properties properties = loadProperties();

		this.baseUrl = stripTrailingSlash(resolveBaseUrl(properties));

		long timeout = Long.parseLong(properties.getProperty("api.timeoutSeconds", "15"));
		this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(timeout)).build();

		this.objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	public static synchronized ApiClient getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new ApiClient();
		}
		return INSTANCE;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public <T> T get(String path, TypeReference<ApiResponse<T>> responseType) {
		return send(request(path).GET(), responseType);
	}

	public <T> T post(String path, Object body, TypeReference<ApiResponse<T>> responseType) {
		return send(request(path).POST(jsonBody(body)), responseType);
	}

	public <T> T put(String path, Object body, TypeReference<ApiResponse<T>> responseType) {
		return send(request(path).PUT(jsonBody(body)), responseType);
	}

	public <T> T delete(String path, TypeReference<ApiResponse<T>> responseType) {
		return send(request(path).DELETE(), responseType);
	}

	private HttpRequest.Builder request(String path) {
		HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(baseUrl + path))
				.header("Accept", "application/json").header("Content-Type", "application/json");
		String token = Session.getToken();
		if (token != null) {
			builder.header("Authorization", "Bearer " + token);
		}
		return builder;
	}

	private HttpRequest.BodyPublisher jsonBody(Object body) {
		try {
			return HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body), StandardCharsets.UTF_8);
		} catch (IOException ex) {
			throw new ApiException("No se pudo convertir la petición a JSON.", ex);
		}
	}

	private <T> T send(HttpRequest.Builder builder, TypeReference<ApiResponse<T>> responseType) {
		HttpResponse<String> response;
		try {
			response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
		} catch (IOException ex) {
			throw new ApiException(
					"No se pudo contactar el servidor (" + baseUrl + "). ¿Está Payara corriendo y el WAR desplegado?",
					ex);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new ApiException("La petición fue interrumpida.", ex);
		}

		if (response.statusCode() >= 400) {
			throw toException(response);
		}

		try {
			ApiResponse<T> apiResponse = objectMapper.readValue(response.body(), responseType);
			return apiResponse == null ? null : apiResponse.getData();
		} catch (IOException ex) {
			throw new ApiException("El servidor respondió algo que no se pudo interpretar.", ex);
		}
	}

	private ApiException toException(HttpResponse<String> response) {
		try {
			ApiResponse<Object> error = objectMapper.readValue(response.body(), new TypeReference<>() {
			});
			String message = error.getMessage() == null ? "Error " + response.statusCode() : error.getMessage();
			List<String> details = error.getErrors() == null ? List.of() : error.getErrors();
			return new ApiException(response.statusCode(), message, details);
		} catch (IOException ex) {
			return new ApiException(response.statusCode(), "Error " + response.statusCode() + " del servidor.",
					List.of());
		}
	}

	private String resolveBaseUrl(Properties properties) {
		String fromSystem = System.getProperty("cjs.api.url");
		if (fromSystem != null && !fromSystem.isBlank()) {
			return fromSystem;
		}
		String fromEnv = System.getenv("CJS_API_URL");
		if (fromEnv != null && !fromEnv.isBlank()) {
			return fromEnv;
		}
		return properties.getProperty("api.baseUrl", "http://localhost:8080/criminal_justice_ws/api/v1");
	}

	private Properties loadProperties() {
		Properties properties = new Properties();
		try (InputStream input = ApiClient.class.getResourceAsStream("/config/api.properties")) {
			if (input != null) {
				properties.load(input);
			}
		} catch (IOException ex) {
			LOGGER.log(Level.WARNING, "No se pudo leer config/api.properties, se usan valores por defecto.");
		}
		return properties;
	}

	private String stripTrailingSlash(String url) {
		return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
	}
}
