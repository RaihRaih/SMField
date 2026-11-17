package domain;

import java.util.List;

/**
 * Resultado da análise de risco de um talhão: índice, nível, fatores e justificativa (RF-05, RF-06, RF-08).
 */
public final class ResultadoRisco {

    private final Integer indice;
    private final NivelRisco nivel;
    private final List<FatorRisco> fatores;
    private final String justificativa;

    private ResultadoRisco(Integer indice, NivelRisco nivel, List<FatorRisco> fatores, String justificativa) {
        this.indice = indice;
        this.nivel = nivel;
        this.fatores = List.copyOf(fatores);
        this.justificativa = justificativa;
    }

    public static ResultadoRisco calculado(int indice, List<FatorRisco> fatores, String justificativa) {
        NivelRisco nivel = NivelRisco.classificar(indice);
        return new ResultadoRisco(indice, nivel, fatores, justificativa);
    }

    public static ResultadoRisco naoCalculado(String motivo) {
        return new ResultadoRisco(null, NivelRisco.NAO_CALCULADO, List.of(), motivo);
    }

    public boolean isCalculado() {
        return indice != null;
    }

    /** Índice de 0 a 100, ou {@code null} quando não calculado. */
    public Integer getIndice() {
        return indice;
    }

    public NivelRisco getNivel() {
        return nivel;
    }

    public List<FatorRisco> getFatores() {
        return fatores;
    }

    public String getJustificativa() {
        return justificativa;
    }
}
