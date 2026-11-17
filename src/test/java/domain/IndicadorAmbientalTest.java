package domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IndicadorAmbientalTest {

    private static final LocalDateTime DATA = LocalDateTime.of(2026, 11, 17, 8, 30, 45);

    private IndicadorAmbiental criar(Double solo, Double temp, Double ar, Double prec) {
        return new IndicadorAmbiental(1, solo, temp, ar, prec, DATA);
    }

    @Test
    void dadosValidos() {
        IndicadorAmbiental i = criar(55.0, 24.0, 65.0, 12.0);
        assertEquals(55.0, i.getUmidadeSolo());
        assertEquals(24.0, i.getTemperatura());
        assertEquals(65.0, i.getUmidadeAr());
        assertEquals(12.0, i.getPrecipitacao24h());
        assertEquals(1, i.getTalhaoId());
        assertEquals(LocalDateTime.of(2026, 11, 17, 8, 30), i.getDataHora(), "segundos são descartados");
    }

    @Test
    void limitesAceitos() {
        criar(0.0, -20.0, 0.0, 0.0);
        criar(100.0, 60.0, 100.0, 500.0);
    }

    @Test
    void umidadeNegativaOuAcimaDe100EhRejeitada() {
        assertThrows(ValidacaoException.class, () -> criar(-0.1, 24.0, 65.0, 12.0));
        assertThrows(ValidacaoException.class, () -> criar(100.1, 24.0, 65.0, 12.0));
        assertThrows(ValidacaoException.class, () -> criar(50.0, 24.0, -1.0, 12.0));
        ValidacaoException e = assertThrows(ValidacaoException.class, () -> criar(50.0, 24.0, 150.0, 12.0));
        assertTrue(e.getMessage().contains("Umidade do ar"));
    }

    @Test
    void precipitacaoNegativaEhRejeitada() {
        assertThrows(ValidacaoException.class, () -> criar(50.0, 24.0, 65.0, -0.5));
    }

    @Test
    void temperaturaForaDaFaixaOperacionalEhRejeitada() {
        assertThrows(ValidacaoException.class, () -> criar(50.0, -20.1, 65.0, 12.0));
        assertThrows(ValidacaoException.class, () -> criar(50.0, 60.1, 65.0, 12.0));
    }

    @Test
    void valoresAusentesNaNOuInfinitosSaoRejeitados() {
        assertThrows(ValidacaoException.class, () -> criar(null, 24.0, 65.0, 12.0));
        assertThrows(ValidacaoException.class, () -> criar(50.0, Double.NaN, 65.0, 12.0));
        assertThrows(ValidacaoException.class, () -> criar(50.0, 24.0, Double.NEGATIVE_INFINITY, 12.0));
    }

    @Test
    void talhaoEDataObrigatorios() {
        assertThrows(ValidacaoException.class, () -> new IndicadorAmbiental(0, 50.0, 24.0, 65.0, 12.0, DATA));
        assertThrows(ValidacaoException.class, () -> new IndicadorAmbiental(1, 50.0, 24.0, 65.0, 12.0, null));
    }
}
