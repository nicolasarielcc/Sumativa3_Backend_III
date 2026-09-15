package cl.duoc.banco.bff.batch.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Registro de transaccion limpio (proviene de cuentas_anuales.csv). */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaccion {

    private Long id;
    private Long cuentaId;
    private LocalDate fecha;
    private String tipo;
    private BigDecimal monto;
    private String descripcion;
}
