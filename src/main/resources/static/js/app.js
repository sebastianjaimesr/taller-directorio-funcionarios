const API = "/api/v1/funcionarios";
const API_AUTH = "/api/v1/auth";

const formulario = document.getElementById("formulario");
const panelFormulario = document.getElementById("panel-formulario");
const campoId = document.getElementById("id");
const campoNombre = document.getElementById("nombre");
const campoCargo = document.getElementById("cargo");
const campoDepartamento = document.getElementById("departamento");
const campoTelefono = document.getElementById("telefono");
const tituloFormulario = document.getElementById("titulo-formulario");
const subtituloFormulario = document.getElementById("subtitulo-formulario");
const btnGuardar = document.getElementById("btn-guardar");
const tablaCuerpo = document.getElementById("tabla-cuerpo");
const tablaVacia = document.getElementById("tabla-vacia");
const tablaVaciaTexto = document.getElementById("tabla-vacia-texto");
const tituloTabla = document.getElementById("titulo-tabla");
const conteoTabla = document.getElementById("conteo-tabla");
const tipoBusqueda = document.getElementById("tipo-busqueda");
const textoBusqueda = document.getElementById("texto-busqueda");
const listaDepartamentos = document.getElementById("lista-departamentos");
const dialogoEliminar = document.getElementById("dialogo-eliminar");
const contenedorMensajes = document.getElementById("mensaje");

// ---------- Comunicación con la API ----------

/** Hace la petición y devuelve { estado, datos }. datos es null en un 204. */
async function pedir(url, opciones = {}) {
	if (window.MODO_DEMO) {
		try {
			return await window.apiDemo(url, opciones);
		} catch (error) {
			if (error.estado === 401) irAlLogin();
			throw error;
		}
	}
	const respuesta = await fetch(url, {
		headers: { "Content-Type": "application/json", Accept: "application/json" },
		credentials: "same-origin",
		...opciones,
	});
	// Sin sesión (o sesión vencida): volver a la pantalla de inicio de sesión.
	if (respuesta.status === 401) {
		irAlLogin();
		throw new Error("Tu sesión expiró. Inicia sesión de nuevo.");
	}
	let datos = null;
	if (respuesta.status !== 204) {
		const texto = await respuesta.text();
		try {
			datos = texto ? JSON.parse(texto) : null;
		} catch (e) {
			datos = null;
		}
	}
	if (!respuesta.ok) {
		const mensajeError = datos && datos.mensaje ? datos.mensaje : `Error ${respuesta.status}`;
		throw new Error(mensajeError);
	}
	return { estado: respuesta.status, datos };
}

function irAlLogin() {
	location.replace("login.html");
}

// ---------- Sesión ----------

function iniciales(texto) {
	return (texto || "?")
		.trim()
		.split(/\s+/)
		.slice(0, 2)
		.map((p) => p[0])
		.join("")
		.toUpperCase();
}

async function cargarSesion() {
	const { datos } = await pedir(`${API_AUTH}/yo`);
	document.getElementById("nombre-usuario").textContent = datos.nombre;
	document.getElementById("login-usuario").textContent = `@${datos.usuario}`;
	document.getElementById("avatar-usuario").textContent = iniciales(datos.nombre);
	document.body.classList.remove("cargando-sesion");
}

document.getElementById("btn-salir").addEventListener("click", async () => {
	try {
		await pedir(`${API_AUTH}/logout`, { method: "POST" });
	} finally {
		irAlLogin();
	}
});

// ---------- Notificaciones ----------

const ICONOS = {
	exito: '<path d="M12 2a10 10 0 1 0 10 10A10 10 0 0 0 12 2Zm-2 15-5-5 1.4-1.4 3.6 3.6 7.6-7.6L19 8Z"/>',
	error: '<path d="M12 2a10 10 0 1 0 10 10A10 10 0 0 0 12 2Zm1 15h-2v-2h2Zm0-4h-2V7h2Z"/>',
	info: '<path d="M12 2a10 10 0 1 0 10 10A10 10 0 0 0 12 2Zm1 15h-2v-6h2Zm0-8h-2V7h2Z"/>',
};

