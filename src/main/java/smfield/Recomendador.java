package smfield;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Motor de recomendacao: nota de 0 a 100 para cada mes de plantio. */
public final class Recomendador {
    public static final String IDEAL = "ideal";
    public static final String ACEITAVEL = "aceit\u00e1vel";
    public static final String DESFAVORAVEL = "desfavor\u00e1vel";

    /** Um mes coberto pelo ciclo; peso = fracao de 30 dias. */
    private record ParteDoCiclo(int indiceMes, double peso) {
    }

    private Recomendador() {
    }

    private static double notaTempMes(double t, Cultura c) {
        if (c.tempIdealMin() <= t && t <= c.tempIdealMax()) {
            return 1.0;
        }
        if (t < c.tempIdealMin()) {
            return Math.max(0.0, (t - c.tempAbsMin()) / (c.tempIdealMin() - c.tempAbsMin()));
        }
        return Math.max(0.0, (c.tempAbsMax() - t) / (c.tempAbsMax() - c.tempIdealMax()));
    }

    private static double notaChuva(double total, Cultura c) {
        if (total <= 0) {
            return 0.0;
        }
        if (total < c.chuvaMinMm()) {
            return total / c.chuvaMinMm();
        }
        if (total > c.chuvaMaxMm()) {
            return c.chuvaMaxMm() / total;
        }
        return 1.0;
    }

    /** Fracao (0-1) dos 2/3 iniciais do ciclo com chuva abaixo de 75% da necessidade mensal. */
    private static double fracaoSecaInicial(List<ParteDoCiclo> partes, Regiao regiao, Cultura cultura) {
        double somaPesos = partes.stream().mapToDouble(ParteDoCiclo::peso).sum();
        double necessidadeMensal = cultura.chuvaMinMm() / somaPesos;
        int limiteIdx = (int) Math.ceil(partes.size() * 2.0 / 3.0);
        List<ParteDoCiclo> iniciais = partes.subList(0, limiteIdx);
        double secos = 0;
        double totalIniciais = 0;
        for (ParteDoCiclo p : iniciais) {
            totalIniciais += p.peso();
            if (regiao.meses().get(p.indiceMes()).chuvaMm() < 0.75 * necessidadeMensal) {
                secos += p.peso();
            }
        }
        return secos / totalIniciais;
    }

    private static List<ParteDoCiclo> mesesDoCiclo(int mesPlantio, int cicloDias) {
        int n = (int) Math.ceil(cicloDias / 30.0);
        List<ParteDoCiclo> partes = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            partes.add(new ParteDoCiclo((mesPlantio - 1 + i) % 12, Math.min(30, cicloDias - 30 * i) / 30.0));
        }
        return partes;
    }

    public static String classificar(double nota) {
        if (nota >= 75) {
            return IDEAL;
        }
        if (nota >= 50) {
            return ACEITAVEL;
        }
        return DESFAVORAVEL;
    }

    /** Arredonda como o round() do Python (meio para o par, sobre o valor exato do double). */
    private static double arredondar(double valor, int casas) {
        return new BigDecimal(valor).setScale(casas, RoundingMode.HALF_EVEN).doubleValue();
    }

    public static Janela avaliarMes(Cultura cultura, Regiao regiao, int mes, int ano) {
        if (mes < 1 || mes > 12) {
            throw new IllegalArgumentException("m\u00eas deve estar entre 1 e 12");
        }
        List<ParteDoCiclo> partes = mesesDoCiclo(mes, cultura.cicloDias());
        double pesoTotal = partes.stream().mapToDouble(ParteDoCiclo::peso).sum();

        double somaTemp = 0;
        double chuvaTotal = 0;
        double maxGeada = 0;
        boolean primeiro = true;
        for (ParteDoCiclo p : partes) {
            ClimaMes clima = regiao.meses().get(p.indiceMes());
            somaTemp += p.peso() * notaTempMes(clima.tempMedia(), cultura);
            chuvaTotal += p.peso() * clima.chuvaMm();
            maxGeada = primeiro ? clima.probGeada() : Math.max(maxGeada, clima.probGeada());
            primeiro = false;
        }
        double nTemp = somaTemp / pesoTotal;
        // chuva total adequada, descontando seca durante a fase inicial (germinacao/crescimento)
        double nChuva = notaChuva(chuvaTotal, cultura) * (1 - fracaoSecaInicial(partes, regiao, cultura));
        double nGeada = cultura.sensivelGeada() ? 1 - maxGeada : 1.0;

        // media geometrica ponderada (lei do minimo): um fator critico muito ruim derruba a nota
        double nota = 100 * Math.pow(nTemp, 0.4) * Math.pow(nChuva, 0.4) * Math.pow(nGeada, 0.2);
        LocalDate inicio = LocalDate.of(ano, mes, 1);
        return new Janela(
                mes,
                arredondar(nota, 1),
                classificar(nota),
                inicio,
                inicio.plusDays(cultura.cicloDias()),
                arredondar(nTemp, 2),
                arredondar(nChuva, 2),
                arredondar(nGeada, 2));
    }

    /** Os 12 meses avaliados, do melhor para o pior. */
    public static List<Janela> recomendarPlantio(Cultura cultura, Regiao regiao, int ano) {
        List<Janela> janelas = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            janelas.add(avaliarMes(cultura, regiao, m, ano));
        }
        janelas.sort(Comparator.comparingDouble(Janela::nota).reversed());
        return janelas;
    }

    /** Culturas ranqueadas para plantio em um determinado mes. */
    public static List<Sugestao> oQuePlantar(Regiao regiao, List<Cultura> culturas, int mes, int ano) {
        List<Sugestao> res = new ArrayList<>();
        for (Cultura c : culturas) {
            res.add(new Sugestao(c, avaliarMes(c, regiao, mes, ano)));
        }
        res.sort(Comparator.comparingDouble((Sugestao s) -> s.janela().nota()).reversed());
        return res;
    }
}
