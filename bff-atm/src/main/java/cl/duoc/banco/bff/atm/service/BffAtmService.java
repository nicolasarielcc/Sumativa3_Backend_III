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
import lombok.RequiredArgsConstructor;

/**
 * Logica del canal ATM: operaciones criticas (consulta de saldo y retiro).
 * Delega en backend-core mediante HTTPS + relay del token JWT.
 */
@Service
@RequiredArgsConstructor
public class BffAtmService {

    private final RestClient coreRestClient;

    public SaldoAtmDTO consultarSaldo(Long cuentaId) {
        CuentaBackendDTO cuenta = coreRestClient.get()
                .uri("/api/cuentas/{id}", cuentaId)
                .header(HttpHeaders.AUTHORIZATION, bearer())
                .retrieve()
                .body(CuentaBackendDTO.class);

        return new SaldoAtmDTO(cuenta.cuentaId(), cuenta.saldo());
    }

    public RetiroAtmDTO retirar(Long cuentaId, BigDecimal monto) {
        RetiroResultBackendDTO result = coreRestClient.post()
                .uri("/api/cuentas/{id}/retiros", cuentaId)
                .header(HttpHeaders.AUTHORIZATION, bearer())
                .body(new RetiroRequestDTO(monto))
                .retrieve()
                .body(RetiroResultBackendDTO.class);

        return new RetiroAtmDTO(result.cuentaId(), result.estado(), result.montoRetirado(), result.saldo());
    }

    private String bearer() {
        String token = TokenRelay.currentTokenValue();
        if (token == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado");
        }
        return "Bearer " + token;
    }
}
