package domain;

/** Tipos de alerta interno (RF-12), do mais para o menos prioritário. */
public enum TipoAlerta {

    RISCO_ALTO("Risco alto"),
    MUDANCA_CLASSIFICACAO("Mudança de classificação"),
    SEM_INDICADORES("Sem indicadores suficientes");

    private final String descricao;

    TipoAlerta(String descricao) {
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
