package ui;

import domain.FatorRisco;
import domain.IndicadorAmbiental;
import domain.NivelRisco;
import domain.ResultadoRisco;
import service.RiscoService;
import service.ServicosSMField;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

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
    private final JButton botaoAgora = Estilo.botao("Agora", Icone.Tipo.RELOGIO);
    private final JButton botaoSalvar = Estilo.botaoPrimario("Salvar e calcular risco", Icone.Tipo.CHECK);
    private final JButton botaoLimpar = Estilo.botao("Limpar campos", Icone.Tipo.LIMPAR);
    private final ResultadoRiscoView resultado = new ResultadoRiscoView();

    private final DefaultTableModel modelo = Estilo.modelo("Data/hora", "Solo (%)", "Temp. (°C)", "Ar (%)",
            "Precip. 24 h (mm)", "Índice", "Classificação", "Fonte dos dados");
    private final JTable tabela = Estilo.tabela(modelo);
    private final JLabel aviso = Estilo.nota(" ");
    private final List<Bloco> blocos = new ArrayList<>();
    private final JLabel ultimoRegistro = Estilo.nota(" ");

    public IndicadoresPanel(ServicosSMField servicos, Runnable aoAlterar) {
        this.servicos = servicos;
        this.aoAlterar = aoAlterar;
        Estilo.pagina(this);
        add(Estilo.cabecalho("Indicadores ambientais",
                "Registre umidade do solo, temperatura, umidade do ar e chuva; o risco é calculado na hora."),
                BorderLayout.NORTH);

        blocos.add(new Bloco("Umidade do solo", Icone.Tipo.SOLO, "%", new Color(0x8A6A3B), IndicadorAmbiental::getUmidadeSolo));
        blocos.add(new Bloco("Temperatura", Icone.Tipo.TEMPERATURA, "°C", new Color(0xD9622B), IndicadorAmbiental::getTemperatura));
        blocos.add(new Bloco("Umidade do ar", Icone.Tipo.AR, "%", new Color(0x2F7FA8), IndicadorAmbiental::getUmidadeAr));
        blocos.add(new Bloco("Precipitação", Icone.Tipo.CHUVA, "mm", new Color(0x2F5FA8), IndicadorAmbiental::getPrecipitacao24h));

        Cartao painelBlocos = new Cartao("Evolução do talhão selecionado", null, Icone.Tipo.TENDENCIA);
        painelBlocos.acoes(ultimoRegistro);
        JPanel grade = new JPanel(new GridLayout(1, 4, 12, 0));
        grade.setOpaque(false);
        blocos.forEach(grade::add);
        painelBlocos.corpo().add(grade, BorderLayout.CENTER);

        Cartao painelResultado = new Cartao("Resultado do último registro salvo",
                "Calculado assim que os indicadores são gravados", Icone.Tipo.CONSULTA);
        painelResultado.corpo().add(resultado, BorderLayout.CENTER);
        painelResultado.corpo().add(Estilo.dica(RiscoService.AVISO_ACADEMICO), BorderLayout.SOUTH);

        Cartao lista = new Cartao("Registros do talhão selecionado", "Mais recente primeiro · registros anteriores "
                + "são preservados no histórico", Icone.Tipo.HISTORICO);
        tabela.getColumnModel().getColumn(0).setMinWidth(130);
        tabela.getColumnModel().getColumn(5).setCellRenderer(Estilo.renderizadorContagem());
        tabela.getColumnModel().getColumn(5).setMaxWidth(80);
        tabela.getColumnModel().getColumn(6).setCellRenderer(Estilo.renderizadorNivel());
        tabela.getColumnModel().getColumn(6).setMinWidth(150);
        tabela.getColumnModel().getColumn(7).setPreferredWidth(260);
        javax.swing.JScrollPane rolagem = Estilo.rolagem(tabela);
        rolagem.setPreferredSize(new Dimension(600, 38 + 6 * 37));
        lista.corpo().add(rolagem, BorderLayout.CENTER);
        lista.corpo().add(aviso, BorderLayout.SOUTH);

        JPanel conteudo = new JPanel(new GridBagLayout());
        conteudo.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 0;
        c.gridheight = 2;
        JComponent form = criarFormulario();
        form.setPreferredSize(new Dimension(390, 486));
        conteudo.add(form, c);
        c.gridheight = 1;
        c.gridx = 1;
        c.weightx = 1;
        c.insets = new Insets(0, 16, 0, 0);
        painelBlocos.setPreferredSize(new Dimension(600, 196));
        conteudo.add(painelBlocos, c);
        c.gridy = 1;
        c.weighty = 1;
        c.insets = new Insets(16, 16, 0, 0);
        painelResultado.setPreferredSize(new Dimension(600, 270));
        conteudo.add(painelResultado, c);
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 2;
        c.weighty = 1;
        c.insets = new Insets(16, 0, 0, 0);
        conteudo.add(lista, c);
        add(Estilo.rolavel(conteudo), BorderLayout.CENTER);

        botaoAgora.addActionListener(e -> campoDataHora.setText(Estilo.formatar(LocalDateTime.now())));
        botaoSalvar.addActionListener(e -> salvar());
        botaoLimpar.addActionListener(e -> limparCampos());
        comboTalhao.addActionListener(e -> carregarRegistros());
        limparCampos();
        atualizar();
    }

    private JComponent criarFormulario() {
        Cartao form = new Cartao("Novo registro", "Campos com * são obrigatórios", Icone.Tipo.MAIS);
        JPanel col = Estilo.coluna();
        Estilo.empilhar(col, Estilo.blocoCampo("Talhão", true, comboTalhao), 14);
        JPanel grade = new JPanel(new GridLayout(2, 2, 12, 12));
        grade.setOpaque(false);
        grade.add(campoIndicador("Umidade do solo (%)", campoSolo, Icone.Tipo.SOLO, "0 a 100"));
        grade.add(campoIndicador("Temperatura (°C)", campoTemperatura, Icone.Tipo.TEMPERATURA, "-20 a 60"));
        grade.add(campoIndicador("Umidade do ar (%)", campoAr, Icone.Tipo.AR, "0 a 100"));
        grade.add(campoIndicador("Precipitação 24 h (mm)", campoPrecipitacao, Icone.Tipo.CHUVA, "acumulado, ≥ 0"));
        grade.setMaximumSize(new Dimension(Integer.MAX_VALUE, grade.getPreferredSize().height));
        Estilo.empilhar(col, grade, 6);
        Estilo.empilhar(col, Estilo.dica("Use vírgula ou ponto nos decimais."), 12);
        JPanel data = new JPanel(new BorderLayout(8, 0));
        data.setOpaque(false);
        campoDataHora.putClientProperty("JTextField.leadingIcon", Icone.de(Icone.Tipo.CALENDARIO, 15, Estilo.TEXTO_FRACO));
        Estilo.exemplo(campoDataHora, "dd/mm/aaaa hh:mm");
        data.add(campoDataHora, BorderLayout.CENTER);
        data.add(botaoAgora, BorderLayout.EAST);
        Estilo.empilhar(col, Estilo.blocoCampo("Data e hora (dd/mm/aaaa hh:mm)", true, data), 18);
        Estilo.empilhar(col, botaoSalvar, 10);
        Estilo.empilhar(col, botaoLimpar, 0);
        form.corpo().add(col, BorderLayout.NORTH);
        return form;
    }

    private static JPanel campoIndicador(String rotulo, JTextField campo, Icone.Tipo icone, String limites) {
        campo.putClientProperty("JTextField.leadingIcon", Icone.de(icone, 15, Estilo.AGRICOLA));
        Estilo.exemplo(campo, limites);
        campo.setToolTipText("Faixa aceita: " + limites);
        return Estilo.blocoCampo(rotulo, true, campo);
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
                    ? "Nenhum talhão cadastrado. Cadastre um talhão na seção \"Talhões\" para registrar indicadores."
                    : "Selecione um talhão.");
            blocos.forEach(b -> b.mostrar(List.of(), null));
            ultimoRegistro.setText(" ");
            return;
        }
        List<IndicadorAmbiental> lista = servicos.indicadores().listarPorTalhao(id);
        for (int i = lista.size() - 1; i >= 0; i--) {
            IndicadorAmbiental ind = lista.get(i);
            ResultadoRisco r = servicos.risco().calcular(ind);
            modelo.addRow(new Object[]{Estilo.formatar(ind.getDataHora()), Estilo.numero(ind.getUmidadeSolo()),
                    Estilo.numero(ind.getTemperatura()), Estilo.numero(ind.getUmidadeAr()),
                    Estilo.numero(ind.getPrecipitacao24h()), r.getIndice(), r.getNivel(), ind.getFonte()});
        }
        List<FatorRisco> fatores = lista.isEmpty() ? List.of() : servicos.risco().calcular(lista.get(lista.size() - 1)).getFatores();
        for (int i = 0; i < blocos.size(); i++) {
            blocos.get(i).mostrar(lista, i < fatores.size() ? fatores.get(i) : null);
        }
        ultimoRegistro.setText(lista.isEmpty() ? " " : "Último registro: "
                + Estilo.formatar(lista.get(lista.size() - 1).getDataHora()) + " · " + lista.size() + " registro(s)");
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

    /**
     * Bloco de um indicador: último valor, faixa de referência e situação (vindas do cálculo
     * de risco) e minigráfico com a evolução dos registros do talhão.
     */
    private static final class Bloco extends JPanel {
        private final String unidade;
        private final Function<IndicadorAmbiental, Double> extrator;
        private final JLabel valor = new JLabel("—");
        private final JLabel faixa = Estilo.dica(" ");
        private final JLabel estado = Estilo.dica(" ");
        private final Graficos.SerieTemporal mini;
        private NivelRisco situacao;

        Bloco(String nome, Icone.Tipo icone, String unidade, Color cor, Function<IndicadorAmbiental, Double> extrator) {
            super(new BorderLayout(0, 4));
            this.unidade = unidade;
            this.extrator = extrator;
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
            JLabel titulo = new JLabel(nome, Icone.de(icone, 15, cor), SwingConstants.LEFT);
            titulo.setFont(Estilo.ROTULO);
            titulo.setForeground(Estilo.TEXTO_SUAVE);
            titulo.setIconTextGap(7);
            valor.setFont(Estilo.fonte("SemiBold", 22f));
            valor.setForeground(Estilo.TEXTO);
            JPanel topo = new JPanel(new BorderLayout(0, 2));
            topo.setOpaque(false);
            topo.add(titulo, BorderLayout.NORTH);
            topo.add(valor, BorderLayout.CENTER);
            JPanel linhas = new JPanel(new GridLayout(2, 1));
            linhas.setOpaque(false);
            linhas.add(faixa);
            linhas.add(estado);
            topo.add(linhas, BorderLayout.SOUTH);
            add(topo, BorderLayout.NORTH);
            mini = new Graficos.SerieTemporal(false, true).cor(cor);
            mini.setPreferredSize(new Dimension(120, 34));
            add(mini, BorderLayout.CENTER);
        }

        void mostrar(List<IndicadorAmbiental> registros, FatorRisco fator) {
            if (registros.isEmpty()) {
                valor.setText("—");
                faixa.setText("sem registros");
                estado.setText(" ");
                situacao = null;
                mini.mostrar(List.of());
            } else {
                IndicadorAmbiental ult = registros.get(registros.size() - 1);
                Double v = extrator.apply(ult);
                valor.setText(v == null ? "—" : Estilo.numero(v) + " " + unidade);
                if (fator != null) {
                    int p = fator.penalidade();
                    situacao = p == 0 ? NivelRisco.BAIXO : p >= 100 ? NivelRisco.ALTO : NivelRisco.MEDIO;
                    faixa.setText("Referência " + fator.faixaIdeal());
                    estado.setText(fator.isDesvio() ? "fora da faixa" : "dentro da faixa");
                    estado.setForeground(Estilo.textoDoNivel(situacao));
                    estado.setFont(Estilo.fonte("Medium", 11.5f));
                    setToolTipText(fator.isDesvio() ? fator.situacao() : "dentro da faixa de referência");
                } else {
                    situacao = null;
                    faixa.setText(" ");
                    estado.setText(" ");
                }
                List<Graficos.Ponto> pts = new ArrayList<>();
                for (IndicadorAmbiental r : registros) {
                    Double vr = extrator.apply(r);
                    if (vr != null) {
                        pts.add(new Graficos.Ponto(r.getDataHora(), vr));
                    }
                }
                mini.mostrar(pts);
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Graficos.preparar(g);
            g2.setColor(new Color(0xF8FAF7));
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
            g2.setColor(Estilo.BORDA);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
            if (situacao != null) {
                g2.setColor(Estilo.corDoNivel(situacao));
                g2.fillOval(getWidth() - 16, 14, 7, 7);
            }
            g2.dispose();
        }
    }
}
