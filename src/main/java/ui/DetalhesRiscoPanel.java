package ui;

import domain.FatorRisco;
import domain.IndicadorAmbiental;
import domain.OcorrenciaCampo;
import service.MonitoramentoService;
import service.RiscoService;
import service.ServicosSMField;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;

/** RF-08: consulta individual de indicadores, índice, classificação, justificativa e ocorrências. */
public class DetalhesRiscoPanel extends JPanel implements Atualizavel {

    private final ServicosSMField servicos;

    private final JComboBox<Estilo.OpcaoTalhao> comboTalhao = new JComboBox<>();
    private final JButton botaoAtualizar = Estilo.botao("Atualizar");
    private final JLabel infoPropriedade = new JLabel("—");
    private final JLabel infoCultura = new JLabel("—");
    private final JLabel infoData = new JLabel("—");
    private final JLabel infoRegistros = new JLabel("—");
    private final JLabel infoFonte = new JLabel("—");
    private final ResultadoRiscoView resultado = new ResultadoRiscoView();
    private final DefaultTableModel modeloFatores = Estilo.modelo("Indicador", "Valor medido", "Faixa de referência",
            "Situação", "Penalidade", "Peso", "Pontos no índice");
    private final JTable tabelaFatores = Estilo.tabela(modeloFatores);
    private final DefaultTableModel modeloOcorrencias = Estilo.modelo("Data", "Tipo", "Descrição");
    private final JTable tabelaOcorrencias = Estilo.tabela(modeloOcorrencias);
    private final JLabel avisoOcorrencias = Estilo.nota(" ");

    public DetalhesRiscoPanel(ServicosSMField servicos) {
        this.servicos = servicos;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setBackground(Estilo.CINZA_FUNDO);

        JPanel topo = new JPanel(new BorderLayout(6, 6));
        topo.setOpaque(false);
        topo.add(Estilo.titulo("Consulta de risco do talhão"), BorderLayout.NORTH);
        JPanel selecao = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selecao.setOpaque(false);
        selecao.add(new JLabel("Talhão:"));
        selecao.add(comboTalhao);
        selecao.add(botaoAtualizar);
        topo.add(selecao, BorderLayout.CENTER);
        add(topo, BorderLayout.NORTH);

        JPanel info = new JPanel(new GridLayout(5, 2, 4, 2));
        info.setBorder(Estilo.secao("Talhão"));
        info.add(new JLabel("Propriedade:"));
        info.add(infoPropriedade);
        info.add(new JLabel("Cultura / área:"));
        info.add(infoCultura);
        info.add(new JLabel("Data do registro:"));
        info.add(infoData);
        info.add(new JLabel("Registros no histórico:"));
        info.add(infoRegistros);
        info.add(new JLabel("Fonte dos dados:"));
        info.add(infoFonte);

        JPanel painelResultado = new JPanel(new BorderLayout(4, 4));
        painelResultado.setBorder(Estilo.secao("Índice, classificação e justificativa"));
        painelResultado.add(resultado, BorderLayout.CENTER);
        painelResultado.add(Estilo.nota(RiscoService.AVISO_ACADEMICO), BorderLayout.SOUTH);

        JPanel linhaSuperior = new JPanel(new BorderLayout(10, 10));
        linhaSuperior.setOpaque(false);
        linhaSuperior.add(info, BorderLayout.WEST);
        linhaSuperior.add(painelResultado, BorderLayout.CENTER);

        int[] larguras = {130, 100, 120, 380, 80, 60, 110};
        for (int i = 0; i < larguras.length; i++) {
            tabelaFatores.getColumnModel().getColumn(i).setPreferredWidth(larguras[i]);
        }
        tabelaOcorrencias.getColumnModel().getColumn(0).setMinWidth(100);
        tabelaOcorrencias.getColumnModel().getColumn(0).setMaxWidth(120);
        tabelaOcorrencias.getColumnModel().getColumn(1).setPreferredWidth(160);
        tabelaOcorrencias.getColumnModel().getColumn(2).setPreferredWidth(700);
        JPanel fatores = new JPanel(new BorderLayout());
        fatores.setBorder(Estilo.secao("Indicadores utilizados no cálculo (pesos: solo 35%, temperatura 30%, ar 15%, precipitação 20%)"));
        fatores.add(Estilo.rolagem(tabelaFatores), BorderLayout.CENTER);

        JPanel ocorr = new JPanel(new BorderLayout(4, 4));
        ocorr.setBorder(Estilo.secao("Ocorrências de campo (informativas — não alteram o índice)"));
        ocorr.add(Estilo.rolagem(tabelaOcorrencias), BorderLayout.CENTER);
        ocorr.add(avisoOcorrencias, BorderLayout.SOUTH);

        JPanel inferior = new JPanel(new GridLayout(2, 1, 10, 10));
        inferior.setOpaque(false);
        inferior.add(fatores);
        inferior.add(ocorr);

        JPanel centro = new JPanel(new BorderLayout(10, 10));
        centro.setOpaque(false);
        centro.add(linhaSuperior, BorderLayout.NORTH);
        centro.add(inferior, BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);

        comboTalhao.addActionListener(e -> mostrar());
        botaoAtualizar.addActionListener(e -> atualizar());
        atualizar();
    }

