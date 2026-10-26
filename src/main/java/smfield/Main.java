package smfield;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/** Interface de linha de comando do SMField (equivalente ao cli.py). */
public final class Main {
    private static final DateTimeFormatter BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final String ICONE_IDEAL = "\uD83D\uDFE2";          // verde
    private static final String ICONE_ACEITAVEL = "\uD83D\uDFE1";      // amarelo
    private static final String ICONE_DESFAVORAVEL = "\uD83D\uDD34";   // vermelho

    private static final String USO = String.join("\n",
            "uso: smfield <comando> [op\u00e7\u00f5es]",
            "",
            "comandos:",
            "  recomendar  --cultura NOME --regiao NOME [--ano AAAA]   melhores meses de plantio/colheita",
            "  agora       --regiao NOME [--mes 1-12] [--ano AAAA]     o que plantar em um m\u00eas",
            "  colheita    --cultura NOME --plantio AAAA-MM-DD         calcula a data de colheita",
            "  listar                                                  culturas e regi\u00f5es");

    /** Erro de uso da linha de comando (equivale ao exit code 2 do argparse). */
    private static final class UsoInvalido extends RuntimeException {
        private static final long serialVersionUID = 1L;

        UsoInvalido(String msg) {
            super(msg);
        }
    }

    private Main() {
    }

