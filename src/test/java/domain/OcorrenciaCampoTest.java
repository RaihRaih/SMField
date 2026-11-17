package domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OcorrenciaCampoTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 11, 17);

    @Test
    void ocorrenciaValida() {
        OcorrenciaCampo o = new OcorrenciaCampo(2, TipoOcorrencia.PRAGA, " Lagartas ", HOJE, HOJE);
        assertEquals(2, o.getTalhaoId());
        assertEquals(TipoOcorrencia.PRAGA, o.getTipo());
        assertEquals("Lagartas", o.getDescricao());
        assertEquals(HOJE, o.getData());
        assertEquals("Praga", o.getTipo().toString());
    }

    @Test
    void validacoes() {
        assertThrows(ValidacaoException.class, () -> new OcorrenciaCampo(0, TipoOcorrencia.PRAGA, "x", HOJE, HOJE));
        assertThrows(ValidacaoException.class, () -> new OcorrenciaCampo(1, null, "x", HOJE, HOJE));
        assertThrows(ValidacaoException.class, () -> new OcorrenciaCampo(1, TipoOcorrencia.DOENCA, " ", HOJE, HOJE));
        assertThrows(ValidacaoException.class, () -> new OcorrenciaCampo(1, TipoOcorrencia.DOENCA, "x", null, HOJE));
        assertThrows(ValidacaoException.class,
                () -> new OcorrenciaCampo(1, TipoOcorrencia.DOENCA, "x", HOJE.plusDays(1), HOJE));
    }
}
