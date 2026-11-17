package ui;

import domain.OcorrenciaCampo;
import domain.TipoOcorrencia;
import service.ConversorEntrada;
import service.ServicosSMField;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.time.LocalDate;
import java.util.List;

/** RF-10: registro e consulta de ocorrências de campo por talhão. */
public class OcorrenciasPanel extends JPanel implements Atualizavel {

    private final ServicosSMField servicos;
    private final Runnable aoAlterar;

    private final JComboBox<Estilo.OpcaoTalhao> comboTalhao = new JComboBox<>();
    private final JComboBox<TipoOcorrencia> comboTipo = new JComboBox<>(TipoOcorrencia.values());
    private final JTextField campoData = new JTextField(10);
    private final JTextArea campoDescricao = new JTextArea(4, 24);
    private final JButton botaoSalvar = Estilo.botaoPrimario("Registrar ocorrência");
    private final JButton botaoExcluir = Estilo.botao("Excluir ocorrência selecionada");

    private final DefaultTableModel modelo = Estilo.modelo("ID", "Data", "Tipo", "Descrição");
    private final JTable tabela = Estilo.tabela(modelo);
    private final JLabel aviso = Estilo.nota(" ");

    public OcorrenciasPanel(ServicosSMField servicos, Runnable aoAlterar) {
        this.servicos = servicos;
        this.aoAlterar = aoAlterar;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setBackground(Estilo.CINZA_FUNDO);
        add(Estilo.titulo("Ocorrências de campo"), BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(Estilo.secao("Nova ocorrência"));
        GridBagConstraints c = Estilo.gbc(0, 0);
        form.add(new JLabel("Talhão *"), c);
        c.gridy++;
        form.add(comboTalhao, c);
        c.gridy++;
        form.add(new JLabel("Tipo de ocorrência *"), c);
        c.gridy++;
        form.add(comboTipo, c);
        c.gridy++;
        form.add(new JLabel("Data * (dd/mm/aaaa)"), c);
        c.gridy++;
        form.add(campoData, c);
        c.gridy++;
        form.add(new JLabel("Descrição *"), c);
        c.gridy++;
        campoDescricao.setLineWrap(true);
        campoDescricao.setWrapStyleWord(true);
        form.add(Estilo.rolagem(campoDescricao), c);
        c.gridy++;
        form.add(botaoSalvar, c);
        c.gridy++;
        form.add(botaoExcluir, c);
        c.gridy++;
        c.weighty = 1;
        c.anchor = GridBagConstraints.NORTHWEST;
        form.add(Estilo.nota("<html>Nesta versão as ocorrências são informativas<br>e não alteram o índice numérico de risco.</html>"), c);
        add(form, BorderLayout.WEST);

        JPanel lista = new JPanel(new BorderLayout(4, 4));
        lista.setBorder(Estilo.secao("Ocorrências do talhão selecionado"));
        tabela.getColumnModel().getColumn(0).setMaxWidth(50);
        lista.add(Estilo.rolagem(tabela), BorderLayout.CENTER);
        lista.add(aviso, BorderLayout.SOUTH);
        add(lista, BorderLayout.CENTER);

        campoData.setText(Estilo.formatar(LocalDate.now()));
        comboTalhao.addActionListener(e -> carregar());
        botaoSalvar.addActionListener(e -> salvar());
        botaoExcluir.addActionListener(e -> excluir());
        atualizar();
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
