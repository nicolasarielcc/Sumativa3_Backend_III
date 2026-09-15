package cl.duoc.banco.bff.mobile.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.banco.bff.mobile.dto.CuentaMobileDetalleDTO;
import cl.duoc.banco.bff.mobile.dto.CuentaMobileResumenDTO;
import cl.duoc.banco.bff.mobile.service.BffMobileService;
import lombok.RequiredArgsConstructor;

/**
 * Endpoints del canal Mobile (respuestas ligeras).
 */
@RestController
@RequestMapping("/api/mobile/cuentas")
@RequiredArgsConstructor
public class CuentaMobileController {

    private final BffMobileService bffMobileService;

    @GetMapping
    public List<CuentaMobileResumenDTO> listarCuentas() {
        return bffMobileService.listarCuentas();
    }

    @GetMapping("/{cuentaId}")
    public CuentaMobileDetalleDTO obtenerDetalle(@PathVariable Long cuentaId) {
        return bffMobileService.obtenerDetalle(cuentaId);
    }
}
