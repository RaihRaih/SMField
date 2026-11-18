package ui;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import domain.NivelRisco;
import domain.Talhao;
import domain.ValidacaoException;
import infra.PersistenciaException;
import service.ServicosSMField;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.font.TextAttribute;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

/**
 * Design system do SMField: paleta da marca, tipografia, componentes padronizados e
 * utilitários visuais compartilhados pelas telas. Somente apresentação — nenhuma regra
 * de negócio passa por aqui.
 */
public final class Estilo {

    // ---------------------------------------------------------------- paleta da marca
    /** Verde floresta: cor institucional (logo, títulos). */
    public static final Color FLORESTA = new Color(0x243F2A);
    /** Verde agrícola: ações e elementos interativos. */
    public static final Color AGRICOLA = new Color(0x3D7650);
    public static final Color AGRICOLA_HOVER = new Color(0x346744);
    public static final Color AGRICOLA_PRESSIONADO = new Color(0x2B5739);
    /** Verde-lima suave: detalhes e destaques. */
    public static final Color LIMA = new Color(0xB5DB63);
    public static final Color LIMA_SUAVE = new Color(0xEEF6DC);
    public static final Color VERDE_TINTA = new Color(0xE9F2EB);
    public static final Color VERDE_HOVER = new Color(0xF1F5F0);

    public static final Color FUNDO = new Color(0xF7F9F6);
    public static final Color SUPERFICIE = Color.WHITE;
    public static final Color TEXTO = new Color(0x26332B);
    public static final Color TEXTO_SUAVE = new Color(0x5E6B62);
    public static final Color TEXTO_FRACO = new Color(0x8A968D);
    public static final Color BORDA = new Color(0xE3E9E2);
    public static final Color BORDA_FORTE = new Color(0xCBD5CA);

    public static final Color PERIGO = new Color(0xB42318);
    public static final Color PERIGO_TINTA = new Color(0xFDF0EF);
    public static final Color INFO = new Color(0x2F6690);
    public static final Color INFO_TINTA = new Color(0xEAF3FA);
    public static final Color ATENCAO_TINTA = new Color(0xFEF6E7);
    public static final Color ATENCAO_TEXTO = new Color(0x7A4A00);

    // ---------------------------------------------------------------- cores de risco
    public static final Color RISCO_ALTO = new Color(0xDC4545);
    public static final Color RISCO_MEDIO = new Color(0xF0A12A);
    public static final Color RISCO_BAIXO = new Color(0x32945A);
    public static final Color RISCO_NAO_CALCULADO = new Color(0x8A948C);

    // Nomes anteriores mantidos para compatibilidade com o restante da interface.
    public static final Color VERDE_ESCURO = FLORESTA;
    public static final Color VERDE = AGRICOLA;
    public static final Color VERDE_CLARO = VERDE_TINTA;
    public static final Color CINZA_FUNDO = FUNDO;
    public static final Color CINZA_BORDA = BORDA_FORTE;
    public static final Color CINZA_TEXTO = TEXTO_SUAVE;

    // ---------------------------------------------------------------- tipografia (Inter, licença OFL)
    private static final Map<String, Font> FONTES = carregarFontes();

    public static final Font TITULO_PAGINA = fonte("SemiBold", 23f);
    public static final Font TITULO = fonte("SemiBold", 18f);
    public static final Font SUBTITULO = fonte("SemiBold", 15f);
    public static final Font NORMAL = fonte("Regular", 13.5f);
    public static final Font MEDIA = fonte("Medium", 13.5f);
    public static final Font NEGRITO = fonte("SemiBold", 13.5f);
    public static final Font PEQUENA = fonte("Regular", 12.5f);
    public static final Font ROTULO = fonte("Medium", 12.5f);
    public static final Font MINUSCULA = fonte("SemiBold", 11f);
    public static final Font NUMERO_GRANDE = fonte("SemiBold", 34f);

    public static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    public static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final Map<String, Image> IMAGENS = new HashMap<>();

    private Estilo() {
    }

    private static Map<String, Font> carregarFontes() {
        Map<String, Font> mapa = new HashMap<>();
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        for (String peso : new String[]{"Regular", "Medium", "SemiBold", "Bold"}) {
            try (InputStream in = Estilo.class.getResourceAsStream("/ui/fontes/Inter-" + peso + ".otf")) {
                if (in != null) {
                    Font f = Font.createFont(Font.TRUETYPE_FONT, in);
                    ge.registerFont(f);
                    mapa.put(peso, f);
                }
            } catch (IOException | FontFormatException | RuntimeException e) {
                // sem a fonte da marca, usa a fonte padrão do sistema
            }
        }
        return mapa;
    }

