package cl.duoc.banco.bff.batch.processor;

import java.math.BigDecimal;
import java.util.Set;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import cl.duoc.banco.bff.batch.exception.DatoInvalidoException;
import cl.duoc.banco.bff.batch.model.Cuenta;
import cl.duoc.banco.bff.batch.util.ParseoUtil;

/**
 * Procesa lineas de intereses.csv ({@code cuenta_id,nombre,saldo,edad,tipo}).
 * Valida y limpia cada registro; los invalidos lanzan {@link DatoInvalidoException}
 * (se omiten via skip). No mantiene estado: la deduplicacion se resuelve a nivel
 * de base de datos (clave unica).
 */
@Component
public class CuentaItemProcessor implements ItemProcessor<String, Cuenta> {

    private static final Set<String> TIPOS_VALIDOS = Set.of("ahorro", "prestamo", "hipoteca");
    private static final int EDAD_MINIMA = 0;
    private static final int EDAD_MAXIMA = 120;

    @Override
    public Cuenta process(String line) {
        String[] c = ParseoUtil.split(line);
        if (c.length < 5) {
            throw new DatoInvalidoException("Columnas insuficientes: " + line);
        }

        Long cuentaId = ParseoUtil.parseLong(c[0]);
        if (cuentaId == null || cuentaId <= 0) {
            throw new DatoInvalidoException("cuenta_id invalido: " + line);
        }

        String nombre = ParseoUtil.clean(c[1]);
        if (nombre.isEmpty() || "unknown".equalsIgnoreCase(nombre)) {
            throw new DatoInvalidoException("nombre vacio o desconocido: " + line);
        }

        BigDecimal saldo = ParseoUtil.parseDecimal(c[2]);
        if (saldo == null || saldo.compareTo(BigDecimal.ZERO) < 0) {
            throw new DatoInvalidoException("saldo vacio o negativo: " + line);
        }

        Integer edad = ParseoUtil.parseInt(c[3]);
        if (edad == null || edad < EDAD_MINIMA || edad > EDAD_MAXIMA) {
            throw new DatoInvalidoException("edad fuera de rango: " + line);
        }

        String tipo = ParseoUtil.normalize(c[4]);
        if (!TIPOS_VALIDOS.contains(tipo)) {
            throw new DatoInvalidoException("tipo de cuenta invalido: " + line);
        }

        return new Cuenta(cuentaId, nombre, saldo, edad, tipo);
    }
}
