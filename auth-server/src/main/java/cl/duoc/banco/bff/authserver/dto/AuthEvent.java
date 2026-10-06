package cl.duoc.banco.bff.authserver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Evento de autenticacion publicado en el topic "autenticaciones" cada vez
 * que el Authorization Server emite un access token (client_credentials).
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class AuthEvent {

    private String cliente;
    private String canal;
    private String fecha;
}
