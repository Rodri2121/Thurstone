package gm.thurstone.repository;

import gm.thurstone.model.Evaluado;
import gm.thurstone.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvaluadoRepository extends JpaRepository<Evaluado, Long> {

    // Evaluados de un psicólogo, del más reciente al más antiguo.
    List<Evaluado> findByPsicologoOrderByFechaRegistroDesc(Usuario psicologo);
}
