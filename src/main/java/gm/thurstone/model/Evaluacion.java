package gm.thurstone.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Un test de Thurstone completado y persistido en PostgreSQL. Guarda la fecha,
 * la duración y las dos áreas de mayor interés, junto con el perfil completo por
 * área ({@link ResultadoEvaluacion}). Solo se registran pruebas válidas (las que
 * superan la validación anti-sabotaje del servicio).
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

    @Column(name = "fecha", nullable = false)
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
