package src.main.java.domain;

public class Propriedade {

    private String nome;
    private String municipio;

    public Propriedade(String nome, String municipio) {
        this.nome = nome;
        this.municipio = municipio;
    }

    public String getNome() {
        return nome; 
    }

    public String getMunicipio() {
        return municipio;
    }
}