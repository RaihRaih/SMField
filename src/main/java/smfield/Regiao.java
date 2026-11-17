package smfield;

import java.util.List;

/** Regiao com 12 meses de clima (jan..dez). */
public record Regiao(String nome, String uf, List<ClimaMes> meses) {
    public Regiao {
        meses = List.copyOf(meses);
    }
}
