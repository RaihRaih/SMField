package ui;

import domain.NivelRisco;
import service.MonitoramentoService;
import service.RiscoService;
import service.ServicosSMField;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
    private final DefaultTableModel modelo = Estilo.modelo("ID", "Propriedade", "Talhão", "Cultura", "Área (ha)",
            "Último índice", "Classificação", "Última atualização");
    private final JTable tabela = Estilo.tabela(modelo);
    private final JLabel aviso = Estilo.nota(" ");
    private final JButton botaoAtualizar = Estilo.botao("Atualizar painel");
    private final JButton botaoDetalhes = Estilo.botaoPrimario("Ver justificativa do talhão selecionado");
    private final JButton botaoDemo = Estilo.botao("Carregar dados de demonstração");

    public DashboardPanel(ServicosSMField servicos, LongConsumer abrirDetalhes, Runnable carregarDemonstracao) {
        this.servicos = servicos;
        this.abrirDetalhes = abrirDetalhes;
        this.carregarDemonstracao = carregarDemonstracao;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setBackground(Estilo.CINZA_FUNDO);

        JPanel topo = new JPanel(new BorderLayout(8, 8));
        topo.setOpaque(false);
        topo.add(Estilo.titulo("Painel de riscos dos talhões"), BorderLayout.NORTH);
        JPanel cartoes = new JPanel(new GridLayout(1, 4, 10, 0));
        cartoes.setOpaque(false);
        cartoes.add(cartao(NivelRisco.ALTO, "Risco alto"));
        cartoes.add(cartao(NivelRisco.MEDIO, "Risco médio"));
        cartoes.add(cartao(NivelRisco.BAIXO, "Risco baixo"));
        cartoes.add(cartao(NivelRisco.NAO_CALCULADO, "Não calculado"));
        topo.add(cartoes, BorderLayout.CENTER);
        add(topo, BorderLayout.NORTH);

        JPanel lista = new JPanel(new BorderLayout(4, 4));
        lista.setBorder(Estilo.secao("Talhões por criticidade (maior risco primeiro)"));
        tabela.setAutoCreateRowSorter(false);
        tabela.getColumnModel().getColumn(6).setCellRenderer(Estilo.renderizadorNivel());
        tabela.getColumnModel().getColumn(0).setMaxWidth(50);
        lista.add(Estilo.rolagem(tabela), BorderLayout.CENTER);
        JPanel rodape = new JPanel(new BorderLayout());
        rodape.setOpaque(false);
        rodape.add(aviso, BorderLayout.NORTH);
        rodape.add(Estilo.nota("Índice de 0 a 100: Baixo 0–33 · Médio 34–66 · Alto 67–100 · Cinza = dados insuficientes. "
                + RiscoService.AVISO_ACADEMICO), BorderLayout.SOUTH);
        lista.add(rodape, BorderLayout.SOUTH);
        add(lista, BorderLayout.CENTER);

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botoes.setOpaque(false);
        botoes.add(botaoDemo);
        botoes.add(botaoAtualizar);
        botoes.add(botaoDetalhes);
        add(botoes, BorderLayout.SOUTH);

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

    private JPanel cartao(NivelRisco nivel, String titulo) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Estilo.corDoNivel(nivel));
        p.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        JLabel t = new JLabel(titulo);
        t.setForeground(Color.WHITE);
        t.setFont(Estilo.SUBTITULO);
        JLabel n = new JLabel("0", SwingConstants.RIGHT);
        n.setForeground(Color.WHITE);
        n.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 30));
        p.add(t, BorderLayout.NORTH);
        p.add(n, BorderLayout.CENTER);
        JLabel rodape = new JLabel("talhão(ões)");
        rodape.setForeground(Color.WHITE);
        p.add(rodape, BorderLayout.SOUTH);
        contadores.put(nivel, n);
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
        for (MonitoramentoService.ItemPainel i : itens) {
            modelo.addRow(new Object[]{i.talhaoId(), i.propriedade(), i.talhao(), i.cultura(),
                    Estilo.numero(i.areaHectares()), i.indice() == null ? "—" : i.indice(), i.nivel(),
                    Estilo.formatar(i.atualizadoEm())});
        }
        Map<NivelRisco, Long> resumo = servicos.monitoramento().resumo();
        contadores.forEach((nivel, rotulo) -> rotulo.setText(String.valueOf(resumo.getOrDefault(nivel, 0L))));
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
}
