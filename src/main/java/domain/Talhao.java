package domain;

import java.util.Objects;

/**
 * Área agrícola (talhão) pertencente a uma propriedade (RF-02, RN-01).
 */
public class Talhao implements Identificavel {

    private long id;
    private long propriedadeId;
    private String nome;
    private double areaHectares;
    private String cultura;

    public Talhao(long propriedadeId, String nome, Double areaHectares, String cultura) {
        this(0, propriedadeId, nome, areaHectares, cultura);
    }

    public Talhao(long id, long propriedadeId, String nome, Double areaHectares, String cultura) {
        if (propriedadeId <= 0) {
            throw new ValidacaoException("O talhão deve estar vinculado a uma propriedade cadastrada.");
        }
        this.id = id;
        this.propriedadeId = propriedadeId;
        this.nome = Validacao.textoObrigatorio(nome, "Nome do talhão");
        this.areaHectares = validarArea(areaHectares);
        this.cultura = Validacao.textoObrigatorio(cultura, "Cultura");
    }

    /** RF-09: altera nome, área e cultura com as mesmas validações do cadastro. */
    public void alterar(String novoNome, Double novaArea, String novaCultura) {
        String n = Validacao.textoObrigatorio(novoNome, "Nome do talhão");
        double a = validarArea(novaArea);
        String c = Validacao.textoObrigatorio(novaCultura, "Cultura");
        this.nome = n;
        this.areaHectares = a;
        this.cultura = c;
    }

    private static double validarArea(Double area) {
        double a = Validacao.numeroFinito(area, "Área (ha)");
        if (a <= 0) {
            throw new ValidacaoException("A área do talhão deve ser maior que zero.");
        }
        return a;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void setId(long id) {
        this.id = id;
    }

    public long getPropriedadeId() {
        return propriedadeId;
    }

    public String getNome() {
        return nome;
    }

    public double getAreaHectares() {
        return areaHectares;
    }

    public String getCultura() {
        return cultura;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Talhao t && t.id == id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return nome + " (" + cultura + ")";
    }
}
