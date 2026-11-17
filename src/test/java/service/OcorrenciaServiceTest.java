package service;

import domain.OcorrenciaCampo;
import domain.Propriedade;
import domain.Talhao;
import domain.TipoOcorrencia;
import domain.ValidacaoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OcorrenciaServiceTest {

    @TempDir
    Path pasta;

    @Test
    void registraListaEExclui() {
        ServicosSMField s = Apoio.servicos(pasta);
        Propriedade p = s.propriedades().cadastrar("Fazenda", "Contagem");
        Talhao t = s.talhoes().cadastrar(p.getId(), "A", 1.0, "Milho");
        LocalDate hoje = Apoio.AGORA.toLocalDate();
        s.ocorrencias().registrar(t.getId(), TipoOcorrencia.PRAGA, "Lagartas", hoje.minusDays(3));
        OcorrenciaCampo recente = s.ocorrencias().registrar(t.getId(), TipoOcorrencia.EXCESSO_AGUA, "Alagamento", hoje);
        List<OcorrenciaCampo> lista = s.ocorrencias().listarPorTalhao(t.getId());
        assertEquals(2, lista.size());
        assertEquals(recente.getId(), lista.get(0).getId(), "mais recente primeiro");
        s.ocorrencias().excluir(recente.getId());
        assertEquals(1, s.ocorrencias().listarPorTalhao(t.getId()).size());
        assertThrows(ValidacaoException.class, () -> s.ocorrencias().excluir(recente.getId()));
    }

    @Test
    void rejeitaTalhaoInexistenteDescricaoVaziaEDataFutura() {
        ServicosSMField s = Apoio.servicos(pasta);
        Propriedade p = s.propriedades().cadastrar("Fazenda", "Contagem");
        Talhao t = s.talhoes().cadastrar(p.getId(), "A", 1.0, "Milho");
        LocalDate hoje = Apoio.AGORA.toLocalDate();
        assertThrows(ValidacaoException.class, () -> s.ocorrencias().registrar(99, TipoOcorrencia.PRAGA, "x", hoje));
        assertThrows(ValidacaoException.class, () -> s.ocorrencias().registrar(t.getId(), TipoOcorrencia.PRAGA, " ", hoje));
        assertThrows(ValidacaoException.class,
                () -> s.ocorrencias().registrar(t.getId(), TipoOcorrencia.PRAGA, "x", hoje.plusDays(1)));
        assertTrue(s.ocorrencias().listarPorTalhao(t.getId()).isEmpty());
    }
}
