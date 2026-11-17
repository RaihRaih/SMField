package smfield;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class CalendarioTest {
    private static final int ANO = 2026;

    @Test
    void dataColheita() {
        Cultura soja = DadosLoader.buscarCultura("soja");
        assertEquals(LocalDate.of(2027, 2, 17), Calendario.dataColheita(LocalDate.of(2026, 10, 20), soja));
    }

    @Test
    void agruparMesesComViradaDeAno() {
        assertEquals(List.of(List.of(10, 11, 12, 1)), Calendario.agruparMeses(List.of(10, 11, 12, 1)));
        assertEquals(List.of(List.of(2, 3), List.of(7)), Calendario.agruparMeses(List.of(2, 3, 7)));
        assertEquals(List.of(), Calendario.agruparMeses(List.of()));
    }

    @Test
    void janelasRecomendadasFormato() {
        List<String> jan = Calendario.janelasRecomendadas(
                DadosLoader.buscarCultura("milho"), DadosLoader.buscarRegiao("Belo Horizonte"), ANO);
        assertFalse(jan.isEmpty());
    }
}
