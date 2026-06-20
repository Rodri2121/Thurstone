package gm.thurstone.repository;

import gm.thurstone.model.Evaluacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvaluacionRepository extends JpaRepository<Evaluacion, Long> {

    // Historial de la más reciente a la más antigua.
    List<Evaluacion> findAllByOrderByFechaDesc();
}