function mostrarMensaje(texto, tipo = "info") {
	const aviso = document.createElement("div");
	aviso.className = `notificacion ${tipo}`;
	aviso.setAttribute("role", tipo === "error" ? "alert" : "status");
	aviso.innerHTML = `<svg viewBox="0 0 24 24">${ICONOS[tipo] || ICONOS.info}</svg>`;
	const span = document.createElement("span");
	span.textContent = texto;
	aviso.appendChild(span);
	contenedorMensajes.appendChild(aviso);

	setTimeout(() => {
		aviso.classList.add("saliendo");
		setTimeout(() => aviso.remove(), 300);
	}, tipo === "error" ? 6000 : 4000);
}

function ocultarMensaje() {
	contenedorMensajes.innerHTML = "";
}

// ---------- Tabla ----------

const COLORES_DEPTO = [
	["#eef2ff", "#4338ca"],
	["#ecfdf5", "#047857"],
	["#fff7ed", "#c2410c"],
	["#fdf2f8", "#be185d"],
	["#f0f9ff", "#0369a1"],
	["#f5f3ff", "#6d28d9"],
	["#fefce8", "#a16207"],
	["#f0fdfa", "#0f766e"],
];

/** Mismo departamento = mismo color, sin importar mayúsculas. */
function colorDepartamento(nombre) {
	let suma = 0;
	for (const letra of nombre.toLowerCase()) suma = (suma * 31 + letra.charCodeAt(0)) >>> 0;
	return COLORES_DEPTO[suma % COLORES_DEPTO.length];
}

function crearBotonIcono(titulo, ruta, clase, alHacerClic) {
	const boton = document.createElement("button");
	boton.type = "button";
	boton.className = `boton-icono ${clase}`;
	boton.title = titulo;
	boton.setAttribute("aria-label", titulo);
	boton.innerHTML = `<svg viewBox="0 0 24 24"><path d="${ruta}"/></svg>`;
	boton.addEventListener("click", alHacerClic);
	return boton;
}

function celda(contenido) {
	const td = document.createElement("td");
	if (contenido instanceof Node) td.appendChild(contenido);
	else td.textContent = contenido;
	return td;
}

function pintarTabla(funcionarios, mensajeVacio = "No hay funcionarios para mostrar.") {
	tablaCuerpo.innerHTML = "";
	tablaVacia.classList.toggle("oculto", funcionarios.length > 0);
	tablaVaciaTexto.textContent = mensajeVacio;
	conteoTabla.textContent =
		funcionarios.length === 1 ? "1 registro" : `${funcionarios.length} registros`;

	for (const f of funcionarios) {
		const fila = document.createElement("tr");

		// Funcionario: avatar + nombre + ID
		const persona = document.createElement("div");
		persona.className = "persona";
		const avatar = document.createElement("div");
		avatar.className = "avatar";
		avatar.textContent = iniciales(f.nombre);
		const datos = document.createElement("div");
		const nombre = document.createElement("strong");
		nombre.textContent = f.nombre;
		const id = document.createElement("small");
		id.textContent = `ID ${f.id}`;
		datos.append(nombre, id);
		persona.append(avatar, datos);
		fila.appendChild(celda(persona));

		fila.appendChild(celda(f.cargo));

		const etiqueta = document.createElement("span");
		etiqueta.className = "etiqueta";
		const [fondo, texto] = colorDepartamento(f.departamento);
		etiqueta.style.background = fondo;
		etiqueta.style.color = texto;
		etiqueta.textContent = f.departamento;
		fila.appendChild(celda(etiqueta));

		const telefono = document.createElement("span");
		if (f.telefono) {
			telefono.textContent = f.telefono;
		} else {
			telefono.className = "sin-dato";
			telefono.textContent = "—";
		}
		fila.appendChild(celda(telefono));

		const acciones = document.createElement("div");
		acciones.className = "acciones-fila";
		acciones.append(
			crearBotonIcono(
				`Editar a ${f.nombre}`,
				"M3 17.3V21h3.8L17.8 9.9l-3.7-3.7ZM20.7 7a1 1 0 0 0 0-1.4l-2.3-2.3a1 1 0 0 0-1.4 0l-1.8 1.8 3.7 3.7Z",
				"",
				() => cargarEnFormulario(f.id)
			),
			crearBotonIcono(
				`Eliminar a ${f.nombre}`,
				"M6 19a2 2 0 0 0 2 2h8a2 2 0 0 0 2-2V7H6ZM19 4h-3.5l-1-1h-5l-1 1H5v2h14Z",
				"peligro",
				() => eliminar(f)
			)
		);
		const tdAcciones = celda(acciones);
		tdAcciones.className = "derecha";
		fila.appendChild(tdAcciones);

		tablaCuerpo.appendChild(fila);
	}
}

