package domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PropriedadeTest {

    @Test
    void cadastroValidoRemoveEspacosExtras() {
        Propriedade p = new Propriedade("  Fazenda Modelo ", " Contagem ");
        assertEquals("Fazenda Modelo", p.getNome());
        assertEquals("Contagem", p.getMunicipio());
        assertEquals(0, p.getId());
        assertTrue(p.toString().contains("Contagem"));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   "})
    void nomeVazioOuSoEspacosEhRejeitado(String nome) {
        ValidacaoException e = assertThrows(ValidacaoException.class, () -> new Propriedade(nome, "Contagem"));
        assertTrue(e.getMessage().contains("Nome da propriedade"));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "  "})
    void municipioVazioEhRejeitado(String municipio) {
        ValidacaoException e = assertThrows(ValidacaoException.class, () -> new Propriedade("Fazenda", municipio));
        assertTrue(e.getMessage().contains("Município"));
    }

    @Test
    void alterarAplicaAsMesmasValidacoes() {
        Propriedade p = new Propriedade(1, "A", "B");
        p.alterar("C", "D");
        assertEquals("C", p.getNome());
        assertThrows(ValidacaoException.class, () -> p.alterar(" ", "D"));
        assertEquals("C", p.getNome());
    }

    @Test
    void igualdadePorIdentificador() {
        assertEquals(new Propriedade(5, "A", "B"), new Propriedade(5, "X", "Y"));
        assertNotEquals(new Propriedade(5, "A", "B"), new Propriedade(6, "A", "B"));
        assertEquals(new Propriedade(5, "A", "B").hashCode(), new Propriedade(5, "Z", "Z").hashCode());
    }
}
