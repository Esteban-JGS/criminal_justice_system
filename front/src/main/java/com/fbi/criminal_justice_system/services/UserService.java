package com.fbi.criminal_justice_system.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fbi.cjs.shared.api.ApiResponse;
import com.fbi.cjs.shared.dto.RoleDTO;
import com.fbi.cjs.shared.dto.UserDTO;
import com.fbi.criminal_justice_system.utils.ApiClient;
import java.util.List;

public class UserService {

	private static final String RESOURCE = "/users";

	private final ApiClient apiClient = ApiClient.getInstance();

	public List<UserDTO> getAll() {
		return apiClient.get(RESOURCE, new TypeReference<ApiResponse<List<UserDTO>>>() {
		});
	}

	public UserDTO create(UserDTO user) {
		return apiClient.post(RESOURCE, user, new TypeReference<ApiResponse<UserDTO>>() {
		});
	}

	public UserDTO update(Long id, UserDTO user) {
		return apiClient.put(RESOURCE + "/" + id, user, new TypeReference<ApiResponse<UserDTO>>() {
		});
	}

	public void delete(Long id) {
		apiClient.delete(RESOURCE + "/" + id, new TypeReference<ApiResponse<Void>>() {
		});
	}

	public List<RoleDTO> getRoles() {
		return apiClient.get("/roles", new TypeReference<ApiResponse<List<RoleDTO>>>() {
		});
	}
}
