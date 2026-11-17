package service;

import domain.Propriedade;
import domain.Talhao;
import domain.TipoOcorrencia;
import domain.ValidacaoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TalhaoServiceTest {

    @TempDir
    Path pasta;
    private ServicosSMField s;
    private Propriedade fazenda;

    @BeforeEach
    void preparar() {
        s = Apoio.servicos(pasta);
        fazenda = s.propriedades().cadastrar("Fazenda Modelo", "Contagem");
    }

    @Test
    void cadastroValidoVinculadoAPropriedade() {
        Talhao t = s.talhoes().cadastrar(fazenda.getId(), "Talhão A", 12.0, "Milho");
        assertTrue(t.getId() > 0);
        assertEquals(fazenda.getId(), t.getPropriedadeId());
        assertEquals(1, s.talhoes().listarPorPropriedade(fazenda.getId()).size());
        assertEquals(t, s.talhoes().obter(t.getId()));
    }

    @Test
    void propriedadeInexistenteEhRejeitada() {
        ValidacaoException e = assertThrows(ValidacaoException.class,
                () -> s.talhoes().cadastrar(999, "Talhão A", 12.0, "Milho"));
        assertTrue(e.getMessage().contains("propriedade"));
        assertTrue(s.talhoes().listar().isEmpty());
    }

    @Test
    void areaZeroOuNegativaEhRejeitada() {
        assertThrows(ValidacaoException.class, () -> s.talhoes().cadastrar(fazenda.getId(), "A", 0.0, "Milho"));
        assertThrows(ValidacaoException.class, () -> s.talhoes().cadastrar(fazenda.getId(), "A", -5.0, "Milho"));
    }

    @Test
    void culturaVaziaEhRejeitada() {
        assertThrows(ValidacaoException.class, () -> s.talhoes().cadastrar(fazenda.getId(), "A", 3.0, "  "));
    }

    @Test
    void nomeDuplicadoNaMesmaPropriedadeEhRejeitado() {
        s.talhoes().cadastrar(fazenda.getId(), "Talhão A", 12.0, "Milho");
        assertThrows(ValidacaoException.class, () -> s.talhoes().cadastrar(fazenda.getId(), "talhão a", 5.0, "Soja"));
        Propriedade outra = s.propriedades().cadastrar("Outra", "Betim");
        s.talhoes().cadastrar(outra.getId(), "Talhão A", 5.0, "Soja");
    }

    @Test
    void editarAlteraNomeAreaECultura() {
        Talhao t = s.talhoes().cadastrar(fazenda.getId(), "Talhão A", 12.0, "Milho");
        s.talhoes().cadastrar(fazenda.getId(), "Talhão B", 2.0, "Soja");
        s.talhoes().editar(t.getId(), "Talhão A1", 15.5, "Soja");
        Talhao lido = s.talhoes().obter(t.getId());
        assertEquals("Talhão A1", lido.getNome());
        assertEquals(15.5, lido.getAreaHectares());
        assertEquals("Soja", lido.getCultura());
        assertThrows(ValidacaoException.class, () -> s.talhoes().editar(t.getId(), "Talhão B", 1.0, "Soja"));
        assertThrows(ValidacaoException.class, () -> s.talhoes().editar(t.getId(), "X", -1.0, "Soja"));
        assertThrows(ValidacaoException.class, () -> s.talhoes().editar(777, "X", 1.0, "Soja"));
    }

    @Test
    void excluirRemoveRegistrosVinculadosEmCascata() {
        Talhao t = s.talhoes().cadastrar(fazenda.getId(), "Talhão A", 12.0, "Milho");
        Talhao outro = s.talhoes().cadastrar(fazenda.getId(), "Talhão B", 3.0, "Soja");
        s.indicadores().registrar(t.getId(), 50.0, 25.0, 60.0, 10.0, Apoio.AGORA.minusDays(1));
        s.indicadores().registrar(t.getId(), 30.0, 35.0, 60.0, 10.0, Apoio.AGORA);
        s.indicadores().registrar(outro.getId(), 50.0, 25.0, 60.0, 10.0, Apoio.AGORA);
        s.ocorrencias().registrar(t.getId(), TipoOcorrencia.PRAGA, "Lagartas", Apoio.AGORA.toLocalDate());

        TalhaoService.Vinculos antes = s.talhoes().contarVinculos(t.getId());
        assertEquals(new TalhaoService.Vinculos(2, 1), antes);
        assertEquals(3, antes.total());

        TalhaoService.Vinculos removidos = s.talhoes().excluir(t.getId());
        assertEquals(antes, removidos);
        assertTrue(s.talhoes().buscar(t.getId()).isEmpty());
        assertTrue(s.indicadores().listarPorTalhao(t.getId()).isEmpty());
        assertTrue(s.ocorrencias().listarPorTalhao(t.getId()).isEmpty());
        assertEquals(1, s.indicadores().listarPorTalhao(outro.getId()).size(), "outros talhões não são afetados");
        assertThrows(ValidacaoException.class, () -> s.talhoes().excluir(t.getId()));
    }
}
