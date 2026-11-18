package service;

import domain.Propriedade;
import domain.Talhao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompatibilidadeServiceTest {

    @TempDir
    Path pasta;

    @Test
    void informaDadosInsuficientes() {
        ServicosSMField s = Apoio.servicos(pasta);
        Propriedade p = s.propriedades().cadastrar("Fazenda", "Contagem/MG");
        Talhao t = s.talhoes().cadastrar(p.getId(), "A", 1.0, "Milho");
        s.indicadores().registrar(t.getId(), 55.0, 24.0, 65.0, 12.0, Apoio.AGORA);
        CompatibilidadeService.Avaliacao a = s.compatibilidade().avaliar("Contagem/MG");
        assertFalse(a.dadosSuficientes());
        assertEquals(1, a.quantidadeRegistros());
        assertNull(a.perfil());
        assertTrue(a.mensagem().contains("insuficientes"));
        assertFalse(s.compatibilidade().avaliar("Inexistente").dadosSuficientes());
        assertFalse(s.compatibilidade().avaliar(null).dadosSuficientes());
    }

    @Test
    void comparaMediasComABaseSimulada() {
        ServicosSMField s = Apoio.servicos(pasta);
        Propriedade p1 = s.propriedades().cadastrar("Fazenda 1", "Contagem/MG");
        Propriedade p2 = s.propriedades().cadastrar("Fazenda 2", "contagem/mg");
        s.propriedades().cadastrar("Fazenda 3", "Betim/MG");
        Talhao a = s.talhoes().cadastrar(p1.getId(), "A", 1.0, "Milho");
        Talhao b = s.talhoes().cadastrar(p2.getId(), "B", 1.0, "Soja");
        s.indicadores().registrar(a.getId(), 50.0, 22.0, 60.0, 10.0, Apoio.AGORA.minusHours(3));
        s.indicadores().registrar(a.getId(), 60.0, 24.0, 70.0, 12.0, Apoio.AGORA.minusHours(2));
        s.indicadores().registrar(b.getId(), 55.0, 29.0, 65.0, 8.0, Apoio.AGORA.minusHours(1));

        assertEquals(List.of("Betim/MG", "Contagem/MG"), s.compatibilidade().municipios());
        CompatibilidadeService.Avaliacao r = s.compatibilidade().avaliar("Contagem/MG");
        assertTrue(r.dadosSuficientes());
        assertEquals(3, r.quantidadeRegistros());
        assertEquals(25.0, r.perfil().temperatura(), 1e-9);
        assertEquals(55.0, r.perfil().umidadeSolo(), 1e-9);
        assertEquals(CompatibilidadeService.CATALOGO_SIMULADO.size(), r.culturas().size());
        CompatibilidadeService.CompatibilidadeCultura milho = r.culturas().stream()
                .filter(c -> c.cultura().equals("Milho")).findFirst().orElseThrow();
        assertTrue(milho.compativel());
        CompatibilidadeService.CompatibilidadeCultura trigo = r.culturas().stream()
                .filter(c -> c.cultura().equals("Trigo")).findFirst().orElseThrow();
        assertFalse(trigo.compativel(), "temperatura média 25 °C acima da faixa simulada do trigo (12–24 °C)");
        assertEquals(1, trigo.divergencias().size());
        assertTrue(trigo.divergencias().get(0).startsWith("Temperatura média"));
        assertTrue(r.mensagem().contains("compatível"));
    }

    @Test
    void municipioComEstacaoUsaClimaRealDoInmetSemUmidadeDoSolo() {
        ServicosSMField s = Apoio.servicos(pasta);
        s.propriedades().cadastrar("Fazenda", "Sorriso/MT");
        CompatibilidadeService.Avaliacao a = s.compatibilidade().avaliar("Sorriso/MT");
        assertTrue(a.dadosSuficientes());
        assertTrue(a.fonte().contains("A904"), a.fonte());
        assertNull(a.perfil().umidadeSolo());
        assertEquals(304, a.quantidadeRegistros());
        Set<String> compativeis = a.culturas().stream().filter(CompatibilidadeService.CompatibilidadeCultura::compativel)
                .map(CompatibilidadeService.CompatibilidadeCultura::cultura).collect(Collectors.toSet());
        assertEquals(Set.of("Milho", "Soja", "Feijão"), compativeis);
        assertTrue(a.culturas().stream().noneMatch(c -> c.divergencias().stream().anyMatch(d -> d.contains("solo"))));

        CompatibilidadeService.Avaliacao cafe = s.compatibilidade().avaliar("Patrocínio/MG");
        assertTrue(cafe.culturas().stream().anyMatch(c -> c.cultura().equals("Café") && c.compativel()));
    }

    @Test
    void semBaseDoInmetUsaApenasOsRegistros() {
        ServicosSMField s = Apoio.servicos(pasta);
        CompatibilidadeService semInmet = new CompatibilidadeService(
                new infra.PropriedadeRepository(pasta), new infra.TalhaoRepository(pasta), new infra.IndicadorRepository(pasta), null);
        s.propriedades().cadastrar("Fazenda", "Sorriso/MT");
        assertFalse(semInmet.avaliar("Sorriso/MT").dadosSuficientes());
    }
}
