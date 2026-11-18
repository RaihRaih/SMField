package ui;

import domain.NivelRisco;
import service.MonitoramentoService;
import service.RiscoService;
import service.ServicosSMField;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
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
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.LongConsumer;

/** RF-07: painel consolidado de riscos, ordenado do maior para o menor risco. */
public class DashboardPanel extends JPanel implements Atualizavel {

    private final ServicosSMField servicos;
    private final LongConsumer abrirDetalhes;
    private final Runnable carregarDemonstracao;

    private final Map<NivelRisco, JLabel> contadores = new EnumMap<>(NivelRisco.class);
    private final Map<NivelRisco, JLabel> percentuais = new EnumMap<>(NivelRisco.class);
    private final DefaultTableModel modelo = Estilo.modelo("ID", "Propriedade", "Talhão", "Cultura", "Área (ha)",
            "Último índice", "Classificação", "Última atualização");
    private final JTable tabela = Estilo.tabela(modelo);
    private final JLabel aviso = Estilo.nota(" ");
    private final JButton botaoAtualizar = Estilo.botao("Atualizar painel", Icone.Tipo.ATUALIZAR);
    private final JButton botaoDetalhes = Estilo.botaoPrimario("Ver justificativa", Icone.Tipo.CONSULTA);
    private final JButton botaoDemo = Estilo.botao("Carregar dados de demonstração", Icone.Tipo.DADOS);
    private final Graficos.Rosca rosca = new Graficos.Rosca();
    private final Graficos.BarrasIndice barras = new Graficos.BarrasIndice();
    private static final int LARGURA_LADO_A_LADO = 1020;
    private final JPanel corpo = new JPanel(new GridBagLayout());
    private Cartao lista;
    private Cartao distribuicao;
    private Cartao indices;
    private Boolean layoutEstreito;

