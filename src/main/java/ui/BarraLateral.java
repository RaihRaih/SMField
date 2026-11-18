package ui;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

/**
 * Menu lateral do SMField: logo, seções agrupadas com ícones, destaque da seção ativa,
 * recolher/expandir animado e rodapé discreto. Apenas navegação — cada item seleciona a
 * tela correspondente da janela principal.
 */
public class BarraLateral extends JPanel {

    static final int LARGURA_EXPANDIDA = 268;
    static final int LARGURA_RECOLHIDA = 76;

    private final List<Item> itens = new ArrayList<>();
    private final JPanel lista = new JPanel();
    private final IntConsumer aoSelecionar;
    private final Logo logo = new Logo();
    private final JLabel rodape = new JLabel();
    private final Item botaoRecolher;
    private final List<JLabel> grupos = new ArrayList<>();
    private boolean recolhida = false;
    private int largura = LARGURA_EXPANDIDA;
    private Timer animacao;

    public BarraLateral(IntConsumer aoSelecionar) {
        super(new BorderLayout());
        this.aoSelecionar = aoSelecionar;
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Estilo.BORDA));

        JPanel topo = new JPanel(new BorderLayout());
        topo.setOpaque(false);
        topo.setBorder(BorderFactory.createEmptyBorder(22, 18, 14, 14));
        topo.add(logo, BorderLayout.CENTER);
        add(topo, BorderLayout.NORTH);

        lista.setOpaque(false);
        lista.setLayout(new BoxLayout(lista, BoxLayout.Y_AXIS));
        lista.setBorder(BorderFactory.createEmptyBorder(4, 12, 8, 12));
        add(lista, BorderLayout.CENTER);

        JPanel base = new JPanel();
        base.setOpaque(false);
        base.setLayout(new BoxLayout(base, BoxLayout.Y_AXIS));
        base.setBorder(BorderFactory.createEmptyBorder(8, 12, 14, 12));
        botaoRecolher = new Item(-1, "Recolher menu", Icone.Tipo.RECOLHER, null);
        botaoRecolher.setToolTipText("Recolher ou expandir o menu lateral");
        base.add(botaoRecolher);
        base.add(Box.createVerticalStrut(8));
        rodape.setFont(Estilo.fonte("Regular", 11f));
        rodape.setForeground(Estilo.TEXTO_FRACO);
        rodape.setBorder(BorderFactory.createEmptyBorder(8, 10, 0, 6));
        rodape.setAlignmentX(Component.LEFT_ALIGNMENT);
        base.add(rodape);
        add(base, BorderLayout.SOUTH);
        setRodape("Projeto acadêmico · Sprint 2");
    }

    /** Inicia um grupo de seções (rótulo em caixa alta). */
    public void grupo(String nome) {
        JLabel g = new JLabel(nome.toUpperCase(java.util.Locale.ROOT));
        g.setName(g.getText());
        g.setFont(Estilo.espacada(Estilo.fonte("SemiBold", 10.5f), 0.08f));
        g.setForeground(Estilo.TEXTO_FRACO);
        g.setBorder(BorderFactory.createEmptyBorder(lista.getComponentCount() == 0 ? 6 : 16, 12, 6, 0));
        g.setAlignmentX(Component.LEFT_ALIGNMENT);
        g.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        grupos.add(g);
        lista.add(g);
    }

    /** Adiciona um item de navegação para a tela de índice {@code indice}. */
    public void item(int indice, String rotulo, Icone.Tipo icone, String atalho) {
        Item it = new Item(indice, rotulo, icone, atalho);
        itens.add(it);
        lista.add(it);
        lista.add(Box.createVerticalStrut(2));
    }

    /** Marca visualmente a seção ativa. */
    public void marcar(int indice) {
        for (Item it : itens) {
            it.ativo = it.indice == indice;
            it.repaint();
        }
    }

    public void setRodape(String texto) {
        rodape.setText("<html>" + texto + "</html>");
    }

    public boolean isRecolhida() {
        return recolhida;
    }

    /** Recolhe ou expande o menu com uma animação curta. */
    public void alternar() {
        recolhida = !recolhida;
        botaoRecolher.rotulo = recolhida ? "Expandir menu" : "Recolher menu";
        botaoRecolher.icone = recolhida ? Icone.Tipo.EXPANDIR : Icone.Tipo.RECOLHER;
        for (JLabel g : grupos) {
            g.setText(recolhida ? " " : g.getName());
        }
        rodape.setVisible(!recolhida);
        int alvo = recolhida ? LARGURA_RECOLHIDA : LARGURA_EXPANDIDA;
        if (animacao != null) {
            animacao.stop();
        }
        int inicio = largura;
        long t0 = System.currentTimeMillis();
        animacao = new Timer(12, e -> {
            double p = Math.min(1, (System.currentTimeMillis() - t0) / 170.0);
            double suave = 1 - Math.pow(1 - p, 3);
            largura = (int) Math.round(inicio + (alvo - inicio) * suave);
            revalidate();
            repaint();
            if (p >= 1) {
                ((Timer) e.getSource()).stop();
            }
        });
        animacao.start();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(largura, super.getPreferredSize().height);
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(largura, 0);
    }

    // ------------------------------------------------------------------ logo

    /** Logo oficial (arquivo original) e slogan; recolhida, mostra apenas o símbolo da folha. */
    private final class Logo extends JComponent {
        private final Image marca = Estilo.imagem("logo-marca.png");
        private final Image folha = Estilo.imagem("folha.png");

        Logo() {
            setPreferredSize(new Dimension(200, 74));
            setToolTipText("SMField — Conheça sua terra. Cultive o futuro.");
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Graficos.preparar(g);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            if (largura < 170 || marca == null) {
                if (folha != null) {
                    int h = 34;
                    int w = folha.getWidth(null) * h / folha.getHeight(null);
                    g2.drawImage(folha, Math.max(0, (getWidth() - w) / 2 - 4), 6, w, h, null);
                }
            } else {
                // proporção da logo original: o slogan é ~9% mais largo que a palavra SMField
                int larguraSlogan = Math.min(getWidth() - 6, 214);
                int w = (int) Math.round(larguraSlogan / 1.09);
                int h = marca.getHeight(null) * w / marca.getWidth(null);
                int xMarca = 2 + (larguraSlogan - w) / 2;
                g2.drawImage(marca, xMarca, 2, w, h, null);
                String slogan = "CONHEÇA SUA TERRA. CULTIVE O FUTURO.";
                float espaco = 0.2f;
                java.awt.Font f = Estilo.espacada(Estilo.fonte("Medium", 8.4f), espaco);
                while (espaco > 0 && g2.getFontMetrics(f).stringWidth(slogan) > larguraSlogan) {
                    espaco -= 0.01f;
                    f = Estilo.espacada(Estilo.fonte("Medium", 8.4f), espaco);
                }
                g2.setFont(f);
                g2.setColor(Estilo.FLORESTA);
                g2.drawString(slogan, 2, h + 18);
            }
            g2.dispose();
        }
    }

    // ------------------------------------------------------------------ item

    private final class Item extends JComponent {
        final int indice;
        String rotulo;
        Icone.Tipo icone;
        final String atalho;
        boolean ativo;
        boolean hover;

        Item(int indice, String rotulo, Icone.Tipo icone, String atalho) {
            this.indice = indice;
            this.rotulo = rotulo;
            this.icone = icone;
            this.atalho = atalho;
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
            setPreferredSize(new Dimension(200, 42));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            // navegação por teclado: menu "Seções" e atalhos Ctrl+1…9 (o item não rouba o foco dos formulários)
            setFocusable(false);
            setName(rotulo);
            setToolTipText(atalho == null ? rotulo : rotulo + " (" + atalho + ")");
            MouseAdapter m = new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    acionar();
                }
            };
            addMouseListener(m);
            addKeyListener(new java.awt.event.KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_SPACE) {
                        acionar();
                    }
                }
            });
            addFocusListener(new java.awt.event.FocusAdapter() {
                @Override
                public void focusGained(java.awt.event.FocusEvent e) {
                    repaint();
                }

                @Override
                public void focusLost(java.awt.event.FocusEvent e) {
                    repaint();
                }
            });
        }

        void acionar() {
            if (indice < 0) {
                alternar();
            } else {
                aoSelecionar.accept(indice);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Graficos.preparar(g);
            int w = getWidth();
            int h = getHeight();
            if (ativo) {
                g2.setColor(Estilo.VERDE_TINTA);
                g2.fillRoundRect(0, 2, w, h - 4, 12, 12);
                g2.setColor(Estilo.AGRICOLA);
                g2.fillRoundRect(0, 11, 4, h - 22, 4, 4);
            } else if (hover) {
                g2.setColor(Estilo.VERDE_HOVER);
                g2.fillRoundRect(0, 2, w, h - 4, 12, 12);
            }
            if (isFocusOwner() && !ativo) {
                g2.setColor(new Color(0x3D, 0x76, 0x50, 110));
                g2.drawRoundRect(1, 3, w - 3, h - 7, 12, 12);
            }
            Color corIcone = ativo ? Estilo.AGRICOLA : Estilo.TEXTO_SUAVE;
            Icone ic = Icone.de(icone, 20, corIcone);
            boolean estreito = w < 120;
            int ix = estreito ? (w - 20) / 2 : 14;
            ic.paintIcon(this, g2, ix, (h - 20) / 2);
            if (!estreito) {
                g2.setFont(ativo ? Estilo.fonte("SemiBold", 13.5f) : Estilo.fonte("Medium", 13.5f));
                g2.setColor(ativo ? Estilo.FLORESTA : Estilo.TEXTO);
                java.awt.FontMetrics fm = g2.getFontMetrics();
                g2.drawString(Graficos.encurtar(fm, rotulo, w - 60), 46, (h + fm.getAscent() - fm.getDescent()) / 2);
            }
            g2.dispose();
        }
    }
}
