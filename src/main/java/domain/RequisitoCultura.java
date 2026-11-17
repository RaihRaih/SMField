package domain;

/**
 * Faixas de referência SIMULADAS de uma cultura, usadas no módulo demonstrativo de compatibilidade
 * por região. Não representam recomendações agronômicas validadas.
 */
public record RequisitoCultura(String cultura,
                               double temperaturaMin, double temperaturaMax,
                               double umidadeSoloMin, double umidadeSoloMax,
                               double umidadeArMin, double umidadeArMax,
                               double precipitacaoMin, double precipitacaoMax) {
}
