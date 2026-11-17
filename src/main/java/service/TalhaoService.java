package service;

import domain.Talhao;
import domain.ValidacaoException;
import infra.IndicadorRepository;
import infra.OcorrenciaRepository;
import infra.PropriedadeRepository;
import infra.TalhaoRepository;

import java.util.List;
import java.util.Optional;

/** Regras de cadastro, edição, exclusão e consulta de talhões (RF-02, RF-03, RF-09, RN-01). */
public class TalhaoService {

    /** Quantidade de registros vinculados a um talhão. */
    public record Vinculos(int indicadores, int ocorrencias) {
        public int total() {
            return indicadores + ocorrencias;
        }
    }

    private final PropriedadeRepository propriedades;
    private final TalhaoRepository talhoes;
    private final IndicadorRepository indicadores;
    private final OcorrenciaRepository ocorrencias;

    public TalhaoService(PropriedadeRepository propriedades, TalhaoRepository talhoes,
                         IndicadorRepository indicadores, OcorrenciaRepository ocorrencias) {
        this.propriedades = propriedades;
        this.talhoes = talhoes;
        this.indicadores = indicadores;
        this.ocorrencias = ocorrencias;
    }

    public Talhao cadastrar(long propriedadeId, String nome, Double areaHectares, String cultura) {
        if (!propriedades.existe(propriedadeId)) {
            throw new ValidacaoException("Selecione uma propriedade cadastrada. Todo talhão deve estar vinculado a uma propriedade.");
        }
        Talhao novo = new Talhao(propriedadeId, nome, areaHectares, cultura);
        if (talhoes.existeComNome(propriedadeId, novo.getNome(), 0)) {
            throw new ValidacaoException("Já existe um talhão chamado \"" + novo.getNome() + "\" nesta propriedade.");
        }
        return talhoes.salvar(novo);
    }

    /** RF-09: altera nome, área e cultura. A propriedade vinculada não muda. */
    public Talhao editar(long id, String nome, Double areaHectares, String cultura) {
        Talhao talhao = obter(id);
        Talhao validado = new Talhao(talhao.getPropriedadeId(), nome, areaHectares, cultura);
        if (talhoes.existeComNome(talhao.getPropriedadeId(), validado.getNome(), id)) {
            throw new ValidacaoException("Já existe um talhão chamado \"" + validado.getNome() + "\" nesta propriedade.");
        }
        talhao.alterar(validado.getNome(), validado.getAreaHectares(), validado.getCultura());
        return talhoes.salvar(talhao);
    }

    public Vinculos contarVinculos(long id) {
        obter(id);
        return new Vinculos(indicadores.listarPorTalhao(id).size(), ocorrencias.listarPorTalhao(id).size());
    }

    /**
     * RF-09: exclui o talhão. Estratégia para registros vinculados: exclusão em cascata dos indicadores
     * (histórico) e ocorrências do talhão, para não deixar registros órfãos. A interface informa a
     * quantidade de registros afetados e pede confirmação antes de chamar este método.
     *
     * @return quantidade de registros vinculados removidos
     */
    public Vinculos excluir(long id) {
        obter(id);
        int ind = indicadores.removerPorTalhao(id);
        int oco = ocorrencias.removerPorTalhao(id);
        talhoes.remover(id);
        return new Vinculos(ind, oco);
    }

    public List<Talhao> listar() {
        return talhoes.listar();
    }

    public List<Talhao> listarPorPropriedade(long propriedadeId) {
        return talhoes.listarPorPropriedade(propriedadeId);
    }

    public Optional<Talhao> buscar(long id) {
        return talhoes.buscarPorId(id);
    }

    public Talhao obter(long id) {
        return talhoes.buscarPorId(id)
                .orElseThrow(() -> new ValidacaoException("Talhão não encontrado (id " + id + ")."));
    }
}
