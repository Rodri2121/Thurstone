package gm.thurstone.service.impl;

import gm.thurstone.dto.EvaluacionDTO;
import gm.thurstone.model.Evaluacion;
import gm.thurstone.model.Evaluado;
import gm.thurstone.model.EstadoEvaluacion;
import gm.thurstone.model.ResultadoArea;
import gm.thurstone.model.ResultadoEvaluacion;
import gm.thurstone.model.Usuario;
import gm.thurstone.repository.EvaluacionRepository;
import gm.thurstone.service.AccesoInvalidoException;
import gm.thurstone.service.ClaveAccesoGenerator;
import gm.thurstone.service.EvaluacionService;
import gm.thurstone.service.TestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EvaluacionServiceImpl implements EvaluacionService {

    private final EvaluacionRepository evaluacionRepository;
    private final ClaveAccesoGenerator claveAccesoGenerator;

    @Override
    @Transactional
    public Evaluacion asignar(Usuario psicologo, Evaluado evaluado) {
        Evaluacion evaluacion = Evaluacion.builder()
                .psicologo(psicologo)
                .evaluado(evaluado)
                .claveAcceso(generarClaveUnica())
                .estado(EstadoEvaluacion.PENDIENTE)
                .fechaAsignacion(LocalDateTime.now())
                .build();
        return evaluacionRepository.save(evaluacion);
    }

    @Override
    @Transactional
    public Evaluacion accederPorClave(String claveAcceso) {
        String clave = claveAcceso == null ? "" : claveAcceso.trim().toUpperCase();
        Evaluacion evaluacion = evaluacionRepository.findByClaveAcceso(clave)
                .orElseThrow(() -> new AccesoInvalidoException("La clave no es válida."));

        if (evaluacion.getEstado() == EstadoEvaluacion.COMPLETADO) {
            throw new AccesoInvalidoException("Este test ya fue completado.");
        }
        if (evaluacion.getEstado() == EstadoEvaluacion.INVALIDADO) {
            throw new AccesoInvalidoException("Esta prueba fue invalidada y no puede volver a realizarse.");
        }
        // Primer acceso válido: pasa de PENDIENTE a EN_PROGRESO.
        if (evaluacion.getEstado() == EstadoEvaluacion.PENDIENTE) {
            evaluacion.setEstado(EstadoEvaluacion.EN_PROGRESO);
        }
        return evaluacion;
    }

    @Override
    @Transactional
    public Evaluacion completar(Long evaluacionId, List<ResultadoArea> resultados, Duration duracion) {
        Evaluacion evaluacion = buscarPorId(evaluacionId);
        if (evaluacion.getEstado() == EstadoEvaluacion.COMPLETADO) {
            throw new IllegalStateException("La evaluación ya fue completada: " + evaluacionId);
        }

        evaluacion.setFecha(LocalDateTime.now());
        evaluacion.setDuracionSegundos(
                duracion == null || duracion.isZero() ? null : duracion.toSeconds());

        evaluacion.setAreaPrimaria(resultados.isEmpty() ? null : resultados.get(0).area());
        evaluacion.setAreaSecundaria(resultados.size() > 1 ? resultados.get(1).area() : null);

        // Idempotencia defensiva: si por alguna razón ya hubiera filas, se rehacen.
        evaluacion.getResultados().clear();
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

        evaluacion.setEstado(EstadoEvaluacion.COMPLETADO);
        return evaluacionRepository.save(evaluacion);
    }

    @Override
    @Transactional
    public Evaluacion invalidar(Long evaluacionId) {
        Evaluacion evaluacion = buscarPorId(evaluacionId);
        if (evaluacion.getEstado() == EstadoEvaluacion.COMPLETADO) {
            throw new IllegalStateException("La evaluación ya fue completada: " + evaluacionId);
        }
        evaluacion.setFecha(LocalDateTime.now());
        evaluacion.setEstado(EstadoEvaluacion.INVALIDADO);
        return evaluacionRepository.save(evaluacion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EvaluacionDTO> listarPorPsicologo(Usuario psicologo) {
        return evaluacionRepository.findByPsicologoOrderByFechaAsignacionDesc(psicologo)
                .stream().map(EvaluacionServiceImpl::aResumen).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EvaluacionDTO buscarDtoPorId(Long id) {
        return aDetalle(buscarPorId(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Evaluacion buscarPorId(Long id) {
        return evaluacionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe la evaluación con id: " + id));
    }

    // Reintenta hasta dar con una clave que no exista (colisiones casi imposibles).
    private String generarClaveUnica() {
        String clave;
        do {
            clave = claveAccesoGenerator.generar();
        } while (evaluacionRepository.existsByClaveAcceso(clave));
        return clave;
    }

    // Resumen para el historial: no incluye el detalle del perfil por área.
    private static EvaluacionDTO aResumen(Evaluacion e) {
        return aDTO(e, List.of());
    }

    // Detalle: reconstruye el perfil por área (con su clase de color por ranking)
    // desde las filas persistidas. El acceso a la colección perezosa ocurre dentro
    // de la transacción del servicio, no en la vista.
    private static EvaluacionDTO aDetalle(Evaluacion e) {
        List<ResultadoArea> perfil = new ArrayList<>();
        List<ResultadoEvaluacion> filas = e.getResultados();
        for (int i = 0; i < filas.size(); i++) {
            ResultadoEvaluacion fila = filas.get(i);
            perfil.add(new ResultadoArea(fila.getArea(), fila.getPuntaje(),
                    fila.getPorcentaje(), TestService.claseCssPorRanking(i), fila.getCarreras()));
        }
        return aDTO(e, perfil);
    }

    private static EvaluacionDTO aDTO(Evaluacion e, List<ResultadoArea> resultados) {
        return new EvaluacionDTO(
                e.getId(),
                e.getPsicologo().getId(),
                e.getEvaluado().getNombreCompleto(),
                e.getEstado(),
                e.getClaveAcceso(),
                e.getFechaAsignacion(),
                e.getFecha(),
                e.getDuracionSegundos(),
                e.getAreaPrimaria(),
                e.getAreaSecundaria(),
                resultados);
    }
}
