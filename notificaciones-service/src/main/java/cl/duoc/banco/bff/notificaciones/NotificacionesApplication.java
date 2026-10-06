package cl.duoc.banco.bff.notificaciones;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Servicio de notificaciones: consumidor asincrono de eventos Kafka.
 * Recibe los eventos {@code retiro.realizado} emitidos por backend-core y
 * los procesa (registro en memoria + log), habilitando una arquitectura
 * orientada a eventos.
 */
@SpringBootApplication
public class NotificacionesApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificacionesApplication.class, args);
    }
}
