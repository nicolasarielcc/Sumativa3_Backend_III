package cl.duoc.banco.bff.core.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Historial de operaciones por cuenta (proviene de cuentas_anuales.csv, ya limpio).
 * El signo del movimiento queda determinado por {@link #tipo} (deposito = credito,
 * retiro/compra/pago = debito). El monto se guarda siempre como valor positivo.
 */
@Entity
@Table(name = "transaccion")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cuenta_id")
    private Long cuentaId;

    private LocalDate fecha;

    private String tipo;

    private BigDecimal monto;

    private String descripcion;
}
