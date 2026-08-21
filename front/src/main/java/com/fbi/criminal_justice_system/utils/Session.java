package com.fbi.criminal_justice_system.utils;

import com.fbi.cjs.shared.dto.UserDTO;
import com.fbi.cjs.shared.enums.Role;

public final class Session {

	private static final String TOKEN_KEY = "session.token";
	private static final String USER_KEY = "session.user";

	private Session() {
	}

	public static void start(String token, UserDTO user) {
		AppContext.getInstance().set(TOKEN_KEY, token);
		AppContext.getInstance().set(USER_KEY, user);
	}

	public static void clear() {
		AppContext.getInstance().delete(TOKEN_KEY);
		AppContext.getInstance().delete(USER_KEY);
	}

	public static String getToken() {
		return (String) AppContext.getInstance().get(TOKEN_KEY);
	}

	public static UserDTO getUser() {
		return (UserDTO) AppContext.getInstance().get(USER_KEY);
	}

	public static boolean isActive() {
		return getToken() != null && getUser() != null;
	}

	public static boolean hasAnyRole(Role... roles) {
		UserDTO user = getUser();
		if (user == null || user.getRole() == null) {
			return false;
		}
		for (Role role : roles) {
			if (user.getRole() == role) {
				return true;
			}
		}
		return false;
	}
}
