package smfield;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/** Carrega e valida os dados de culturas e regioes (arquivos JSON em src/main/resources/dados). */
public final class DadosLoader {
    private static final String RECURSO_CULTURAS = "/dados/culturas.json";
    private static final String RECURSO_REGIOES = "/dados/regioes.json";

    private DadosLoader() {
    }

    /** Remove acentos, espacos nas pontas e converte para minusculas. */
    public static String normalizar(String texto) {
        String semAcento = Normalizer.normalize(texto, Normalizer.Form.NFKD).replaceAll("[^\\p{ASCII}]", "");
        return semAcento.strip().toLowerCase(Locale.ROOT);
    }

    // ---------------------------------------------------------------- culturas

    public static List<Cultura> carregarCulturas() {
        return carregarCulturasDeTexto(lerRecurso(RECURSO_CULTURAS));
    }

    public static List<Cultura> carregarCulturas(Path caminho) {
        return carregarCulturasDeTexto(lerArquivo(caminho));
    }

    public static List<Cultura> carregarCulturasDeTexto(String json) {
        List<Cultura> culturas = new ArrayList<>();
        for (Object o : lista(Json.parse(json), "culturas")) {
            Map<String, Object> m = objeto(o, "cultura");
            Cultura c = new Cultura(
                    texto(m, "nome"),
                    (int) numero(m, "ciclo_dias"),
                    numero(m, "temp_ideal_min"),
                    numero(m, "temp_ideal_max"),
                    numero(m, "temp_abs_min"),
                    numero(m, "temp_abs_max"),
                    numero(m, "chuva_min_mm"),
                    numero(m, "chuva_max_mm"),
                    booleano(m, "sensivel_geada"));
            if (c.cicloDias() <= 0) {
                throw new IllegalArgumentException("Ciclo inv\u00e1lido para " + c.nome());
            }
            if (!(c.tempAbsMin() < c.tempIdealMin() && c.tempIdealMin() <= c.tempIdealMax()
                    && c.tempIdealMax() < c.tempAbsMax())) {
                throw new IllegalArgumentException("Faixas de temperatura inconsistentes para " + c.nome());
            }
            culturas.add(c);
        }
        return culturas;
    }

    // ---------------------------------------------------------------- regioes

    public static List<Regiao> carregarRegioes() {
        return carregarRegioesDeTexto(lerRecurso(RECURSO_REGIOES));
    }

    public static List<Regiao> carregarRegioes(Path caminho) {
        return carregarRegioesDeTexto(lerArquivo(caminho));
    }

    public static List<Regiao> carregarRegioesDeTexto(String json) {
        List<Regiao> regioes = new ArrayList<>();
        for (Object o : lista(Json.parse(json), "regioes")) {
            Map<String, Object> r = objeto(o, "regiao");
            List<ClimaMes> meses = new ArrayList<>();
            for (Object mo : lista(r.get("meses"), "meses")) {
                Map<String, Object> m = objeto(mo, "mes");
                meses.add(new ClimaMes(numero(m, "temp_media"), numero(m, "chuva_mm"), numero(m, "prob_geada")));
            }
            String nome = texto(r, "nome");
            if (meses.size() != 12) {
                throw new IllegalArgumentException("Regi\u00e3o " + nome + " precisa de 12 meses de clima");
            }
            regioes.add(new Regiao(nome, texto(r, "uf"), meses));
        }
        return regioes;
    }

    // ---------------------------------------------------------------- busca

    public static Cultura buscarCultura(String nome) {
        return buscarCultura(nome, carregarCulturas());
    }

    public static Cultura buscarCultura(String nome, List<Cultura> culturas) {
        return buscar(culturas, Cultura::nome, nome);
    }

    public static Regiao buscarRegiao(String nome) {
        return buscarRegiao(nome, carregarRegioes());
    }

    public static Regiao buscarRegiao(String nome, List<Regiao> regioes) {
        return buscar(regioes, Regiao::nome, nome);
    }

    private static <T> T buscar(List<T> itens, java.util.function.Function<T, String> nomeDe, String nome) {
        String alvo = normalizar(nome);
        for (T item : itens) {
            if (normalizar(nomeDe.apply(item)).equals(alvo)) {
                return item;
            }
        }
        String opcoes = itens.stream().map(nomeDe).collect(Collectors.joining(", "));
        throw new NoSuchElementException("'" + nome + "' n\u00e3o encontrado. Op\u00e7\u00f5es: " + opcoes);
    }

    // ---------------------------------------------------------------- utilitarios

    private static String lerRecurso(String recurso) {
        try (InputStream in = DadosLoader.class.getResourceAsStream(recurso)) {
            if (in == null) {
                throw new IllegalStateException("Arquivo de dados n\u00e3o encontrado: " + recurso);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String lerArquivo(Path caminho) {
        try {
            return Files.readString(caminho, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static List<Object> lista(Object o, String que) {
        if (!(o instanceof List)) {
            throw new IllegalArgumentException("Esperada uma lista em '" + que + "'");
        }
        return (List<Object>) o;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> objeto(Object o, String que) {
        if (!(o instanceof Map)) {
            throw new IllegalArgumentException("Esperado um objeto em '" + que + "'");
        }
        return (Map<String, Object>) o;
    }

    private static double numero(Map<String, Object> m, String chave) {
        Object v = m.get(chave);
        if (!(v instanceof Double)) {
            throw new IllegalArgumentException("Campo num\u00e9rico ausente ou inv\u00e1lido: " + chave);
        }
        return (Double) v;
    }

    private static String texto(Map<String, Object> m, String chave) {
        Object v = m.get(chave);
        if (!(v instanceof String)) {
            throw new IllegalArgumentException("Campo de texto ausente ou inv\u00e1lido: " + chave);
        }
        return (String) v;
    }

    private static boolean booleano(Map<String, Object> m, String chave) {
        Object v = m.get(chave);
        if (!(v instanceof Boolean)) {
            throw new IllegalArgumentException("Campo booleano ausente ou inv\u00e1lido: " + chave);
        }
        return (Boolean) v;
    }
}
