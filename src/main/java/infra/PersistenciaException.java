package infra;

/** Falha de leitura ou gravação dos arquivos de dados locais. */
public class PersistenciaException extends RuntimeException {

    public PersistenciaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
