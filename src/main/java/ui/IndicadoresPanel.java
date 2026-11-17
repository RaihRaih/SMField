package ui;

import domain.IndicadorAmbiental;
import domain.ResultadoRisco;
import service.RiscoService;
import service.ServicosSMField;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.time.LocalDateTime;
import java.util.List;

/** RF-04 (registro de indicadores) com cálculo imediato do risco (RF-05/RF-06) e lista de registros. */
public class IndicadoresPanel extends JPanel implements Atualizavel {

    private final ServicosSMField servicos;
    private final Runnable aoAlterar;

    private final JComboBox<Estilo.OpcaoTalhao> comboTalhao = new JComboBox<>();
    private final JTextField campoSolo = new JTextField(8);
    private final JTextField campoTemperatura = new JTextField(8);
    private final JTextField campoAr = new JTextField(8);
    private final JTextField campoPrecipitacao = new JTextField(8);
    private final JTextField campoDataHora = new JTextField(14);
    private final JButton botaoAgora = Estilo.botao("Agora");
    private final JButton botaoSalvar = Estilo.botaoPrimario("Salvar e calcular risco");
    private final JButton botaoLimpar = Estilo.botao("Limpar campos");
    private final ResultadoRiscoView resultado = new ResultadoRiscoView();

    private final DefaultTableModel modelo = Estilo.modelo("Data/hora", "Solo (%)", "Temp. (°C)", "Ar (%)",
            "Precip. 24 h (mm)", "Índice", "Classificação");
    private final JTable tabela = Estilo.tabela(modelo);
    private final JLabel aviso = Estilo.nota(" ");

    public IndicadoresPanel(ServicosSMField servicos, Runnable aoAlterar) {
        this.servicos = servicos;
        this.aoAlterar = aoAlterar;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setBackground(Estilo.CINZA_FUNDO);
        add(Estilo.titulo("Indicadores ambientais"), BorderLayout.NORTH);

        JPanel topo = new JPanel(new BorderLayout(10, 10));
        topo.setOpaque(false);
        topo.add(criarFormulario(), BorderLayout.WEST);
        JPanel painelResultado = new JPanel(new BorderLayout(4, 4));
        painelResultado.setBorder(Estilo.secao("Resultado do último registro salvo"));
        painelResultado.add(resultado, BorderLayout.CENTER);
        painelResultado.add(Estilo.nota(RiscoService.AVISO_ACADEMICO), BorderLayout.SOUTH);
        topo.add(painelResultado, BorderLayout.CENTER);

        JPanel lista = new JPanel(new BorderLayout(4, 4));
        lista.setBorder(Estilo.secao("Registros do talhão selecionado (mais recente primeiro)"));
        tabela.getColumnModel().getColumn(6).setCellRenderer(Estilo.renderizadorNivel());
        lista.add(Estilo.rolagem(tabela), BorderLayout.CENTER);
        lista.add(aviso, BorderLayout.SOUTH);

        JPanel centro = new JPanel(new BorderLayout(10, 10));
        centro.setOpaque(false);
        centro.add(topo, BorderLayout.NORTH);
        centro.add(lista, BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);

        botaoAgora.addActionListener(e -> campoDataHora.setText(Estilo.formatar(LocalDateTime.now())));
        botaoSalvar.addActionListener(e -> salvar());
        botaoLimpar.addActionListener(e -> limparCampos());
        comboTalhao.addActionListener(e -> carregarRegistros());
        limparCampos();
        atualizar();
    }

