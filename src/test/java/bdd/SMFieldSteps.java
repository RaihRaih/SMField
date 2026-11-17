package bdd;

import domain.Alerta;
import domain.NivelRisco;
import domain.Propriedade;
import domain.RegistroHistorico;
import domain.ResultadoRisco;
import domain.Talhao;
import domain.TipoAlerta;
import domain.ValidacaoException;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.After;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import service.MonitoramentoService;
import service.ServicosSMField;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/** Passos dos cenários BDD. Todos usam os serviços reais da aplicação com persistência em pasta temporária. */
public class SMFieldSteps {

    private static final Clock RELOGIO = Clock.fixed(Instant.parse("2026-11-17T15:00:00Z"), ZoneId.of("America/Sao_Paulo"));

    private Path pasta;
    private ServicosSMField servicos;
    private String nomeInformado;
    private String municipioInformado;
    private Propriedade propriedade;
    private Talhao talhao;
    private ResultadoRisco resultado;
    private ValidacaoException erro;
    private NivelRisco nivelClassificado;
    private Double umidadeSoloInformada;
    private List<MonitoramentoService.ItemPainel> painel;
    private LocalDateTime proximoHorario = LocalDateTime.now(RELOGIO).minusDays(1);

    @Dado("um sistema SMField sem dados cadastrados")
    public void sistemaVazio() throws IOException {
        pasta = Files.createTempDirectory("smfield-bdd");
        servicos = new ServicosSMField(pasta, RELOGIO);
    }

