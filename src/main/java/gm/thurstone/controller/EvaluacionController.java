package gm.thurstone.controller;

import gm.thurstone.model.Evaluacion;
import gm.thurstone.model.ResultadoArea;
import gm.thurstone.model.ResultadoEvaluacion;
import gm.thurstone.model.Respuesta;
import gm.thurstone.security.UsuarioDetails;
import gm.thurstone.service.AccesoInvalidoException;
import gm.thurstone.service.EvaluacionService;
import gm.thurstone.service.TestService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class EvaluacionController {

    // Reloj autoritativo del servidor para la duración de la prueba.
    private static final String ATRIBUTO_INICIO = "inicioEvaluacion";
    // Id de la evaluación cuya clave validó el evaluado en esta sesión.
    private static final String ATRIBUTO_EVALUACION = "evaluacionId";

    // Cuántos pares se muestran por página del wizard (100 pares / 10 = 10 páginas).
    private static final int PARES_POR_PAGINA = 10;

    private final TestService testService;
    private final EvaluacionService evaluacionService;

    public EvaluacionController(TestService testService, EvaluacionService evaluacionService) {
        this.testService = testService;
        this.evaluacionService = evaluacionService;
    }

    @GetMapping("/")
    public String inicio() {
        return "index";
    }

    // ===================== Flujo del evaluado (acceso por clave) =====================

    @GetMapping("/acceso")
    public String acceso() {
        return "acceso";
    }

    @PostMapping("/acceso")
    public String validarClave(@RequestParam String clave, HttpSession session, Model model) {
        try {
            Evaluacion evaluacion = evaluacionService.accederPorClave(clave);
            // Nuevo intento: se fija la evaluación y se reinicia el reloj del servidor.
            session.setAttribute(ATRIBUTO_EVALUACION, evaluacion.getId());
            session.removeAttribute(ATRIBUTO_INICIO);
            return "redirect:/evaluacion";
        } catch (AccesoInvalidoException e) {
            model.addAttribute("error", e.getMessage());
            return "acceso";
        }
    }

    @GetMapping("/evaluacion")
    public String evaluacion(Model model, HttpSession session) {
        // Sin una clave validada en la sesión no se puede rendir el test.
        if (session.getAttribute(ATRIBUTO_EVALUACION) == null) {
            return "redirect:/acceso";
        }
        // El reloj autoritativo vive en la sesión del servidor; solo se fija si no
        // existe, para no reiniciar el conteo si el usuario recarga la página.
        if (session.getAttribute(ATRIBUTO_INICIO) == null) {
            session.setAttribute(ATRIBUTO_INICIO, Instant.now());
        }
        model.addAttribute("pares", testService.obtenerPares());
        model.addAttribute("paresPorPagina", PARES_POR_PAGINA);
        return "evaluacion";
    }

    @PostMapping("/resultados")
    public String resultados(
            @RequestParam Map<String, String> parametros,
            @RequestParam(name = "inicioCliente", required = false) String inicioCliente,
            HttpSession session, Model model) {

        Object idSesion = session.getAttribute(ATRIBUTO_EVALUACION);
        if (!(idSesion instanceof Long evaluacionId)) {
            // No hay un test asignado en curso: se vuelve al acceso por clave.
            return "redirect:/acceso";
        }

        Duration duracion = calcularDuracion(session, inicioCliente);
        model.addAttribute("duracion", duracion.isZero() ? null : formatear(duracion));

        Map<Integer, Respuesta> respuestas = testService.parsearRespuestas(parametros);

        // Validación anti-sabotaje en servidor (el JS del cliente es evitable).
        boolean invalida = testService.esSabotaje(respuestas);
        model.addAttribute("invalida", invalida);
        if (invalida) {
            // El sabotaje anula la prueba (estado INVALIDADO) y consume la clave:
            // no se puede reintentar con la misma clave.
            evaluacionService.invalidar(evaluacionId);
            session.removeAttribute(ATRIBUTO_EVALUACION);
            return "resultados";
        }

        List<ResultadoArea> resultados = testService.calcularResultados(respuestas);

        // Completa la evaluación asignada (estado COMPLETADO) y la persiste.
        evaluacionService.completar(evaluacionId, resultados, duracion);
        session.removeAttribute(ATRIBUTO_EVALUACION);

        model.addAttribute("resultados", resultados);
        if (!resultados.isEmpty()) {
            model.addAttribute("area1", resultados.get(0).area());
        }
        if (resultados.size() > 1) {
            model.addAttribute("area2", resultados.get(1).area());
        }
        return "resultados";
    }

    // ===================== Zona del psicólogo =====================

    @GetMapping("/evaluaciones")
    public String historial(@AuthenticationPrincipal UsuarioDetails principal, Model model) {
        model.addAttribute("evaluaciones",
                evaluacionService.listarPorPsicologo(principal.getUsuario()));
        return "evaluaciones";
    }

    @GetMapping("/evaluaciones/{id}")
    public String detalle(@AuthenticationPrincipal UsuarioDetails principal,
                          @PathVariable Long id, Model model) {
        Evaluacion evaluacion = evaluacionService.buscarPorId(id);
        if (!evaluacion.getPsicologo().getId().equals(principal.getUsuario().getId())) {
            throw new AccessDeniedException("La evaluación no pertenece al psicólogo.");
        }
        model.addAttribute("evaluacion", evaluacion);
        model.addAttribute("resultados", aResultadoArea(evaluacion));
        model.addAttribute("duracion", evaluacion.getDuracionSegundos() == null ? null
                : formatear(Duration.ofSeconds(evaluacion.getDuracionSegundos())));
        return "detalle";
    }

    // Reconstruye los ResultadoArea (con su clase de color por ranking) desde las
    // filas persistidas, para reusar el mismo gráfico de la pantalla de resultados.
    private List<ResultadoArea> aResultadoArea(Evaluacion evaluacion) {
        List<ResultadoArea> lista = new ArrayList<>();
        List<ResultadoEvaluacion> filas = evaluacion.getResultados();
        for (int i = 0; i < filas.size(); i++) {
            ResultadoEvaluacion fila = filas.get(i);
            lista.add(new ResultadoArea(fila.getArea(), fila.getPuntaje(),
                    fila.getPorcentaje(), TestService.claseCssPorRanking(i), fila.getCarreras()));
        }
        return lista;
    }

    private Duration calcularDuracion(HttpSession session, String inicioCliente) {
        Object inicio = session.getAttribute(ATRIBUTO_INICIO);
        session.removeAttribute(ATRIBUTO_INICIO);
        if (inicio instanceof Instant instante) {
            return Duration.between(instante, Instant.now());
        }
        if (inicioCliente == null || inicioCliente.isBlank()) {
            return Duration.ZERO;
        }
        try {
            Instant instante = Instant.ofEpochMilli(Long.parseLong(inicioCliente.trim()));
            Duration duracion = Duration.between(instante, Instant.now());
            return duracion.isNegative() ? Duration.ZERO : duracion;
        } catch (NumberFormatException e) {
            return Duration.ZERO;
        }
    }

    private String formatear(Duration duracion) {
        return "%d min %02d s".formatted(duracion.toMinutes(), duracion.toSecondsPart());
    }
}
