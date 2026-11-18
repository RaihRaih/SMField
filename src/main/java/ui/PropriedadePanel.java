package ui;

import domain.Propriedade;
import domain.Talhao;
import service.ServicosSMField;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.util.List;

/** RF-01 e RF-03: cadastro de propriedades e consulta dos talhões de cada propriedade. */
public class PropriedadePanel extends JPanel implements Atualizavel {

    private final ServicosSMField servicos;
    private final Runnable aoAlterar;

    private final JTextField campoNome = new JTextField(24);
    private final JTextField campoMunicipio = new JTextField(24);
    private final JLabel rotuloModo = Estilo.indicadorModo("Novo cadastro");
    private final JButton botaoSalvar = Estilo.botaoPrimario("Salvar propriedade", Icone.Tipo.CHECK);
    private final JButton botaoNovo = Estilo.botao("Limpar / novo", Icone.Tipo.LIMPAR);
    private final JButton botaoExcluir = Estilo.botaoPerigo("Excluir");

    private final DefaultTableModel modeloPropriedades = Estilo.modelo("ID", "Propriedade", "Município", "Talhões");
    private final JTable tabelaPropriedades = Estilo.tabela(modeloPropriedades);
    private final DefaultTableModel modeloTalhoes = Estilo.modelo("ID", "Talhão", "Área (ha)", "Cultura");
    private final JTable tabelaTalhoes = Estilo.tabela(modeloTalhoes);
    private final JLabel avisoPropriedades = Estilo.nota(" ");
    private final JLabel avisoTalhoes = Estilo.nota("Selecione uma propriedade para consultar seus talhões.");

    private long idEmEdicao = 0;

