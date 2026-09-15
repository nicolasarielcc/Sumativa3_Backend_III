package cl.duoc.banco.bff.batch.listener;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Antes de cada ejecucion del job, limpia los datos previos (hace el batch
 * idempotente: re-ejecutar el job reproduce el mismo estado).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LimpiarDatosListener implements JobExecutionListener {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        jdbcTemplate.update("DELETE FROM transaccion");
        jdbcTemplate.update("DELETE FROM movimiento");
        jdbcTemplate.update("DELETE FROM cuenta");
        log.info("Datos previos eliminados");
    }
}
