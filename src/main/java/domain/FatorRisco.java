package domain;

/**
 * Contribuição de um indicador para o índice de risco, usada na justificativa (RF-08, RN-06).
 *
 * @param indicador    nome do indicador (ex.: "Temperatura")
 * @param valor        valor medido
 * @param unidade      unidade de medida
 * @param faixaIdeal   descrição da faixa de referência sem penalidade
 * @param situacao     descrição do desvio (ex.: "acima da faixa de referência"); vazio quando ideal
 * @param penalidade   0, 50 ou 100
 * @param peso         peso do indicador na fórmula (0..1)
 */
public record FatorRisco(String indicador, double valor, String unidade, String faixaIdeal,
                         String situacao, int penalidade, double peso) {

    /** Pontos que este indicador somou ao índice (penalidade × peso). */
    public double contribuicao() {
        return penalidade * peso;
    }

    public boolean isDesvio() {
        return penalidade > 0;
    }
}
