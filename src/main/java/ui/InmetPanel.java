package ui;

import domain.EstacaoMeteorologica;
import domain.LeituraClimaticaDiaria;
import domain.Talhao;
import service.ConversorEntrada;
import service.InmetService;
import service.ServicosSMField;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Importação de clima real do INMET para um talhão, com prévia do risco antes de gravar. */
public class InmetPanel extends JPanel implements Atualizavel {

    private final ServicosSMField servicos;
    private final Runnable aoAlterar;

    private final JComboBox<Estilo.OpcaoTalhao> comboTalhao = new JComboBox<>();
    private final JTextField campoBusca = new JTextField(16);
    private final JButton botaoBuscar = Estilo.botao("Buscar estação");
    private final JComboBox<EstacaoMeteorologica> comboEstacao = new JComboBox<>();
    private final JLabel infoEstacao = Estilo.nota("Carregando a base do INMET...");
    private final JTextField campoInicio = new JTextField(10);
    private final JTextField campoFim = new JTextField(10);
    private final JButton botaoPrevia = Estilo.botao("Visualizar prévia");
    private final JButton botaoImportar = Estilo.botaoPrimario("Importar para o talhão");

    private final DefaultTableModel modelo = Estilo.modelo("Data", "Temp. média (°C)", "Umidade do ar (%)",
            "Chuva do dia (mm)", "Umidade do solo SIMULADA (%)", "Índice", "Classificação", "Situação");
    private final JTable tabela = Estilo.tabela(modelo);
    private final JLabel resumo = new JLabel(" ");

    private boolean baseCarregada = false;
    private boolean carregandoCombos = false;

    public InmetPanel(ServicosSMField servicos, Runnable aoAlterar) {
        this.servicos = servicos;
        this.aoAlterar = aoAlterar;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setBackground(Estilo.CINZA_FUNDO);

        JPanel topo = new JPanel(new BorderLayout(6, 6));
        topo.setOpaque(false);
        topo.add(Estilo.titulo("Dados reais do INMET"), BorderLayout.NORTH);
        topo.add(Estilo.nota("<html>" + InmetService.AVISO + "</html>"), BorderLayout.CENTER);
        add(topo, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(Estilo.secao("Importação"));
        GridBagConstraints c = Estilo.gbc(0, 0);
        form.add(new JLabel("Talhão de destino *"), c);
        c.gridx = 1;
        c.gridwidth = 3;
        form.add(comboTalhao, c);
        c.gridwidth = 1;
        c.gridy++;
        c.gridx = 0;
        form.add(new JLabel("Buscar estação (cidade, UF ou código)"), c);
        c.gridx = 1;
        form.add(campoBusca, c);
        c.gridx = 2;
        form.add(botaoBuscar, c);
        c.gridy++;
        c.gridx = 0;
        form.add(new JLabel("Estação INMET *"), c);
        c.gridx = 1;
        c.gridwidth = 3;
        form.add(comboEstacao, c);
        c.gridy++;
        c.gridx = 1;
        form.add(infoEstacao, c);
        c.gridwidth = 1;
        c.gridy++;
        c.gridx = 0;
        form.add(new JLabel("Período (dd/mm/aaaa) *"), c);
        JPanel periodo = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        periodo.setOpaque(false);
        periodo.add(campoInicio);
        periodo.add(new JLabel("até"));
        periodo.add(campoFim);
        periodo.add(Estilo.nota("máximo " + InmetService.LIMITE_DIAS + " dias"));
        c.gridx = 1;
        c.gridwidth = 3;
        form.add(periodo, c);
        c.gridy++;
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        botoes.setOpaque(false);
        botoes.add(botaoPrevia);
        botoes.add(botaoImportar);
        form.add(botoes, c);

        JPanel lista = new JPanel(new BorderLayout(4, 4));
        lista.setBorder(Estilo.secao("Prévia dia a dia (nada é gravado até clicar em Importar)"));
        tabela.getColumnModel().getColumn(6).setCellRenderer(Estilo.renderizadorNivel());
        tabela.getColumnModel().getColumn(4).setPreferredWidth(200);
        tabela.getColumnModel().getColumn(7).setPreferredWidth(220);
        lista.add(Estilo.rolagem(tabela), BorderLayout.CENTER);
        resumo.setFont(Estilo.NORMAL);
        lista.add(resumo, BorderLayout.SOUTH);

        JPanel centro = new JPanel(new BorderLayout(10, 10));
        centro.setOpaque(false);
        centro.add(form, BorderLayout.NORTH);
        centro.add(lista, BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);

        habilitar(false);
        botaoBuscar.addActionListener(e -> buscar(campoBusca.getText(), null));
        campoBusca.addActionListener(e -> buscar(campoBusca.getText(), null));
        comboEstacao.addActionListener(e -> {
            if (!carregandoCombos) {
                estacaoSelecionada();
            }
        });
        comboTalhao.addActionListener(e -> {
            if (!carregandoCombos && baseCarregada) {
                sugerirEstacao();
            }
        });
        botaoPrevia.addActionListener(e -> previa());
        botaoImportar.addActionListener(e -> importar());
        carregarBaseEmSegundoPlano();
    }

    private void carregarBaseEmSegundoPlano() {
        new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() {
                return servicos.inmet().estacoes().size();
            }

            @Override
            protected void done() {
                try {
                    int total = get();
                    baseCarregada = true;
                    habilitar(true);
                    infoEstacao.setText(total + " estações disponíveis.");
                    atualizar();
                } catch (Exception e) {
                    infoEstacao.setText("Não foi possível carregar a base do INMET: " + e.getMessage());
                }
            }
        }.execute();
    }

