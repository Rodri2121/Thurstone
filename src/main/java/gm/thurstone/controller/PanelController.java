package gm.thurstone.controller;

import gm.thurstone.model.Usuario;
import gm.thurstone.security.UsuarioDetails;
import gm.thurstone.service.EvaluadoService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Panel del psicólogo: lista de sus evaluados con accesos a registrar uno nuevo,
 * asignarles el test y ver el historial.
 */
@Controller
public class PanelController {

    private final EvaluadoService evaluadoService;

    public PanelController(EvaluadoService evaluadoService) {
        this.evaluadoService = evaluadoService;
    }

    @GetMapping("/panel")
    public String panel(@AuthenticationPrincipal UsuarioDetails principal, Model model) {
        Usuario psicologo = principal.getUsuario();
        model.addAttribute("psicologo", psicologo);
        model.addAttribute("evaluados", evaluadoService.listarDe(psicologo));
        return "panel";
    }
}
