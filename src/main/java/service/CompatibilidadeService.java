package service;

import domain.EstacaoMeteorologica;
import domain.IndicadorAmbiental;
import domain.LeituraClimaticaDiaria;
import domain.Propriedade;
import domain.RequisitoCultura;
import domain.Talhao;
import infra.IndicadorRepository;
import infra.PropriedadeRepository;
import infra.TalhaoRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

/**
 * Módulo DEMONSTRATIVO de compatibilidade de culturas por município (alinhamento ao pré-projeto).
 * <p>
 * Fonte do clima, em ordem de preferência:
 * <ol>
 *   <li><b>estação do INMET do município</b> (clima real jan–out/2026: temperatura, umidade do ar e chuva).
 *       A umidade do solo não é considerada, porque o INMET não a mede;</li>
 *   <li>médias dos indicadores registrados nos talhões do município (inclui umidade do solo).</li>
 * </ol>
 * As médias são comparadas com uma tabela <b>SIMULADA</b> de faixas por cultura. Não é recomendação
 * agronômica; pH, textura e fertilidade do solo não fazem parte desta versão.
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

    /** Médias usadas na comparação. {@code umidadeSolo} é {@code null} quando a fonte é o INMET. */
    public record PerfilAmbiental(double temperatura, Double umidadeSolo, double umidadeAr, double precipitacao) {
    }

    /** Resultado da comparação para uma cultura. */
    public record CompatibilidadeCultura(String cultura, boolean compativel, List<String> divergencias) {
    }

    /** Resultado completo da avaliação de um município. */
    public record Avaliacao(String municipio, int quantidadeRegistros, boolean dadosSuficientes, String mensagem,
                            PerfilAmbiental perfil, List<CompatibilidadeCultura> culturas, String fonte) {
    }

    private final PropriedadeRepository propriedades;
    private final TalhaoRepository talhoes;
    private final IndicadorRepository indicadores;
    private final InmetService inmet;

    /**
     * @param inmet base do INMET; {@code null} para usar apenas os indicadores registrados
     */
    public CompatibilidadeService(PropriedadeRepository propriedades, TalhaoRepository talhoes,
                                  IndicadorRepository indicadores, InmetService inmet) {
        this.propriedades = propriedades;
        this.talhoes = talhoes;
        this.indicadores = indicadores;
        this.inmet = inmet;
    }

    /** Municípios das propriedades cadastradas, em ordem alfabética. */
    public List<String> municipios() {
        Set<String> nomes = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        propriedades.listar().forEach(p -> nomes.add(p.getMunicipio()));
        return new ArrayList<>(nomes);
    }

    public Avaliacao avaliar(String municipio) {
        Optional<EstacaoMeteorologica> estacao = inmet == null ? Optional.empty() : inmet.sugerirParaMunicipio(municipio);
        if (estacao.isPresent()) {
            List<LeituraClimaticaDiaria> dias = inmet.leiturasCompletas(estacao.get().codigo());
            if (dias.size() >= MINIMO_REGISTROS) {
                PerfilAmbiental perfil = new PerfilAmbiental(
                        dias.stream().mapToDouble(LeituraClimaticaDiaria::tempMedia).average().orElse(0), null,
                        dias.stream().mapToDouble(LeituraClimaticaDiaria::umidadeAr).average().orElse(0),
                        dias.stream().mapToDouble(LeituraClimaticaDiaria::chuvaMm).average().orElse(0));
                String fonte = "Clima real do INMET — estação " + estacao.get().descricao() + ", " + dias.size()
                        + " dias (jan–out/2026). Umidade do solo não considerada: o INMET não a mede.";
                return comparar(municipio, dias.size(), perfil, fonte);
            }
        }
        return avaliarPorRegistros(municipio);
    }

    private Avaliacao avaliarPorRegistros(String municipio) {
        List<IndicadorAmbiental> registros = new ArrayList<>();
        for (Propriedade p : propriedades.listar()) {
            if (!p.getMunicipio().equalsIgnoreCase(municipio == null ? "" : municipio.trim())) {
                continue;
            }
            for (Talhao t : talhoes.listarPorPropriedade(p.getId())) {
                registros.addAll(indicadores.listarPorTalhao(t.getId()));
            }
        }
        String fonte = "Médias dos indicadores registrados nos talhões do município (município sem estação do INMET).";
        if (registros.size() < MINIMO_REGISTROS) {
            return new Avaliacao(municipio, registros.size(), false,
                    "Dados insuficientes: o município não tem estação do INMET e são necessários pelo menos "
                            + MINIMO_REGISTROS + " registros de indicadores em talhões deste município (encontrados: "
                            + registros.size() + ").",
                    null, List.of(), fonte);
        }
        PerfilAmbiental perfil = new PerfilAmbiental(
                registros.stream().mapToDouble(IndicadorAmbiental::getTemperatura).average().orElse(0),
                registros.stream().mapToDouble(IndicadorAmbiental::getUmidadeSolo).average().orElse(0),
                registros.stream().mapToDouble(IndicadorAmbiental::getUmidadeAr).average().orElse(0),
                registros.stream().mapToDouble(IndicadorAmbiental::getPrecipitacao24h).average().orElse(0));
        return comparar(municipio, registros.size(), perfil, fonte);
    }

    private Avaliacao comparar(String municipio, int quantidade, PerfilAmbiental perfil, String fonte) {
        List<CompatibilidadeCultura> resultado = new ArrayList<>();
        for (RequisitoCultura req : CATALOGO_SIMULADO) {
            List<String> div = new ArrayList<>();
            verificar(div, "Temperatura média", perfil.temperatura(), req.temperaturaMin(), req.temperaturaMax(), "°C");
            if (perfil.umidadeSolo() != null) {
                verificar(div, "Umidade do solo média", perfil.umidadeSolo(), req.umidadeSoloMin(), req.umidadeSoloMax(), "%");
            }
            verificar(div, "Umidade do ar média", perfil.umidadeAr(), req.umidadeArMin(), req.umidadeArMax(), "%");
            verificar(div, "Precipitação média (24 h)", perfil.precipitacao(), req.precipitacaoMin(), req.precipitacaoMax(), "mm");
            resultado.add(new CompatibilidadeCultura(req.cultura(), div.isEmpty(), div));
        }
        long compativeis = resultado.stream().filter(CompatibilidadeCultura::compativel).count();
        String msg = compativeis == 0
                ? "Nenhuma cultura da base simulada é compatível com as médias do município."
                : compativeis + " cultura(s) potencialmente compatível(is) segundo a base simulada.";
        return new Avaliacao(municipio, quantidade, true, msg, perfil, resultado, fonte);
    }

    private static void verificar(List<String> div, String nome, double valor, double min, double max, String un) {
        if (valor < min || valor > max) {
            div.add(String.format(Locale.forLanguageTag("pt-BR"), "%s %.1f %s fora da faixa simulada (%.0f–%.0f %s)",
                    nome, valor, un, min, max, un));
        }
    }
}
