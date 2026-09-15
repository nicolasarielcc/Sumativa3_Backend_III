package cl.duoc.banco.bff.core.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.banco.bff.core.dto.MovimientoDTO;
import cl.duoc.banco.bff.core.service.MovimientoService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/movimientos")
@RequiredArgsConstructor
public class MovimientoController {

    private final MovimientoService movimientoService;

    @GetMapping
    public List<MovimientoDTO> listar() {
        return movimientoService.listar();
    }
}
