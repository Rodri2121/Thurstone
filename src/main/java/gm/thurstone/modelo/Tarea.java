package gm.thurstone.modelo;

/**
 * Una actividad profesional del test. El campo {@code area} (la carrera a la
 * que pertenece) nunca debe enviarse a la vista: el HTML solo recibe id y
 * descripción para que el mapeo tarea→carrera no sea visible en el DOM.
 */
public record Tarea(int id, String descripcion, String area) {
}
