package infra;

import domain.EstacaoMeteorologica;
import domain.LeituraClimaticaDiaria;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Leitura (somente consulta) da base real do INMET já tratada:
 * {@code estacoes.csv} (catálogo) e {@code diario_consolidado.csv} (um dia por linha e estação).
 * <p>
 * Os arquivos ficam em {@code src/main/resources/inmet/} e são lidos uma única vez por execução.
 */
public class InmetRepository {

    public static final String PASTA_RECURSOS = "/inmet/";

    private static InmetRepository padrao;

    private final Map<String, EstacaoMeteorologica> estacoes = new LinkedHashMap<>();
    private final Map<String, NavigableMap<LocalDate, LeituraClimaticaDiaria>> leituras = new HashMap<>();

    /** Carrega a partir de leitores de texto (usado pelos testes e pelo carregamento padrão). */
    public InmetRepository(Reader catalogoEstacoes, Reader diario) {
        try {
            carregarEstacoes(catalogoEstacoes);
            carregarDiario(diario);
        } catch (IOException | RuntimeException e) {
            throw new PersistenciaException("Não foi possível ler a base do INMET: " + e.getMessage(), e);
        }
    }

    /** Base completa embutida no programa (carregada na primeira chamada e reaproveitada). */
    public static synchronized InmetRepository padrao() {
        if (padrao == null) {
            padrao = new InmetRepository(abrir("estacoes.csv"), abrir("diario_consolidado.csv"));
        }
        return padrao;
    }

    private static Reader abrir(String arquivo) {
        InputStream in = InmetRepository.class.getResourceAsStream(PASTA_RECURSOS + arquivo);
        if (in == null) {
            throw new PersistenciaException("Arquivo da base INMET não encontrado: " + PASTA_RECURSOS + arquivo, null);
        }
        return new InputStreamReader(in, StandardCharsets.UTF_8);
    }

    /** Estações em ordem de UF e nome. */
    public List<EstacaoMeteorologica> listarEstacoes() {
        List<EstacaoMeteorologica> lista = new ArrayList<>(estacoes.values());
        lista.sort(Comparator.comparing(EstacaoMeteorologica::uf).thenComparing(EstacaoMeteorologica::nome));
        return lista;
    }

    public Optional<EstacaoMeteorologica> buscarEstacao(String codigo) {
        return codigo == null ? Optional.empty() : Optional.ofNullable(estacoes.get(codigo.trim().toUpperCase()));
    }

    public Optional<LeituraClimaticaDiaria> leitura(String codigo, LocalDate data) {
        NavigableMap<LocalDate, LeituraClimaticaDiaria> mapa = leituras.get(codigo);
        return mapa == null ? Optional.empty() : Optional.ofNullable(mapa.get(data));
    }

    /** Leituras da estação entre as datas (inclusive), em ordem cronológica. */
    public List<LeituraClimaticaDiaria> leituras(String codigo, LocalDate inicio, LocalDate fim) {
        NavigableMap<LocalDate, LeituraClimaticaDiaria> mapa = leituras.get(codigo);
        if (mapa == null || inicio.isAfter(fim)) {
            return List.of();
        }
        return new ArrayList<>(mapa.subMap(inicio, true, fim, true).values());
    }

    /** Todas as leituras da estação. */
    public List<LeituraClimaticaDiaria> leituras(String codigo) {
        NavigableMap<LocalDate, LeituraClimaticaDiaria> mapa = leituras.get(codigo);
        return mapa == null ? List.of() : Collections.unmodifiableList(new ArrayList<>(mapa.values()));
    }

    public int totalLeituras() {
        return leituras.values().stream().mapToInt(Map::size).sum();
    }

    // ------------------------------------------------------------------ leitura dos CSV

    private void carregarEstacoes(Reader reader) throws IOException {
        try (BufferedReader in = new BufferedReader(reader)) {
            Map<String, Integer> col = cabecalho(in.readLine());
            String linha;
            while ((linha = in.readLine()) != null) {
                if (linha.isBlank()) {
                    continue;
                }
                List<String> c = dividir(linha);
                String codigo = c.get(col.get("estacao")).trim().toUpperCase();
                estacoes.put(codigo, new EstacaoMeteorologica(codigo, c.get(col.get("nome")).trim(),
                        c.get(col.get("uf")).trim(), c.get(col.get("regiao")).trim(),
                        numero(c.get(col.get("latitude")), 0), numero(c.get(col.get("longitude")), 0),
                        numero(c.get(col.get("altitude_m")), 0), numero(c.get(col.get("cobertura_pct")), 0)));
            }
        }
    }

    private void carregarDiario(Reader reader) throws IOException {
        try (BufferedReader in = new BufferedReader(reader)) {
            Map<String, Integer> col = cabecalho(in.readLine());
            String linha;
            while ((linha = in.readLine()) != null) {
                if (linha.isBlank()) {
                    continue;
                }
                List<String> c = dividir(linha);
                LocalDate data;
                try {
                    data = LocalDate.parse(c.get(col.get("data")).trim());
                } catch (DateTimeParseException e) {
                    continue; // linha inválida é ignorada
                }
                String codigo = c.get(col.get("estacao")).trim().toUpperCase();
                LeituraClimaticaDiaria l = new LeituraClimaticaDiaria(codigo, data,
                        opcional(c, col, "temp_media"), opcional(c, col, "temp_min"),
                        opcional(c, col, "umidade_ar_media"), opcional(c, col, "chuva_mm"));
                leituras.computeIfAbsent(codigo, k -> new TreeMap<>()).put(data, l);
            }
        }
    }

    private static Map<String, Integer> cabecalho(String linha) {
        if (linha == null) {
            throw new IllegalStateException("arquivo vazio");
        }
        Map<String, Integer> mapa = new HashMap<>();
        List<String> nomes = dividir(linha.replace("﻿", ""));
        for (int i = 0; i < nomes.size(); i++) {
            mapa.put(nomes.get(i).trim(), i);
        }
        return mapa;
    }

    /** Divide uma linha CSV respeitando campos entre aspas (ex.: "1,2,3"). */
    static List<String> dividir(String linha) {
        List<String> campos = new ArrayList<>();
        StringBuilder atual = new StringBuilder();
        boolean aspas = false;
        for (char ch : linha.toCharArray()) {
            if (ch == '"') {
                aspas = !aspas;
            } else if (ch == ',' && !aspas) {
                campos.add(atual.toString());
                atual.setLength(0);
            } else {
                atual.append(ch);
            }
        }
        campos.add(atual.toString());
        return campos;
    }

    private static Double opcional(List<String> c, Map<String, Integer> col, String nome) {
        Integer i = col.get(nome);
        if (i == null || i >= c.size() || c.get(i).isBlank()) {
            return null;
        }
        double v = Double.parseDouble(c.get(i).trim());
        return Double.isFinite(v) ? v : null;
    }

    private static double numero(String texto, double padrao) {
        try {
            return texto == null || texto.isBlank() ? padrao : Double.parseDouble(texto.trim());
        } catch (NumberFormatException e) {
            return padrao;
        }
    }
}
