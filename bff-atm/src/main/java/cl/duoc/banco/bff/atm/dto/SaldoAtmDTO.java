package cl.duoc.banco.bff.atm.dto;

import java.math.BigDecimal;

/** Consulta de saldo del canal ATM (respuesta minimalista). */
public record SaldoAtmDTO(
        Long cuentaId,
        BigDecimal saldo) {
}
