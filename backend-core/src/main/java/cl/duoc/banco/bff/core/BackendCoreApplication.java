package cl.duoc.banco.bff.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Backend Core: expone la logica de negocio y el acceso a datos (JPA/MySQL).
 * Escanea el paquete base {@code cl.duoc.banco.bff} para recoger los beans
 * de seguridad/JWT compartidos del modulo common.
 */
@SpringBootApplication(scanBasePackages = "cl.duoc.banco.bff")
public class BackendCoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendCoreApplication.class, args);
    }
}
