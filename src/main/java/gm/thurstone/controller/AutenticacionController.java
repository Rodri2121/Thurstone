package gm.thurstone.controller;

import gm.thurstone.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    public String registroForm() {
        return "registro";
    }

    @PostMapping("/registro")
    public String registrar(@RequestParam String nombre,
                            @RequestParam(required = false) String apellido,
                            @RequestParam String correo,
                            @RequestParam String contrasena,
                            RedirectAttributes flash,
                            Model model) {
        try {
            usuarioService.registrar(nombre, apellido, correo, contrasena);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("nombre", nombre);
            model.addAttribute("apellido", apellido);
            model.addAttribute("correo", correo);
            return "registro";
        }
        flash.addFlashAttribute("registrado", true);
        return "redirect:/login";
    }
}
