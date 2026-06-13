/* Wizard de la evaluación: muestra un par a la vez, sin retroceso.
   El avance y las respuestas se persisten en sessionStorage para que una
   recarga accidental no pierda 25 minutos de prueba. */
(function () {
    'use strict';

    var CLAVE_INICIO = 'thurstone_inicio';
    var CLAVE_PASO = 'thurstone_paso';
    var CLAVE_SELECCION = 'thurstone_seleccion';

    var formulario = document.getElementById('formularioEvaluacion');
    var pares = Array.prototype.slice.call(document.querySelectorAll('.pregunta'));
    var botonSiguiente = document.getElementById('botonSiguiente');
    var numeroPregunta = document.getElementById('numeroPregunta');
    var rellenoProgreso = document.getElementById('rellenoProgreso');
    var aviso = document.getElementById('avisoSeleccion');

    /* --- Telemetría: timestamp de inicio (respaldo del reloj del servidor) --- */
    var inicio = Number(sessionStorage.getItem(CLAVE_INICIO));
    if (!inicio) {
        inicio = Date.now();
        sessionStorage.setItem(CLAVE_INICIO, String(inicio));
    }
    document.getElementById('inicioCliente').value = String(inicio);

    /* --- Restaurar avance tras una recarga --- */
    restaurarSelecciones();
    var indiceActual = Math.min(
        Number(sessionStorage.getItem(CLAVE_PASO)) || 0,
        pares.length - 1
    );
    mostrar(indiceActual);

    /* Refuerzo visual de selección (respaldo de :has()) + persistencia */
    formulario.addEventListener('change', function (evento) {
        var input = evento.target;
        if (input.type !== 'radio' || input.name.indexOf('par_') !== 0) {
            return;
        }
        var grupo = input.closest('.par');
        Array.prototype.forEach.call(
            grupo.querySelectorAll('.opcion'),
            function (opcion) { opcion.classList.remove('seleccionada'); });
        input.closest('.opcion').classList.add('seleccionada');
        ocultarAviso();
        guardarSelecciones();
    });

    /* Enter no debe enviar el formulario antes del último par */
    formulario.addEventListener('keydown', function (evento) {
        if (evento.key === 'Enter') {
            evento.preventDefault();
        }
    });

    botonSiguiente.addEventListener('click', function () {
        // No se puede avanzar sin responder: "ninguna" es una opción explícita,
        // así que un par en blanco siempre es una omisión, no una respuesta.
        if (!parRespondido(indiceActual)) {
            mostrarAviso();
            return;
        }
        if (indiceActual >= pares.length - 1) {
            botonSiguiente.disabled = true;
            formulario.submit();
            return;
        }
        indiceActual += 1;
        sessionStorage.setItem(CLAVE_PASO, String(indiceActual));
        mostrar(indiceActual);
    });

    function mostrar(indice) {
        pares.forEach(function (par, i) {
            par.classList.toggle('activa', i === indice);
        });
        numeroPregunta.textContent = String(indice + 1);
        rellenoProgreso.style.width =
            Math.round(((indice + 1) / pares.length) * 100) + '%';
        botonSiguiente.textContent = indice === pares.length - 1
            ? 'Finalizar y ver resultados'
            : 'Siguiente';
        ocultarAviso();
        window.scrollTo({ top: 0, behavior: 'smooth' });
    }

    function parRespondido(indice) {
        return !!pares[indice].querySelector('input[type="radio"]:checked');
    }

    function mostrarAviso() {
        if (aviso) { aviso.hidden = false; }
    }

    function ocultarAviso() {
        if (aviso) { aviso.hidden = true; }
    }

    function guardarSelecciones() {
        var datos = {};
        Array.prototype.forEach.call(
            formulario.querySelectorAll('input[type="radio"]:checked'),
            function (radio) { datos[radio.name] = radio.value; });
        sessionStorage.setItem(CLAVE_SELECCION, JSON.stringify(datos));
    }

    function restaurarSelecciones() {
        var datos = {};
        try {
            datos = JSON.parse(sessionStorage.getItem(CLAVE_SELECCION)) || {};
        } catch (error) {
            datos = {};
        }
        Object.keys(datos).forEach(function (nombre) {
            var radio = formulario.querySelector(
                'input[name="' + CSS.escape(nombre) + '"][value="' + CSS.escape(datos[nombre]) + '"]');
            if (radio) {
                radio.checked = true;
                radio.closest('.opcion').classList.add('seleccionada');
            }
        });
    }
})();
