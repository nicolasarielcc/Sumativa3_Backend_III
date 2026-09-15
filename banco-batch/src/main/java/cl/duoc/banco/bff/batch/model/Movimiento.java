package cl.duoc.banco.bff.batch.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Registro de movimiento limpio (proviene de transacciones.csv). */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Movimiento {

    private Long id;
    private LocalDate fecha;
    private BigDecimal monto;
    private String tipo;
}
