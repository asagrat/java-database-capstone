package com.project.back_end.services;

import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class TokenService {
	private final AdminRepository adminRepository;
	private final DoctorRepository doctorRepository;
	private final PatientRepository patientRepository;

	@Value("${jwt.secret}")
	private String secret;

	public TokenService(
			AdminRepository adminRepository,
			DoctorRepository doctorRepository,
			PatientRepository patientRepository
	) {
		this.adminRepository = adminRepository;
		this.doctorRepository = doctorRepository;
		this.patientRepository = patientRepository;
	}

	private SecretKey getSigningKey() {
		return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
	}

	public String generateToken(String email) {
		long weekMillis = 7L * 24 * 60 * 60 * 1000;
		Date now = new Date();
		Date expiry = new Date(now.getTime() + weekMillis);

		return Jwts.builder()
				.subject(email)
				.issuedAt(now)
				.expiration(expiry)
				.signWith(getSigningKey())
				.compact();
	}

	public String extractEmail(String token) {
		return Jwts.parser()
				.verifyWith(getSigningKey())
				.build()
				.parseSignedClaims(token)
				.getPayload()
				.getSubject();
	}

	public boolean validateToken(String token, String role) {
		try {
			String email = extractEmail(token);

			return switch (role) {
				case "admin" -> adminRepository.findByUsername(email) != null;
				case "doctor" -> doctorRepository.findByEmail(email) != null;
				case "patient" -> patientRepository.findByEmail(email) != null;
				default -> false;
			};
		} catch (Exception ex) {
			return false;
		}
	}


}
