package service;

import domain.EstacaoMeteorologica;
import domain.IndicadorAmbiental;
import domain.NivelRisco;
import domain.Propriedade;
import domain.Talhao;
import domain.ValidacaoException;
import infra.InmetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.StringReader;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InmetServiceTest {

    private static final LocalDate D1 = LocalDate.of(2026, 10, 1);
    private static final LocalDate D7 = LocalDate.of(2026, 10, 7);

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
    void buscaEstacoesSemDiferenciarAcentosEMaiusculas() {
        List<EstacaoMeteorologica> bh = s.inmet().buscarEstacoes("belo HORIZONTE");
        assertTrue(bh.stream().anyMatch(e -> e.codigo().equals("A521")));
        assertTrue(s.inmet().buscarEstacoes("cuiabá").stream().anyMatch(e -> e.codigo().equals("A901")));
        assertTrue(s.inmet().buscarEstacoes("a521").stream().allMatch(e -> e.codigo().equals("A521")));
        assertEquals(s.inmet().estacoes().size(), s.inmet().buscarEstacoes("  ").size());
        assertTrue(s.inmet().buscarEstacoes("cidade que não existe").isEmpty());
        assertEquals("A904", s.inmet().buscarEstacoes("Sorriso/MT").get(0).codigo());
        assertTrue(s.inmet().buscarEstacoes("rio verde go").stream().anyMatch(e -> e.codigo().equals("A025")));
    }

    @Test
    void sugereEstacaoPeloMunicipio() {
        EstacaoMeteorologica e = s.inmet().sugerirParaMunicipio("Belo Horizonte").orElseThrow();
        assertTrue(e.nome().startsWith("Belo Horizonte"));
        assertEquals("A521", e.codigo(), "maior cobertura; no empate (A521 e F501 com 99,7%), o menor código");
        assertTrue(s.inmet().sugerirParaMunicipio("Contagem").isEmpty());
        assertEquals("A904", s.inmet().sugerirParaMunicipio("Sorriso/MT").orElseThrow().codigo());
        assertEquals("A025", s.inmet().sugerirParaMunicipio("Rio Verde - GO").orElseThrow().codigo());
        assertEquals("A523", s.inmet().sugerirParaMunicipio("Patrocínio/MG").orElseThrow().codigo());
        assertTrue(s.inmet().sugerirParaMunicipio("Sorriso/SP").isEmpty(), "UF diferente");
        assertTrue(s.inmet().sugerirParaMunicipio("/MT").isEmpty());
        assertTrue(s.inmet().sugerirParaMunicipio(null).isEmpty());
    }

    @Test
    void periodoDisponivelEEstacaoInexistente() {
        LocalDate[] p = s.inmet().periodoDisponivel("A521").orElseThrow();
        assertEquals(LocalDate.of(2026, 1, 1), p[0]);
        assertEquals(LocalDate.of(2026, 10, 31), p[1]);
        assertTrue(s.inmet().periodoDisponivel("A015").isEmpty(), "estação sem dias completos");
        assertThrows(ValidacaoException.class, () -> s.inmet().obterEstacao("XXXX"));
        assertTrue(s.inmet().leitura("A521", D1).isPresent());
    }

    @Test
    void umidadeDoSoloSimuladaSegueAFormulaDocumentada() {
        // 01/10/2026 em A521: chuva baixa na semana, ar seco e dia quente -> solo seco
        Double seco = s.inmet().umidadeSoloSimulada("A521", D1);
        // 05/10/2026: 66,6 mm no dia -> solo úmido
        Double umido = s.inmet().umidadeSoloSimulada("A521", LocalDate.of(2026, 10, 5));
        assertTrue(seco < 25, "solo seco: " + seco);
        assertTrue(umido > 70, "solo úmido: " + umido);
        assertEquals(seco, s.inmet().umidadeSoloSimulada("A521", D1), "determinística");
        for (LocalDate d = LocalDate.of(2026, 1, 1); d.isBefore(LocalDate.of(2026, 11, 1)); d = d.plusDays(1)) {
            Double v = s.inmet().umidadeSoloSimulada("A901", d);
            if (v != null) {
                assertTrue(v >= 5 && v <= 95, "fora de 5–95: " + v);
            }
        }
        assertNull(s.inmet().umidadeSoloSimulada("A521", LocalDate.of(2026, 12, 1)), "sem dado no dia");
    }

    @Test
    void previaCalculaRiscoSemGravar() {
        List<InmetService.DiaInmet> dias = s.inmet().previa(talhao.getId(), "A521", D1, D7);
        assertEquals(7, dias.size());
        assertTrue(dias.stream().allMatch(d -> d.situacao() == InmetService.Situacao.PRONTO));
        InmetService.DiaInmet primeiro = dias.get(0);
        assertEquals(27.54, primeiro.leitura().tempMedia());
        assertTrue(primeiro.resultado().isCalculado());
        assertEquals(NivelRisco.classificar(primeiro.resultado().getIndice()), primeiro.resultado().getNivel());
        assertTrue(s.indicadores().listarPorTalhao(talhao.getId()).isEmpty());
        assertEquals("Pronto para importar", InmetService.Situacao.PRONTO.toString());
    }

    @Test
    void importaDiasCompletosSemDuplicarEPreservandoAFonte() {
        InmetService.ResultadoImportacao r = s.inmet().importar(talhao.getId(), "A521", D1, D7);
        assertEquals(new InmetService.ResultadoImportacao(7, 0, 0), r);
        List<IndicadorAmbiental> registros = s.indicadores().listarPorTalhao(talhao.getId());
        assertEquals(7, registros.size());
        IndicadorAmbiental primeiro = registros.get(0);
        assertEquals(D1.atTime(12, 0), primeiro.getDataHora());
        assertEquals(27.54, primeiro.getTemperatura());
        assertTrue(primeiro.isDadoInmet());
        assertTrue(primeiro.getFonte().contains("A521"));
        assertTrue(primeiro.getFonte().contains("SIMULADA"));
        assertEquals(s.inmet().umidadeSoloSimulada("A521", D1), primeiro.getUmidadeSolo());

        InmetService.ResultadoImportacao repetida = s.inmet().importar(talhao.getId(), "A521", D1, LocalDate.of(2026, 10, 9));
        assertEquals(new InmetService.ResultadoImportacao(2, 7, 0), repetida);
        assertEquals(9, s.indicadores().listarPorTalhao(talhao.getId()).size());

        // persistência: continua lá depois de reabrir, com a fonte
        ServicosSMField reaberto = Apoio.servicos(pasta);
        IndicadorAmbiental lido = reaberto.indicadores().listarPorTalhao(talhao.getId()).get(0);
        assertTrue(lido.isDadoInmet());
        assertEquals(primeiro.getFonte(), lido.getFonte());
        assertEquals(LocalDate.of(2026, 10, 9), reaberto.monitoramento().painel().get(0).atualizadoEm().toLocalDate());
    }

    @Test
    void diasIncompletosSaoIgnorados() {
        // A015 (Itapaci/GO) não tem nenhum dia completo na base
        InmetService.ResultadoImportacao r = s.inmet().importar(talhao.getId(), "A015", D1, D7);
        assertEquals(0, r.importados());
        assertEquals(7, r.incompletos());
        assertTrue(s.inmet().previa(0, "A015", D1, D1).get(0).resultado().getNivel() == NivelRisco.NAO_CALCULADO);
    }

    @Test
    void validaPeriodoTalhaoEEstacao() {
        assertThrows(ValidacaoException.class, () -> s.inmet().importar(talhao.getId(), "A521", D7, D1));
        assertThrows(ValidacaoException.class,
                () -> s.inmet().importar(talhao.getId(), "A521", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30)));
        assertThrows(ValidacaoException.class, () -> s.inmet().importar(talhao.getId(), "A521", null, D7));
        assertThrows(ValidacaoException.class, () -> s.inmet().importar(999, "A521", D1, D7));
        assertThrows(ValidacaoException.class, () -> s.inmet().importar(talhao.getId(), "XXXX", D1, D7));
        assertTrue(s.indicadores().listarPorTalhao(talhao.getId()).isEmpty());
    }

    @Test
    void funcionaComBaseInjetada() {
        String est = "estacao,nome,uf,regiao,latitude,longitude,altitude_m,dias_com_dado,dias_no_periodo,cobertura_pct,meses_validos\n"
                + "T001,Cidade Teste,MG,SE,0,0,0,1,1,100,\"10\"\n";
        String dia = "data,estacao,temp_media,temp_min,umidade_ar_media,chuva_mm\n2026-10-01,T001,24,18,60,10\n";
        InmetService servico = new InmetService(() -> new InmetRepository(new StringReader(est), new StringReader(dia)),
                s.talhoes(), s.indicadores(), s.risco());
        // solo = 20 + 0,8×10 + 0,3×(60−60) − 0 = 28,0
        assertEquals(28.0, servico.umidadeSoloSimulada("T001", D1));
        assertEquals(1, servico.importar(talhao.getId(), "T001", D1, D1).importados());
        assertEquals("cidade sao joao", InmetService.normalizar("  Cidade   São JOÃO "));
        assertEquals("", InmetService.normalizar(null));
    }
}
