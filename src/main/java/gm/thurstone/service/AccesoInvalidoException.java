package gm.thurstone.service;

/**
 * La clave ingresada por el evaluado no existe o el test ya fue completado.
 * El controlador la traduce en un mensaje en la pantalla de acceso.
 */
public class AccesoInvalidoException extends RuntimeException {

    public AccesoInvalidoException(String mensaje) {
        super(mensaje);
    }
}
