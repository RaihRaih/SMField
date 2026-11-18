package infra;

import domain.EstacaoMeteorologica;
import domain.LeituraClimaticaDiaria;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InmetRepositoryTest {

    private static final String ESTACOES = "﻿estacao,nome,uf,regiao,latitude,longitude,altitude_m,dias_com_dado,"
            + "dias_no_periodo,cobertura_pct,meses_validos\n"
            + "A521,Belo Horizonte - Pampulha,MG,SE,-19.88,-43.96,854.02,304,305,99.7,\"1,2,3\"\n"
            + "a999,Teste,XX,N,abc,,10,0,0,,\n\n";
    private static final String DIARIO = "﻿data,estacao,temp_media,temp_min,umidade_ar_media,chuva_mm\n"
            + "2026-01-02,A521,23.04,19.5,78.54,2.0\n"
            + "2026-01-01,A521,22.38,17.1,77.46,17.8\n"
            + "2026-01-03,A521,,,60.0,\n"
            + "data-invalida,A521,1,1,1,1\n\n";

    private InmetRepository pequeno() {
        return new InmetRepository(new StringReader(ESTACOES), new StringReader(DIARIO));
    }

    @Test
    void leCatalogoComAspasBomEValoresAusentes() {
        InmetRepository r = pequeno();
        List<EstacaoMeteorologica> lista = r.listarEstacoes();
        assertEquals(2, lista.size());
        EstacaoMeteorologica a = r.buscarEstacao("a521").orElseThrow();
        assertEquals("Belo Horizonte - Pampulha", a.nome());
        assertEquals("MG", a.uf());
        assertEquals(99.7, a.coberturaPct());
        assertEquals("A521 — Belo Horizonte - Pampulha/MG", a.toString());
        EstacaoMeteorologica b = r.buscarEstacao("A999").orElseThrow();
        assertEquals(0.0, b.latitude(), "número inválido vira 0");
        assertTrue(r.buscarEstacao(null).isEmpty());
    }

    @Test
    void leDiarioOrdenadoIgnorandoLinhasInvalidas() {
        InmetRepository r = pequeno();
        List<LeituraClimaticaDiaria> l = r.leituras("A521");
        assertEquals(3, l.size());
        assertEquals(LocalDate.of(2026, 1, 1), l.get(0).data());
        assertEquals(17.8, l.get(0).chuvaMm());
        assertTrue(l.get(0).completa());
        LeituraClimaticaDiaria incompleta = r.leitura("A521", LocalDate.of(2026, 1, 3)).orElseThrow();
        assertNull(incompleta.tempMedia());
        assertNull(incompleta.chuvaMm());
        assertFalse(incompleta.completa());
        assertEquals(2, r.leituras("A521", LocalDate.of(2026, 1, 2), LocalDate.of(2026, 1, 10)).size());
        assertTrue(r.leituras("A521", LocalDate.of(2026, 2, 1), LocalDate.of(2026, 1, 1)).isEmpty());
        assertTrue(r.leituras("ZZZZ", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 9)).isEmpty());
        assertTrue(r.leitura("ZZZZ", LocalDate.of(2026, 1, 1)).isEmpty());
        assertTrue(r.leituras("ZZZZ").isEmpty());
        assertEquals(3, r.totalLeituras());
    }

    @Test
    void arquivoVazioOuMalFormadoGeraErroClaro() {
        assertThrows(PersistenciaException.class, () -> new InmetRepository(new StringReader(""), new StringReader(DIARIO)));
        assertThrows(PersistenciaException.class,
                () -> new InmetRepository(new StringReader(ESTACOES), new StringReader("data,estacao,chuva_mm\n2026-01-01,A521,xyz\n")));
    }

    @Test
    void baseRealEmbutidaNoProjeto() {
        InmetRepository r = InmetRepository.padrao();
        assertSame(r, InmetRepository.padrao(), "carregada uma única vez");
        assertTrue(r.listarEstacoes().size() > 600);
        assertTrue(r.totalLeituras() > 100_000);
        LeituraClimaticaDiaria l = r.leitura("A521", LocalDate.of(2026, 1, 1)).orElseThrow();
        assertEquals(25.26, l.tempMedia());
        assertEquals(71.17, l.umidadeAr());
        assertEquals(2.2, l.chuvaMm());
        assertEquals("Belo Horizonte - Pampulha", r.buscarEstacao("A521").orElseThrow().nome());
    }

    @Test
    void divideCamposCsv() {
        assertEquals(List.of("a", "1,2", ""), InmetRepository.dividir("a,\"1,2\","));
    }
}
