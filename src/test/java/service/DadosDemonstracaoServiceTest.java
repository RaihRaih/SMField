package service;

import domain.Alerta;
import domain.IndicadorAmbiental;
import domain.NivelRisco;
import domain.Propriedade;
import domain.TipoAlerta;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DadosDemonstracaoServiceTest {

    @TempDir
    Path pasta;

    @Test
    void carregaPolosReaisDoAgronegocioComClimaDoInmetSemDuplicar() {
        ServicosSMField s = Apoio.servicos(pasta);
        assertFalse(s.demonstracao().jaCarregado());
        assertTrue(s.demonstracao().carregar());

        List<Propriedade> props = s.propriedades().listar();
        assertEquals(3, props.size());
        assertTrue(props.stream().allMatch(p -> p.getNome().startsWith(DadosDemonstracaoService.MARCADOR)));
        assertEquals(Set.of("Sorriso/MT", "Rio Verde/GO", "Patrocínio/MG"),
                props.stream().map(Propriedade::getMunicipio).collect(Collectors.toSet()));
        assertEquals(6, s.talhoes().listar().size());

        // todos os registros vêm do INMET, com a fonte identificada
        List<IndicadorAmbiental> registros = s.indicadores().listarTodos();
        assertEquals(31 + 91 + 92 + 92 + 92, registros.size(), "dias completos importados das três estações");
        assertTrue(registros.stream().allMatch(IndicadorAmbiental::isDadoInmet));
        assertTrue(registros.stream().anyMatch(i -> i.getFonte().contains("A904")));

        // variedade real de situações no painel
        Map<NivelRisco, Long> resumo = s.monitoramento().resumo();
        assertEquals(1L, resumo.get(NivelRisco.ALTO));
        assertEquals(2L, resumo.get(NivelRisco.MEDIO));
        assertEquals(2L, resumo.get(NivelRisco.BAIXO));
        assertEquals(1L, resumo.get(NivelRisco.NAO_CALCULADO));
        MonitoramentoService.ItemPainel maisCritico = s.monitoramento().painel().get(0);
        assertEquals("Talhão Milho Safrinha", maisCritico.talhao());
        assertTrue(maisCritico.propriedade().contains("Santa Helena"), "Rio Verde, seca de agosto");
        assertEquals(68, maisCritico.indice());

        List<Alerta> alertas = s.monitoramento().alertas();
        assertEquals(1, alertas.stream().filter(a -> a.tipo() == TipoAlerta.RISCO_ALTO).count());
        assertEquals(1, alertas.stream().filter(a -> a.tipo() == TipoAlerta.SEM_INDICADORES).count());
        assertTrue(alertas.stream().anyMatch(a -> a.tipo() == TipoAlerta.MUDANCA_CLASSIFICACAO));
        assertEquals(3, s.talhoes().listar().stream().mapToInt(t -> s.ocorrencias().listarPorTalhao(t.getId()).size()).sum());

        // carregar novamente (inclusive após reiniciar) não duplica
        assertFalse(s.demonstracao().carregar());
        ServicosSMField reaberto = Apoio.servicos(pasta);
        assertTrue(reaberto.demonstracao().jaCarregado());
        assertFalse(reaberto.demonstracao().carregar());
        assertEquals(6, reaberto.talhoes().listar().size());
        assertEquals(registros.size(), reaberto.indicadores().listarTodos().size());

        // compatibilidade passa a usar as médias reais do município
        assertTrue(reaberto.compatibilidade().avaliar("Sorriso/MT").dadosSuficientes());
    }
}
