package ui;

import domain.FatorRisco;
import domain.IndicadorAmbiental;
import domain.NivelRisco;
import domain.OcorrenciaCampo;
import service.MonitoramentoService;
import service.RiscoService;
import service.ServicosSMField;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/** RF-08: consulta individual de indicadores, índice, classificação, justificativa e ocorrências. */
public class DetalhesRiscoPanel extends JPanel implements Atualizavel {

    private final ServicosSMField servicos;

    private final JComboBox<Estilo.OpcaoTalhao> comboTalhao = new JComboBox<>();
    private final JButton botaoAtualizar = Estilo.botao("Atualizar", Icone.Tipo.ATUALIZAR);
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
        Estilo.pagina(this);
        comboTalhao.setPreferredSize(new Dimension(430, comboTalhao.getPreferredSize().height));
        comboTalhao.setToolTipText("Talhão consultado");
        JLabel rotulo = Estilo.rotuloCampo("Talhão", false);
        add(Estilo.cabecalho("Consulta de risco",
                "Índice, classificação, justificativa e indicadores usados no cálculo.",
                rotulo, comboTalhao, botaoAtualizar), BorderLayout.NORTH);

        Cartao info = new Cartao("Talhão", "Dados do último registro", Icone.Tipo.TALHAO);
        JPanel fichas = new JPanel();
        fichas.setOpaque(false);
        fichas.setLayout(new BoxLayout(fichas, BoxLayout.Y_AXIS));
        adicionarFicha(fichas, Estilo.informacao(Icone.Tipo.PROPRIEDADE, "Propriedade (município)", infoPropriedade));
        adicionarFicha(fichas, Estilo.informacao(Icone.Tipo.BROTO, "Cultura / área", infoCultura));
        adicionarFicha(fichas, Estilo.informacao(Icone.Tipo.CALENDARIO, "Data do registro", infoData));
        adicionarFicha(fichas, Estilo.informacao(Icone.Tipo.HISTORICO, "Registros no histórico", infoRegistros));
        adicionarFicha(fichas, Estilo.informacao(Icone.Tipo.DADOS, "Fonte dos dados", infoFonte));
        info.corpo().add(fichas, BorderLayout.NORTH);

        Cartao painelResultado = new Cartao("Índice de risco", "Classificação e justificativa geradas pelo sistema",
                Icone.Tipo.CONSULTA);
        painelResultado.corpo().add(resultado, BorderLayout.CENTER);
        painelResultado.corpo().add(Estilo.dica(RiscoService.AVISO_ACADEMICO), BorderLayout.SOUTH);

        int[] larguras = {175, 105, 120, 330, 150, 64, 130};
        int[] minimas = {170, 100, 110, 160, 140, 60, 120};
        for (int i = 0; i < larguras.length; i++) {
            tabelaFatores.getColumnModel().getColumn(i).setPreferredWidth(larguras[i]);
            tabelaFatores.getColumnModel().getColumn(i).setMinWidth(minimas[i]);
        }
        tabelaFatores.getColumnModel().getColumn(0).setCellRenderer(new RenderizadorIndicador());
        tabelaFatores.getColumnModel().getColumn(4).setCellRenderer(Estilo.renderizadorPilula(DetalhesRiscoPanel::pilulaPenalidade));
        tabelaFatores.getColumnModel().getColumn(6).setCellRenderer(new RenderizadorPontos());
        tabelaOcorrencias.getColumnModel().getColumn(0).setMinWidth(100);
        tabelaOcorrencias.getColumnModel().getColumn(0).setMaxWidth(120);
        tabelaOcorrencias.getColumnModel().getColumn(1).setPreferredWidth(170);
        tabelaOcorrencias.getColumnModel().getColumn(1).setMaxWidth(220);
        tabelaOcorrencias.getColumnModel().getColumn(1).setCellRenderer(OcorrenciasPanel.renderizadorTipo());
        tabelaOcorrencias.getColumnModel().getColumn(2).setPreferredWidth(700);

        Cartao fatores = new Cartao("Composição do índice",
                "Pesos: umidade do solo 35% · temperatura 30% · umidade do ar 15% · precipitação 20% · "
                        + "penalidade 0 (ideal), 50 (intermediária) ou 100 (crítica)", Icone.Tipo.INDICADORES);
        javax.swing.JScrollPane rolagemFatores = Estilo.rolagem(tabelaFatores);
        rolagemFatores.setPreferredSize(new Dimension(600, 38 + 4 * 37 + 4));
        fatores.corpo().add(rolagemFatores, BorderLayout.CENTER);

