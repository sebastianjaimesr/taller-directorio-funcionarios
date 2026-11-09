package com.campuslands.directorio.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.campuslands.directorio.dto.FuncionarioRequest;
import com.campuslands.directorio.model.Funcionario;
import com.campuslands.directorio.service.FuncionarioService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/funcionarios")
public class FuncionarioController {

	private final FuncionarioService servicio;

	public FuncionarioController(FuncionarioService servicio) {
		this.servicio = servicio;
	}

	/** Listar, buscar por nombre (?nombre=) o filtrar por departamento (?departamento=). */
	@GetMapping
	public ResponseEntity<List<Funcionario>> listar(
			@RequestParam(required = false) String nombre,
			@RequestParam(required = false) String departamento) {
		List<Funcionario> resultado = servicio.listar(nombre, departamento);
		if (resultado.isEmpty()) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.ok(resultado);
	}

	@GetMapping("/{id}")
	public ResponseEntity<Funcionario> consultar(@PathVariable Long id) {
		return ResponseEntity.ok(servicio.buscarPorId(id));
	}

	@PostMapping
	public ResponseEntity<Funcionario> registrar(@Valid @RequestBody FuncionarioRequest datos) {
		Funcionario creado = servicio.registrar(datos);
		return ResponseEntity.created(URI.create("/api/v1/funcionarios/" + creado.getId())).body(creado);
	}

	@PutMapping("/{id}")
	public ResponseEntity<Funcionario> actualizar(@PathVariable Long id,
			@Valid @RequestBody FuncionarioRequest datos) {
		return ResponseEntity.ok(servicio.actualizar(id, datos));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable Long id) {
		servicio.eliminar(id);
		return ResponseEntity.noContent().build();
	}
}
