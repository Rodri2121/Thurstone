package gm.thurstone.service;

import gm.thurstone.dto.EvaluacionDTO;
import gm.thurstone.model.Evaluacion;
import gm.thurstone.model.Evaluado;
import gm.thurstone.model.ResultadoArea;
import gm.thurstone.model.Usuario;

import java.time.Duration;
import java.util.List;

/**
 * Ciclo de vida de una evaluación: el psicólogo la {@link #asignar asigna}
 * (generando una clave), el evaluado {@link #accederPorClave accede} con esa
 * clave y, al terminar, se {@link #completar completa} con el perfil calculado
 * por {@link TestService}.
 */
public interface EvaluacionService {

    /** Crea una evaluación PENDIENTE con clave de acceso única para el evaluado. */
    Evaluacion asignar(Usuario psicologo, Evaluado evaluado);

    /**
     * Valida la clave del evaluado y devuelve su evaluación, marcándola
     * EN_PROGRESO. Lanza {@link AccesoInvalidoException} si la clave no existe o
     * el test ya fue completado.
     */
    Evaluacion accederPorClave(String claveAcceso);

    /** Completa una evaluación con el perfil calculado y la marca COMPLETADO. */
    Evaluacion completar(Long evaluacionId, List<ResultadoArea> resultados, Duration duracion);

    /**
     * Anula una evaluación por sabotaje y la marca INVALIDADO (estado terminal).
     * Tras esto la clave queda inutilizable: no se puede reintentar.
     */
    Evaluacion invalidar(Long evaluacionId);

    /** Resumen de las evaluaciones del psicólogo para el historial (sin perfil). */
    List<EvaluacionDTO> listarPorPsicologo(Usuario psicologo);

    /** Detalle de una evaluación para la vista, con el perfil por área. */
    EvaluacionDTO buscarDtoPorId(Long id);

    /** Entidad para uso interno del servicio; no se expone a las vistas. */
    Evaluacion buscarPorId(Long id);
}
