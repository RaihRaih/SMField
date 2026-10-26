package smfield;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;

class DadosTest {

    @Test
    void dadosCarregamESaoValidos() {
        assertTrue(DadosLoader.carregarCulturas().size() >= 5);
        assertTrue(DadosLoader.carregarRegioes().stream().allMatch(r -> r.meses().size() == 12));
    }

    @Test
    void buscaIgnoraAcentoECaixa() {
        assertEquals("Feij\u00e3o", DadosLoader.buscarCultura("feijao").nome());
        assertEquals("MT", DadosLoader.buscarRegiao("CUIABA").uf());
        assertEquals("cuiaba", DadosLoader.normalizar("  Cuiab\u00e1 "));
    }

    @Test
    void buscaInexistenteLevantaErro() {
        assertThrows(NoSuchElementException.class, () -> DadosLoader.buscarCultura("banana-ouro"));
    }

    @Test
    void cicloInvalidoEhRejeitado() {
        String json = "[{\"nome\":\"X\",\"ciclo_dias\":0,\"temp_ideal_min\":20,\"temp_ideal_max\":30,"
                + "\"temp_abs_min\":10,\"temp_abs_max\":38,\"chuva_min_mm\":1,\"chuva_max_mm\":2,\"sensivel_geada\":false}]";
        assertThrows(IllegalArgumentException.class, () -> DadosLoader.carregarCulturasDeTexto(json));
    }

    @Test
    void regiaoSemDozeMesesEhRejeitada() {
        String json = "[{\"nome\":\"X\",\"uf\":\"MG\",\"meses\":[{\"temp_media\":20,\"chuva_mm\":10,\"prob_geada\":0}]}]";
        assertThrows(IllegalArgumentException.class, () -> DadosLoader.carregarRegioesDeTexto(json));
    }
}
