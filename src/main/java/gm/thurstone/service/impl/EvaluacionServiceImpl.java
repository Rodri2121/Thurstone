package gm.thurstone.service.impl;

import gm.thurstone.model.Evaluacion;
import gm.thurstone.model.ResultadoArea;
import gm.thurstone.model.ResultadoEvaluacion;
import gm.thurstone.repository.EvaluacionRepository;
import gm.thurstone.service.EvaluacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EvaluacionServiceImpl implements EvaluacionService {

    private final EvaluacionRepository evaluacionRepository;

    @Override
    @Transactional
    public Evaluacion registrar(List<ResultadoArea> resultados, Duration duracion) {
        Evaluacion evaluacion = new Evaluacion();
        evaluacion.setFecha(LocalDateTime.now());
        evaluacion.setDuracionSegundos(
                duracion == null || duracion.isZero() ? null : duracion.toSeconds());

        if (!resultados.isEmpty()) {
            evaluacion.setAreaPrimaria(resultados.get(0).area());
        }
        if (resultados.size() > 1) {
            evaluacion.setAreaSecundaria(resultados.get(1).area());
        }

        for (int i = 0; i < resultados.size(); i++) {
            ResultadoArea r = resultados.get(i);
            ResultadoEvaluacion fila = new ResultadoEvaluacion();
            fila.setPosicion(i + 1);
            fila.setArea(r.area());
            fila.setPuntaje(r.puntaje());
            fila.setPorcentaje(r.porcentaje());
            fila.setCarreras(r.carreras());
            evaluacion.agregarResultado(fila);
        }

        return evaluacionRepository.save(evaluacion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Evaluacion> listarTodas() {
        return evaluacionRepository.findAllByOrderByFechaDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public Evaluacion buscarPorId(Long id) {
        return evaluacionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe la evaluación con id: " + id));
    }
}
