package gm.thurstone.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * El alumno o paciente al que el psicólogo le aplica el test. Los datos clínicos
 * ({@code edad}, {@code genero}) son opcionales y no intervienen en el cálculo
 * del perfil; sirven al registro del psicólogo que lo da de alta.
 */
@Entity
@Table(name = "evaluado")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Evaluado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "nombre_completo", nullable = false, length = 150)
    private String nombreCompleto;

    @Column(name = "correo", length = 150)
    private String correo;

    @Column(name = "edad")
    private Integer edad;

    @Column(name = "genero", length = 20)
    private String genero;

    // Psicólogo que registró al evaluado.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_psicologo", nullable = false)
    private Usuario psicologo;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;
}
