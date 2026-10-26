package smfield;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class MainTest {

    private static final class Resultado {
        int codigo;
        String out;
        String err;
    }

    private static Resultado rodar(String... args) {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        ByteArrayOutputStream e = new ByteArrayOutputStream();
        Resultado r = new Resultado();
        r.codigo = Main.executar(args,
                new PrintStream(o, true, StandardCharsets.UTF_8), new PrintStream(e, true, StandardCharsets.UTF_8));
        r.out = o.toString(StandardCharsets.UTF_8);
        r.err = e.toString(StandardCharsets.UTF_8);
        return r;
    }

    @Test
    void colheitaImprimeDataEstimada() {
        Resultado r = rodar("colheita", "--cultura", "milho", "--plantio", "2026-10-20");
        assertEquals(0, r.codigo);
        assertTrue(r.out.contains("20/10/2026"));
        assertTrue(r.out.contains("17/02/2027"));
    }

    @Test
    void listarMostraCulturasERegioes() {
        Resultado r = rodar("listar");
        assertEquals(0, r.codigo);
        assertTrue(r.out.contains("Soja"));
        assertTrue(r.out.contains("Passo Fundo/RS"));
    }

    @Test
    void culturaInexistenteRetornaCodigo1() {
        Resultado r = rodar("recomendar", "--cultura", "banana-ouro", "--regiao", "Cuiaba");
        assertEquals(1, r.codigo);
        assertTrue(r.err.startsWith("Erro:"));
    }

    @Test
    void mesInvalidoRetornaCodigo1() {
        assertEquals(1, rodar("agora", "--regiao", "Cuiaba", "--mes", "13").codigo);
    }

    @Test
    void usoIncorretoRetornaCodigo2() {
        assertEquals(2, rodar().codigo);
        assertEquals(2, rodar("recomendar", "--cultura", "soja").codigo);
        assertEquals(2, rodar("comando-que-nao-existe").codigo);
    }
}
