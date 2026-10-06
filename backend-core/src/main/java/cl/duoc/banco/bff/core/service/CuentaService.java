package cl.duoc.banco.bff.core.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import cl.duoc.banco.bff.core.config.KafkaProducerConfig;
import cl.duoc.banco.bff.core.dto.CuentaDTO;
import cl.duoc.banco.bff.core.dto.RetiroEvent;
import cl.duoc.banco.bff.core.dto.RetiroResultDTO;
import cl.duoc.banco.bff.core.entity.Cuenta;
import cl.duoc.banco.bff.core.entity.Transaccion;
import cl.duoc.banco.bff.core.repository.CuentaRepository;
import cl.duoc.banco.bff.core.repository.TransaccionRepository;
import lombok.RequiredArgsConstructor;

/**
 * Reglas de negocio de cuentas: consulta, detalle y retiro.
 */
@Service
@RequiredArgsConstructor
public class CuentaService {

    private final CuentaRepository cuentaRepository;
    private final TransaccionRepository transaccionRepository;
    private final KafkaTemplate<String, RetiroEvent> kafkaTemplate;

    public List<CuentaDTO> listar() {
        return cuentaRepository.findAll().stream().map(CuentaDTO::from).toList();
    }

    public CuentaDTO obtener(Long cuentaId) {
        return CuentaDTO.from(buscarCuenta(cuentaId));
    }

    @Transactional
    public RetiroResultDTO retirar(Long cuentaId, BigDecimal monto) {
        Cuenta cuenta = buscarCuenta(cuentaId);

        if (monto.compareTo(cuenta.getSaldo()) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Saldo insuficiente");
        }

        cuenta.setSaldo(cuenta.getSaldo().subtract(monto));
        cuentaRepository.save(cuenta);

        transaccionRepository.save(new Transaccion(
                null, cuentaId, LocalDate.now(), "retiro", monto, "Retiro por cajero automatico"));

        publicarEventoRetiro(cuentaId, monto);

        return new RetiroResultDTO(cuentaId, "APROBADO", monto, cuenta.getSaldo());
    }

    private Cuenta buscarCuenta(Long cuentaId) {
        return cuentaRepository.findById(cuentaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No existe la cuenta con id " + cuentaId));
    }

    /**
     * Publica el evento de retiro en Kafka (de forma asincrona). Si el broker
     * no esta disponible, la falla se registra sin interrumpir la operacion.
     */
    private void publicarEventoRetiro(Long cuentaId, BigDecimal monto) {
        try {
            RetiroEvent evento = new RetiroEvent(cuentaId, monto, "APROBADO", LocalDate.now().toString());
            kafkaTemplate.send(KafkaProducerConfig.TOPIC, evento)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            System.err.println("No se pudo publicar el evento de retiro: " + ex.getMessage());
                        }
                    });
        } catch (Exception e) {
            System.err.println("Error al publicar evento de retiro: " + e.getMessage());
        }
    }
}
