package ui;

import domain.Propriedade;
import domain.Talhao;
import service.ConversorEntrada;
import service.ServicosSMField;
import service.TalhaoService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;

/** RF-02, RF-03 e RF-09: cadastro, edição, exclusão e listagem de talhões. */
public class TalhaoPanel extends JPanel implements Atualizavel {

    private static final String[] CULTURAS = {"Milho", "Soja", "Café", "Feijão", "Trigo", "Cana-de-açúcar", "Algodão"};

    private record OpcaoPropriedade(long id, String rotulo) {
        @Override
        public String toString() {
            return rotulo;
        }
    }

    private final ServicosSMField servicos;
    private final Runnable aoAlterar;

    private final JComboBox<OpcaoPropriedade> comboPropriedade = new JComboBox<>();
    private final JTextField campoNome = new JTextField(22);
    private final JTextField campoArea = new JTextField(8);
    private final JComboBox<String> comboCultura = new JComboBox<>(CULTURAS);
    private final JLabel rotuloModo = Estilo.nota("Novo cadastro");
    private final JButton botaoSalvar = Estilo.botaoPrimario("Salvar talhão");
    private final JButton botaoNovo = Estilo.botao("Limpar / novo");
    private final JButton botaoExcluir = Estilo.botao("Excluir talhão");

    private final JComboBox<OpcaoPropriedade> comboFiltro = new JComboBox<>();
    private final DefaultTableModel modelo = Estilo.modelo("ID", "Propriedade", "Talhão", "Área (ha)", "Cultura", "Registros");
    private final JTable tabela = Estilo.tabela(modelo);
    private final JLabel aviso = Estilo.nota(" ");

    private long idEmEdicao = 0;
    private boolean carregando = false;

