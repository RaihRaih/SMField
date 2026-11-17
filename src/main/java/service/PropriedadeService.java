package service;

import domain.Propriedade;
import domain.ValidacaoException;
import infra.PropriedadeRepository;
import infra.TalhaoRepository;

import java.util.List;
import java.util.Optional;

/** Regras de cadastro e consulta de propriedades (RF-01, RF-03). */
public class PropriedadeService {

    private final PropriedadeRepository propriedades;
    private final TalhaoRepository talhoes;

    public PropriedadeService(PropriedadeRepository propriedades, TalhaoRepository talhoes) {
        this.propriedades = propriedades;
        this.talhoes = talhoes;
    }

    public Propriedade cadastrar(String nome, String municipio) {
        Propriedade nova = new Propriedade(nome, municipio);
        if (propriedades.existeComNomeEMunicipio(nova.getNome(), nova.getMunicipio(), 0)) {
            throw new ValidacaoException("Já existe a propriedade \"" + nova.getNome() + "\" em " + nova.getMunicipio() + ".");
        }
        return propriedades.salvar(nova);
    }

    public Propriedade editar(long id, String nome, String municipio) {
        Propriedade existente = obter(id);
        Propriedade validada = new Propriedade(nome, municipio);
        if (propriedades.existeComNomeEMunicipio(validada.getNome(), validada.getMunicipio(), id)) {
            throw new ValidacaoException("Já existe a propriedade \"" + validada.getNome() + "\" em " + validada.getMunicipio() + ".");
        }
        existente.alterar(validada.getNome(), validada.getMunicipio());
        return propriedades.salvar(existente);
    }

    /** Exclui a propriedade somente se ela não possuir talhões (preserva RN-01). */
    public void excluir(long id) {
        obter(id);
        int quantidade = talhoes.listarPorPropriedade(id).size();
        if (quantidade > 0) {
            throw new ValidacaoException("A propriedade possui " + quantidade
                    + " talhão(ões). Exclua os talhões antes de excluir a propriedade.");
        }
        propriedades.remover(id);
    }

    public List<Propriedade> listar() {
        return propriedades.listarPorNome();
    }

    public Optional<Propriedade> buscar(long id) {
        return propriedades.buscarPorId(id);
    }

    public Propriedade obter(long id) {
        return propriedades.buscarPorId(id)
                .orElseThrow(() -> new ValidacaoException("Propriedade não encontrada (id " + id + ")."));
    }
}
