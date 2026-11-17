package domain;

import java.util.Objects;

/**
 * Propriedade rural monitorada pelo SMField (RF-01).
 * Classe original do repositório, corrigida (pacote) e ampliada com identificador e validações.
 */
public class Propriedade implements Identificavel {

    private long id;
    private String nome;
    private String municipio;

    public Propriedade(String nome, String municipio) {
        this(0, nome, municipio);
    }

    public Propriedade(long id, String nome, String municipio) {
        this.id = id;
        this.nome = Validacao.textoObrigatorio(nome, "Nome da propriedade");
        this.municipio = Validacao.textoObrigatorio(municipio, "Município");
    }

    /** Altera os dados cadastrais, mantendo as mesmas validações do cadastro. */
    public void alterar(String novoNome, String novoMunicipio) {
        String n = Validacao.textoObrigatorio(novoNome, "Nome da propriedade");
        String m = Validacao.textoObrigatorio(novoMunicipio, "Município");
        this.nome = n;
        this.municipio = m;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void setId(long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public String getMunicipio() {
        return municipio;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Propriedade p && p.id == id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return nome + " — " + municipio;
    }
}
