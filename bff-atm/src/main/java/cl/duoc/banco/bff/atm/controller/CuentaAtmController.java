package cl.duoc.banco.bff.atm.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.banco.bff.atm.dto.RetiroAtmDTO;
import cl.duoc.banco.bff.atm.dto.RetiroAtmRequest;
import cl.duoc.banco.bff.atm.dto.SaldoAtmDTO;
import cl.duoc.banco.bff.atm.service.BffAtmService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Endpoints del canal ATM (operaciones criticas).
 */
@RestController
@RequestMapping("/api/atm/cuentas")
@RequiredArgsConstructor
public class CuentaAtmController {

    private final BffAtmService bffAtmService;

    @GetMapping("/{cuentaId}/saldo")
    public SaldoAtmDTO consultarSaldo(@PathVariable Long cuentaId) {
        return bffAtmService.consultarSaldo(cuentaId);
    }

    @PostMapping("/{cuentaId}/retiros")
    public RetiroAtmDTO retirar(@PathVariable Long cuentaId, @Valid @RequestBody RetiroAtmRequest request) {
        return bffAtmService.retirar(cuentaId, request.monto());
    }
}