    private void habilitar(boolean ativo) {
        botaoBuscar.setEnabled(ativo);
        botaoPrevia.setEnabled(ativo);
        botaoImportar.setEnabled(ativo);
        comboEstacao.setEnabled(ativo);
        campoBusca.setEnabled(ativo);
    }

    /** Preenche a busca com o município da propriedade do talhão e seleciona a estação sugerida. */
    private void sugerirEstacao() {
        long id = Estilo.idTalhao(comboTalhao);
        Optional<Talhao> t = id > 0 ? servicos.talhoes().buscar(id) : Optional.empty();
        if (t.isEmpty()) {
            return;
        }
        servicos.propriedades().buscar(t.get().getPropriedadeId()).ifPresent(p -> {
            Optional<EstacaoMeteorologica> sugestao = servicos.inmet().sugerirParaMunicipio(p.getMunicipio());
            if (sugestao.isPresent()) {
                campoBusca.setText(p.getMunicipio());
                buscar(p.getMunicipio(), sugestao.get().codigo());
            } else {
                semSugestao(p.getMunicipio());
            }
        });
    }

    /** Município sem estação própria: lista todas, sem pré-selecionar, e orienta a busca. */
    private void semSugestao(String municipio) {
        campoBusca.setText("");
        buscar("", null);
        carregandoCombos = true;
        comboEstacao.setSelectedIndex(-1);
        carregandoCombos = false;
        campoInicio.setText("");
        campoFim.setText("");
        infoEstacao.setText("Não há estação do INMET chamada \"" + municipio
                + "\". Busque a cidade mais próxima (ex.: Belo Horizonte) e selecione a estação.");
    }

    private void buscar(String filtro, String selecionar) {
        List<EstacaoMeteorologica> encontradas = servicos.inmet().buscarEstacoes(filtro);
        if (selecionar == null && !filtro.isBlank()) {
            // na busca, pré-seleciona a estação com mais dias de dados
            selecionar = encontradas.stream()
                    .max(InmetService.MAIS_DADOS)
                    .map(EstacaoMeteorologica::codigo).orElse(null);
        }
        carregandoCombos = true;
        comboEstacao.removeAllItems();
        for (EstacaoMeteorologica e : encontradas) {
            comboEstacao.addItem(e);
            if (e.codigo().equals(selecionar)) {
                comboEstacao.setSelectedItem(e);
            }
        }
        carregandoCombos = false;
        if (encontradas.isEmpty()) {
            infoEstacao.setText("Nenhuma estação encontrada para \"" + filtro + "\". Tente o nome da cidade, a UF ou o código.");
        }
        estacaoSelecionada();
    }

