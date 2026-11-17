package service;

import domain.FatorRisco;
import domain.IndicadorAmbiental;
import domain.NivelRisco;
import domain.ResultadoRisco;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Motor de análise de risco (RF-05, RF-06, RF-08, RN-02, RN-03, RN-05, RN-06).
 *
 * <h2>Modelo acadêmico</h2>
 * Cada indicador recebe penalidade 0 (dentro da faixa de referência), 50 (faixa intermediária)
 * ou 100 (faixa crítica). O índice é a média ponderada das penalidades:
 * <pre>risco = solo×0,35 + temperatura×0,30 + umidadeAr×0,15 + precipitação×0,20</pre>
 * arredondada para o inteiro mais próximo (meio para cima). O cálculo é feito com aritmética inteira,
 * por isso é determinístico e sempre fica entre 0 e 100.
 * <p>
 * <b>Limitação:</b> as faixas são hipóteses acadêmicas para demonstrar a lógica do software. Não são
 * limites agronômicos validados para todas as culturas.
 */
public class RiscoService {

    public static final int PESO_SOLO = 35;
    public static final int PESO_TEMPERATURA = 30;
    public static final int PESO_UMIDADE_AR = 15;
    public static final int PESO_PRECIPITACAO = 20;

    public static final String AVISO_ACADEMICO = "Faixas de referência acadêmicas (hipóteses para fins didáticos), "
            + "não validadas agronomicamente para todas as culturas.";

    /** Classificação de um valor em relação às faixas de um indicador. */
    private record Avaliacao(int penalidade, String situacao) {
    }

    /** Calcula o risco a partir de um registro completo de indicadores. */
    public ResultadoRisco calcular(IndicadorAmbiental indicador) {
        if (indicador == null) {
            return ResultadoRisco.naoCalculado("Risco não calculado: o talhão ainda não possui indicadores ambientais registrados.");
        }
        return calcular(indicador.getUmidadeSolo(), indicador.getTemperatura(), indicador.getUmidadeAr(),
                indicador.getPrecipitacao24h());
    }

    /**
     * Calcula o risco a partir dos quatro valores. Se algum for nulo, NaN ou infinito, devolve
     * {@link NivelRisco#NAO_CALCULADO} com a lista do que falta (RN-05).
     */
    public ResultadoRisco calcular(Double umidadeSolo, Double temperatura, Double umidadeAr, Double precipitacao24h) {
        List<String> faltando = new ArrayList<>();
        if (invalido(umidadeSolo)) {
            faltando.add("umidade do solo");
        }
        if (invalido(temperatura)) {
            faltando.add("temperatura");
        }
        if (invalido(umidadeAr)) {
            faltando.add("umidade do ar");
        }
        if (invalido(precipitacao24h)) {
            faltando.add("precipitação");
        }
        if (!faltando.isEmpty()) {
            return ResultadoRisco.naoCalculado("Risco não calculado: dados insuficientes. Faltando: "
                    + String.join(", ", faltando) + ".");
        }

        List<FatorRisco> fatores = List.of(
                fator("Umidade do solo", umidadeSolo, "%", "40–70 %", avaliarUmidadeSolo(umidadeSolo), PESO_SOLO),
                fator("Temperatura", temperatura, "°C", "18–30 °C", avaliarTemperatura(temperatura), PESO_TEMPERATURA),
                fator("Umidade do ar", umidadeAr, "%", "40–80 %", avaliarUmidadeAr(umidadeAr), PESO_UMIDADE_AR),
                fator("Precipitação (24 h)", precipitacao24h, "mm", "5–30 mm", avaliarPrecipitacao(precipitacao24h),
                        PESO_PRECIPITACAO));

        int soma = avaliarUmidadeSolo(umidadeSolo).penalidade() * PESO_SOLO
                + avaliarTemperatura(temperatura).penalidade() * PESO_TEMPERATURA
                + avaliarUmidadeAr(umidadeAr).penalidade() * PESO_UMIDADE_AR
                + avaliarPrecipitacao(precipitacao24h).penalidade() * PESO_PRECIPITACAO;
        int indice = (soma + 50) / 100; // arredondamento para o inteiro mais próximo (meio para cima)
        indice = Math.max(0, Math.min(100, indice));

        NivelRisco nivel = NivelRisco.classificar(indice);
        return ResultadoRisco.calculado(indice, fatores, justificar(indice, nivel, fatores));
    }

    /** RF-06: classificação de um índice já calculado. */
    public NivelRisco classificar(int indice) {
        return NivelRisco.classificar(indice);
    }

