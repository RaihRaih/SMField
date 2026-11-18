package ui;

import javax.swing.Icon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

/**
 * Ícones vetoriais de traço (estilo linha, cantos arredondados) desenhados em Java2D.
 * São desenhados numa grade de 24 × 24 unidades e escalados para o tamanho pedido,
 * ficando nítidos em qualquer resolução, sem arquivos de imagem.
 */
public final class Icone implements Icon {

    /** Desenhos disponíveis. */
    public enum Tipo {
        DASHBOARD, PROPRIEDADE, TALHAO, INDICADORES, CONSULTA, OCORRENCIAS, HISTORICO, INMET, COMPATIBILIDADE,
        SOLO, TEMPERATURA, AR, CHUVA, ALERTA, INFO, CHECK, FECHAR, MAIS, LIXEIRA, ATUALIZAR, BUSCAR, MENU,
        RECOLHER, EXPANDIR, CALENDARIO, IMPORTAR, OLHO, SETA_CIMA, SETA_BAIXO, LOCAL, ESTACAO, LIMPAR,
        SALVAR, DADOS, MUDANCA, AREA, BROTO, RELOGIO, ESCUDO, TENDENCIA, CIRCULO
    }

    private final Tipo tipo;
    private final int tamanho;
    private final Color cor;

    public Icone(Tipo tipo, int tamanho, Color cor) {
        this.tipo = tipo;
        this.tamanho = tamanho;
        this.cor = cor;
    }

    public static Icone de(Tipo tipo, int tamanho, Color cor) {
        return new Icone(tipo, tamanho, cor);
    }

    public Tipo getTipo() {
        return tipo;
    }

    /** Mesmo desenho em outra cor (por exemplo, para o estado selecionado). */
    public Icone comCor(Color outra) {
        return new Icone(tipo, tamanho, outra);
    }

    @Override
    public int getIconWidth() {
        return tamanho;
    }

