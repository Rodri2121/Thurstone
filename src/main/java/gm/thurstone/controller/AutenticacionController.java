package gm.thurstone.controller;

import gm.thurstone.dto.RegistroDTO;
import gm.thurstone.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Login y registro del psicólogo. El POST de login lo procesa Spring Security;
 * aquí solo se sirven las vistas y el alta de usuario.
 */
@Controller
public class AutenticacionController {

    private final UsuarioService usuarioService;

    public AutenticacionController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/registro")
    public String registroForm(Model model) {
        // El formulario vacío respalda los th:field de la vista (th:object).
        model.addAttribute("registroDTO", new RegistroDTO());
        return "registro";
    }

    @PostMapping("/registro")
    public String registrar(@Valid @ModelAttribute("registroDTO") RegistroDTO form,
                            BindingResult result,
                            RedirectAttributes flash) {
        // Errores de formato (campos vacíos, correo mal escrito, contraseña corta):
        // los reúne Bean Validation y la misma vista los muestra junto a cada campo.
        if (result.hasErrors()) {
            return "registro";
        }
        try {
            usuarioService.registrar(form.getNombre(), form.getApellido(),
                    form.getCorreo(), form.getContrasena());
        } catch (IllegalArgumentException e) {
            // Regla de negocio que solo el servicio conoce (p. ej. correo ya
            // registrado): se muestra como error global del formulario.
            result.reject("registro.fallido", e.getMessage());
            return "registro";
        }
        flash.addFlashAttribute("registrado", true);
        return "redirect:/login";
    }
}