    /** Fonte da marca no peso e tamanho pedidos (cai para a fonte do sistema se indisponível). */
    public static Font fonte(String peso, float tamanho) {
        Font base = FONTES.get(peso);
        if (base == null) {
            int estilo = "SemiBold".equals(peso) || "Bold".equals(peso) ? Font.BOLD : Font.PLAIN;
            return new Font(Font.SANS_SERIF, estilo, Math.round(tamanho));
        }
        return base.deriveFont(tamanho);
    }

    /** Fonte com espaçamento entre letras (rótulos em caixa alta). */
    public static Font espacada(Font f, float espaco) {
        return f.deriveFont(Map.of(TextAttribute.TRACKING, espaco));
    }

    // ---------------------------------------------------------------- tema global

    /** Instala o FlatLaf com a identidade visual do SMField. Chamado uma vez na inicialização. */
    public static void instalarTema() {
        FlatLaf.setGlobalExtraDefaults(Map.of("@accentColor", hex(AGRICOLA)));
        try {
            FlatLightLaf.setup();
        } catch (RuntimeException e) {
            return; // mantém a aparência padrão do Swing
        }
        UIManager.put("defaultFont", new FontUIResource(NORMAL));
        UIManager.put("Panel.background", FUNDO);
        UIManager.put("Label.foreground", TEXTO);
        UIManager.put("Component.arc", 10);
        UIManager.put("Button.arc", 10);
        UIManager.put("TextComponent.arc", 10);
        UIManager.put("CheckBox.arc", 6);
        UIManager.put("Component.focusWidth", 2);
        UIManager.put("Component.innerFocusWidth", 0);
        UIManager.put("Component.focusColor", new Color(0x3D, 0x76, 0x50, 70));
        UIManager.put("Component.focusedBorderColor", AGRICOLA);
        UIManager.put("Component.borderColor", BORDA_FORTE);
        UIManager.put("Component.hoverBorderColor", new Color(0xA9B7A8));
        UIManager.put("TextField.margin", new Insets(6, 10, 6, 10));
        UIManager.put("FormattedTextField.margin", new Insets(6, 10, 6, 10));
        UIManager.put("TextArea.margin", new Insets(6, 8, 6, 8));
        UIManager.put("ComboBox.padding", new Insets(5, 8, 5, 6));
        UIManager.put("ComboBox.buttonStyle", "mac");
        UIManager.put("ComboBox.selectionBackground", VERDE_TINTA);
        UIManager.put("ComboBox.selectionForeground", TEXTO);
        UIManager.put("List.selectionBackground", VERDE_TINTA);
        UIManager.put("List.selectionForeground", TEXTO);
        UIManager.put("Button.margin", new Insets(7, 14, 7, 14));
        UIManager.put("Button.iconTextGap", 8);
        UIManager.put("Button.background", Color.WHITE);
        UIManager.put("Button.borderColor", BORDA_FORTE);
        UIManager.put("Button.hoverBackground", VERDE_HOVER);
        UIManager.put("Button.pressedBackground", VERDE_TINTA);
        UIManager.put("Button.default.background", AGRICOLA);
        UIManager.put("Button.default.foreground", Color.WHITE);
        UIManager.put("Button.default.hoverBackground", AGRICOLA_HOVER);
        UIManager.put("Button.default.pressedBackground", AGRICOLA_PRESSIONADO);
        UIManager.put("Button.default.borderWidth", 0);
        UIManager.put("Button.default.focusedBackground", AGRICOLA);
        UIManager.put("Button.default.focusedBorderColor", FLORESTA);
        UIManager.put("Button.focusedBackground", Color.WHITE);
        // diálogos sempre em português, independentemente do idioma do sistema
        UIManager.put("OptionPane.yesButtonText", "Sim");
        UIManager.put("OptionPane.noButtonText", "Não");
        UIManager.put("OptionPane.cancelButtonText", "Cancelar");
        UIManager.put("OptionPane.okButtonText", "OK");

        UIManager.put("Table.rowHeight", 36);
        UIManager.put("Table.showHorizontalLines", true);
        UIManager.put("Table.showVerticalLines", false);
        UIManager.put("Table.gridColor", new Color(0xEEF2ED));
        UIManager.put("Table.intercellSpacing", new Dimension(0, 1));
        UIManager.put("Table.cellMargins", new Insets(2, 12, 2, 12));
        UIManager.put("Table.selectionBackground", new Color(0xE3EFE6));
        UIManager.put("Table.selectionForeground", TEXTO);
        UIManager.put("Table.selectionInactiveBackground", new Color(0xEBF3ED));
        UIManager.put("Table.selectionInactiveForeground", TEXTO);
        UIManager.put("Table.foreground", TEXTO);
        UIManager.put("Table.background", Color.WHITE);
        UIManager.put("TableHeader.background", new Color(0xF4F7F3));
        UIManager.put("TableHeader.foreground", TEXTO_SUAVE);
        UIManager.put("TableHeader.font", new FontUIResource(ROTULO));
        UIManager.put("TableHeader.height", 38);
        UIManager.put("TableHeader.cellMargins", new Insets(2, 12, 2, 12));
        UIManager.put("TableHeader.separatorColor", new Color(0xF4F7F3));
        UIManager.put("TableHeader.bottomSeparatorColor", BORDA);

        UIManager.put("ScrollBar.width", 11);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
        UIManager.put("ScrollBar.track", new Color(0, 0, 0, 0));
        UIManager.put("ScrollBar.thumb", new Color(0xC9D3C8));
        UIManager.put("ScrollBar.hoverThumbColor", new Color(0xAFBCAE));
        UIManager.put("ScrollBar.showButtons", false);
        UIManager.put("ScrollPane.smoothScrolling", true);

        UIManager.put("TabbedPane.selectedForeground", FLORESTA);
        UIManager.put("TabbedPane.underlineColor", AGRICOLA);
        UIManager.put("TabbedPane.inactiveUnderlineColor", AGRICOLA);
        UIManager.put("TabbedPane.hoverColor", VERDE_HOVER);
        UIManager.put("TabbedPane.tabHeight", 36);
        UIManager.put("TabbedPane.contentAreaColor", Color.WHITE);
        UIManager.put("TabbedPane.background", Color.WHITE);

        UIManager.put("SplitPane.background", FUNDO);
        UIManager.put("SplitPaneDivider.gripColor", BORDA_FORTE);
        UIManager.put("SplitPane.dividerSize", 12);

        UIManager.put("ToolTip.background", new Color(0x26332B));
        UIManager.put("ToolTip.foreground", Color.WHITE);
        UIManager.put("ToolTip.border", BorderFactory.createEmptyBorder(6, 9, 6, 9));

        UIManager.put("MenuBar.background", Color.WHITE);
        UIManager.put("MenuBar.borderColor", BORDA);
        UIManager.put("MenuItem.selectionBackground", VERDE_TINTA);
        UIManager.put("MenuItem.selectionForeground", TEXTO);
        UIManager.put("Menu.selectionBackground", VERDE_TINTA);
        UIManager.put("Menu.selectionForeground", TEXTO);
        UIManager.put("PopupMenu.borderCornerRadius", 10);
        UIManager.put("TitlePane.unifiedBackground", false);
        UIManager.put("TitlePane.background", Color.WHITE);
        UIManager.put("TitlePane.inactiveBackground", Color.WHITE);
        UIManager.put("TitlePane.menuBarEmbedded", true);

        UIManager.put("OptionPane.background", Color.WHITE);
        UIManager.put("OptionPane.messageFont", new FontUIResource(NORMAL));
        UIManager.put("OptionPane.buttonFont", new FontUIResource(MEDIA));
        UIManager.put("OptionPane.minimumSize", new Dimension(340, 100));
        UIManager.put("OptionPane.informationIcon", Icone.de(Icone.Tipo.CHECK, 30, AGRICOLA));
        UIManager.put("OptionPane.warningIcon", Icone.de(Icone.Tipo.ALERTA, 30, new Color(0xC27803)));
        UIManager.put("OptionPane.errorIcon", Icone.de(Icone.Tipo.ALERTA, 30, PERIGO));
        UIManager.put("OptionPane.questionIcon", Icone.de(Icone.Tipo.INFO, 30, AGRICOLA));
        UIManager.put("RootPane.background", Color.WHITE);
        FlatLaf.updateUI();
    }

