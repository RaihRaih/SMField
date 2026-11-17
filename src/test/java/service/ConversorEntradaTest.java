package service;

import domain.ValidacaoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConversorEntradaTest {

    @Test
    void aceitaVirgulaEPonto() {
        assertEquals(12.5, ConversorEntrada.numeroOpcional("12,5", "x"));
        assertEquals(12.5, ConversorEntrada.numeroOpcional(" 12.5 ", "x"));
        assertEquals(-3.0, ConversorEntrada.numeroOpcional("-3", "x"));
        assertEquals(7.0, ConversorEntrada.numeroObrigatorio("7", "x"));
    }

    @Test
    void vazioEhNuloNoOpcionalEErroNoObrigatorio() {
        assertNull(ConversorEntrada.numeroOpcional("  ", "x"));
        assertNull(ConversorEntrada.numeroOpcional(null, "x"));
        assertThrows(ValidacaoException.class, () -> ConversorEntrada.numeroObrigatorio("", "Área"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "12a", "NaN", "Infinity", "-Infinity", "1e3", "1,2,3", "--1"})
    void rejeitaLetrasNaNEInfinito(String texto) {
        assertThrows(ValidacaoException.class, () -> ConversorEntrada.numeroOpcional(texto, "Temperatura"));
    }

    @Test
    void converteDatas() {
        assertEquals(LocalDateTime.of(2026, 11, 17, 8, 30), ConversorEntrada.dataHora("17/11/2026 08:30"));
        assertEquals(LocalDate.of(2026, 2, 28), ConversorEntrada.data("28/02/2026"));
        assertThrows(ValidacaoException.class, () -> ConversorEntrada.dataHora("31/02/2026 08:00"));
        assertThrows(ValidacaoException.class, () -> ConversorEntrada.dataHora(""));
        assertThrows(ValidacaoException.class, () -> ConversorEntrada.data("2026-11-17"));
        assertThrows(ValidacaoException.class, () -> ConversorEntrada.data(null));
    }
}
