package ui;

import service.CompatibilidadeService;
import service.ServicosSMField;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;

/** Módulo demonstrativo (P3): compatibilidade de culturas por município com base SIMULADA. */
public class CompatibilidadePanel extends JPanel implements Atualizavel {

    private final ServicosSMField servicos;

    private final JComboBox<String> comboMunicipio = new JComboBox<>();
    private final JButton botaoAvaliar = Estilo.botaoPrimario("Avaliar compatibilidade");
    private final JLabel mensagem = new JLabel(" ");
    private final JLabel fonte = Estilo.nota(" ");
    private final JLabel perfilTemp = new JLabel("—");
    private final JLabel perfilSolo = new JLabel("—");
    private final JLabel perfilAr = new JLabel("—");
    private final JLabel perfilPrec = new JLabel("—");
    private final DefaultTableModel modelo = Estilo.modelo("Cultura", "Resultado", "Divergências em relação à base simulada");
    private final JTable tabela = Estilo.tabela(modelo);

    public CompatibilidadePanel(ServicosSMField servicos) {
        this.servicos = servicos;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setBackground(Estilo.CINZA_FUNDO);

        JPanel topo = new JPanel(new BorderLayout(4, 4));
        topo.setOpaque(false);
        topo.add(Estilo.titulo("Compatibilidade de culturas por município (módulo demonstrativo)"), BorderLayout.NORTH);
        JPanel selecao = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selecao.setOpaque(false);
        selecao.add(new JLabel("Município:"));
        selecao.add(comboMunicipio);
        selecao.add(botaoAvaliar);
        topo.add(selecao, BorderLayout.CENTER);
        JLabel aviso = Estilo.nota(CompatibilidadeService.AVISO + " Usa o clima real da estação do INMET do município "
                + "(ou, se não houver estação, os indicadores registrados nos talhões); pH, textura e fertilidade não fazem parte desta versão.");
        topo.add(aviso, BorderLayout.SOUTH);
        add(topo, BorderLayout.NORTH);

        JPanel perfil = new JPanel(new GridLayout(4, 2, 12, 8));
        perfil.setBorder(Estilo.secao("Médias do clima no município"));
        perfil.add(new JLabel("Temperatura:"));
        perfil.add(perfilTemp);
        perfil.add(new JLabel("Umidade do solo:"));
        perfil.add(perfilSolo);
        perfil.add(new JLabel("Umidade do ar:"));
        perfil.add(perfilAr);
        perfil.add(new JLabel("Precipitação 24 h:"));
        perfil.add(perfilPrec);

        JPanel resultado = new JPanel(new BorderLayout(4, 4));
        resultado.setBorder(Estilo.secao("Culturas da base simulada"));
        mensagem.setFont(Estilo.SUBTITULO);
        JPanel cabecalho = new JPanel(new BorderLayout(2, 2));
        cabecalho.setOpaque(false);
        cabecalho.add(mensagem, BorderLayout.NORTH);
        cabecalho.add(fonte, BorderLayout.SOUTH);
        resultado.add(cabecalho, BorderLayout.NORTH);
        tabela.getColumnModel().getColumn(0).setPreferredWidth(90);
        tabela.getColumnModel().getColumn(1).setMinWidth(190);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(700);
        resultado.add(Estilo.rolagem(tabela), BorderLayout.CENTER);

        JPanel colunaPerfil = new JPanel(new BorderLayout());
        colunaPerfil.setOpaque(false);
        colunaPerfil.add(perfil, BorderLayout.NORTH);
        add(colunaPerfil, BorderLayout.WEST);
        add(resultado, BorderLayout.CENTER);

        botaoAvaliar.addActionListener(e -> avaliar());
        atualizar();
    }

    private void avaliar() {
        modelo.setRowCount(0);
        Object m = comboMunicipio.getSelectedItem();
        if (m == null) {
            mensagem.setText("Cadastre propriedades para selecionar um município.");
            return;
        }
        CompatibilidadeService.Avaliacao a = servicos.compatibilidade().avaliar(m.toString());
        mensagem.setText(a.mensagem());
        fonte.setText("<html>Fonte: " + a.fonte() + "</html>");
        if (!a.dadosSuficientes()) {
            perfilTemp.setText("—");
            perfilSolo.setText("—");
            perfilAr.setText("—");
            perfilPrec.setText("—");
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
        }
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
}
