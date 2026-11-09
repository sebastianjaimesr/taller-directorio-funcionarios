package com.campuslands.directorio.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.campuslands.directorio.dto.FuncionarioRequest;
import com.campuslands.directorio.exception.FuncionarioNoEncontradoException;
import com.campuslands.directorio.exception.SolicitudInvalidaException;
import com.campuslands.directorio.model.Funcionario;
import com.campuslands.directorio.repository.FuncionarioRepository;

@Service
public class FuncionarioService {

	private final FuncionarioRepository repositorio;

	public FuncionarioService(FuncionarioRepository repositorio) {
		this.repositorio = repositorio;
	}

	/**
	 * Lista con filtros opcionales. Un filtro vacío o con solo espacios se
	 * considera ausente; si llegan ambos, se aplica el de nombre.
	 */
	@Transactional(readOnly = true)
	public List<Funcionario> listar(String nombre, String departamento) {
		String filtroNombre = limpiar(nombre);
		String filtroDepartamento = limpiar(departamento);

		if (filtroNombre != null) {
			return repositorio.findByNombreContainingIgnoreCaseOrderByIdAsc(filtroNombre);
		}
		if (filtroDepartamento != null) {
			return repositorio.findByDepartamentoIgnoreCaseOrderByIdAsc(filtroDepartamento);
		}
		return repositorio.findAllByOrderByIdAsc();
	}

	@Transactional(readOnly = true)
	public Funcionario buscarPorId(Long id) {
		return repositorio.findById(id).orElseThrow(() -> new FuncionarioNoEncontradoException(id));
	}

	@Transactional
	public Funcionario registrar(FuncionarioRequest datos) {
		if (datos.id() != null) {
			throw new SolicitudInvalidaException("No se debe enviar un ID al registrar un funcionario");
		}
		Funcionario funcionario = new Funcionario();
		copiarDatos(datos, funcionario);
		return repositorio.save(funcionario);
	}

	@Transactional
	public Funcionario actualizar(Long id, FuncionarioRequest datos) {
		// El ID de la ruta identifica el registro; un ID en el cuerpo se ignora.
		Funcionario funcionario = buscarPorId(id);
		copiarDatos(datos, funcionario);
		return repositorio.save(funcionario);
	}

	@Transactional
	public void eliminar(Long id) {
		Funcionario funcionario = buscarPorId(id);
		repositorio.delete(funcionario);
	}

	private void copiarDatos(FuncionarioRequest datos, Funcionario funcionario) {
		funcionario.setNombre(datos.nombre().trim());
		funcionario.setCargo(datos.cargo().trim());
		funcionario.setDepartamento(datos.departamento().trim());
		funcionario.setTelefono(limpiar(datos.telefono()));
	}

	/** Devuelve el texto sin espacios externos, o null si queda vacío. */
	private String limpiar(String texto) {
		if (texto == null || texto.isBlank()) {
			return null;
		}
		return texto.trim();
	}
}
