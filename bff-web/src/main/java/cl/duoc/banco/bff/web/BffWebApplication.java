package cl.duoc.banco.bff.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * BFF Web: backend especializado para navegadores. Expone datos completos y
 * resumenes. Consume backend-core por HTTPS con token JWT (relay).
 */
@SpringBootApplication(scanBasePackages = "cl.duoc.banco.bff")
public class BffWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(BffWebApplication.class, args);
    }
}
