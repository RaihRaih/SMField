package ui;

import domain.NivelRisco;
import domain.ResultadoRisco;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

/** Componente visual que exibe índice, classificação colorida e justificativa de um resultado. */
public class ResultadoRiscoView extends JPanel {

    private final JLabel indice = new JLabel("—", SwingConstants.CENTER);
    private final JLabel nivel = new JLabel("NÃO CALCULADO", SwingConstants.CENTER);
    private final JPanel selo = new JPanel(new GridLayout(2, 1));
    private final JTextArea justificativa = new JTextArea(6, 40);

    public ResultadoRiscoView() {
        setLayout(new BorderLayout(10, 0));
        setOpaque(false);
        indice.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 34));
        indice.setForeground(Color.WHITE);
        nivel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        nivel.setForeground(Color.WHITE);
        selo.add(indice);
        selo.add(nivel);
        selo.setPreferredSize(new Dimension(150, 110));
        selo.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        justificativa.setLineWrap(true);
        justificativa.setWrapStyleWord(true);
        justificativa.setEditable(false);
        justificativa.setFont(Estilo.NORMAL);
        justificativa.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        JPanel coluna = new JPanel(new BorderLayout());
        coluna.setOpaque(false);
        coluna.add(selo, BorderLayout.NORTH);
        add(coluna, BorderLayout.WEST);
        add(Estilo.rolagem(justificativa), BorderLayout.CENTER);
        limpar("Nenhum resultado para exibir.");
    }

    public void mostrar(ResultadoRisco r) {
        NivelRisco n = r.getNivel();
        selo.setBackground(Estilo.corDoNivel(n));
        indice.setText(r.isCalculado() ? String.valueOf(r.getIndice()) : "—");
        nivel.setText(Estilo.textoNivel(n));
        justificativa.setText(r.getJustificativa());
        justificativa.setCaretPosition(0);
    }

    public void limpar(String mensagem) {
        selo.setBackground(Estilo.RISCO_NAO_CALCULADO);
        indice.setText("—");
        nivel.setText("NÃO CALCULADO");
        justificativa.setText(mensagem);
    }
}
