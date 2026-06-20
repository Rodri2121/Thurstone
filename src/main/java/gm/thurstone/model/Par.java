package gm.thurstone.model;

/**
 * Un ítem del test: un par de tareas que el evaluado compara. Se responde con
 * una de cuatro opciones (ver {@link Respuesta}).
 */
public record Par(int numero, Tarea primera, Tarea segunda) {
}
