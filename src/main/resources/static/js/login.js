// Pantalla de inicio de sesión.
// Envía usuario y clave a /api/v1/auth/login (lo atiende Spring Security, ver SeguridadConfig.java).
// Si son correctos, el servidor crea la sesión (cookie JSESSIONID) y pasamos al directorio.

const formLogin = document.getElementById("form-login");
const campoUsuario = document.getElementById("usuario");
const campoClave = document.getElementById("clave");
const cajaError = document.getElementById("error-login");
const btnEntrar = document.getElementById("btn-entrar");
const btnVerClave = document.getElementById("ver-clave");

function mostrarError(texto) {
	cajaError.textContent = texto;
	cajaError.classList.remove("oculto");
	formLogin.classList.remove("sacudir");
	void formLogin.offsetWidth; // reinicia la animación
	formLogin.classList.add("sacudir");
}

function cargando(activo) {
	btnEntrar.disabled = activo;
	btnEntrar.querySelector(".texto-boton").textContent = activo ? "Verificando…" : "Entrar";
	btnEntrar.querySelector(".spinner").classList.toggle("oculto", !activo);
}

btnVerClave.addEventListener("click", () => {
	const visible = campoClave.type === "text";
	campoClave.type = visible ? "password" : "text";
	btnVerClave.classList.toggle("activo", !visible);
	const texto = visible ? "Mostrar contraseña" : "Ocultar contraseña";
	btnVerClave.title = texto;
	btnVerClave.setAttribute("aria-label", texto);
	campoClave.focus();
});

/** Envía las credenciales como formulario (application/x-www-form-urlencoded). */
async function iniciarSesion(usuario, clave) {
	const cuerpo = new URLSearchParams({ usuario, clave });
	if (window.MODO_DEMO) {
		return window.apiDemo("/api/v1/auth/login", { method: "POST", body: cuerpo.toString() });
	}
	const respuesta = await fetch("/api/v1/auth/login", {
		method: "POST",
		headers: { "Content-Type": "application/x-www-form-urlencoded", Accept: "application/json" },
		credentials: "same-origin",
		body: cuerpo,
	});
	const datos = await respuesta.json().catch(() => ({}));
	if (!respuesta.ok) {
		throw new Error(datos.mensaje || "No se pudo iniciar sesión.");
	}
	return { estado: respuesta.status, datos };
}

formLogin.addEventListener("submit", async (evento) => {
	evento.preventDefault();
	cajaError.classList.add("oculto");

	const usuario = campoUsuario.value.trim();
	const clave = campoClave.value;
	campoUsuario.classList.toggle("invalido", !usuario);
	campoClave.classList.toggle("invalido", !clave);
	if (!usuario || !clave) {
		mostrarError("Escribe tu usuario y tu contraseña.");
		(usuario ? campoClave : campoUsuario).focus();
		return;
	}

	cargando(true);
	try {
		await iniciarSesion(usuario, clave);
		location.replace("index.html");
	} catch (error) {
		cargando(false);
		mostrarError(error.message);
		campoClave.value = "";
		campoClave.focus();
	}
});

for (const campo of [campoUsuario, campoClave]) {
	campo.addEventListener("input", () => campo.classList.remove("invalido"));
}

// Si ya hay sesión abierta, ir directo al directorio.
(async function () {
	try {
		if (window.MODO_DEMO) {
			await window.apiDemo("/api/v1/auth/yo");
			location.replace("index.html");
			return;
		}
		const r = await fetch("/api/v1/auth/yo", { headers: { Accept: "application/json" }, credentials: "same-origin" });
		if (r.ok) location.replace("index.html");
	} catch (e) {
		/* sin sesión: se queda en el login */
	}
})();
