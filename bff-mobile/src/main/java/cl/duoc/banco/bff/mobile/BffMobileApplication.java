package cl.duoc.banco.bff.mobile;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * BFF Mobile: backend especializado para dispositivos moviles. Entrega
 * respuestas ligeras (datos esenciales + ultimos movimientos).
 */
@SpringBootApplication(scanBasePackages = "cl.duoc.banco.bff")
public class BffMobileApplication {

    public static void main(String[] args) {
        SpringApplication.run(BffMobileApplication.class, args);
    }
}
