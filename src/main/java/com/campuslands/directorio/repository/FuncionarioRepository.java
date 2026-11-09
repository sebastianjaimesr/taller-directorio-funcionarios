package com.campuslands.directorio.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.campuslands.directorio.model.Funcionario;

public interface FuncionarioRepository extends JpaRepository<Funcionario, Long> {

	/** Coincidencia parcial del nombre, sin distinguir mayúsculas. */
	List<Funcionario> findByNombreContainingIgnoreCaseOrderByIdAsc(String nombre);

	/** Nombre completo del departamento, sin distinguir mayúsculas. */
	List<Funcionario> findByDepartamentoIgnoreCaseOrderByIdAsc(String departamento);

	List<Funcionario> findAllByOrderByIdAsc();
}
