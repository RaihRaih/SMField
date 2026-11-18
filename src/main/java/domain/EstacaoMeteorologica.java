package domain;

/**
 * Estação meteorológica automática do INMET (dados reais, tratados pelo script
 * {@code ferramentas/inmet/tratar_inmet.py}).
 *
 * @param codigo       código INMET (ex.: A521)
 * @param nome         nome da estação (ex.: "Belo Horizonte - Pampulha")
 * @param uf           unidade da federação
 * @param regiao       região do país (N, NE, CO, SE, S)
 * @param latitude     latitude em graus decimais
 * @param longitude    longitude em graus decimais
 * @param altitudeM    altitude em metros
 * @param coberturaPct percentual de dias com dado no período
 */
public record EstacaoMeteorologica(String codigo, String nome, String uf, String regiao, double latitude,
                                   double longitude, double altitudeM, double coberturaPct) {

    /** Texto usado em listas e na identificação da fonte dos registros. */
    public String descricao() {
        return codigo + " — " + nome + "/" + uf;
    }

    @Override
    public String toString() {
        return descricao();
    }
}
