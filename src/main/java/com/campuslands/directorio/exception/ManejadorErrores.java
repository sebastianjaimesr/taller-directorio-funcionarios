package com.campuslands.directorio.exception;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/** Convierte las excepciones en respuestas JSON con un mensaje claro. */
@RestControllerAdvice
public class ManejadorErrores {

	@ExceptionHandler(FuncionarioNoEncontradoException.class)
	public ResponseEntity<Map<String, String>> noEncontrado(FuncionarioNoEncontradoException ex) {
		return respuesta(HttpStatus.NOT_FOUND, ex.getMessage());
	}

	@ExceptionHandler(SolicitudInvalidaException.class)
	public ResponseEntity<Map<String, String>> invalida(SolicitudInvalidaException ex) {
		return respuesta(HttpStatus.BAD_REQUEST, ex.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, String>> validacion(MethodArgumentNotValidException ex) {
		String mensaje = ex.getBindingResult().getFieldErrors().stream()
				.map(e -> e.getDefaultMessage())
				.distinct()
				.collect(Collectors.joining(". "));
		return respuesta(HttpStatus.BAD_REQUEST, mensaje);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<Map<String, String>> cuerpoIlegible(HttpMessageNotReadableException ex) {
		return respuesta(HttpStatus.BAD_REQUEST, "El cuerpo de la solicitud no es un JSON válido");
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<Map<String, String>> tipoInvalido(MethodArgumentTypeMismatchException ex) {
		return respuesta(HttpStatus.BAD_REQUEST, "El valor '" + ex.getValue() + "' no es válido para " + ex.getName());
	}

	private ResponseEntity<Map<String, String>> respuesta(HttpStatus estado, String mensaje) {
		return ResponseEntity.status(estado).body(Map.of("mensaje", mensaje));
	}
}
