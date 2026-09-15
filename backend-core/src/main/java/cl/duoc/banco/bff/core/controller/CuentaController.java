package cl.duoc.banco.bff.core.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.banco.bff.core.dto.CuentaDTO;
import cl.duoc.banco.bff.core.dto.RetiroRequest;
import cl.duoc.banco.bff.core.dto.RetiroResultDTO;
import cl.duoc.banco.bff.core.service.CuentaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * API de cuentas (acceso de datos + operacion de retiro).
 * Consumida por los BFFs mediante token JWT.
 */
@RestController
@RequestMapping("/api/cuentas")
@RequiredArgsConstructor
public class CuentaController {

    private final CuentaService cuentaService;

    @GetMapping
    public List<CuentaDTO> listarCuentas() {
        return cuentaService.listar();
    }

    @GetMapping("/{cuentaId}")
    public CuentaDTO obtenerCuenta(@PathVariable Long cuentaId) {
        return cuentaService.obtener(cuentaId);
    }

    @PostMapping("/{cuentaId}/retiros")
    public RetiroResultDTO retirar(@PathVariable Long cuentaId, @Valid @RequestBody RetiroRequest request) {
        return cuentaService.retirar(cuentaId, request.monto());
    }
}
