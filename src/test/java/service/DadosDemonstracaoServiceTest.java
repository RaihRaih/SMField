package service;

import domain.NivelRisco;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DadosDemonstracaoServiceTest {

    @TempDir
    Path pasta;

    @Test
    void carregaDadosSimuladosComTodosOsNiveisSemDuplicar() {
        ServicosSMField s = Apoio.servicos(pasta);
        assertFalse(s.demonstracao().jaCarregado());
        assertTrue(s.demonstracao().carregar());
        assertEquals(3, s.propriedades().listar().size());
        assertEquals(6, s.talhoes().listar().size());
        assertTrue(s.propriedades().listar().stream().allMatch(p -> p.getNome().startsWith(DadosDemonstracaoService.MARCADOR)));
        assertTrue(s.propriedades().listar().stream().allMatch(p -> p.getMunicipio().contains("Fictício")));

        Map<NivelRisco, Long> resumo = s.monitoramento().resumo();
        assertEquals(1L, resumo.get(NivelRisco.ALTO));
        assertEquals(2L, resumo.get(NivelRisco.MEDIO));
        assertEquals(2L, resumo.get(NivelRisco.BAIXO));
        assertEquals(1L, resumo.get(NivelRisco.NAO_CALCULADO));

        // carregar novamente (inclusive após reiniciar) não duplica
        assertFalse(s.demonstracao().carregar());
        ServicosSMField reaberto = Apoio.servicos(pasta);
        assertTrue(reaberto.demonstracao().jaCarregado());
        assertFalse(reaberto.demonstracao().carregar());
        assertEquals(6, reaberto.talhoes().listar().size());
        // Café Encosta: risco alto + mudança; Café Novo: sem indicadores; Milho Norte: mudança Médio -> Baixo
        assertEquals(4, reaberto.monitoramento().alertas().size());
    }
}
