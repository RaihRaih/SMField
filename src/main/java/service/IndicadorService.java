package service;

import domain.IndicadorAmbiental;
import domain.ValidacaoException;
import infra.IndicadorRepository;
import infra.TalhaoRepository;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Registro e consulta de indicadores ambientais (RF-04, RF-11, RN-04, RN-05).
 * Cada registro é um novo item do histórico: registros anteriores nunca são sobrescritos.
 */
public class IndicadorService {

    private final TalhaoRepository talhoes;
    private final IndicadorRepository indicadores;
    private final Clock relogio;

    public IndicadorService(TalhaoRepository talhoes, IndicadorRepository indicadores, Clock relogio) {
        this.talhoes = talhoes;
        this.indicadores = indicadores;
        this.relogio = relogio;
    }

    /**
     * Registra os quatro indicadores de um talhão.
     *
     * @param precipitacao24h precipitação acumulada nas últimas 24 horas (mm)
     */
    public IndicadorAmbiental registrar(long talhaoId, Double umidadeSolo, Double temperatura,
                                        Double umidadeAr, Double precipitacao24h, LocalDateTime dataHora) {
        return registrar(talhaoId, umidadeSolo, temperatura, umidadeAr, precipitacao24h, dataHora, null);
    }

    /**
     * Registra os indicadores informando a origem dos dados (ex.: importação do INMET).
     *
     * @param fonte descrição da origem; {@code null} para registro manual
     */
    public IndicadorAmbiental registrar(long talhaoId, Double umidadeSolo, Double temperatura,
                                        Double umidadeAr, Double precipitacao24h, LocalDateTime dataHora,
                                        String fonte) {
        if (!talhoes.existe(talhaoId)) {
            throw new ValidacaoException("Selecione um talhão cadastrado.");
        }
        List<String> faltando = new ArrayList<>();
        if (umidadeSolo == null) {
            faltando.add("umidade do solo");
        }
        if (temperatura == null) {
            faltando.add("temperatura");
        }
        if (umidadeAr == null) {
            faltando.add("umidade do ar");
        }
        if (precipitacao24h == null) {
            faltando.add("precipitação");
        }
        if (!faltando.isEmpty()) {
            throw new ValidacaoException("Dados insuficientes: preencha os quatro indicadores para registrar e calcular o risco. Faltando: "
                    + String.join(", ", faltando) + ".");
        }
        LocalDateTime agora = LocalDateTime.now(relogio);
        if (dataHora != null && dataHora.isAfter(agora.plusMinutes(5))) {
            throw new ValidacaoException("A data e hora do registro não pode estar no futuro.");
        }
        IndicadorAmbiental registro = new IndicadorAmbiental(talhaoId, umidadeSolo, temperatura, umidadeAr,
                precipitacao24h, dataHora, fonte);
        return indicadores.salvar(registro);
    }

    /** Variante que recebe o texto digitado nos campos da interface. */
    public IndicadorAmbiental registrarTexto(long talhaoId, String umidadeSolo, String temperatura,
                                             String umidadeAr, String precipitacao24h, String dataHora) {
        Double solo = ConversorEntrada.numeroOpcional(umidadeSolo, "Umidade do solo");
        Double temp = ConversorEntrada.numeroOpcional(temperatura, "Temperatura");
        Double ar = ConversorEntrada.numeroOpcional(umidadeAr, "Umidade do ar");
        Double prec = ConversorEntrada.numeroOpcional(precipitacao24h, "Precipitação (24 h)");
        LocalDateTime quando = ConversorEntrada.dataHora(dataHora);
        return registrar(talhaoId, solo, temp, ar, prec, quando);
    }

    /** Histórico do talhão em ordem cronológica. */
    public List<IndicadorAmbiental> listarPorTalhao(long talhaoId) {
        return indicadores.listarPorTalhao(talhaoId);
    }

    public Optional<IndicadorAmbiental> ultimo(long talhaoId) {
        return indicadores.ultimoDoTalhao(talhaoId);
    }

    public List<IndicadorAmbiental> listarTodos() {
        return indicadores.listar();
    }
}
