const API = "/api/v1/funcionarios";

const formulario = document.getElementById("formulario");
const campoId = document.getElementById("id");
const campoNombre = document.getElementById("nombre");
const campoCargo = document.getElementById("cargo");
const campoDepartamento = document.getElementById("departamento");
const campoTelefono = document.getElementById("telefono");
const tituloFormulario = document.getElementById("titulo-formulario");
const btnGuardar = document.getElementById("btn-guardar");
const tablaCuerpo = document.getElementById("tabla-cuerpo");
const tituloTabla = document.getElementById("titulo-tabla");
const caja = document.getElementById("mensaje");

// ---------- Comunicación con la API ----------

/** Hace la petición y devuelve { estado, datos }. datos es null en un 204. */
async function pedir(url, opciones = {}) {
	const respuesta = await fetch(url, {
		headers: { "Content-Type": "application/json" },
		...opciones,
	});
	let datos = null;
	if (respuesta.status !== 204) {
		const texto = await respuesta.text();
		datos = texto ? JSON.parse(texto) : null;
	}
	if (!respuesta.ok) {
		const mensajeError = datos && datos.mensaje ? datos.mensaje : `Error ${respuesta.status}`;
		throw new Error(mensajeError);
	}
	return { estado: respuesta.status, datos };
}

// ---------- Mensajes ----------

function mostrarMensaje(texto, tipo) {
	caja.textContent = texto;
	caja.className = `mensaje ${tipo}`;
}

function ocultarMensaje() {
	caja.className = "mensaje oculto";
}

// ---------- Tabla ----------

function pintarTabla(funcionarios) {
	tablaCuerpo.innerHTML = "";
	for (const f of funcionarios) {
		const fila = document.createElement("tr");
		for (const valor of [f.id, f.nombre, f.cargo, f.departamento, f.telefono || ""]) {
			const celda = document.createElement("td");
			celda.textContent = valor;
			fila.appendChild(celda);
		}

		const celdaAcciones = document.createElement("td");
		celdaAcciones.className = "acciones-fila";

		const btnEditar = document.createElement("button");
		btnEditar.type = "button";
		btnEditar.textContent = "Editar";
		btnEditar.addEventListener("click", () => cargarEnFormulario(f.id));

		const btnEliminar = document.createElement("button");
		btnEliminar.type = "button";
		btnEliminar.className = "peligro";
		btnEliminar.textContent = "Eliminar";
		btnEliminar.addEventListener("click", () => eliminar(f));

		celdaAcciones.append(btnEditar, btnEliminar);
		fila.appendChild(celdaAcciones);
		tablaCuerpo.appendChild(fila);
	}
}

/** Consulta la API con la URL dada y pinta el resultado. Devuelve true si hubo filas. */
async function cargarLista(url, titulo, mensajeVacio) {
	tituloTabla.textContent = titulo;
	try {
		const { datos } = await pedir(url);
		const lista = datos || [];
		pintarTabla(lista);
		if (lista.length === 0) {
			mostrarMensaje(mensajeVacio, "info");
		}
		return lista.length > 0;
	} catch (error) {
		pintarTabla([]);
		mostrarMensaje(`No se pudo cargar la información: ${error.message}`, "error");
		return false;
	}
}

function listarTodos() {
	return cargarLista(API, "Funcionarios", "El directorio está vacío.");
}

// ---------- Formulario ----------

function limpiarFormulario() {
	formulario.reset();
	campoId.value = "";
	tituloFormulario.textContent = "Registrar funcionario";
	btnGuardar.textContent = "Registrar";
}

async function cargarEnFormulario(id) {
	try {
		const { datos } = await pedir(`${API}/${id}`);
		campoId.value = datos.id;
		campoNombre.value = datos.nombre;
		campoCargo.value = datos.cargo;
		campoDepartamento.value = datos.departamento;
		campoTelefono.value = datos.telefono || "";
		tituloFormulario.textContent = `Editar funcionario #${datos.id}`;
		btnGuardar.textContent = "Guardar cambios";
		ocultarMensaje();
		campoNombre.focus();
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

	if (!cuerpo.nombre || !cuerpo.cargo || !cuerpo.departamento) {
		mostrarMensaje("Nombre, cargo y departamento son obligatorios.", "error");
		return;
	}

	const id = campoId.value;
	try {
		if (id) {
			await pedir(`${API}/${id}`, { method: "PUT", body: JSON.stringify(cuerpo) });
		} else {
			await pedir(API, { method: "POST", body: JSON.stringify(cuerpo) });
		}
		limpiarFormulario();
		await listarTodos();
		mostrarMensaje(id ? "Funcionario actualizado correctamente." : "Funcionario registrado correctamente.", "exito");
	} catch (error) {
		mostrarMensaje(error.message, "error");
	}
});

document.getElementById("btn-limpiar").addEventListener("click", () => {
	limpiarFormulario();
	ocultarMensaje();
});

// ---------- Eliminar ----------

async function eliminar(funcionario) {
	const confirmado = confirm(`¿Eliminar a ${funcionario.nombre} (ID ${funcionario.id})?`);
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

document.getElementById("form-id").addEventListener("submit", async (evento) => {
	evento.preventDefault();
	const id = document.getElementById("buscar-id").value.trim();
	if (!id) {
		mostrarMensaje("Escribe un ID para consultar.", "error");
		return;
	}
	tituloTabla.textContent = `Funcionario con ID ${id}`;
	try {
		const { datos } = await pedir(`${API}/${encodeURIComponent(id)}`);
		pintarTabla([datos]);
		ocultarMensaje();
	} catch (error) {
		pintarTabla([]);
		mostrarMensaje(error.message, "error");
	}
});

document.getElementById("form-nombre").addEventListener("submit", async (evento) => {
	evento.preventDefault();
	const nombre = document.getElementById("buscar-nombre").value.trim();
	if (!nombre) {
		mostrarMensaje("Escribe parte del nombre para buscar.", "error");
		return;
	}
	ocultarMensaje();
	await cargarLista(
		`${API}?nombre=${encodeURIComponent(nombre)}`,
		`Resultados para "${nombre}"`,
		`No hay funcionarios cuyo nombre contenga "${nombre}".`
	);
});

document.getElementById("form-departamento").addEventListener("submit", async (evento) => {
	evento.preventDefault();
	const departamento = document.getElementById("buscar-departamento").value.trim();
	if (!departamento) {
		mostrarMensaje("Escribe el nombre del departamento.", "error");
		return;
	}
	ocultarMensaje();
	await cargarLista(
		`${API}?departamento=${encodeURIComponent(departamento)}`,
		`Departamento: ${departamento}`,
		`No hay funcionarios en el departamento "${departamento}".`
	);
});

document.getElementById("btn-todos").addEventListener("click", async () => {
	document.getElementById("buscar-id").value = "";
	document.getElementById("buscar-nombre").value = "";
	document.getElementById("buscar-departamento").value = "";
	ocultarMensaje();
	await listarTodos();
});

// ---------- Inicio ----------

listarTodos();