    private static String hex(Color c) {
        return String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
    }

    // ---------------------------------------------------------------- imagens da marca

    /** Imagem da marca em {@code /ui/marca/<nome>} (ou {@code null} se ausente). */
    public static Image imagem(String nome) {
        return IMAGENS.computeIfAbsent(nome, n -> {
            URL url = Estilo.class.getResource("/ui/marca/" + n);
            return url == null ? null : new ImageIcon(url).getImage();
        });
    }

    /** Ícones da janela (barra de tarefas e título) em vários tamanhos. */
    public static List<Image> iconesJanela() {
        return java.util.stream.Stream.of(16, 24, 32, 48, 64, 128, 256)
                .map(s -> imagem("icone-" + s + ".png"))
                .filter(java.util.Objects::nonNull).toList();
    }

    // ---------------------------------------------------------------- risco

    public static Color corDoNivel(NivelRisco nivel) {
        return switch (nivel) {
            case BAIXO -> RISCO_BAIXO;
            case MEDIO -> RISCO_MEDIO;
            case ALTO -> RISCO_ALTO;
            case NAO_CALCULADO -> RISCO_NAO_CALCULADO;
        };
    }

    /** Fundo suave (tinta) do nível, usado em selos e cartões. */
    public static Color tintaDoNivel(NivelRisco nivel) {
        return switch (nivel) {
            case BAIXO -> new Color(0xE6F4EB);
            case MEDIO -> new Color(0xFEF3E0);
            case ALTO -> new Color(0xFDECEC);
            case NAO_CALCULADO -> new Color(0xEEF1EE);
        };
    }

