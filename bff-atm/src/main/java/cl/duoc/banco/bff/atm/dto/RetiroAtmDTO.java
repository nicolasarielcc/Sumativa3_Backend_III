package cl.duoc.banco.bff.atm.dto;

import java.math.BigDecimal;

/** Resultado de retiro del canal ATM. */
public record RetiroAtmDTO(
        Long cuentaId,
        String estado,
        BigDecimal montoRetirado,
        BigDecimal saldo) {
}
