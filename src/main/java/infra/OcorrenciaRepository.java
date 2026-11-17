package infra;

import domain.OcorrenciaCampo;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

/** Persistência das ocorrências de campo em {@code ocorrencias.json}. */
public class OcorrenciaRepository extends JsonRepository<OcorrenciaCampo> {

    public OcorrenciaRepository(Path diretorio) {
        super(diretorio.resolve("ocorrencias.json"), OcorrenciaCampo.class);
    }

    /** Ocorrências do talhão, mais recentes primeiro. */
    public List<OcorrenciaCampo> listarPorTalhao(long talhaoId) {
        return filtrar(o -> o.getTalhaoId() == talhaoId).stream()
                .sorted(Comparator.comparing(OcorrenciaCampo::getData).reversed()
                        .thenComparing(Comparator.comparingLong(OcorrenciaCampo::getId).reversed()))
                .toList();
    }

    public int removerPorTalhao(long talhaoId) {
        return removerSe(o -> o.getTalhaoId() == talhaoId);
    }
}
