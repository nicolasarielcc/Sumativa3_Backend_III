package cl.duoc.banco.bff.web.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.banco.bff.web.dto.CuentaWebDetalleDTO;
import cl.duoc.banco.bff.web.dto.CuentaWebResumenDTO;
import cl.duoc.banco.bff.web.service.BffWebService;
import lombok.RequiredArgsConstructor;

/**
 * Endpoints del canal Web (datos completos).
 */
@RestController
@RequestMapping("/api/web/cuentas")
@RequiredArgsConstructor
public class CuentaWebController {

    private final BffWebService bffWebService;

    @GetMapping
    public List<CuentaWebResumenDTO> listarCuentas() {
        return bffWebService.listarCuentas();
    }

    @GetMapping("/{cuentaId}")
    public CuentaWebDetalleDTO obtenerDetalle(@PathVariable Long cuentaId) {
        return bffWebService.obtenerDetalle(cuentaId);
    }
}
