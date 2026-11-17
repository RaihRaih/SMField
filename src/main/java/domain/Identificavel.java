package domain;

/**
 * Contrato para entidades persistidas que possuem identificador numérico único.
 */
public interface Identificavel {

    long getId();

    void setId(long id);
}
