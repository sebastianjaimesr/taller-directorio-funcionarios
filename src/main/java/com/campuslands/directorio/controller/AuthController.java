package com.campuslands.directorio.controller;

import java.security.Principal;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.campuslands.directorio.repository.UsuarioRepository;
import com.campuslands.directorio.security.IntentosLogin;

import jakarta.servlet.http.HttpServletRequest;

/**
 * El login y el logout los atiende Spring Security (ver SeguridadConfig).
 * Aquí están "quién soy" (nombre del usuario) y el estado de bloqueo por intentos fallidos.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

	private final UsuarioRepository usuarios;
	private final IntentosLogin intentos;

	public AuthController(UsuarioRepository usuarios, IntentosLogin intentos) {
		this.usuarios = usuarios;
		this.intentos = intentos;
	}

	/** Público: indica si este navegador tiene el login bloqueado y por cuántos segundos. */
	@GetMapping("/estado")
	public Map<String, Object> estado(HttpServletRequest peticion) {
		long segundos = intentos.segundosBloqueo(peticion);
		return Map.of("bloqueado", segundos > 0, "segundos", segundos);
	}

	@GetMapping("/yo")
	public Map<String, String> yo(Principal principal) {
		return usuarios.findByUsuarioIgnoreCase(principal.getName())
				.map(u -> Map.of("usuario", u.getUsuario(), "nombre", u.getNombre(), "rol", u.getRol()))
				.orElse(Map.of("usuario", principal.getName(), "nombre", principal.getName(), "rol", ""));
	}
}
