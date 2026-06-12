/* Wizard de la evaluación: muestra una pregunta a la vez, sin retroceso.
   El avance y las selecciones se persisten en sessionStorage para que una
   recarga accidental no pierda 25 minutos de prueba. */
(function () {
    'use strict';

    var CLAVE_INICIO = 'thurstone_inicio';
    var CLAVE_PASO = 'thurstone_paso';
    var CLAVE_SELECCION = 'thurstone_seleccion';

    var formulario = document.getElementById('formularioEvaluacion');
    var preguntas = Array.prototype.slice.call(document.querySelectorAll('.pregunta'));
    var botonSiguiente = document.getElementById('botonSiguiente');
    var numeroPregunta = document.getElementById('numeroPregunta');
    var rellenoProgreso = document.getElementById('rellenoProgreso');

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
        preguntas.length - 1
    );
    mostrar(indiceActual);

    /* Refuerzo visual de selección (respaldo de :has()) + persistencia */
    formulario.addEventListener('change', function (evento) {
        if (!evento.target.classList.contains('tarjeta__check')) {
            return;
        }
        evento.target.closest('.tarjeta')
            .classList.toggle('seleccionada', evento.target.checked);
        guardarSelecciones();
    });

    /* El envío solo puede ocurrir con el botón de la última pregunta */
    formulario.addEventListener('keydown', function (evento) {
        if (evento.key === 'Enter') {
            evento.preventDefault();
        }
    });

    botonSiguiente.addEventListener('click', function () {
        if (indiceActual >= preguntas.length - 1) {
            botonSiguiente.disabled = true;
            formulario.submit();
            return;
        }
        indiceActual += 1;
        sessionStorage.setItem(CLAVE_PASO, String(indiceActual));
        mostrar(indiceActual);
    });

    function mostrar(indice) {
        preguntas.forEach(function (pregunta, i) {
            pregunta.classList.toggle('activa', i === indice);
        });
        numeroPregunta.textContent = String(indice + 1);
        rellenoProgreso.style.width =
            Math.round(((indice + 1) / preguntas.length) * 100) + '%';
        botonSiguiente.textContent = indice === preguntas.length - 1
            ? 'Finalizar y ver resultados'
            : 'Siguiente';
        window.scrollTo({ top: 0, behavior: 'smooth' });
    }

    function guardarSelecciones() {
        var ids = Array.prototype.slice
            .call(formulario.querySelectorAll('.tarjeta__check:checked'))
            .map(function (check) { return check.value; });
        sessionStorage.setItem(CLAVE_SELECCION, JSON.stringify(ids));
    }

    function restaurarSelecciones() {
        var ids = [];
        try {
            ids = JSON.parse(sessionStorage.getItem(CLAVE_SELECCION)) || [];
        } catch (error) {
            ids = [];
        }
        ids.forEach(function (id) {
            var check = formulario.querySelector(
                '.tarjeta__check[value="' + CSS.escape(id) + '"]');
            if (check) {
                check.checked = true;
                check.closest('.tarjeta').classList.add('seleccionada');
            }
        });
    }
})();
