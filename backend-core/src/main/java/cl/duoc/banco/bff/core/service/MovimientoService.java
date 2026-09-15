package cl.duoc.banco.bff.core.service;

import java.util.List;

import org.springframework.stereotype.Service;

import cl.duoc.banco.bff.core.dto.MovimientoDTO;
import cl.duoc.banco.bff.core.repository.MovimientoRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MovimientoService {

    private final MovimientoRepository movimientoRepository;

    public List<MovimientoDTO> listar() {
        return movimientoRepository.findAllByOrderByFechaDesc().stream()
                .map(MovimientoDTO::from)
                .toList();
    }
}
