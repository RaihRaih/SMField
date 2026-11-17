package service;

import domain.NivelRisco;
import domain.Propriedade;
import domain.RegistroHistorico;
import domain.ResultadoRisco;
import domain.Talhao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Roteiro de teste manual (seção 20 do plano) automatizado na camada de serviços:
 * Fazenda Modelo / Contagem / Talhão A / 12 ha / milho, duas medições, histórico e reabertura.
 * Valores fictícios — não representam condições reais de Contagem.
 */
class RoteiroDemonstracaoTest {

    @TempDir
    Path pasta;

    @Test
    void roteiroCompletoComReabertura() {
        ServicosSMField s = Apoio.servicos(pasta);
        Propriedade fazenda = s.propriedades().cadastrar("Fazenda Modelo", "Contagem");
        Talhao talhao = s.talhoes().cadastrar(fazenda.getId(), "Talhão A", 12.0, "Milho");

        s.indicadores().registrarTexto(talhao.getId(), "55", "24", "65", "12", "17/11/2026 08:00");
        ResultadoRisco primeiro = s.monitoramento().detalhe(talhao.getId()).resultado();
        assertEquals(0, primeiro.getIndice());
        assertEquals(NivelRisco.BAIXO, primeiro.getNivel());

        List<MonitoramentoService.ItemPainel> painel = s.monitoramento().painel();
        assertEquals(1, painel.size());
        assertEquals("Talhão A", painel.get(0).talhao());
        assertEquals("Fazenda Modelo", painel.get(0).propriedade());

        s.indicadores().registrarTexto(talhao.getId(), "22", "36", "35", "0", "17/11/2026 11:00");
        ResultadoRisco segundo = s.monitoramento().detalhe(talhao.getId()).resultado();
        // solo crítico 35 + temperatura intermediária 15 + ar intermediário 7,5 + precipitação 10 = 67,5 -> 68
        assertEquals(68, segundo.getIndice());
        assertEquals(NivelRisco.ALTO, segundo.getNivel());
        assertTrue(segundo.getJustificativa().contains("umidade do solo muito abaixo"));

        List<RegistroHistorico> historico = s.monitoramento().historico(talhao.getId());
        assertEquals(2, historico.size());
        assertTrue(historico.get(1).mudouClassificacao());

        ServicosSMField reaberto = Apoio.servicos(pasta);
        assertEquals(1, reaberto.propriedades().listar().size());
        assertEquals("Contagem", reaberto.propriedades().listar().get(0).getMunicipio());
        assertEquals(2, reaberto.monitoramento().historico(talhao.getId()).size());
        assertEquals(68, reaberto.monitoramento().painel().get(0).indice());
        assertEquals(pasta, reaberto.diretorioDados());
        assertEquals(1, reaberto.monitoramento().alertas().stream()
                .filter(a -> a.tipo() == domain.TipoAlerta.RISCO_ALTO).count());
    }
}
