package domain;

import java.time.LocalDateTime;

/**
 * Item do histórico de um talhão: um registro de indicadores e o risco calculado para ele (RF-11).
 *
 * @param indicador    registro de indicadores preservado
 * @param resultado    risco calculado a partir desse registro
 * @param nivelAnterior nível do registro imediatamente anterior ({@code null} se for o primeiro)
 */
public record RegistroHistorico(IndicadorAmbiental indicador, ResultadoRisco resultado, NivelRisco nivelAnterior) {

    public LocalDateTime dataHora() {
        return indicador.getDataHora();
    }

    /** Indica se a classificação mudou em relação ao registro anterior. */
    public boolean mudouClassificacao() {
        return nivelAnterior != null && nivelAnterior != resultado.getNivel();
    }
}
