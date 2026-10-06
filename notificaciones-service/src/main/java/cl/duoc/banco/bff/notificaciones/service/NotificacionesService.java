package cl.duoc.banco.bff.notificaciones.service;

import java.util.List;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import cl.duoc.banco.bff.notificaciones.config.KafkaConsumerConfig;
import cl.duoc.banco.bff.notificaciones.dto.RetiroEvent;

/**
 * Consumidor asincrono de eventos de retiro. Acumula las notificaciones en
 * memoria para poder consultarlas via REST (evidencia de la mensajeria).
 */
@Service
public class NotificacionesService {

    private final java.util.concurrent.CopyOnWriteArrayList<RetiroEvent> recibidos =
            new java.util.concurrent.CopyOnWriteArrayList<>();

    @KafkaListener(id = "retiroListener", topics = KafkaConsumerConfig.TOPIC,
            groupId = KafkaConsumerConfig.CONSUMER_GROUP_ID)
    public void consumirRetiro(RetiroEvent evento, Acknowledgment ack) {
        try {
            recibidos.add(evento);
            System.out.println("Evento consumido: " + evento.toString());
            ack.acknowledge();
        } catch (Exception e) {
            System.out.println("ERROR al consumir evento");
            e.printStackTrace();
        }
    }

    public List<RetiroEvent> listarRecibidos() {
        return List.copyOf(recibidos);
    }
}
