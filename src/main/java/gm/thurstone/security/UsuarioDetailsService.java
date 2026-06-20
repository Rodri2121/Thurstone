package gm.thurstone.security;

import gm.thurstone.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Carga al psicólogo por su correo (nombre de usuario) para la autenticación.
 * Normaliza el correo a minúsculas para coincidir con cómo se persiste.
 */
@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        String normalizado = correo == null ? "" : correo.trim().toLowerCase();
        return usuarioRepository.findByCorreo(normalizado)
                .map(UsuarioDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("No existe el usuario: " + normalizado));
    }
}
