package gm.thurstone.model;

/**
 * Ciclo de vida de una {@link Evaluacion} desde que el psicólogo la asigna
 * hasta que el evaluado la termina.
 */
public enum EstadoEvaluacion {

    /** Asignada por el psicólogo; el evaluado todavía no entró con su clave. */
    PENDIENTE,

    /** El evaluado validó la clave y está respondiendo el test. */
    EN_PROGRESO,

    /** El test se respondió y se calculó el perfil; ya no puede reutilizarse. */
    COMPLETADO,

    /** La prueba se anuló por sabotaje (incompleta o sin discriminar); la clave queda inutilizable. */
    INVALIDADO
}
