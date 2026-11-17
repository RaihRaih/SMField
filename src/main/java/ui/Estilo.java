package ui;

import domain.NivelRisco;
import domain.Talhao;
import domain.ValidacaoException;
import infra.PersistenciaException;
import service.ServicosSMField;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/** Paleta, fontes e utilitários visuais compartilhados pelas telas (apenas apresentação). */
public final class Estilo {

    public static final Color VERDE_ESCURO = new Color(0x1B5E20);
    public static final Color VERDE = new Color(0x2E7D32);
    public static final Color VERDE_CLARO = new Color(0xE8F5E9);
    public static final Color CINZA_FUNDO = new Color(0xF5F6F5);
    public static final Color CINZA_BORDA = new Color(0xC8CCC8);
    public static final Color CINZA_TEXTO = new Color(0x555555);

    public static final Color RISCO_BAIXO = new Color(0x2E7D32);
    public static final Color RISCO_MEDIO = new Color(0xEF8F00);
    public static final Color RISCO_ALTO = new Color(0xC62828);
    public static final Color RISCO_NAO_CALCULADO = new Color(0x757575);

    public static final Font TITULO = new Font(Font.SANS_SERIF, Font.BOLD, 18);
    public static final Font SUBTITULO = new Font(Font.SANS_SERIF, Font.BOLD, 14);
    public static final Font NORMAL = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
    public static final Font PEQUENA = new Font(Font.SANS_SERIF, Font.ITALIC, 12);

    public static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    public static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private Estilo() {
    }

    public static Color corDoNivel(NivelRisco nivel) {
        return switch (nivel) {
            case BAIXO -> RISCO_BAIXO;
            case MEDIO -> RISCO_MEDIO;
            case ALTO -> RISCO_ALTO;
            case NAO_CALCULADO -> RISCO_NAO_CALCULADO;
        };
    }

    public static String textoNivel(NivelRisco nivel) {
        return nivel == NivelRisco.NAO_CALCULADO ? "NÃO CALCULADO" : nivel.getDescricao().toUpperCase(Locale.ROOT);
    }

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

    public static JLabel titulo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(TITULO);
        l.setForeground(VERDE_ESCURO);
        return l;
    }

    public static JLabel nota(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(PEQUENA);
        l.setForeground(CINZA_TEXTO);
        return l;
    }

    public static Border secao(String titulo) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(CINZA_BORDA), titulo,
                        0, 0, SUBTITULO, VERDE_ESCURO),
                BorderFactory.createEmptyBorder(6, 8, 8, 8));
    }

    public static JButton botaoPrimario(String texto) {
        JButton b = new JButton(texto);
        b.setFont(NORMAL.deriveFont(Font.BOLD));
        b.setBackground(VERDE);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        return b;
    }

    public static JButton botao(String texto) {
        JButton b = new JButton(texto);
        b.setFont(NORMAL);
        b.setFocusPainted(false);
        return b;
    }

    public static JPanel painel() {
        JPanel p = new JPanel();
        p.setBackground(Color.WHITE);
        return p;
    }

    /** Tabela somente leitura com seleção de uma linha. */
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
        };
        t.setToolTipText("");
        t.setFont(NORMAL);
        t.setRowHeight(24);
        t.getTableHeader().setFont(NORMAL.deriveFont(Font.BOLD));
        t.getTableHeader().setBackground(VERDE_CLARO);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setFillsViewportHeight(true);
        t.setAutoCreateRowSorter(true);
        t.setGridColor(new Color(0xE0E0E0));
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

    public static JScrollPane rolagem(JComponent c) {
        JScrollPane s = new JScrollPane(c);
        s.setBorder(BorderFactory.createLineBorder(CINZA_BORDA));
        return s;
    }

    /** Renderizador que pinta a célula com a cor do nível de risco (valor = NivelRisco). */
    public static DefaultTableCellRenderer renderizadorNivel() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(NORMAL.deriveFont(Font.BOLD));
                if (value instanceof NivelRisco n) {
                    setText(textoNivel(n));
                    setBackground(corDoNivel(n));
                    setForeground(Color.WHITE);
                } else {
                    setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
                    setForeground(Color.BLACK);
                }
                return this;
            }
        };
    }

    public static GridBagConstraints gbc(int x, int y) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = x;
        c.gridy = y;
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        return c;
    }

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
}
