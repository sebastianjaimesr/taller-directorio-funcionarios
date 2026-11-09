package com.campuslands.directorio.exception;

public class FuncionarioNoEncontradoException extends RuntimeException {

	public FuncionarioNoEncontradoException(Long id) {
		super("No existe un funcionario con ID " + id);
	}
}
