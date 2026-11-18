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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Importação de clima real do INMET para um talhão, com prévia do risco antes de gravar. */
public class InmetPanel extends JPanel implements Atualizavel {

    private final ServicosSMField servicos;
    private final Runnable aoAlterar;

    private final JComboBox<Estilo.OpcaoTalhao> comboTalhao = new JComboBox<>();
    private final JTextField campoBusca = new JTextField(16);
    private final JButton botaoBuscar = Estilo.botao("Buscar", Icone.Tipo.BUSCAR);
    private final JComboBox<EstacaoMeteorologica> comboEstacao = new JComboBox<>();
    private final JLabel infoEstacao = Estilo.nota("Carregando a base do INMET...");
    private final JTextField campoInicio = new JTextField(10);
    private final JTextField campoFim = new JTextField(10);
    private final JButton botaoPrevia = Estilo.botao("Visualizar prévia", Icone.Tipo.OLHO);
    private final JButton botaoImportar = Estilo.botaoPrimario("Importar para o talhão", Icone.Tipo.IMPORTAR);

    private final DefaultTableModel modelo = Estilo.modelo("Data", "Temp. média (°C)", "Umidade do ar (%)",
            "Chuva do dia (mm)", "Umidade do solo SIMULADA (%)", "Índice", "Classificação", "Situação");
    private final JTable tabela = Estilo.tabela(modelo);
    private final JLabel resumo = new JLabel(" ");
    private final JLabel estCodigo = new JLabel("—");
    private final JLabel estLocal = new JLabel("—");
    private final JLabel estRegiao = new JLabel("—");
    private final JLabel estAltitude = new JLabel("—");
    private final JLabel estCobertura = new JLabel("—");
    private final JLabel estPeriodo = new JLabel("—");
    private final Graficos.SerieTemporal graficoPrevia = new Graficos.SerieTemporal(true, false)
            .vazio("Clique em \"Visualizar prévia\" para ver o índice dia a dia.");

    private boolean baseCarregada = false;
    private boolean carregandoCombos = false;

