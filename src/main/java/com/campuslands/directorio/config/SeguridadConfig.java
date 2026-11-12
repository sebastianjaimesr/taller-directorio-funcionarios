package com.campuslands.directorio.config;

import java.io.IOException;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.PathPatternRequestMatcher;

import com.campuslands.directorio.repository.UsuarioRepository;

import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.json.JsonMapper;

/**
 * Configuración de seguridad.
 * <ul>
 * <li>Público: la página de login, sus estilos/scripts y el endpoint de inicio de sesión.</li>
 * <li>Todo lo demás (la app y la API de funcionarios) exige haber iniciado sesión.</li>
 * <li>La sesión se guarda en una cookie (JSESSIONID) que el navegador envía sola.</li>
 * </ul>
 */
@Configuration
public class SeguridadConfig {

	private final JsonMapper json = JsonMapper.builder().build();

	@Bean
	public PasswordEncoder passwordEncoder() {
		// BCrypt: el mismo algoritmo con el que el script SQL guarda las claves.
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain filtroSeguridad(HttpSecurity http, UsuarioRepository usuarios) throws Exception {
		http
				.authorizeHttpRequests(reglas -> reglas
						.requestMatchers("/login.html", "/css/**", "/js/login.js", "/js/demo.js", "/img/**",
								"/favicon.svg", "/error")
						.permitAll()
						.requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
						.anyRequest().authenticated())

				// Inicio de sesión: login.js envía usuario y clave a /api/v1/auth/login.
				.formLogin(login -> login
						.loginPage("/login.html")
						.loginProcessingUrl("/api/v1/auth/login")
						.usernameParameter("usuario")
						.passwordParameter("clave")
						.successHandler((peticion, respuesta, autenticacion) -> {
							String nombre = usuarios.findByUsuarioIgnoreCase(autenticacion.getName())
									.map(u -> u.getNombre())
									.orElse(autenticacion.getName());
							escribir(respuesta, HttpStatus.OK,
									Map.of("usuario", autenticacion.getName(), "nombre", nombre));
						})
						.failureHandler((peticion, respuesta, error) -> {
							String mensaje = error instanceof DisabledException
									? "Tu usuario está desactivado. Contacta al administrador."
									: "Usuario o contraseña incorrectos.";
							escribir(respuesta, HttpStatus.UNAUTHORIZED, Map.of("mensaje", mensaje));
						}))

				.logout(salida -> salida
						.logoutUrl("/api/v1/auth/logout")
						.logoutSuccessHandler((peticion, respuesta, autenticacion) ->
								respuesta.setStatus(HttpStatus.NO_CONTENT.value())))

				// Sin sesión: la API responde 401 en JSON y las páginas redirigen al login.
				.exceptionHandling(errores -> errores
						.defaultAuthenticationEntryPointFor(
								(peticion, respuesta, ex) -> escribir(respuesta, HttpStatus.UNAUTHORIZED,
										Map.of("mensaje", "Tu sesión expiró. Inicia sesión de nuevo.")),
								PathPatternRequestMatcher.pathPattern("/api/**"))
						.defaultAuthenticationEntryPointFor(
								new LoginUrlAuthenticationEntryPoint("/login.html"),
								PathPatternRequestMatcher.pathPattern("/**")))

				// El frontend está en el mismo servidor y la cookie de sesión es SameSite=Lax
				// (ver application.properties), así que otro sitio no puede enviar peticiones
				// con la sesión del usuario. Por eso se desactiva el token CSRF y el JS queda simple.
				.csrf(csrf -> csrf.disable());

		return http.build();
	}

	private void escribir(HttpServletResponse respuesta, HttpStatus estado, Map<String, String> cuerpo)
			throws IOException {
		respuesta.setStatus(estado.value());
		respuesta.setContentType(MediaType.APPLICATION_JSON_VALUE);
		respuesta.setCharacterEncoding("UTF-8");
		json.writeValue(respuesta.getWriter(), cuerpo);
	}
}
