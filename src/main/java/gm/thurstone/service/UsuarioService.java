package gm.thurstone.service;

import gm.thurstone.model.Usuario;

/**
 * Alta y consulta de psicólogos. La contraseña se recibe en claro y se guarda
 * hasheada (BCrypt) en {@link #registrar}.
 */
public interface UsuarioService {

    Usuario registrar(String nombre, String apellido, String correo, String contrasenaPlana);
}
