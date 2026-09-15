package cl.duoc.banco.bff.core.service;

import java.util.List;

import org.springframework.stereotype.Service;

import cl.duoc.banco.bff.core.dto.TransaccionDTO;
import cl.duoc.banco.bff.core.repository.TransaccionRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransaccionService {

    private final TransaccionRepository transaccionRepository;

    public List<TransaccionDTO> listarPorCuenta(Long cuentaId) {
        return transaccionRepository.findByCuentaIdOrderByFechaDesc(cuentaId).stream()
                .map(TransaccionDTO::from)
                .toList();
    }
}
