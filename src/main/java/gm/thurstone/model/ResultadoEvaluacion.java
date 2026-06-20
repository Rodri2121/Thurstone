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

/**
 * El puntaje de una de las 10 áreas dentro de una {@link Evaluacion}. Es la
 * versión persistida del record {@code ResultadoArea}; se omite la clase CSS
 * (presentación) y se añade {@code posicion} para conservar el ranking.
 */
@Entity
@Table(name = "resultado_evaluacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResultadoEvaluacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evaluacion_id", nullable = false)
    private Evaluacion evaluacion;

    // Ranking 1..N de mayor a menor puntaje.
    @Column(name = "posicion", nullable = false)
    private int posicion;

    @Column(name = "area", nullable = false, length = 100)
    private String area;

    @Column(name = "puntaje", nullable = false)
    private int puntaje;

    @Column(name = "porcentaje", nullable = false)
    private int porcentaje;

    @Column(name = "carreras", columnDefinition = "TEXT")
    private String carreras;
}
