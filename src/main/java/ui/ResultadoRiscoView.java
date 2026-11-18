package ui;

import domain.NivelRisco;
import domain.ResultadoRisco;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

/**
 * Exibe o resultado de um cálculo de risco: medidor do índice (0–100), selo da
 * classificação com ícone e texto, e a justificativa produzida pelo sistema (sem alterações).
 */
public class ResultadoRiscoView extends JPanel {

    private final Graficos.Medidor medidor = new Graficos.Medidor();
    private final JLabel indice = new JLabel("—");
    private final JLabel nivel = Estilo.selo(Estilo.pilulaDoNivel(NivelRisco.NAO_CALCULADO));
    private final JPanel selo = new JPanel(new BorderLayout(0, 6));
    private final JTextArea justificativa = new JTextArea(5, 40);

    public ResultadoRiscoView() {
        setLayout(new BorderLayout(18, 0));
        setOpaque(false);
        selo.setOpaque(false);
        selo.add(medidor, BorderLayout.CENTER);
        JPanel linhaSelo = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        linhaSelo.setOpaque(false);
        linhaSelo.add(nivel);
        selo.add(linhaSelo, BorderLayout.SOUTH);
        selo.setPreferredSize(new Dimension(220, 166));
        add(selo, BorderLayout.WEST);

        JPanel texto = new JPanel(new BorderLayout(0, 6));
        texto.setOpaque(false);
        JLabel titulo = new JLabel("Justificativa");
        titulo.setFont(Estilo.ROTULO);
        titulo.setForeground(Estilo.TEXTO_SUAVE);
        texto.add(titulo, BorderLayout.NORTH);
        justificativa.setLineWrap(true);
        justificativa.setWrapStyleWord(true);
        justificativa.setEditable(false);
        justificativa.setFont(Estilo.fonte("Regular", 13.5f));
        justificativa.setForeground(Estilo.TEXTO);
        justificativa.setBackground(new Color(0xF8FAF7));
        justificativa.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        justificativa.putClientProperty("FlatLaf.style", "background:#F8FAF7");
        javax.swing.JScrollPane rolagem = Estilo.rolagem(justificativa);
        rolagem.getViewport().setBackground(new Color(0xF8FAF7));
        texto.add(rolagem, BorderLayout.CENTER);
        add(texto, BorderLayout.CENTER);
        limpar("Nenhum resultado para exibir.");
    }

    public void mostrar(ResultadoRisco r) {
        NivelRisco n = r.getNivel();
        indice.setText(r.isCalculado() ? String.valueOf(r.getIndice()) : "—");
        medidor.mostrar(r.isCalculado() ? r.getIndice() : null, n);
        Estilo.atualizarSelo(nivel, Estilo.pilulaDoNivel(n));
        justificativa.setText(r.getJustificativa());
        justificativa.setCaretPosition(0);
    }

    public void limpar(String mensagem) {
        indice.setText("—");
        medidor.mostrar(null, NivelRisco.NAO_CALCULADO);
        Estilo.atualizarSelo(nivel, Estilo.pilulaDoNivel(NivelRisco.NAO_CALCULADO));
        justificativa.setText(mensagem);
    }
}
