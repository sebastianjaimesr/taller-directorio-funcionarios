package com.campuslands.directorio.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.campuslands.directorio.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

	/** Busca el usuario por su nombre de inicio de sesión, sin distinguir mayúsculas. */
	Optional<Usuario> findByUsuarioIgnoreCase(String usuario);
}
