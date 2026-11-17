package service;

import domain.FatorRisco;
import domain.IndicadorAmbiental;
import domain.NivelRisco;
import domain.ResultadoRisco;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RiscoServiceTest {

    private final RiscoService risco = new RiscoService();

    @Test
    void todosOsIndicadoresNaFaixaIdealResultamEmZero() {
        ResultadoRisco r = risco.calcular(55.0, 24.0, 65.0, 12.0);
        assertEquals(0, r.getIndice());
        assertEquals(NivelRisco.BAIXO, r.getNivel());
        assertTrue(r.getJustificativa().contains("não foram identificados desvios"));
        assertEquals(4, r.getFatores().size());
        assertTrue(r.getFatores().stream().noneMatch(FatorRisco::isDesvio));
    }

    @Test
    void todosEmFaixaIntermediariaResultamEmCinquenta() {
        ResultadoRisco r = risco.calcular(30.0, 33.0, 30.0, 2.0);
        assertEquals(50, r.getIndice());
        assertEquals(NivelRisco.MEDIO, r.getNivel());
    }

    @Test
    void todosEmFaixaCriticaResultamEmCem() {
        ResultadoRisco r = risco.calcular(10.0, 45.0, 10.0, 150.0);
        assertEquals(100, r.getIndice());
        assertEquals(NivelRisco.ALTO, r.getNivel());
        assertTrue(r.getJustificativa().startsWith("Risco alto (índice 100) identificado devido a:"));
    }

    @Test
    void arredondaParaOInteiroMaisProximo() {
        // apenas solo intermediário: 50 x 0,35 = 17,5 -> 18
        assertEquals(18, risco.calcular(30.0, 24.0, 65.0, 12.0).getIndice());
        // solo + temperatura intermediários: 17,5 + 15 = 32,5 -> 33 (ainda BAIXO)
        ResultadoRisco r = risco.calcular(30.0, 33.0, 65.0, 12.0);
        assertEquals(33, r.getIndice());
        assertEquals(NivelRisco.BAIXO, r.getNivel());
        // temperatura crítica + umidade do ar intermediária: 30 + 7,5 = 37,5 -> 38
        assertEquals(38, risco.calcular(55.0, 40.0, 30.0, 12.0).getIndice());
    }

    @Test
    void cenarioDeRiscoAltoComJustificativaDosFatores() {
        // solo crítico (35) + temperatura crítica (30) + ar intermediário (7,5) + precipitação intermediária (10) = 82,5 -> 83
        ResultadoRisco r = risco.calcular(20.0, 39.0, 30.0, 0.0);
        assertEquals(83, r.getIndice());
        assertEquals(NivelRisco.ALTO, r.getNivel());
        String j = r.getJustificativa();
        assertTrue(j.contains("umidade do solo muito abaixo da faixa de referência"), j);
        assertTrue(j.contains("temperatura muito acima da faixa de referência"), j);
        assertTrue(j.contains("umidade do ar abaixo"), j);
        assertTrue(j.contains("precipitação abaixo"), j);
        assertTrue(j.indexOf("umidade do solo") < j.indexOf("temperatura"), "fatores ordenados pela contribuição");
    }

    @ParameterizedTest(name = "solo {0}% -> penalidade {1}")
    @CsvSource({"0,100", "24.9,100", "25,50", "39.9,50", "40,0", "70,0", "70.1,50", "85,50", "85.1,100", "100,100"})
    void faixasDaUmidadeDoSolo(double valor, int esperado) {
        assertEquals(esperado, risco.penalidadeUmidadeSolo(valor));
    }

    @ParameterizedTest(name = "temperatura {0} °C -> penalidade {1}")
    @CsvSource({"-20,100", "9.9,100", "10,50", "17.9,50", "18,0", "30,0", "30.1,50", "38,50", "38.1,100", "60,100"})
    void faixasDaTemperatura(double valor, int esperado) {
        assertEquals(esperado, risco.penalidadeTemperatura(valor));
    }

    @ParameterizedTest(name = "umidade do ar {0}% -> penalidade {1}")
    @CsvSource({"19.9,100", "20,50", "39.9,50", "40,0", "80,0", "80.1,50", "90,50", "90.1,100"})
    void faixasDaUmidadeDoAr(double valor, int esperado) {
        assertEquals(esperado, risco.penalidadeUmidadeAr(valor));
    }

    @ParameterizedTest(name = "precipitação {0} mm -> penalidade {1}")
    @CsvSource({"0,50", "4.9,50", "5,0", "30,0", "30.1,50", "100,50", "100.1,100", "500,100"})
    void faixasDaPrecipitacao(double valor, int esperado) {
        assertEquals(esperado, risco.penalidadePrecipitacao(valor));
    }

    @Test
    void indiceSempreEntreZeroECemEClassificacaoCoerente() {
        double[] solos = {0, 10, 24, 25, 30, 40, 55, 70, 75, 85, 90, 100};
        double[] temps = {-20, 0, 9, 10, 15, 18, 24, 30, 35, 38, 45, 60};
        double[] ares = {0, 19, 20, 30, 40, 60, 80, 85, 90, 95, 100};
        double[] precs = {0, 3, 5, 15, 30, 60, 100, 150, 500};
        int combinacoes = 0;
        for (double so : solos) {
            for (double t : temps) {
                for (double a : ares) {
                    for (double p : precs) {
                        ResultadoRisco r = risco.calcular(so, t, a, p);
                        assertTrue(r.getIndice() >= 0 && r.getIndice() <= 100);
                        assertEquals(NivelRisco.classificar(r.getIndice()), r.getNivel());
                        assertFalse(r.getJustificativa().isBlank());
                        combinacoes++;
                    }
                }
            }
        }
        assertEquals(12 * 12 * 11 * 9, combinacoes);
    }

    @Test
    void resultadoDeterministicoParaAMesmaEntrada() {
        ResultadoRisco a = risco.calcular(33.3, 31.7, 85.2, 47.0);
        for (int i = 0; i < 50; i++) {
            ResultadoRisco b = new RiscoService().calcular(33.3, 31.7, 85.2, 47.0);
            assertEquals(a.getIndice(), b.getIndice());
            assertEquals(a.getNivel(), b.getNivel());
            assertEquals(a.getJustificativa(), b.getJustificativa());
        }
    }

    @Test
    void dadosInsuficientesNaoSaoClassificadosComoBaixo() {
        ResultadoRisco r = risco.calcular(50.0, null, 60.0, Double.NaN);
        assertFalse(r.isCalculado());
        assertNull(r.getIndice());
        assertEquals(NivelRisco.NAO_CALCULADO, r.getNivel());
        assertTrue(r.getJustificativa().contains("temperatura"));
        assertTrue(r.getJustificativa().contains("precipitação"));
        ResultadoRisco semRegistro = risco.calcular((IndicadorAmbiental) null);
        assertEquals(NivelRisco.NAO_CALCULADO, semRegistro.getNivel());
        assertEquals(NivelRisco.NAO_CALCULADO, risco.calcular(null, 1.0, 1.0, Double.POSITIVE_INFINITY).getNivel());
        assertEquals(NivelRisco.NAO_CALCULADO, risco.calcular(1.0, 1.0, null, 1.0).getNivel());
    }

    @Test
    void calculaAPartirDeUmRegistro() {
        IndicadorAmbiental i = new IndicadorAmbiental(1, 88.0, 22.0, 85.0, 120.0, LocalDateTime.of(2026, 1, 1, 0, 0));
        ResultadoRisco r = risco.calcular(i);
        assertEquals(63, r.getIndice());
        assertEquals(NivelRisco.MEDIO, risco.classificar(r.getIndice()));
        assertTrue(r.getJustificativa().contains("precipitação muito acima"));
        assertTrue(r.getJustificativa().contains("umidade do solo muito acima"));
        assertTrue(r.getJustificativa().contains("umidade do ar acima"));
    }

    @Test
    void pesosSomamCemPorCento() {
        assertEquals(100, RiscoService.PESO_SOLO + RiscoService.PESO_TEMPERATURA + RiscoService.PESO_UMIDADE_AR
                + RiscoService.PESO_PRECIPITACAO);
        assertEquals("17,5", RiscoService.formatar(17.5));
        assertEquals("30", RiscoService.formatar(30.0));
    }
}
