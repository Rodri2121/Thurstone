package gm.thurstone.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * El psicólogo que usa la plataforma: registra evaluados (alumnos/pacientes) y
 * les asigna tests. Es el usuario autenticado del sistema (Spring Security);
 * su {@code correo} actúa de nombre de usuario y {@code contrasenaHash} guarda
 * el hash BCrypt (nunca la contraseña en claro).
 *
 * Implementa {@link Serializable} porque viaja dentro del {@code SecurityContext}
 * de la sesión, que Spring Session JDBC persiste serializado en la base.
 */
@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "apellido", length = 100)
    private String apellido;

    @Column(name = "correo", nullable = false, unique = true, length = 150)
    private String correo;

    @Column(name = "contrasena_hash", nullable = false, length = 255)
    private String contrasenaHash;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;
}
