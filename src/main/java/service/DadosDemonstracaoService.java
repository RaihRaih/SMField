package service;

import domain.Propriedade;
import domain.Talhao;
import domain.TipoOcorrencia;

import java.time.LocalDate;

/**
 * Carrega a demonstração do SMField em três polos reais do agronegócio, com <b>clima real do INMET</b>:
 * <ul>
 *   <li>Sorriso/MT (estação A904) — soja e milho safrinha;</li>
 *   <li>Rio Verde/GO (estação A025) — soja e milho safrinha;</li>
 *   <li>Patrocínio/MG (estação A523) — café do Cerrado Mineiro.</li>
 * </ul>
 * Os municípios e os dados climáticos (temperatura, umidade do ar e chuva) são reais. As propriedades,
 * os talhões e as ocorrências são <b>fictícios</b> (prefixo {@value #MARCADOR}) e a umidade do solo é
 * <b>simulada</b>. A carga é idempotente: não duplica dados se já tiver sido feita.
 */
public class DadosDemonstracaoService {

    public static final String MARCADOR = "[DEMO]";

    private final PropriedadeService propriedades;
    private final TalhaoService talhoes;
    private final OcorrenciaService ocorrencias;
    private final InmetService inmet;

    public DadosDemonstracaoService(PropriedadeService propriedades, TalhaoService talhoes,
                                    OcorrenciaService ocorrencias, InmetService inmet) {
        this.propriedades = propriedades;
        this.talhoes = talhoes;
        this.ocorrencias = ocorrencias;
        this.inmet = inmet;
    }

    public boolean jaCarregado() {
        return propriedades.listar().stream().anyMatch(p -> p.getNome().startsWith(MARCADOR));
    }

    /** @return {@code true} se os dados foram carregados; {@code false} se já existiam */
    public boolean carregar() {
        if (jaCarregado()) {
            return false;
        }
        // Sorriso/MT — maior produtor de soja do país
        Propriedade sorriso = propriedades.cadastrar(MARCADOR + " Fazenda Boa Esperança", "Sorriso/MT");
        Talhao sojaSorriso = talhoes.cadastrar(sorriso.getId(), "Talhão Soja 26/27", 120.0, "Soja");
        Talhao milhoSorriso = talhoes.cadastrar(sorriso.getId(), "Talhão Milho Safrinha", 95.0, "Milho");
        inmet.importar(sojaSorriso.getId(), "A904", data(10, 1), data(10, 31));     // plantio com a volta das chuvas
        inmet.importar(milhoSorriso.getId(), "A904", data(4, 1), data(6, 30));      // safrinha até a colheita

        // Rio Verde/GO — polo de grãos do sudoeste goiano
        Propriedade rioVerde = propriedades.cadastrar(MARCADOR + " Fazenda Santa Helena", "Rio Verde/GO");
        Talhao sojaRioVerde = talhoes.cadastrar(rioVerde.getId(), "Talhão Soja", 80.0, "Soja");
        Talhao milhoRioVerde = talhoes.cadastrar(rioVerde.getId(), "Talhão Milho Safrinha", 60.0, "Milho");
        inmet.importar(sojaRioVerde.getId(), "A025", data(8, 1), data(10, 31));     // seca até as chuvas de outubro
        inmet.importar(milhoRioVerde.getId(), "A025", data(6, 1), data(8, 31));     // fim de ciclo na seca

        // Patrocínio/MG — café do Cerrado Mineiro
        Propriedade patrocinio = propriedades.cadastrar(MARCADOR + " Sítio Cerrado Mineiro", "Patrocínio/MG");
        Talhao cafe = talhoes.cadastrar(patrocinio.getId(), "Talhão Café Catuaí", 15.0, "Café");
        talhoes.cadastrar(patrocinio.getId(), "Talhão Café Novo", 5.0, "Café"); // sem indicadores (NÃO CALCULADO)
        inmet.importar(cafe.getId(), "A523", data(8, 1), data(10, 31));              // seca até a florada

        ocorrencias.registrar(milhoRioVerde.getId(), TipoOcorrencia.FALTA_IRRIGACAO,
                MARCADOR + " Estresse hídrico no fim do ciclo: folhas enroladas nas horas mais quentes.", data(8, 28));
        ocorrencias.registrar(sojaRioVerde.getId(), TipoOcorrencia.EXCESSO_AGUA,
                MARCADOR + " Pontos de encharcamento nas linhas após chuva forte.", data(10, 31));
        ocorrencias.registrar(cafe.getId(), TipoOcorrencia.PRAGA,
                MARCADOR + " Minas de bicho-mineiro em folhas do terço superior.", data(10, 20));
        return true;
    }

    private static LocalDate data(int mes, int dia) {
        return LocalDate.of(2026, mes, dia);
    }
}
