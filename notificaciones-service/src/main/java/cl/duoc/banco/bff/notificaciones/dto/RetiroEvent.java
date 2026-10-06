package cl.duoc.banco.bff.notificaciones.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Evento de retiro publicado por backend-core en el topic "retiros".
 * Se mantiene identico en productor y consumidor para la (de)serializacion JSON.
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