        Cartao ocorr = new Cartao("Ocorrências de campo", "Informativas — não alteram o índice numérico de risco",
                Icone.Tipo.OCORRENCIAS);
        javax.swing.JScrollPane rolagemOcorr = Estilo.rolagem(tabelaOcorrencias);
        rolagemOcorr.setPreferredSize(new Dimension(600, 38 + 3 * 37 + 4));
        ocorr.corpo().add(rolagemOcorr, BorderLayout.CENTER);
        ocorr.corpo().add(avisoOcorrencias, BorderLayout.SOUTH);

        JPanel conteudo = new JPanel(new GridBagLayout());
        conteudo.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        info.setPreferredSize(new Dimension(370, 318));
        conteudo.add(info, c);
        c.gridx = 1;
        c.weightx = 1;
        c.insets = new Insets(0, 16, 0, 0);
        painelResultado.setPreferredSize(new Dimension(600, 318));
        conteudo.add(painelResultado, c);
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 2;
        c.insets = new Insets(16, 0, 0, 0);
        c.weighty = 0.55;
        conteudo.add(fatores, c);
        c.gridy = 2;
        c.weighty = 0.45;
        conteudo.add(ocorr, c);
        add(Estilo.rolavel(conteudo), BorderLayout.CENTER);

        comboTalhao.addActionListener(e -> mostrar());
        botaoAtualizar.addActionListener(e -> atualizar());
        atualizar();
    }

    private static void adicionarFicha(JPanel destino, JComponent ficha) {
        if (destino.getComponentCount() > 0) {
            destino.add(Box.createVerticalStrut(7));
        }
        ficha.setAlignmentX(Component.LEFT_ALIGNMENT);
        destino.add(ficha);
    }

    private static Estilo.Pilula pilulaPenalidade(Object v) {
        if (!(v instanceof Number n)) {
            return null;
        }
        int p = n.intValue();
        NivelRisco nivel = p == 0 ? NivelRisco.BAIXO : p >= 100 ? NivelRisco.ALTO : NivelRisco.MEDIO;
        String texto = p + (p == 0 ? " · ideal" : p >= 100 ? " · crítica" : " · intermediária");
        return new Estilo.Pilula(texto, Estilo.tintaDoNivel(nivel), Estilo.textoDoNivel(nivel), null);
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
        infoPropriedade.setToolTipText(infoPropriedade.getText());
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

    /** Nome do indicador com o ícone correspondente. */
    private static final class RenderizadorIndicador extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String t = String.valueOf(value).toLowerCase(java.util.Locale.ROOT);
            Icone.Tipo tipo = t.contains("solo") ? Icone.Tipo.SOLO : t.contains("temperatura") ? Icone.Tipo.TEMPERATURA
                    : t.contains("ar") && !t.contains("precip") ? Icone.Tipo.AR : Icone.Tipo.CHUVA;
            setIcon(Icone.de(tipo, 16, Estilo.AGRICOLA));
            setIconTextGap(9);
            setFont(Estilo.MEDIA);
            return this;
        }
    }

    /** Pontos que o indicador somou ao índice, com uma barra proporcional ao máximo do seu peso. */
    private static final class RenderizadorPontos extends DefaultTableCellRenderer {
        private double fracao;
        private Color cor = Estilo.AGRICOLA;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setFont(Estilo.NEGRITO);
            int modelo = table.convertRowIndexToModel(row);
            Object pen = table.getModel().getValueAt(modelo, 4);
            int p = pen instanceof Number n ? n.intValue() : 0;
            fracao = p / 100.0;
            NivelRisco nivel = p == 0 ? NivelRisco.BAIXO : p >= 100 ? NivelRisco.ALTO : NivelRisco.MEDIO;
            cor = Estilo.corDoNivel(nivel);
            setForeground(Estilo.textoDoNivel(nivel));
            setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 60));
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = Graficos.preparar(g);
            int largura = 46;
            int x = getWidth() - largura - 12;
            int y = getHeight() / 2 - 3;
            g2.setColor(new Color(0xEEF1EE));
            g2.fillRoundRect(x, y, largura, 6, 6, 6);
            g2.setColor(cor);
            g2.fillRoundRect(x, y, (int) Math.max(fracao > 0 ? 6 : 0, largura * fracao), 6, 6, 6);
            g2.dispose();
        }
    }
}
