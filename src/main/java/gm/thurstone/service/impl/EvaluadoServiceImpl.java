package gm.thurstone.service.impl;

import gm.thurstone.model.Evaluado;
import gm.thurstone.model.Usuario;
import gm.thurstone.repository.EvaluadoRepository;
import gm.thurstone.service.EvaluadoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EvaluadoServiceImpl implements EvaluadoService {

    private final EvaluadoRepository evaluadoRepository;

    @Override
    @Transactional
    public Evaluado registrar(Usuario psicologo, String nombreCompleto, String correo, Integer edad, String genero) {
        if (nombreCompleto == null || nombreCompleto.isBlank()) {
            throw new IllegalArgumentException("El nombre del evaluado es obligatorio.");
        }
        Evaluado evaluado = Evaluado.builder()
                .nombreCompleto(nombreCompleto == null ? null : nombreCompleto.trim())
                .correo(correo == null || correo.isBlank() ? null : correo.trim())
                .edad(edad)
                .genero(genero == null || genero.isBlank() ? null : genero.trim())
                .psicologo(psicologo)
                .fechaRegistro(LocalDateTime.now())
                .build();
        return evaluadoRepository.save(evaluado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Evaluado> listarDe(Usuario psicologo) {
        return evaluadoRepository.findByPsicologoOrderByFechaRegistroDesc(psicologo);
    }

    @Override
    @Transactional(readOnly = true)
    public Evaluado buscarPorId(Long id) {
        return evaluadoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe el evaluado con id: " + id));
    }
}
