package service;

import domain.IndicadorAmbiental;
import domain.Propriedade;
import domain.Talhao;
import domain.ValidacaoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IndicadorServiceTest {

    @TempDir
    Path pasta;
    private ServicosSMField s;
    private Talhao talhao;

    @BeforeEach
    void preparar() {
        s = Apoio.servicos(pasta);
        Propriedade p = s.propriedades().cadastrar("Fazenda Modelo", "Contagem");
        talhao = s.talhoes().cadastrar(p.getId(), "Talhão A", 12.0, "Milho");
    }

    @Test
    void registraDadosValidos() {
        IndicadorAmbiental i = s.indicadores().registrar(talhao.getId(), 55.0, 24.0, 65.0, 12.0, Apoio.AGORA);
        assertTrue(i.getId() > 0);
        assertEquals(i.getId(), s.indicadores().ultimo(talhao.getId()).orElseThrow().getId());
    }

    @Test
    void registraAPartirDoTextoDosCampos() {
        IndicadorAmbiental i = s.indicadores().registrarTexto(talhao.getId(), "55,5", "24", "65", "12.5", "17/11/2026 08:00");
        assertEquals(55.5, i.getUmidadeSolo());
        assertEquals(12.5, i.getPrecipitacao24h());
    }

    @Test
    void umidadeNegativaOuAcimaDe100() {
        assertThrows(ValidacaoException.class, () -> s.indicadores().registrar(talhao.getId(), -1.0, 24.0, 65.0, 12.0, Apoio.AGORA));
        assertThrows(ValidacaoException.class, () -> s.indicadores().registrar(talhao.getId(), 101.0, 24.0, 65.0, 12.0, Apoio.AGORA));
        assertTrue(s.indicadores().listarPorTalhao(talhao.getId()).isEmpty());
    }

    @Test
    void precipitacaoNegativaETemperaturaForaDoIntervalo() {
        assertThrows(ValidacaoException.class, () -> s.indicadores().registrar(talhao.getId(), 50.0, 24.0, 65.0, -1.0, Apoio.AGORA));
        assertThrows(ValidacaoException.class, () -> s.indicadores().registrar(talhao.getId(), 50.0, 61.0, 65.0, 1.0, Apoio.AGORA));
        assertThrows(ValidacaoException.class, () -> s.indicadores().registrar(talhao.getId(), 50.0, -25.0, 65.0, 1.0, Apoio.AGORA));
    }

    @Test
    void indicadoresAusentesSaoRejeitadosComMensagemClara() {
        ValidacaoException e = assertThrows(ValidacaoException.class,
                () -> s.indicadores().registrar(talhao.getId(), 50.0, null, 65.0, null, Apoio.AGORA));
        assertTrue(e.getMessage().contains("temperatura"));
        assertTrue(e.getMessage().contains("precipitação"));
        assertTrue(e.getMessage().contains("insuficientes"));
        ValidacaoException e2 = assertThrows(ValidacaoException.class,
                () -> s.indicadores().registrarTexto(talhao.getId(), "", "24", " ", "1", "17/11/2026 08:00"));
        assertTrue(e2.getMessage().contains("umidade do solo"));
        assertTrue(e2.getMessage().contains("umidade do ar"));
        assertThrows(ValidacaoException.class,
                () -> s.indicadores().registrar(talhao.getId(), null, 24.0, 60.0, 1.0, Apoio.AGORA));
    }

    @Test
    void letrasNaNEInfinitoSaoRejeitados() {
        assertThrows(ValidacaoException.class,
                () -> s.indicadores().registrarTexto(talhao.getId(), "abc", "24", "65", "12", "17/11/2026 08:00"));
        assertThrows(ValidacaoException.class,
                () -> s.indicadores().registrarTexto(talhao.getId(), "50", "NaN", "65", "12", "17/11/2026 08:00"));
        assertThrows(ValidacaoException.class,
                () -> s.indicadores().registrar(talhao.getId(), 50.0, Double.POSITIVE_INFINITY, 65.0, 12.0, Apoio.AGORA));
    }

    @Test
    void talhaoInexistenteEDataFuturaSaoRejeitados() {
        assertThrows(ValidacaoException.class, () -> s.indicadores().registrar(999, 50.0, 24.0, 65.0, 12.0, Apoio.AGORA));
        assertThrows(ValidacaoException.class,
                () -> s.indicadores().registrar(talhao.getId(), 50.0, 24.0, 65.0, 12.0, Apoio.AGORA.plusDays(1)));
    }

    @Test
    void novosRegistrosPreservamOsAnterioresEmOrdemCronologica() {
        s.indicadores().registrar(talhao.getId(), 30.0, 35.0, 30.0, 2.0, Apoio.AGORA);
        s.indicadores().registrar(talhao.getId(), 55.0, 24.0, 65.0, 12.0, Apoio.AGORA.minusDays(2));
        s.indicadores().registrar(talhao.getId(), 60.0, 22.0, 60.0, 10.0, Apoio.AGORA.minusDays(1));
        List<IndicadorAmbiental> h = s.indicadores().listarPorTalhao(talhao.getId());
        assertEquals(3, h.size());
        assertTrue(h.get(0).getDataHora().isBefore(h.get(1).getDataHora()));
        assertTrue(h.get(1).getDataHora().isBefore(h.get(2).getDataHora()));
        assertEquals(30.0, s.indicadores().ultimo(talhao.getId()).orElseThrow().getUmidadeSolo());
        assertEquals(3, s.indicadores().listarTodos().size());
    }
}
