package service;

import domain.IndicadorAmbiental;
import domain.Propriedade;
import domain.RequisitoCultura;
import domain.Talhao;
import infra.IndicadorRepository;
import infra.PropriedadeRepository;
import infra.TalhaoRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

/**
 * Módulo DEMONSTRATIVO de compatibilidade de culturas por município (alinhamento ao pré-projeto).
 * <p>
 * Compara a média dos indicadores já registrados nos talhões de um município com uma tabela
 * <b>SIMULADA</b> de faixas por cultura. Não é recomendação agronômica: as faixas são fictícias,
 * e características de solo como pH, textura e fertilidade não fazem parte desta versão
 * (apenas a umidade do solo registrada é considerada).
 */
public class CompatibilidadeService {

    public static final int MINIMO_REGISTROS = 3;

    public static final String AVISO = "Base de requisitos SIMULADA — uso acadêmico. Não representa recomendação agronômica.";

    /** Tabela simulada (temperatura °C, umidade do solo %, umidade do ar %, precipitação mm/24 h). */
    public static final List<RequisitoCultura> CATALOGO_SIMULADO = List.of(
            new RequisitoCultura("Milho", 18, 32, 40, 75, 40, 85, 3, 35),
            new RequisitoCultura("Soja", 20, 30, 45, 75, 45, 85, 4, 30),
            new RequisitoCultura("Café", 16, 26, 40, 70, 50, 85, 2, 25),
            new RequisitoCultura("Feijão", 18, 28, 40, 70, 45, 80, 3, 25),
            new RequisitoCultura("Trigo", 12, 24, 35, 65, 40, 75, 2, 20));

    /** Médias dos indicadores registrados em um município. */
    public record PerfilAmbiental(double temperatura, double umidadeSolo, double umidadeAr, double precipitacao) {
    }

    /** Resultado da comparação para uma cultura. */
    public record CompatibilidadeCultura(String cultura, boolean compativel, List<String> divergencias) {
    }

    /** Resultado completo da avaliação de um município. */
    public record Avaliacao(String municipio, int quantidadeRegistros, boolean dadosSuficientes, String mensagem,
                            PerfilAmbiental perfil, List<CompatibilidadeCultura> culturas) {
    }

    private final PropriedadeRepository propriedades;
    private final TalhaoRepository talhoes;
    private final IndicadorRepository indicadores;

    public CompatibilidadeService(PropriedadeRepository propriedades, TalhaoRepository talhoes,
                                  IndicadorRepository indicadores) {
        this.propriedades = propriedades;
        this.talhoes = talhoes;
        this.indicadores = indicadores;
    }

    /** Municípios das propriedades cadastradas, em ordem alfabética. */
    public List<String> municipios() {
        Set<String> nomes = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        propriedades.listar().forEach(p -> nomes.add(p.getMunicipio()));
        return new ArrayList<>(nomes);
    }

    public Avaliacao avaliar(String municipio) {
        List<IndicadorAmbiental> registros = new ArrayList<>();
        for (Propriedade p : propriedades.listar()) {
            if (!p.getMunicipio().equalsIgnoreCase(municipio == null ? "" : municipio.trim())) {
                continue;
            }
            for (Talhao t : talhoes.listarPorPropriedade(p.getId())) {
                registros.addAll(indicadores.listarPorTalhao(t.getId()));
            }
        }
        if (registros.size() < MINIMO_REGISTROS) {
            return new Avaliacao(municipio, registros.size(), false,
                    "Dados insuficientes: são necessários pelo menos " + MINIMO_REGISTROS
                            + " registros de indicadores em talhões deste município (encontrados: " + registros.size() + ").",
                    null, List.of());
        }
        PerfilAmbiental perfil = new PerfilAmbiental(
                registros.stream().mapToDouble(IndicadorAmbiental::getTemperatura).average().orElse(0),
                registros.stream().mapToDouble(IndicadorAmbiental::getUmidadeSolo).average().orElse(0),
                registros.stream().mapToDouble(IndicadorAmbiental::getUmidadeAr).average().orElse(0),
                registros.stream().mapToDouble(IndicadorAmbiental::getPrecipitacao24h).average().orElse(0));
        List<CompatibilidadeCultura> resultado = new ArrayList<>();
        for (RequisitoCultura req : CATALOGO_SIMULADO) {
            List<String> div = new ArrayList<>();
            verificar(div, "Temperatura média", perfil.temperatura(), req.temperaturaMin(), req.temperaturaMax(), "°C");
            verificar(div, "Umidade do solo média", perfil.umidadeSolo(), req.umidadeSoloMin(), req.umidadeSoloMax(), "%");
            verificar(div, "Umidade do ar média", perfil.umidadeAr(), req.umidadeArMin(), req.umidadeArMax(), "%");
            verificar(div, "Precipitação média (24 h)", perfil.precipitacao(), req.precipitacaoMin(), req.precipitacaoMax(), "mm");
            resultado.add(new CompatibilidadeCultura(req.cultura(), div.isEmpty(), div));
        }
        long compativeis = resultado.stream().filter(CompatibilidadeCultura::compativel).count();
        String msg = compativeis == 0
                ? "Nenhuma cultura da base simulada é compatível com as médias registradas."
                : compativeis + " cultura(s) potencialmente compatível(is) segundo a base simulada.";
        return new Avaliacao(municipio, registros.size(), true, msg, perfil, resultado);
    }

    private static void verificar(List<String> div, String nome, double valor, double min, double max, String un) {
        if (valor < min || valor > max) {
            div.add(String.format(Locale.forLanguageTag("pt-BR"), "%s %.1f %s fora da faixa simulada (%.0f–%.0f %s)",
                    nome, valor, un, min, max, un));
        }
    }
}
