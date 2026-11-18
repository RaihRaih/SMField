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
    private final JLabel rotuloModo = Estilo.indicadorModo("Novo cadastro");
    private final JButton botaoSalvar = Estilo.botaoPrimario("Salvar talhão", Icone.Tipo.CHECK);
    private final JButton botaoNovo = Estilo.botao("Limpar / novo", Icone.Tipo.LIMPAR);
    private final JButton botaoExcluir = Estilo.botaoPerigo("Excluir");

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
        Estilo.pagina(this);
        add(Estilo.cabecalho("Talhões",
                "Áreas de cultivo de cada propriedade: área, cultura e quantidade de registros de indicadores."),
                BorderLayout.NORTH);
        add(criarFormulario(), BorderLayout.WEST);

        Cartao lista = new Cartao("Talhões cadastrados", "Selecione uma linha para editar ou excluir", Icone.Tipo.TALHAO);
        JLabel rotuloFiltro = new JLabel("Propriedade");
        rotuloFiltro.setFont(Estilo.ROTULO);
        rotuloFiltro.setForeground(Estilo.TEXTO_SUAVE);
        comboFiltro.setPreferredSize(new java.awt.Dimension(280, comboFiltro.getPreferredSize().height));
        lista.acoes(rotuloFiltro, comboFiltro);
        tabela.getColumnModel().getColumn(0).setMaxWidth(56);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(220);
        tabela.getColumnModel().getColumn(1).setCellRenderer(Estilo.renderizadorComIcone(Icone.Tipo.PROPRIEDADE));
        tabela.getColumnModel().getColumn(2).setPreferredWidth(200);
        tabela.getColumnModel().getColumn(3).setMinWidth(84);
        tabela.getColumnModel().getColumn(3).setMaxWidth(100);
        tabela.getColumnModel().getColumn(4).setMinWidth(120);
        tabela.getColumnModel().getColumn(4).setCellRenderer(Estilo.renderizadorComIcone(Icone.Tipo.BROTO));
        tabela.getColumnModel().getColumn(5).setMinWidth(96);
        tabela.getColumnModel().getColumn(5).setMaxWidth(110);
        tabela.getColumnModel().getColumn(5).setCellRenderer(Estilo.renderizadorContagem());
        lista.corpo().add(Estilo.rolagem(tabela), BorderLayout.CENTER);
        lista.corpo().add(aviso, BorderLayout.SOUTH);
        JPanel centro = new JPanel(new BorderLayout());
        centro.setOpaque(false);
        centro.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 0));
        centro.add(lista, BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);

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
        Cartao form = new Cartao("Cadastro / edição", "Campos com * são obrigatórios", Icone.Tipo.MAIS);
        JPanel col = Estilo.coluna();
        Estilo.empilhar(col, rotuloModo, 14);
        Estilo.empilhar(col, Estilo.blocoCampo("Propriedade", true, comboPropriedade), 14);
        Estilo.exemplo(campoNome, "Ex.: Talhão Norte");
        Estilo.empilhar(col, Estilo.blocoCampo("Nome do talhão", true, campoNome), 14);
        JPanel areaCultura = new JPanel(new java.awt.GridLayout(1, 2, 12, 0));
        areaCultura.setOpaque(false);
        Estilo.exemplo(campoArea, "Ex.: 12,5");
        campoArea.putClientProperty("JTextField.trailingComponent", Estilo.dica("ha  "));
        areaCultura.add(Estilo.blocoCampo("Área (hectares)", true, campoArea));
        areaCultura.add(Estilo.blocoCampo("Cultura agrícola", true, comboCultura));
        areaCultura.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, areaCultura.getPreferredSize().height));
        Estilo.empilhar(col, areaCultura, 6);
        Estilo.empilhar(col, Estilo.dica("A área deve ser maior que zero. A cultura pode ser digitada."), 18);
        Estilo.empilhar(col, botaoSalvar, 10);
        JPanel secundarios = new JPanel(new java.awt.GridLayout(1, 2, 10, 0));
        secundarios.setOpaque(false);
        secundarios.add(botaoNovo);
        secundarios.add(botaoExcluir);
        secundarios.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, botaoNovo.getPreferredSize().height));
        Estilo.empilhar(col, secundarios, 16);
        Estilo.empilhar(col, Estilo.faixaAviso("Ao excluir um talhão, os indicadores e ocorrências dele também são "
                + "removidos, após confirmação.", true), 0);
        form.corpo().add(col, BorderLayout.NORTH);
        form.setPreferredSize(new java.awt.Dimension(380, 300));
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
            rotuloModo.setText("Editando o talhão #" + t.getId() + " · propriedade fixa");
            rotuloModo.setToolTipText("Na edição, a propriedade do talhão não pode ser alterada.");
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
