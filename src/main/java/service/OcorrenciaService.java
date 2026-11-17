package service;

import domain.OcorrenciaCampo;
import domain.TipoOcorrencia;
import domain.ValidacaoException;
import infra.OcorrenciaRepository;
import infra.TalhaoRepository;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/**
 * Registro de ocorrências de campo (RF-10).
 * Nesta versão as ocorrências são informativas: não alteram o índice numérico de risco.
 */
public class OcorrenciaService {

    private final TalhaoRepository talhoes;
    private final OcorrenciaRepository ocorrencias;
    private final Clock relogio;

    public OcorrenciaService(TalhaoRepository talhoes, OcorrenciaRepository ocorrencias, Clock relogio) {
        this.talhoes = talhoes;
        this.ocorrencias = ocorrencias;
        this.relogio = relogio;
    }

    public OcorrenciaCampo registrar(long talhaoId, TipoOcorrencia tipo, String descricao, LocalDate data) {
        if (!talhoes.existe(talhaoId)) {
            throw new ValidacaoException("Selecione um talhão cadastrado.");
        }
        OcorrenciaCampo nova = new OcorrenciaCampo(talhaoId, tipo, descricao, data, LocalDate.now(relogio));
        return ocorrencias.salvar(nova);
    }

    public List<OcorrenciaCampo> listarPorTalhao(long talhaoId) {
        return ocorrencias.listarPorTalhao(talhaoId);
    }

    public void excluir(long id) {
        if (!ocorrencias.remover(id)) {
            throw new ValidacaoException("Ocorrência não encontrada (id " + id + ").");
        }
    }
}
