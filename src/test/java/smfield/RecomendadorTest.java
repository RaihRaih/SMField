package smfield;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.Test;

class RecomendadorTest {
    private static final int ANO = 2026;

    @Test
    void classificacaoLimites() {
        assertEquals("ideal", Recomendador.classificar(75));
        assertEquals("aceit\u00e1vel", Recomendador.classificar(50));
        assertEquals("desfavor\u00e1vel", Recomendador.classificar(49.9));
    }

    @Test
    void notaSempreEntre0e100() {
        for (Cultura c : DadosLoader.carregarCulturas()) {
            for (Regiao r : DadosLoader.carregarRegioes()) {
                for (int m = 1; m <= 12; m++) {
                    double nota = Recomendador.avaliarMes(c, r, m, ANO).nota();
                    assertTrue(nota >= 0 && nota <= 100, c.nome() + "/" + r.nome() + "/" + m + " nota=" + nota);
                }
            }
        }
    }

    @Test
    void mesInvalido() {
        Cultura milho = DadosLoader.buscarCultura("milho");
        Regiao bh = DadosLoader.buscarRegiao("Belo Horizonte");
        assertThrows(IllegalArgumentException.class, () -> Recomendador.avaliarMes(milho, bh, 13, ANO));
        assertThrows(IllegalArgumentException.class, () -> Recomendador.avaliarMes(milho, bh, 0, ANO));
    }

    @Test
    void milhoEmBhMelhorNaEstacaoChuvosa() {
        Cultura c = DadosLoader.buscarCultura("milho");
        Regiao r = DadosLoader.buscarRegiao("Belo Horizonte");
        assertEquals("ideal", Recomendador.avaliarMes(c, r, 10, ANO).classificacao());
        for (int mesSeco : new int[] {6, 7, 8}) { // plantio na seca: sem chuva na fase inicial
            assertNotEquals("ideal", Recomendador.avaliarMes(c, r, mesSeco, ANO).classificacao());
        }
        assertTrue(Recomendador.avaliarMes(c, r, 10, ANO).nota() > Recomendador.avaliarMes(c, r, 6, ANO).nota() + 30);
    }

    @Test
    void sojaNoSulEvitaInvernoComGeada() {
        Cultura c = DadosLoader.buscarCultura("soja");
        Regiao r = DadosLoader.buscarRegiao("Passo Fundo");
        assertNotEquals("desfavor\u00e1vel", Recomendador.avaliarMes(c, r, 10, ANO).classificacao());
        assertEquals("desfavor\u00e1vel", Recomendador.avaliarMes(c, r, 6, ANO).classificacao());
    }

    @Test
    void geadaPenalizaApenasCulturasSensiveis() {
        Regiao r = DadosLoader.buscarRegiao("Passo Fundo");
        Cultura sensivel = DadosLoader.buscarCultura("tomate");
        Cultura resistente = new Cultura(sensivel.nome(), sensivel.cicloDias(), sensivel.tempIdealMin(),
                sensivel.tempIdealMax(), sensivel.tempAbsMin(), sensivel.tempAbsMax(), sensivel.chuvaMinMm(),
                sensivel.chuvaMaxMm(), false);
        assertTrue(Recomendador.avaliarMes(sensivel, r, 5, ANO).notaGeada() < 1);
        assertEquals(1.0, Recomendador.avaliarMes(resistente, r, 5, ANO).notaGeada());
    }

    @Test
    void oQuePlantarOrdenado() {
        List<Sugestao> res = Recomendador.oQuePlantar(
                DadosLoader.buscarRegiao("Cuiab\u00e1"), DadosLoader.carregarCulturas(), 10, ANO);
        List<Double> notas = new ArrayList<>();
        for (Sugestao s : res) {
            notas.add(s.janela().nota());
        }
        List<Double> esperado = new ArrayList<>(notas);
        esperado.sort(Comparator.reverseOrder());
        assertEquals(esperado, notas);
    }

    @Test
    void recomendarPlantioRetornaDozeMesesOrdenados() {
        List<Janela> js = Recomendador.recomendarPlantio(
                DadosLoader.buscarCultura("soja"), DadosLoader.buscarRegiao("Cuiab\u00e1"), ANO);
        assertEquals(12, js.size());
        for (int i = 1; i < js.size(); i++) {
            assertTrue(js.get(i - 1).nota() >= js.get(i).nota());
        }
    }
}
