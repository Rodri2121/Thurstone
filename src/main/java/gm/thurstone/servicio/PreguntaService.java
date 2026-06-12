package gm.thurstone.servicio;

import gm.thurstone.modelo.Pregunta;
import gm.thurstone.modelo.ResultadoArea;
import gm.thurstone.modelo.Tarea;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PreguntaService {

    // Mock de demostración: 4 bloques con una tarea por área vocacional.
    // Se reemplazará por las preguntas reales del Test de Thurstone.
    private static final List<Pregunta> PREGUNTAS = List.of(
            new Pregunta(1, "¿Cuáles de estas actividades disfrutarías en tu día a día?", List.of(
                    new Tarea(1, "Diseñar los planos de un edificio de apartamentos", "Arquitectura"),
                    new Tarea(2, "Medir la reacción de una sustancia química en el laboratorio", "Química"),
                    new Tarea(3, "Programar una aplicación que automatice un proceso", "Ingeniería de Sistemas"),
                    new Tarea(4, "Atender a un paciente y diagnosticar su enfermedad", "Medicina"),
                    new Tarea(5, "Redactar la defensa legal de un caso", "Derecho"))),
            new Pregunta(2, "¿Cuáles de estos retos te resultarían atractivos?", List.of(
                    new Tarea(6, "Elaborar la maqueta de un centro comercial", "Arquitectura"),
                    new Tarea(7, "Analizar la composición de un medicamento", "Química"),
                    new Tarea(8, "Diseñar la base de datos de una empresa", "Ingeniería de Sistemas"),
                    new Tarea(9, "Interpretar radiografías y exámenes clínicos", "Medicina"),
                    new Tarea(10, "Estudiar jurisprudencia para resolver un conflicto", "Derecho"))),
            new Pregunta(3, "¿En cuáles de estas tareas te imaginas trabajando?", List.of(
                    new Tarea(11, "Remodelar espacios urbanos para mejorar una ciudad", "Arquitectura"),
                    new Tarea(12, "Experimentar para desarrollar nuevos materiales", "Química"),
                    new Tarea(13, "Configurar redes y servidores informáticos", "Ingeniería de Sistemas"),
                    new Tarea(14, "Asistir al equipo médico durante una cirugía", "Medicina"),
                    new Tarea(15, "Representar a una persona en un juicio", "Derecho"))),
            new Pregunta(4, "¿Cuáles de estas actividades elegirías hacer hoy?", List.of(
                    new Tarea(16, "Dibujar bocetos de fachadas y estructuras", "Arquitectura"),
                    new Tarea(17, "Controlar la calidad química de un producto industrial", "Química"),
                    new Tarea(18, "Desarrollar un videojuego o una aplicación móvil", "Ingeniería de Sistemas"),
                    new Tarea(19, "Aplicar primeros auxilios en una emergencia", "Medicina"),
                    new Tarea(20, "Redactar contratos y documentos legales", "Derecho"))));

    private static final Map<Integer, Tarea> TAREAS_POR_ID = PREGUNTAS.stream()
            .flatMap(pregunta -> pregunta.tareas().stream())
            .collect(Collectors.toUnmodifiableMap(Tarea::id, Function.identity()));

    public List<Pregunta> obtenerPreguntas() {
        return PREGUNTAS;
    }

    public int totalTareas() {
        return TAREAS_POR_ID.size();
    }

    /**
     * Califica las tareas seleccionadas: descarta ids duplicados o desconocidos
     * (el POST es manipulable), cuenta una unidad por área y ordena de mayor a
     * menor puntaje. La clase CSS aplica la colorimetría pedida: 1.º azul
     * oscuro, 2.º azul claro y el resto colores pastel.
     */
    public List<ResultadoArea> calcularResultados(List<Integer> seleccionadas) {
        Map<String, Integer> conteo = new LinkedHashMap<>();
        PREGUNTAS.stream()
                .flatMap(pregunta -> pregunta.tareas().stream())
                .forEach(tarea -> conteo.putIfAbsent(tarea.area(), 0));

        seleccionadas.stream()
                .distinct()
                .map(TAREAS_POR_ID::get)
                .filter(tarea -> tarea != null)
                .forEach(tarea -> conteo.merge(tarea.area(), 1, Integer::sum));

        int maximoPorArea = PREGUNTAS.size();
        List<Map.Entry<String, Integer>> ordenadas = conteo.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .toList();

        String[] pasteles = {"pastel-1", "pastel-2", "pastel-3"};
        List<ResultadoArea> resultados = new ArrayList<>();
        for (int i = 0; i < ordenadas.size(); i++) {
            Map.Entry<String, Integer> entrada = ordenadas.get(i);
            String clase = switch (i) {
                case 0 -> "nivel-1";
                case 1 -> "nivel-2";
                default -> pasteles[(i - 2) % pasteles.length];
            };
            int porcentaje = (int) Math.round(entrada.getValue() * 100.0 / maximoPorArea);
            resultados.add(new ResultadoArea(entrada.getKey(), entrada.getValue(), porcentaje, clase));
        }
        return resultados;
    }
}
