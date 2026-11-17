package domain;

/**
 * Classificação do índice de risco (RF-06, RN-03).
 * {@link #NAO_CALCULADO} é um estado separado dos três níveis e é usado quando faltam dados (RN-05).
 */
public enum NivelRisco {

    BAIXO("Baixo", 0, 33),
    MEDIO("Médio", 34, 66),
    ALTO("Alto", 67, 100),
    NAO_CALCULADO("Não calculado", -1, -1);

    private final String descricao;
    private final int minimo;
    private final int maximo;

    NivelRisco(String descricao, int minimo, int maximo) {
        this.descricao = descricao;
        this.minimo = minimo;
        this.maximo = maximo;
    }

    /** Converte um índice de 0 a 100 no nível correspondente. */
    public static NivelRisco classificar(int indice) {
        if (indice < 0 || indice > 100) {
            throw new ValidacaoException("O índice de risco deve estar entre 0 e 100 (recebido: " + indice + ").");
        }
        if (indice <= BAIXO.maximo) {
            return BAIXO;
        }
        if (indice <= MEDIO.maximo) {
            return MEDIO;
        }
        return ALTO;
    }

    public boolean isCalculado() {
        return this != NAO_CALCULADO;
    }

    /** Peso usado para ordenar por criticidade (maior = mais crítico). */
    public int gravidade() {
        return switch (this) {
            case ALTO -> 3;
            case MEDIO -> 2;
            case BAIXO -> 1;
            case NAO_CALCULADO -> 0;
        };
    }

    public String getDescricao() {
        return descricao;
    }

    public int getMinimo() {
        return minimo;
    }

    public int getMaximo() {
        return maximo;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