    public InmetPanel(ServicosSMField servicos, Runnable aoAlterar) {
        this.servicos = servicos;
        this.aoAlterar = aoAlterar;
        Estilo.pagina(this);
        JPanel topo = new JPanel(new BorderLayout(0, 14));
        topo.setOpaque(false);
        topo.add(Estilo.cabecalho("Dados reais do INMET",
                "Clima diário de estações meteorológicas reais, importado para os talhões com prévia do risco."),
                BorderLayout.NORTH);
        topo.add(Estilo.faixaAviso(InmetService.AVISO, false), BorderLayout.CENTER);
        add(topo, BorderLayout.NORTH);

        Cartao form = new Cartao("Importação", "Campos com * são obrigatórios", Icone.Tipo.IMPORTAR);
        JPanel col = Estilo.coluna();
        Estilo.empilhar(col, Estilo.blocoCampo("Talhão de destino", true, comboTalhao), 12);
        JPanel busca = new JPanel(new BorderLayout(8, 0));
        busca.setOpaque(false);
        campoBusca.putClientProperty("JTextField.leadingIcon", Icone.de(Icone.Tipo.BUSCAR, 15, Estilo.TEXTO_FRACO));
        Estilo.exemplo(campoBusca, "cidade, UF ou código (ex.: Sorriso/MT)");
        busca.add(campoBusca, BorderLayout.CENTER);
        busca.add(botaoBuscar, BorderLayout.EAST);
        Estilo.empilhar(col, Estilo.blocoCampo("Buscar estação", false, busca), 12);
        Estilo.empilhar(col, Estilo.blocoCampo("Estação INMET", true, comboEstacao), 12);
        JPanel periodo = new JPanel(new java.awt.GridBagLayout());
        periodo.setOpaque(false);
        GridBagConstraints p = new GridBagConstraints();
        p.fill = GridBagConstraints.HORIZONTAL;
        p.weightx = 1;
        campoInicio.putClientProperty("JTextField.leadingIcon", Icone.de(Icone.Tipo.CALENDARIO, 15, Estilo.TEXTO_FRACO));
        campoFim.putClientProperty("JTextField.leadingIcon", Icone.de(Icone.Tipo.CALENDARIO, 15, Estilo.TEXTO_FRACO));
        periodo.add(campoInicio, p);
        p.weightx = 0;
        p.insets = new java.awt.Insets(0, 8, 0, 8);
        JLabel ate = Estilo.nota("até");
        periodo.add(ate, p);
        p.weightx = 1;
        p.insets = new java.awt.Insets(0, 0, 0, 0);
        periodo.add(campoFim, p);
        Estilo.empilhar(col, Estilo.blocoCampo("Período (dd/mm/aaaa)", true, periodo), 4);
        Estilo.empilhar(col, Estilo.dica("Máximo de " + InmetService.LIMITE_DIAS + " dias por importação."), 16);
        Estilo.empilhar(col, botaoPrevia, 10);
        Estilo.empilhar(col, botaoImportar, 0);
        form.corpo().add(col, BorderLayout.NORTH);

        Cartao estacao = new Cartao("Estação selecionada", null, Icone.Tipo.ESTACAO);
        JPanel fichas = new JPanel(new java.awt.GridLayout(3, 2, 16, 12));
        fichas.setOpaque(false);
        fichas.add(Estilo.informacao(Icone.Tipo.ESTACAO, "Código", estCodigo));
        fichas.add(Estilo.informacao(Icone.Tipo.LOCAL, "Município / UF", estLocal));
        fichas.add(Estilo.informacao(Icone.Tipo.AREA, "Região", estRegiao));
        fichas.add(Estilo.informacao(Icone.Tipo.TENDENCIA, "Altitude", estAltitude));
        fichas.add(Estilo.informacao(Icone.Tipo.DADOS, "Cobertura da base", estCobertura));
        fichas.add(Estilo.informacao(Icone.Tipo.CALENDARIO, "Dados disponíveis", estPeriodo));
        JPanel topoEstacao = new JPanel(new BorderLayout(0, 12));
        topoEstacao.setOpaque(false);
        topoEstacao.add(fichas, BorderLayout.NORTH);
        infoEstacao.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        topoEstacao.add(infoEstacao, BorderLayout.CENTER);
        estacao.corpo().add(topoEstacao, BorderLayout.NORTH);
        JPanel blocoGrafico = new JPanel(new BorderLayout(0, 6));
        blocoGrafico.setOpaque(false);
        blocoGrafico.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Estilo.BORDA), BorderFactory.createEmptyBorder(10, 0, 0, 0)));
        JLabel tituloGrafico = new JLabel("Índice de risco na prévia (dia a dia)");
        tituloGrafico.setFont(Estilo.ROTULO);
        tituloGrafico.setForeground(Estilo.TEXTO_SUAVE);
        blocoGrafico.add(tituloGrafico, BorderLayout.NORTH);
        blocoGrafico.add(graficoPrevia, BorderLayout.CENTER);
        estacao.corpo().add(blocoGrafico, BorderLayout.CENTER);
        estacao.corpo().add(Estilo.dica("Fonte dos dados: INMET (estações automáticas). Umidade do solo SIMULADA a partir da chuva e do clima."),
                BorderLayout.SOUTH);

        Cartao lista = new Cartao("Prévia dia a dia", "Nada é gravado até clicar em \"Importar para o talhão\"",
                Icone.Tipo.OLHO);
        tabela.getColumnModel().getColumn(0).setMinWidth(96);
        tabela.getColumnModel().getColumn(4).setPreferredWidth(200);
        tabela.getColumnModel().getColumn(5).setCellRenderer(Estilo.renderizadorContagem());
        tabela.getColumnModel().getColumn(5).setMaxWidth(70);
        tabela.getColumnModel().getColumn(6).setCellRenderer(Estilo.renderizadorNivel());
        tabela.getColumnModel().getColumn(6).setMinWidth(150);
        tabela.getColumnModel().getColumn(7).setCellRenderer(Estilo.renderizadorPilula(InmetPanel::pilulaSituacao));
        tabela.getColumnModel().getColumn(7).setPreferredWidth(240);
        tabela.getColumnModel().getColumn(7).setMinWidth(230);
        graficoPrevia.setPreferredSize(new Dimension(340, 150));
        lista.corpo().add(Estilo.rolagem(tabela), BorderLayout.CENTER);
        resumo.setFont(Estilo.MEDIA);
        resumo.setForeground(Estilo.TEXTO_SUAVE);
        lista.corpo().add(resumo, BorderLayout.SOUTH);

        JPanel conteudo = new JPanel(new java.awt.GridBagLayout());
        conteudo.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.gridy = 0;
        c.gridx = 0;
        form.setPreferredSize(new Dimension(420, 506));
        conteudo.add(form, c);
        c.gridx = 1;
        c.weightx = 1;
        c.insets = new java.awt.Insets(0, 16, 0, 0);
        estacao.setPreferredSize(new Dimension(500, 506));
        conteudo.add(estacao, c);
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 2;
        c.weighty = 1;
        c.insets = new java.awt.Insets(16, 0, 0, 0);
        lista.setPreferredSize(new Dimension(600, 340));
        conteudo.add(lista, c);
        add(Estilo.rolavel(conteudo), BorderLayout.CENTER);

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
            for (JLabel l : new JLabel[]{estCodigo, estLocal, estRegiao, estAltitude, estCobertura, estPeriodo}) {
                l.setText("—");
            }
            return;
        }
        Optional<LocalDate[]> periodo = servicos.inmet().periodoDisponivel(e.codigo());
        if (periodo.isEmpty()) {
            infoEstacao.setText(e.nome() + "/" + e.uf() + " — sem dias completos na base.");
            return;
        }
        estCodigo.setText(e.codigo());
        estLocal.setText(e.nome() + " / " + e.uf());
        estRegiao.setText(e.regiao());
        estAltitude.setText(Estilo.numero(e.altitudeM()) + " m");
        estCobertura.setText(Estilo.numero(e.coberturaPct()) + "% dos dias");
        estPeriodo.setText(Estilo.formatar(periodo.get()[0]) + " a " + Estilo.formatar(periodo.get()[1]));
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
        java.util.List<Graficos.Ponto> pontos = new java.util.ArrayList<>();
        for (InmetService.DiaInmet d : dias[0]) {
            if (d.resultado().getIndice() != null) {
                pontos.add(new Graficos.Ponto(d.data().atTime(12, 0), d.resultado().getIndice(), d.resultado().getNivel(), false));
            }
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
        graficoPrevia.mostrar(pontos);
        resumo.setText(dias[0].size() + " dia(s) no período · " + prontos + " pronto(s) para importar"
                + (talhaoId <= 0 ? " · selecione um talhão para importar" : "") + ".");
    }

    private void importar() {
        long talhaoId = Estilo.idTalhao(comboTalhao);
        if (talhaoId <= 0) {
            JOptionPane.showMessageDialog(this, "Selecione o talhão de destino. Cadastre um talhão na seção \"Talhões\" se necessário.");
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

    private static Estilo.Pilula pilulaSituacao(Object v) {
        if (!(v instanceof InmetService.Situacao s)) {
            return null;
        }
        return switch (s) {
            case PRONTO -> new Estilo.Pilula(s.toString(), new Color(0xE6F4EB), new Color(0x1C6A3C), Icone.Tipo.CHECK);
            case JA_IMPORTADO -> new Estilo.Pilula(s.toString(), Estilo.INFO_TINTA, Estilo.INFO, Icone.Tipo.DADOS);
            case INCOMPLETO -> new Estilo.Pilula(s.toString(), new Color(0xEEF1EE), new Color(0x4F5A52), Icone.Tipo.CIRCULO);
        };
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
