package cl.duoc.banco.bff.batch.controller;

import java.util.Map;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/**
 * Punto de entrada para disparar el proceso batch de carga/limpieza de datos.
 */
@RestController
@RequestMapping("/api/batch")
@RequiredArgsConstructor
public class BatchController {

    private final JobLauncher jobLauncher;
    private final Job importarDatosJob;

    @GetMapping("/procesar")
    public Map<String, Object> procesar() throws Exception {
        JobParameters parameters = new JobParametersBuilder()
                .addLong("ejecucion", System.currentTimeMillis())
                .toJobParameters();

        JobExecution execution = jobLauncher.run(importarDatosJob, parameters);

        long leidos = execution.getStepExecutions().stream().mapToLong(se -> se.getReadCount()).sum();
        long escritos = execution.getStepExecutions().stream().mapToLong(se -> se.getWriteCount()).sum();
        long omitidos = execution.getStepExecutions().stream().mapToLong(se -> se.getSkipCount()).sum();

        return Map.of(
                "jobId", execution.getJobId(),
                "estado", execution.getStatus().name(),
                "leidos", leidos,
                "escritos", escritos,
                "omitidos", omitidos);
    }
}
