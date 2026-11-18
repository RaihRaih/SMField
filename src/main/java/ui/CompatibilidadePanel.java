package ui;

import service.CompatibilidadeService;
import service.ServicosSMField;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;

/** Módulo demonstrativo (P3): compatibilidade de culturas por município com base SIMULADA. */
public class CompatibilidadePanel extends JPanel implements Atualizavel {

    private static final Color COMPATIVEL_TINTA = new Color(0xE6F4EB);
    private static final Color COMPATIVEL_TEXTO = new Color(0x1C6A3C);
    private static final Color INCOMPATIVEL_TINTA = new Color(0xFDECEC);
    private static final Color INCOMPATIVEL_TEXTO = new Color(0xA92424);

    private final ServicosSMField servicos;

    private final JComboBox<String> comboMunicipio = new JComboBox<>();
    private final JButton botaoAvaliar = Estilo.botaoPrimario("Avaliar compatibilidade", Icone.Tipo.COMPATIBILIDADE);
    private final JLabel mensagem = new JLabel(" ");
    private final JLabel fonte = Estilo.nota(" ");
    private final JLabel perfilTemp = new JLabel("—");
    private final JLabel perfilSolo = new JLabel("—");
    private final JLabel perfilAr = new JLabel("—");
    private final JLabel perfilPrec = new JLabel("—");
    private final DefaultTableModel modelo = Estilo.modelo("Cultura", "Resultado", "Divergências em relação à base simulada");
    private final JTable tabela = Estilo.tabela(modelo);
    private final JPanel grade = new JPanel(new GridLayout(0, 3, 12, 12));

