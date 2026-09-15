package cl.duoc.banco.bff.atm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * BFF ATM: backend especializado para cajeros automaticos. Solo expone
 * operaciones criticas: consulta de saldo y retiro.
 */
@SpringBootApplication(scanBasePackages = "cl.duoc.banco.bff")
public class BffAtmApplication {

    public static void main(String[] args) {
        SpringApplication.run(BffAtmApplication.class, args);
    }
}
