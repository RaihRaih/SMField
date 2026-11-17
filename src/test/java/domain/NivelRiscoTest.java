package domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NivelRiscoTest {

    @ParameterizedTest(name = "índice {0} -> {1}")
    @CsvSource({"0,BAIXO", "33,BAIXO", "34,MEDIO", "66,MEDIO", "67,ALTO", "100,ALTO"})
    void classificaNosLimites(int indice, NivelRisco esperado) {
        assertEquals(esperado, NivelRisco.classificar(indice));
    }

    @Test
    void indiceForaDoIntervaloEhRejeitado() {
        assertThrows(ValidacaoException.class, () -> NivelRisco.classificar(-1));
        assertThrows(ValidacaoException.class, () -> NivelRisco.classificar(101));
    }

    @Test
    void naoCalculadoEhSeparadoDosTresNiveis() {
        for (int i = 0; i <= 100; i++) {
            assertTrue(NivelRisco.classificar(i).isCalculado());
        }
        assertFalse(NivelRisco.NAO_CALCULADO.isCalculado());
        assertEquals("Não calculado", NivelRisco.NAO_CALCULADO.toString());
        assertEquals(0, NivelRisco.NAO_CALCULADO.gravidade());
        assertTrue(NivelRisco.ALTO.gravidade() > NivelRisco.MEDIO.gravidade());
        assertTrue(NivelRisco.MEDIO.gravidade() > NivelRisco.BAIXO.gravidade());
        assertEquals(34, NivelRisco.MEDIO.getMinimo());
        assertEquals(66, NivelRisco.MEDIO.getMaximo());
    }

    @Test
    void resultadoNaoCalculadoNaoTemIndice() {
        ResultadoRisco r = ResultadoRisco.naoCalculado("faltam dados");
        assertFalse(r.isCalculado());
        assertNull(r.getIndice());
        assertEquals(NivelRisco.NAO_CALCULADO, r.getNivel());
        assertEquals("faltam dados", r.getJustificativa());
        assertTrue(r.getFatores().isEmpty());
    }

    @Test
    void resultadoCalculadoCorrespondeAoIndice() {
        ResultadoRisco r = ResultadoRisco.calculado(67, List.of(), "x");
        assertEquals(NivelRisco.ALTO, r.getNivel());
        assertThrows(ValidacaoException.class, () -> ResultadoRisco.calculado(120, List.of(), "x"));
    }
}
