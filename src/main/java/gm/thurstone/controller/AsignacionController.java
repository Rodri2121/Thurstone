package gm.thurstone.controller;

import gm.thurstone.model.Evaluacion;
import gm.thurstone.model.Evaluado;
import gm.thurstone.model.Usuario;
import gm.thurstone.security.UsuarioDetails;
import gm.thurstone.service.EvaluacionService;
import gm.thurstone.service.EvaluadoService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Asignación de un test a un evaluado: genera la clave de acceso y la muestra
 * para que el psicólogo se la entregue. Cada acción verifica que el evaluado y
 * la evaluación pertenezcan al psicólogo autenticado.
 */
@Controller
@RequestMapping("/asignaciones")
public class AsignacionController {

    private final EvaluadoService evaluadoService;
    private final EvaluacionService evaluacionService;

    public AsignacionController(EvaluadoService evaluadoService, EvaluacionService evaluacionService) {
        this.evaluadoService = evaluadoService;
        this.evaluacionService = evaluacionService;
    }

    @PostMapping
    public String asignar(@AuthenticationPrincipal UsuarioDetails principal,
                          @RequestParam Long evaluadoId) {
        Usuario psicologo = principal.getUsuario();
        Evaluado evaluado = evaluadoService.buscarPorId(evaluadoId);
        if (!evaluado.getPsicologo().getId().equals(psicologo.getId())) {
            throw new AccessDeniedException("El evaluado no pertenece al psicólogo.");
        }
        Evaluacion evaluacion = evaluacionService.asignar(psicologo, evaluado);
        return "redirect:/asignaciones/" + evaluacion.getId();
    }

    @GetMapping("/{id}")
    public String detalle(@AuthenticationPrincipal UsuarioDetails principal,
                          @PathVariable Long id, Model model) {
        Evaluacion evaluacion = evaluacionService.buscarPorId(id);
        if (!evaluacion.getPsicologo().getId().equals(principal.getUsuario().getId())) {
            throw new AccessDeniedException("La evaluación no pertenece al psicólogo.");
        }
        model.addAttribute("evaluacion", evaluacion);
        return "asignacion-clave";
    }
}
