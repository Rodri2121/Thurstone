package gm.thurstone.controller;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Traduce excepciones de los controladores en páginas amables
 * ({@code templates/error.html}) en lugar de la página técnica por defecto.
 * {@code AccessDeniedException} se deja a Spring Security (responde 403) y la
 * propia {@code error.html} la presenta.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    // Recurso inexistente (p. ej. buscarPorId con un id que no existe) -> 404.
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String noEncontrado(IllegalArgumentException e, Model model) {
        model.addAttribute("titulo", "No encontrado");
        model.addAttribute("mensaje", "El recurso solicitado no existe.");
        return "error";
    }

    // Acción ya no disponible: test ya completado o colisión de concurrencia.
    @ExceptionHandler({IllegalStateException.class, OptimisticLockingFailureException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public String conflicto(Exception e, Model model) {
        model.addAttribute("titulo", "Acción no disponible");
        model.addAttribute("mensaje",
                "Esta acción ya no está disponible (es posible que el test ya se haya completado).");
        return "error";
    }
}
