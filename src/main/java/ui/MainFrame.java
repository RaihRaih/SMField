package ui;

import service.RiscoService;
import service.ServicosSMField;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JComponent;
import javax.swing.JLayer;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Janela principal: menu lateral, área de conteúdo com as seções e barra de status.
 * <p>
 * As seções continuam num {@link JTabbedPane} (com a faixa de abas oculta), o que preserva
 * toda a navegação existente — menu "Seções", atalhos Ctrl+1…9 e a abertura da consulta a
 * partir do painel — enquanto o menu lateral oferece a navegação visual.
 */
public class MainFrame extends JFrame {

    public static final String TITULO = "SMField — Monitoramento Inteligente Agrícola";

    private final ServicosSMField servicos;
    private final JTabbedPane abas = new JTabbedPane();
    private final List<Atualizavel> telas = new ArrayList<>();
    private final JLabel status = Estilo.nota(" ");
    private final BarraLateral menuLateral = new BarraLateral(abas::setSelectedIndex);
    private final Transicao transicao = new Transicao();

    private final DashboardPanel dashboard;
    private final DetalhesRiscoPanel detalhes;

    public MainFrame(ServicosSMField servicos) {
        super(TITULO);
        this.servicos = servicos;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setIconImages(Estilo.iconesJanela());
        setMinimumSize(new Dimension(1120, 720));
        Dimension tela = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(Math.min(1440, tela.width - 40), Math.min(900, tela.height - 60));
        setLocationRelativeTo(null);

        Runnable aoAlterar = this::notificarAlteracao;
        dashboard = new DashboardPanel(servicos, this::abrirDetalhes, this::carregarDemonstracao);
        detalhes = new DetalhesRiscoPanel(servicos);

        menuLateral.grupo("Visão geral");
        adicionar("Dashboard", Icone.Tipo.DASHBOARD, dashboard);
        menuLateral.grupo("Cadastros");
        adicionar("Propriedades", Icone.Tipo.PROPRIEDADE, new PropriedadePanel(servicos, aoAlterar));
        adicionar("Talhões", Icone.Tipo.TALHAO, new TalhaoPanel(servicos, aoAlterar));
        adicionar("Indicadores ambientais", Icone.Tipo.INDICADORES, new IndicadoresPanel(servicos, aoAlterar));
        menuLateral.grupo("Monitoramento");
        adicionar("Consulta de risco", Icone.Tipo.CONSULTA, detalhes);
        adicionar("Ocorrências", Icone.Tipo.OCORRENCIAS, new OcorrenciasPanel(servicos, aoAlterar));
        adicionar("Histórico e alertas", Icone.Tipo.HISTORICO, new HistoricoAlertasPanel(servicos));
        menuLateral.grupo("Dados e análises");
        adicionar("Dados reais (INMET)", Icone.Tipo.INMET, new InmetPanel(servicos, aoAlterar));
        adicionar("Compatibilidade de culturas", Icone.Tipo.COMPATIBILIDADE, new CompatibilidadePanel(servicos));
        abas.setUI(new AbasSemFaixa());
        abas.setBackground(Estilo.FUNDO);
        abas.addChangeListener(e -> {
            Component c = abas.getSelectedComponent();
            menuLateral.marcar(abas.getSelectedIndex());
            transicao.iniciar();
            if (c instanceof Atualizavel a) {
                a.atualizar();
            }
        });
        menuLateral.marcar(0);

        setJMenuBar(criarMenu());
        JPanel conteudo = new JPanel(new BorderLayout());
        conteudo.setBackground(Estilo.FUNDO);
        conteudo.add(new JLayer<>(abas, transicao), BorderLayout.CENTER);
        JPanel rodape = new JPanel(new BorderLayout());
        rodape.setBackground(Color.WHITE);
        rodape.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Estilo.BORDA),
                BorderFactory.createEmptyBorder(6, 16, 6, 16)));
        status.setIcon(Icone.de(Icone.Tipo.DADOS, 14, Estilo.TEXTO_FRACO));
        status.setIconTextGap(8);
        status.setFont(Estilo.fonte("Regular", 11.5f));
        rodape.add(status, BorderLayout.WEST);
        conteudo.add(rodape, BorderLayout.SOUTH);

        setLayout(new BorderLayout());
        add(menuLateral, BorderLayout.WEST);
        add(conteudo, BorderLayout.CENTER);
        atualizarStatus();
    }

    private void adicionar(String titulo, Icone.Tipo icone, Component tela) {
        int indice = abas.getTabCount();
        abas.addTab(titulo, tela);
        menuLateral.item(indice, titulo, icone, indice < 9 ? "Ctrl+" + (indice + 1) : null);
        if (tela instanceof Atualizavel a) {
            telas.add(a);
        }
    }

    private JMenuBar criarMenu() {
        JMenuBar barra = new JMenuBar();

        JMenu arquivo = new JMenu("Arquivo");
        JMenuItem menuLateralItem = new JMenuItem("Recolher/expandir menu lateral");
        menuLateralItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.CTRL_DOWN_MASK));
        menuLateralItem.addActionListener(e -> menuLateral.alternar());
        JMenuItem demo = new JMenuItem("Carregar dados de demonstração (clima real do INMET)");
        demo.addActionListener(e -> carregarDemonstracao());
        JMenuItem atualizar = new JMenuItem("Atualizar todas as telas");
        atualizar.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        atualizar.addActionListener(e -> notificarAlteracao());
        JMenuItem sair = new JMenuItem("Sair");
        sair.addActionListener(e -> dispose());
        arquivo.add(demo);
        arquivo.add(atualizar);
        arquivo.add(menuLateralItem);
        arquivo.addSeparator();
        arquivo.add(sair);

        JMenu secoes = new JMenu("Seções");
        for (int i = 0; i < abas.getTabCount(); i++) {
            int indice = i;
            JMenuItem item = new JMenuItem(abas.getTitleAt(i));
            if (i < 9) {
                item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1 + i, InputEvent.CTRL_DOWN_MASK));
            }
            item.addActionListener(e -> abas.setSelectedIndex(indice));
            secoes.add(item);
        }

        JMenu ajuda = new JMenu("Ajuda");
        JMenuItem modelo = new JMenuItem("Como o risco é calculado");
        modelo.addActionListener(e -> JOptionPane.showMessageDialog(this, textoModelo(), "Modelo de risco",
                JOptionPane.INFORMATION_MESSAGE));
        JMenuItem sobre = new JMenuItem("Sobre o SMField");
        sobre.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "<html><b>SMField — Monitoramento Inteligente e Análise de Riscos Agrícolas</b><br>"
                        + "Projeto de Gestão e Qualidade de Software — Squad Ânima HUB<br>"
                        + "Alunos: Arthur, Brener, João, Raissa e Tayná | Professora: Rafaela<br><br>"
                        + "Dados gravados em: " + servicos.diretorioDados().toAbsolutePath() + "<br><br>"
                        + "<span style='color:#5E6B62'>Fonte Inter (SIL Open Font License 1.1) · "
                        + "aparência FlatLaf (Apache 2.0)</span></html>",
                "Sobre", JOptionPane.PLAIN_MESSAGE, logoSobre()));
        ajuda.add(modelo);
        ajuda.add(sobre);

        barra.add(arquivo);
        barra.add(secoes);
        barra.add(ajuda);
        return barra;
    }

    static String textoModelo() {
        return "<html><body style='width:460px'>"
                + "<b>Índice de risco (0 a 100)</b><br>"
                + "Cada indicador recebe penalidade 0 (faixa ideal), 50 (intermediária) ou 100 (crítica).<br><br>"
                + "risco = solo × 0,35 + temperatura × 0,30 + umidade do ar × 0,15 + precipitação × 0,20<br>"
                + "(arredondado para o inteiro mais próximo)<br><br>"
                + "<b>Faixas ideais:</b> umidade do solo 40–70 %; temperatura 18–30 °C; umidade do ar 40–80 %; "
                + "precipitação acumulada em 24 h 5–30 mm.<br><br>"
                + "<b>Classificação:</b> Baixo 0–33 · Médio 34–66 · Alto 67–100. Sem os quatro indicadores: NÃO CALCULADO.<br><br>"
                + "<i>" + RiscoService.AVISO_ACADEMICO + "</i></body></html>";
    }

    /** Recarrega todas as telas a partir dos dados persistidos. */
    public void notificarAlteracao() {
        for (Atualizavel t : telas) {
            t.atualizar();
        }
        atualizarStatus();
    }

    public void abrirDetalhes(long talhaoId) {
        abas.setSelectedComponent(detalhes);
        detalhes.selecionarTalhao(talhaoId);
    }

    private void carregarDemonstracao() {
        if (servicos.demonstracao().jaCarregado()) {
            JOptionPane.showMessageDialog(this, "Os dados de demonstração já foram carregados anteriormente.");
            return;
        }
        int r = JOptionPane.showConfirmDialog(this,
                "Carregar a demonstração em três polos do agronegócio, com clima REAL do INMET?\n\n"
                        + "• Sorriso/MT — soja e milho safrinha (estação A904)\n"
                        + "• Rio Verde/GO — soja e milho safrinha (estação A025)\n"
                        + "• Patrocínio/MG — café (estação A523)\n\n"
                        + "Municípios e clima (temperatura, umidade do ar, chuva) são reais.\n"
                        + "Propriedades, talhões e ocorrências são fictícios [DEMO]; a umidade do solo é simulada.",
                "Dados de demonstração", JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION && Estilo.executar(this, () -> servicos.demonstracao().carregar())) {
            notificarAlteracao();
            abas.setSelectedComponent(dashboard);
            Estilo.sucesso(this, "Dados de demonstração carregados.");
        }
    }

    private void atualizarStatus() {
        status.setText("Dados locais: " + servicos.diretorioDados().toAbsolutePath()
                + "   ·   Propriedades: " + servicos.propriedades().listar().size()
                + "   ·   Talhões: " + servicos.talhoes().listar().size()
                + "   ·   Registros de indicadores: " + servicos.indicadores().listarTodos().size());
        menuLateral.setRodape("Projeto acadêmico · Sprint 2<br>" + servicos.talhoes().listar().size()
                + " talhão(ões) monitorado(s)");
    }

    /** Logo oficial completa (com slogan) para a janela "Sobre". */
    private static javax.swing.Icon logoSobre() {
        java.awt.Image img = Estilo.imagem("logo-smfield.png");
        if (img == null) {
            return null;
        }
        int w = 300;
        int h = img.getHeight(null) * w / img.getWidth(null);
        java.awt.image.BufferedImage b = new java.awt.image.BufferedImage(w, h + 16, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = b.createGraphics();
        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        // reduz em etapas (metade por vez) para manter a nitidez da logo original
        java.awt.Image atual = img;
        int aw = img.getWidth(null);
        int ah = img.getHeight(null);
        while (aw / 2 >= w) {
            aw /= 2;
            ah /= 2;
            java.awt.image.BufferedImage passo = new java.awt.image.BufferedImage(aw, ah, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            java.awt.Graphics2D gp = passo.createGraphics();
            gp.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            gp.drawImage(atual, 0, 0, aw, ah, null);
            gp.dispose();
            atual = passo;
        }
        g.drawImage(atual, 0, 4, w, h, null);
        g.dispose();
        return new javax.swing.ImageIcon(b);
    }

    /** Oculta a faixa de abas: a navegação visual é feita pelo menu lateral. */
    private static final class AbasSemFaixa extends javax.swing.plaf.basic.BasicTabbedPaneUI {
        @Override
        protected int calculateTabAreaHeight(int placement, int runCount, int maxTabHeight) {
            return 0;
        }

        @Override
        protected java.awt.Insets getContentBorderInsets(int tabPlacement) {
            return new java.awt.Insets(0, 0, 0, 0);
        }

        @Override
        protected java.awt.Insets getTabAreaInsets(int tabPlacement) {
            return new java.awt.Insets(0, 0, 0, 0);
        }

        @Override
        protected void paintTabArea(java.awt.Graphics g, int tabPlacement, int selectedIndex) {
            // sem faixa de abas
        }

        @Override
        protected void paintContentBorder(java.awt.Graphics g, int tabPlacement, int selectedIndex) {
            // sem borda
        }
    }

    /** Transição suave (esmaecimento rápido) ao trocar de seção. */
    private static final class Transicao extends javax.swing.plaf.LayerUI<JTabbedPane> {
        private float opacidade = 1f;
        private javax.swing.Timer timer;
        private JLayer<?> camada;

        @Override
        public void installUI(JComponent c) {
            super.installUI(c);
            camada = (JLayer<?>) c;
        }

        void iniciar() {
            if (camada == null) {
                return;
            }
            if (timer != null) {
                timer.stop();
            }
            long t0 = System.currentTimeMillis();
            opacidade = 0.35f;
            timer = new javax.swing.Timer(15, e -> {
                float p = Math.min(1f, (System.currentTimeMillis() - t0) / 160f);
                opacidade = 0.35f + 0.65f * (1 - (1 - p) * (1 - p));
                camada.repaint();
                if (p >= 1f) {
                    ((javax.swing.Timer) e.getSource()).stop();
                }
            });
            timer.start();
        }

        @Override
        public void paint(java.awt.Graphics g, JComponent c) {
            if (opacidade >= 0.999f) {
                super.paint(g, c);
                return;
            }
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setColor(Estilo.FUNDO);
            g2.fillRect(0, 0, c.getWidth(), c.getHeight());
            g2.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, opacidade));
            super.paint(g2, c);
            g2.dispose();
        }
    }

    JTabbedPane getAbas() {
        return abas;
    }
}
