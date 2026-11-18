package ui;

import domain.NivelRisco;

import javax.swing.JComponent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Gráficos leves desenhados em Java2D. Eles apenas exibem dados já existentes no sistema
 * (índices calculados, registros e contagens); nenhum valor é estimado ou inventado.
 */
public final class Graficos {

    private Graficos() {
    }

    static Graphics2D preparar(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        return g2;
    }

    static void textoCentral(Graphics2D g, String texto, Font f, Color cor, int cx, int y) {
        g.setFont(f);
        g.setColor(cor);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(texto, cx - fm.stringWidth(texto) / 2, y);
    }

    static String encurtar(FontMetrics fm, String texto, int largura) {
        if (fm.stringWidth(texto) <= largura) {
            return texto;
        }
        String s = texto;
        while (s.length() > 1 && fm.stringWidth(s + "…") > largura) {
            s = s.substring(0, s.length() - 1);
        }
        return s + "…";
    }

    // =================================================================== Medidor (índice 0–100)

    /** Medidor semicircular do índice de risco, com as três faixas de classificação. */
    public static final class Medidor extends JComponent {
        private Integer valor;
        private NivelRisco nivel = NivelRisco.NAO_CALCULADO;

        public Medidor() {
            setPreferredSize(new Dimension(220, 140));
            setOpaque(false);
        }

