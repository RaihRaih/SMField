package service;

import domain.Propriedade;
import domain.ValidacaoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PropriedadeServiceTest {

    @TempDir
    Path pasta;
    private ServicosSMField s;

    @BeforeEach
    void preparar() {
        s = Apoio.servicos(pasta);
    }

    @Test
    void cadastroValidoGeraIdentificadorUnicoEApareceNaListagem() {
        Propriedade a = s.propriedades().cadastrar("Fazenda Modelo", "Contagem");
        Propriedade b = s.propriedades().cadastrar("Sítio Bela Vista", "Betim");
        assertTrue(a.getId() > 0);
        assertNotEquals(a.getId(), b.getId());
        assertEquals(2, s.propriedades().listar().size());
        assertEquals("Fazenda Modelo", s.propriedades().listar().get(0).getNome());
    }

    @Test
    void nomeOuMunicipioVazioSaoRejeitados() {
        assertThrows(ValidacaoException.class, () -> s.propriedades().cadastrar("  ", "Contagem"));
        assertThrows(ValidacaoException.class, () -> s.propriedades().cadastrar("Fazenda", ""));
        assertTrue(s.propriedades().listar().isEmpty());
    }

    @Test
    void duplicidadeDeNomeEMunicipioEhRejeitada() {
        s.propriedades().cadastrar("Fazenda Modelo", "Contagem");
        assertThrows(ValidacaoException.class, () -> s.propriedades().cadastrar("fazenda modelo", " CONTAGEM "));
        s.propriedades().cadastrar("Fazenda Modelo", "Betim");
        assertEquals(2, s.propriedades().listar().size());
    }

    @Test
    void editarEExcluir() {
        Propriedade p = s.propriedades().cadastrar("A", "B");
        Propriedade q = s.propriedades().cadastrar("C", "D");
        s.propriedades().editar(p.getId(), "A2", "B2");
        assertEquals("A2", s.propriedades().obter(p.getId()).getNome());
        assertThrows(ValidacaoException.class, () -> s.propriedades().editar(p.getId(), "C", "D"));
        s.propriedades().excluir(q.getId());
        assertTrue(s.propriedades().buscar(q.getId()).isEmpty());
        assertThrows(ValidacaoException.class, () -> s.propriedades().obter(999));
    }

    @Test
    void naoExcluiPropriedadeComTalhoes() {
        Propriedade p = s.propriedades().cadastrar("A", "B");
        s.talhoes().cadastrar(p.getId(), "T1", 1.0, "Milho");
        ValidacaoException e = assertThrows(ValidacaoException.class, () -> s.propriedades().excluir(p.getId()));
        assertTrue(e.getMessage().contains("talhão"));
    }
}
