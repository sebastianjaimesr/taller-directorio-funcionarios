package com.campuslands.directorio;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.campuslands.directorio.model.Funcionario;
import com.campuslands.directorio.repository.FuncionarioRepository;

/** Pruebas de la API con un usuario ya autenticado (@WithMockUser). */
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "admin")
class FuncionarioApiTest {

	private static final String API = "/api/v1/funcionarios";

	@Autowired
	private MockMvc mvc;

	@Autowired
	private FuncionarioRepository repositorio;

	@BeforeEach
	void limpiar() {
		repositorio.deleteAll();
	}

	private Funcionario guardar(String nombre, String cargo, String departamento, String telefono) {
		Funcionario f = new Funcionario();
		f.setNombre(nombre);
		f.setCargo(cargo);
		f.setDepartamento(departamento);
		f.setTelefono(telefono);
		return repositorio.save(f);
	}

	private String json(String cuerpo) {
		return cuerpo.replace('\'', '"');
	}

	@Test
	void listarVacioDevuelve204() throws Exception {
		mvc.perform(get(API)).andExpect(status().isNoContent()).andExpect(content().string(""));
	}

	@Test
	void listarDevuelve200ConLista() throws Exception {
		guardar("Ana Pérez", "Contadora", "Contabilidad", null);
		guardar("Luis Gómez", "Analista", "Sistemas", "3001234567");
		mvc.perform(get(API)).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(2)));
	}

	@Test
	void consultarPorId() throws Exception {
		Funcionario f = guardar("Ana Pérez", "Contadora", "Contabilidad", null);
		mvc.perform(get(API + "/" + f.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nombre").value("Ana Pérez"));
		mvc.perform(get(API + "/999999")).andExpect(status().isNotFound());
	}

	@Test
	void registrarDevuelve201ConId() throws Exception {
		mvc.perform(post(API).contentType(MediaType.APPLICATION_JSON)
				.content(json("{'nombre':'  Ana  ','cargo':'Contadora','departamento':'Contabilidad'}")))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.nombre").value("Ana"));
	}

	@Test
	void registrarConIdNuloEsValido() throws Exception {
		mvc.perform(post(API).contentType(MediaType.APPLICATION_JSON)
				.content(json("{'id':null,'nombre':'Ana','cargo':'C','departamento':'D','telefono':''}")))
				.andExpect(status().isCreated());
	}

	@Test
	void registrarInvalidoDevuelve400() throws Exception {
		// falta cargo
		mvc.perform(post(API).contentType(MediaType.APPLICATION_JSON)
				.content(json("{'nombre':'Ana','departamento':'D'}")))
				.andExpect(status().isBadRequest());
		// solo espacios
		mvc.perform(post(API).contentType(MediaType.APPLICATION_JSON)
				.content(json("{'nombre':'   ','cargo':'C','departamento':'D'}")))
				.andExpect(status().isBadRequest());
		// id no nulo
		mvc.perform(post(API).contentType(MediaType.APPLICATION_JSON)
				.content(json("{'id':5,'nombre':'Ana','cargo':'C','departamento':'D'}")))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.mensaje").exists());
	}

	@Test
	void actualizar() throws Exception {
		Funcionario f = guardar("Ana", "Contadora", "Contabilidad", "123");
		mvc.perform(put(API + "/" + f.getId()).contentType(MediaType.APPLICATION_JSON)
				.content(json("{'nombre':'Ana María','cargo':'Jefa','departamento':'Contabilidad','telefono':''}")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(f.getId()))
				.andExpect(jsonPath("$.nombre").value("Ana María"));
		mvc.perform(put(API + "/999999").contentType(MediaType.APPLICATION_JSON)
				.content(json("{'nombre':'X','cargo':'Y','departamento':'Z'}")))
				.andExpect(status().isNotFound());
		mvc.perform(put(API + "/" + f.getId()).contentType(MediaType.APPLICATION_JSON)
				.content(json("{'nombre':'','cargo':'Y','departamento':'Z'}")))
				.andExpect(status().isBadRequest());
	}

	@Test
	void eliminar() throws Exception {
		Funcionario f = guardar("Ana", "Contadora", "Contabilidad", null);
		mvc.perform(delete(API + "/" + f.getId())).andExpect(status().isNoContent());
		mvc.perform(delete(API + "/" + f.getId())).andExpect(status().isNotFound());
	}

	@Test
	void buscarPorNombreParcialSinMayusculas() throws Exception {
		guardar("Ana Pérez", "Contadora", "Contabilidad", null);
		guardar("Mariana López", "Analista", "Sistemas", null);
		guardar("Luis Gómez", "Analista", "Sistemas", null);
		mvc.perform(get(API).param("nombre", "ANA"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)));
		mvc.perform(get(API).param("nombre", "zzz")).andExpect(status().isNoContent());
	}

	@Test
	void filtrarPorDepartamentoExacto() throws Exception {
		guardar("Ana", "Contadora", "Contabilidad", null);
		guardar("Luis", "Analista", "Sistemas", null);
		mvc.perform(get(API).param("departamento", "contabilidad"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)));
		mvc.perform(get(API).param("departamento", "Conta")).andExpect(status().isNoContent());
	}

	@Test
	void filtrosVaciosSeIgnoranYNombreTienePrioridad() throws Exception {
		guardar("Ana", "Contadora", "Contabilidad", null);
		guardar("Luis", "Analista", "Sistemas", null);
		mvc.perform(get(API).param("nombre", "   ").param("departamento", ""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)));
		mvc.perform(get(API).param("nombre", "luis").param("departamento", "Contabilidad"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].nombre").value("Luis"));
	}
}
