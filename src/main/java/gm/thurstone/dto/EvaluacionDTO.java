package gm.thurstone.dto;

import gm.thurstone.model.EstadoEvaluacion;
import gm.thurstone.model.ResultadoArea;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Vista de una evaluación para las plantillas del psicólogo (historial, detalle
 * y clave asignada). Reemplaza a la entidad {@code Evaluacion} en el modelo para
 * no arrastrar a la capa de presentación ni el {@code Usuario} psicólogo (con su
 * hash de contraseña) ni las relaciones perezosas. El servicio lo arma dentro de
 * su transacción, de modo que la vista no depende de la sesión de Hibernate.
 *
 * {@code psicologoId} no es para mostrar: permite al controlador verificar que la
 * evaluación pertenece al psicólogo autenticado. {@code resultados} viene vacío
 * en el resumen del historial y con el perfil por área en el detalle.
 */
public record EvaluacionDTO(
        Long id,
        Long psicologoId,
        String evaluadoNombre,
        EstadoEvaluacion estado,
        String claveAcceso,
        LocalDateTime fechaAsignacion,
        LocalDateTime fecha,
        Long duracionSegundos,
        String areaPrimaria,
        String areaSecundaria,
        List<ResultadoArea> resultados
) {
}
