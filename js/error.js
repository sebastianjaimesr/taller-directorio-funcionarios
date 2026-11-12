// Página de bloqueo tras 3 intentos fallidos.
// Pregunta al servidor cuánto falta para desbloquear y muestra una cuenta regresiva.

const contador = document.getElementById("contador");
const btnVolver = document.getElementById("btn-volver");

function formato(segundos) {
	const m = Math.floor(segundos / 60);
	const s = segundos % 60;
	return `${String(m).padStart(2, "0")}:${String(s).padStart(2, "0")}`;
}

function habilitarVolver() {
	contador.textContent = "00:00";
	btnVolver.classList.remove("deshabilitado");
	btnVolver.removeAttribute("aria-disabled");
}

btnVolver.addEventListener("click", (evento) => {
	if (btnVolver.classList.contains("deshabilitado")) evento.preventDefault();
});

async function consultarEstado() {
	if (window.MODO_DEMO) {
		return (await window.apiDemo("/api/v1/auth/estado")).datos;
	}
	const respuesta = await fetch("/api/v1/auth/estado", { headers: { Accept: "application/json" } });
	return respuesta.json();
}

(async function iniciar() {
	let segundos = 0;
	try {
		const estado = await consultarEstado();
		segundos = estado.bloqueado ? Number(estado.segundos) : 0;
	} catch (e) {
		segundos = 0;
	}
	if (segundos <= 0) {
		habilitarVolver();
		return;
	}
	const fin = Date.now() + segundos * 1000;
	const tic = () => {
		const quedan = Math.max(0, Math.ceil((fin - Date.now()) / 1000));
		contador.textContent = formato(quedan);
		if (quedan === 0) {
			clearInterval(intervalo);
			habilitarVolver();
		}
	};
	const intervalo = setInterval(tic, 1000);
	tic();
})();
