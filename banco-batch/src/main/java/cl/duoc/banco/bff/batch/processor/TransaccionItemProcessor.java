package cl.duoc.banco.bff.batch.processor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import cl.duoc.banco.bff.batch.exception.DatoInvalidoException;
import cl.duoc.banco.bff.batch.model.Transaccion;
import cl.duoc.banco.bff.batch.util.ParseoUtil;

/**
 * Procesa lineas de cuentas_anuales.csv
 * ({@code cuenta_id,fecha,transaccion,monto,descripcion}).
 * Normaliza fechas (multi-formato) y montos (signo implicito en el tipo),
 * y usa valor por defecto para descripciones vacias.
 */
@Component
public class TransaccionItemProcessor implements ItemProcessor<String, Transaccion> {

    private static final Set<String> TIPOS_VALIDOS = Set.of("deposito", "retiro", "compra", "pago");
    private static final String DESCRIPCION_POR_DEFECTO = "Sin descripción";

    @Override
    public Transaccion process(String line) {
        String[] c = ParseoUtil.split(line);
        if (c.length < 5) {
            throw new DatoInvalidoException("Columnas insuficientes: " + line);
        }

        Long cuentaId = ParseoUtil.parseLong(c[0]);
        if (cuentaId == null || cuentaId <= 0) {
            throw new DatoInvalidoException("cuenta_id invalido: " + line);
        }

        LocalDate fecha = ParseoUtil.parseFecha(c[1]);
        if (fecha == null) {
            throw new DatoInvalidoException("fecha invalida: " + line);
        }

        String tipo = ParseoUtil.normalize(c[2]);
        if (!TIPOS_VALIDOS.contains(tipo)) {
            throw new DatoInvalidoException("tipo de transaccion invalido: " + line);
        }

        BigDecimal monto = ParseoUtil.parseDecimal(c[3]);
        if (monto == null || monto.compareTo(BigDecimal.ZERO) == 0) {
            throw new DatoInvalidoException("monto vacio o cero: " + line);
        }
        monto = monto.abs();

        String descripcion = ParseoUtil.clean(c[4]);
        if (descripcion.isEmpty()) {
            descripcion = DESCRIPCION_POR_DEFECTO;
        }

        return new Transaccion(null, cuentaId, fecha, tipo, monto, descripcion);
    }
}