    /** Cor de texto com bom contraste sobre a tinta do nível. */
    public static Color textoDoNivel(NivelRisco nivel) {
        return switch (nivel) {
            case BAIXO -> new Color(0x1C6A3C);
            case MEDIO -> new Color(0x8A5200);
            case ALTO -> new Color(0xA92424);
            case NAO_CALCULADO -> new Color(0x4F5A52);
        };
    }

    /** Ícone do nível, para que a classificação não dependa só da cor. */
    public static Icone.Tipo iconeDoNivel(NivelRisco nivel) {
        return switch (nivel) {
            case ALTO -> Icone.Tipo.ALERTA;
            case MEDIO -> Icone.Tipo.INFO;
            case BAIXO -> Icone.Tipo.CHECK;
            case NAO_CALCULADO -> Icone.Tipo.CIRCULO;
        };
    }

    public static String textoNivel(NivelRisco nivel) {
        return nivel == NivelRisco.NAO_CALCULADO ? "NÃO CALCULADO" : nivel.getDescricao().toUpperCase(Locale.ROOT);
    }

    // ---------------------------------------------------------------- formatação

    public static String formatar(LocalDateTime dt) {
        return dt == null ? "—" : DATA_HORA.format(dt);
    }

    public static String formatar(LocalDate d) {
        return d == null ? "—" : DATA.format(d);
    }

    public static String numero(double v) {
        if (v == Math.rint(v)) {
            return String.valueOf((long) v);
        }
        return String.format(Locale.forLanguageTag("pt-BR"), "%.1f", v);
    }

    // ---------------------------------------------------------------- textos

