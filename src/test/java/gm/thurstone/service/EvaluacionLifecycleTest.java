package gm.thurstone.service;

import gm.thurstone.dto.EvaluacionDTO;
import gm.thurstone.model.Evaluacion;
import gm.thurstone.model.Evaluado;
import gm.thurstone.model.EstadoEvaluacion;
import gm.thurstone.model.Par;
import gm.thurstone.model.Respuesta;
import gm.thurstone.model.ResultadoArea;
import gm.thurstone.model.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Ciclo de vida de la evaluación sobre la base H2 de tests: el psicólogo la
 * asigna (clave + PENDIENTE), el evaluado accede con la clave (EN_PROGRESO) y se
 * completa con el perfil (COMPLETADO). Cubre también los accesos inválidos.
 */
@SpringBootTest
class EvaluacionLifecycleTest {

    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private EvaluadoService evaluadoService;
    @Autowired
    private EvaluacionService evaluacionService;
    @Autowired
    private TestService testService;

    @Test
    void cicloCompletoAsignarAccederCompletar() {
        Usuario psicologo = usuarioService.registrar("Ana", "García", "ana.ciclo@thurstone.test", "secreta123");
        Evaluado evaluado = evaluadoService.registrar(psicologo, "Juan Pérez", null, 19, "Masculino");

        Evaluacion asignada = evaluacionService.asignar(psicologo, evaluado);
        assertEquals(EstadoEvaluacion.PENDIENTE, asignada.getEstado());
        assertNotNull(asignada.getClaveAcceso());
        assertEquals(8, asignada.getClaveAcceso().length());
        assertNull(asignada.getFecha(), "una asignación no tiene fecha de finalización");

        Evaluacion enCurso = evaluacionService.accederPorClave(asignada.getClaveAcceso());
        assertEquals(asignada.getId(), enCurso.getId());
        assertEquals(EstadoEvaluacion.EN_PROGRESO, enCurso.getEstado());

        List<ResultadoArea> perfil = testService.calcularResultados(respuestasValidas());
        Evaluacion completada = evaluacionService.completar(asignada.getId(), perfil, Duration.ofMinutes(12));

        assertEquals(EstadoEvaluacion.COMPLETADO, completada.getEstado());
        assertNotNull(completada.getFecha());
        assertEquals(720L, completada.getDuracionSegundos());
        assertEquals(10, completada.getResultados().size());
        assertEquals(perfil.get(0).area(), completada.getAreaPrimaria());
        assertEquals(perfil.get(1).area(), completada.getAreaSecundaria());
    }

    @Test
    void claveInexistenteEsRechazada() {
        assertThrows(AccesoInvalidoException.class,
                () -> evaluacionService.accederPorClave("NOEXISTE9"));
    }

    @Test
    void noSePuedeReingresarUnTestCompletado() {
        Usuario psicologo = usuarioService.registrar("Beto", "López", "beto.reingreso@thurstone.test", "secreta123");
        Evaluado evaluado = evaluadoService.registrar(psicologo, "María Gómez", null, null, null);
        Evaluacion asignada = evaluacionService.asignar(psicologo, evaluado);

        evaluacionService.completar(asignada.getId(), testService.calcularResultados(respuestasValidas()), Duration.ZERO);

        assertThrows(AccesoInvalidoException.class,
                () -> evaluacionService.accederPorClave(asignada.getClaveAcceso()));
    }

    @Test
    void pruebaSaboteadaInvalidaLaClave() {
        Usuario psicologo = usuarioService.registrar("Dani", "Soto", "dani.sabotaje@thurstone.test", "secreta123");
        Evaluado evaluado = evaluadoService.registrar(psicologo, "Eva Ríos", null, null, null);
        Evaluacion asignada = evaluacionService.asignar(psicologo, evaluado);
        evaluacionService.accederPorClave(asignada.getClaveAcceso());

        Evaluacion invalidada = evaluacionService.invalidar(asignada.getId());
        assertEquals(EstadoEvaluacion.INVALIDADO, invalidada.getEstado());

        // Tras el sabotaje la clave queda inutilizable: no se puede reintentar.
        assertThrows(AccesoInvalidoException.class,
                () -> evaluacionService.accederPorClave(asignada.getClaveAcceso()));
    }

    @Test
    void cadaAsignacionGeneraUnaClaveDistinta() {
        Usuario psicologo = usuarioService.registrar("Caro", "Ruiz", "caro.claves@thurstone.test", "secreta123");
        Evaluado evaluado = evaluadoService.registrar(psicologo, "Pedro Díaz", null, null, null);

        Evaluacion a = evaluacionService.asignar(psicologo, evaluado);
        Evaluacion b = evaluacionService.asignar(psicologo, evaluado);
        assertNotEquals(a.getClaveAcceso(), b.getClaveAcceso());
    }

    @Test
    void listarPorPsicologoDevuelveResumenComoDto() {
        Usuario psicologo = usuarioService.registrar("Lia", "Vega", "lia.dto@thurstone.test", "secreta123");
        Evaluado evaluado = evaluadoService.registrar(psicologo, "Tomás Roca", null, 20, "Masculino");
        evaluacionService.asignar(psicologo, evaluado);

        List<EvaluacionDTO> dtos = evaluacionService.listarPorPsicologo(psicologo);

        assertEquals(1, dtos.size());
        EvaluacionDTO dto = dtos.get(0);
        assertEquals("Tomás Roca", dto.evaluadoNombre());
        assertEquals(psicologo.getId(), dto.psicologoId());
        assertEquals(EstadoEvaluacion.PENDIENTE, dto.estado());
        assertNotNull(dto.claveAcceso());
        assertTrue(dto.resultados().isEmpty(), "el resumen del historial no incluye el perfil");
    }

    @Test
    void buscarDtoPorIdDeUnTestCompletadoTraeElPerfil() {
        Usuario psicologo = usuarioService.registrar("Ivo", "Paz", "ivo.dto@thurstone.test", "secreta123");
        Evaluado evaluado = evaluadoService.registrar(psicologo, "Sara Lima", null, null, null);
        Evaluacion asignada = evaluacionService.asignar(psicologo, evaluado);
        evaluacionService.completar(asignada.getId(),
                testService.calcularResultados(respuestasValidas()), Duration.ofMinutes(10));

        EvaluacionDTO dto = evaluacionService.buscarDtoPorId(asignada.getId());

        assertEquals(EstadoEvaluacion.COMPLETADO, dto.estado());
        assertEquals("Sara Lima", dto.evaluadoNombre());
        assertEquals(psicologo.getId(), dto.psicologoId());
        assertEquals(10, dto.resultados().size(), "el detalle reconstruye las 10 áreas");
        assertEquals("nivel-1", dto.resultados().get(0).claseCss());
        assertEquals(600L, dto.duracionSegundos());
    }

    // Respuestas completas y con variación (no son sabotaje): perfil con 10 áreas.
    private Map<Integer, Respuesta> respuestasValidas() {
        Map<Integer, Respuesta> respuestas = new LinkedHashMap<>();
        for (Par par : testService.obtenerPares()) {
            respuestas.put(par.numero(), par.numero() % 2 == 0 ? Respuesta.PRIMERA : Respuesta.SEGUNDA);
        }
        assertTrue(respuestas.size() == testService.totalPares());
        return respuestas;
    }
}
