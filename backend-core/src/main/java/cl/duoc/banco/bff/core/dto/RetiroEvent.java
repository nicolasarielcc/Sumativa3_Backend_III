package cl.duoc.banco.bff.core.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Evento de retiro publicado en el topic "retiros" tras un retiro aprobado.
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class RetiroEvent {

    private Long cuentaId;
    private BigDecimal monto;
    private String estado;
    private String fecha;
}
