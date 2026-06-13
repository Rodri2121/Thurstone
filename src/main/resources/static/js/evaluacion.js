/* Wizard de la evaluación: muestra una página de pares a la vez, sin retroceso.
   Una vez marcado un par, su respuesta queda FIJA (1 clic, no se puede cambiar).
   El avance y las respuestas se persisten en sessionStorage para que una
   recarga accidental no pierda la prueba. */
(function () {
    'use strict';

    var CLAVE_INICIO = 'thurstone_inicio';
    var CLAVE_PASO = 'thurstone_paso';
    var CLAVE_SELECCION = 'thurstone_seleccion';

    var formulario = document.getElementById('formularioEvaluacion');
    var items = Array.prototype.slice.call(document.querySelectorAll('.par-item'));
    var botonSiguiente = document.getElementById('botonSiguiente');
    var numeroPagina = document.getElementById('numeroPagina');
    var totalPaginasSpan = document.getElementById('totalPaginas');
    var rellenoProgreso = document.getElementById('rellenoProgreso');
    var aviso = document.getElementById('avisoSeleccion');

    function paginaDe(item) {
        return Number(item.getAttribute('data-pagina'));
    }

    var totalPaginas = items.reduce(function (max, item) {
        return Math.max(max, paginaDe(item));
    }, 0) + 1;
    totalPaginasSpan.textContent = String(totalPaginas);

    /* --- Telemetría: timestamp de inicio (respaldo del reloj del servidor) --- */
    var inicio = Number(sessionStorage.getItem(CLAVE_INICIO));
    if (!inicio) {
        inicio = Date.now();
        sessionStorage.setItem(CLAVE_INICIO, String(inicio));
    }
    document.getElementById('inicioCliente').value = String(inicio);

    /* --- Restaurar avance tras una recarga --- */
    restaurarSelecciones();
    var paginaActual = Math.min(
        Number(sessionStorage.getItem(CLAVE_PASO)) || 0,
        totalPaginas - 1
    );
    mostrar(paginaActual);

    /* Al marcar una opción se bloquea el par (1 clic y queda fijo) + persistencia */
    formulario.addEventListener('change', function (evento) {
        var input = evento.target;
        if (input.type !== 'radio' || input.name.indexOf('par_') !== 0) {
            return;
        }
        bloquearGrupo(input.closest('.par'), input);
        if (paginaCompleta(paginaActual)) {
            ocultarAviso();
        }
        guardarSelecciones();
    });

    /* Enter no debe enviar el formulario antes de la última página */
    formulario.addEventListener('keydown', function (evento) {
        if (evento.key === 'Enter') {
            evento.preventDefault();
        }
    });

    botonSiguiente.addEventListener('click', function () {
        // No se puede avanzar dejando pares en blanco: "ninguna" es una opción
        // explícita, así que un par sin marcar siempre es una omisión.
        if (!paginaCompleta(paginaActual)) {
            mostrarAviso();
            return;
        }
        if (paginaActual >= totalPaginas - 1) {
            botonSiguiente.disabled = true;
            formulario.submit();
            return;
        }
        paginaActual += 1;
        sessionStorage.setItem(CLAVE_PASO, String(paginaActual));
        mostrar(paginaActual);
    });

    /* Marca la opción elegida y deshabilita las otras tres del par: así la
       respuesta no se puede cambiar. La elegida queda habilitada para que su
       valor siga viajando en el POST (los controles deshabilitados no se envían). */
    function bloquearGrupo(grupo, elegido) {
        Array.prototype.forEach.call(grupo.querySelectorAll('.opcion'), function (opcion) {
            var radio = opcion.querySelector('input[type="radio"]');
            var esElegido = radio === elegido;
            opcion.classList.toggle('seleccionada', esElegido);
            if (!esElegido) {
                radio.disabled = true;
            }
        });
    }

    function itemsDePagina(pagina) {
        return items.filter(function (item) { return paginaDe(item) === pagina; });
    }

    function mostrar(pagina) {
        items.forEach(function (item) {
            item.classList.toggle('activa', paginaDe(item) === pagina);
        });
        numeroPagina.textContent = String(pagina + 1);
        rellenoProgreso.style.width =
            Math.round(((pagina + 1) / totalPaginas) * 100) + '%';
        botonSiguiente.textContent = pagina === totalPaginas - 1
            ? 'Finalizar y ver resultados'
            : 'Siguiente';
        ocultarAviso();
        window.scrollTo({ top: 0, behavior: 'smooth' });
    }

    function paginaCompleta(pagina) {
        return itemsDePagina(pagina).every(function (item) {
            return item.querySelector('input[type="radio"]:checked');
        });
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
                bloquearGrupo(radio.closest('.par'), radio);
            }
        });
    }
})();
