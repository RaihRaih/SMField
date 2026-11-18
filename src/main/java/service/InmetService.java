package service;

import domain.EstacaoMeteorologica;
import domain.IndicadorAmbiental;
import domain.LeituraClimaticaDiaria;
import domain.ResultadoRisco;
import domain.Talhao;
import domain.ValidacaoException;
import infra.InmetRepository;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Integra a base REAL do INMET (estações automáticas, jan–out/2026) ao SMField.
 * <p>
 * Temperatura média, umidade relativa do ar e chuva do dia são <b>dados reais</b>. As estações do INMET
 * não medem umidade do solo; por isso ela é <b>SIMULADA</b> por uma fórmula acadêmica a partir da
 * chuva e da umidade do ar dos últimos 7 dias (ver {@link #umidadeSoloSimulada}). O cálculo do risco
 * continua sendo o modelo acadêmico do {@link RiscoService}.
 */
public class InmetService {

    /** Máximo de dias importados de uma vez (cerca de um trimestre). */
    public static final int LIMITE_DIAS = 92;
    /** Horário atribuído aos registros diários importados. */
    public static final LocalTime HORARIO_REGISTRO = LocalTime.NOON;
    public static final String AVISO = "Temperatura, umidade do ar e chuva: dados reais do INMET (estações automáticas, "
            + "jan–out/2026). Umidade do solo: SIMULADA por fórmula acadêmica, pois o INMET não a mede. "
            + "O índice de risco segue o modelo acadêmico do SMField.";

    /** Situação de um dia na prévia da importação. */
    public enum Situacao {
        PRONTO("Pronto para importar"),
        JA_IMPORTADO("Já importado neste talhão"),
        INCOMPLETO("Dia sem dados completos no INMET");

        private final String descricao;

        Situacao(String descricao) {
            this.descricao = descricao;
        }

        @Override
        public String toString() {
            return descricao;
        }
    }

    /** Um dia da prévia: leitura real, umidade do solo simulada e risco que seria calculado. */
    public record DiaInmet(LocalDate data, LeituraClimaticaDiaria leitura, Double umidadeSoloSimulada,
                           ResultadoRisco resultado, Situacao situacao) {
    }

    /** Resumo de uma importação. */
    public record ResultadoImportacao(int importados, int jaExistentes, int incompletos) {
    }

    /** Ordem de preferência: maior cobertura; no empate, o menor código (estações "A" são as principais). */
    public static final Comparator<EstacaoMeteorologica> MAIS_DADOS = Comparator
            .comparingDouble(EstacaoMeteorologica::coberturaPct)
            .thenComparing(EstacaoMeteorologica::codigo, Comparator.reverseOrder());

    private final Supplier<InmetRepository> repositorio;
    private final TalhaoService talhoes;
    private final IndicadorService indicadores;
    private final RiscoService risco;
    private InmetRepository carregado;

    public InmetService(Supplier<InmetRepository> repositorio, TalhaoService talhoes,
                        IndicadorService indicadores, RiscoService risco) {
        this.repositorio = repositorio;
        this.talhoes = talhoes;
        this.indicadores = indicadores;
        this.risco = risco;
    }

    private synchronized InmetRepository base() {
        if (carregado == null) {
            carregado = repositorio.get();
        }
        return carregado;
    }

    // ------------------------------------------------------------------ estações

    public List<EstacaoMeteorologica> estacoes() {
        return base().listarEstacoes();
    }

    /**
     * Busca por código, nome ou UF, sem diferenciar acentos e maiúsculas. Todas as palavras do filtro
     * precisam aparecer (ex.: "Sorriso/MT" ou "rio verde go"). Filtro vazio lista todas.
     */
    public List<EstacaoMeteorologica> buscarEstacoes(String filtro) {
        String[] palavras = normalizar(filtro).replaceAll("[/\\-]", " ").trim().split("\\s+");
        if (palavras.length == 1 && palavras[0].isEmpty()) {
            return estacoes();
        }
        return estacoes().stream()
                .filter(e -> {
                    String texto = normalizar(e.codigo() + " " + e.nome() + " " + e.uf());
                    for (String p : palavras) {
                        if (!texto.contains(p)) {
                            return false;
                        }
                    }
                    return true;
                })
                .toList();
    }

    /**
     * Estação cujo nome começa com o município (ex.: "Belo Horizonte" → A521). Aceita a UF junto
     * ("Sorriso/MT" ou "Sorriso - MT"), que então também precisa coincidir. Prioriza maior cobertura.
     */
    public Optional<EstacaoMeteorologica> sugerirParaMunicipio(String municipio) {
        if (municipio == null || municipio.isBlank()) {
            return Optional.empty();
        }
        String[] partes = municipio.split("/| - ");
        String nome = normalizar(partes[0]);
        String uf = partes.length > 1 ? partes[1].trim() : null;
        if (nome.isEmpty()) {
            return Optional.empty();
        }
        return estacoes().stream()
                .filter(e -> normalizar(e.nome()).startsWith(nome))
                .filter(e -> uf == null || uf.isEmpty() || e.uf().equalsIgnoreCase(uf))
                .max(MAIS_DADOS);
    }

    public EstacaoMeteorologica obterEstacao(String codigo) {
        return base().buscarEstacao(codigo)
                .orElseThrow(() -> new ValidacaoException("Estação INMET não encontrada: " + codigo + "."));
    }

    /** Primeiro e último dia com dados completos da estação, ou vazio se não houver nenhum. */
    public Optional<LocalDate[]> periodoDisponivel(String codigo) {
        List<LeituraClimaticaDiaria> completas = base().leituras(codigo).stream()
                .filter(LeituraClimaticaDiaria::completa).toList();
        if (completas.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new LocalDate[]{completas.get(0).data(), completas.get(completas.size() - 1).data()});
    }

    /** Todos os dias com temperatura, umidade do ar e chuva disponíveis na estação. */
    public List<LeituraClimaticaDiaria> leiturasCompletas(String codigo) {
        return base().leituras(codigo).stream().filter(LeituraClimaticaDiaria::completa).toList();
    }

    public Optional<LeituraClimaticaDiaria> leitura(String codigo, LocalDate data) {
        return base().leitura(codigo, data);
    }

    // ------------------------------------------------------------------ umidade do solo simulada

    /**
     * Umidade do solo SIMULADA (fórmula acadêmica, não medida):
     * <pre>
     * solo = 20 + 0,8 × Σ (chuva do dia d−i × 0,75^i), i = 0..6
     *           + 0,3 × (média da umidade do ar dos 7 dias − 60)
     *           − max(0, temperatura média do dia − 25)
     * </pre>
     * limitada a 5–95 % e arredondada a 1 casa. A chuva recente pesa mais (fator 0,75 por dia); ar
     * úmido aumenta e calor reduz a umidade estimada. Dias sem dado são desconsiderados.
     *
     * @return {@code null} se o próprio dia não tiver leitura completa
     */
    public Double umidadeSoloSimulada(String codigo, LocalDate data) {
        LeituraClimaticaDiaria dia = base().leitura(codigo, data).orElse(null);
        if (dia == null || !dia.completa()) {
            return null;
        }
        double chuvaPonderada = 0;
        double somaUmidade = 0;
        int diasUmidade = 0;
        for (int i = 0; i < 7; i++) {
            LeituraClimaticaDiaria l = base().leitura(codigo, data.minusDays(i)).orElse(null);
            if (l == null) {
                continue;
            }
            if (l.chuvaMm() != null) {
                chuvaPonderada += l.chuvaMm() * Math.pow(0.75, i);
            }
            if (l.umidadeAr() != null) {
                somaUmidade += l.umidadeAr();
                diasUmidade++;
            }
        }
        double mediaUmidade = somaUmidade / diasUmidade; // diasUmidade >= 1: o próprio dia está completo
        double solo = 20 + 0.8 * chuvaPonderada + 0.3 * (mediaUmidade - 60) - Math.max(0, dia.tempMedia() - 25);
        solo = Math.max(5, Math.min(95, solo));
        return Math.round(solo * 10) / 10.0;
    }

    // ------------------------------------------------------------------ prévia e importação

    /** Prévia dia a dia do período, sem gravar nada. */
    public List<DiaInmet> previa(long talhaoId, String codigo, LocalDate inicio, LocalDate fim) {
        validarPeriodo(inicio, fim);
        obterEstacao(codigo);
        String fonte = fonte(codigo);
        List<IndicadorAmbiental> existentes = talhaoId > 0 ? indicadores.listarPorTalhao(talhaoId) : List.of();
        List<DiaInmet> dias = new ArrayList<>();
        for (LocalDate d = inicio; !d.isAfter(fim); d = d.plusDays(1)) {
            LeituraClimaticaDiaria l = base().leitura(codigo, d).orElse(null);
            if (l == null || !l.completa()) {
                dias.add(new DiaInmet(d, l, null, ResultadoRisco.naoCalculado("Dia sem dados completos no INMET."),
                        Situacao.INCOMPLETO));
                continue;
            }
            Double solo = umidadeSoloSimulada(codigo, d);
            ResultadoRisco r = risco.calcular(solo, l.tempMedia(), l.umidadeAr(), l.chuvaMm());
            LocalDateTime quando = d.atTime(HORARIO_REGISTRO);
            boolean existe = existentes.stream()
                    .anyMatch(i -> i.getDataHora().equals(quando) && i.getFonte().equals(fonte));
            dias.add(new DiaInmet(d, l, solo, r, existe ? Situacao.JA_IMPORTADO : Situacao.PRONTO));
        }
        return dias;
    }

    /**
     * Grava como indicadores do talhão os dias completos do período. Dias já importados da mesma
     * estação são ignorados (não duplica) e dias incompletos no INMET são pulados.
     */
    public ResultadoImportacao importar(long talhaoId, String codigo, LocalDate inicio, LocalDate fim) {
        talhoes.obter(talhaoId);
        List<DiaInmet> dias = previa(talhaoId, codigo, inicio, fim);
        String fonte = fonte(codigo);
        int importados = 0;
        int existentes = 0;
        int incompletos = 0;
        for (DiaInmet d : dias) {
            switch (d.situacao()) {
                case INCOMPLETO -> incompletos++;
                case JA_IMPORTADO -> existentes++;
                case PRONTO -> {
                    LeituraClimaticaDiaria l = d.leitura();
                    indicadores.registrar(talhaoId, d.umidadeSoloSimulada(), l.tempMedia(), l.umidadeAr(),
                            l.chuvaMm(), d.data().atTime(HORARIO_REGISTRO), fonte);
                    importados++;
                }
            }
        }
        return new ResultadoImportacao(importados, existentes, incompletos);
    }

    /** Texto gravado no campo "fonte" dos registros importados. */
    public String fonte(String codigo) {
        EstacaoMeteorologica e = obterEstacao(codigo);
        return IndicadorAmbiental.PREFIXO_INMET + " " + e.codigo() + " (" + e.nome() + "/" + e.uf()
                + ") · umidade do solo SIMULADA";
    }

    private static void validarPeriodo(LocalDate inicio, LocalDate fim) {
        if (inicio == null || fim == null) {
            throw new ValidacaoException("Informe a data inicial e a data final.");
        }
        if (inicio.isAfter(fim)) {
            throw new ValidacaoException("A data inicial deve ser anterior ou igual à data final.");
        }
        long dias = ChronoUnit.DAYS.between(inicio, fim) + 1;
        if (dias > LIMITE_DIAS) {
            throw new ValidacaoException("Escolha um período de até " + LIMITE_DIAS + " dias (informado: " + dias + ").");
        }
    }

    static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        String semAcento = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return semAcento.toLowerCase(Locale.ROOT).trim().replaceAll("\\s+", " ");
    }
}
