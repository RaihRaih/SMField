package ui;

import domain.Alerta;
import domain.IndicadorAmbiental;
import domain.RegistroHistorico;
import domain.TipoAlerta;
import service.ServicosSMField;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

/** RF-11 (histórico de indicadores e riscos) e RF-12 (alertas internos). */
public class HistoricoAlertasPanel extends JPanel implements Atualizavel {

    private final ServicosSMField servicos;

    private final DefaultTableModel modeloAlertas = Estilo.modelo("Tipo", "Propriedade", "Talhão", "Mensagem", "Data");
    private final JTable tabelaAlertas = Estilo.tabela(modeloAlertas);
    private final JLabel avisoAlertas = Estilo.nota(" ");
    private final JLabel contAlto = Estilo.selo(pilulaAlerta(TipoAlerta.RISCO_ALTO));
    private final JLabel contMudanca = Estilo.selo(pilulaAlerta(TipoAlerta.MUDANCA_CLASSIFICACAO));
    private final JLabel contSemDados = Estilo.selo(pilulaAlerta(TipoAlerta.SEM_INDICADORES));

    private final JComboBox<Estilo.OpcaoTalhao> comboTalhao = new JComboBox<>();
    private final DefaultTableModel modeloHistorico = Estilo.modelo("Data/hora", "Solo (%)", "Temp. (°C)", "Ar (%)",
            "Precip. 24 h (mm)", "Índice", "Classificação", "Variação", "Fonte dos dados");
    private final JTable tabelaHistorico = Estilo.tabela(modeloHistorico);
    private final JLabel avisoHistorico = Estilo.nota(" ");
    private final Graficos.SerieTemporal grafico = new Graficos.SerieTemporal(true, false)
            .vazio("Sem registros de indicadores para este talhão.");

    public HistoricoAlertasPanel(ServicosSMField servicos) {
        this.servicos = servicos;
        Estilo.pagina(this);
        add(Estilo.cabecalho("Histórico e alertas",
                "Alertas internos do monitoramento e a evolução do risco de cada talhão ao longo do tempo."),
                BorderLayout.NORTH);

        Cartao alertas = new Cartao("Alertas internos", "Ordenados por severidade", Icone.Tipo.ALERTA);
        alertas.acoes(contAlto, contMudanca, contSemDados);
        tabelaAlertas.getColumnModel().getColumn(0).setCellRenderer(
                Estilo.renderizadorPilula(v -> v instanceof TipoAlerta t ? pilulaAlerta(t) : null));
        tabelaAlertas.getColumnModel().getColumn(0).setMinWidth(230);
        tabelaAlertas.getColumnModel().getColumn(0).setMaxWidth(250);
        tabelaAlertas.getColumnModel().getColumn(1).setPreferredWidth(200);
        tabelaAlertas.getColumnModel().getColumn(2).setPreferredWidth(170);
        tabelaAlertas.getColumnModel().getColumn(3).setPreferredWidth(520);
        tabelaAlertas.getColumnModel().getColumn(4).setMinWidth(142);
        tabelaAlertas.getColumnModel().getColumn(4).setMaxWidth(150);
        alertas.corpo().add(Estilo.rolagem(tabelaAlertas), BorderLayout.CENTER);
        alertas.corpo().add(avisoAlertas, BorderLayout.SOUTH);

        Cartao historico = new Cartao("Evolução do risco do talhão",
                "Registros anteriores preservados, em ordem cronológica · pontos destacados = mudança de classificação",
                Icone.Tipo.TENDENCIA);
        JLabel rotulo = new JLabel("Talhão");
        rotulo.setFont(Estilo.ROTULO);
        rotulo.setForeground(Estilo.TEXTO_SUAVE);
        comboTalhao.setPreferredSize(new Dimension(380, comboTalhao.getPreferredSize().height));
        historico.acoes(rotulo, comboTalhao);
        grafico.setPreferredSize(new Dimension(600, 180));
        grafico.setMinimumSize(new Dimension(200, 140));
        tabelaHistorico.getColumnModel().getColumn(0).setMinWidth(142);
        for (int i = 1; i <= 4; i++) {
            tabelaHistorico.getColumnModel().getColumn(i).setMinWidth(i == 4 ? 120 : 84);
        }
        tabelaHistorico.getColumnModel().getColumn(5).setCellRenderer(Estilo.renderizadorContagem());
        tabelaHistorico.getColumnModel().getColumn(5).setMaxWidth(80);
        tabelaHistorico.getColumnModel().getColumn(6).setCellRenderer(Estilo.renderizadorNivel());
        tabelaHistorico.getColumnModel().getColumn(6).setMinWidth(150);
        tabelaHistorico.getColumnModel().getColumn(7).setCellRenderer(Estilo.renderizadorPilula(HistoricoAlertasPanel::pilulaVariacao));
        tabelaHistorico.getColumnModel().getColumn(7).setMinWidth(190);
        tabelaHistorico.getColumnModel().getColumn(8).setPreferredWidth(240);
        JPanel corpoHistorico = new JPanel(new BorderLayout(0, 12));
        corpoHistorico.setOpaque(false);
        corpoHistorico.add(grafico, BorderLayout.NORTH);
        corpoHistorico.add(Estilo.rolagem(tabelaHistorico), BorderLayout.CENTER);
        historico.corpo().add(corpoHistorico, BorderLayout.CENTER);
        historico.corpo().add(avisoHistorico, BorderLayout.SOUTH);

        alertas.setMinimumSize(new Dimension(300, 170));
        historico.setMinimumSize(new Dimension(300, 300));
        JSplitPane divisao = new JSplitPane(JSplitPane.VERTICAL_SPLIT, alertas, historico);
        divisao.setResizeWeight(0.36);
        divisao.setBorder(null);
        divisao.setOpaque(false);
        divisao.setDividerSize(14);
        add(divisao, BorderLayout.CENTER);

        comboTalhao.addActionListener(e -> carregarHistorico());
        atualizar();
    }

