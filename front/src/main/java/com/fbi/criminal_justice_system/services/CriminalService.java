package com.fbi.criminal_justice_system.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fbi.cjs.shared.api.ApiResponse;
import com.fbi.cjs.shared.dto.CriminalDTO;
import com.fbi.cjs.shared.enums.CriminalStatus;
import com.fbi.cjs.shared.enums.DangerLevel;
import com.fbi.criminal_justice_system.utils.ApiClient;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CriminalService {

	private static final String RESOURCE = "/criminals";

	private final ApiClient apiClient = ApiClient.getInstance();

	public List<CriminalDTO> getAll() {
		return apiClient.get(RESOURCE, new TypeReference<ApiResponse<List<CriminalDTO>>>() {
		});
	}

	public List<CriminalDTO> search(String text, CriminalStatus status, DangerLevel dangerLevel) {
		List<String> params = new ArrayList<>();
		if (text != null && !text.isBlank()) {
			params.add("search=" + encode(text.trim()));
		}
		if (status != null) {
			params.add("status=" + status.name());
		}
		if (dangerLevel != null) {
			params.add("dangerLevel=" + dangerLevel.name());
		}

		String path = params.isEmpty() ? RESOURCE : RESOURCE + "?" + String.join("&", params);

		return apiClient.get(path, new TypeReference<ApiResponse<List<CriminalDTO>>>() {
		});
	}

	public CriminalDTO create(CriminalDTO criminal) {
		return apiClient.post(RESOURCE, criminal, new TypeReference<ApiResponse<CriminalDTO>>() {
		});
	}

	public CriminalDTO update(Long id, CriminalDTO criminal) {
		return apiClient.put(RESOURCE + "/" + id, criminal, new TypeReference<ApiResponse<CriminalDTO>>() {
		});
	}

	public void delete(Long id) {
		apiClient.delete(RESOURCE + "/" + id, new TypeReference<ApiResponse<Void>>() {
		});
	}

	private String encode(String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8);
	}
}
