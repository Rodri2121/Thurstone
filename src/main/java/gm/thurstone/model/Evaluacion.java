package gm.thurstone.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Un test de Thurstone, desde que el psicólogo lo asigna hasta que el evaluado
 * lo completa. Unifica la asignación (clave de acceso, estado, fecha de
 * asignación, vínculos a psicólogo y evaluado) con el resultado (fecha de
 * finalización, duración, áreas y el perfil completo por área,
 * {@link ResultadoEvaluacion}). El ciclo de vida lo marca {@link EstadoEvaluacion}.
 * Solo se completan las pruebas válidas (las que superan la validación
 * anti-sabotaje del servicio).
 */
@Entity
@Table(name = "evaluacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Evaluacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    // Bloqueo optimista: evita que dos envíos concurrentes de /resultados pisen
    // el resultado del otro (la 2.ª transacción falla en vez de duplicar).
    @Version
    @Column(name = "version")
    private Long version;

    // Evaluado (alumno/paciente) al que se le asignó el test.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_evaluado", nullable = false)
    private Evaluado evaluado;

    // Psicólogo que asignó el test.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_psicologo", nullable = false)
    private Usuario psicologo;

    // Clave que el psicólogo entrega al evaluado para acceder a su test.
    @Column(name = "clave_acceso", nullable = false, unique = true, length = 12)
    private String claveAcceso;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoEvaluacion estado;

    @Column(name = "fecha_asignacion", nullable = false)
    private LocalDateTime fechaAsignacion;

    // Fecha de finalización; null hasta que el evaluado completa el test.
    @Column(name = "fecha")
    private LocalDateTime fecha;

    // Duración total de la prueba en segundos; null si no se pudo medir.
    @Column(name = "duracion_segundos")
    private Long duracionSegundos;

    @Column(name = "area_primaria", length = 100)
    private String areaPrimaria;

    @Column(name = "area_secundaria", length = 100)
    private String areaSecundaria;

    @OneToMany(mappedBy = "evaluacion", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("posicion ASC")
    @Builder.Default
    private List<ResultadoEvaluacion> resultados = new ArrayList<>();

    /** Añade una fila de resultado manteniendo la relación bidireccional. */
    public void agregarResultado(ResultadoEvaluacion resultado) {
        resultado.setEvaluacion(this);
        this.resultados.add(resultado);
    }
}