    public static JLabel titulo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(TITULO);
        l.setForeground(TEXTO);
        return l;
    }

    public static JLabel nota(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(PEQUENA);
        l.setForeground(TEXTO_SUAVE);
        return l;
    }

    /** Rótulo de campo de formulário; obrigatórios recebem um asterisco destacado. */
    public static JLabel rotuloCampo(String texto, boolean obrigatorio) {
        JLabel l = new JLabel(obrigatorio
                ? "<html>" + texto + " <span style='color:#C0392B'>*</span></html>" : texto);
        l.setFont(ROTULO);
        l.setForeground(TEXTO);
        return l;
    }

    /** Texto de apoio de um campo (exemplos, limites). */
    public static JLabel dica(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(fonte("Regular", 11.5f));
        l.setForeground(TEXTO_FRACO);
        return l;
    }

    /** Indicador do modo do formulário ("Novo cadastro" / "Editando ..."), atualizado via setText. */
    public static JLabel indicadorModo(String texto) {
        JLabel l = selo(new Pilula(texto, INFO_TINTA, INFO, Icone.Tipo.INFO));
        l.setHorizontalAlignment(SwingConstants.LEFT);
        return l;
    }

    /** Renderizador com ícone fixo antes do texto (ex.: município com marcador de local). */
    public static DefaultTableCellRenderer renderizadorComIcone(Icone.Tipo tipo) {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setIcon(Icone.de(tipo, 15, TEXTO_FRACO));
                setIconTextGap(8);
                return this;
            }
        };
    }

    /** Renderizador numérico em negrito e centralizado (contagens). */
    public static DefaultTableCellRenderer renderizadorContagem() {
        DefaultTableCellRenderer r = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setFont(NEGRITO);
                return this;
            }
        };
        r.setHorizontalAlignment(SwingConstants.CENTER);
        return r;
    }

    /** Bloco de formulário: rótulo (com * se obrigatório) acima do campo. */
    public static JPanel blocoCampo(String rotulo, boolean obrigatorio, JComponent campo) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setOpaque(false);
        p.add(rotuloCampo(rotulo, obrigatorio), BorderLayout.NORTH);
        p.add(campo, BorderLayout.CENTER);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, p.getPreferredSize().height));
        return p;
    }

    /** Coluna vertical de formulário (BoxLayout) já transparente. */
    public static JPanel coluna() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new javax.swing.BoxLayout(p, javax.swing.BoxLayout.Y_AXIS));
        return p;
    }

    /** Adiciona um componente à coluna com espaçamento abaixo. */
    public static void empilhar(JPanel coluna, JComponent c, int espacoDepois) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (c instanceof JButton) {
            c.setMaximumSize(new Dimension(Integer.MAX_VALUE, c.getPreferredSize().height));
        }
        coluna.add(c);
        if (espacoDepois > 0) {
            coluna.add(javax.swing.Box.createVerticalStrut(espacoDepois));
        }
    }

    /** Placeholder (texto de exemplo) em um campo de texto. */
    public static <T extends JTextField> T exemplo(T campo, String texto) {
        campo.putClientProperty("JTextField.placeholderText", texto);
        return campo;
    }

    /** Mantida por compatibilidade: as seções agora são {@link Cartao}. */
    public static Border secao(String titulo) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(BORDA), titulo,
                        0, 0, SUBTITULO, FLORESTA),
                BorderFactory.createEmptyBorder(6, 8, 8, 8));
    }

    // ---------------------------------------------------------------- estrutura das páginas

    /** Configura uma tela (seção) com fundo e margens padronizados. */
    public static void pagina(JPanel p) {
        p.setLayout(new BorderLayout(0, 18));
        p.setBackground(FUNDO);
        p.setBorder(BorderFactory.createEmptyBorder(22, 28, 18, 28));
    }

    /** Cabeçalho de página: título, subtítulo explicativo e ações à direita. */
    public static JPanel cabecalho(String titulo, String subtitulo, JComponent... acoes) {
        JPanel cab = new JPanel(new BorderLayout(16, 0));
        cab.setOpaque(false);
        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new javax.swing.BoxLayout(textos, javax.swing.BoxLayout.Y_AXIS));
        JLabel t = new JLabel(titulo);
        t.setFont(TITULO_PAGINA);
        t.setForeground(TEXTO);
        textos.add(t);
        if (subtitulo != null) {
            textos.add(javax.swing.Box.createVerticalStrut(4));
            JLabel s = new JLabel(subtitulo);
            s.setFont(NORMAL);
            s.setForeground(TEXTO_SUAVE);
            textos.add(s);
        }
        cab.add(textos, BorderLayout.CENTER);
        if (acoes.length > 0) {
            cab.add(linha(acoes), BorderLayout.EAST);
        }
        return cab;
    }

    /** Linha horizontal de componentes alinhados ao centro, com espaçamento padrão. */
    public static JPanel linha(JComponent... itens) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        p.setOpaque(false);
        for (JComponent c : itens) {
            p.add(c);
        }
        return p;
    }

    /** Faixa de aviso (informação ou atenção) com ícone e fundo suave. */
    public static JPanel faixaAviso(String texto, boolean atencao) {
        Color fundo = atencao ? ATENCAO_TINTA : INFO_TINTA;
        Color cor = atencao ? ATENCAO_TEXTO : INFO;
        JPanel p = new JPanel(new BorderLayout(10, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(fundo);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(new Color(cor.getRed(), cor.getGreen(), cor.getBlue(), 45));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(9, 12, 9, 12));
        JLabel icone = new JLabel(Icone.de(atencao ? Icone.Tipo.ALERTA : Icone.Tipo.INFO, 17, cor));
        icone.setVerticalAlignment(SwingConstants.TOP);
        p.add(icone, BorderLayout.WEST);
        JLabel t = new JLabel("<html>" + texto + "</html>");
        t.setFont(PEQUENA);
        t.setForeground(cor.darker());
        p.add(t, BorderLayout.CENTER);
        return p;
    }

    // ---------------------------------------------------------------- botões

    /** Ação principal da tela (verde agrícola). */
    public static JButton botaoPrimario(String texto) {
        return botaoPrimario(texto, null);
    }

    public static JButton botaoPrimario(String texto, Icone.Tipo icone) {
        JButton b = base(texto, icone, Color.WHITE);
        b.putClientProperty("FlatLaf.style", "background:" + hex(AGRICOLA) + "; foreground:#FFFFFF;"
                + " hoverBackground:" + hex(AGRICOLA_HOVER) + "; pressedBackground:" + hex(AGRICOLA_PRESSIONADO) + ";"
                + " borderWidth:0; focusedBorderColor:" + hex(FLORESTA) + "; disabledBackground:#A9C2B1;"
                + " disabledText:#F2F6F2");
        b.setFont(NEGRITO);
        return b;
    }

    /** Ação secundária (contorno neutro). */
    public static JButton botao(String texto) {
        return botao(texto, null);
    }

    public static JButton botao(String texto, Icone.Tipo icone) {
        JButton b = base(texto, icone, TEXTO_SUAVE);
        b.putClientProperty("FlatLaf.style", "background:#FFFFFF; foreground:" + hex(TEXTO) + ";"
                + " borderColor:" + hex(BORDA_FORTE) + "; hoverBackground:" + hex(VERDE_HOVER) + ";"
                + " pressedBackground:" + hex(VERDE_TINTA) + "; hoverBorderColor:#A9B7A8");
        return b;
    }

    /** Ação destrutiva (exclusões): texto vermelho e fundo suave ao passar o mouse. */
    public static JButton botaoPerigo(String texto) {
        JButton b = base(texto, Icone.Tipo.LIXEIRA, PERIGO);
        b.putClientProperty("FlatLaf.style", "background:#FFFFFF; foreground:" + hex(PERIGO) + ";"
                + " borderColor:#F0C9C5; hoverBackground:" + hex(PERIGO_TINTA) + "; pressedBackground:#F9DCD9;"
                + " hoverBorderColor:#E4A39C");
        return b;
    }

    private static JButton base(String texto, Icone.Tipo icone, Color corIcone) {
        JButton b = new JButton(texto);
        b.setFont(MEDIA);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (icone != null) {
            b.setIcon(Icone.de(icone, 16, corIcone));
        }
        return b;
    }

    public static JPanel painel() {
        JPanel p = new JPanel();
        p.setBackground(Color.WHITE);
        return p;
    }

    // ---------------------------------------------------------------- tabelas

    /** Tabela somente leitura, seleção de uma linha, linhas confortáveis e dicas para textos longos. */
    public static JTable tabela(DefaultTableModel modelo) {
        JTable t = new JTable(modelo) {
            @Override
            public String getToolTipText(java.awt.event.MouseEvent e) {
                int linha = rowAtPoint(e.getPoint());
                int coluna = columnAtPoint(e.getPoint());
                if (linha < 0 || coluna < 0) {
                    return null;
                }
                Object v = getValueAt(linha, coluna);
                String texto = v == null ? "" : v.toString();
                return texto.length() > 30 ? "<html><body style='width:420px'>" + texto + "</body></html>" : null;
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getRowCount() == 0) {
                    // estado vazio: mensagem discreta no centro da área visível
                    java.awt.Rectangle v = getVisibleRect();
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    int cy = v.y + Math.max(40, v.height / 2 - 20);
                    Icone.de(Icone.Tipo.DADOS, 26, BORDA_FORTE).paintIcon(this, g2, v.x + v.width / 2 - 13, cy - 30);
                    g2.setFont(PEQUENA);
                    g2.setColor(TEXTO_FRACO);
                    String msg = "Nenhum registro para exibir";
                    int w = g2.getFontMetrics().stringWidth(msg);
                    g2.drawString(msg, v.x + (v.width - w) / 2, cy + 16);
                    g2.dispose();
                }
            }
        };
        t.setToolTipText("");
        t.setFont(NORMAL);
        t.setRowHeight(36);
        t.getTableHeader().setFont(ROTULO);
        t.getTableHeader().setReorderingAllowed(false);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setFillsViewportHeight(true);
        t.setAutoCreateRowSorter(true);
        t.setShowVerticalLines(false);
        t.setIntercellSpacing(new Dimension(0, 1));
        return t;
    }

    public static DefaultTableModel modelo(String... colunas) {
        return new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    /** Área de rolagem com borda discreta (usada dentro dos cartões). */
    public static JScrollPane rolagem(JComponent c) {
        JScrollPane s = new JScrollPane(c);
        s.setBorder(new BordaArredondada(BORDA, 10));
        s.getViewport().setBackground(Color.WHITE);
        s.putClientProperty("JScrollPane.smoothScrolling", true);
        return s;
    }

    /** Área de rolagem sem borda (conteúdo que já está dentro de um cartão). */
    public static JScrollPane rolagemLimpa(JComponent c) {
        JScrollPane s = new JScrollPane(c);
        s.setBorder(BorderFactory.createEmptyBorder());
        s.setOpaque(false);
        s.getViewport().setOpaque(false);
        s.getVerticalScrollBar().setUnitIncrement(18);
        return s;
    }

    /**
     * Envolve o conteúdo de uma página numa rolagem vertical que só aparece quando a janela
     * é menor que o conteúdo; caso contrário o conteúdo ocupa toda a área disponível.
     */
    public static JScrollPane rolavel(JComponent conteudo) {
        JScrollPane s = rolagemLimpa(new PainelRolavel(conteudo));
        s.getVerticalScrollBar().setUnitIncrement(24);
        s.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        return s;
    }

    private static final class PainelRolavel extends JPanel implements javax.swing.Scrollable {
        PainelRolavel(JComponent c) {
            super(new BorderLayout());
            setOpaque(false);
            add(c, BorderLayout.CENTER);
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(java.awt.Rectangle r, int orientacao, int direcao) {
            return 24;
        }

        @Override
        public int getScrollableBlockIncrement(java.awt.Rectangle r, int orientacao, int direcao) {
            return Math.max(24, r.height - 48);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return getParent() instanceof javax.swing.JViewport v && v.getHeight() >= getMinimumSize().height;
        }

        @Override
        public Dimension getMinimumSize() {
            Dimension d = super.getPreferredSize();
            return new Dimension(0, d.height);
        }
    }

    /** Linha "rótulo / valor" empilhada, com ícone, para fichas de informação. */
    public static JPanel informacao(Icone.Tipo icone, String rotulo, JLabel valor) {
        JPanel p = new JPanel(new BorderLayout(12, 0));
        p.setOpaque(false);
        JLabel ic = new JLabel(Icone.de(icone, 17, AGRICOLA)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(VERDE_TINTA);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        ic.setHorizontalAlignment(SwingConstants.CENTER);
        ic.setPreferredSize(new Dimension(32, 32));
        JPanel icTopo = new JPanel(new BorderLayout());
        icTopo.setOpaque(false);
        icTopo.add(ic, BorderLayout.NORTH);
        p.add(icTopo, BorderLayout.WEST);
        JPanel textos = new JPanel(new java.awt.GridLayout(2, 1, 0, 0));
        textos.setOpaque(false);
        JLabel r = new JLabel(rotulo);
        r.setFont(fonte("Regular", 11.5f));
        r.setForeground(TEXTO_FRACO);
        valor.setFont(MEDIA);
        valor.setForeground(TEXTO);
        textos.add(r);
        textos.add(valor);
        p.add(textos, BorderLayout.CENTER);
        return p;
    }

    /** Selo arredondado (pílula) com ícone e texto, usado nos renderizadores e cartões. */
    public record Pilula(String texto, Color fundo, Color cor, Icone.Tipo icone) {
    }

    public static Pilula pilulaDoNivel(NivelRisco n) {
        return new Pilula(textoNivel(n), tintaDoNivel(n), textoDoNivel(n), iconeDoNivel(n));
    }

    /** Componente de selo, pronto para ser usado fora de tabelas. */
    public static JLabel selo(Pilula p) {
        SeloLabel l = new SeloLabel();
        l.aplicar(p, false);
        return l;
    }

    /** Atualiza um selo criado por {@link #selo(Pilula)}. */
    public static void atualizarSelo(JLabel selo, Pilula p) {
        if (selo instanceof SeloLabel s) {
            s.aplicar(p, false);
            s.revalidate();
            s.repaint();
        }
    }

    /** Renderizador de classificação de risco como selo colorido com ícone (valor = NivelRisco). */
    public static DefaultTableCellRenderer renderizadorNivel() {
        return renderizadorPilula(v -> v instanceof NivelRisco n ? pilulaDoNivel(n) : null);
    }

    /** Renderizador genérico que transforma o valor da célula em selo. */
    public static DefaultTableCellRenderer renderizadorPilula(Function<Object, Pilula> conversor) {
        return new DefaultTableCellRenderer() {
            private final SeloLabel selo = new SeloLabel();

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Pilula p = conversor.apply(value);
                if (p == null) {
                    return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                }
                selo.aplicar(p, true);
                selo.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                return selo;
            }
        };
    }

    /** Rótulo que pinta uma pílula arredondada atrás do texto. */
    static final class SeloLabel extends JLabel {
        private Pilula pilula;
        private boolean celula;

        SeloLabel() {
            setFont(MINUSCULA);
            setOpaque(false);
            setIconTextGap(5);
        }

        void aplicar(Pilula p, boolean emCelula) {
            this.pilula = p;
            this.celula = emCelula;
            setOpaque(emCelula);
            setText(p.texto());
            setForeground(p.cor());
            setIcon(p.icone() == null ? null : Icone.de(p.icone(), 12, p.cor()));
            setHorizontalAlignment(emCelula ? SwingConstants.LEFT : SwingConstants.CENTER);
            setBorder(BorderFactory.createEmptyBorder(0, emCelula ? 20 : 10, 0, 10));
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            return new Dimension(d.width + (celula ? 8 : 0), Math.max(d.height, 24));
        }

        @Override
        public Dimension getMaximumSize() {
            return getPreferredSize();
        }

        @Override
        public Dimension getMinimumSize() {
            return getPreferredSize();
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (isOpaque()) {
                g.setColor(getBackground());
                g.fillRect(0, 0, getWidth(), getHeight());
            }
            if (pilula != null) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                java.awt.FontMetrics fm = g2.getFontMetrics(getFont());
                int textoW = fm.stringWidth(getText()) + (getIcon() != null ? getIcon().getIconWidth() + getIconTextGap() : 0);
                int w = textoW + 20;
                int h = 24;
                int x = celula ? 10 : (getWidth() - w) / 2;
                int y = (getHeight() - h) / 2;
                g2.setColor(pilula.fundo());
                g2.fillRoundRect(x, y, w, h, h, h);
                g2.dispose();
            }
            super.paintComponent(g);
        }
    }

    /** Borda de 1 px com cantos arredondados. */
    public static final class BordaArredondada implements Border {
        private final Color cor;
        private final int arco;

        public BordaArredondada(Color cor, int arco) {
            this.cor = cor;
            this.arco = arco;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(cor);
            g2.drawRoundRect(x, y, width - 1, height - 1, arco, arco);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(1, 1, 1, 1);
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }
    }

    public static GridBagConstraints gbc(int x, int y) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = x;
        c.gridy = y;
        c.insets = new Insets(4, 0, 4, 0);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        return c;
    }

    // ---------------------------------------------------------------- mensagens

    /** Executa uma ação de cadastro mostrando erros de validação de forma amigável. */
    public static boolean executar(Component pai, Runnable acao) {
        try {
            acao.run();
            return true;
        } catch (ValidacaoException e) {
            JOptionPane.showMessageDialog(pai, e.getMessage(), "Dados inválidos", JOptionPane.WARNING_MESSAGE);
        } catch (PersistenciaException e) {
            JOptionPane.showMessageDialog(pai, e.getMessage(), "Erro ao gravar dados", JOptionPane.ERROR_MESSAGE);
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(pai, "Erro inesperado: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }

    public static void sucesso(Component pai, String mensagem) {
        JOptionPane.showMessageDialog(pai, mensagem, "SMField", JOptionPane.INFORMATION_MESSAGE);
    }

    // ---------------------------------------------------------------- seleção de talhões

    /** Item de combo para selecionar um talhão mostrando a propriedade. */
    public record OpcaoTalhao(long id, String rotulo) {
        @Override
        public String toString() {
            return rotulo;
        }
    }

    /** Recarrega um combo de talhões preservando a seleção atual quando possível. */
    public static void carregarTalhoes(JComboBox<OpcaoTalhao> combo, ServicosSMField s) {
        Object selecionado = combo.getSelectedItem();
        long idAnterior = selecionado instanceof OpcaoTalhao o ? o.id() : -1;
        combo.removeAllItems();
        List<Talhao> lista = s.talhoes().listar();
        for (Talhao t : lista) {
            String prop = s.propriedades().buscar(t.getPropriedadeId()).map(p -> p.getNome()).orElse("?");
            OpcaoTalhao op = new OpcaoTalhao(t.getId(), prop + "  ›  " + t.getNome() + " (" + t.getCultura() + ")");
            combo.addItem(op);
            if (t.getId() == idAnterior) {
                combo.setSelectedItem(op);
            }
        }
    }

    public static void selecionarTalhao(JComboBox<OpcaoTalhao> combo, long talhaoId) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (combo.getItemAt(i).id() == talhaoId) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    public static long idTalhao(JComboBox<OpcaoTalhao> combo) {
        Object o = combo.getSelectedItem();
        return o instanceof OpcaoTalhao op ? op.id() : -1;
    }

    /** Ícone simples para usar em rótulos. */
    public static Icon icone(Icone.Tipo tipo, int tamanho, Color cor) {
        return Icone.de(tipo, tamanho, cor);
    }
}
