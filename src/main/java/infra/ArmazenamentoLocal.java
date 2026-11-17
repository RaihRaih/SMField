package infra;

import java.nio.file.Path;

/**
 * Agrupa os repositórios que gravam no mesmo diretório de dados.
 * <p>
 * Diretório padrão: {@code ./dados} (relativo à pasta onde o programa é iniciado), ou o valor da
 * propriedade de sistema {@code smfield.dados}.
 */
public class ArmazenamentoLocal {

    public static final String PROPRIEDADE_DIRETORIO = "smfield.dados";

    private final Path diretorio;
    private final PropriedadeRepository propriedades;
    private final TalhaoRepository talhoes;
    private final IndicadorRepository indicadores;
    private final OcorrenciaRepository ocorrencias;

    public ArmazenamentoLocal(Path diretorio) {
        this.diretorio = diretorio;
        this.propriedades = new PropriedadeRepository(diretorio);
        this.talhoes = new TalhaoRepository(diretorio);
        this.indicadores = new IndicadorRepository(diretorio);
        this.ocorrencias = new OcorrenciaRepository(diretorio);
    }

    public static Path diretorioPadrao() {
        String configurado = System.getProperty(PROPRIEDADE_DIRETORIO);
        if (configurado != null && !configurado.isBlank()) {
            return Path.of(configurado);
        }
        return Path.of("dados");
    }

    public Path getDiretorio() {
        return diretorio;
    }

    public PropriedadeRepository propriedades() {
        return propriedades;
    }

    public TalhaoRepository talhoes() {
        return talhoes;
    }

    public IndicadorRepository indicadores() {
        return indicadores;
    }

    public OcorrenciaRepository ocorrencias() {
        return ocorrencias;
    }
}
