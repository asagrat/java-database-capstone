package com.project.back_end.services;

import java.util.HashMap;
import java.util.Map;

@org.springframework.stereotype.Service
public class Service {
	private final TokenService tokenService;

	public Service(TokenService tokenService) {
		this.tokenService = tokenService;
	}

	public Map<String, String> validateToken(String token, String role) {
		Map<String, String> response = new HashMap<>();

		if (token == null || token.isBlank()) {
			response.put("message", "Token is missing");
			return response;
		}

		boolean valid = tokenService.validateToken(token, role);
		if (!valid) {
			response.put("message", "Invalid or expired token");
		}

		return response;
	}


}
