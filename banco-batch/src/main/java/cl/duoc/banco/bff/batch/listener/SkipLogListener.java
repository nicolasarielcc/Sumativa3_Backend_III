package cl.duoc.banco.bff.batch.listener;

import org.springframework.batch.core.SkipListener;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Registra (log) cada registro omitido por las politicas de skip.
 */
@Slf4j
@Component
public class SkipLogListener implements SkipListener<String, Object> {

    @Override
    public void onSkipInRead(Throwable t) {
        log.warn("[SKIP] registro omitido en lectura: {}", t.getMessage());
    }

    @Override
    public void onSkipInProcess(String item, Throwable t) {
        log.warn("[SKIP] registro omitido en procesamiento: {} => {}", t.getMessage(), item);
    }

    @Override
    public void onSkipInWrite(Object item, Throwable t) {
        log.warn("[SKIP] registro omitido en escritura: {} => {}", t.getMessage(), item);
    }
}
