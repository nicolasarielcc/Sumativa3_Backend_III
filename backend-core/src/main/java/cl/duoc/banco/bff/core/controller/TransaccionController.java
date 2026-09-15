package cl.duoc.banco.bff.core.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.banco.bff.core.dto.TransaccionDTO;
import cl.duoc.banco.bff.core.service.TransaccionService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/transacciones")
@RequiredArgsConstructor
public class TransaccionController {

    private final TransaccionService transaccionService;

    @GetMapping("/cuenta/{cuentaId}")
    public List<TransaccionDTO> listarPorCuenta(@PathVariable Long cuentaId) {
        return transaccionService.listarPorCuenta(cuentaId);
    }
}
