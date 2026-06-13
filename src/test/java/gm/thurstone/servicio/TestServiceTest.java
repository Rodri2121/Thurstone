package gm.thurstone.servicio;

import gm.thurstone.modelo.Respuesta;
import gm.thurstone.modelo.ResultadoArea;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifica el motor de cálculo del test (puntaje crudo por carrera, orden,
 * colorimetría, anti-sabotaje y descarte de parámetros manipulados) sobre la
 * matriz completa cargada desde el CSV. No necesita el contexto de Spring.
 */
class TestServiceTest {

    private final TestService servicio = new TestService();

    @Test
    void cargaLaMatrizCompleta() {
        assertEquals(100, servicio.totalPares());
    }

    @Test
    void calculaPerfilOrdenadoConColorimetria() {
        // Todas "ninguna" salvo dos pares cuya opción 1 es Ciencias Físicas
        // (pares 1 y 11): así CF queda en primer lugar con 2 puntos.
        Map<Integer, Respuesta> respuestas = new LinkedHashMap<>();
        for (int i = 1; i <= servicio.totalPares(); i++) {
            respuestas.put(i, Respuesta.NINGUNA);
        }
        respuestas.put(1, Respuesta.PRIMERA);
        respuestas.put(11, Respuesta.PRIMERA);

        List<ResultadoArea> resultados = servicio.calcularResultados(respuestas);

        assertEquals(10, resultados.size(), "deben aparecer las 10 áreas");

        ResultadoArea primera = resultados.get(0);
        assertEquals("Ciencias Físicas", primera.area());
        assertEquals(2, primera.puntaje());
        assertEquals(10, primera.porcentaje(), "2 de 20 puntos posibles = 10%");
        assertEquals("nivel-1", primera.claseCss());
        assertEquals("nivel-2", resultados.get(1).claseCss());
        assertEquals("pastel-1", resultados.get(2).claseCss());

        for (int i = 1; i < resultados.size(); i++) {
            assertTrue(resultados.get(i - 1).puntaje() >= resultados.get(i).puntaje(),
                    "el perfil debe quedar ordenado de mayor a menor puntaje");
        }
    }

    @Test
    void detectaSabotaje() {
        // Prueba incompleta (menos respuestas que pares).
        assertTrue(servicio.esSabotaje(Map.of(1, Respuesta.PRIMERA)));

        // Completa pero respondiendo siempre lo mismo: no discrimina.
        Map<Integer, Respuesta> todoIgual = new LinkedHashMap<>();
        for (int i = 1; i <= servicio.totalPares(); i++) {
            todoIgual.put(i, Respuesta.AMBAS);
        }
        assertTrue(servicio.esSabotaje(todoIgual));

        // Completa y con variación: válida.
        Map<Integer, Respuesta> variada = new LinkedHashMap<>();
        for (int i = 1; i <= servicio.totalPares(); i++) {
            variada.put(i, i % 2 == 0 ? Respuesta.PRIMERA : Respuesta.SEGUNDA);
        }
        assertFalse(servicio.esSabotaje(variada));
    }

    @Test
    void parsearDescartaParametrosManipulados() {
        Map<String, String> parametros = new LinkedHashMap<>();
        parametros.put("par_1", "PRIMERA");
        parametros.put("par_999", "PRIMERA");   // número de par inexistente
        parametros.put("par_2", "BASURA");      // valor de respuesta inválido
        parametros.put("inicioCliente", "123"); // parámetro ajeno

        Map<Integer, Respuesta> respuestas = servicio.parsearRespuestas(parametros);

        assertEquals(1, respuestas.size());
        assertEquals(Respuesta.PRIMERA, respuestas.get(1));
    }
}
