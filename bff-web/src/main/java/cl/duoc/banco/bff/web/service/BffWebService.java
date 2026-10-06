package cl.duoc.banco.bff.web.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import cl.duoc.banco.bff.common.dto.backend.CuentaBackendDTO;
import cl.duoc.banco.bff.common.dto.backend.TransaccionBackendDTO;
import cl.duoc.banco.bff.common.security.TokenRelay;
import cl.duoc.banco.bff.web.dto.CuentaWebDetalleDTO;
import cl.duoc.banco.bff.web.dto.CuentaWebResumenDTO;
import cl.duoc.banco.bff.web.dto.ResumenWebDTO;
import cl.duoc.banco.bff.web.dto.TransaccionWebDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Logica del canal Web: consume backend-core (con relay del token) y
 * entrega datos completos mas un resumen calculado, con tolerancia a fallos.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BffWebService {

    private static final Set<String> TIPOS_DEBITO = Set.of("retiro", "compra", "pago");

    private final RestClient coreRestClient;

    @CircuitBreaker(name = "backendCore", fallbackMethod = "listarCuentasFallback")
    @Retry(name = "backendCore")
    public List<CuentaWebResumenDTO> listarCuentas() {
        List<CuentaBackendDTO> cuentas = coreRestClient.get()
                .uri("/api/cuentas")
                .header(HttpHeaders.AUTHORIZATION, bearer())
                .retrieve()
                .body(new ParameterizedTypeReference<List<CuentaBackendDTO>>() {
                });
        return cuentas.stream()
                .map(c -> new CuentaWebResumenDTO(c.cuentaId(), c.nombre(), c.saldo(), c.edad(), c.tipo()))
                .toList();
    }

    public List<CuentaWebResumenDTO> listarCuentasFallback(Throwable t) {
        log.warn("Fallback listado de cuentas (web): {}", t.getMessage());
        return List.of();
    }

    @CircuitBreaker(name = "backendCore", fallbackMethod = "obtenerDetalleFallback")
    @Retry(name = "backendCore")
    public CuentaWebDetalleDTO obtenerDetalle(Long cuentaId) {
        CuentaBackendDTO cuenta = coreRestClient.get()
                .uri("/api/cuentas/{id}", cuentaId)
                .header(HttpHeaders.AUTHORIZATION, bearer())
                .retrieve()
                .body(CuentaBackendDTO.class);

        List<TransaccionBackendDTO> transacciones = coreRestClient.get()
                .uri("/api/transacciones/cuenta/{id}", cuentaId)
                .header(HttpHeaders.AUTHORIZATION, bearer())
                .retrieve()
                .body(new ParameterizedTypeReference<List<TransaccionBackendDTO>>() {
                });

        List<TransaccionWebDTO> movimientos = transacciones.stream()
                .map(t -> new TransaccionWebDTO(t.fecha(), t.tipo(), t.monto(), t.descripcion()))
                .toList();

        ResumenWebDTO resumen = calcularResumen(transacciones);

        return new CuentaWebDetalleDTO(
                cuenta.cuentaId(), cuenta.nombre(), cuenta.saldo(), cuenta.edad(), cuenta.tipo(),
                movimientos, resumen);
    }

    public CuentaWebDetalleDTO obtenerDetalleFallback(Long cuentaId, Throwable t) {
        log.warn("Fallback detalle de cuenta {} (web): {}", cuentaId, t.getMessage());
        return new CuentaWebDetalleDTO(cuentaId, "No disponible", null, null, null,
                List.of(), new ResumenWebDTO(BigDecimal.ZERO, BigDecimal.ZERO, 0));
    }

    private ResumenWebDTO calcularResumen(List<TransaccionBackendDTO> transacciones) {
        BigDecimal totalDepositos = transacciones.stream()
                .filter(t -> "deposito".equals(t.tipo()))
                .map(TransaccionBackendDTO::monto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRetiros = transacciones.stream()
                .filter(t -> TIPOS_DEBITO.contains(t.tipo()))
                .map(TransaccionBackendDTO::monto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ResumenWebDTO(totalDepositos, totalRetiros, transacciones.size());
    }

    private String bearer() {
        String token = TokenRelay.currentTokenValue();
        if (token == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado");
        }
        return "Bearer " + token;
    }
}