    private void mostrar() {
        modeloFatores.setRowCount(0);
        modeloOcorrencias.setRowCount(0);
        long id = Estilo.idTalhao(comboTalhao);
        if (id <= 0 || servicos.talhoes().buscar(id).isEmpty()) {
            infoPropriedade.setText("—");
            infoCultura.setText("—");
            infoData.setText("—");
            infoRegistros.setText("—");
            infoFonte.setText("—");
            infoFonte.setToolTipText(null);
            resultado.limpar(comboTalhao.getItemCount() == 0
                    ? "Nenhum talhão cadastrado." : "Selecione um talhão.");
            avisoOcorrencias.setText(" ");
            return;
        }
        MonitoramentoService.DetalheTalhao d = servicos.monitoramento().detalhe(id);
        infoPropriedade.setText(d.propriedade() == null ? "—"
                : d.propriedade().getNome() + " (" + d.propriedade().getMunicipio() + ")");
        infoCultura.setText(d.talhao().getCultura() + " — " + Estilo.numero(d.talhao().getAreaHectares()) + " ha");
        IndicadorAmbiental ult = d.ultimoRegistro();
        infoData.setText(ult == null ? "sem registros" : Estilo.formatar(ult.getDataHora()));
        infoRegistros.setText(String.valueOf(d.totalRegistros()));
        infoFonte.setText(ult == null ? "—" : ult.isDadoInmet() ? "INMET (real) + solo simulado" : ult.getFonte());
        infoFonte.setToolTipText(ult == null ? null : ult.getFonte());
        resultado.mostrar(d.resultado());
        for (FatorRisco f : d.resultado().getFatores()) {
            modeloFatores.addRow(new Object[]{f.indicador(), Estilo.numero(f.valor()) + " " + f.unidade(),
                    f.faixaIdeal(), f.isDesvio() ? f.situacao() : "dentro da faixa", f.penalidade(),
                    Math.round(f.peso() * 100) + "%", Estilo.numero(f.contribuicao())});
        }
        for (OcorrenciaCampo o : d.ocorrencias()) {
            modeloOcorrencias.addRow(new Object[]{Estilo.formatar(o.getData()), o.getTipo(), o.getDescricao()});
        }
        avisoOcorrencias.setText(d.ocorrencias().isEmpty() ? "Nenhuma ocorrência registrada para este talhão."
                : d.ocorrencias().size() + " ocorrência(s).");
    }

    public void selecionarTalhao(long talhaoId) {
        Estilo.selecionarTalhao(comboTalhao, talhaoId);
        mostrar();
    }

    @Override
    public void atualizar() {
        Estilo.carregarTalhoes(comboTalhao, servicos);
        mostrar();
    }
}