        public void mostrar(Integer valor, NivelRisco nivel) {
            this.valor = valor;
            this.nivel = nivel;
            setToolTipText(valor == null ? "Índice não calculado"
                    : "Índice de risco " + valor + " de 100, " + Estilo.textoNivel(nivel));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = preparar(g);
            int espessura = 16;
            int diam = Math.min(getWidth() - espessura - 8, (getHeight() - 30) * 2 - espessura);
            int x = (getWidth() - diam) / 2;
            int y = 8 + espessura / 2;
            g2.setStroke(new BasicStroke(espessura, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
            // faixas: Baixo 0–33, Médio 34–66, Alto 67–100 (180° = 100 pontos)
            double[][] faixas = {{0, 33.5}, {33.5, 66.5}, {66.5, 100}};
            NivelRisco[] niveis = {NivelRisco.BAIXO, NivelRisco.MEDIO, NivelRisco.ALTO};
            for (int i = 0; i < 3; i++) {
                Color c = valor == null ? new Color(0xEDF0ED) : Estilo.tintaDoNivel(niveis[i]);
                g2.setColor(c);
                double ini = 180 - faixas[i][0] * 1.8;
                double ext = -(faixas[i][1] - faixas[i][0]) * 1.8;
                g2.draw(new Arc2D.Double(x, y, diam, diam, ini - (i == 0 ? 0 : 0.6), ext + (i == 0 ? -0.6 : 0), Arc2D.OPEN));
            }
            if (valor != null) {
                g2.setColor(Estilo.corDoNivel(nivel));
                g2.setStroke(new BasicStroke(espessura, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                double ext = -Math.max(1, valor) * 1.8;
                g2.draw(new Arc2D.Double(x, y, diam, diam, 180, ext, Arc2D.OPEN));
                // marcador na ponta
                double ang = Math.toRadians(180 - valor * 1.8);
                double cx = x + diam / 2.0 + Math.cos(ang) * diam / 2.0;
                double cy = y + diam / 2.0 - Math.sin(ang) * diam / 2.0;
                g2.setColor(Color.WHITE);
                g2.fill(new Ellipse2D.Double(cx - 5, cy - 5, 10, 10));
                g2.setColor(Estilo.corDoNivel(nivel));
                g2.setStroke(new BasicStroke(2.5f));
                g2.draw(new Ellipse2D.Double(cx - 5, cy - 5, 10, 10));
            }
            int centroX = x + diam / 2;
            int base = y + diam / 2;
            textoCentral(g2, valor == null ? "—" : String.valueOf(valor), Estilo.fonte("SemiBold", 40f),
                    valor == null ? Estilo.TEXTO_FRACO : Estilo.TEXTO, centroX, base - 4);
            textoCentral(g2, "de 100", Estilo.fonte("Medium", 11.5f), Estilo.TEXTO_FRACO, centroX, base + 13);
            g2.setFont(Estilo.fonte("Medium", 10.5f));
            g2.setColor(Estilo.TEXTO_FRACO);
            FontMetrics fmr = g2.getFontMetrics();
            g2.drawString("0", x - fmr.stringWidth("0") / 2, base + espessura / 2 + 14);
            g2.drawString("100", x + diam - fmr.stringWidth("100") / 2, base + espessura / 2 + 14);
            g2.dispose();
        }
    }

    // =================================================================== Rosca (distribuição)

    /** Fatia da rosca. */
    public record Fatia(String rotulo, long valor, Color cor) {
    }

    /** Gráfico de rosca com legenda (contagem e percentual de cada fatia). */
    public static final class Rosca extends JComponent {
        private List<Fatia> fatias = List.of();
        private String legendaCentro = "talhões";

        public Rosca() {
            setPreferredSize(new Dimension(320, 170));
            setOpaque(false);
        }

        public void mostrar(List<Fatia> novas, String legendaCentro) {
            this.fatias = List.copyOf(novas);
            this.legendaCentro = legendaCentro;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = preparar(g);
            long total = fatias.stream().mapToLong(Fatia::valor).sum();
            int diam = Math.min(getHeight() - 10, 132);
            int x = 4;
            int y = (getHeight() - diam) / 2;
            int esp = 18;
            g2.setStroke(new BasicStroke(esp, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
            Arc2D.Double arco = new Arc2D.Double(x + esp / 2.0, y + esp / 2.0, diam - esp, diam - esp, 0, 0, Arc2D.OPEN);
            if (total == 0) {
                g2.setColor(new Color(0xEDF0ED));
                arco.setAngleStart(90);
                arco.setAngleExtent(360);
                g2.draw(arco);
            } else {
                double ini = 90;
                for (Fatia f : fatias) {
                    if (f.valor() == 0) {
                        continue;
                    }
                    double ext = -360.0 * f.valor() / total;
                    g2.setColor(f.cor());
                    arco.setAngleStart(ini - 0.8);
                    arco.setAngleExtent(ext + 1.6 > -0.5 ? ext : ext + 1.6);
                    g2.draw(arco);
                    ini += ext;
                }
            }
            int cx = x + diam / 2;
            int cy = y + diam / 2;
            textoCentral(g2, String.valueOf(total), Estilo.fonte("SemiBold", 28f), Estilo.TEXTO, cx, cy + 6);
            textoCentral(g2, legendaCentro, Estilo.fonte("Medium", 11f), Estilo.TEXTO_FRACO, cx, cy + 22);

            // legenda: nome da fatia e, abaixo, contagem e percentual
            int lx = x + diam + 20;
            int linhaH = 33;
            int ly = cy - (fatias.size() * linhaH) / 2 + 12;
            for (Fatia f : fatias) {
                g2.setColor(f.cor());
                g2.fillRoundRect(lx, ly - 9, 10, 10, 4, 4);
                g2.setFont(Estilo.fonte("Medium", 12.5f));
                g2.setColor(Estilo.TEXTO);
                g2.drawString(encurtar(g2.getFontMetrics(), f.rotulo(), getWidth() - lx - 22), lx + 17, ly);
                String num = f.valor() + (total > 0 ? " · " + Math.round(100.0 * f.valor() / total) + "%" : "");
                g2.setFont(Estilo.fonte("Regular", 11.5f));
                g2.setColor(Estilo.TEXTO_SUAVE);
                g2.drawString(num, lx + 17, ly + 14);
                ly += linhaH;
            }
            g2.dispose();
        }
    }

    // =================================================================== Barras do índice por talhão

    /** Linha do gráfico de barras. */
    public record Barra(String rotulo, String detalhe, Integer indice, NivelRisco nivel) {
    }

    /** Barras horizontais com o último índice de cada talhão e as linhas de corte das faixas. */
    public static final class BarrasIndice extends JComponent {
        private static final int LINHA = 34;
        private List<Barra> barras = List.of();

        public BarrasIndice() {
            setOpaque(false);
        }

        public void mostrar(List<Barra> novas) {
            barras = List.copyOf(novas);
            revalidate();
            repaint();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(260, Math.max(60, barras.size() * LINHA + 26));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = preparar(g);
            if (barras.isEmpty()) {
                textoCentral(g2, "Sem talhões para exibir.", Estilo.PEQUENA, Estilo.TEXTO_FRACO, getWidth() / 2, 30);
                g2.dispose();
                return;
            }
            int esq = 2;
            int dir = getWidth() - 52;
            int larg = dir - esq;
            // linhas de corte 33 / 66
            g2.setFont(Estilo.fonte("Medium", 10f));
            int topo = 4;
            int fundo = barras.size() * LINHA + 2;
            float[] tracejado = {3f, 4f};
            for (int corte : new int[]{0, 33, 66, 100}) {
                int cx = esq + larg * corte / 100;
                g2.setColor(new Color(0xE3E9E2));
                g2.setStroke(corte == 0 || corte == 100 ? new BasicStroke(1f)
                        : new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f, tracejado, 0f));
                g2.drawLine(cx, topo, cx, fundo);
                g2.setColor(Estilo.TEXTO_FRACO);
                String t = String.valueOf(corte);
                g2.drawString(t, cx - g2.getFontMetrics().stringWidth(t) / 2, fundo + 14);
            }
            int y = topo + 2;
            for (Barra b : barras) {
                g2.setFont(Estilo.fonte("Medium", 12f));
                FontMetrics fm = g2.getFontMetrics();
                g2.setColor(Estilo.TEXTO);
                g2.drawString(encurtar(fm, b.rotulo(), larg - 10), esq + 2, y + 12);
                g2.setFont(Estilo.fonte("Regular", 10.5f));
                fm = g2.getFontMetrics();
                g2.setColor(Estilo.TEXTO_FRACO);
                int rotW = g2.getFontMetrics(Estilo.fonte("Medium", 12f)).stringWidth(encurtar(
                        g2.getFontMetrics(Estilo.fonte("Medium", 12f)), b.rotulo(), larg - 10));
                g2.drawString(encurtar(fm, "· " + b.detalhe(), Math.max(0, larg - rotW - 16)), esq + rotW + 8, y + 12);
                int by = y + 17;
                g2.setColor(new Color(0xF0F3EF));
                g2.fillRoundRect(esq, by, larg, 9, 9, 9);
                if (b.indice() != null) {
                    int w = Math.max(10, larg * b.indice() / 100);
                    Color cor = Estilo.corDoNivel(b.nivel());
                    g2.setPaint(new GradientPaint(esq, 0, cor.brighter(), esq + w, 0, cor));
                    g2.fillRoundRect(esq, by, w, 9, 9, 9);
                    g2.setFont(Estilo.fonte("SemiBold", 12.5f));
                    g2.setColor(Estilo.textoDoNivel(b.nivel()));
                    g2.drawString(String.valueOf(b.indice()), dir + 10, by + 10);
                } else {
                    g2.setFont(Estilo.fonte("Medium", 11f));
                    g2.setColor(Estilo.TEXTO_FRACO);
                    g2.drawString("s/ dados", dir + 6, by + 9);
                }
                y += LINHA;
            }
            g2.dispose();
        }
    }

    // =================================================================== Série temporal

    /** Ponto de uma série no tempo. */
    public record Ponto(LocalDateTime quando, double valor, NivelRisco nivel, boolean destaque) {
        public Ponto(LocalDateTime quando, double valor) {
            this(quando, valor, null, false);
        }
    }

    /**
     * Linha do tempo de um valor. No modo risco o eixo vai de 0 a 100 e o fundo mostra as
     * faixas Baixo, Médio e Alto; no modo compacto (minigráfico) não há eixos.
     */
    public static final class SerieTemporal extends JComponent {
        private static final DateTimeFormatter DIA = DateTimeFormatter.ofPattern("dd/MM", Locale.forLanguageTag("pt-BR"));
        private static final DateTimeFormatter DIA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        private List<Ponto> pontos = List.of();
        private final boolean modoRisco;
        private final boolean compacto;
        private Color cor = Estilo.AGRICOLA;
        private String unidade = "";
        private String vazio = "Sem registros para exibir.";
        private int hover = -1;

        public SerieTemporal(boolean modoRisco, boolean compacto) {
            this.modoRisco = modoRisco;
            this.compacto = compacto;
            setOpaque(false);
            setPreferredSize(compacto ? new Dimension(120, 36) : new Dimension(400, 190));
            if (!compacto) {
                MouseAdapter m = new MouseAdapter() {
                    @Override
                    public void mouseMoved(MouseEvent e) {
                        int novo = maisProximo(e.getX());
                        if (novo != hover) {
                            hover = novo;
                            repaint();
                        }
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        hover = -1;
                        repaint();
                    }
                };
                addMouseListener(m);
                addMouseMotionListener(m);
            }
        }

        public SerieTemporal cor(Color c) {
            this.cor = c;
            return this;
        }

        public SerieTemporal unidade(String u) {
            this.unidade = u;
            return this;
        }

        public SerieTemporal vazio(String texto) {
            this.vazio = texto;
            return this;
        }

        public void mostrar(List<Ponto> novos) {
            pontos = new ArrayList<>(novos);
            hover = -1;
            repaint();
        }

        private int margemEsq() {
            return compacto ? 2 : 34;
        }

        private int margemDir() {
            return compacto ? 2 : (modoRisco ? 52 : 14);
        }

        private int margemTopo() {
            return compacto ? 3 : 10;
        }

        private int margemBase() {
            return compacto ? 3 : 24;
        }

        private double xDe(int i) {
            int w = getWidth() - margemEsq() - margemDir();
            return pontos.size() == 1 ? margemEsq() + w / 2.0 : margemEsq() + (double) w * i / (pontos.size() - 1);
        }

        private int maisProximo(int x) {
            if (pontos.isEmpty()) {
                return -1;
            }
            int melhor = 0;
            double dist = Double.MAX_VALUE;
            for (int i = 0; i < pontos.size(); i++) {
                double d = Math.abs(xDe(i) - x);
                if (d < dist) {
                    dist = d;
                    melhor = i;
                }
            }
            return melhor;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = preparar(g);
            int x0 = margemEsq();
            int x1 = getWidth() - margemDir();
            int y0 = margemTopo();
            int y1 = getHeight() - margemBase();
            double min;
            double max;
            if (modoRisco) {
                min = 0;
                max = 100;
            } else {
                min = pontos.stream().mapToDouble(Ponto::valor).min().orElse(0);
                max = pontos.stream().mapToDouble(Ponto::valor).max().orElse(1);
                double folga = Math.max((max - min) * 0.15, compacto ? 0.5 : 1);
                min -= folga;
                max += folga;
            }
            final double fMin = min;
            final double fMax = max;
            java.util.function.DoubleUnaryOperator yDe = v -> y1 - (v - fMin) / (fMax - fMin) * (y1 - y0);

            if (!compacto) {
                g2.setFont(Estilo.fonte("Regular", 10.5f));
                if (modoRisco) {
                    double[][] faixas = {{0, 33}, {33, 66}, {66, 100}};
                    NivelRisco[] niveis = {NivelRisco.BAIXO, NivelRisco.MEDIO, NivelRisco.ALTO};
                    String[] nomes = {"Baixo", "Médio", "Alto"};
                    for (int i = 0; i < 3; i++) {
                        int ya = (int) yDe.applyAsDouble(faixas[i][1]);
                        int yb = (int) yDe.applyAsDouble(faixas[i][0]);
                        Color t = Estilo.tintaDoNivel(niveis[i]);
                        g2.setColor(new Color(t.getRed(), t.getGreen(), t.getBlue(), 150));
                        g2.fillRect(x0, ya, x1 - x0, yb - ya);
                        g2.setColor(Estilo.textoDoNivel(niveis[i]));
                        g2.setFont(Estilo.fonte("Medium", 10.5f));
                        g2.drawString(nomes[i], x1 + 8, (ya + yb) / 2 + 4);
                    }
                }
                g2.setFont(Estilo.fonte("Regular", 10.5f));
                int marcas = 4;
                for (int i = 0; i <= marcas; i++) {
                    double v = min + (max - min) * i / marcas;
                    int yy = (int) yDe.applyAsDouble(v);
                    g2.setColor(new Color(0xE6EBE5));
                    g2.drawLine(x0, yy, x1, yy);
                    g2.setColor(Estilo.TEXTO_FRACO);
                    String t = Estilo.numero(Math.round(v * 10) / 10.0);
                    g2.drawString(t, x0 - 6 - g2.getFontMetrics().stringWidth(t), yy + 4);
                }
            }
            if (pontos.isEmpty()) {
                if (!compacto) {
                    textoCentral(g2, vazio, Estilo.PEQUENA, Estilo.TEXTO_FRACO, (x0 + x1) / 2, (y0 + y1) / 2);
                }
                g2.dispose();
                return;
            }
            Path2D linha = new Path2D.Double();
            Path2D area = new Path2D.Double();
            for (int i = 0; i < pontos.size(); i++) {
                double px = xDe(i);
                double py = yDe.applyAsDouble(pontos.get(i).valor());
                if (i == 0) {
                    linha.moveTo(px, py);
                    area.moveTo(px, y1);
                    area.lineTo(px, py);
                } else {
                    linha.lineTo(px, py);
                    area.lineTo(px, py);
                }
            }
            area.lineTo(xDe(pontos.size() - 1), y1);
            area.closePath();
            Color linhaCor = modoRisco ? Estilo.FLORESTA : cor;
            g2.setPaint(new GradientPaint(0, y0, new Color(linhaCor.getRed(), linhaCor.getGreen(), linhaCor.getBlue(),
                    modoRisco ? 40 : 70), 0, y1, new Color(linhaCor.getRed(), linhaCor.getGreen(), linhaCor.getBlue(), 0)));
            g2.fill(area);
            g2.setColor(linhaCor);
            g2.setStroke(new BasicStroke(compacto ? 1.6f : 2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(linha);

            boolean pontosVisiveis = !compacto && pontos.size() <= 45;
            for (int i = 0; i < pontos.size(); i++) {
                Ponto p = pontos.get(i);
                boolean mostra = pontosVisiveis || p.destaque() || i == pontos.size() - 1 || i == hover;
                if (!mostra) {
                    continue;
                }
                double px = xDe(i);
                double py = yDe.applyAsDouble(p.valor());
                double r = compacto ? 2.5 : (p.destaque() || i == hover ? 4.5 : 3.2);
                Color c = p.nivel() != null ? Estilo.corDoNivel(p.nivel()) : linhaCor;
                g2.setColor(Color.WHITE);
                g2.fill(new Ellipse2D.Double(px - r - 1.5, py - r - 1.5, 2 * r + 3, 2 * r + 3));
                g2.setColor(c);
                g2.fill(new Ellipse2D.Double(px - r, py - r, 2 * r, 2 * r));
                if (p.destaque() && !compacto) {
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.draw(new Ellipse2D.Double(px - r - 3.5, py - r - 3.5, 2 * r + 7, 2 * r + 7));
                }
            }
            if (!compacto) {
                g2.setFont(Estilo.fonte("Regular", 10.5f));
                g2.setColor(Estilo.TEXTO_FRACO);
                int n = pontos.size();
                int[] idx = n == 1 ? new int[]{0} : n == 2 ? new int[]{0, 1} : new int[]{0, n / 2, n - 1};
                for (int i : idx) {
                    String t = DIA.format(pontos.get(i).quando());
                    int tw = g2.getFontMetrics().stringWidth(t);
                    int tx = (int) xDe(i) - tw / 2;
                    tx = Math.max(x0 - 4, Math.min(tx, x1 - tw + 4));
                    g2.drawString(t, tx, y1 + 16);
                }
                if (hover >= 0) {
                    desenharDica(g2, hover, yDe.applyAsDouble(pontos.get(hover).valor()), y0, y1);
                }
            }
            g2.dispose();
        }

        private void desenharDica(Graphics2D g2, int i, double py, int y0, int y1) {
            Ponto p = pontos.get(i);
            double px = xDe(i);
            g2.setColor(new Color(0x26332B));
            g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f, new float[]{3f, 3f}, 0));
            g2.drawLine((int) px, y0, (int) px, y1);
            String l1 = DIA_HORA.format(p.quando());
            String l2 = (modoRisco ? "Índice " : "") + Estilo.numero(p.valor()) + (unidade.isEmpty() ? "" : " " + unidade)
                    + (p.nivel() != null ? " · " + Estilo.textoNivel(p.nivel()) : "");
            g2.setFont(Estilo.fonte("SemiBold", 11.5f));
            FontMetrics fm = g2.getFontMetrics();
            int w = Math.max(fm.stringWidth(l1), fm.stringWidth(l2)) + 18;
            int h = 40;
            int bx = (int) px + 10;
            if (bx + w > getWidth() - 4) {
                bx = (int) px - w - 10;
            }
            int by = (int) Math.max(y0, Math.min(py - h / 2.0, y1 - h));
            g2.setColor(new Color(0x26332B));
            g2.fillRoundRect(bx, by, w, h, 10, 10);
            g2.setColor(Color.WHITE);
            g2.drawString(l2, bx + 9, by + 17);
            g2.setFont(Estilo.fonte("Regular", 10.5f));
            g2.setColor(new Color(0xC8D2C9));
            g2.drawString(l1, bx + 9, by + 32);
        }
    }
}