    /** Selo do tipo de alerta, com ícone e cor de severidade. */
    static Estilo.Pilula pilulaAlerta(TipoAlerta t) {
        return switch (t) {
            case RISCO_ALTO -> new Estilo.Pilula(t.toString(), new Color(0xFDECEC), new Color(0xA92424), Icone.Tipo.ALERTA);
            case MUDANCA_CLASSIFICACAO -> new Estilo.Pilula(t.toString(), new Color(0xFEF3E0), new Color(0x8A5200), Icone.Tipo.MUDANCA);
            case SEM_INDICADORES -> new Estilo.Pilula(t.toString(), new Color(0xEEF1EE), new Color(0x4F5A52), Icone.Tipo.CIRCULO);
        };
    }

    private static Estilo.Pilula pilulaVariacao(Object v) {
        String t = String.valueOf(v);
        if (t.contains("→")) {
            return new Estilo.Pilula(t, Estilo.ATENCAO_TINTA, Estilo.ATENCAO_TEXTO, Icone.Tipo.MUDANCA);
        }
        return new Estilo.Pilula(t, new Color(0xF3F5F2), Estilo.TEXTO_SUAVE, null);
    }

    private void carregarAlertas() {
        modeloAlertas.setRowCount(0);
        List<Alerta> lista = servicos.monitoramento().alertas();
        for (Alerta a : lista) {
            modeloAlertas.addRow(new Object[]{a.tipo(), a.propriedade(), a.talhao(), a.mensagem(),
                    Estilo.formatar(a.dataHora())});
        }
        long altos = lista.stream().filter(a -> a.tipo() == TipoAlerta.RISCO_ALTO).count();
        long mudancas = lista.stream().filter(a -> a.tipo() == TipoAlerta.MUDANCA_CLASSIFICACAO).count();
        long semDados = lista.stream().filter(a -> a.tipo() == TipoAlerta.SEM_INDICADORES).count();
        Estilo.atualizarSelo(contAlto, contador(TipoAlerta.RISCO_ALTO, altos));
        Estilo.atualizarSelo(contMudanca, contador(TipoAlerta.MUDANCA_CLASSIFICACAO, mudancas));
        Estilo.atualizarSelo(contSemDados, contador(TipoAlerta.SEM_INDICADORES, semDados));
        avisoAlertas.setText(lista.isEmpty() ? "Nenhum alerta no momento."
                : lista.size() + " alerta(s), sendo " + altos + " de risco alto.");
    }

    private static Estilo.Pilula contador(TipoAlerta t, long n) {
        Estilo.Pilula base = pilulaAlerta(t);
        return new Estilo.Pilula(n + " · " + t, base.fundo(), base.cor(), base.icone());
    }

    private void carregarHistorico() {
        modeloHistorico.setRowCount(0);
        long id = Estilo.idTalhao(comboTalhao);
        if (id <= 0) {
            avisoHistorico.setText(comboTalhao.getItemCount() == 0 ? "Nenhum talhão cadastrado." : "Selecione um talhão.");
            grafico.mostrar(List.of());
            return;
        }
        List<RegistroHistorico> hist = servicos.monitoramento().historico(id);
        List<Graficos.Ponto> pontos = new ArrayList<>();
        for (RegistroHistorico h : hist) {
            IndicadorAmbiental i = h.indicador();
            String variacao = h.nivelAnterior() == null ? "primeiro registro"
                    : h.mudouClassificacao() ? Estilo.textoNivel(h.nivelAnterior()) + " → " + Estilo.textoNivel(h.resultado().getNivel())
                    : "sem mudança";
            modeloHistorico.addRow(new Object[]{Estilo.formatar(i.getDataHora()), Estilo.numero(i.getUmidadeSolo()),
                    Estilo.numero(i.getTemperatura()), Estilo.numero(i.getUmidadeAr()),
                    Estilo.numero(i.getPrecipitacao24h()), h.resultado().getIndice(), h.resultado().getNivel(), variacao,
                    i.getFonte()});
            if (h.resultado().isCalculado()) {
                pontos.add(new Graficos.Ponto(i.getDataHora(), h.resultado().getIndice(), h.resultado().getNivel(),
                        h.mudouClassificacao()));
            }
        }
        grafico.mostrar(pontos);
        avisoHistorico.setText(hist.isEmpty() ? "Sem registros de indicadores para este talhão."
                : hist.size() + " registro(s) em ordem cronológica (mais antigo primeiro).");
    }

    @Override
    public void atualizar() {
        carregarAlertas();
        Estilo.carregarTalhoes(comboTalhao, servicos);
        carregarHistorico();
    }
}