    public CompatibilidadePanel(ServicosSMField servicos) {
        this.servicos = servicos;
        Estilo.pagina(this);
        JPanel topo = new JPanel(new BorderLayout(0, 14));
        topo.setOpaque(false);
        JLabel rotulo = new JLabel("Município");
        rotulo.setFont(Estilo.ROTULO);
        rotulo.setForeground(Estilo.TEXTO_SUAVE);
        comboMunicipio.setPreferredSize(new Dimension(240, comboMunicipio.getPreferredSize().height));
        topo.add(Estilo.cabecalho("Compatibilidade de culturas",
                "Clima do município comparado a uma base simulada de requisitos por cultura.",
                rotulo, comboMunicipio, botaoAvaliar), BorderLayout.NORTH);
        topo.add(Estilo.faixaAviso(CompatibilidadeService.AVISO + " Usa o clima real da estação do INMET do município "
                + "(ou, se não houver estação, os indicadores registrados nos talhões); pH, textura e fertilidade não "
                + "fazem parte desta versão.", true), BorderLayout.CENTER);
        add(topo, BorderLayout.NORTH);

        Cartao perfil = new Cartao("Médias climáticas utilizadas", "Base da comparação com cada cultura",
                Icone.Tipo.INMET);
        JPanel fichas = new JPanel();
        fichas.setOpaque(false);
        fichas.setLayout(new BoxLayout(fichas, BoxLayout.Y_AXIS));
        ficha(fichas, Icone.Tipo.TEMPERATURA, "Temperatura média", perfilTemp);
        ficha(fichas, Icone.Tipo.SOLO, "Umidade do solo", perfilSolo);
        ficha(fichas, Icone.Tipo.AR, "Umidade do ar", perfilAr);
        ficha(fichas, Icone.Tipo.CHUVA, "Precipitação 24 h", perfilPrec);
        perfil.corpo().add(fichas, BorderLayout.NORTH);
        fonte.setVerticalAlignment(SwingConstants.TOP);
        perfil.corpo().add(fonte, BorderLayout.CENTER);
        perfil.setPreferredSize(new Dimension(340, 300));

        Cartao resultado = new Cartao("Culturas avaliadas", "Resultado demonstrativo — não é recomendação agronômica",
                Icone.Tipo.BROTO);
        mensagem.setFont(Estilo.fonte("SemiBold", 14.5f));
        mensagem.setForeground(Estilo.FLORESTA);
        resultado.corpo().add(mensagem, BorderLayout.NORTH);
        grade.setOpaque(false);
        JPanel gradeTopo = new JPanel(new BorderLayout());
        gradeTopo.setOpaque(false);
        gradeTopo.setBorder(BorderFactory.createEmptyBorder(12, 2, 4, 2));
        gradeTopo.add(grade, BorderLayout.NORTH);
        tabela.getColumnModel().getColumn(0).setPreferredWidth(110);
        tabela.getColumnModel().getColumn(0).setCellRenderer(Estilo.renderizadorComIcone(Icone.Tipo.BROTO));
        tabela.getColumnModel().getColumn(1).setMinWidth(210);
        tabela.getColumnModel().getColumn(1).setCellRenderer(Estilo.renderizadorPilula(v ->
                v == null ? null : "Potencialmente compatível".equals(v)
                        ? new Estilo.Pilula(v.toString(), COMPATIVEL_TINTA, COMPATIVEL_TEXTO, Icone.Tipo.CHECK)
                        : new Estilo.Pilula(v.toString(), INCOMPATIVEL_TINTA, INCOMPATIVEL_TEXTO, Icone.Tipo.FECHAR)));
        tabela.getColumnModel().getColumn(2).setPreferredWidth(700);
        JTabbedPane visoes = new JTabbedPane();
        visoes.putClientProperty("JTabbedPane.tabType", "underlined");
        visoes.putClientProperty("JTabbedPane.showTabSeparators", false);
        visoes.addTab("Cartões", Icone.de(Icone.Tipo.DASHBOARD, 15, Estilo.TEXTO_SUAVE), Estilo.rolavel(gradeTopo));
        JPanel tabelaPainel = new JPanel(new BorderLayout());
        tabelaPainel.setOpaque(false);
        tabelaPainel.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        tabelaPainel.add(Estilo.rolagem(tabela), BorderLayout.CENTER);
        visoes.addTab("Tabela", Icone.de(Icone.Tipo.MENU, 15, Estilo.TEXTO_SUAVE), tabelaPainel);
        resultado.corpo().add(visoes, BorderLayout.CENTER);

        JPanel centro = new JPanel(new BorderLayout(16, 0));
        centro.setOpaque(false);
        centro.add(perfil, BorderLayout.WEST);
        centro.add(resultado, BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);

        // grade responsiva: quantas colunas de cartões cabem na largura disponível
        gradeTopo.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int colunas = Math.max(1, Math.min(4, gradeTopo.getWidth() / 250));
                if (((GridLayout) grade.getLayout()).getColumns() != colunas) {
                    grade.setLayout(new GridLayout(0, colunas, 12, 12));
                    grade.revalidate();
                }
            }
        });
        botaoAvaliar.addActionListener(e -> avaliar());
        atualizar();
    }

    private static void ficha(JPanel destino, Icone.Tipo icone, String rotulo, JLabel valor) {
        JPanel f = Estilo.informacao(icone, rotulo, valor);
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        destino.add(f);
        destino.add(Box.createVerticalStrut(14));
    }

    private void avaliar() {
        modelo.setRowCount(0);
        grade.removeAll();
        Object m = comboMunicipio.getSelectedItem();
        if (m == null) {
            mensagem.setText("Cadastre propriedades para selecionar um município.");
            grade.revalidate();
            grade.repaint();
            return;
        }
        CompatibilidadeService.Avaliacao a = servicos.compatibilidade().avaliar(m.toString());
        mensagem.setText(a.mensagem());
        fonte.setText("<html><span style='color:#8A968D'>Fonte:</span> " + a.fonte() + "</html>");
        if (!a.dadosSuficientes()) {
            perfilTemp.setText("—");
            perfilSolo.setText("—");
            perfilAr.setText("—");
            perfilPrec.setText("—");
            grade.revalidate();
            grade.repaint();
            return;
        }
        perfilTemp.setText(Estilo.numero(a.perfil().temperatura()) + " °C");
        perfilSolo.setText(a.perfil().umidadeSolo() == null ? "não considerada (INMET não mede)"
                : Estilo.numero(a.perfil().umidadeSolo()) + " %");
        perfilAr.setText(Estilo.numero(a.perfil().umidadeAr()) + " %");
        perfilPrec.setText(Estilo.numero(a.perfil().precipitacao()) + " mm/dia (" + a.quantidadeRegistros() + " dias)");
        for (CompatibilidadeService.CompatibilidadeCultura c : a.culturas()) {
            modelo.addRow(new Object[]{c.cultura(), c.compativel() ? "Potencialmente compatível" : "Não compatível",
                    c.divergencias().isEmpty() ? "—" : String.join("; ", c.divergencias())});
            grade.add(new CartaoCultura(c));
        }
        grade.revalidate();
        grade.repaint();
    }

    @Override
    public void atualizar() {
        Object atual = comboMunicipio.getSelectedItem();
        comboMunicipio.removeAllItems();
        for (String m : servicos.compatibilidade().municipios()) {
            comboMunicipio.addItem(m);
        }
        if (atual != null) {
            comboMunicipio.setSelectedItem(atual);
        }
    }

    /** Cartão de uma cultura: ícone, nome, resultado e as divergências apontadas pelo serviço. */
    private static final class CartaoCultura extends JPanel {
        private final boolean compativel;

        CartaoCultura(CompatibilidadeService.CompatibilidadeCultura c) {
            super(new BorderLayout(0, 10));
            this.compativel = c.compativel();
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
            Color texto = compativel ? COMPATIVEL_TEXTO : INCOMPATIVEL_TEXTO;
            Color tinta = compativel ? COMPATIVEL_TINTA : INCOMPATIVEL_TINTA;

            JLabel icone = new JLabel(Icone.de(Icone.Tipo.BROTO, 20, texto)) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = Graficos.preparar(g);
                    g2.setColor(tinta);
                    g2.fillOval(0, 0, getWidth(), getHeight());
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            icone.setHorizontalAlignment(SwingConstants.CENTER);
            icone.setPreferredSize(new Dimension(40, 40));
            JLabel nome = new JLabel(c.cultura());
            nome.setFont(Estilo.fonte("SemiBold", 15f));
            nome.setForeground(Estilo.TEXTO);
            JLabel selo = Estilo.selo(new Estilo.Pilula(compativel ? "Potencialmente compatível" : "Não compatível",
                    tinta, texto, compativel ? Icone.Tipo.CHECK : Icone.Tipo.FECHAR));
            JPanel titulo = new JPanel(new BorderLayout(10, 0));
            titulo.setOpaque(false);
            titulo.add(icone, BorderLayout.WEST);
            titulo.add(nome, BorderLayout.CENTER);
            JPanel linhaSelo = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            linhaSelo.setOpaque(false);
            linhaSelo.add(selo);
            JPanel cabecalho = new JPanel(new BorderLayout(0, 10));
            cabecalho.setOpaque(false);
            cabecalho.add(titulo, BorderLayout.NORTH);
            cabecalho.add(linhaSelo, BorderLayout.SOUTH);
            add(cabecalho, BorderLayout.NORTH);

            String corpo = c.divergencias().isEmpty()
                    ? "Nenhuma divergência em relação à base simulada."
                    : String.join("\n", c.divergencias().stream().map(d -> "• " + d).toList());
            javax.swing.JTextArea detalhes = new javax.swing.JTextArea(corpo);
            detalhes.setLineWrap(true);
            detalhes.setWrapStyleWord(true);
            detalhes.setEditable(false);
            detalhes.setFocusable(false);
            detalhes.setOpaque(false);
            detalhes.setBorder(BorderFactory.createEmptyBorder());
            detalhes.setFont(Estilo.PEQUENA);
            detalhes.setForeground(Estilo.TEXTO_SUAVE);
            add(detalhes, BorderLayout.CENTER);
            setToolTipText(c.cultura() + ": " + (c.divergencias().isEmpty() ? "sem divergências" : String.join("; ", c.divergencias())));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Graficos.preparar(g);
            g2.setColor(new Color(0xFBFCFA));
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
            g2.setColor(Estilo.BORDA);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
            g2.setColor(compativel ? Estilo.RISCO_BAIXO : Estilo.RISCO_ALTO);
            g2.fillRoundRect(0, 16, 3, 28, 3, 3);
            g2.dispose();
        }
    }
}
