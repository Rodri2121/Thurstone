package gm.thurstone.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Datos del formulario de alta de un evaluado. La edad es {@link Integer} para
 * que un campo vacío se enlace como {@code null} sin el parseo manual que antes
 * hacía el controlador; correo, edad y género son opcionales.
 */
@Getter
@Setter
public class EvaluadoDTO {

    @NotBlank(message = "El nombre del evaluado es obligatorio.")
    @Size(max = 150, message = "El nombre no puede superar los 150 caracteres.")
    private String nombreCompleto;

    // Opcional: @Email considera válido el valor vacío o nulo.
    @Email(message = "El correo no es válido.")
    @Size(max = 150, message = "El correo no puede superar los 150 caracteres.")
    private String correo;

    // Opcional: si se informa, debe ser un valor humano razonable.
    @Min(value = 1, message = "La edad debe ser mayor que 0.")
    @Max(value = 120, message = "La edad no parece válida.")
    private Integer edad;

    @Size(max = 20, message = "El género no puede superar los 20 caracteres.")
    private String genero;
}
