package gm.thurstone.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * Genera claves de acceso aleatorias para entregar al evaluado. Usa un alfabeto
 * sin caracteres ambiguos (sin 0/O, 1/I/L) para que la clave se pueda dictar o
 * copiar sin errores. La unicidad se garantiza en el servicio reintentando ante
 * una colisión.
 */
@Component
public class ClaveAccesoGenerator {

    private static final char[] ALFABETO = "ABCDEFGHJKMNPQRSTUVWXYZ23456789".toCharArray();
    private static final int LONGITUD = 8;

    private final SecureRandom random = new SecureRandom();

    public String generar() {
        StringBuilder clave = new StringBuilder(LONGITUD);
        for (int i = 0; i < LONGITUD; i++) {
            clave.append(ALFABETO[random.nextInt(ALFABETO.length)]);
        }
        return clave.toString();
    }
}
