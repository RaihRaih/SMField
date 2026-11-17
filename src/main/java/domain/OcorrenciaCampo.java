package domain;

import java.time.LocalDate;

/**
 * Ocorrência observada em campo e vinculada a um talhão (RF-10).
 * Nesta versão a ocorrência é informativa e não altera o índice numérico de risco.
 */
public class OcorrenciaCampo implements Identificavel {

    private long id;
    private long talhaoId;
    private TipoOcorrencia tipo;
    private String descricao;
    private LocalDate data;

    public OcorrenciaCampo(long talhaoId, TipoOcorrencia tipo, String descricao, LocalDate data, LocalDate hoje) {
        if (talhaoId <= 0) {
            throw new ValidacaoException("Selecione um talhão cadastrado.");
        }
        if (tipo == null) {
            throw new ValidacaoException("O campo \"Tipo de ocorrência\" é obrigatório.");
        }
        if (data == null) {
            throw new ValidacaoException("O campo \"Data\" é obrigatório.");
        }
        if (hoje != null && data.isAfter(hoje)) {
            throw new ValidacaoException("A data da ocorrência não pode ser futura.");
        }
        this.talhaoId = talhaoId;
        this.tipo = tipo;
        this.descricao = Validacao.textoObrigatorio(descricao, "Descrição");
        this.data = data;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void setId(long id) {
        this.id = id;
    }

    public long getTalhaoId() {
        return talhaoId;
    }

    public TipoOcorrencia getTipo() {
        return tipo;
    }

    public String getDescricao() {
        return descricao;
    }

    public LocalDate getData() {
        return data;
    }
}
