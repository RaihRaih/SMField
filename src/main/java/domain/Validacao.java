package domain;

/**
 * Funções utilitárias de validação reutilizadas pelas entidades.
 */
public final class Validacao {

    private Validacao() {
    }

    /** Exige texto não nulo, não vazio e que não seja composto apenas por espaços. */
    public static String textoObrigatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new ValidacaoException("O campo \"" + campo + "\" é obrigatório.");
        }
        return valor.trim();
    }

    /** Exige número finito (rejeita null, NaN e infinito). */
    public static double numeroFinito(Double valor, String campo) {
        if (valor == null) {
            throw new ValidacaoException("O campo \"" + campo + "\" é obrigatório.");
        }
        if (valor.isNaN() || valor.isInfinite()) {
            throw new ValidacaoException("O campo \"" + campo + "\" deve ser um número válido.");
        }
        return valor;
    }

    /** Exige número finito dentro do intervalo fechado [minimo, maximo]. */
    public static double numeroNoIntervalo(Double valor, double minimo, double maximo, String campo, String unidade) {
        double v = numeroFinito(valor, campo);
        if (v < minimo || v > maximo) {
            throw new ValidacaoException(String.format(java.util.Locale.ROOT,
                    "O campo \"%s\" deve estar entre %s e %s %s (valor informado: %s).",
                    campo, formatar(minimo), formatar(maximo), unidade, formatar(v)));
        }
        return v;
    }

    static String formatar(double v) {
        if (v == Math.rint(v)) {
            return String.valueOf((long) v);
        }
        return String.valueOf(v);
    }
}
