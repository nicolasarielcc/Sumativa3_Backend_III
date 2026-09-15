package cl.duoc.banco.bff.mobile.service;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import cl.duoc.banco.bff.common.dto.backend.CuentaBackendDTO;
import cl.duoc.banco.bff.common.dto.backend.TransaccionBackendDTO;
import cl.duoc.banco.bff.common.security.TokenRelay;
import cl.duoc.banco.bff.mobile.dto.CuentaMobileDetalleDTO;
import cl.duoc.banco.bff.mobile.dto.CuentaMobileResumenDTO;
import cl.duoc.banco.bff.mobile.dto.MovimientoMobileDTO;
import lombok.RequiredArgsConstructor;

/**
 * Logica del canal Mobile: consume backend-core y recorta los datos a lo
 * esencial (reduce consumo de ancho de banda). Solo los ultimos N movimientos.
 */
@Service
@RequiredArgsConstructor
public class BffMobileService {

    private static final int MAX_ULTIMOS_MOVIMIENTOS = 5;

    private final RestClient coreRestClient;

    public List<CuentaMobileResumenDTO> listarCuentas() {
        List<CuentaBackendDTO> cuentas = coreRestClient.get()
                .uri("/api/cuentas")
                .header(HttpHeaders.AUTHORIZATION, bearer())
                .retrieve()
                .body(new ParameterizedTypeReference<List<CuentaBackendDTO>>() {
                });
        return cuentas.stream()
                .map(c -> new CuentaMobileResumenDTO(c.cuentaId(), c.nombre(), c.saldo()))
                .toList();
    }

    public CuentaMobileDetalleDTO obtenerDetalle(Long cuentaId) {
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

        List<MovimientoMobileDTO> ultimos = transacciones.stream()
                .limit(MAX_ULTIMOS_MOVIMIENTOS)
                .map(t -> new MovimientoMobileDTO(t.fecha(), t.monto(), t.tipo()))
                .toList();

        return new CuentaMobileDetalleDTO(cuenta.cuentaId(), cuenta.nombre(), cuenta.saldo(), ultimos);
    }

    private String bearer() {
        String token = TokenRelay.currentTokenValue();
        if (token == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado");
        }
        return "Bearer " + token;
    }
}
