package domain;

/** Tipos de ocorrência de campo (RF-10). */
public enum TipoOcorrencia {

    PRAGA("Praga"),
    DOENCA("Doença"),
    FALTA_IRRIGACAO("Falta de irrigação"),
    EXCESSO_AGUA("Excesso de água"),
    OUTRA("Outra ocorrência");

    private final String descricao;

    TipoOcorrencia(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