    @After
    public void limpar() throws IOException {
        if (pasta != null) {
            try (Stream<Path> arquivos = Files.walk(pasta)) {
                arquivos.sorted(Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
            }
        }
    }

    // ------------------------------------------------------------------ cadastro

    @Dado("que o usuário informa o nome {string} e o município {string}")
    public void usuarioInforma(String nome, String municipio) {
        nomeInformado = nome;
        municipioInformado = municipio;
    }

    @Quando("cadastrar a propriedade")
    public void cadastrarPropriedade() {
        propriedade = servicos.propriedades().cadastrar(nomeInformado, municipioInformado);
    }

    @Entao("a propriedade {string} deverá ser armazenada")
    public void propriedadeArmazenada(String nome) {
        ServicosSMField reaberto = new ServicosSMField(pasta, RELOGIO);
        Propriedade lida = reaberto.propriedades().obter(propriedade.getId());
        assertEquals(nome, lida.getNome());
    }

    @E("a propriedade {string} deverá aparecer na listagem")
    public void propriedadeNaListagem(String nome) {
        assertTrue(servicos.propriedades().listar().stream().anyMatch(p -> p.getNome().equals(nome)));
    }

    @Dado("uma propriedade existente chamada {string} no município {string}")
    public void propriedadeExistente(String nome, String municipio) {
        propriedade = servicos.propriedades().cadastrar(nome, municipio);
    }

    @Quando("cadastrar o talhão {string} com área {double} hectares e cultura {string}")
    public void cadastrarTalhao(String nome, double area, String cultura) {
        talhao = servicos.talhoes().cadastrar(propriedade.getId(), nome, area, cultura);
    }

    @Entao("o talhão {string} deverá estar vinculado à propriedade {string}")
    public void talhaoVinculado(String nomeTalhao, String nomePropriedade) {
        List<Talhao> talhoes = servicos.talhoes().listarPorPropriedade(propriedade.getId());
        assertEquals(1, talhoes.size());
        assertEquals(nomeTalhao, talhoes.get(0).getNome());
        assertEquals(nomePropriedade, servicos.propriedades().obter(talhoes.get(0).getPropriedadeId()).getNome());
    }

    @Quando("tentar cadastrar o talhão {string} em uma propriedade inexistente")
    public void talhaoSemPropriedade(String nome) {
        try {
            servicos.talhoes().cadastrar(999, nome, 5.0, "Soja");
            fail("O cadastro deveria ter sido rejeitado");
        } catch (ValidacaoException e) {
            erro = e;
        }
    }

    @Entao("o cadastro deverá ser rejeitado com a mensagem contendo {string}")
    public void cadastroRejeitado(String trecho) {
        assertNotNull(erro, "nenhum erro de validação foi gerado");
        assertTrue(erro.getMessage().contains(trecho), erro.getMessage());
    }

    @E("nenhum talhão deverá estar cadastrado")
    public void nenhumTalhao() {
        assertTrue(servicos.talhoes().listar().isEmpty());
    }

    // ------------------------------------------------------------------ indicadores e risco

    @Dado("um talhão {string} de cultura {string} na propriedade {string}")
    public void umTalhao(String nome, String cultura, String nomePropriedade) {
        propriedade = servicos.propriedades().cadastrar(nomePropriedade, "Município Fictício");
        talhao = servicos.talhoes().cadastrar(propriedade.getId(), nome, 10.0, cultura);
    }

    @Dado("indicadores dentro das faixas ideais: solo {double}, temperatura {double}, umidade do ar {double} e precipitação {double}")
    public void indicadoresIdeais(double solo, double temp, double ar, double prec) {
        registrar(talhao, solo, temp, ar, prec);
    }

    @Dado("indicadores críticos: solo {double}, temperatura {double}, umidade do ar {double} e precipitação {double}")
    public void indicadoresCriticos(double solo, double temp, double ar, double prec) {
        registrar(talhao, solo, temp, ar, prec);
    }

    private void registrar(Talhao t, double solo, double temp, double ar, double prec) {
        servicos.indicadores().registrar(t.getId(), solo, temp, ar, prec, proximoHorario);
        proximoHorario = proximoHorario.plusHours(1);
    }

    @Quando("o sistema calcular o risco do talhão")
    public void calcularRisco() {
        resultado = servicos.monitoramento().detalhe(talhao.getId()).resultado();
    }

    @Entao("o índice deverá estar entre {int} e {int}")
    public void indiceEntre(int minimo, int maximo) {
        assertTrue(resultado.isCalculado());
        int indice = resultado.getIndice();
        assertTrue(indice >= minimo && indice <= maximo, "índice obtido: " + indice);
    }

    @E("a classificação deverá ser {string}")
    public void classificacao(String esperado) {
        String obtido = resultado.getNivel() == NivelRisco.NAO_CALCULADO ? "NÃO CALCULADO" : resultado.getNivel().name();
        assertEquals(esperado, obtido);
    }

    @E("a justificativa deverá conter {string}")
    public void justificativaContem(String trecho) {
        assertTrue(resultado.getJustificativa().contains(trecho), resultado.getJustificativa());
    }

    @Dado("um talhão sem todos os indicadores registrados")
    public void talhaoSemIndicadores() {
        assertTrue(servicos.indicadores().listarPorTalhao(talhao.getId()).isEmpty());
    }

    @Quando("solicitar a análise de risco com a temperatura ausente")
    public void analiseSemTemperatura() {
        try {
            servicos.indicadores().registrar(talhao.getId(), 55.0, null, 65.0, 12.0, proximoHorario);
            fail("Registro incompleto deveria ser rejeitado");
        } catch (ValidacaoException e) {
            erro = e;
        }
        resultado = servicos.risco().calcular(55.0, null, 65.0, 12.0);
    }

    @Entao("o sistema deverá informar que não há dados suficientes")
    public void dadosInsuficientes() {
        assertFalse(resultado.isCalculado());
        assertTrue(resultado.getJustificativa().contains("dados insuficientes"), resultado.getJustificativa());
        assertTrue(erro.getMessage().contains("Dados insuficientes"), erro.getMessage());
        assertEquals(NivelRisco.NAO_CALCULADO, servicos.monitoramento().detalhe(talhao.getId()).resultado().getNivel());
    }

    @Dado("um valor de umidade do solo igual a {double}")
    public void umidadeInvalida(double valor) {
        umidadeSoloInformada = valor;
    }

    @Quando("registrar os indicadores")
    public void registrarIndicadores() {
        try {
            servicos.indicadores().registrar(talhao.getId(), umidadeSoloInformada, 24.0, 65.0, 12.0, proximoHorario);
        } catch (ValidacaoException e) {
            erro = e;
        }
    }

    @E("o talhão deverá continuar sem registros de indicadores")
    public void semRegistros() {
        assertTrue(servicos.indicadores().listarPorTalhao(talhao.getId()).isEmpty());
    }

    @Quando("o índice {int} for classificado")
    public void classificarIndice(int indice) {
        nivelClassificado = servicos.risco().classificar(indice);
    }

    @Entao("a classificação do índice deverá ser {string}")
    public void classificacaoIndice(String esperado) {
        assertEquals(NivelRisco.valueOf(esperado), nivelClassificado);
    }

    // ------------------------------------------------------------------ dashboard, alertas e histórico

    @Dado("os seguintes talhões e indicadores na propriedade {string}:")
    public void talhoesComIndicadores(String nomePropriedade, DataTable tabela) {
        propriedade = servicos.propriedades().cadastrar(nomePropriedade, "Município Fictício");
        for (Map<String, String> linha : tabela.asMaps()) {
            Talhao t = servicos.talhoes().cadastrar(propriedade.getId(), linha.get("talhao"), 5.0, linha.get("cultura"));
            registrar(t, Double.parseDouble(linha.get("solo")), Double.parseDouble(linha.get("temperatura")),
                    Double.parseDouble(linha.get("umidadeAr")), Double.parseDouble(linha.get("precipitacao")));
        }
    }

    @E("um talhão {string} sem indicadores")
    public void talhaoNovoSemIndicadores(String nome) {
        servicos.talhoes().cadastrar(propriedade.getId(), nome, 2.0, "Café");
    }

    @Quando("abrir o dashboard")
    public void abrirDashboard() {
        painel = servicos.monitoramento().painel();
    }

    @Entao("os talhões deverão aparecer nesta ordem com estas classificações:")
    public void ordemDoPainel(DataTable tabela) {
        List<Map<String, String>> esperado = tabela.asMaps();
        assertEquals(esperado.size(), painel.size());
        for (int i = 0; i < esperado.size(); i++) {
            assertEquals(esperado.get(i).get("talhao"), painel.get(i).talhao());
            NivelRisco nivel = NivelRisco.valueOf(esperado.get(i).get("classificacao"));
            assertEquals(nivel, painel.get(i).nivel());
            if (nivel.isCalculado()) {
                assertEquals(nivel, NivelRisco.classificar(painel.get(i).indice()));
            }
        }
    }

    @E("deverá existir um alerta de {string} para o talhão {string}")
    public void alertaExiste(String tipo, String nomeTalhao) {
        List<Alerta> alertas = servicos.monitoramento().alertas();
        assertTrue(alertas.stream().anyMatch(a -> a.tipo() == TipoAlerta.valueOf(tipo) && a.talhao().equals(nomeTalhao)),
                "alertas: " + alertas);
    }

    @Quando("o sistema for fechado e aberto novamente")
    public void reabrir() {
        servicos = new ServicosSMField(pasta, RELOGIO);
    }

    @Entao("o histórico do talhão deverá ter {int} registros")
    public void historicoTem(int quantidade) {
        assertEquals(quantidade, servicos.monitoramento().historico(talhao.getId()).size());
    }

    @E("o último registro deverá indicar mudança de {string} para {string}")
    public void mudanca(String de, String para) {
        List<RegistroHistorico> h = servicos.monitoramento().historico(talhao.getId());
        RegistroHistorico ultimo = h.get(h.size() - 1);
        assertTrue(ultimo.mudouClassificacao());
        assertEquals(NivelRisco.valueOf(de), ultimo.nivelAnterior());
        assertEquals(NivelRisco.valueOf(para), ultimo.resultado().getNivel());
    }
}