/** Consulta la API con la URL dada y pinta el resultado. Devuelve true si hubo filas. */
async function cargarLista(url, titulo, mensajeVacio) {
	tituloTabla.textContent = titulo;
	try {
		const { datos } = await pedir(url);
		const lista = datos || [];
		pintarTabla(lista, mensajeVacio);
		if (lista.length === 0) {
			mostrarMensaje(mensajeVacio, "info");
		}
		return lista.length > 0;
	} catch (error) {
		pintarTabla([], "No se pudo cargar la información.");
		mostrarMensaje(`No se pudo cargar la información: ${error.message}`, "error");
		return false;
	}
}

/** Recalcula los indicadores y la lista de departamentos sugeridos. */
async function actualizarResumen() {
	try {
		const { datos } = await pedir(API);
		const lista = datos || [];
		const departamentos = new Map();
		for (const f of lista) {
			const clave = f.departamento.toLowerCase();
			if (!departamentos.has(clave)) departamentos.set(clave, f.departamento);
		}
		document.getElementById("ind-total").textContent = lista.length;
		document.getElementById("ind-departamentos").textContent = departamentos.size;
		document.getElementById("ind-telefono").textContent = lista.filter((f) => f.telefono).length;

		listaDepartamentos.innerHTML = "";
		for (const nombre of [...departamentos.values()].sort((a, b) => a.localeCompare(b))) {
			const opcion = document.createElement("option");
			opcion.value = nombre;
			listaDepartamentos.appendChild(opcion);
		}
	} catch (e) {
		/* el error ya se muestra al cargar la tabla */
	}
}

async function listarTodos() {
	await cargarLista(API, "Todos los funcionarios", "El directorio está vacío. Registra el primer funcionario.");
	await actualizarResumen();
}

// ---------- Formulario ----------

function limpiarFormulario() {
	formulario.reset();
	campoId.value = "";
	tituloFormulario.textContent = "Registrar funcionario";
	subtituloFormulario.textContent = "Los campos con * son obligatorios.";
	btnGuardar.textContent = "Registrar";
	panelFormulario.classList.remove("editando");
	for (const campo of [campoNombre, campoCargo, campoDepartamento]) campo.classList.remove("invalido");
}

async function cargarEnFormulario(id) {
	try {
		const { datos } = await pedir(`${API}/${id}`);
		limpiarFormulario();
		campoId.value = datos.id;
		campoNombre.value = datos.nombre;
		campoCargo.value = datos.cargo;
		campoDepartamento.value = datos.departamento;
		campoTelefono.value = datos.telefono || "";
		tituloFormulario.textContent = `Editar funcionario #${datos.id}`;
		subtituloFormulario.textContent = "Modifica los datos y guarda los cambios.";
		btnGuardar.textContent = "Guardar cambios";
		panelFormulario.classList.add("editando");
		panelFormulario.scrollIntoView({ behavior: "smooth", block: "start" });
		campoNombre.focus({ preventScroll: true });
	} catch (error) {
		mostrarMensaje(error.message, "error");
	}
}

formulario.addEventListener("submit", async (evento) => {
	evento.preventDefault();

	const cuerpo = {
		nombre: campoNombre.value.trim(),
		cargo: campoCargo.value.trim(),
		departamento: campoDepartamento.value.trim(),
		telefono: campoTelefono.value.trim(),
	};

	const faltantes = [
		[campoNombre, cuerpo.nombre],
		[campoCargo, cuerpo.cargo],
		[campoDepartamento, cuerpo.departamento],
	];
	let primero = null;
	for (const [campo, valor] of faltantes) {
		campo.classList.toggle("invalido", !valor);
		if (!valor && !primero) primero = campo;
	}
	if (primero) {
		mostrarMensaje("Nombre, cargo y departamento son obligatorios.", "error");
		primero.focus();
		return;
	}

	const id = campoId.value;
	const editando = Boolean(id);
	btnGuardar.disabled = true;
	try {
		if (editando) {
			await pedir(`${API}/${id}`, { method: "PUT", body: JSON.stringify(cuerpo) });
		} else {
			await pedir(API, { method: "POST", body: JSON.stringify(cuerpo) });
		}
		limpiarFormulario();
		await listarTodos();
		mostrarMensaje(
			editando ? "Funcionario actualizado correctamente." : "Funcionario registrado correctamente.",
			"exito"
		);
	} catch (error) {
		mostrarMensaje(error.message, "error");
	} finally {
		btnGuardar.disabled = false;
	}
});

