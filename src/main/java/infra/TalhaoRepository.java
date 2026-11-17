package infra;

import domain.Talhao;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

/** Persistência dos talhões em {@code talhoes.json}. */
public class TalhaoRepository extends JsonRepository<Talhao> {

    public TalhaoRepository(Path diretorio) {
        super(diretorio.resolve("talhoes.json"), Talhao.class);
    }

    public List<Talhao> listarPorPropriedade(long propriedadeId) {
        return filtrar(t -> t.getPropriedadeId() == propriedadeId).stream()
                .sorted(Comparator.comparing(Talhao::getNome, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public boolean existeComNome(long propriedadeId, String nome, long ignorarId) {
        return listarPorPropriedade(propriedadeId).stream()
                .anyMatch(t -> t.getId() != ignorarId && t.getNome().equalsIgnoreCase(nome.trim()));
    }
}
