package ui;

import infra.ArmazenamentoLocal;
import infra.PersistenciaException;
import service.ServicosSMField;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.nio.file.Path;

/**
 * Ponto de entrada do SMField.
 * <p>
 * Os dados ficam na pasta {@code dados/} do diretório de execução, ou no caminho indicado por
 * {@code -Dsmfield.dados=...}.
 */
public final class AppMain {

    private AppMain() {
    }

    public static void main(String[] args) {
        configurarAparencia();
        SwingUtilities.invokeLater(() -> {
            Path pasta = ArmazenamentoLocal.diretorioPadrao();
            try {
                ServicosSMField servicos = new ServicosSMField(pasta);
                new MainFrame(servicos).setVisible(true);
            } catch (PersistenciaException e) {
                JOptionPane.showMessageDialog(null, e.getMessage(), "SMField — erro ao abrir os dados",
                        JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }

    private static void configurarAparencia() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    UIManager.put("nimbusBase", Estilo.VERDE_ESCURO);
                    UIManager.put("nimbusSelectionBackground", Estilo.VERDE);
                    UIManager.put("control", Estilo.CINZA_FUNDO);
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException e) {
            // mantém a aparência padrão do Swing
        }
    }
}
