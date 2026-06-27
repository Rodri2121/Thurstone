package gm.thurstone.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Datos del formulario de registro de psicólogo. Reúne en un solo objeto los
 * campos que antes llegaban sueltos como {@code @RequestParam} y declara aquí,
 * junto a cada campo, las reglas de formato (Bean Validation). El controlador
 * solo comprueba el {@code BindingResult}; las reglas de negocio que necesitan
 * la base de datos (correo único) siguen en el servicio.
 */
@Getter
@Setter
public class RegistroDTO {

    @NotBlank(message = "El nombre es obligatorio.")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres.")
    private String nombre;

    // Opcional: solo se acota su longitud.
    @Size(max = 100, message = "El apellido no puede superar los 100 caracteres.")
    private String apellido;

    @NotBlank(message = "El correo es obligatorio.")
    @Email(message = "El correo no es válido.")
    @Size(max = 150, message = "El correo no puede superar los 150 caracteres.")
    private String correo;

    // Tope de 72 porque BCrypt solo considera los primeros 72 bytes.
    @NotBlank(message = "La contraseña es obligatoria.")
    @Size(min = 6, max = 72, message = "La contraseña debe tener entre 6 y 72 caracteres.")
    private String contrasena;
}