    private void estacaoSelecionada() {
        Object o = comboEstacao.getSelectedItem();
        if (!(o instanceof EstacaoMeteorologica e)) {
            return;
        }
        Optional<LocalDate[]> periodo = servicos.inmet().periodoDisponivel(e.codigo());
        if (periodo.isEmpty()) {
            infoEstacao.setText(e.nome() + "/" + e.uf() + " — sem dias completos na base.");
            return;
        }
        LocalDate fim = periodo.get()[1];
        LocalDate inicio = fim.minusDays(29).isBefore(periodo.get()[0]) ? periodo.get()[0] : fim.minusDays(29);
        infoEstacao.setText(String.format("%s/%s · região %s · altitude %s m · cobertura %s%% · dados de %s a %s",
                e.nome(), e.uf(), e.regiao(), Estilo.numero(e.altitudeM()), Estilo.numero(e.coberturaPct()),
                Estilo.formatar(periodo.get()[0]), Estilo.formatar(fim)));
        campoInicio.setText(Estilo.formatar(inicio));
        campoFim.setText(Estilo.formatar(fim));
    }

    private String codigoEstacao() {
        Object o = comboEstacao.getSelectedItem();
        return o instanceof EstacaoMeteorologica e ? e.codigo() : null;
    }

    private void previa() {
        modelo.setRowCount(0);
        long talhaoId = Estilo.idTalhao(comboTalhao);
        List<InmetService.DiaInmet>[] dias = new List[1];
        boolean ok = Estilo.executar(this, () -> {
            if (codigoEstacao() == null) {
                throw new domain.ValidacaoException("Selecione uma estação do INMET.");
            }
            dias[0] = servicos.inmet().previa(talhaoId, codigoEstacao(),
                    ConversorEntrada.data(campoInicio.getText()), ConversorEntrada.data(campoFim.getText()));
        });
        if (!ok) {
            return;
        }
        int prontos = 0;
        for (InmetService.DiaInmet d : dias[0]) {
            LeituraClimaticaDiaria l = d.leitura();
            boolean completo = d.situacao() != InmetService.Situacao.INCOMPLETO;
            modelo.addRow(new Object[]{Estilo.formatar(d.data()),
                    completo ? Estilo.numero(l.tempMedia()) : "—",
                    completo ? Estilo.numero(l.umidadeAr()) : "—",
                    completo ? Estilo.numero(l.chuvaMm()) : "—",
                    completo ? Estilo.numero(d.umidadeSoloSimulada()) : "—",
                    d.resultado().getIndice() == null ? "—" : d.resultado().getIndice(),
                    d.resultado().getNivel(), d.situacao()});
            if (d.situacao() == InmetService.Situacao.PRONTO) {
                prontos++;
            }
        }
        resumo.setText(dias[0].size() + " dia(s) no período · " + prontos + " pronto(s) para importar"
                + (talhaoId <= 0 ? " · selecione um talhão para importar" : "") + ".");
    }

    private void importar() {
        long talhaoId = Estilo.idTalhao(comboTalhao);
        if (talhaoId <= 0) {
            JOptionPane.showMessageDialog(this, "Selecione o talhão de destino. Cadastre um talhão na aba \"Talhões\" se necessário.");
            return;
        }
        InmetService.ResultadoImportacao[] r = new InmetService.ResultadoImportacao[1];
        boolean ok = Estilo.executar(this, () -> {
            if (codigoEstacao() == null) {
                throw new domain.ValidacaoException("Selecione uma estação do INMET.");
            }
            r[0] = servicos.inmet().importar(talhaoId, codigoEstacao(),
                    ConversorEntrada.data(campoInicio.getText()), ConversorEntrada.data(campoFim.getText()));
        });
        if (ok) {
            Estilo.sucesso(this, r[0].importados() + " dia(s) importado(s) como indicadores do talhão.\n"
                    + r[0].jaExistentes() + " já existiam (não duplicados) · " + r[0].incompletos()
                    + " sem dados completos no INMET (ignorados).\n\nUmidade do solo SIMULADA; demais valores reais do INMET.");
            aoAlterar.run();
            previa();
        }
    }

    public void selecionarTalhao(long talhaoId) {
        Estilo.selecionarTalhao(comboTalhao, talhaoId);
    }

    @Override
    public void atualizar() {
        carregandoCombos = true;
        Estilo.carregarTalhoes(comboTalhao, servicos);
        carregandoCombos = false;
        if (baseCarregada && comboEstacao.getItemCount() == 0) {
            sugerirEstacao();
            if (comboEstacao.getItemCount() == 0) {
                semSugestao("");
            }
        }
    }
}