document.getElementById("btn-limpiar").addEventListener("click", () => {
	limpiarFormulario();
	campoNombre.focus();
});

document.getElementById("btn-nuevo").addEventListener("click", () => {
	limpiarFormulario();
	panelFormulario.scrollIntoView({ behavior: "smooth", block: "start" });
	campoNombre.focus({ preventScroll: true });
});

for (const campo of [campoNombre, campoCargo, campoDepartamento]) {
	campo.addEventListener("input", () => campo.classList.remove("invalido"));
}

// ---------- Eliminar (con confirmación) ----------

function confirmar(texto) {
	return new Promise((resolver) => {
		document.getElementById("dialogo-texto").textContent = texto;
		dialogoEliminar.returnValue = "";
		dialogoEliminar.addEventListener(
			"close",
			() => resolver(dialogoEliminar.returnValue === "confirmar"),
			{ once: true }
		);
		dialogoEliminar.showModal();
	});
}

async function eliminar(funcionario) {
	const confirmado = await confirmar(
		`Se eliminará a "${funcionario.nombre}" (ID ${funcionario.id}). Esta acción no se puede deshacer.`
	);
	if (!confirmado) {
		return;
	}
	try {
		await pedir(`${API}/${funcionario.id}`, { method: "DELETE" });
		if (campoId.value === String(funcionario.id)) {
			limpiarFormulario();
		}
		await listarTodos();
		mostrarMensaje("Funcionario eliminado correctamente.", "exito");
	} catch (error) {
		mostrarMensaje(error.message, "error");
	}
}

// ---------- Búsquedas ----------

const PLACEHOLDERS = {
	nombre: "Buscar por fragmento del nombre…",
	departamento: "Nombre completo del departamento…",
	id: "Número de ID…",
};

tipoBusqueda.addEventListener("change", () => {
	const tipo = tipoBusqueda.value;
	textoBusqueda.placeholder = PLACEHOLDERS[tipo];
	textoBusqueda.type = tipo === "id" ? "number" : "search";
	textoBusqueda.min = tipo === "id" ? "1" : "";
	// Solo se sugieren departamentos cuando se filtra por departamento.
	if (tipo === "departamento") textoBusqueda.setAttribute("list", "lista-departamentos");
	else textoBusqueda.removeAttribute("list");
	textoBusqueda.value = "";
	textoBusqueda.focus();
});
textoBusqueda.removeAttribute("list");

document.getElementById("form-busqueda").addEventListener("submit", async (evento) => {
	evento.preventDefault();
	const tipo = tipoBusqueda.value;
	const valor = textoBusqueda.value.trim();
	ocultarMensaje();

	if (tipo === "id") {
		if (!valor) {
			mostrarMensaje("Escribe un ID para consultar.", "error");
			return;
		}
		tituloTabla.textContent = `Funcionario con ID ${valor}`;
		try {
			const { datos } = await pedir(`${API}/${encodeURIComponent(valor)}`);
			pintarTabla([datos]);
		} catch (error) {
			pintarTabla([], error.message);
			mostrarMensaje(error.message, "error");
		}
		return;
	}

	if (tipo === "nombre") {
		if (!valor) {
			mostrarMensaje("Escribe parte del nombre para buscar.", "error");
			return;
		}
		await cargarLista(
			`${API}?nombre=${encodeURIComponent(valor)}`,
			`Resultados para "${valor}"`,
			`No hay funcionarios cuyo nombre contenga "${valor}".`
		);
		return;
	}

	if (!valor) {
		mostrarMensaje("Escribe el nombre del departamento.", "error");
		return;
	}
	await cargarLista(
		`${API}?departamento=${encodeURIComponent(valor)}`,
		`Departamento: ${valor}`,
		`No hay funcionarios en el departamento "${valor}".`
	);
});

document.getElementById("btn-todos").addEventListener("click", async () => {
	textoBusqueda.value = "";
	ocultarMensaje();
	await listarTodos();
});

// ---------- Inicio ----------

(async function iniciar() {
	try {
		await cargarSesion();
	} catch (e) {
		return; // pedir() ya redirige al login si no hay sesión
	}
	await listarTodos();
})();
