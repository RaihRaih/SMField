package domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TalhaoTest {

    @Test
    void cadastroValido() {
        Talhao t = new Talhao(1, "Talhão A", 12.0, "Milho");
        assertEquals(1, t.getPropriedadeId());
        assertEquals("Talhão A", t.getNome());
        assertEquals(12.0, t.getAreaHectares());
        assertEquals("Milho", t.getCultura());
        assertEquals("Talhão A (Milho)", t.toString());
    }

    @Test
    void talhaoSemPropriedadeEhRejeitado() {
        assertThrows(ValidacaoException.class, () -> new Talhao(0, "A", 1.0, "Soja"));
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0, -1.0, -0.01})
    void areaZeroOuNegativaEhRejeitada(double area) {
        ValidacaoException e = assertThrows(ValidacaoException.class, () -> new Talhao(1, "A", area, "Soja"));
        assertTrue(e.getMessage().contains("maior que zero"));
    }

    @Test
    void areaAusenteNaNOuInfinitaEhRejeitada() {
        assertThrows(ValidacaoException.class, () -> new Talhao(1, "A", null, "Soja"));
        assertThrows(ValidacaoException.class, () -> new Talhao(1, "A", Double.NaN, "Soja"));
        assertThrows(ValidacaoException.class, () -> new Talhao(1, "A", Double.POSITIVE_INFINITY, "Soja"));
    }

    @Test
    void culturaENomeObrigatorios() {
        assertThrows(ValidacaoException.class, () -> new Talhao(1, "A", 1.0, " "));
        assertThrows(ValidacaoException.class, () -> new Talhao(1, "", 1.0, "Soja"));
    }

    @Test
    void alterarValidaEMantemPropriedade() {
        Talhao t = new Talhao(3, 1, "A", 1.0, "Soja");
        t.alterar("B", 2.5, "Café");
        assertEquals("B", t.getNome());
        assertEquals(2.5, t.getAreaHectares());
        assertEquals("Café", t.getCultura());
        assertEquals(1, t.getPropriedadeId());
        assertThrows(ValidacaoException.class, () -> t.alterar("B", 0.0, "Café"));
        assertEquals(2.5, t.getAreaHectares());
        assertEquals(new Talhao(3, 9, "X", 1.0, "Y"), t);
        assertNotEquals(new Talhao(4, 1, "A", 1.0, "Soja"), t);
        assertEquals(new Talhao(3, 9, "X", 1.0, "Y").hashCode(), t.hashCode());
    }
}
