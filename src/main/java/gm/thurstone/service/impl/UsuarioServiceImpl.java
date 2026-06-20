package gm.thurstone.service.impl;

import gm.thurstone.model.Usuario;
import gm.thurstone.repository.UsuarioRepository;
import gm.thurstone.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public Usuario registrar(String nombre, String apellido, String correo, String contrasenaPlana) {
        // Validación en servidor (el navegador es evitable con un POST directo).
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        String correoNormalizado = normalizar(correo);
        if (correoNormalizado.isBlank() || !correoNormalizado.contains("@")) {
            throw new IllegalArgumentException("El correo no es válido.");
        }
        if (contrasenaPlana == null || contrasenaPlana.length() < 6) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres.");
        }
        if (usuarioRepository.existsByCorreo(correoNormalizado)) {
            throw new IllegalArgumentException("Ya existe un usuario con el correo: " + correoNormalizado);
        }
        Usuario usuario = Usuario.builder()
                .nombre(nombre == null ? null : nombre.trim())
                .apellido(apellido == null || apellido.isBlank() ? null : apellido.trim())
                .correo(correoNormalizado)
                .contrasenaHash(passwordEncoder.encode(contrasenaPlana))
                .fechaRegistro(LocalDateTime.now())
                .build();
        return usuarioRepository.save(usuario);
    }

    // El correo se guarda y compara siempre en minúsculas y sin espacios.
    private static String normalizar(String correo) {
        return correo == null ? "" : correo.trim().toLowerCase();
    }
}