    // ---------------------------------------------------------------- penalidades por indicador

    public int penalidadeUmidadeSolo(double valor) {
        return avaliarUmidadeSolo(valor).penalidade();
    }

    public int penalidadeTemperatura(double valor) {
        return avaliarTemperatura(valor).penalidade();
    }

    public int penalidadeUmidadeAr(double valor) {
        return avaliarUmidadeAr(valor).penalidade();
    }

    public int penalidadePrecipitacao(double valor) {
        return avaliarPrecipitacao(valor).penalidade();
    }

    private Avaliacao avaliarUmidadeSolo(double v) {
        return faixas(v, 25, 40, 70, 85, "umidade do solo");
    }

    private Avaliacao avaliarTemperatura(double v) {
        return faixas(v, 10, 18, 30, 38, "temperatura");
    }

    private Avaliacao avaliarUmidadeAr(double v) {
        return faixas(v, 20, 40, 80, 90, "umidade do ar");
    }

    private Avaliacao avaliarPrecipitacao(double v) {
        if (v >= 5 && v <= 30) {
            return new Avaliacao(0, "");
        }
        if (v < 5) {
            return new Avaliacao(50, "precipitação abaixo da faixa de referência, com pouca chuva nas últimas 24 h");
        }
        if (v <= 100) {
            return new Avaliacao(50, "precipitação acima da faixa de referência, com chuva intensa nas últimas 24 h");
        }
        return new Avaliacao(100, "precipitação muito acima da faixa de referência, com chuva excessiva nas últimas 24 h");
    }

    /**
     * Faixas simétricas: [idealMin, idealMax] → 0; [criticoMin, idealMin) ou (idealMax, criticoMax] → 50;
     * fora de [criticoMin, criticoMax] → 100.
     */
    private Avaliacao faixas(double v, double criticoMin, double idealMin, double idealMax, double criticoMax,
                             String nome) {
        if (v >= idealMin && v <= idealMax) {
            return new Avaliacao(0, "");
        }
        if (v < criticoMin) {
            return new Avaliacao(100, nome + " muito abaixo da faixa de referência");
        }
        if (v < idealMin) {
            return new Avaliacao(50, nome + " abaixo da faixa de referência");
        }
        if (v <= criticoMax) {
            return new Avaliacao(50, nome + " acima da faixa de referência");
        }
        return new Avaliacao(100, nome + " muito acima da faixa de referência");
    }

    private FatorRisco fator(String nome, double valor, String unidade, String faixa, Avaliacao avaliacao, int peso) {
        return new FatorRisco(nome, valor, unidade, faixa, avaliacao.situacao(), avaliacao.penalidade(), peso / 100.0);
    }

    // ---------------------------------------------------------------- justificativa

    /** RN-06: texto montado a partir dos indicadores que efetivamente contribuíram para o índice. */
    private String justificar(int indice, NivelRisco nivel, List<FatorRisco> fatores) {
        List<FatorRisco> desvios = fatores.stream()
                .filter(FatorRisco::isDesvio)
                .sorted(Comparator.comparingDouble(FatorRisco::contribuicao).reversed())
                .toList();
        String prefixo = "Risco " + nivel.getDescricao().toLowerCase() + " (índice " + indice + ")";
        if (desvios.isEmpty()) {
            return prefixo + ": não foram identificados desvios nas faixas de referência acadêmicas adotadas. "
                    + "Todos os indicadores estão dentro das faixas ideais.";
        }
        List<String> partes = new ArrayList<>();
        for (FatorRisco f : desvios) {
            partes.add(String.format(java.util.Locale.forLanguageTag("pt-BR"), "%s (%s %s; referência %s; +%s pontos)",
                    f.situacao(), formatar(f.valor()), f.unidade(), f.faixaIdeal(), formatar(f.contribuicao())));
        }
        return prefixo + " identificado devido a: " + juntar(partes) + ".";
    }

    private static String juntar(List<String> partes) {
        if (partes.size() == 1) {
            return partes.get(0);
        }
        return String.join("; ", partes.subList(0, partes.size() - 1)) + " e " + partes.get(partes.size() - 1);
    }

    static String formatar(double v) {
        if (v == Math.rint(v)) {
            return String.valueOf((long) v);
        }
        return String.format(java.util.Locale.forLanguageTag("pt-BR"), "%.1f", v);
    }

    private static boolean invalido(Double v) {
        return v == null || v.isNaN() || v.isInfinite();
    }
}