    @Override
    public int getIconHeight() {
        return tamanho;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g2.translate(x, y);
            double escala = tamanho / 24.0;
            g2.scale(escala, escala);
            Color efetiva = c != null && !c.isEnabled() ? new Color(cor.getRed(), cor.getGreen(), cor.getBlue(), 110) : cor;
            g2.setColor(efetiva);
            g2.setStroke(new BasicStroke(tamanho <= 16 ? 2.1f : 1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            desenhar(g2);
        } finally {
            g2.dispose();
        }
    }

    private void desenhar(Graphics2D g) {
        switch (tipo) {
            case DASHBOARD -> {
                g.draw(rr(3, 3, 7.5, 8.5, 2));
                g.draw(rr(13.5, 3, 7.5, 5, 2));
                g.draw(rr(13.5, 11.5, 7.5, 9.5, 2));
                g.draw(rr(3, 14.5, 7.5, 6.5, 2));
            }
            case PROPRIEDADE -> {
                Path2D p = new Path2D.Double();
                p.moveTo(3, 10.5);
                p.lineTo(12, 3.5);
                p.lineTo(21, 10.5);
                g.draw(p);
                Path2D casa = new Path2D.Double();
                casa.moveTo(5, 9);
                casa.lineTo(5, 20.5);
                casa.lineTo(19, 20.5);
                casa.lineTo(19, 9);
                g.draw(casa);
                g.draw(rr(9.5, 13.5, 5, 7, 1));
                g.draw(new Line2D.Double(9.5, 13.5, 14.5, 20.5));
                g.draw(new Line2D.Double(14.5, 13.5, 9.5, 20.5));
            }
            case TALHAO -> {
                Path2D p = new Path2D.Double();
                p.moveTo(12, 3);
                p.lineTo(21.5, 8);
                p.lineTo(12, 13);
                p.lineTo(2.5, 8);
                p.closePath();
                g.draw(p);
                Path2D p2 = new Path2D.Double();
                p2.moveTo(2.5, 12.5);
                p2.lineTo(12, 17.5);
                p2.lineTo(21.5, 12.5);
                g.draw(p2);
                Path2D p3 = new Path2D.Double();
                p3.moveTo(2.5, 16.5);
                p3.lineTo(12, 21.5);
                p3.lineTo(21.5, 16.5);
                g.draw(p3);
            }
            case INDICADORES, TENDENCIA -> {
                Path2D p = new Path2D.Double();
                p.moveTo(2.5, 12.5);
                p.lineTo(6.5, 12.5);
                p.lineTo(9.5, 4.5);
                p.lineTo(14.5, 19.5);
                p.lineTo(17.5, 11);
                p.lineTo(21.5, 11);
                g.draw(p);
            }
            case CONSULTA -> {
                g.draw(new Arc2D.Double(2.5, 5, 19, 19, 0, 180, Arc2D.OPEN));
                g.draw(new Line2D.Double(12, 14.5, 16.5, 9.5));
                g.fill(new Ellipse2D.Double(10.4, 12.9, 3.2, 3.2));
                g.draw(new Line2D.Double(2.5, 14.5, 4.5, 14.5));
                g.draw(new Line2D.Double(19.5, 14.5, 21.5, 14.5));
                g.draw(new Line2D.Double(12, 5, 12, 7));
                g.draw(new Line2D.Double(5.3, 7.8, 6.7, 9.2));
            }
            case OCORRENCIAS -> {
                g.draw(rr(5, 4, 14, 17.5, 2.5));
                g.draw(rr(8.5, 2.5, 7, 3.5, 1.2));
                g.draw(new Line2D.Double(8.5, 11, 15.5, 11));
                g.draw(new Line2D.Double(8.5, 14.5, 15.5, 14.5));
                g.draw(new Line2D.Double(8.5, 18, 12.5, 18));
            }
            case HISTORICO, RELOGIO -> {
                if (tipo == Tipo.HISTORICO) {
                    g.draw(new Arc2D.Double(3, 3, 18, 18, 140, 310, Arc2D.OPEN));
                    Path2D seta = new Path2D.Double();
                    seta.moveTo(3, 4.5);
                    seta.lineTo(3.6, 9.2);
                    seta.lineTo(8.2, 8.4);
                    g.draw(seta);
                } else {
                    g.draw(new Ellipse2D.Double(3, 3, 18, 18));
                }
                Path2D ponteiro = new Path2D.Double();
                ponteiro.moveTo(12, 7.5);
                ponteiro.lineTo(12, 12);
                ponteiro.lineTo(15, 14);
                g.draw(ponteiro);
            }
            case INMET -> {
                g.draw(new Arc2D.Double(3, 2.5, 9, 9, 20, 200, Arc2D.OPEN));
                g.draw(new Line2D.Double(7.5, 0.5, 7.5, 0.6));
                nuvem(g, 6, 9);
            }
            case CHUVA -> {
                nuvem(g, 3.5, 3);
                g.draw(new Line2D.Double(8, 18.5, 7, 21.5));
                g.draw(new Line2D.Double(12.5, 18.5, 11.5, 21.5));
                g.draw(new Line2D.Double(17, 18.5, 16, 21.5));
            }
            case COMPATIBILIDADE, BROTO -> {
                g.draw(new Line2D.Double(12, 21, 12, 11));
                Path2D esq = new Path2D.Double();
                esq.moveTo(12, 12);
                esq.curveTo(12, 7, 8.5, 4.5, 3.5, 4.5);
                esq.curveTo(3.5, 9.5, 7, 12, 12, 12);
                g.draw(esq);
                Path2D dir = new Path2D.Double();
                dir.moveTo(12, 14.5);
                dir.curveTo(12, 10.5, 15, 8, 20.5, 8);
                dir.curveTo(20.5, 12.5, 17.5, 14.5, 12, 14.5);
                g.draw(dir);
                if (tipo == Tipo.COMPATIBILIDADE) {
                    g.draw(new Line2D.Double(6, 21, 18, 21));
                }
            }
            case SOLO -> {
                Path2D gota = new Path2D.Double();
                gota.moveTo(12, 2.5);
                gota.curveTo(12, 2.5, 6, 9, 6, 13);
                gota.curveTo(6, 16.5, 8.7, 19, 12, 19);
                gota.curveTo(15.3, 19, 18, 16.5, 18, 13);
                gota.curveTo(18, 9, 12, 2.5, 12, 2.5);
                g.draw(gota);
                g.draw(new Line2D.Double(3, 21.5, 21, 21.5));
            }
            case TEMPERATURA -> {
                Path2D t = new Path2D.Double();
                t.moveTo(9.5, 14.3);
                t.lineTo(9.5, 5);
                t.curveTo(9.5, 2, 14.5, 2, 14.5, 5);
                t.lineTo(14.5, 14.3);
                g.draw(t);
                g.draw(new Arc2D.Double(7.5, 13, 9, 9, 120, 300, Arc2D.OPEN));
                g.draw(new Line2D.Double(12, 10, 12, 16));
            }
            case AR -> {
                Path2D v1 = new Path2D.Double();
                v1.moveTo(2.5, 9);
                v1.lineTo(15, 9);
                v1.curveTo(18.5, 9, 18.5, 4, 15, 4);
                v1.curveTo(13.5, 4, 12.5, 5, 12.5, 6);
                g.draw(v1);
                Path2D v2 = new Path2D.Double();
                v2.moveTo(2.5, 13.5);
                v2.lineTo(18.5, 13.5);
                v2.curveTo(22.5, 13.5, 22.5, 19.5, 18.5, 19.5);
                v2.curveTo(17, 19.5, 15.8, 18.5, 15.8, 17.3);
                g.draw(v2);
                g.draw(new Line2D.Double(2.5, 17.5, 10, 17.5));
            }
            case ALERTA -> {
                Path2D tri = new Path2D.Double();
                tri.moveTo(12, 3);
                tri.lineTo(22, 20.5);
                tri.lineTo(2, 20.5);
                tri.closePath();
                g.draw(tri);
                g.draw(new Line2D.Double(12, 9.5, 12, 14));
                g.fill(new Ellipse2D.Double(10.9, 16.2, 2.2, 2.2));
            }
            case INFO -> {
                g.draw(new Ellipse2D.Double(2.5, 2.5, 19, 19));
                g.draw(new Line2D.Double(12, 11, 12, 16.5));
                g.fill(new Ellipse2D.Double(10.9, 6.6, 2.2, 2.2));
            }
            case CHECK -> {
                Path2D p = new Path2D.Double();
                p.moveTo(4.5, 12.5);
                p.lineTo(9.5, 17.5);
                p.lineTo(19.5, 6.5);
                g.draw(p);
            }
            case FECHAR -> {
                g.draw(new Line2D.Double(6, 6, 18, 18));
                g.draw(new Line2D.Double(18, 6, 6, 18));
            }
            case MAIS -> {
                g.draw(new Line2D.Double(12, 5, 12, 19));
                g.draw(new Line2D.Double(5, 12, 19, 12));
            }
            case LIXEIRA -> {
                g.draw(new Line2D.Double(3.5, 6.5, 20.5, 6.5));
                Path2D corpo = new Path2D.Double();
                corpo.moveTo(5.5, 6.5);
                corpo.lineTo(6.5, 20);
                corpo.curveTo(6.6, 20.8, 7.2, 21.5, 8, 21.5);
                corpo.lineTo(16, 21.5);
                corpo.curveTo(16.8, 21.5, 17.4, 20.8, 17.5, 20);
                corpo.lineTo(18.5, 6.5);
                g.draw(corpo);
                Path2D tampa = new Path2D.Double();
                tampa.moveTo(9, 6.5);
                tampa.lineTo(9, 4);
                tampa.lineTo(15, 4);
                tampa.lineTo(15, 6.5);
                g.draw(tampa);
                g.draw(new Line2D.Double(10, 10.5, 10, 17.5));
                g.draw(new Line2D.Double(14, 10.5, 14, 17.5));
            }
            case ATUALIZAR -> {
                g.draw(new Arc2D.Double(3.5, 3.5, 17, 17, 20, 280, Arc2D.OPEN));
                Path2D seta = new Path2D.Double();
                seta.moveTo(20.5, 3.5);
                seta.lineTo(20.5, 9.2);
                seta.lineTo(14.8, 9.2);
                g.draw(seta);
            }
            case BUSCAR -> {
                g.draw(new Ellipse2D.Double(3.5, 3.5, 13, 13));
                g.draw(new Line2D.Double(14.5, 14.5, 20.5, 20.5));
            }
            case MENU -> {
                g.draw(new Line2D.Double(4, 6.5, 20, 6.5));
                g.draw(new Line2D.Double(4, 12, 20, 12));
                g.draw(new Line2D.Double(4, 17.5, 20, 17.5));
            }
            case RECOLHER, EXPANDIR -> {
                g.draw(rr(3, 3.5, 18, 17, 3));
                g.draw(new Line2D.Double(9.5, 3.5, 9.5, 20.5));
                Path2D ch = new Path2D.Double();
                if (tipo == Tipo.RECOLHER) {
                    ch.moveTo(16, 9);
                    ch.lineTo(13, 12);
                    ch.lineTo(16, 15);
                } else {
                    ch.moveTo(13.5, 9);
                    ch.lineTo(16.5, 12);
                    ch.lineTo(13.5, 15);
                }
                g.draw(ch);
            }
            case CALENDARIO -> {
                g.draw(rr(3.5, 5, 17, 16, 2.5));
                g.draw(new Line2D.Double(3.5, 10, 20.5, 10));
                g.draw(new Line2D.Double(8, 3, 8, 7));
                g.draw(new Line2D.Double(16, 3, 16, 7));
            }
            case IMPORTAR -> {
                g.draw(new Line2D.Double(12, 3, 12, 15));
                Path2D seta = new Path2D.Double();
                seta.moveTo(7, 10.5);
                seta.lineTo(12, 15.5);
                seta.lineTo(17, 10.5);
                g.draw(seta);
                Path2D base = new Path2D.Double();
                base.moveTo(3.5, 15.5);
                base.lineTo(3.5, 19);
                base.curveTo(3.5, 20.1, 4.4, 21, 5.5, 21);
                base.lineTo(18.5, 21);
                base.curveTo(19.6, 21, 20.5, 20.1, 20.5, 19);
                base.lineTo(20.5, 15.5);
                g.draw(base);
            }
            case OLHO -> {
                Path2D olho = new Path2D.Double();
                olho.moveTo(2, 12);
                olho.curveTo(4.5, 7, 8, 5, 12, 5);
                olho.curveTo(16, 5, 19.5, 7, 22, 12);
                olho.curveTo(19.5, 17, 16, 19, 12, 19);
                olho.curveTo(8, 19, 4.5, 17, 2, 12);
                g.draw(olho);
                g.draw(new Ellipse2D.Double(9, 9, 6, 6));
            }
            case SETA_CIMA, SETA_BAIXO -> {
                double s = tipo == Tipo.SETA_CIMA ? 1 : -1;
                g.translate(0, tipo == Tipo.SETA_CIMA ? 0 : 24);
                g.scale(1, s);
                g.draw(new Line2D.Double(12, 19.5, 12, 4.5));
                Path2D p = new Path2D.Double();
                p.moveTo(5.5, 11);
                p.lineTo(12, 4.5);
                p.lineTo(18.5, 11);
                g.draw(p);
            }
            case LOCAL -> {
                Path2D pino = new Path2D.Double();
                pino.moveTo(12, 21.5);
                pino.curveTo(12, 21.5, 4.5, 14.5, 4.5, 9.5);
                pino.curveTo(4.5, 5.4, 7.9, 2.5, 12, 2.5);
                pino.curveTo(16.1, 2.5, 19.5, 5.4, 19.5, 9.5);
                pino.curveTo(19.5, 14.5, 12, 21.5, 12, 21.5);
                g.draw(pino);
                g.draw(new Ellipse2D.Double(9.3, 6.8, 5.4, 5.4));
            }
            case ESTACAO -> {
                g.draw(new Line2D.Double(12, 10, 7.5, 21.5));
                g.draw(new Line2D.Double(12, 10, 16.5, 21.5));
                g.draw(new Line2D.Double(9, 17.5, 15, 17.5));
                g.draw(new Ellipse2D.Double(10.3, 6.8, 3.4, 3.4));
                g.draw(new Arc2D.Double(6.5, 3, 11, 11, 35, 110, Arc2D.OPEN));
                g.draw(new Arc2D.Double(3.5, 0.5, 17, 17, 40, 100, Arc2D.OPEN));
            }
            case LIMPAR -> {
                g.draw(new Arc2D.Double(3.5, 3.5, 17, 17, 160, -280, Arc2D.OPEN));
                Path2D seta = new Path2D.Double();
                seta.moveTo(3.5, 3.5);
                seta.lineTo(3.5, 9.2);
                seta.lineTo(9.2, 9.2);
                g.draw(seta);
            }
            case SALVAR -> {
                Path2D p = new Path2D.Double();
                p.moveTo(5, 3.5);
                p.lineTo(16, 3.5);
                p.lineTo(20.5, 8);
                p.lineTo(20.5, 19);
                p.curveTo(20.5, 19.8, 19.8, 20.5, 19, 20.5);
                p.lineTo(5, 20.5);
                p.curveTo(4.2, 20.5, 3.5, 19.8, 3.5, 19);
                p.lineTo(3.5, 5);
                p.curveTo(3.5, 4.2, 4.2, 3.5, 5, 3.5);
                g.draw(p);
                g.draw(rr(7, 13, 10, 7.5, 1));
                g.draw(new Line2D.Double(8, 3.5, 8, 8));
                g.draw(new Line2D.Double(8, 8, 14.5, 8));
            }
            case DADOS -> {
                g.draw(new Ellipse2D.Double(4, 3, 16, 5.5));
                Path2D p = new Path2D.Double();
                p.moveTo(4, 5.75);
                p.lineTo(4, 18.25);
                p.curveTo(4, 21.5, 20, 21.5, 20, 18.25);
                p.lineTo(20, 5.75);
                g.draw(p);
                g.draw(new Arc2D.Double(4, 9.25, 16, 5.5, 180, 180, Arc2D.OPEN));
            }
            case MUDANCA -> {
                Path2D a = new Path2D.Double();
                a.moveTo(3.5, 8);
                a.lineTo(20.5, 8);
                a.moveTo(16.5, 4);
                a.lineTo(20.5, 8);
                a.lineTo(16.5, 12);
                g.draw(a);
                Path2D b = new Path2D.Double();
                b.moveTo(20.5, 16);
                b.lineTo(3.5, 16);
                b.moveTo(7.5, 12);
                b.lineTo(3.5, 16);
                b.lineTo(7.5, 20);
                g.draw(b);
            }
            case AREA -> {
                g.draw(rr(3.5, 3.5, 17, 17, 2));
                g.draw(new Line2D.Double(3.5, 12, 20.5, 12));
                g.draw(new Line2D.Double(12, 3.5, 12, 20.5));
            }
            case ESCUDO -> {
                Path2D p = new Path2D.Double();
                p.moveTo(12, 21.5);
                p.curveTo(12, 21.5, 20, 17.5, 20, 11.5);
                p.lineTo(20, 5.5);
                p.lineTo(12, 2.5);
                p.lineTo(4, 5.5);
                p.lineTo(4, 11.5);
                p.curveTo(4, 17.5, 12, 21.5, 12, 21.5);
                g.draw(p);
            }
            case CIRCULO -> g.draw(new Ellipse2D.Double(4, 4, 16, 16));
            default -> {
            }
        }
    }

    private static void nuvem(Graphics2D g, double x, double y) {
        Path2D n = new Path2D.Double();
        n.moveTo(x + 4, y + 14);
        n.lineTo(x + 13, y + 14);
        n.curveTo(x + 16, y + 14, x + 17.5, y + 12, x + 17.5, y + 10);
        n.curveTo(x + 17.5, y + 7.6, x + 15.6, y + 6, x + 13.4, y + 6.2);
        n.curveTo(x + 12.5, y + 3.3, x + 10, y + 1.5, x + 7.4, y + 1.8);
        n.curveTo(x + 4.2, y + 2.2, x + 2, y + 5, x + 2.4, y + 8.2);
        n.curveTo(x + 0.6, y + 8.8, x - 0.4, y + 10.4, x, y + 12);
        n.curveTo(x + 0.4, y + 13.3, x + 2, y + 14, x + 4, y + 14);
        g.draw(n);
    }

    private static RoundRectangle2D rr(double x, double y, double w, double h, double r) {
        return new RoundRectangle2D.Double(x, y, w, h, r * 2, r * 2);
    }
}
