package smfield;

import java.time.LocalDate;

/** Resultado da avaliacao de um mes de plantio. A nota vai de 0 a 100. */
public record Janela(
        int mesPlantio,
        double nota,
        String classificacao,   // ideal | aceit\u00e1vel | desfavor\u00e1vel
        LocalDate plantioInicio,
        LocalDate colheitaEstimada,
        double notaTemperatura,
        double notaChuva,
        double notaGeada) {
}
