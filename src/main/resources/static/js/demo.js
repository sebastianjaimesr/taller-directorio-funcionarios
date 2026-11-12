/*
 * Modo demostración para GitHub Pages.
 * GitHub Pages solo sirve archivos estáticos (no ejecuta Spring Boot ni MySQL),
 * así que allí la página usa esta API simulada que guarda los datos en el navegador
 * (localStorage) y respeta las mismas reglas y códigos de respuesta que la API real.
 * Al ejecutar la aplicación con Spring Boot este modo no se activa.
 * También simula el inicio de sesión (usuario demo: admin / Admin123*).
 */
(function () {
	window.MODO_DEMO = location.hostname.endsWith("github.io");
	if (!window.MODO_DEMO) {
		return;
	}

	const CLAVE = "directorio-funcionarios-demo";
	const INICIALES = [
		{ nombre: "Ana Pérez", cargo: "Contadora", departamento: "Contabilidad", telefono: "3001112233" },
		{ nombre: "Luis Gómez", cargo: "Analista", departamento: "Sistemas", telefono: null },
		{ nombre: "Mariana López", cargo: "Coordinadora", departamento: "Talento Humano", telefono: "3104445566" },
	];

	// Usuario de prueba de la demo (en local los usuarios viven en la tabla usuarios de MySQL).
	const USUARIOS_DEMO = [{ usuario: "admin", clave: "Admin123*", nombre: "Administrador", rol: "ADMIN" }];
	const CLAVE_SESION = "directorio-funcionarios-sesion";

	function sesionActual() {
		let sesion = null;
		try {
			sesion = JSON.parse(sessionStorage.getItem(CLAVE_SESION));
		} catch (e) {
			sesion = null;
		}
		if (!sesion) error(401, "Tu sesión expiró. Inicia sesión de nuevo.");
		return sesion;
	}

	function cargar() {
		try {
			const guardado = JSON.parse(localStorage.getItem(CLAVE));
			if (guardado && Array.isArray(guardado.lista)) {
				return guardado;
			}
		} catch (e) {
			/* almacenamiento no disponible: se usan los datos iniciales */
		}
		return {
			siguienteId: INICIALES.length + 1,
			lista: INICIALES.map((f, i) => ({ id: i + 1, ...f })),
		};
	}

	let db = cargar();

	function guardar() {
		try {
			localStorage.setItem(CLAVE, JSON.stringify(db));
		} catch (e) {
			/* sin almacenamiento: los cambios duran hasta recargar */
		}
	}

	function error(estado, mensaje) {
		const e = new Error(mensaje);
		e.estado = estado;
		throw e;
	}

	function limpiar(texto) {
		if (texto === null || texto === undefined) return null;
		const t = String(texto).trim();
		return t === "" ? null : t;
	}

	function validar(cuerpo) {
		const faltan = [];
		if (!limpiar(cuerpo.nombre)) faltan.push("El nombre es obligatorio");
		if (!limpiar(cuerpo.cargo)) faltan.push("El cargo es obligatorio");
		if (!limpiar(cuerpo.departamento)) faltan.push("El departamento es obligatorio");
		if (faltan.length) error(400, faltan.join(". "));
	}

	function datos(cuerpo) {
		return {
			nombre: limpiar(cuerpo.nombre),
			cargo: limpiar(cuerpo.cargo),
			departamento: limpiar(cuerpo.departamento),
			telefono: limpiar(cuerpo.telefono),
		};
	}

	function buscar(id) {
		const f = db.lista.find((x) => x.id === id);
		if (!f) error(404, `No existe un funcionario con ID ${id}`);
		return f;
	}

	function lista(resultado) {
		return resultado.length
			? { estado: 200, datos: resultado.map((f) => ({ ...f })) }
			: { estado: 204, datos: null };
	}

	window.apiDemo = async function (url, opciones = {}) {
		const metodo = (opciones.method || "GET").toUpperCase();
		const u = new URL(url, location.origin);
		const partes = u.pathname.split("/").filter(Boolean); // api, v1, funcionarios, {id}

		// ----- Inicio de sesión simulado (en la versión real lo hace Spring Security + MySQL) -----
		if (partes[2] === "auth") {
			const accion = partes[3];
			if (accion === "login" && metodo === "POST") {
				const datosLogin = new URLSearchParams(opciones.body || "");
				const usuario = (datosLogin.get("usuario") || "").trim().toLowerCase();
				const clave = datosLogin.get("clave") || "";
				const encontrado = USUARIOS_DEMO.find((x) => x.usuario === usuario && x.clave === clave);
				if (!encontrado) error(401, "Usuario o contraseña incorrectos.");
				const sesion = { usuario: encontrado.usuario, nombre: encontrado.nombre, rol: encontrado.rol };
				try {
					sessionStorage.setItem(CLAVE_SESION, JSON.stringify(sesion));
				} catch (e) {
					/* sin almacenamiento */
				}
				return { estado: 200, datos: sesion };
			}
			if (accion === "logout") {
				try {
					sessionStorage.removeItem(CLAVE_SESION);
				} catch (e) {
					/* sin almacenamiento */
				}
				return { estado: 204, datos: null };
			}
			if (accion === "yo") {
				return { estado: 200, datos: sesionActual() };
			}
			error(405, "Operación no soportada");
		}
		sesionActual(); // la API de funcionarios exige sesión, igual que la real

		const idTexto = partes[3];
		let id = null;
		if (idTexto !== undefined) {
			if (!/^\d+$/.test(idTexto)) error(400, `El valor '${idTexto}' no es válido para id`);
			id = Number(idTexto);
		}
		let cuerpo = {};
		if (opciones.body) {
			try {
				cuerpo = JSON.parse(opciones.body);
			} catch (e) {
				error(400, "El cuerpo de la solicitud no es un JSON válido");
			}
		}

		if (metodo === "GET" && id === null) {
			const nombre = limpiar(u.searchParams.get("nombre"));
			const departamento = limpiar(u.searchParams.get("departamento"));
			let resultado = db.lista;
			if (nombre) {
				resultado = resultado.filter((f) => f.nombre.toLowerCase().includes(nombre.toLowerCase()));
			} else if (departamento) {
				resultado = resultado.filter((f) => f.departamento.toLowerCase() === departamento.toLowerCase());
			}
			return lista(resultado);
		}
		if (metodo === "GET") {
			return { estado: 200, datos: { ...buscar(id) } };
		}
		if (metodo === "POST" && id === null) {
			validar(cuerpo);
			if (cuerpo.id !== undefined && cuerpo.id !== null) {
				error(400, "No se debe enviar un ID al registrar un funcionario");
			}
			const nuevo = { id: db.siguienteId++, ...datos(cuerpo) };
			db.lista.push(nuevo);
			guardar();
			return { estado: 201, datos: { ...nuevo } };
		}
		if (metodo === "PUT" && id !== null) {
			const f = buscar(id);
			validar(cuerpo);
			Object.assign(f, datos(cuerpo));
			guardar();
			return { estado: 200, datos: { ...f } };
		}
		if (metodo === "DELETE" && id !== null) {
			buscar(id);
			db.lista = db.lista.filter((x) => x.id !== id);
			guardar();
			return { estado: 204, datos: null };
		}
		error(405, "Operación no soportada");
	};

	document.addEventListener("DOMContentLoaded", () => {
		const aviso = document.getElementById("aviso-demo");
		if (aviso) aviso.classList.remove("oculto");
	});
})();