    public PropriedadePanel(ServicosSMField servicos, Runnable aoAlterar) {
        this.servicos = servicos;
        this.aoAlterar = aoAlterar;
        Estilo.pagina(this);
        add(Estilo.cabecalho("Propriedades rurais",
                "Cadastre, edite e consulte as propriedades e os talhões vinculados a cada uma."), BorderLayout.NORTH);
        add(criarFormulario(), BorderLayout.WEST);

        Cartao lista = new Cartao("Propriedades cadastradas", "Selecione uma linha para editar ou ver os talhões",
                Icone.Tipo.PROPRIEDADE);
        tabelaPropriedades.getColumnModel().getColumn(0).setMaxWidth(56);
        tabelaPropriedades.getColumnModel().getColumn(1).setPreferredWidth(300);
        tabelaPropriedades.getColumnModel().getColumn(2).setPreferredWidth(200);
        tabelaPropriedades.getColumnModel().getColumn(2).setCellRenderer(Estilo.renderizadorComIcone(Icone.Tipo.LOCAL));
        tabelaPropriedades.getColumnModel().getColumn(3).setMaxWidth(90);
        tabelaPropriedades.getColumnModel().getColumn(3).setCellRenderer(Estilo.renderizadorContagem());
        lista.corpo().add(Estilo.rolagem(tabelaPropriedades), BorderLayout.CENTER);
        lista.corpo().add(avisoPropriedades, BorderLayout.SOUTH);

        Cartao talhoes = new Cartao("Talhões da propriedade selecionada", "Consulta dos talhões vinculados",
                Icone.Tipo.TALHAO);
        tabelaTalhoes.getColumnModel().getColumn(0).setMaxWidth(56);
        tabelaTalhoes.getColumnModel().getColumn(2).setMinWidth(96);
        tabelaTalhoes.getColumnModel().getColumn(2).setMaxWidth(120);
        tabelaTalhoes.getColumnModel().getColumn(3).setCellRenderer(Estilo.renderizadorComIcone(Icone.Tipo.BROTO));
        talhoes.corpo().add(Estilo.rolagem(tabelaTalhoes), BorderLayout.CENTER);
        talhoes.corpo().add(avisoTalhoes, BorderLayout.SOUTH);

        JSplitPane divisao = new JSplitPane(JSplitPane.VERTICAL_SPLIT, lista, talhoes);
        divisao.setResizeWeight(0.55);
        divisao.setBorder(null);
        divisao.setOpaque(false);
        divisao.setDividerSize(14);
        JPanel centro = new JPanel(new BorderLayout());
        centro.setOpaque(false);
        centro.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 0));
        centro.add(divisao, BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);

        botaoSalvar.addActionListener(e -> salvar());
        botaoNovo.addActionListener(e -> limpar());
        botaoExcluir.addActionListener(e -> excluir());
        tabelaPropriedades.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selecionar();
            }
        });
        atualizar();
    }

    private JPanel criarFormulario() {
        Cartao form = new Cartao("Cadastro", "Campos com * são obrigatórios", Icone.Tipo.MAIS);
        JPanel col = Estilo.coluna();
        Estilo.empilhar(col, rotuloModo, 14);
        Estilo.exemplo(campoNome, "Ex.: Fazenda Boa Vista");
        Estilo.exemplo(campoMunicipio, "Ex.: Sorriso/MT");
        campoMunicipio.putClientProperty("JTextField.leadingIcon", Icone.de(Icone.Tipo.LOCAL, 15, Estilo.TEXTO_FRACO));
        Estilo.empilhar(col, Estilo.blocoCampo("Nome da propriedade", true, campoNome), 14);
        Estilo.empilhar(col, Estilo.blocoCampo("Município", true, campoMunicipio), 6);
        Estilo.empilhar(col, Estilo.dica("Use Cidade/UF para achar a estação do INMET."), 18);
        Estilo.empilhar(col, botaoSalvar, 10);
        JPanel secundarios = new JPanel(new java.awt.GridLayout(1, 2, 10, 0));
        secundarios.setOpaque(false);
        secundarios.add(botaoNovo);
        secundarios.add(botaoExcluir);
        secundarios.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, botaoNovo.getPreferredSize().height));
        Estilo.empilhar(col, secundarios, 0);
        form.corpo().add(col, BorderLayout.NORTH);
        form.setPreferredSize(new java.awt.Dimension(360, 300));
        return form;
    }

    private void salvar() {
        boolean edicao = idEmEdicao > 0;
        boolean ok = Estilo.executar(this, () -> {
            if (edicao) {
                servicos.propriedades().editar(idEmEdicao, campoNome.getText(), campoMunicipio.getText());
            } else {
                servicos.propriedades().cadastrar(campoNome.getText(), campoMunicipio.getText());
            }
        });
        if (ok) {
            Estilo.sucesso(this, edicao ? "Propriedade alterada com sucesso." : "Propriedade cadastrada com sucesso.");
            limpar();
            aoAlterar.run();
        }
    }

    private void excluir() {
        if (idEmEdicao <= 0) {
            JOptionPane.showMessageDialog(this, "Selecione uma propriedade na lista para excluir.");
            return;
        }
        int r = JOptionPane.showConfirmDialog(this, "Excluir a propriedade \"" + campoNome.getText() + "\"?",
                "Confirmar exclusão", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (r == JOptionPane.YES_OPTION && Estilo.executar(this, () -> servicos.propriedades().excluir(idEmEdicao))) {
            Estilo.sucesso(this, "Propriedade excluída.");
            limpar();
            aoAlterar.run();
        }
    }

    private void limpar() {
        idEmEdicao = 0;
        campoNome.setText("");
        campoMunicipio.setText("");
        rotuloModo.setText("Novo cadastro");
        tabelaPropriedades.clearSelection();
        campoNome.requestFocusInWindow();
    }

    private void selecionar() {
        int linha = tabelaPropriedades.getSelectedRow();
        if (linha >= tabelaPropriedades.getRowCount()) {
            return;
        }
        modeloTalhoes.setRowCount(0);
        if (linha < 0) {
            avisoTalhoes.setText("Selecione uma propriedade para consultar seus talhões.");
            return;
        }
        long id = (Long) modeloPropriedades.getValueAt(tabelaPropriedades.convertRowIndexToModel(linha), 0);
        servicos.propriedades().buscar(id).ifPresent(p -> {
            idEmEdicao = p.getId();
            campoNome.setText(p.getNome());
            campoMunicipio.setText(p.getMunicipio());
            rotuloModo.setText("Editando a propriedade #" + p.getId());
            List<Talhao> talhoes = servicos.talhoes().listarPorPropriedade(p.getId());
            for (Talhao t : talhoes) {
                modeloTalhoes.addRow(new Object[]{t.getId(), t.getNome(), Estilo.numero(t.getAreaHectares()), t.getCultura()});
            }
            avisoTalhoes.setText(talhoes.isEmpty()
                    ? "Esta propriedade ainda não possui talhões. Cadastre-os na aba \"Talhões\"."
                    : talhoes.size() + " talhão(ões) em " + p.getNome() + ".");
        });
    }

    @Override
    public void atualizar() {
        long manter = idEmEdicao;
        tabelaPropriedades.clearSelection();
        idEmEdicao = manter;
        modeloPropriedades.setRowCount(0);
        List<Propriedade> lista = servicos.propriedades().listar();
        for (Propriedade p : lista) {
            modeloPropriedades.addRow(new Object[]{p.getId(), p.getNome(), p.getMunicipio(),
                    servicos.talhoes().listarPorPropriedade(p.getId()).size()});
        }
        avisoPropriedades.setText(lista.isEmpty()
                ? "Nenhuma propriedade cadastrada. Preencha o formulário ao lado para começar."
                : lista.size() + " propriedade(s) cadastrada(s).");
        if (manter > 0) {
            for (int i = 0; i < tabelaPropriedades.getRowCount(); i++) {
                if (((Long) tabelaPropriedades.getValueAt(i, 0)) == manter) {
                    tabelaPropriedades.setRowSelectionInterval(i, i);
                    return;
                }
            }
        }
        modeloTalhoes.setRowCount(0);
    }
}
