package service;

import domain.Propriedade;
import domain.Talhao;
import domain.TipoOcorrencia;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Carrega dados fictícios para demonstração. Todos os nomes são marcados como
 * <b>SIMULADO — USO ACADÊMICO</b> e os municípios são fictícios; os valores não representam
 * condições agrícolas reais. A carga é idempotente: não duplica dados se já tiver sido feita.
 */
public class DadosDemonstracaoService {

    public static final String MARCADOR = "[SIMULADO]";

    private final PropriedadeService propriedades;
    private final TalhaoService talhoes;
    private final IndicadorService indicadores;
    private final OcorrenciaService ocorrencias;
    private final Clock relogio;

    public DadosDemonstracaoService(PropriedadeService propriedades, TalhaoService talhoes,
                                    IndicadorService indicadores, OcorrenciaService ocorrencias, Clock relogio) {
        this.propriedades = propriedades;
        this.talhoes = talhoes;
        this.indicadores = indicadores;
        this.ocorrencias = ocorrencias;
        this.relogio = relogio;
    }

    public boolean jaCarregado() {
        return propriedades.listar().stream().anyMatch(p -> p.getNome().startsWith(MARCADOR));
    }

    /** @return {@code true} se os dados foram carregados; {@code false} se já existiam */
    public boolean carregar() {
        if (jaCarregado()) {
            return false;
        }
        LocalDateTime agora = LocalDateTime.now(relogio).truncatedTo(ChronoUnit.HOURS);
        LocalDate hoje = LocalDate.now(relogio);

        Propriedade boaVista = propriedades.cadastrar(MARCADOR + " Fazenda Boa Vista", "Município Fictício A");
        Propriedade santaLuzia = propriedades.cadastrar(MARCADOR + " Sítio Santa Luzia", "Município Fictício A");
        Propriedade serraAzul = propriedades.cadastrar(MARCADOR + " Fazenda Serra Azul", "Município Fictício B");

        Talhao milhoNorte = talhoes.cadastrar(boaVista.getId(), "Talhão Milho Norte", 12.0, "Milho");
        Talhao sojaSul = talhoes.cadastrar(boaVista.getId(), "Talhão Soja Sul", 25.5, "Soja");
        Talhao cafeEncosta = talhoes.cadastrar(santaLuzia.getId(), "Talhão Café Encosta", 8.0, "Café");
        Talhao milhoBaixada = talhoes.cadastrar(santaLuzia.getId(), "Talhão Milho Baixada", 10.0, "Milho");
        Talhao sojaPlato = talhoes.cadastrar(serraAzul.getId(), "Talhão Soja Platô", 30.0, "Soja");
        talhoes.cadastrar(serraAzul.getId(), "Talhão Café Novo", 6.0, "Café"); // sem indicadores (NÃO CALCULADO)

        // Milho Norte: histórico de baixo -> médio -> baixo
        indicadores.registrar(milhoNorte.getId(), 55.0, 24.0, 65.0, 12.0, agora.minusDays(2));
        indicadores.registrar(milhoNorte.getId(), 33.0, 33.0, 55.0, 2.0, agora.minusDays(1));
        indicadores.registrar(milhoNorte.getId(), 52.0, 26.0, 60.0, 10.0, agora.minusHours(3));
        // Soja Sul: médio
        indicadores.registrar(sojaSul.getId(), 35.0, 33.0, 35.0, 3.0, agora.minusHours(5));
        // Café Encosta: baixo -> alto (gera alerta de mudança e de risco alto)
        indicadores.registrar(cafeEncosta.getId(), 38.0, 31.0, 50.0, 8.0, agora.minusDays(1));
        indicadores.registrar(cafeEncosta.getId(), 20.0, 39.5, 18.0, 0.0, agora.minusHours(2));
        // Milho Baixada: excesso de água (médio)
        indicadores.registrar(milhoBaixada.getId(), 88.0, 22.0, 85.0, 120.0, agora.minusHours(6));
        // Soja Platô: ideal (baixo)
        indicadores.registrar(sojaPlato.getId(), 60.0, 25.0, 70.0, 15.0, agora.minusHours(4));

        ocorrencias.registrar(cafeEncosta.getId(), TipoOcorrencia.FALTA_IRRIGACAO,
                MARCADOR + " Folhas murchas observadas na parte alta do talhão.", hoje);
        ocorrencias.registrar(sojaSul.getId(), TipoOcorrencia.PRAGA,
                MARCADOR + " Presença de lagartas em plantas da bordadura.", hoje.minusDays(1));
        ocorrencias.registrar(milhoBaixada.getId(), TipoOcorrencia.EXCESSO_AGUA,
                MARCADOR + " Acúmulo de água nas linhas de plantio após chuva forte.", hoje);
        return true;
    }
}
