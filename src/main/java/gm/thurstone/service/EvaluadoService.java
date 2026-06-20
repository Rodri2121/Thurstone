package gm.thurstone.service;

import gm.thurstone.model.Evaluado;
import gm.thurstone.model.Usuario;

import java.util.List;

/**
 * Alta y consulta de evaluados (alumnos/pacientes) en el ámbito de un psicólogo.
 */
public interface EvaluadoService {

    Evaluado registrar(Usuario psicologo, String nombreCompleto, String correo, Integer edad, String genero);

    List<Evaluado> listarDe(Usuario psicologo);

    Evaluado buscarPorId(Long id);
}
