package gm.thurstone.servicio;

import gm.thurstone.modelo.AreaInteres;
import gm.thurstone.modelo.Par;
import gm.thurstone.modelo.ResultadoArea;
import gm.thurstone.modelo.Respuesta;
import gm.thurstone.modelo.Tarea;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TestService {

    // Matriz completa de la Escala de Thurstone (100 pares) cargada desde un CSV
    // editable: numero;area1;ocupacion1;area2;ocupacion2. El área define el
    // puntaje; la ocupación es solo texto (pendiente de cambiar por tareas).
    private static final String RUTA_DATOS = "datos/pares.csv";

    // Puntaje crudo: cada círculo marcado vale 1 punto. "Ambas" marca dos
    // círculos (1 a cada carrera); "ninguna" (X) no suma. Perfil 0-20 por área.
    private static final int PUNTOS_POR_CIRCULO = 1;

    private static final String[] PASTELES = {"pastel-1", "pastel-2", "pastel-3"};

    private static final List<Par> PARES = cargarPares();
    private static final Set<Integer> NUMEROS_VALIDOS =
            PARES.stream().map(Par::numero).collect(Collectors.toUnmodifiableSet());
    private static final Map<AreaInteres, Integer> APARICIONES_POR_AREA = contarApariciones(PARES);
    // Máximo común para que todas las barras compartan el eje 0-20 (cada área se
    // compara la misma cantidad de veces en la matriz completa).
    private static final int MAX_APARICIONES =
            APARICIONES_POR_AREA.isEmpty() ? 0 : Collections.max(APARICIONES_POR_AREA.values());

    public List<Par> obtenerPares() {
        return PARES;
    }

    public int totalPares() {
        return PARES.size();
    }

    /**
     * Convierte los parámetros del formulario (claves "par_N") en respuestas
     * tipadas, descartando números o valores manipulados que no existan.
     */
    public Map<Integer, Respuesta> parsearRespuestas(Map<String, String> parametros) {
        Map<Integer, Respuesta> respuestas = new LinkedHashMap<>();
        parametros.forEach((clave, valor) -> {
            if (clave == null || !clave.startsWith("par_")) {
                return;
            }
            try {
                int numero = Integer.parseInt(clave.substring("par_".length()));
                if (NUMEROS_VALIDOS.contains(numero)) {
                    respuestas.put(numero, Respuesta.valueOf(valor));
                }
            } catch (IllegalArgumentException ignored) {
                // Clave o valor manipulado en el POST: se descarta en silencio.
            }
        });
        return respuestas;
    }

    /**
     * Regla anti-sabotaje validada en servidor (el JS del cliente es evitable).
     * Conductas erráticas que anulan la prueba (según el psicólogo): marcar todo
     * con "X" (todo NINGUNA), todos los círculos (todo AMBAS) o dejar todo en
     * blanco (incompleto). Se generaliza a: prueba incompleta o todas las
     * respuestas idénticas (no discrimina entre carreras).
     */
    public boolean esSabotaje(Map<Integer, Respuesta> respuestas) {
        if (respuestas.size() < PARES.size()) {
            return true;
        }
        long distintas = respuestas.values().stream().distinct().count();
        return distintas <= 1;
    }

    /**
     * Calcula el perfil por carrera (puntaje crudo 0-20), lo ordena de mayor a
     * menor y asigna la clase CSS de colorimetría: 1.º azul oscuro, 2.º azul
     * claro, resto pasteles.
     */
    public List<ResultadoArea> calcularResultados(Map<Integer, Respuesta> respuestas) {
        Map<AreaInteres, Integer> puntos = new EnumMap<>(AreaInteres.class);
        APARICIONES_POR_AREA.keySet().forEach(area -> puntos.put(area, 0));

        for (Par par : PARES) {
            Respuesta respuesta = respuestas.get(par.numero());
            if (respuesta == null) {
                continue;
            }
            switch (respuesta) {
                case PRIMERA -> puntos.merge(par.primera().area(), PUNTOS_POR_CIRCULO, Integer::sum);
                case SEGUNDA -> puntos.merge(par.segunda().area(), PUNTOS_POR_CIRCULO, Integer::sum);
                case AMBAS -> {
                    puntos.merge(par.primera().area(), PUNTOS_POR_CIRCULO, Integer::sum);
                    puntos.merge(par.segunda().area(), PUNTOS_POR_CIRCULO, Integer::sum);
                }
                case NINGUNA -> {
                    // "X" en ambas: no suma puntos a ninguna carrera.
                }
            }
        }

        List<Map.Entry<AreaInteres, Integer>> ordenadas = puntos.entrySet().stream()
                .sorted(Map.Entry.<AreaInteres, Integer>comparingByValue().reversed()
                        .thenComparing(entrada -> entrada.getKey().name()))
                .toList();

        List<ResultadoArea> resultados = new ArrayList<>();
        for (int i = 0; i < ordenadas.size(); i++) {
            AreaInteres area = ordenadas.get(i).getKey();
            int puntaje = ordenadas.get(i).getValue();
            int porcentaje = MAX_APARICIONES == 0 ? 0 : (int) Math.round(puntaje * 100.0 / MAX_APARICIONES);
            String clase = switch (i) {
                case 0 -> "nivel-1";
                case 1 -> "nivel-2";
                default -> PASTELES[(i - 2) % PASTELES.length];
            };
            resultados.add(new ResultadoArea(area.getNombre(), puntaje, porcentaje, clase, area.getCarreras()));
        }
        return resultados;
    }

    private static List<Par> cargarPares() {
        List<Par> lista = new ArrayList<>();
        try (BufferedReader lector = new BufferedReader(new InputStreamReader(
                new ClassPathResource(RUTA_DATOS).getInputStream(), StandardCharsets.UTF_8))) {
            String linea;
            while ((linea = lector.readLine()) != null) {
                String fila = linea.trim();
                if (fila.isEmpty() || fila.startsWith("#")) {
                    continue;
                }
                String[] campos = fila.split(";");
                if (campos.length < 5) {
                    throw new IllegalStateException("Línea con menos de 5 campos en " + RUTA_DATOS + ": " + fila);
                }
                int numero = Integer.parseInt(campos[0].trim());
                Tarea primera = new Tarea(campos[2].trim(), AreaInteres.valueOf(campos[1].trim()));
                Tarea segunda = new Tarea(campos[4].trim(), AreaInteres.valueOf(campos[3].trim()));
                lista.add(new Par(numero, primera, segunda));
            }
        } catch (IOException e) {
            throw new IllegalStateException("No se pudieron cargar los pares desde " + RUTA_DATOS, e);
        }
        if (lista.isEmpty()) {
            throw new IllegalStateException("El archivo de pares está vacío: " + RUTA_DATOS);
        }
        return List.copyOf(lista);
    }

    private static Map<AreaInteres, Integer> contarApariciones(List<Par> pares) {
        Map<AreaInteres, Integer> apariciones = new EnumMap<>(AreaInteres.class);
        for (Par par : pares) {
            apariciones.merge(par.primera().area(), 1, Integer::sum);
            apariciones.merge(par.segunda().area(), 1, Integer::sum);
        }
        return apariciones;
    }
}
