package gm.thurstone.model;

/**
 * Un ítem que se muestra al evaluado. No se muestran nombres de ocupaciones ni
 * de carreras, sino la tarea o acción propia de la carrera (p. ej. "Dibujar un
 * paisaje" en vez de "Arquitecto"), para no revelar a qué área pertenece. El
 * campo {@code area} —la carrera a la que pertenece la tarea— vive solo en el
 * servidor y no se renderiza en el DOM.
 */
public record Tarea(String descripcion, AreaInteres area) {
}
