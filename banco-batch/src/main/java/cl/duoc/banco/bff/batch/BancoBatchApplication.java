package cl.duoc.banco.bff.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Proceso batch: lee los CSV legacy de bank_legacy_data, los limpia
 * (validacion + deduplicacion) y los persiste en MySQL usando Spring Batch
 * con politicas de retry y skip.
 */
@SpringBootApplication
public class BancoBatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(BancoBatchApplication.class, args);
    }
}
