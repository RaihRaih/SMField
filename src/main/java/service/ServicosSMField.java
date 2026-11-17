package service;

import infra.ArmazenamentoLocal;

import java.nio.file.Path;
import java.time.Clock;

/**
 * Ponto único de criação dos serviços, compartilhando os mesmos repositórios.
 * Assim todas as telas trabalham com os mesmos dados persistidos.
 */
public class ServicosSMField {

    private final ArmazenamentoLocal armazenamento;
    private final PropriedadeService propriedades;
    private final TalhaoService talhoes;
    private final IndicadorService indicadores;
    private final RiscoService risco;
    private final OcorrenciaService ocorrencias;
    private final MonitoramentoService monitoramento;
    private final CompatibilidadeService compatibilidade;
    private final DadosDemonstracaoService demonstracao;

    public ServicosSMField(Path diretorioDados, Clock relogio) {
        this.armazenamento = new ArmazenamentoLocal(diretorioDados);
        this.risco = new RiscoService();
        this.propriedades = new PropriedadeService(armazenamento.propriedades(), armazenamento.talhoes());
        this.talhoes = new TalhaoService(armazenamento.propriedades(), armazenamento.talhoes(),
                armazenamento.indicadores(), armazenamento.ocorrencias());
        this.indicadores = new IndicadorService(armazenamento.talhoes(), armazenamento.indicadores(), relogio);
        this.ocorrencias = new OcorrenciaService(armazenamento.talhoes(), armazenamento.ocorrencias(), relogio);
        this.monitoramento = new MonitoramentoService(armazenamento.propriedades(), armazenamento.talhoes(),
                armazenamento.indicadores(), armazenamento.ocorrencias(), risco);
        this.compatibilidade = new CompatibilidadeService(armazenamento.propriedades(), armazenamento.talhoes(),
                armazenamento.indicadores());
        this.demonstracao = new DadosDemonstracaoService(propriedades, talhoes, indicadores, ocorrencias, relogio);
    }

    public ServicosSMField(Path diretorioDados) {
        this(diretorioDados, Clock.systemDefaultZone());
    }

    public Path diretorioDados() {
        return armazenamento.getDiretorio();
    }

    public PropriedadeService propriedades() {
        return propriedades;
    }

    public TalhaoService talhoes() {
        return talhoes;
    }

    public IndicadorService indicadores() {
        return indicadores;
    }

    public RiscoService risco() {
        return risco;
    }

    public OcorrenciaService ocorrencias() {
        return ocorrencias;
    }

    public MonitoramentoService monitoramento() {
        return monitoramento;
    }

    public CompatibilidadeService compatibilidade() {
        return compatibilidade;
    }

    public DadosDemonstracaoService demonstracao() {
        return demonstracao;
    }
}
