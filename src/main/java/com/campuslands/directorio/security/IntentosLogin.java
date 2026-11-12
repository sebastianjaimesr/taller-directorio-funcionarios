package com.campuslands.directorio.security;

import java.time.Duration;
import java.time.Instant;

import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Cuenta los intentos fallidos de inicio de sesión de cada navegador (sesión).
 * Al llegar a {@link #MAXIMO_INTENTOS} fallos, el login queda bloqueado durante
 * {@link #TIEMPO_BLOQUEO} y la página redirige a error.html.
 */
@Component
public class IntentosLogin {

	public static final int MAXIMO_INTENTOS = 3;
	public static final Duration TIEMPO_BLOQUEO = Duration.ofMinutes(5);

	private static final String FALLOS = "login.fallos";
	private static final String BLOQUEADO_HASTA = "login.bloqueadoHasta";

	/** Registra un fallo y devuelve cuántos intentos le quedan (0 = quedó bloqueado). */
	public int registrarFallo(HttpServletRequest peticion) {
		HttpSession sesion = peticion.getSession(true);
		int fallos = fallos(sesion) + 1;
		if (fallos >= MAXIMO_INTENTOS) {
			sesion.setAttribute(BLOQUEADO_HASTA, Instant.now().plus(TIEMPO_BLOQUEO));
			sesion.setAttribute(FALLOS, 0);
			return 0;
		}
		sesion.setAttribute(FALLOS, fallos);
		return MAXIMO_INTENTOS - fallos;
	}

	/** Tras un login correcto se borra el contador. */
	public void reiniciar(HttpServletRequest peticion) {
		HttpSession sesion = peticion.getSession(false);
		if (sesion != null) {
			sesion.removeAttribute(FALLOS);
			sesion.removeAttribute(BLOQUEADO_HASTA);
		}
	}

	/** Segundos que faltan para desbloquear; 0 si no está bloqueado. */
	public long segundosBloqueo(HttpServletRequest peticion) {
		HttpSession sesion = peticion.getSession(false);
		if (sesion == null || !(sesion.getAttribute(BLOQUEADO_HASTA) instanceof Instant hasta)) {
			return 0;
		}
		long segundos = Duration.between(Instant.now(), hasta).toSeconds();
		if (segundos <= 0) {
			sesion.removeAttribute(BLOQUEADO_HASTA);
			return 0;
		}
		return segundos;
	}

	private int fallos(HttpSession sesion) {
		return sesion.getAttribute(FALLOS) instanceof Integer n ? n : 0;
	}
}
