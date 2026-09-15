package cl.duoc.banco.bff.batch.config;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.PassThroughLineMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import cl.duoc.banco.bff.batch.exception.DatoInvalidoException;
import cl.duoc.banco.bff.batch.listener.LimpiarDatosListener;
import cl.duoc.banco.bff.batch.listener.SkipLogListener;
import cl.duoc.banco.bff.batch.model.Cuenta;
import cl.duoc.banco.bff.batch.model.Movimiento;
import cl.duoc.banco.bff.batch.model.Transaccion;
import cl.duoc.banco.bff.batch.processor.CuentaItemProcessor;
import cl.duoc.banco.bff.batch.processor.MovimientoItemProcessor;
import cl.duoc.banco.bff.batch.processor.TransaccionItemProcessor;
import lombok.RequiredArgsConstructor;

/**
 * Configuracion del job batch: lee los CSV legacy (Reader de lineas), los
 * valida/limpia (Processor) y los persiste (Writer JDBC idempotente), aplicando
 * politicas de skip (registros invalidos) y retry (fallas transitorias).
 *
 * <p>La escritura usa {@code INSERT IGNORE}: la deduplicacion se resuelve con la
 * clave unica de cada tabla (sin estado en el procesador).</p>
 */
@Configuration
@RequiredArgsConstructor
public class BatchConfig {

    private static final int CHUNK_SIZE = 50;
    private static final int SKIP_LIMIT = 1_000_000;
    private static final int RETRY_LIMIT = 3;

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final JdbcTemplate jdbcTemplate;

    private final CuentaItemProcessor cuentaItemProcessor;
    private final TransaccionItemProcessor transaccionItemProcessor;
    private final MovimientoItemProcessor movimientoItemProcessor;

    private final SkipLogListener skipLogListener;
    private final LimpiarDatosListener limpiarDatosListener;

    @Value("${batch.data.intereses:data/semana_3/intereses.csv}")
    private String interesesPath;

    @Value("${batch.data.cuentas-anuales:data/semana_3/cuentas_anuales.csv}")
    private String cuentasAnualesPath;

    @Value("${batch.data.transacciones:data/semana_3/transacciones.csv}")
    private String transaccionesPath;

    @Bean
    public Job importarDatosJob() {
        return new JobBuilder("importarDatosJob", jobRepository)
                .listener(limpiarDatosListener)
                .start(cuentasStep())
                .next(transaccionesStep())
                .next(movimientosStep())
                .build();
    }

    @Bean
    public Step cuentasStep() {
        return new StepBuilder("cuentasStep", jobRepository)
                .<String, Cuenta>chunk(CHUNK_SIZE, transactionManager)
                .reader(rawReader(interesesPath))
                .processor(cuentaItemProcessor)
                .writer(cuentaWriter())
                .faultTolerant()
                .skip(DatoInvalidoException.class)
                .skipLimit(SKIP_LIMIT)
                .retry(TransientDataAccessException.class)
                .retryLimit(RETRY_LIMIT)
                .listener(skipLogListener)
                .build();
    }

    @Bean
    public Step transaccionesStep() {
        return new StepBuilder("transaccionesStep", jobRepository)
                .<String, Transaccion>chunk(CHUNK_SIZE, transactionManager)
                .reader(rawReader(cuentasAnualesPath))
                .processor(transaccionItemProcessor)
                .writer(transaccionWriter())
                .faultTolerant()
                .skip(DatoInvalidoException.class)
                .skipLimit(SKIP_LIMIT)
                .retry(TransientDataAccessException.class)
                .retryLimit(RETRY_LIMIT)
                .listener(skipLogListener)
                .build();
    }

    @Bean
    public Step movimientosStep() {
        return new StepBuilder("movimientosStep", jobRepository)
                .<String, Movimiento>chunk(CHUNK_SIZE, transactionManager)
                .reader(rawReader(transaccionesPath))
                .processor(movimientoItemProcessor)
                .writer(movimientoWriter())
                .faultTolerant()
                .skip(DatoInvalidoException.class)
                .skipLimit(SKIP_LIMIT)
                .retry(TransientDataAccessException.class)
                .retryLimit(RETRY_LIMIT)
                .listener(skipLogListener)
                .build();
    }

    public ItemWriter<Cuenta> cuentaWriter() {
        return items -> {
            List<Cuenta> list = new ArrayList<>();
            for (Cuenta c : items) {
                list.add(c);
            }
            jdbcTemplate.batchUpdate(
                    "INSERT IGNORE INTO cuenta (cuenta_id, nombre, saldo, edad, tipo) VALUES (?, ?, ?, ?, ?)",
                    list, list.size(), (ps, c) -> {
                        ps.setLong(1, c.getCuentaId());
                        ps.setString(2, c.getNombre());
                        ps.setBigDecimal(3, c.getSaldo());
                        ps.setInt(4, c.getEdad());
                        ps.setString(5, c.getTipo());
                    });
        };
    }

    public ItemWriter<Transaccion> transaccionWriter() {
        return items -> {
            List<Transaccion> list = new ArrayList<>();
            for (Transaccion t : items) {
                list.add(t);
            }
            jdbcTemplate.batchUpdate(
                    "INSERT INTO transaccion (cuenta_id, fecha, tipo, monto, descripcion) VALUES (?, ?, ?, ?, ?)",
                    list, list.size(), (ps, t) -> {
                        ps.setLong(1, t.getCuentaId());
                        ps.setDate(2, Date.valueOf(t.getFecha()));
                        ps.setString(3, t.getTipo());
                        ps.setBigDecimal(4, t.getMonto());
                        ps.setString(5, t.getDescripcion());
                    });
        };
    }

    public ItemWriter<Movimiento> movimientoWriter() {
        return items -> {
            List<Movimiento> list = new ArrayList<>();
            for (Movimiento m : items) {
                list.add(m);
            }
            jdbcTemplate.batchUpdate(
                    "INSERT IGNORE INTO movimiento (id, fecha, monto, tipo) VALUES (?, ?, ?, ?)",
                    list, list.size(), (ps, m) -> {
                        ps.setLong(1, m.getId());
                        ps.setDate(2, Date.valueOf(m.getFecha()));
                        ps.setBigDecimal(3, m.getMonto());
                        ps.setString(4, m.getTipo());
                    });
        };
    }

    /** Lector de lineas crudas (sin parseo); el procesador se encarga de validar/limpiar. */
    private ItemReader<String> rawReader(String classpathFile) {
        FlatFileItemReader<String> reader = new FlatFileItemReader<>();
        reader.setResource(new ClassPathResource(classpathFile));
        reader.setLineMapper(new PassThroughLineMapper());
        reader.setLinesToSkip(1);
        return reader;
    }
}
