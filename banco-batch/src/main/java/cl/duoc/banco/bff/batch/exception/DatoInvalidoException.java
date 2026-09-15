package cl.duoc.banco.bff.batch.exception;

/**
 * Registro invalido (dato sucio) que debe ser omitido (skip) por el proceso batch.
 */
public class DatoInvalidoException extends RuntimeException {

    public DatoInvalidoException(String message) {
        super(message);
    }
}
