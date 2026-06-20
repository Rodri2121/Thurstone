package gm.thurstone.controller;

import gm.thurstone.model.Evaluado;
import gm.thurstone.security.UsuarioDetails;
import gm.thurstone.service.EvaluadoService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Alta de evaluados (alumnos/pacientes) por parte del psicólogo autenticado.
 */
@Controller
@RequestMapping("/evaluados")
public class EvaluadoController {

    private final EvaluadoService evaluadoService;

    public EvaluadoController(EvaluadoService evaluadoService) {
        this.evaluadoService = evaluadoService;
    }

    @GetMapping("/nuevo")
    public String nuevo() {
        return "evaluado-form";
    }

    @PostMapping
    public String crear(@AuthenticationPrincipal UsuarioDetails principal,
                        @RequestParam String nombreCompleto,
                        @RequestParam(required = false) String correo,
                        @RequestParam(required = false) String edad,
                        @RequestParam(required = false) String genero,
                        RedirectAttributes flash, Model model) {
        try {
            Evaluado evaluado = evaluadoService.registrar(
                    principal.getUsuario(), nombreCompleto, correo, parseEdad(edad), genero);
            flash.addFlashAttribute("mensaje", "Evaluado registrado: " + evaluado.getNombreCompleto());
            return "redirect:/panel";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "evaluado-form";
        }
    }

    // La edad es opcional; un valor vacío o no numérico se guarda como null.
    private Integer parseEdad(String edad) {
        if (edad == null || edad.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(edad.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
