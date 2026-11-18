package domain;

import java.time.LocalDate;

/**
 * Resumo diário real de uma estação do INMET. Campos podem ser {@code null} quando a estação
 * não registrou horas suficientes naquele dia.
 *
 * @param estacao    código da estação
 * @param data       dia (horário de Brasília)
 * @param tempMedia  temperatura média do ar (°C)
 * @param tempMinima temperatura mínima do ar (°C)
 * @param umidadeAr  umidade relativa média do ar (%)
 * @param chuvaMm    precipitação total do dia (mm)
 */
public record LeituraClimaticaDiaria(String estacao, LocalDate data, Double tempMedia, Double tempMinima,
                                     Double umidadeAr, Double chuvaMm) {

    /** Verdadeiro quando temperatura, umidade do ar e chuva estão disponíveis. */
    public boolean completa() {
        return tempMedia != null && umidadeAr != null && chuvaMm != null;
    }
}
