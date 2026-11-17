package service;

import domain.Alerta;
import domain.IndicadorAmbiental;
import domain.NivelRisco;
import domain.OcorrenciaCampo;
import domain.Propriedade;
import domain.RegistroHistorico;
import domain.ResultadoRisco;
import domain.Talhao;
import domain.TipoAlerta;
import infra.IndicadorRepository;
import infra.OcorrenciaRepository;
import infra.PropriedadeRepository;
import infra.TalhaoRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Consolida propriedades, talhões, indicadores e risco para o painel, a consulta individual,
 * o histórico e os alertas (RF-07, RF-08, RF-11, RF-12).
 */
public class MonitoramentoService {

    /** Linha do painel de riscos. {@code indice} e {@code atualizadoEm} são nulos quando não há cálculo. */
    public record ItemPainel(long propriedadeId, String propriedade, long talhaoId, String talhao, String cultura,
                             double areaHectares, Integer indice, NivelRisco nivel, LocalDateTime atualizadoEm) {
    }

    /** Dados da consulta individual de um talhão. {@code ultimoRegistro} é nulo se não houver indicadores. */
    public record DetalheTalhao(Talhao talhao, Propriedade propriedade, IndicadorAmbiental ultimoRegistro,
                                ResultadoRisco resultado, List<OcorrenciaCampo> ocorrencias, int totalRegistros) {
    }

    /** Ordem do painel: maior criticidade e maior índice primeiro; não calculados ao final. */
    public static final Comparator<ItemPainel> POR_CRITICIDADE = Comparator
            .comparingInt((ItemPainel i) -> i.nivel().gravidade()).reversed()
            .thenComparing(i -> i.indice() == null ? -1 : i.indice(), Comparator.reverseOrder())
            .thenComparing(ItemPainel::propriedade, String.CASE_INSENSITIVE_ORDER)
            .thenComparing(ItemPainel::talhao, String.CASE_INSENSITIVE_ORDER);

    private final PropriedadeRepository propriedades;
    private final TalhaoRepository talhoes;
    private final IndicadorRepository indicadores;
    private final OcorrenciaRepository ocorrencias;
    private final RiscoService risco;

    public MonitoramentoService(PropriedadeRepository propriedades, TalhaoRepository talhoes,
                                IndicadorRepository indicadores, OcorrenciaRepository ocorrencias,
                                RiscoService risco) {
        this.propriedades = propriedades;
        this.talhoes = talhoes;
        this.indicadores = indicadores;
        this.ocorrencias = ocorrencias;
        this.risco = risco;
    }

    /** RF-07: todos os talhões com o último índice calculado, ordenados por criticidade. */
    public List<ItemPainel> painel() {
        List<ItemPainel> itens = new ArrayList<>();
        for (Talhao t : talhoes.listar()) {
            String nomePropriedade = propriedades.buscarPorId(t.getPropriedadeId())
                    .map(Propriedade::getNome).orElse("(propriedade removida)");
            IndicadorAmbiental ultimo = indicadores.ultimoDoTalhao(t.getId()).orElse(null);
            ResultadoRisco r = risco.calcular(ultimo);
            itens.add(new ItemPainel(t.getPropriedadeId(), nomePropriedade, t.getId(), t.getNome(), t.getCultura(),
                    t.getAreaHectares(), r.getIndice(), r.getNivel(), ultimo == null ? null : ultimo.getDataHora()));
        }
        itens.sort(POR_CRITICIDADE);
        return itens;
    }

    /** Quantidade de talhões por nível, incluindo NÃO CALCULADO. */
    public Map<NivelRisco, Long> resumo() {
        Map<NivelRisco, Long> mapa = new EnumMap<>(NivelRisco.class);
        for (NivelRisco n : NivelRisco.values()) {
            mapa.put(n, 0L);
        }
        for (ItemPainel item : painel()) {
            mapa.merge(item.nivel(), 1L, Long::sum);
        }
        return mapa;
    }

    /** RF-08: indicadores, índice, classificação, justificativa e ocorrências do talhão. */
    public DetalheTalhao detalhe(long talhaoId) {
        Talhao t = talhoes.buscarPorId(talhaoId)
                .orElseThrow(() -> new domain.ValidacaoException("Talhão não encontrado (id " + talhaoId + ")."));
        Propriedade p = propriedades.buscarPorId(t.getPropriedadeId()).orElse(null);
        IndicadorAmbiental ultimo = indicadores.ultimoDoTalhao(talhaoId).orElse(null);
        return new DetalheTalhao(t, p, ultimo, risco.calcular(ultimo), ocorrencias.listarPorTalhao(talhaoId),
                indicadores.listarPorTalhao(talhaoId).size());
    }

    /** RF-11: histórico cronológico (mais antigo primeiro) com o risco de cada registro. */
    public List<RegistroHistorico> historico(long talhaoId) {
        List<RegistroHistorico> lista = new ArrayList<>();
        NivelRisco anterior = null;
        for (IndicadorAmbiental ind : indicadores.listarPorTalhao(talhaoId)) {
            ResultadoRisco r = risco.calcular(ind);
            lista.add(new RegistroHistorico(ind, r, anterior));
            anterior = r.getNivel();
        }
        return lista;
    }

    /** RF-12: alertas de risco alto, mudança de classificação e ausência de indicadores. */
    public List<Alerta> alertas() {
        List<Alerta> lista = new ArrayList<>();
        for (Talhao t : talhoes.listar()) {
            String prop = propriedades.buscarPorId(t.getPropriedadeId()).map(Propriedade::getNome).orElse("-");
            List<RegistroHistorico> hist = historico(t.getId());
            if (hist.isEmpty()) {
                lista.add(new Alerta(TipoAlerta.SEM_INDICADORES, t.getId(), prop, t.getNome(),
                        "Talhão sem indicadores suficientes: registre umidade do solo, temperatura, umidade do ar e precipitação.",
                        null));
                continue;
            }
            RegistroHistorico ultimo = hist.get(hist.size() - 1);
            ResultadoRisco r = ultimo.resultado();
            if (r.getNivel() == NivelRisco.ALTO) {
                lista.add(new Alerta(TipoAlerta.RISCO_ALTO, t.getId(), prop, t.getNome(),
                        r.getJustificativa(), ultimo.dataHora()));
            }
            if (ultimo.mudouClassificacao()) {
                lista.add(new Alerta(TipoAlerta.MUDANCA_CLASSIFICACAO, t.getId(), prop, t.getNome(),
                        "Classificação mudou de " + ultimo.nivelAnterior().getDescricao() + " para "
                                + r.getNivel().getDescricao() + " no último registro.", ultimo.dataHora()));
            }
        }
        lista.sort(Comparator.comparing(Alerta::tipo)
                .thenComparing(a -> a.dataHora() == null ? LocalDateTime.MIN : a.dataHora(), Comparator.reverseOrder()));
        return lista;
    }
}
