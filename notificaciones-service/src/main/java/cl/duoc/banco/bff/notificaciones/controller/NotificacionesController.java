package cl.duoc.banco.bff.notificaciones.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.banco.bff.notificaciones.dto.RetiroEvent;
import cl.duoc.banco.bff.notificaciones.service.NotificacionesService;
import lombok.RequiredArgsConstructor;

/**
 * Expone los eventos de retiro consumidos desde Kafka (evidencia del flujo
 * asincrono productor -> consumidor).
 */
@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
public class NotificacionesController {

    private final NotificacionesService notificacionesService;

    @GetMapping
    public List<RetiroEvent> listarNotificaciones() {
        return notificacionesService.listarRecibidos();
    }
}
