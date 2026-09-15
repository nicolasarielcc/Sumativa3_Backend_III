package cl.duoc.banco.bff.core.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad maestro de cuentas (proviene de intereses.csv, ya limpio).
 */
@Entity
@Table(name = "cuenta")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cuenta {

    @Id
    @Column(name = "cuenta_id")
    private Long cuentaId;

    private String nombre;

    private BigDecimal saldo;

    private Integer edad;

    private String tipo;
}
