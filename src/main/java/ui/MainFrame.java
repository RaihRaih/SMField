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
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/** Janela principal: cabeçalho, menu de seções e abas que compartilham os mesmos serviços. */
public class MainFrame extends JFrame {

    public static final String TITULO = "SMField — Monitoramento Inteligente Agrícola";

    private final ServicosSMField servicos;
    private final JTabbedPane abas = new JTabbedPane();
    private final List<Atualizavel> telas = new ArrayList<>();
    private final JLabel status = Estilo.nota(" ");

    private final DashboardPanel dashboard;
    private final DetalhesRiscoPanel detalhes;

    public MainFrame(ServicosSMField servicos) {
        super(TITULO);
        this.servicos = servicos;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 720));
        setSize(1280, 800);
        setLocationRelativeTo(null);

        Runnable aoAlterar = this::notificarAlteracao;
        dashboard = new DashboardPanel(servicos, this::abrirDetalhes, this::carregarDemonstracao);
        detalhes = new DetalhesRiscoPanel(servicos);

        adicionar("1. Dashboard", dashboard);
        adicionar("2. Propriedades", new PropriedadePanel(servicos, aoAlterar));
        adicionar("3. Talhões", new TalhaoPanel(servicos, aoAlterar));
        adicionar("4. Indicadores ambientais", new IndicadoresPanel(servicos, aoAlterar));
        adicionar("Consulta de risco", detalhes);
        adicionar("5. Ocorrências", new OcorrenciasPanel(servicos, aoAlterar));
        adicionar("6. Histórico e alertas", new HistoricoAlertasPanel(servicos));
        adicionar("Compatibilidade (demo)", new CompatibilidadePanel(servicos));
        abas.setFont(Estilo.NORMAL.deriveFont(Font.BOLD));
        abas.addChangeListener(e -> {
            Component c = abas.getSelectedComponent();
            if (c instanceof Atualizavel a) {
                a.atualizar();
            }
        });

        setJMenuBar(criarMenu());
        setLayout(new BorderLayout());
        add(criarCabecalho(), BorderLayout.NORTH);
        add(abas, BorderLayout.CENTER);
        JPanel rodape = new JPanel(new BorderLayout());
        rodape.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        rodape.add(status, BorderLayout.WEST);
        add(rodape, BorderLayout.SOUTH);
        atualizarStatus();
    }

    private void adicionar(String titulo, Component tela) {
        abas.addTab(titulo, tela);
        if (tela instanceof Atualizavel a) {
            telas.add(a);
        }
    }

    private JPanel criarCabecalho() {
        JPanel cab = new JPanel(new BorderLayout());
        cab.setBackground(Estilo.VERDE_ESCURO);
        cab.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        JLabel titulo = new JLabel(TITULO);
        titulo.setForeground(Color.WHITE);
        titulo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        JLabel sub = new JLabel("Sistema de Monitoramento Inteligente e Análise de Riscos Agrícolas — projeto acadêmico");
        sub.setForeground(new Color(0xC8E6C9));
        sub.setFont(Estilo.NORMAL);
        cab.add(titulo, BorderLayout.NORTH);
        cab.add(sub, BorderLayout.SOUTH);
        return cab;
    }

    private JMenuBar criarMenu() {
        JMenuBar barra = new JMenuBar();

        JMenu arquivo = new JMenu("Arquivo");
        JMenuItem demo = new JMenuItem("Carregar dados de demonstração (SIMULADOS)");
        demo.addActionListener(e -> carregarDemonstracao());
        JMenuItem atualizar = new JMenuItem("Atualizar todas as telas");
        atualizar.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        atualizar.addActionListener(e -> notificarAlteracao());
        JMenuItem sair = new JMenuItem("Sair");
        sair.addActionListener(e -> dispose());
        arquivo.add(demo);
        arquivo.add(atualizar);
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
                "SMField — Monitoramento Inteligente e Análise de Riscos Agrícolas\n"
                        + "Projeto de Gestão e Qualidade de Software — Squad Ânima HUB\n"
                        + "Alunos: Arthur, Brener, João, Raissa e Tayná | Professora: Rafaela\n\n"
                        + "Dados gravados em: " + servicos.diretorioDados().toAbsolutePath(),
                "Sobre", JOptionPane.INFORMATION_MESSAGE));
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
                "Carregar 3 propriedades, 6 talhões, indicadores e ocorrências FICTÍCIOS?\n"
                        + "Os registros são marcados como [SIMULADO] — uso acadêmico — e não representam condições reais.",
                "Dados de demonstração", JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION && Estilo.executar(this, () -> servicos.demonstracao().carregar())) {
            notificarAlteracao();
            abas.setSelectedComponent(dashboard);
            Estilo.sucesso(this, "Dados de demonstração carregados.");
        }
    }

    private void atualizarStatus() {
        status.setText("Dados locais: " + servicos.diretorioDados().toAbsolutePath()
                + "   |   Propriedades: " + servicos.propriedades().listar().size()
                + "   |   Talhões: " + servicos.talhoes().listar().size()
                + "   |   Registros de indicadores: " + servicos.indicadores().listarTodos().size());
    }

    JTabbedPane getAbas() {
        return abas;
    }
}
