package service;

import domain.Alerta;
import domain.NivelRisco;
import domain.Propriedade;
import domain.RegistroHistorico;
import domain.Talhao;
import domain.TipoAlerta;
import domain.TipoOcorrencia;
import domain.ValidacaoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MonitoramentoServiceTest {

    @TempDir
    Path pasta;
    private ServicosSMField s;
    private Talhao baixo;
    private Talhao medio;
    private Talhao alto;
    private Talhao semDados;

    @BeforeEach
    void preparar() {
        s = Apoio.servicos(pasta);
        Propriedade p = s.propriedades().cadastrar("Fazenda Modelo", "Contagem");
        baixo = s.talhoes().cadastrar(p.getId(), "Baixo", 1.0, "Soja");
        medio = s.talhoes().cadastrar(p.getId(), "Médio", 1.0, "Milho");
        alto = s.talhoes().cadastrar(p.getId(), "Alto", 1.0, "Café");
        semDados = s.talhoes().cadastrar(p.getId(), "Sem dados", 1.0, "Café");
        s.indicadores().registrar(baixo.getId(), 55.0, 24.0, 65.0, 12.0, Apoio.AGORA.minusHours(3));
        s.indicadores().registrar(medio.getId(), 30.0, 33.0, 30.0, 2.0, Apoio.AGORA.minusHours(2));
        s.indicadores().registrar(alto.getId(), 55.0, 24.0, 65.0, 12.0, Apoio.AGORA.minusDays(1));
        s.indicadores().registrar(alto.getId(), 10.0, 45.0, 10.0, 150.0, Apoio.AGORA.minusHours(1));
    }

    @Test
    void painelOrdenadoDoMaiorParaOMenorRisco() {
        List<MonitoramentoService.ItemPainel> painel = s.monitoramento().painel();
        assertEquals(4, painel.size());
        assertEquals(List.of("Alto", "Médio", "Baixo", "Sem dados"), painel.stream().map(MonitoramentoService.ItemPainel::talhao).toList());
        assertEquals(100, painel.get(0).indice());
        assertEquals(NivelRisco.ALTO, painel.get(0).nivel());
        assertEquals("Fazenda Modelo", painel.get(0).propriedade());
        assertEquals("Café", painel.get(0).cultura());
        assertEquals(Apoio.AGORA.minusHours(1), painel.get(0).atualizadoEm());
        assertNull(painel.get(3).indice());
        assertNull(painel.get(3).atualizadoEm());
        assertEquals(NivelRisco.NAO_CALCULADO, painel.get(3).nivel());
    }

    @Test
    void resumoContaTalhoesPorNivel() {
        Map<NivelRisco, Long> r = s.monitoramento().resumo();
        assertEquals(1L, r.get(NivelRisco.ALTO));
        assertEquals(1L, r.get(NivelRisco.MEDIO));
        assertEquals(1L, r.get(NivelRisco.BAIXO));
        assertEquals(1L, r.get(NivelRisco.NAO_CALCULADO));
    }

    @Test
    void detalheTrazUltimoRegistroJustificativaEOcorrencias() {
        s.ocorrencias().registrar(alto.getId(), TipoOcorrencia.DOENCA, "Ferrugem", Apoio.AGORA.toLocalDate());
        MonitoramentoService.DetalheTalhao d = s.monitoramento().detalhe(alto.getId());
        assertEquals(10.0, d.ultimoRegistro().getUmidadeSolo());
        assertEquals(100, d.resultado().getIndice());
        assertEquals(2, d.totalRegistros());
        assertEquals(1, d.ocorrencias().size());
        assertEquals("Fazenda Modelo", d.propriedade().getNome());
        MonitoramentoService.DetalheTalhao vazio = s.monitoramento().detalhe(semDados.getId());
        assertNull(vazio.ultimoRegistro());
        assertEquals(NivelRisco.NAO_CALCULADO, vazio.resultado().getNivel());
        assertThrows(ValidacaoException.class, () -> s.monitoramento().detalhe(12345));
    }

    @Test
    void historicoPreservaRegistrosEIndicaMudanca() {
        List<RegistroHistorico> h = s.monitoramento().historico(alto.getId());
        assertEquals(2, h.size());
        assertNull(h.get(0).nivelAnterior());
        assertFalse(h.get(0).mudouClassificacao());
        assertEquals(NivelRisco.BAIXO, h.get(1).nivelAnterior());
        assertTrue(h.get(1).mudouClassificacao());
        assertEquals(Apoio.AGORA.minusHours(1), h.get(1).dataHora());
    }

    @Test
    void alertasDeRiscoAltoMudancaESemIndicadores() {
        List<Alerta> alertas = s.monitoramento().alertas();
        assertEquals(3, alertas.size());
        assertEquals(TipoAlerta.RISCO_ALTO, alertas.get(0).tipo());
        assertEquals("Alto", alertas.get(0).talhao());
        assertEquals(TipoAlerta.MUDANCA_CLASSIFICACAO, alertas.get(1).tipo());
        assertTrue(alertas.get(1).mensagem().contains("Baixo para Alto"));
        assertEquals(TipoAlerta.SEM_INDICADORES, alertas.get(2).tipo());
        assertEquals(semDados.getId(), alertas.get(2).talhaoId());
        assertNull(alertas.get(2).dataHora());
    }

    @Test
    void painelSemTalhoesFicaVazio() {
        ServicosSMField vazio = Apoio.servicos(pasta.resolve("vazio"));
        assertTrue(vazio.monitoramento().painel().isEmpty());
        assertTrue(vazio.monitoramento().alertas().isEmpty());
        assertEquals(0L, vazio.monitoramento().resumo().get(NivelRisco.ALTO));
    }
}
