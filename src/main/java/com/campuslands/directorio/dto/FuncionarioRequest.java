package com.campuslands.directorio.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Datos que llegan en el cuerpo de POST y PUT.
 * {@code @NotBlank} rechaza campos ausentes, vacíos o con solo espacios.
 */
public record FuncionarioRequest(
		Long id,
		@NotBlank(message = "El nombre es obligatorio") String nombre,
		@NotBlank(message = "El cargo es obligatorio") String cargo,
		@NotBlank(message = "El departamento es obligatorio") String departamento,
		String telefono) {
}
