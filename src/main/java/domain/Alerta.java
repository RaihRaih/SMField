package domain;

import java.time.LocalDateTime;

/**
 * Alerta visual interno gerado pelo monitoramento (RF-12).
 *
 * @param tipo        tipo do alerta
 * @param talhaoId    talhão relacionado
 * @param propriedade nome da propriedade
 * @param talhao      nome do talhão
 * @param mensagem    texto explicativo
 * @param dataHora    data do registro que originou o alerta ({@code null} se não houver registro)
 */
public record Alerta(TipoAlerta tipo, long talhaoId, String propriedade, String talhao,
                     String mensagem, LocalDateTime dataHora) {
}
