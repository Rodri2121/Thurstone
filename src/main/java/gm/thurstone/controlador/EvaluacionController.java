package gm.thurstone.controlador;

import gm.thurstone.modelo.ResultadoArea;
import gm.thurstone.servicio.PreguntaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Controller
public class EvaluacionController {

    private static final String ATRIBUTO_INICIO = "inicioEvaluacion";

    private final PreguntaService preguntaService;

    public EvaluacionController(PreguntaService preguntaService) {
        this.preguntaService = preguntaService;
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
        model.addAttribute("preguntas", preguntaService.obtenerPreguntas());
        return "evaluacion";
    }

    @PostMapping("/resultados")
    public String resultados(
            @RequestParam(name = "selecciones", required = false) List<Integer> selecciones,
            @RequestParam(name = "inicioCliente", required = false) String inicioCliente,
            HttpSession session, Model model) {

        Duration duracion = calcularDuracion(session, inicioCliente);
        model.addAttribute("duracion", duracion.isZero() ? null : formatear(duracion));

        List<Integer> ids = selecciones == null ? List.of() : selecciones.stream().distinct().toList();

        // Regla anti-sabotaje validada en servidor (el JS del cliente es evitable):
        // marcar todas las actividades o ninguna invalida la prueba.
        boolean sabotaje = ids.isEmpty() || ids.size() >= preguntaService.totalTareas();
        model.addAttribute("invalida", sabotaje);
        if (sabotaje) {
            return "resultados";
        }

        List<ResultadoArea> resultados = preguntaService.calcularResultados(ids);
        model.addAttribute("resultados", resultados);
        model.addAttribute("carrera1", resultados.get(0).area());
        model.addAttribute("carrera2", resultados.get(1).area());
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
