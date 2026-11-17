package domain;

/**
 * Exceção lançada quando um dado informado viola uma regra de validação ou de negócio.
 * A mensagem é escrita para ser exibida diretamente ao usuário.
 */
public class ValidacaoException extends IllegalArgumentException {

    public ValidacaoException(String mensagem) {
        super(mensagem);
    }
}
