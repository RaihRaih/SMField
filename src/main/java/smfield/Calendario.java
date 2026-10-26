package smfield;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/** Calendario agricola: colheita estimada e agrupamento de meses em janelas. */
public final class Calendario {
    public static final String[] MESES =
            {"jan", "fev", "mar", "abr", "mai", "jun", "jul", "ago", "set", "out", "nov", "dez"};

    private Calendario() {
    }

    public static LocalDate dataColheita(LocalDate plantio, Cultura cultura) {
        return plantio.plusDays(cultura.cicloDias());
    }

    /** [10, 11, 12, 1] -> [[10, 11, 12, 1]] (trata a virada de ano). */
    public static List<List<Integer>> agruparMeses(List<Integer> meses) {
        List<Integer> ordenados = new ArrayList<>(new TreeSet<>(meses));
        List<List<Integer>> grupos = new ArrayList<>();
        if (ordenados.isEmpty()) {
            return grupos;
        }
        grupos.add(new ArrayList<>(List.of(ordenados.get(0))));
        for (int k = 1; k < ordenados.size(); k++) {
            int m = ordenados.get(k);
            List<Integer> ultimo = grupos.get(grupos.size() - 1);
            if (m == ultimo.get(ultimo.size() - 1) + 1) {
                ultimo.add(m);
            } else {
                grupos.add(new ArrayList<>(List.of(m)));
            }
        }
        List<Integer> primeiro = grupos.get(0);
        List<Integer> ultimo = grupos.get(grupos.size() - 1);
        if (grupos.size() > 1 && primeiro.get(0) == 1 && ultimo.get(ultimo.size() - 1) == 12) {
            ultimo.addAll(grupos.remove(0));
        }
        return grupos;
    }

    private static String rotulo(List<Integer> grupo) {
        String ini = MESES[grupo.get(0) - 1];
        if (grupo.size() == 1) {
            return ini;
        }
        return ini + "\u2013" + MESES[grupo.get(grupo.size() - 1) - 1];
    }

    /** Retorna rotulos como ["out\u2013nov"] com os periodos de plantio recomendados. */
    public static List<String> janelasRecomendadas(Cultura cultura, Regiao regiao, int ano, double notaMinima) {
        List<Integer> bons = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            if (Recomendador.avaliarMes(cultura, regiao, m, ano).nota() >= notaMinima) {
                bons.add(m);
            }
        }
        List<String> rotulos = new ArrayList<>();
        for (List<Integer> g : agruparMeses(bons)) {
            rotulos.add(rotulo(g));
        }
        return rotulos;
    }

    public static List<String> janelasRecomendadas(Cultura cultura, Regiao regiao, int ano) {
        return janelasRecomendadas(cultura, regiao, ano, 75);
    }
}
