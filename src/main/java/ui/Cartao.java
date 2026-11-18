package ui;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;

/**
 * Cartão: superfície branca com cantos arredondados, sombra discreta e cabeçalho opcional
 * (ícone, título, subtítulo e ações). O conteúdo vai no {@link #corpo()}.
 */
public class Cartao extends JPanel {

    static final int ARCO = 16;
    static final int SOMBRA = 4;

    private final JPanel corpo;
    private final JPanel acoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
    private final JLabel rotuloTitulo = new JLabel();
    private final JLabel rotuloSubtitulo = new JLabel();

    public Cartao() {
        this(null, null, null);
    }

    public Cartao(String titulo) {
        this(titulo, null, null);
    }

    public Cartao(String titulo, String subtitulo) {
        this(titulo, subtitulo, null);
    }

    public Cartao(String titulo, String subtitulo, Icone.Tipo icone) {
        super(new BorderLayout(0, 12));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(16 + 1, 18 + 1, 16 + SOMBRA, 18 + 2));
        corpo = new JPanel(new BorderLayout(0, 8));
        corpo.setOpaque(false);
        acoes.setOpaque(false);
        if (titulo != null) {
            JPanel cab = new JPanel(new BorderLayout(10, 0));
            cab.setOpaque(false);
            JPanel textos = new JPanel();
            textos.setOpaque(false);
            textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
            rotuloTitulo.setText(titulo);
            rotuloTitulo.setFont(Estilo.SUBTITULO);
            rotuloTitulo.setForeground(Estilo.TEXTO);
            if (icone != null) {
                rotuloTitulo.setIcon(Icone.de(icone, 18, Estilo.AGRICOLA));
                rotuloTitulo.setIconTextGap(9);
            }
            textos.add(rotuloTitulo);
            if (subtitulo != null) {
                textos.add(Box.createVerticalStrut(3));
                rotuloSubtitulo.setText(subtitulo);
                rotuloSubtitulo.setFont(Estilo.PEQUENA);
                rotuloSubtitulo.setForeground(Estilo.TEXTO_SUAVE);
                if (icone != null) {
                    rotuloSubtitulo.setBorder(BorderFactory.createEmptyBorder(0, 27, 0, 0));
                }
                textos.add(rotuloSubtitulo);
            }
            cab.add(textos, BorderLayout.CENTER);
            cab.add(acoes, BorderLayout.EAST);
            super.add(cab, BorderLayout.NORTH);
        }
        super.add(corpo, BorderLayout.CENTER);
    }

    /** Painel interno onde o conteúdo do cartão é montado (BorderLayout por padrão). */
    public JPanel corpo() {
        return corpo;
    }

    /** Troca o layout do corpo. */
    public Cartao comLayout(LayoutManager layout) {
        corpo.setLayout(layout);
        return this;
    }

    /** Adiciona componentes à direita do título. */
    public Cartao acoes(JComponent... itens) {
        for (JComponent c : itens) {
            acoes.add(c);
        }
        return this;
    }

    public void setTitulo(String titulo) {
        rotuloTitulo.setText(titulo);
    }

    public void setSubtitulo(String subtitulo) {
        rotuloSubtitulo.setText(subtitulo);
    }

    /** Conteúdo sem margem interna (ex.: tabela encostada nas bordas). */
    public Cartao semMargem() {
        setBorder(BorderFactory.createEmptyBorder(1, 1, 1 + SOMBRA, 2));
        return this;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth() - 2;
        int h = getHeight() - SOMBRA;
        // sombra suave em camadas
        for (int i = SOMBRA; i >= 1; i--) {
            g2.setColor(new Color(24, 48, 30, 7 + (SOMBRA - i) * 3));
            g2.fillRoundRect(1, i, w, h, ARCO + i, ARCO + i);
        }
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(0, 0, w, h, ARCO, ARCO);
        g2.setColor(Estilo.BORDA);
        g2.drawRoundRect(0, 0, w - 1, h - 1, ARCO, ARCO);
        g2.dispose();
    }
}
