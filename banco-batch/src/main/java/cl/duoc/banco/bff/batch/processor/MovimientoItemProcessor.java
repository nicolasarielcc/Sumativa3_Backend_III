package cl.duoc.banco.bff.batch.processor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import cl.duoc.banco.bff.batch.exception.DatoInvalidoException;
import cl.duoc.banco.bff.batch.model.Movimiento;
import cl.duoc.banco.bff.batch.util.ParseoUtil;

/**
 * Procesa lineas de transacciones.csv ({@code id,fecha,monto,tipo}).
 * Omite montos negativos/cero y tipos invalidos. Sin estado (dedup en BD).
 */
@Component
public class MovimientoItemProcessor implements ItemProcessor<String, Movimiento> {

    private static final Set<String> TIPOS_VALIDOS = Set.of("debito", "credito");

    @Override
    public Movimiento process(String line) {
        String[] c = ParseoUtil.split(line);
        if (c.length < 4) {
            throw new DatoInvalidoException("Columnas insuficientes: " + line);
        }

        Long id = ParseoUtil.parseLong(c[0]);
        if (id == null || id <= 0) {
            throw new DatoInvalidoException("id invalido: " + line);
        }

        LocalDate fecha = ParseoUtil.parseFecha(c[1]);
        if (fecha == null) {
            throw new DatoInvalidoException("fecha invalida: " + line);
        }

        BigDecimal monto = ParseoUtil.parseDecimal(c[2]);
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DatoInvalidoException("monto vacio, cero o negativo: " + line);
        }

        String tipo = ParseoUtil.normalize(c[3]);
        if (!TIPOS_VALIDOS.contains(tipo)) {
            throw new DatoInvalidoException("tipo de movimiento invalido: " + line);
        }

        return new Movimiento(id, fecha, monto, tipo);
    }
}