    public static void main(String[] args) {
        PrintStream out = new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8);
        PrintStream err = new PrintStream(new FileOutputStream(FileDescriptor.err), true, StandardCharsets.UTF_8);
        int codigo = executar(args, out, err);
        out.flush();
        err.flush();
        System.exit(codigo);
    }

    /** Executa um comando e devolve o codigo de saida (0 ok, 1 erro de dados, 2 erro de uso). */
    public static int executar(String[] args, PrintStream out, PrintStream err) {
        if (args.length == 0) {
            err.println(USO);
            return 2;
        }
        String cmd = args[0];
        if (cmd.equals("-h") || cmd.equals("--help") || cmd.equals("help")) {
            out.println(USO);
            return 0;
        }
        try {
            switch (cmd) {
                case "recomendar":
                    cmdRecomendar(opcoes(args, Set.of("cultura", "regiao", "ano"), Set.of("cultura", "regiao")), out);
                    break;
                case "agora":
                    cmdAgora(opcoes(args, Set.of("regiao", "mes", "ano"), Set.of("regiao")), out);
                    break;
                case "colheita":
                    cmdColheita(opcoes(args, Set.of("cultura", "plantio"), Set.of("cultura", "plantio")), out);
                    break;
                case "listar":
                    opcoes(args, Set.of(), Set.of());
                    cmdListar(out);
                    break;
                default:
                    throw new UsoInvalido("comando inv\u00e1lido: '" + cmd + "'");
            }
            return 0;
        } catch (UsoInvalido e) {
            err.println("smfield: erro: " + e.getMessage());
            err.println(USO);
            return 2;
        } catch (NoSuchElementException | IllegalArgumentException | DateTimeException e) {
            err.println("Erro: " + e.getMessage());
            return 1;
        }
    }

    // ---------------------------------------------------------------- comandos

    private static void cmdRecomendar(Map<String, String> a, PrintStream out) {
        Cultura c = DadosLoader.buscarCultura(a.get("cultura"));
        Regiao r = DadosLoader.buscarRegiao(a.get("regiao"));
        int ano = inteiro(a, "ano", LocalDate.now().getYear());
        out.println();
        out.println(c.nome() + " em " + r.nome() + "/" + r.uf() + " \u2014 ciclo de " + c.cicloDias() + " dias");
        List<String> jan = Calendario.janelasRecomendadas(c, r, ano);
        out.println("Melhores per\u00edodos de plantio: "
                + (jan.isEmpty() ? "nenhum (considere irriga\u00e7\u00e3o/estufa)" : String.join(", ", jan)));
        out.println();
        out.println(String.format("%-5s%6s  %-17s%s", "M\u00eas", "Nota", "Situa\u00e7\u00e3o", "Colheita estimada"));
        List<Janela> janelas = Recomendador.recomendarPlantio(c, r, ano);
        janelas.sort((x, y) -> Integer.compare(x.mesPlantio(), y.mesPlantio()));
        for (Janela j : janelas) {
            out.println(String.format("%-5s%6s  %s %-14s%s",
                    Calendario.MESES[j.mesPlantio() - 1],
                    String.valueOf(j.nota()),
                    icone(j.classificacao()),
                    j.classificacao(),
                    j.colheitaEstimada().format(BR)));
        }
    }

    private static void cmdAgora(Map<String, String> a, PrintStream out) {
        Regiao r = DadosLoader.buscarRegiao(a.get("regiao"));
        int mes = inteiro(a, "mes", LocalDate.now().getMonthValue());
        int ano = inteiro(a, "ano", LocalDate.now().getYear());
        if (mes < 1 || mes > 12) {
            throw new IllegalArgumentException("m\u00eas deve estar entre 1 e 12");
        }
        out.println();
        out.println("O que plantar em " + Calendario.MESES[mes - 1] + " \u2014 " + r.nome() + "/" + r.uf());
        for (Sugestao s : Recomendador.oQuePlantar(r, DadosLoader.carregarCulturas(), mes, ano)) {
            out.println(String.format("  %s %-10s nota %5s  colheita ~%s",
                    icone(s.janela().classificacao()),
                    s.cultura().nome(),
                    String.valueOf(s.janela().nota()),
                    s.janela().colheitaEstimada().format(BR)));
        }
    }

    private static void cmdColheita(Map<String, String> a, PrintStream out) {
        Cultura c = DadosLoader.buscarCultura(a.get("cultura"));
        LocalDate plantio;
        try {
            plantio = LocalDate.parse(a.get("plantio"));
        } catch (DateTimeException e) {
            throw new IllegalArgumentException("data de plantio inv\u00e1lida (use AAAA-MM-DD): " + a.get("plantio"));
        }
        out.println(c.nome() + ": plantio " + plantio.format(BR)
                + " \u2192 colheita ~" + Calendario.dataColheita(plantio, c).format(BR));
    }

    private static void cmdListar(PrintStream out) {
        StringBuilder culturas = new StringBuilder();
        for (Cultura c : DadosLoader.carregarCulturas()) {
            culturas.append(culturas.length() == 0 ? "" : ", ").append(c.nome());
        }
        StringBuilder regioes = new StringBuilder();
        for (Regiao r : DadosLoader.carregarRegioes()) {
            regioes.append(regioes.length() == 0 ? "" : ", ").append(r.nome()).append('/').append(r.uf());
        }
        out.println("Culturas: " + culturas);
        out.println("Regi\u00f5es : " + regioes);
    }

    // ---------------------------------------------------------------- utilitarios

    private static String icone(String classificacao) {
        if (Recomendador.IDEAL.equals(classificacao)) {
            return ICONE_IDEAL;
        }
        if (Recomendador.ACEITAVEL.equals(classificacao)) {
            return ICONE_ACEITAVEL;
        }
        return ICONE_DESFAVORAVEL;
    }

    /** Le "--chave valor" ou "--chave=valor" e valida opcoes permitidas e obrigatorias. */
    private static Map<String, String> opcoes(String[] args, Set<String> permitidas, Set<String> obrigatorias) {
        Map<String, String> res = new LinkedHashMap<>();
        for (int i = 1; i < args.length; i++) {
            String token = args[i];
            if (!token.startsWith("--")) {
                throw new UsoInvalido("argumento n\u00e3o reconhecido: '" + token + "'");
            }
            String chave = token.substring(2);
            String valor;
            int igual = chave.indexOf('=');
            if (igual >= 0) {
                valor = chave.substring(igual + 1);
                chave = chave.substring(0, igual);
            } else {
                if (i + 1 >= args.length) {
                    throw new UsoInvalido("a op\u00e7\u00e3o --" + chave + " exige um valor");
                }
                valor = args[++i];
            }
            if (!permitidas.contains(chave)) {
                throw new UsoInvalido("op\u00e7\u00e3o n\u00e3o reconhecida: '--" + chave + "'");
            }
            res.put(chave, valor);
        }
        Set<String> faltando = new HashSet<>(obrigatorias);
        faltando.removeAll(res.keySet());
        if (!faltando.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (String o : obrigatorias) {
                if (faltando.contains(o)) {
                    sb.append(sb.length() == 0 ? "" : ", ").append("--").append(o);
                }
            }
            throw new UsoInvalido("s\u00e3o obrigat\u00f3rios os argumentos: " + sb);
        }
        return res;
    }

    private static int inteiro(Map<String, String> a, String chave, int padrao) {
        String v = a.get(chave);
        if (v == null) {
            return padrao;
        }
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            throw new UsoInvalido("argumento --" + chave + ": valor inteiro inv\u00e1lido: '" + v + "'");
        }
    }
}
