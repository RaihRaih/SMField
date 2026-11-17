package infra;

import domain.IndicadorAmbiental;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** Persistência dos registros de indicadores ambientais (histórico) em {@code indicadores.json}. */
public class IndicadorRepository extends JsonRepository<IndicadorAmbiental> {

    private static final Comparator<IndicadorAmbiental> CRONOLOGICO =
            Comparator.comparing(IndicadorAmbiental::getDataHora).thenComparingLong(IndicadorAmbiental::getId);

    public IndicadorRepository(Path diretorio) {
        super(diretorio.resolve("indicadores.json"), IndicadorAmbiental.class);
    }

    /** Registros do talhão em ordem cronológica (mais antigo primeiro). */
    public List<IndicadorAmbiental> listarPorTalhao(long talhaoId) {
        return filtrar(i -> i.getTalhaoId() == talhaoId).stream().sorted(CRONOLOGICO).toList();
    }

    /** Registro mais recente do talhão. */
    public Optional<IndicadorAmbiental> ultimoDoTalhao(long talhaoId) {
        return filtrar(i -> i.getTalhaoId() == talhaoId).stream().max(CRONOLOGICO);
    }

    public int removerPorTalhao(long talhaoId) {
        return removerSe(i -> i.getTalhaoId() == talhaoId);
    }
}
