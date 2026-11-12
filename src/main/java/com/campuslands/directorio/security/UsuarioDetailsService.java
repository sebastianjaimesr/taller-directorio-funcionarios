package com.campuslands.directorio.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.campuslands.directorio.model.Usuario;
import com.campuslands.directorio.repository.UsuarioRepository;

/**
 * Le dice a Spring Security cómo encontrar un usuario: lo busca en la tabla
 * {@code usuarios} de MySQL. Spring compara luego la clave escrita con el hash BCrypt.
 */
@Service
public class UsuarioDetailsService implements UserDetailsService {

	private final UsuarioRepository repositorio;

	public UsuarioDetailsService(UsuarioRepository repositorio) {
		this.repositorio = repositorio;
	}

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String nombreUsuario) throws UsernameNotFoundException {
		String limpio = nombreUsuario == null ? "" : nombreUsuario.trim();
		Usuario usuario = repositorio.findByUsuarioIgnoreCase(limpio)
				.orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + limpio));

		return User.withUsername(usuario.getUsuario())
				.password(usuario.getClave())
				.roles(usuario.getRol().trim().toUpperCase())
				.disabled(!usuario.isActivo())
				.build();
	}
}
