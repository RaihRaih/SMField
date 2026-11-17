package service;

import domain.ValidacaoException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.regex.Pattern;

/**
 * Converte textos digitados pelo usuário em valores, com mensagens de erro claras (RNF-07).
 * Fica na camada de serviço para que a interface não contenha regras de validação.
 */
public final class ConversorEntrada {

    public static final DateTimeFormatter FORMATO_DATA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm").withResolverStyle(ResolverStyle.STRICT);
    public static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    private static final Pattern NUMERO = Pattern.compile("[+-]?\\d+(\\.\\d+)?");

    private ConversorEntrada() {
    }

    /**
     * Converte um número aceitando vírgula ou ponto como separador decimal.
     * Texto vazio resulta em {@code null} (campo não preenchido).
     * Letras, "NaN" e "Infinity" são rejeitados.
     */
    public static Double numeroOpcional(String texto, String campo) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        String normalizado = texto.trim().replace(',', '.');
        if (!NUMERO.matcher(normalizado).matches()) {
            throw new ValidacaoException("O campo \"" + campo + "\" deve conter apenas números (ex.: 12,5). Valor informado: \""
                    + texto.trim() + "\".");
        }
        return Double.parseDouble(normalizado);
    }

    /** Igual a {@link #numeroOpcional}, mas exige preenchimento. */
    public static double numeroObrigatorio(String texto, String campo) {
        Double valor = numeroOpcional(texto, campo);
        if (valor == null) {
            throw new ValidacaoException("O campo \"" + campo + "\" é obrigatório.");
        }
        return valor;
    }

    /** Converte "dd/MM/aaaa HH:mm". */
    public static LocalDateTime dataHora(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new ValidacaoException("O campo \"Data e hora\" é obrigatório.");
        }
        try {
            return LocalDateTime.parse(texto.trim(), FORMATO_DATA_HORA);
        } catch (DateTimeParseException e) {
            throw new ValidacaoException("Data e hora inválidas. Use o formato dd/mm/aaaa hh:mm (ex.: 17/11/2026 08:30).");
        }
    }

    /** Converte "dd/MM/aaaa". */
    public static LocalDate data(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new ValidacaoException("O campo \"Data\" é obrigatório.");
        }
        try {
            return LocalDate.parse(texto.trim(), FORMATO_DATA);
        } catch (DateTimeParseException e) {
            throw new ValidacaoException("Data inválida. Use o formato dd/mm/aaaa (ex.: 17/11/2026).");
        }
    }
}
