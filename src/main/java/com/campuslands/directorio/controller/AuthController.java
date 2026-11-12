package com.campuslands.directorio.controller;

import java.security.Principal;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.campuslands.directorio.repository.UsuarioRepository;

/**
 * El login y el logout los atiende Spring Security (ver SeguridadConfig).
 * Aquí solo está "quién soy", que usa la página para mostrar el nombre del usuario.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

	private final UsuarioRepository usuarios;

	public AuthController(UsuarioRepository usuarios) {
		this.usuarios = usuarios;
	}

	@GetMapping("/yo")
	public Map<String, String> yo(Principal principal) {
		return usuarios.findByUsuarioIgnoreCase(principal.getName())
				.map(u -> Map.of("usuario", u.getUsuario(), "nombre", u.getNombre(), "rol", u.getRol()))
				.orElse(Map.of("usuario", principal.getName(), "nombre", principal.getName(), "rol", ""));
	}
}
