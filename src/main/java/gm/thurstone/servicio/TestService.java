package gm.thurstone.servicio;

import gm.thurstone.modelo.AreaInteres;
import gm.thurstone.modelo.Par;
import gm.thurstone.modelo.ResultadoArea;
import gm.thurstone.modelo.Respuesta;
import gm.thurstone.modelo.Tarea;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static gm.thurstone.modelo.AreaInteres.A;
import static gm.thurstone.modelo.AreaInteres.C;
import static gm.thurstone.modelo.AreaInteres.CB;
import static gm.thurstone.modelo.AreaInteres.CF;
import static gm.thurstone.modelo.AreaInteres.Cp;
import static gm.thurstone.modelo.AreaInteres.E;
import static gm.thurstone.modelo.AreaInteres.H;
import static gm.thurstone.modelo.AreaInteres.L;
import static gm.thurstone.modelo.AreaInteres.M;
import static gm.thurstone.modelo.AreaInteres.P;

@Service
public class TestService {

    // Subconjunto de DEMO con TAREAS (acciones de cada carrera), no nombres de
    // ocupaciones. El psicólogo entregará el contenido real: una matriz de 20
    // carreras universitarias (~190 pares). Para ampliarlo basta extender esta
    // lista. En la demo cada carrera aparece dos veces —una como primera y otra
    // como segunda opción— para que el perfil quede equilibrado.
    private static final List<Par> PARES = List.of(
            par(1, "Medir la reacción de una sustancia en el laboratorio", CF,
                    "Atender y diagnosticar a un paciente", CB),
            par(2, "Estudiar el comportamiento de los animales", CB,
                    "Resolver un problema mediante cálculos matemáticos", Cp),
            par(3, "Analizar grandes volúmenes de datos y estadísticas", Cp,
                    "Administrar las cuentas y finanzas de un negocio", C),
            par(4, "Negociar la compra y venta de productos", C,
                    "Dirigir y coordinar a un equipo de personas", E),
            par(5, "Tomar decisiones para administrar una empresa", E,
                    "Convencer a una audiencia con un discurso", P),
            par(6, "Defender una postura argumentando con firmeza", P,
                    "Redactar un reportaje o artículo", L),
            par(7, "Escribir y corregir textos y publicaciones", L,
                    "Ayudar a personas en situación vulnerable", H),
            par(8, "Orientar y acompañar a quien lo necesita", H,
                    "Crear una obra visual o una pintura", A),
            par(9, "Esculpir o modelar una figura artística", A,
                    "Interpretar una pieza con un instrumento", M),
            par(10, "Componer o dirigir una obra musical", M,
                    "Calcular las fuerzas que soporta una estructura", CF));

    // Puntaje crudo confirmado por el psicólogo: cada círculo marcado vale 1
    // punto. "Ambas" marca dos círculos (1 a cada carrera); "ninguna" (X) no
    // suma. El perfil se grafica directo en una escala 0–20 por carrera, sin
    // baremos ni percentiles.
    private static final int PUNTOS_POR_CIRCULO = 1;

    private static final String[] PASTELES = {"pastel-1", "pastel-2", "pastel-3"};

    private static final Set<Integer> NUMEROS_VALIDOS =
            PARES.stream().map(Par::numero).collect(Collectors.toUnmodifiableSet());
    private static final Map<AreaInteres, Integer> APARICIONES_POR_AREA = contarApariciones(PARES);
    // Máximo común para que todas las barras compartan un mismo eje: en el test
    // real cada carrera se compara la misma cantidad de veces (escala 0–20).
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
     * El psicólogo definió como conductas erráticas que anulan la prueba: marcar
     * todo con "X" (todo NINGUNA), marcar todos los círculos (todo AMBAS) o
     * dejar todo en blanco (incompleto). Se generaliza a: prueba incompleta o
     * todas las respuestas idénticas (no discrimina entre carreras).
     */
    public boolean esSabotaje(Map<Integer, Respuesta> respuestas) {
        if (respuestas.size() < PARES.size()) {
            return true;
        }
        long distintas = respuestas.values().stream().distinct().count();
        return distintas <= 1;
    }

    /**
     * Calcula el perfil por carrera (puntaje crudo), lo ordena de mayor a menor
     * y asigna la clase CSS de colorimetría: 1.º azul oscuro, 2.º azul claro,
     * resto pasteles.
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

    private static Par par(int numero, String desc1, AreaInteres area1, String desc2, AreaInteres area2) {
        return new Par(numero, new Tarea(desc1, area1), new Tarea(desc2, area2));
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
