package gm.thurstone.modelo;

import java.util.List;

public record Pregunta(int numero, String enunciado, List<Tarea> tareas) {
}
