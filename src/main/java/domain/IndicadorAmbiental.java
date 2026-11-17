package domain;

import java.time.LocalDateTime;

/**
 * Registro de indicadores ambientais de um talhão em um instante (RF-04, RN-04).
 * A precipitação refere-se ao acumulado das últimas 24 horas.
 * Os objetos são imutáveis: novos registros não sobrescrevem os anteriores (RF-11).
 */
public class IndicadorAmbiental implements Identificavel {

    public static final double UMIDADE_MIN = 0;
    public static final double UMIDADE_MAX = 100;
    public static final double TEMPERATURA_MIN = -20;
    public static final double TEMPERATURA_MAX = 60;
    public static final double PRECIPITACAO_MIN = 0;
    /** Limite superior de plausibilidade para precipitação acumulada em 24 h. */
    public static final double PRECIPITACAO_MAX = 500;

    private long id;
    private long talhaoId;
    private double umidadeSolo;
    private double temperatura;
    private double umidadeAr;
    private double precipitacao24h;
    private LocalDateTime dataHora;

    public IndicadorAmbiental(long talhaoId, Double umidadeSolo, Double temperatura,
                              Double umidadeAr, Double precipitacao24h, LocalDateTime dataHora) {
        if (talhaoId <= 0) {
            throw new ValidacaoException("Selecione um talhão cadastrado.");
        }
        if (dataHora == null) {
            throw new ValidacaoException("O campo \"Data e hora\" é obrigatório.");
        }
        this.talhaoId = talhaoId;
        this.umidadeSolo = Validacao.numeroNoIntervalo(umidadeSolo, UMIDADE_MIN, UMIDADE_MAX, "Umidade do solo", "%");
        this.temperatura = Validacao.numeroNoIntervalo(temperatura, TEMPERATURA_MIN, TEMPERATURA_MAX, "Temperatura", "°C");
        this.umidadeAr = Validacao.numeroNoIntervalo(umidadeAr, UMIDADE_MIN, UMIDADE_MAX, "Umidade do ar", "%");
        this.precipitacao24h = Validacao.numeroNoIntervalo(precipitacao24h, PRECIPITACAO_MIN, PRECIPITACAO_MAX,
                "Precipitação (24 h)", "mm");
        this.dataHora = dataHora.withSecond(0).withNano(0);
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void setId(long id) {
        this.id = id;
    }

    public long getTalhaoId() {
        return talhaoId;
    }

    public double getUmidadeSolo() {
        return umidadeSolo;
    }

    public double getTemperatura() {
        return temperatura;
    }

    public double getUmidadeAr() {
        return umidadeAr;
    }

    public double getPrecipitacao24h() {
        return precipitacao24h;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }
}
