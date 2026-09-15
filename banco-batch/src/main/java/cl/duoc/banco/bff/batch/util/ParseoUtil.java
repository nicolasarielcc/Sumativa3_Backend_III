package cl.duoc.banco.bff.batch.util;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Utilidades de parseo y limpieza de datos legacy.
 */
public final class ParseoUtil {

    private static final List<DateTimeFormatter> FORMATOS_FECHA = List.of(
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy")
    );

    private ParseoUtil() {
    }

    /** Divide una linea CSV respetando campos vacios (no descarta trailing empties). */
    public static String[] split(String line) {
        return line.split(",", -1);
    }

    public static String clean(String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * Normaliza texto: elimina acentos y pasa a minusculas.
     * Ej. "Depósito" -> "deposito".
     */
    public static String normalize(String value) {
        String cleaned = clean(value).toLowerCase();
        return Normalizer.normalize(cleaned, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
    }

    public static Long parseLong(String value) {
        try {
            return Long.parseLong(clean(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Integer parseInt(String value) {
        try {
            return Integer.parseInt(clean(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static BigDecimal parseDecimal(String value) {
        try {
            return new BigDecimal(clean(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Parsea fechas con varios formatos (los CSV legacy mezclan formatos). */
    public static LocalDate parseFecha(String value) {
        String cleaned = clean(value);
        for (DateTimeFormatter formatter : FORMATOS_FECHA) {
            try {
                return LocalDate.parse(cleaned, formatter);
            } catch (DateTimeParseException ignored) {
                // intentar con el siguiente formato
            }
        }
        return null;
    }
}
