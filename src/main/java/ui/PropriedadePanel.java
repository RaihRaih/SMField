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
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;

/** RF-01 e RF-03: cadastro de propriedades e consulta dos talhões de cada propriedade. */
public class PropriedadePanel extends JPanel implements Atualizavel {

    private final ServicosSMField servicos;
    private final Runnable aoAlterar;

    private final JTextField campoNome = new JTextField(24);
    private final JTextField campoMunicipio = new JTextField(24);
    private final JLabel rotuloModo = Estilo.nota("Novo cadastro");
    private final JButton botaoSalvar = Estilo.botaoPrimario("Salvar propriedade");
    private final JButton botaoNovo = Estilo.botao("Limpar / novo");
    private final JButton botaoExcluir = Estilo.botao("Excluir propriedade");

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
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setBackground(Estilo.CINZA_FUNDO);

        add(Estilo.titulo("Propriedades rurais"), BorderLayout.NORTH);
        add(criarFormulario(), BorderLayout.WEST);

        JPanel lista = new JPanel(new BorderLayout(4, 4));
        lista.setBorder(Estilo.secao("Propriedades cadastradas"));
        lista.add(Estilo.rolagem(tabelaPropriedades), BorderLayout.CENTER);
        lista.add(avisoPropriedades, BorderLayout.SOUTH);

        JPanel talhoes = new JPanel(new BorderLayout(4, 4));
        talhoes.setBorder(Estilo.secao("Talhões da propriedade selecionada"));
        talhoes.add(Estilo.rolagem(tabelaTalhoes), BorderLayout.CENTER);
        talhoes.add(avisoTalhoes, BorderLayout.SOUTH);

        JSplitPane divisao = new JSplitPane(JSplitPane.VERTICAL_SPLIT, lista, talhoes);
        divisao.setResizeWeight(0.55);
        divisao.setBorder(null);
        add(divisao, BorderLayout.CENTER);

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
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(Estilo.secao("Cadastro"));
        GridBagConstraints c = Estilo.gbc(0, 0);
        form.add(new JLabel("Nome da propriedade *"), c);
        c.gridy++;
        form.add(campoNome, c);
        c.gridy++;
        form.add(new JLabel("Município *"), c);
        c.gridy++;
        form.add(campoMunicipio, c);
        c.gridy++;
        form.add(rotuloModo, c);
        c.gridy++;
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 4));
        botoes.setOpaque(false);
        botoes.add(botaoSalvar);
        form.add(botoes, c);
        c.gridy++;
        form.add(botaoNovo, c);
        c.gridy++;
        form.add(botaoExcluir, c);
        c.gridy++;
        c.weighty = 1;
        form.add(Estilo.nota("* campos obrigatórios"), c);
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
