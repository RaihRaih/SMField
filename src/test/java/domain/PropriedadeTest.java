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
        Propriedade p = new Propriedade(1, "Fazenda Boa Vista", "Sorriso/MT");
        p.alterar("Sítio Primavera", "Patrocínio/MG");
        assertEquals("Sítio Primavera", p.getNome());
        assertEquals("Patrocínio/MG", p.getMunicipio());
        assertThrows(ValidacaoException.class, () -> p.alterar(" ", "Patrocínio/MG"));
        assertEquals("Sítio Primavera", p.getNome());
    }

    @Test
    void igualdadePorIdentificador() {
        assertEquals(new Propriedade(5, "Fazenda Boa Vista", "Sorriso/MT"), new Propriedade(5, "Fazenda Santa Clara", "Rio Verde/GO"));
        assertNotEquals(new Propriedade(5, "Fazenda Boa Vista", "Sorriso/MT"), new Propriedade(6, "Fazenda Boa Vista", "Sorriso/MT"));
        assertEquals(new Propriedade(5, "Fazenda Boa Vista", "Sorriso/MT").hashCode(),
                new Propriedade(5, "Sítio Primavera", "Patrocínio/MG").hashCode());
    }
}
