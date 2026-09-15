package cl.duoc.banco.bff.core.dto;

import java.math.BigDecimal;

import cl.duoc.banco.bff.core.entity.Cuenta;

public record CuentaDTO(
        Long cuentaId,
        String nombre,
        BigDecimal saldo,
        Integer edad,
        String tipo) {

    public static CuentaDTO from(Cuenta c) {
        return new CuentaDTO(c.getCuentaId(), c.getNombre(), c.getSaldo(), c.getEdad(), c.getTipo());
    }
}
