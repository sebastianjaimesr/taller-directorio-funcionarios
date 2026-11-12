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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

import com.campuslands.directorio.repository.UsuarioRepository;
import com.campuslands.directorio.security.IntentosLogin;

import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.json.JsonMapper;

/**
 * Configuración de seguridad.
 * <ul>
 * <li>Público: la página de login, sus estilos/scripts y el endpoint de inicio de sesión.</li>
 * <li>Todo lo demás (la app y la API de funcionarios) exige haber iniciado sesión.</li>
 * <li>La sesión se guarda en una cookie (JSESSIONID) que el navegador envía sola.</li>
 * <li>Tras 3 intentos fallidos el login se bloquea 5 minutos y la página va a error.html.</li>
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
	public SecurityFilterChain filtroSeguridad(HttpSecurity http, UsuarioRepository usuarios, IntentosLogin intentos)
			throws Exception {
		http
				.authorizeHttpRequests(reglas -> reglas
						.requestMatchers("/login.html", "/error.html", "/css/**", "/js/login.js", "/js/error.js",
								"/js/demo.js", "/img/**", "/favicon.svg", "/error")
						.permitAll()
						.requestMatchers(HttpMethod.GET, "/api/v1/auth/estado").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
						.anyRequest().authenticated())

				// Inicio de sesión: login.js envía usuario y clave a /api/v1/auth/login.
				.formLogin(login -> login
						.loginPage("/login.html")
						.loginProcessingUrl("/api/v1/auth/login")
						.usernameParameter("usuario")
						.passwordParameter("clave")
						.successHandler((peticion, respuesta, autenticacion) -> {
							intentos.reiniciar(peticion);
							String nombre = usuarios.findByUsuarioIgnoreCase(autenticacion.getName())
									.map(u -> u.getNombre())
									.orElse(autenticacion.getName());
							escribir(respuesta, HttpStatus.OK,
									Map.of("usuario", autenticacion.getName(), "nombre", nombre));
						})
						.failureHandler((peticion, respuesta, error) -> {
							int restantes = intentos.registrarFallo(peticion);
							if (restantes == 0) {
								escribirBloqueo(respuesta, intentos.segundosBloqueo(peticion));
								return;
							}
							String mensaje = error instanceof DisabledException
									? "Tu usuario está desactivado. Contacta al administrador."
									: "Usuario o contraseña incorrectos.";
							mensaje += restantes == 1 ? " Te queda 1 intento." : " Te quedan " + restantes + " intentos.";
							escribir(respuesta, HttpStatus.UNAUTHORIZED,
									Map.of("mensaje", mensaje, "intentosRestantes", restantes));
						}))

				// Si el navegador está bloqueado, ni siquiera se revisa la contraseña.
				.addFilterBefore(new OncePerRequestFilter() {
					@Override
					protected void doFilterInternal(HttpServletRequest peticion, HttpServletResponse respuesta,
							FilterChain cadena) throws ServletException, IOException {
						long segundos = intentos.segundosBloqueo(peticion);
						if (segundos > 0) {
							escribirBloqueo(respuesta, segundos);
							return;
						}
						cadena.doFilter(peticion, respuesta);
					}

					@Override
					protected boolean shouldNotFilter(HttpServletRequest peticion) {
						return !("POST".equals(peticion.getMethod())
								&& peticion.getRequestURI().equals(peticion.getContextPath() + "/api/v1/auth/login"));
					}
				}, UsernamePasswordAuthenticationFilter.class)

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

	/** 423 Locked: demasiados intentos; login.js redirige a error.html. */
	private void escribirBloqueo(HttpServletResponse respuesta, long segundos) throws IOException {
		escribir(respuesta, HttpStatus.LOCKED, Map.of(
				"mensaje", "Demasiados intentos fallidos. Intenta de nuevo más tarde.",
				"bloqueado", true,
				"segundos", segundos));
	}

	private void escribir(HttpServletResponse respuesta, HttpStatus estado, Map<String, ?> cuerpo)
			throws IOException {
		respuesta.setStatus(estado.value());
		respuesta.setContentType(MediaType.APPLICATION_JSON_VALUE);
		respuesta.setCharacterEncoding("UTF-8");
		json.writeValue(respuesta.getWriter(), cuerpo);
	}
}
