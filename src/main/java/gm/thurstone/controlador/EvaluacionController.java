package gm.thurstone.controlador;

import gm.thurstone.modelo.ResultadoArea;
import gm.thurstone.modelo.Respuesta;
import gm.thurstone.servicio.TestService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Controller
public class EvaluacionController {

    private static final String ATRIBUTO_INICIO = "inicioEvaluacion";

    // Cuántos pares se muestran por página del wizard (100 pares / 10 = 10 páginas).
    private static final int PARES_POR_PAGINA = 10;

    private final TestService testService;

    public EvaluacionController(TestService testService) {
        this.testService = testService;
    }

    @GetMapping("/")
    public String inicio() {
        return "index";
    }

    @GetMapping("/evaluacion")
    public String evaluacion(Model model, HttpSession session) {
        // El reloj autoritativo vive en la sesión del servidor; el timestamp de
        // sessionStorage del navegador es solo telemetría de respaldo, porque
        // cualquier valor que venga del cliente es manipulable. Solo se fija si
        // no existe, para no reiniciar el conteo si el usuario recarga la página.
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

        Duration duracion = calcularDuracion(session, inicioCliente);
        model.addAttribute("duracion", duracion.isZero() ? null : formatear(duracion));

        Map<Integer, Respuesta> respuestas = testService.parsearRespuestas(parametros);

        // Validación anti-sabotaje en servidor (el JS del cliente es evitable).
        boolean invalida = testService.esSabotaje(respuestas);
        model.addAttribute("invalida", invalida);
        if (invalida) {
            return "resultados";
        }

        List<ResultadoArea> resultados = testService.calcularResultados(respuestas);
        model.addAttribute("resultados", resultados);
        model.addAttribute("area1", resultados.get(0).area());
        model.addAttribute("area2", resultados.get(1).area());
        return "resultados";
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
