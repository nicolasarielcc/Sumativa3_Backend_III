package cl.duoc.banco.bff.core.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Libro mayor de transacciones diarias globales (proviene de transacciones.csv, ya limpio).
 */
@Entity
@Table(name = "movimiento")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Movimiento {

    @Id
    private Long id;

    private LocalDate fecha;

    private BigDecimal monto;

    @Column(name = "tipo")
    private String tipo;
}