    private JPanel criarFormulario() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(Estilo.secao("Novo registro"));
        GridBagConstraints c = Estilo.gbc(0, 0);
        c.gridwidth = 3;
        form.add(new JLabel("Talhão *"), c);
        c.gridy++;
        form.add(comboTalhao, c);
        c.gridwidth = 1;
        linha(form, c, "Umidade do solo (%) *", campoSolo, "0 a 100");
        linha(form, c, "Temperatura (°C) *", campoTemperatura, "-20 a 60");
        linha(form, c, "Umidade do ar (%) *", campoAr, "0 a 100");
        linha(form, c, "Precipitação 24 h (mm) *", campoPrecipitacao, "acumulado últimas 24 h, ≥ 0");
        c.gridy++;
        c.gridx = 0;
        form.add(new JLabel("Data e hora *"), c);
        c.gridx = 1;
        form.add(campoDataHora, c);
        c.gridx = 2;
        form.add(botaoAgora, c);
        c.gridy++;
        c.gridx = 0;
        c.gridwidth = 3;
        form.add(Estilo.nota("Formato dd/mm/aaaa hh:mm. Use vírgula ou ponto nos decimais."), c);
        c.gridy++;
        form.add(botaoSalvar, c);
        c.gridy++;
        form.add(botaoLimpar, c);
        return form;
    }

    private void linha(JPanel form, GridBagConstraints c, String rotulo, JTextField campo, String dica) {
        c.gridy++;
        c.gridx = 0;
        form.add(new JLabel(rotulo), c);
        c.gridx = 1;
        form.add(campo, c);
        c.gridx = 2;
        form.add(Estilo.nota(dica), c);
    }

    private void salvar() {
        long talhaoId = Estilo.idTalhao(comboTalhao);
        IndicadorAmbiental[] salvo = new IndicadorAmbiental[1];
        boolean ok = Estilo.executar(this, () -> salvo[0] = servicos.indicadores().registrarTexto(talhaoId,
                campoSolo.getText(), campoTemperatura.getText(), campoAr.getText(), campoPrecipitacao.getText(),
                campoDataHora.getText()));
        if (ok) {
            ResultadoRisco r = servicos.risco().calcular(salvo[0]);
            resultado.mostrar(r);
            Estilo.sucesso(this, "Indicadores registrados. Índice de risco: " + r.getIndice() + " ("
                    + Estilo.textoNivel(r.getNivel()) + ").");
            limparCampos();
            aoAlterar.run();
        }
    }

    private void limparCampos() {
        campoSolo.setText("");
        campoTemperatura.setText("");
        campoAr.setText("");
        campoPrecipitacao.setText("");
        campoDataHora.setText(Estilo.formatar(LocalDateTime.now()));
    }

    private void carregarRegistros() {
        modelo.setRowCount(0);
        long id = Estilo.idTalhao(comboTalhao);
        if (id <= 0) {
            aviso.setText(comboTalhao.getItemCount() == 0
                    ? "Nenhum talhão cadastrado. Cadastre um talhão na aba \"Talhões\" para registrar indicadores."
                    : "Selecione um talhão.");
            return;
        }
        List<IndicadorAmbiental> lista = servicos.indicadores().listarPorTalhao(id);
        for (int i = lista.size() - 1; i >= 0; i--) {
            IndicadorAmbiental ind = lista.get(i);
            ResultadoRisco r = servicos.risco().calcular(ind);
            modelo.addRow(new Object[]{Estilo.formatar(ind.getDataHora()), Estilo.numero(ind.getUmidadeSolo()),
                    Estilo.numero(ind.getTemperatura()), Estilo.numero(ind.getUmidadeAr()),
                    Estilo.numero(ind.getPrecipitacao24h()), r.getIndice(), r.getNivel()});
        }
        aviso.setText(lista.isEmpty() ? "Este talhão ainda não possui indicadores registrados (risco NÃO CALCULADO)."
                : lista.size() + " registro(s). Os registros anteriores são preservados no histórico.");
    }

    @Override
    public void atualizar() {
        Estilo.carregarTalhoes(comboTalhao, servicos);
        carregarRegistros();
    }

    /** Usado pela navegação a partir de outras telas. */
    public void selecionarTalhao(long talhaoId) {
        Estilo.selecionarTalhao(comboTalhao, talhaoId);
    }
}