    public DashboardPanel(ServicosSMField servicos, LongConsumer abrirDetalhes, Runnable carregarDemonstracao) {
        this.servicos = servicos;
        this.abrirDetalhes = abrirDetalhes;
        this.carregarDemonstracao = carregarDemonstracao;
        Estilo.pagina(this);
        botaoDetalhes.setToolTipText("Abre a consulta de risco do talhão selecionado na tabela");
        add(Estilo.cabecalho("Painel de riscos",
                "Centro de controle dos talhões monitorados: índice, classificação e última atualização.",
                botaoDemo, botaoAtualizar), BorderLayout.NORTH);

        JPanel cartoes = new JPanel(new GridLayout(1, 4, 16, 0));
        cartoes.setOpaque(false);
        cartoes.add(cartao(NivelRisco.ALTO, "Risco alto", "Exigem atenção imediata"));
        cartoes.add(cartao(NivelRisco.MEDIO, "Risco médio", "Acompanhar de perto"));
        cartoes.add(cartao(NivelRisco.BAIXO, "Risco baixo", "Condições favoráveis"));
        cartoes.add(cartao(NivelRisco.NAO_CALCULADO, "Não calculado", "Sem os quatro indicadores"));

        Cartao lista = new Cartao("Talhões por criticidade",
                "Maior risco primeiro · dê dois cliques em uma linha para abrir a justificativa", Icone.Tipo.TALHAO);
        lista.acoes(botaoDetalhes);
        tabela.setAutoCreateRowSorter(false);
        tabela.setRowHeight(48);
        javax.swing.table.TableColumnModel colunas = tabela.getColumnModel();
        colunas.getColumn(6).setCellRenderer(Estilo.renderizadorNivel());
        colunas.getColumn(5).setCellRenderer(new RenderizadorIndice());
        colunas.getColumn(2).setCellRenderer(new RenderizadorDuasLinhas(1, ""));
        colunas.getColumn(3).setCellRenderer(new RenderizadorDuasLinhas(4, " ha"));
        colunas.getColumn(3).setHeaderValue("Cultura / área");
        colunas.getColumn(5).setHeaderValue("Índice");
        colunas.getColumn(0).setMinWidth(44);
        colunas.getColumn(0).setMaxWidth(50);
        colunas.getColumn(2).setPreferredWidth(260);
        colunas.getColumn(3).setMinWidth(110);
        colunas.getColumn(3).setMaxWidth(130);
        colunas.getColumn(5).setMinWidth(72);
        colunas.getColumn(5).setMaxWidth(84);
        colunas.getColumn(6).setMinWidth(162);
        colunas.getColumn(6).setMaxWidth(176);
        colunas.getColumn(7).setMinWidth(136);
        colunas.getColumn(7).setMaxWidth(146);
        // propriedade e área aparecem como segunda linha de talhão e cultura (as colunas continuam no modelo)
        colunas.removeColumn(colunas.getColumn(4));
        colunas.removeColumn(colunas.getColumn(1));
        lista.corpo().add(Estilo.rolagem(tabela), BorderLayout.CENTER);
        JPanel rodape = new JPanel();
        rodape.setOpaque(false);
        rodape.setLayout(new BoxLayout(rodape, BoxLayout.Y_AXIS));
        rodape.add(aviso);
        rodape.add(Box.createVerticalStrut(3));
        rodape.add(Estilo.dica("Índice de 0 a 100: Baixo 0–33 · Médio 34–66 · Alto 67–100 · Não calculado = dados insuficientes. "
                + RiscoService.AVISO_ACADEMICO));
        lista.corpo().add(rodape, BorderLayout.SOUTH);

        Cartao distribuicao = new Cartao("Distribuição do risco", "Talhões por classificação atual", Icone.Tipo.ESCUDO);
        distribuicao.corpo().add(rosca, BorderLayout.CENTER);
        Cartao indices = new Cartao("Índice atual por talhão", "Último índice calculado (0 a 100)", Icone.Tipo.TENDENCIA);
        indices.corpo().add(Estilo.rolagemLimpa(barras), BorderLayout.CENTER);

        this.lista = lista;
        this.distribuicao = distribuicao;
        this.indices = indices;
        distribuicao.setPreferredSize(new Dimension(340, 236));

        lista.setPreferredSize(new Dimension(600, 330));
        corpo.setOpaque(false);
        corpo.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                organizar(corpo.getWidth() < LARGURA_LADO_A_LADO);
            }
        });
        organizar(false);

        JPanel centro = new JPanel(new BorderLayout(0, 16));
        centro.setOpaque(false);
        centro.add(cartoes, BorderLayout.NORTH);
        centro.add(corpo, BorderLayout.CENTER);
        add(Estilo.rolavel(centro), BorderLayout.CENTER);

        botaoAtualizar.addActionListener(e -> atualizar());
        botaoDetalhes.addActionListener(e -> abrirSelecionado());
        botaoDemo.addActionListener(e -> this.carregarDemonstracao.run());
        tabela.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    abrirSelecionado();
                }
            }
        });
        atualizar();
    }

    /**
     * Layout responsivo: em telas largas os gráficos ficam ao lado da tabela; em telas
     * estreitas descem para baixo dela, lado a lado, para a tabela manter colunas legíveis.
     */
    private void organizar(boolean estreito) {
        if (Boolean.valueOf(estreito).equals(layoutEstreito)) {
            return;
        }
        layoutEstreito = estreito;
        corpo.removeAll();
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.BOTH;
        if (estreito) {
            JPanel graficos = new JPanel(new GridLayout(1, 2, 16, 0));
            graficos.setOpaque(false);
            graficos.add(distribuicao);
            graficos.add(indices);
            graficos.setPreferredSize(new Dimension(600, 270));
            g.gridx = 0;
            g.gridy = 0;
            g.weightx = 1;
            g.weighty = 1;
            corpo.add(lista, g);
            g.gridy = 1;
            g.weighty = 0;
            g.insets = new Insets(16, 0, 0, 0);
            corpo.add(graficos, g);
        } else {
            JPanel coluna = new JPanel(new BorderLayout(0, 16));
            coluna.setOpaque(false);
            coluna.add(distribuicao, BorderLayout.NORTH);
            coluna.add(indices, BorderLayout.CENTER);
            coluna.setPreferredSize(new Dimension(330, 330));
            coluna.setMinimumSize(new Dimension(320, 200));
            g.gridy = 0;
            g.weighty = 1;
            g.gridx = 0;
            g.weightx = 1;
            corpo.add(lista, g);
            g.gridx = 1;
            g.weightx = 0;
            g.insets = new Insets(0, 16, 0, 0);
            corpo.add(coluna, g);
        }
        corpo.revalidate();
        corpo.repaint();
    }

    /** Cartão de indicador (KPI) de um nível de risco. */
    private JPanel cartao(NivelRisco nivel, String titulo, String legenda) {
        Color cor = Estilo.corDoNivel(nivel);
        Cartao p = new Cartao() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = Graficos.preparar(g);
                g2.setColor(cor);
                g2.fillRoundRect(0, 14, 4, getHeight() - Cartao.SOMBRA - 28, 4, 4);
                g2.dispose();
            }
        };
        p.corpo().setLayout(new BorderLayout(14, 0));
        JLabel icone = new JLabel(Icone.de(Estilo.iconeDoNivel(nivel), 20, Estilo.textoDoNivel(nivel))) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = Graficos.preparar(g);
                g2.setColor(Estilo.tintaDoNivel(nivel));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        icone.setHorizontalAlignment(SwingConstants.CENTER);
        icone.setPreferredSize(new Dimension(42, 42));
        JPanel iconeTopo = new JPanel(new BorderLayout());
        iconeTopo.setOpaque(false);
        iconeTopo.add(icone, BorderLayout.NORTH);
        p.corpo().add(iconeTopo, BorderLayout.WEST);

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        JLabel t = new JLabel(titulo);
        t.setFont(Estilo.ROTULO);
        t.setForeground(Estilo.TEXTO_SUAVE);
        JLabel n = new JLabel("0");
        n.setFont(Estilo.NUMERO_GRANDE);
        n.setForeground(Estilo.TEXTO);
        JLabel pct = new JLabel(" ");
        pct.setFont(Estilo.fonte("Medium", 12f));
        pct.setForeground(Estilo.textoDoNivel(nivel));
        JLabel rodape = Estilo.dica(legenda);
        textos.add(t);
        textos.add(n);
        textos.add(pct);
        textos.add(Box.createVerticalStrut(2));
        textos.add(rodape);
        p.corpo().add(textos, BorderLayout.CENTER);
        p.getAccessibleContext().setAccessibleName(titulo);
        contadores.put(nivel, n);
        percentuais.put(nivel, pct);
        return p;
    }

    private void abrirSelecionado() {
        int linha = tabela.getSelectedRow();
        if (linha < 0) {
            JOptionPane.showMessageDialog(this, "Selecione um talhão na tabela.");
            return;
        }
        abrirDetalhes.accept((Long) modelo.getValueAt(linha, 0));
    }

    @Override
    public void atualizar() {
        tabela.clearSelection();
        modelo.setRowCount(0);
        List<MonitoramentoService.ItemPainel> itens = servicos.monitoramento().painel();
        List<Graficos.Barra> linhas = new ArrayList<>();
        for (MonitoramentoService.ItemPainel i : itens) {
            modelo.addRow(new Object[]{i.talhaoId(), i.propriedade(), i.talhao(), i.cultura(),
                    Estilo.numero(i.areaHectares()), i.indice() == null ? "—" : i.indice(), i.nivel(),
                    Estilo.formatar(i.atualizadoEm())});
            linhas.add(new Graficos.Barra(i.talhao(), i.propriedade(), i.indice(), i.nivel()));
        }
        Map<NivelRisco, Long> resumo = servicos.monitoramento().resumo();
        long total = itens.size();
        contadores.forEach((nivel, rotulo) -> rotulo.setText(String.valueOf(resumo.getOrDefault(nivel, 0L))));
        percentuais.forEach((nivel, rotulo) -> {
            long q = resumo.getOrDefault(nivel, 0L);
            rotulo.setText(total == 0 ? "talhão(ões)" : q + " de " + total + " talhão(ões) · " + Math.round(100.0 * q / total) + "%");
        });
        rosca.mostrar(List.of(
                new Graficos.Fatia("Alto", resumo.getOrDefault(NivelRisco.ALTO, 0L), Estilo.RISCO_ALTO),
                new Graficos.Fatia("Médio", resumo.getOrDefault(NivelRisco.MEDIO, 0L), Estilo.RISCO_MEDIO),
                new Graficos.Fatia("Baixo", resumo.getOrDefault(NivelRisco.BAIXO, 0L), Estilo.RISCO_BAIXO),
                new Graficos.Fatia("Não calculado", resumo.getOrDefault(NivelRisco.NAO_CALCULADO, 0L),
                        Estilo.RISCO_NAO_CALCULADO)), "talhões");
        barras.mostrar(linhas);
        botaoDemo.setVisible(!servicos.demonstracao().jaCarregado());
        if (itens.isEmpty()) {
            aviso.setText("Nenhum talhão cadastrado. Cadastre propriedades e talhões ou use \"Carregar dados de demonstração\".");
        } else {
            long semDados = resumo.getOrDefault(NivelRisco.NAO_CALCULADO, 0L);
            aviso.setText(itens.size() + " talhão(ões) monitorado(s)"
                    + (semDados > 0 ? " — " + semDados + " sem indicadores suficientes." : ".")
                    + " Dê dois cliques em uma linha para ver a justificativa.");
        }
    }

    /** Valor principal com um valor complementar da mesma linha abaixo, em texto menor (sem quebrar linha). */
    private static final class RenderizadorDuasLinhas extends JPanel implements javax.swing.table.TableCellRenderer {
        private final JLabel nome = new JLabel();
        private final JLabel propriedade = new JLabel();
        private final int colunaComplemento;
        private final String sufixo;

        RenderizadorDuasLinhas(int colunaComplemento, String sufixo) {
            super(new GridLayout(2, 1));
            this.colunaComplemento = colunaComplemento;
            this.sufixo = sufixo;
            setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
            nome.setFont(Estilo.MEDIA);
            nome.setForeground(Estilo.TEXTO);
            nome.setVerticalAlignment(SwingConstants.BOTTOM);
            propriedade.setFont(Estilo.fonte("Regular", 11.5f));
            propriedade.setForeground(Estilo.TEXTO_FRACO);
            propriedade.setVerticalAlignment(SwingConstants.TOP);
            add(nome);
            add(propriedade);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            Object prop = table.getModel().getValueAt(table.convertRowIndexToModel(row), colunaComplemento);
            nome.setText(String.valueOf(value));
            propriedade.setText(prop + sufixo);
            setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            setToolTipText(value + " — " + prop + sufixo);
            return this;
        }
    }

    /** Índice numérico em destaque, com a cor de texto do nível da mesma linha. */
    private static final class RenderizadorIndice extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setFont(Estilo.NEGRITO);
            Object n = table.getModel().getValueAt(table.convertRowIndexToModel(row), 6);
            setForeground(n instanceof NivelRisco nivel && value instanceof Integer
                    ? Estilo.textoDoNivel(nivel) : Estilo.TEXTO_FRACO);
            setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
            return this;
        }
    }
}
