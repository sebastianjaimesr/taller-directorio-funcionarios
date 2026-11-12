package com.campuslands.directorio;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.campuslands.directorio.model.Usuario;
import com.campuslands.directorio.repository.UsuarioRepository;

/** Inicio de sesión contra la tabla usuarios. */
@SpringBootTest
@AutoConfigureMockMvc
class AutenticacionTest {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private UsuarioRepository usuarios;

	@Autowired
	private PasswordEncoder cifrador;

	@BeforeEach
	void prepararUsuarios() {
		usuarios.deleteAll();
		usuarios.save(usuario("admin", "Admin123*", "Administrador", true));
		usuarios.save(usuario("inactivo", "Clave123*", "Usuario Inactivo", false));
	}

	private Usuario usuario(String login, String clave, String nombre, boolean activo) {
		Usuario u = new Usuario();
		u.setUsuario(login);
		u.setClave(cifrador.encode(clave));
		u.setNombre(nombre);
		u.setRol("ADMIN");
		u.setActivo(activo);
		return u;
	}

	@Test
	void sinSesionLaApiResponde401() throws Exception {
		mvc.perform(get("/api/v1/funcionarios"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.mensaje").exists());
	}

	@Test
	void sinSesionLaPaginaRedirigeAlLogin() throws Exception {
		mvc.perform(get("/index.html"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login.html"));
	}

	@Test
	void laPaginaDeLoginEsPublica() throws Exception {
		mvc.perform(get("/login.html")).andExpect(status().isOk());
	}

	@Test
	void claveIncorrectaResponde401() throws Exception {
		mvc.perform(post("/api/v1/auth/login").param("usuario", "admin").param("clave", "mala"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.mensaje").value("Usuario o contraseña incorrectos. Te quedan 2 intentos."))
				.andExpect(jsonPath("$.intentosRestantes").value(2));
	}

	@Test
	void usuarioInexistenteResponde401() throws Exception {
		mvc.perform(post("/api/v1/auth/login").param("usuario", "nadie").param("clave", "x"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void usuarioInactivoNoPuedeEntrar() throws Exception {
		mvc.perform(post("/api/v1/auth/login").param("usuario", "inactivo").param("clave", "Clave123*"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.mensaje").value(
						"Tu usuario está desactivado. Contacta al administrador. Te quedan 2 intentos."));
	}

	@Test
	void loginCorrectoAbreSesionYPermiteUsarLaApi() throws Exception {
		MockHttpSession sesion = (MockHttpSession) mvc
				.perform(post("/api/v1/auth/login").param("usuario", "admin").param("clave", "Admin123*"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nombre").value("Administrador"))
				.andReturn().getRequest().getSession();

		mvc.perform(get("/api/v1/auth/yo").session(sesion))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.usuario").value("admin"));
		mvc.perform(get("/api/v1/funcionarios").session(sesion))
				.andExpect(status().isNoContent());

		// Cerrar sesión: la API vuelve a pedir login.
		mvc.perform(post("/api/v1/auth/logout").session(sesion)).andExpect(status().isNoContent());
		mvc.perform(get("/api/v1/funcionarios").session(sesion)).andExpect(status().isUnauthorized());
	}

	@Test
	void laPaginaDeErrorEsPublica() throws Exception {
		mvc.perform(get("/error.html")).andExpect(status().isOk());
		mvc.perform(get("/api/v1/auth/estado"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.bloqueado").value(false));
	}

	@Test
	void tresFallosBloqueanElLoginAunqueLaClaveSeaCorrecta() throws Exception {
		MockHttpSession sesion = new MockHttpSession();
		mvc.perform(post("/api/v1/auth/login").session(sesion).param("usuario", "admin").param("clave", "x1"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.intentosRestantes").value(2));
		mvc.perform(post("/api/v1/auth/login").session(sesion).param("usuario", "admin").param("clave", "x2"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.mensaje").value("Usuario o contraseña incorrectos. Te queda 1 intento."));
		// Tercer fallo: 423 y bloqueado
		mvc.perform(post("/api/v1/auth/login").session(sesion).param("usuario", "admin").param("clave", "x3"))
				.andExpect(status().isLocked())
				.andExpect(jsonPath("$.bloqueado").value(true));
		// Bloqueado: ni la clave correcta entra
		mvc.perform(post("/api/v1/auth/login").session(sesion).param("usuario", "admin").param("clave", "Admin123*"))
				.andExpect(status().isLocked());
		mvc.perform(get("/api/v1/auth/estado").session(sesion))
				.andExpect(jsonPath("$.bloqueado").value(true))
				.andExpect(jsonPath("$.segundos").isNumber());
	}

	@Test
	void unLoginCorrectoReiniciaElContador() throws Exception {
		MockHttpSession sesion = new MockHttpSession();
		mvc.perform(post("/api/v1/auth/login").session(sesion).param("usuario", "admin").param("clave", "x1"));
		mvc.perform(post("/api/v1/auth/login").session(sesion).param("usuario", "admin").param("clave", "x2"));
		MockHttpSession nueva = (MockHttpSession) mvc
				.perform(post("/api/v1/auth/login").session(sesion).param("usuario", "admin").param("clave", "Admin123*"))
				.andExpect(status().isOk())
				.andReturn().getRequest().getSession();
		mvc.perform(post("/api/v1/auth/logout").session(nueva));
		MockHttpSession otra = new MockHttpSession();
		mvc.perform(post("/api/v1/auth/login").session(otra).param("usuario", "admin").param("clave", "x"))
				.andExpect(jsonPath("$.intentosRestantes").value(2));
	}
}
