package cl.duoc.banco.bff.atm.service;

import java.math.BigDecimal;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import cl.duoc.banco.bff.atm.dto.RetiroAtmDTO;
import cl.duoc.banco.bff.atm.dto.SaldoAtmDTO;
import cl.duoc.banco.bff.common.dto.backend.CuentaBackendDTO;
import cl.duoc.banco.bff.common.dto.backend.RetiroRequestDTO;
import cl.duoc.banco.bff.common.dto.backend.RetiroResultBackendDTO;
import cl.duoc.banco.bff.common.security.TokenRelay;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Logica del canal ATM: operaciones criticas (consulta de saldo y retiro).
 * Delega en backend-core mediante HTTPS + relay del token, con tolerancia a
 * fallos (Circuit Breaker + Retry + Fallback) via Resilience4j.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BffAtmService {

    private final RestClient coreRestClient;

    @CircuitBreaker(name = "backendCore", fallbackMethod = "consultarSaldoFallback")
    @Retry(name = "backendCore")
    public SaldoAtmDTO consultarSaldo(Long cuentaId) {
        CuentaBackendDTO cuenta = coreRestClient.get()
                .uri("/api/cuentas/{id}", cuentaId)
                .header(HttpHeaders.AUTHORIZATION, bearer())
                .retrieve()
                .body(CuentaBackendDTO.class);

        return new SaldoAtmDTO(cuenta.cuentaId(), cuenta.saldo());
    }

    public SaldoAtmDTO consultarSaldoFallback(Long cuentaId, Throwable t) {
        log.warn("Fallback consulta de saldo para cuenta {}: {}", cuentaId, t.getMessage());
        return new SaldoAtmDTO(cuentaId, null);
    }

    @CircuitBreaker(name = "backendCore", fallbackMethod = "retirarFallback")
    @Retry(name = "backendCore")
    public RetiroAtmDTO retirar(Long cuentaId, BigDecimal monto) {
        RetiroResultBackendDTO result = coreRestClient.post()
                .uri("/api/cuentas/{id}/retiros", cuentaId)
                .header(HttpHeaders.AUTHORIZATION, bearer())
                .body(new RetiroRequestDTO(monto))
                .retrieve()
                .body(RetiroResultBackendDTO.class);

        return new RetiroAtmDTO(result.cuentaId(), result.estado(), result.montoRetirado(), result.saldo());
    }

    /**
     * Fallback seguro: si backend-core no responde, el retiro se RECHAZA
     * (un cajero nunca debe dispensar dinero sin confirmar el saldo).
     */
    public RetiroAtmDTO retirarFallback(Long cuentaId, BigDecimal monto, Throwable t) {
        log.warn("Fallback retiro para cuenta {}: {}", cuentaId, t.getMessage());
        return new RetiroAtmDTO(cuentaId, "RECHAZADO", null, null);
    }

    private String bearer() {
        String token = TokenRelay.currentTokenValue();
        if (token == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado");
        }
        return "Bearer " + token;
    }
}
