package cl.duoc.banco.bff.batch.model;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Registro de cuenta limpio (proviene de intereses.csv). */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cuenta {

    private Long cuentaId;
    private String nombre;
    private BigDecimal saldo;
    private Integer edad;
    private String tipo;
}
