package gm.thurstone.modelo;

/**
 * Un ítem que se muestra al evaluado. Por indicación del psicólogo NO se
 * muestran nombres de ocupaciones ni de carreras, sino la TAREA o acción
 * propia de la carrera (p. ej. "Dibujar un paisaje" en vez de "Arquitecto").
 * El campo {@code area} —la carrera a la que pertenece la tarea— vive solo en
 * el servidor y nunca debe renderizarse en el DOM.
 */
public record Tarea(String descripcion, AreaInteres area) {
}
