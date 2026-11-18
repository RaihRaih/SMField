package ui;

import domain.OcorrenciaCampo;
import domain.TipoOcorrencia;
import service.ConversorEntrada;
import service.ServicosSMField;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.util.List;

/** RF-10: registro e consulta de ocorrências de campo por talhão. */
public class OcorrenciasPanel extends JPanel implements Atualizavel {

    private final ServicosSMField servicos;
    private final Runnable aoAlterar;

    private final JComboBox<Estilo.OpcaoTalhao> comboTalhao = new JComboBox<>();
    private final JComboBox<TipoOcorrencia> comboTipo = new JComboBox<>(TipoOcorrencia.values());
    private final JTextField campoData = new JTextField(10);
    private final JTextArea campoDescricao = new JTextArea(6, 24);
    private final JButton botaoSalvar = Estilo.botaoPrimario("Registrar ocorrência", Icone.Tipo.MAIS);
    private final JButton botaoExcluir = Estilo.botaoPerigo("Excluir selecionada");

    private final DefaultTableModel modelo = Estilo.modelo("ID", "Data", "Tipo", "Descrição");
    private final JTable tabela = Estilo.tabela(modelo);
    private final JLabel aviso = Estilo.nota(" ");

    public OcorrenciasPanel(ServicosSMField servicos, Runnable aoAlterar) {
        this.servicos = servicos;
        this.aoAlterar = aoAlterar;
        Estilo.pagina(this);
        add(Estilo.cabecalho("Ocorrências de campo",
                "Pragas, doenças e problemas de manejo observados em cada talhão."), BorderLayout.NORTH);

        Cartao form = new Cartao("Nova ocorrência", "Campos com * são obrigatórios", Icone.Tipo.MAIS);
        JPanel campos = new JPanel();
        campos.setOpaque(false);
        campos.setLayout(new BoxLayout(campos, BoxLayout.Y_AXIS));
        campo(campos, "Talhão", comboTalhao);
        comboTipo.setRenderer(new javax.swing.DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(javax.swing.JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof TipoOcorrencia t) {
                    Estilo.Pilula p = pilulaTipo(t);
                    setIcon(Icone.de(p.icone(), 15, p.cor()));
                    setIconTextGap(8);
                }
                return this;
            }
        });
        JPanel tipoData = new JPanel(new GridLayout(1, 2, 12, 0));
        tipoData.setOpaque(false);
        tipoData.add(bloco("Tipo de ocorrência", comboTipo));
        campoData.putClientProperty("JTextField.leadingIcon", Icone.de(Icone.Tipo.CALENDARIO, 15, Estilo.TEXTO_FRACO));
        Estilo.exemplo(campoData, "dd/mm/aaaa");
        tipoData.add(bloco("Data (dd/mm/aaaa)", campoData));
        tipoData.setAlignmentX(Component.LEFT_ALIGNMENT);
        tipoData.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        campos.add(tipoData);
        campos.add(Box.createVerticalStrut(12));
        campoDescricao.setLineWrap(true);
        campoDescricao.setWrapStyleWord(true);
        campoDescricao.setFont(Estilo.NORMAL);
        campoDescricao.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        JScrollPane rolagemDescricao = Estilo.rolagem(campoDescricao);
        rolagemDescricao.setPreferredSize(new Dimension(300, 130));
        campo(campos, "Descrição", rolagemDescricao);
        JLabel dica = Estilo.dica("Descreva o que foi observado, onde e com que intensidade.");
        dica.setAlignmentX(Component.LEFT_ALIGNMENT);
        campos.add(dica);
        campos.add(Box.createVerticalStrut(16));
        botaoSalvar.setAlignmentX(Component.LEFT_ALIGNMENT);
        botaoSalvar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        campos.add(botaoSalvar);
        campos.add(Box.createVerticalStrut(16));
        JPanel nota = Estilo.faixaAviso("Nesta versão as ocorrências são informativas e não alteram o índice numérico de risco.", false);
        nota.setAlignmentX(Component.LEFT_ALIGNMENT);
        campos.add(nota);
        form.corpo().add(campos, BorderLayout.NORTH);
        form.setPreferredSize(new Dimension(380, 300));
        add(form, BorderLayout.WEST);

        Cartao lista = new Cartao("Ocorrências do talhão selecionado", "Selecione uma linha para excluir uma ocorrência",
                Icone.Tipo.OCORRENCIAS);
        lista.acoes(botaoExcluir);
        tabela.getColumnModel().getColumn(0).setMaxWidth(56);
        tabela.getColumnModel().getColumn(1).setMinWidth(100);
        tabela.getColumnModel().getColumn(1).setMaxWidth(120);
        tabela.getColumnModel().getColumn(2).setMinWidth(170);
        tabela.getColumnModel().getColumn(2).setMaxWidth(220);
        tabela.getColumnModel().getColumn(2).setCellRenderer(renderizadorTipo());
        lista.corpo().add(Estilo.rolagem(tabela), BorderLayout.CENTER);
        lista.corpo().add(aviso, BorderLayout.SOUTH);
        JPanel centro = new JPanel(new BorderLayout());
        centro.setOpaque(false);
        centro.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 0));
        centro.add(lista, BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);

        campoData.setText(Estilo.formatar(LocalDate.now()));
        comboTalhao.addActionListener(e -> carregar());
        botaoSalvar.addActionListener(e -> salvar());
        botaoExcluir.addActionListener(e -> excluir());
        atualizar();
    }

    private static JPanel bloco(String rotulo, JComponent campo) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setOpaque(false);
        p.add(Estilo.rotuloCampo(rotulo, true), BorderLayout.NORTH);
        p.add(campo, BorderLayout.CENTER);
        return p;
    }

    private static void campo(JPanel destino, String rotulo, JComponent campo) {
        JPanel b = bloco(rotulo, campo);
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, b.getPreferredSize().height));
        destino.add(b);
        destino.add(Box.createVerticalStrut(12));
    }

    /** Selo de cada tipo de ocorrência (cor e ícone próprios, sempre com o texto). */
    static Estilo.Pilula pilulaTipo(TipoOcorrencia t) {
        return switch (t) {
            case PRAGA -> new Estilo.Pilula(t.toString(), new Color(0xFDECEC), new Color(0xA92424), Icone.Tipo.ALERTA);
            case DOENCA -> new Estilo.Pilula(t.toString(), new Color(0xF4ECFA), new Color(0x6B3FA0), Icone.Tipo.INFO);
            case FALTA_IRRIGACAO -> new Estilo.Pilula(t.toString(), new Color(0xFEF3E0), new Color(0x8A5200), Icone.Tipo.SOLO);
            case EXCESSO_AGUA -> new Estilo.Pilula(t.toString(), new Color(0xE8F1FA), new Color(0x245C8A), Icone.Tipo.CHUVA);
            case OUTRA -> new Estilo.Pilula(t.toString(), new Color(0xEEF1EE), new Color(0x4F5A52), Icone.Tipo.OCORRENCIAS);
        };
    }

    static DefaultTableCellRenderer renderizadorTipo() {
        return Estilo.renderizadorPilula(v -> v instanceof TipoOcorrencia t ? pilulaTipo(t) : null);
    }

    private void salvar() {
        long talhaoId = Estilo.idTalhao(comboTalhao);
        boolean ok = Estilo.executar(this, () -> servicos.ocorrencias().registrar(talhaoId,
                (TipoOcorrencia) comboTipo.getSelectedItem(), campoDescricao.getText(),
                ConversorEntrada.data(campoData.getText())));
        if (ok) {
            Estilo.sucesso(this, "Ocorrência registrada com sucesso.");
            campoDescricao.setText("");
            aoAlterar.run();
        }
    }

    private void excluir() {
        int linha = tabela.getSelectedRow();
        if (linha < 0) {
            JOptionPane.showMessageDialog(this, "Selecione uma ocorrência na tabela.");
            return;
        }
        long id = (Long) modelo.getValueAt(tabela.convertRowIndexToModel(linha), 0);
        int r = JOptionPane.showConfirmDialog(this, "Excluir a ocorrência selecionada?", "Confirmar exclusão",
                JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION && Estilo.executar(this, () -> servicos.ocorrencias().excluir(id))) {
            aoAlterar.run();
        }
    }

    private void carregar() {
        tabela.clearSelection();
        modelo.setRowCount(0);
        long id = Estilo.idTalhao(comboTalhao);
        if (id <= 0) {
            aviso.setText(comboTalhao.getItemCount() == 0 ? "Nenhum talhão cadastrado." : "Selecione um talhão.");
            return;
        }
        List<OcorrenciaCampo> lista = servicos.ocorrencias().listarPorTalhao(id);
        for (OcorrenciaCampo o : lista) {
            modelo.addRow(new Object[]{o.getId(), Estilo.formatar(o.getData()), o.getTipo(), o.getDescricao()});
        }
        aviso.setText(lista.isEmpty() ? "Nenhuma ocorrência registrada para este talhão." : lista.size() + " ocorrência(s).");
    }

    @Override
    public void atualizar() {
        Estilo.carregarTalhoes(comboTalhao, servicos);
        carregar();
    }
}
