package gm.thurstone.service;

import gm.thurstone.model.Evaluacion;
import gm.thurstone.model.ResultadoArea;

import java.time.Duration;
import java.util.List;

/**
 * Persistencia de las evaluaciones completadas. Convierte el perfil calculado
 * por {@link TestService} (lista de {@link ResultadoArea}) en una
 * {@link Evaluacion} y la guarda en PostgreSQL.
 */
public interface EvaluacionService {

    Evaluacion registrar(List<ResultadoArea> resultados, Duration duracion);

    List<Evaluacion> listarTodas();

    Evaluacion buscarPorId(Long id);
}
