package ui;

import domain.Alerta;
import domain.IndicadorAmbiental;
import domain.RegistroHistorico;
import domain.TipoAlerta;
import service.ServicosSMField;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;

/** RF-11 (histórico de indicadores e riscos) e RF-12 (alertas internos). */
public class HistoricoAlertasPanel extends JPanel implements Atualizavel {

    private final ServicosSMField servicos;

    private final DefaultTableModel modeloAlertas = Estilo.modelo("Tipo", "Propriedade", "Talhão", "Mensagem", "Data");
    private final JTable tabelaAlertas = Estilo.tabela(modeloAlertas);
    private final JLabel avisoAlertas = Estilo.nota(" ");

    private final JComboBox<Estilo.OpcaoTalhao> comboTalhao = new JComboBox<>();
    private final DefaultTableModel modeloHistorico = Estilo.modelo("Data/hora", "Solo (%)", "Temp. (°C)", "Ar (%)",
            "Precip. 24 h (mm)", "Índice", "Classificação", "Variação");
    private final JTable tabelaHistorico = Estilo.tabela(modeloHistorico);
    private final JLabel avisoHistorico = Estilo.nota(" ");

    public HistoricoAlertasPanel(ServicosSMField servicos) {
        this.servicos = servicos;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setBackground(Estilo.CINZA_FUNDO);
        add(Estilo.titulo("Histórico e alertas"), BorderLayout.NORTH);

        JPanel alertas = new JPanel(new BorderLayout(4, 4));
        alertas.setBorder(Estilo.secao("Alertas internos"));
        tabelaAlertas.getColumnModel().getColumn(0).setCellRenderer(new RenderizadorAlerta());
        tabelaAlertas.getColumnModel().getColumn(0).setPreferredWidth(170);
        tabelaAlertas.getColumnModel().getColumn(3).setPreferredWidth(520);
        alertas.add(Estilo.rolagem(tabelaAlertas), BorderLayout.CENTER);
        alertas.add(avisoAlertas, BorderLayout.SOUTH);

        JPanel historico = new JPanel(new BorderLayout(4, 4));
        historico.setBorder(Estilo.secao("Histórico cronológico do talhão (registros anteriores preservados)"));
        JPanel selecao = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selecao.add(new JLabel("Talhão:"));
        selecao.add(comboTalhao);
        historico.add(selecao, BorderLayout.NORTH);
        tabelaHistorico.getColumnModel().getColumn(6).setCellRenderer(Estilo.renderizadorNivel());
        historico.add(Estilo.rolagem(tabelaHistorico), BorderLayout.CENTER);
        historico.add(avisoHistorico, BorderLayout.SOUTH);

        JSplitPane divisao = new JSplitPane(JSplitPane.VERTICAL_SPLIT, alertas, historico);
        divisao.setResizeWeight(0.45);
        divisao.setBorder(null);
        add(divisao, BorderLayout.CENTER);

        comboTalhao.addActionListener(e -> carregarHistorico());
        atualizar();
    }

    private void carregarAlertas() {
        modeloAlertas.setRowCount(0);
        List<Alerta> lista = servicos.monitoramento().alertas();
        for (Alerta a : lista) {
            modeloAlertas.addRow(new Object[]{a.tipo(), a.propriedade(), a.talhao(), a.mensagem(),
                    Estilo.formatar(a.dataHora())});
        }
        long altos = lista.stream().filter(a -> a.tipo() == TipoAlerta.RISCO_ALTO).count();
        avisoAlertas.setText(lista.isEmpty() ? "Nenhum alerta no momento."
                : lista.size() + " alerta(s), sendo " + altos + " de risco alto.");
    }

    private void carregarHistorico() {
        modeloHistorico.setRowCount(0);
        long id = Estilo.idTalhao(comboTalhao);
        if (id <= 0) {
            avisoHistorico.setText(comboTalhao.getItemCount() == 0 ? "Nenhum talhão cadastrado." : "Selecione um talhão.");
            return;
        }
        List<RegistroHistorico> hist = servicos.monitoramento().historico(id);
        for (RegistroHistorico h : hist) {
            IndicadorAmbiental i = h.indicador();
            String variacao = h.nivelAnterior() == null ? "primeiro registro"
                    : h.mudouClassificacao() ? Estilo.textoNivel(h.nivelAnterior()) + " → " + Estilo.textoNivel(h.resultado().getNivel())
                    : "sem mudança";
            modeloHistorico.addRow(new Object[]{Estilo.formatar(i.getDataHora()), Estilo.numero(i.getUmidadeSolo()),
                    Estilo.numero(i.getTemperatura()), Estilo.numero(i.getUmidadeAr()),
                    Estilo.numero(i.getPrecipitacao24h()), h.resultado().getIndice(), h.resultado().getNivel(), variacao});
        }
        avisoHistorico.setText(hist.isEmpty() ? "Sem registros de indicadores para este talhão."
                : hist.size() + " registro(s) em ordem cronológica (mais antigo primeiro).");
    }

    @Override
    public void atualizar() {
        carregarAlertas();
        Estilo.carregarTalhoes(comboTalhao, servicos);
        carregarHistorico();
    }

    private static final class RenderizadorAlerta extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setFont(Estilo.NORMAL.deriveFont(Font.BOLD));
            setForeground(Color.WHITE);
            if (value instanceof TipoAlerta t) {
                setBackground(switch (t) {
                    case RISCO_ALTO -> Estilo.RISCO_ALTO;
                    case MUDANCA_CLASSIFICACAO -> Estilo.RISCO_MEDIO;
                    case SEM_INDICADORES -> Estilo.RISCO_NAO_CALCULADO;
                });
            }
            return this;
        }
    }
}
