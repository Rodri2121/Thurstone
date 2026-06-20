package gm.thurstone.repository;

import gm.thurstone.model.Evaluacion;
import gm.thurstone.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EvaluacionRepository extends JpaRepository<Evaluacion, Long> {

    // Acceso del evaluado mediante su clave.
    Optional<Evaluacion> findByClaveAcceso(String claveAcceso);

    boolean existsByClaveAcceso(String claveAcceso);

    // Historial del psicólogo (asignadas y completadas), de la más reciente a la más antigua.
    List<Evaluacion> findByPsicologoOrderByFechaAsignacionDesc(Usuario psicologo);
}
