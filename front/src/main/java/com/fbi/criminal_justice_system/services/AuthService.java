package com.fbi.criminal_justice_system.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fbi.cjs.shared.api.ApiResponse;
import com.fbi.cjs.shared.dto.LoginRequestDTO;
import com.fbi.cjs.shared.dto.LoginResponseDTO;
import com.fbi.cjs.shared.dto.UserDTO;
import com.fbi.criminal_justice_system.utils.ApiClient;
import com.fbi.criminal_justice_system.utils.Session;

public class AuthService {

	private final ApiClient apiClient = ApiClient.getInstance();

	public UserDTO login(String username, String password) {
		LoginResponseDTO response = apiClient.post("/auth/login", new LoginRequestDTO(username, password),
				new TypeReference<ApiResponse<LoginResponseDTO>>() {
				});

		Session.start(response.getToken(), response.getUser());
		return response.getUser();
	}

	public void logout() {
		try {
			apiClient.post("/auth/logout", null, new TypeReference<ApiResponse<Void>>() {
			});
		} catch (RuntimeException ignored) {
		} finally {
			Session.clear();
		}
	}
}
