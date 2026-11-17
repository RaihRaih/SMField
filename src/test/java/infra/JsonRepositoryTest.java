package infra;

import domain.IndicadorAmbiental;
import domain.OcorrenciaCampo;
import domain.Propriedade;
import domain.Talhao;
import domain.TipoOcorrencia;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonRepositoryTest {

    @TempDir
    Path pasta;

    @Test
    void salvaERecuperaAposReinicializacao() {
        ArmazenamentoLocal a = new ArmazenamentoLocal(pasta);
        Propriedade p = a.propriedades().salvar(new Propriedade("Fazenda Modelo", "Contagem"));
        Talhao t = a.talhoes().salvar(new Talhao(p.getId(), "Talhão A", 12.0, "Milho"));
        LocalDateTime quando = LocalDateTime.of(2026, 11, 17, 9, 15);
        a.indicadores().salvar(new IndicadorAmbiental(t.getId(), 55.0, 24.0, 65.0, 12.0, quando));
        a.ocorrencias().salvar(new OcorrenciaCampo(t.getId(), TipoOcorrencia.DOENCA, "Ferrugem", LocalDate.of(2026, 11, 16), null));
        assertTrue(Files.exists(pasta.resolve("propriedades.json")));
        assertEquals(pasta, a.getDiretorio());

        ArmazenamentoLocal reaberto = new ArmazenamentoLocal(pasta);
        Propriedade lida = reaberto.propriedades().buscarPorId(p.getId()).orElseThrow();
        assertEquals("Fazenda Modelo", lida.getNome());
        Talhao tl = reaberto.talhoes().listarPorPropriedade(p.getId()).get(0);
        assertEquals("Talhão A", tl.getNome());
        assertEquals(12.0, tl.getAreaHectares());
        IndicadorAmbiental il = reaberto.indicadores().ultimoDoTalhao(t.getId()).orElseThrow();
        assertEquals(quando, il.getDataHora());
        assertEquals(24.0, il.getTemperatura());
        OcorrenciaCampo ol = reaberto.ocorrencias().listarPorTalhao(t.getId()).get(0);
        assertEquals(TipoOcorrencia.DOENCA, ol.getTipo());
        assertEquals(LocalDate.of(2026, 11, 16), ol.getData());
    }

    @Test
    void preservaRegistrosAnteriores() {
        IndicadorRepository repo = new IndicadorRepository(pasta);
        repo.salvar(new IndicadorAmbiental(1, 50.0, 20.0, 60.0, 10.0, LocalDateTime.of(2026, 11, 1, 8, 0)));
        repo.salvar(new IndicadorAmbiental(1, 30.0, 35.0, 30.0, 2.0, LocalDateTime.of(2026, 11, 2, 8, 0)));
        List<IndicadorAmbiental> lidos = new IndicadorRepository(pasta).listarPorTalhao(1);
        assertEquals(2, lidos.size());
        assertEquals(50.0, lidos.get(0).getUmidadeSolo());
        assertEquals(30.0, lidos.get(1).getUmidadeSolo());
    }

    @Test
    void identificadoresNaoSaoReutilizadosAposExclusao() {
        PropriedadeRepository repo = new PropriedadeRepository(pasta);
        Propriedade a = repo.salvar(new Propriedade("A", "X"));
        Propriedade b = repo.salvar(new Propriedade("B", "X"));
        assertTrue(repo.remover(b.getId()));
        assertFalse(repo.remover(b.getId()));
        Propriedade c = new PropriedadeRepository(pasta).salvar(new Propriedade("C", "X"));
        assertTrue(c.getId() > b.getId());
        assertEquals(1, a.getId());
    }

    @Test
    void atualizaRegistroExistenteSemDuplicar() {
        PropriedadeRepository repo = new PropriedadeRepository(pasta);
        Propriedade a = repo.salvar(new Propriedade("A", "X"));
        a.alterar("A2", "X2");
        repo.salvar(a);
        assertEquals(1, repo.contar());
        assertEquals("A2", new PropriedadeRepository(pasta).buscarPorId(a.getId()).orElseThrow().getNome());
        repo.salvar(new Propriedade(10, "Importada", "Y"));
        assertEquals(11, repo.salvar(new Propriedade("Nova", "Y")).getId());
        assertTrue(repo.existeComNomeEMunicipio("importada", "y", 0));
        assertFalse(repo.existeComNomeEMunicipio("importada", "y", 10));
    }

    @Test
    void removerSeEFiltrar() {
        TalhaoRepository repo = new TalhaoRepository(pasta);
        repo.salvar(new Talhao(1, "A", 1.0, "Milho"));
        repo.salvar(new Talhao(1, "B", 1.0, "Soja"));
        repo.salvar(new Talhao(2, "C", 1.0, "Soja"));
        assertEquals(2, repo.filtrar(t -> t.getCultura().equals("Soja")).size());
        assertEquals(0, repo.removerSe(t -> t.getPropriedadeId() == 99));
        assertEquals(2, repo.removerSe(t -> t.getPropriedadeId() == 1));
        assertEquals(1, new TalhaoRepository(pasta).contar());
    }

    @Test
    void arquivoCorrompidoGeraErroClaro() throws Exception {
        Files.writeString(pasta.resolve("propriedades.json"), "{ isto não é json");
        PersistenciaException e = assertThrows(PersistenciaException.class, () -> new PropriedadeRepository(pasta));
        assertTrue(e.getMessage().contains("propriedades.json"));
    }

    @Test
    void falhaDeGravacaoGeraErroClaro() throws Exception {
        Path arquivoNoLugarDaPasta = pasta.resolve("ocupado");
        Files.writeString(arquivoNoLugarDaPasta, "x");
        PropriedadeRepository repo = new PropriedadeRepository(arquivoNoLugarDaPasta);
        assertThrows(PersistenciaException.class, () -> repo.salvar(new Propriedade("A", "B")));
    }

    @Test
    void diretorioPadraoConfiguravel() {
        String anterior = System.getProperty(ArmazenamentoLocal.PROPRIEDADE_DIRETORIO);
        try {
            System.clearProperty(ArmazenamentoLocal.PROPRIEDADE_DIRETORIO);
            assertEquals(Path.of("dados"), ArmazenamentoLocal.diretorioPadrao());
            System.setProperty(ArmazenamentoLocal.PROPRIEDADE_DIRETORIO, pasta.toString());
            assertEquals(pasta, ArmazenamentoLocal.diretorioPadrao());
        } finally {
            if (anterior == null) {
                System.clearProperty(ArmazenamentoLocal.PROPRIEDADE_DIRETORIO);
            } else {
                System.setProperty(ArmazenamentoLocal.PROPRIEDADE_DIRETORIO, anterior);
            }
        }
    }
}
