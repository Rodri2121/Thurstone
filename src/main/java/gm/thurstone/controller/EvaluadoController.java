package gm.thurstone.controller;

import gm.thurstone.dto.EvaluadoDTO;
import gm.thurstone.model.Evaluado;
import gm.thurstone.security.UsuarioDetails;
import gm.thurstone.service.EvaluadoService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public String nuevo(Model model) {
        // El formulario vacío respalda los th:field de la vista (th:object).
        model.addAttribute("evaluadoDTO", new EvaluadoDTO());
        return "evaluado-form";
    }

    @PostMapping
    public String crear(@AuthenticationPrincipal UsuarioDetails principal,
                        @Valid @ModelAttribute("evaluadoDTO") EvaluadoDTO form,
                        BindingResult result,
                        RedirectAttributes flash) {
        // La edad ya llega tipada (Integer) y validada; un campo vacío se enlaza
        // como null, sin el parseo manual que antes vivía en este controlador.
        if (result.hasErrors()) {
            return "evaluado-form";
        }
        Evaluado evaluado = evaluadoService.registrar(principal.getUsuario(),
                form.getNombreCompleto(), form.getCorreo(), form.getEdad(), form.getGenero());
        flash.addFlashAttribute("mensaje", "Evaluado registrado: " + evaluado.getNombreCompleto());
        return "redirect:/panel";
    }
}
