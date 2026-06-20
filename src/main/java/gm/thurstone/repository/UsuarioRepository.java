package gm.thurstone.repository;

import gm.thurstone.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // El correo actúa de nombre de usuario en el login.
    Optional<Usuario> findByCorreo(String correo);

    boolean existsByCorreo(String correo);
}
