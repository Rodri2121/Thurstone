package gm.thurstone.config;

import gm.thurstone.repository.UsuarioRepository;
import gm.thurstone.service.UsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Crea un psicólogo de demostración la primera vez que arranca la app (cuando no
 * hay ningún usuario), para poder iniciar sesión sin pasos manuales. En
 * producción debe registrarse un usuario real; esta semilla se puede desactivar
 * con {@code thurstone.seed-demo-user=false}.
 */
@Component
@ConditionalOnProperty(prefix = "thurstone", name = "seed-demo-user", matchIfMissing = true)
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;

    public DataInitializer(UsuarioRepository usuarioRepository, UsuarioService usuarioService) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioService = usuarioService;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() == 0) {
            usuarioService.registrar("Psicólogo", "Demo", "psicologo@thurstone.test", "thurstone123");
            // No se registra la contraseña en el log; está definida arriba como semilla de DEV.
            log.warn("Psicólogo de demostración creado (usuario: psicologo@thurstone.test). "
                    + "CAMBIAR EN PRODUCCIÓN o deshabilitar con thurstone.seed-demo-user=false.");
        }
    }
}
