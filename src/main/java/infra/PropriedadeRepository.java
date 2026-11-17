package infra;

import domain.Propriedade;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

/** Persistência das propriedades em {@code propriedades.json}. */
public class PropriedadeRepository extends JsonRepository<Propriedade> {

    public PropriedadeRepository(Path diretorio) {
        super(diretorio.resolve("propriedades.json"), Propriedade.class);
    }

    public List<Propriedade> listarPorNome() {
        return listar().stream()
                .sorted(Comparator.comparing(Propriedade::getNome, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public boolean existeComNomeEMunicipio(String nome, String municipio, long ignorarId) {
        return listar().stream().anyMatch(p -> p.getId() != ignorarId
                && p.getNome().equalsIgnoreCase(nome.trim())
                && p.getMunicipio().equalsIgnoreCase(municipio.trim()));
    }
}
