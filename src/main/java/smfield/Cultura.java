package smfield;

/** Parametros agronomicos de uma cultura (equivalente ao dataclass Cultura do Python). */
public record Cultura(
        String nome,
        int cicloDias,
        double tempIdealMin,
        double tempIdealMax,
        double tempAbsMin,
        double tempAbsMax,
        double chuvaMinMm,   // necessidade hidrica total no ciclo
        double chuvaMaxMm,
        boolean sensivelGeada) {
}
