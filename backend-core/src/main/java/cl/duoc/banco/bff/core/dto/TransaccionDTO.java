package cl.duoc.banco.bff.core.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import cl.duoc.banco.bff.core.entity.Transaccion;

public record TransaccionDTO(
        Long id,
        Long cuentaId,
        LocalDate fecha,
        String tipo,
        BigDecimal monto,
        String descripcion) {

    public static TransaccionDTO from(Transaccion t) {
        return new TransaccionDTO(t.getId(), t.getCuentaId(), t.getFecha(),
                t.getTipo(), t.getMonto(), t.getDescripcion());
    }
}