    public TalhaoPanel(ServicosSMField servicos, Runnable aoAlterar) {
        this.servicos = servicos;
        this.aoAlterar = aoAlterar;
        comboCultura.setEditable(true);
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setBackground(Estilo.CINZA_FUNDO);
        add(Estilo.titulo("Talhões"), BorderLayout.NORTH);
        add(criarFormulario(), BorderLayout.WEST);

        JPanel lista = new JPanel(new BorderLayout(4, 4));
        lista.setBorder(Estilo.secao("Talhões cadastrados"));
        JPanel filtro = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filtro.add(new JLabel("Filtrar por propriedade:"));
        filtro.add(comboFiltro);
        lista.add(filtro, BorderLayout.NORTH);
        lista.add(Estilo.rolagem(tabela), BorderLayout.CENTER);
        lista.add(aviso, BorderLayout.SOUTH);
        add(lista, BorderLayout.CENTER);

        botaoSalvar.addActionListener(e -> salvar());
        botaoNovo.addActionListener(e -> limpar());
        botaoExcluir.addActionListener(e -> excluir());
        comboFiltro.addActionListener(e -> {
            if (!carregando) {
                carregarTabela();
            }
        });
        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selecionar();
            }
        });
        atualizar();
    }

    private JPanel criarFormulario() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(Estilo.secao("Cadastro / edição"));
        GridBagConstraints c = Estilo.gbc(0, 0);
        form.add(new JLabel("Propriedade *"), c);
        c.gridy++;
        form.add(comboPropriedade, c);
        c.gridy++;
        form.add(new JLabel("Nome do talhão *"), c);
        c.gridy++;
        form.add(campoNome, c);
        c.gridy++;
        form.add(new JLabel("Área (hectares) * — maior que zero"), c);
        c.gridy++;
        form.add(campoArea, c);
        c.gridy++;
        form.add(new JLabel("Cultura agrícola *"), c);
        c.gridy++;
        form.add(comboCultura, c);
        c.gridy++;
        form.add(rotuloModo, c);
        c.gridy++;
        form.add(botaoSalvar, c);
        c.gridy++;
        form.add(botaoNovo, c);
        c.gridy++;
        form.add(botaoExcluir, c);
        c.gridy++;
        c.weighty = 1;
        c.anchor = GridBagConstraints.NORTHWEST;
        form.add(Estilo.nota("<html>Ao excluir, os indicadores e ocorrências<br>do talhão também são removidos<br>(após confirmação).</html>"), c);
        return form;
    }

    private void salvar() {
        boolean edicao = idEmEdicao > 0;
        Object prop = comboPropriedade.getSelectedItem();
        String cultura = comboCultura.getEditor().getItem() == null ? "" : comboCultura.getEditor().getItem().toString();
        boolean ok = Estilo.executar(this, () -> {
            Double area = ConversorEntrada.numeroOpcional(campoArea.getText(), "Área (ha)");
            if (edicao) {
                servicos.talhoes().editar(idEmEdicao, campoNome.getText(), area, cultura);
            } else {
                long propriedadeId = prop instanceof OpcaoPropriedade op ? op.id() : -1;
                servicos.talhoes().cadastrar(propriedadeId, campoNome.getText(), area, cultura);
            }
        });
        if (ok) {
            Estilo.sucesso(this, edicao ? "Talhão alterado com sucesso." : "Talhão cadastrado com sucesso.");
            limpar();
            aoAlterar.run();
        }
    }

    private void excluir() {
        if (idEmEdicao <= 0) {
            JOptionPane.showMessageDialog(this, "Selecione um talhão na lista para excluir.");
            return;
        }
        long id = idEmEdicao;
        TalhaoService.Vinculos v = servicos.talhoes().contarVinculos(id);
        String msg = "Excluir o talhão \"" + campoNome.getText() + "\"?\n\n"
                + "Também serão removidos " + v.indicadores() + " registro(s) de indicadores (histórico) e "
                + v.ocorrencias() + " ocorrência(s) vinculados a ele.\nEsta ação não pode ser desfeita.";
        int r = JOptionPane.showConfirmDialog(this, msg, "Confirmar exclusão", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (r == JOptionPane.YES_OPTION && Estilo.executar(this, () -> servicos.talhoes().excluir(id))) {
            Estilo.sucesso(this, "Talhão excluído.");
            limpar();
            aoAlterar.run();
        }
    }

    private void limpar() {
        idEmEdicao = 0;
        campoNome.setText("");
        campoArea.setText("");
        comboCultura.setSelectedIndex(0);
        comboPropriedade.setEnabled(true);
        rotuloModo.setText("Novo cadastro");
        tabela.clearSelection();
    }

    private void selecionar() {
        int linha = tabela.getSelectedRow();
        if (linha >= tabela.getRowCount()) {
            return;
        }
        if (linha < 0) {
            return;
        }
        long id = (Long) modelo.getValueAt(tabela.convertRowIndexToModel(linha), 0);
        servicos.talhoes().buscar(id).ifPresent(t -> {
            idEmEdicao = t.getId();
            campoNome.setText(t.getNome());
            campoArea.setText(Estilo.numero(t.getAreaHectares()));
            comboCultura.setSelectedItem(t.getCultura());
            for (int i = 0; i < comboPropriedade.getItemCount(); i++) {
                if (comboPropriedade.getItemAt(i).id() == t.getPropriedadeId()) {
                    comboPropriedade.setSelectedIndex(i);
                }
            }
            comboPropriedade.setEnabled(false);
            rotuloModo.setText("Editando o talhão #" + t.getId() + " (propriedade não pode ser alterada)");
        });
    }

    private void carregarTabela() {
        tabela.clearSelection();
        tabela.setRowSorter(null); // recriado ao final, evitando ordenador desatualizado durante a recarga
        modelo.setRowCount(0);
        Object f = comboFiltro.getSelectedItem();
        long filtro = f instanceof OpcaoPropriedade op ? op.id() : 0;
        List<Talhao> lista = filtro > 0 ? servicos.talhoes().listarPorPropriedade(filtro) : servicos.talhoes().listar();
        for (Talhao t : lista) {
            String prop = servicos.propriedades().buscar(t.getPropriedadeId()).map(Propriedade::getNome).orElse("?");
            modelo.addRow(new Object[]{t.getId(), prop, t.getNome(), Estilo.numero(t.getAreaHectares()), t.getCultura(),
                    servicos.indicadores().listarPorTalhao(t.getId()).size()});
        }
        tabela.setRowSorter(new TableRowSorter<>(modelo));
        if (servicos.propriedades().listar().isEmpty()) {
            aviso.setText("Nenhuma propriedade cadastrada. Cadastre uma propriedade antes de cadastrar talhões.");
        } else {
            aviso.setText(lista.isEmpty() ? "Nenhum talhão cadastrado para o filtro selecionado."
                    : lista.size() + " talhão(ões) listado(s).");
        }
    }

    @Override
    public void atualizar() {
        carregando = true;
        Object filtroAtual = comboFiltro.getSelectedItem();
        long filtroId = filtroAtual instanceof OpcaoPropriedade op ? op.id() : 0;
        comboPropriedade.removeAllItems();
        comboFiltro.removeAllItems();
        comboFiltro.addItem(new OpcaoPropriedade(0, "(Todas as propriedades)"));
        for (Propriedade p : servicos.propriedades().listar()) {
            OpcaoPropriedade op = new OpcaoPropriedade(p.getId(), p.getNome() + " — " + p.getMunicipio());
            comboPropriedade.addItem(op);
            comboFiltro.addItem(op);
            if (p.getId() == filtroId) {
                comboFiltro.setSelectedItem(op);
            }
        }
        carregando = false;
        carregarTabela();
        if (idEmEdicao > 0 && servicos.talhoes().buscar(idEmEdicao).isEmpty()) {
            limpar();
        }
    }
}
