package infra;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import domain.Identificavel;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Repositório genérico persistido em um arquivo JSON local.
 * <p>
 * Formato do arquivo: {@code {"proximoId": n, "itens": [ ... ]}}. O identificador é sequencial e
 * nunca é reutilizado, mesmo após exclusões. Cada alteração grava o arquivo inteiro em um arquivo
 * temporário e o substitui atomicamente, reduzindo o risco de corrupção.
 *
 * @param <T> tipo da entidade
 */
public abstract class JsonRepository<T extends Identificavel> {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();

    private final Path arquivo;
    private final Type tipoArquivo;
    private final List<T> itens = new ArrayList<>();
    private long proximoId = 1;

    protected JsonRepository(Path arquivo, Class<T> classe) {
        this.arquivo = arquivo;
        this.tipoArquivo = TypeToken.getParameterized(Conteudo.class, classe).getType();
        carregar();
    }

    /** Insere (id = 0) ou atualiza (id existente) a entidade e grava o arquivo. */
    public synchronized T salvar(T entidade) {
        if (entidade.getId() <= 0) {
            entidade.setId(proximoId++);
            itens.add(entidade);
        } else {
            int pos = posicao(entidade.getId());
            if (pos >= 0) {
                itens.set(pos, entidade);
            } else {
                itens.add(entidade);
                proximoId = Math.max(proximoId, entidade.getId() + 1);
            }
        }
        gravar();
        return entidade;
    }

    public synchronized Optional<T> buscarPorId(long id) {
        int pos = posicao(id);
        return pos >= 0 ? Optional.of(itens.get(pos)) : Optional.empty();
    }

    public synchronized boolean existe(long id) {
        return posicao(id) >= 0;
    }

    /** Lista todos os itens em ordem de identificador. */
    public synchronized List<T> listar() {
        List<T> copia = new ArrayList<>(itens);
        copia.sort(Comparator.comparingLong(Identificavel::getId));
        return copia;
    }

    public synchronized List<T> filtrar(Predicate<T> criterio) {
        return listar().stream().filter(criterio).toList();
    }

    public synchronized boolean remover(long id) {
        boolean removido = itens.removeIf(i -> i.getId() == id);
        if (removido) {
            gravar();
        }
        return removido;
    }

    /** Remove todos os itens que atendem ao critério e devolve a quantidade removida. */
    public synchronized int removerSe(Predicate<T> criterio) {
        int antes = itens.size();
        itens.removeIf(criterio);
        int removidos = antes - itens.size();
        if (removidos > 0) {
            gravar();
        }
        return removidos;
    }

    public synchronized int contar() {
        return itens.size();
    }

    public Path getArquivo() {
        return arquivo;
    }

    private int posicao(long id) {
        for (int i = 0; i < itens.size(); i++) {
            if (itens.get(i).getId() == id) {
                return i;
            }
        }
        return -1;
    }

    private void carregar() {
        if (!Files.exists(arquivo)) {
            return;
        }
        try (Reader reader = Files.newBufferedReader(arquivo, StandardCharsets.UTF_8)) {
            Conteudo<T> conteudo = GSON.fromJson(reader, tipoArquivo);
            if (conteudo != null && conteudo.itens != null) {
                itens.addAll(conteudo.itens);
                long maiorId = itens.stream().mapToLong(Identificavel::getId).max().orElse(0);
                proximoId = Math.max(conteudo.proximoId, maiorId + 1);
            }
        } catch (IOException | JsonParseException e) {
            throw new PersistenciaException("Não foi possível ler o arquivo de dados " + arquivo
                    + ". Verifique se ele não foi alterado manualmente.", e);
        }
    }

    private void gravar() {
        try {
            Path pasta = arquivo.toAbsolutePath().getParent();
            Files.createDirectories(pasta);
            Path temporario = Files.createTempFile(pasta, arquivo.getFileName().toString(), ".tmp");
            Conteudo<T> conteudo = new Conteudo<>();
            conteudo.proximoId = proximoId;
            conteudo.itens = listar();
            try (Writer writer = Files.newBufferedWriter(temporario, StandardCharsets.UTF_8)) {
                GSON.toJson(conteudo, tipoArquivo, writer);
            }
            Files.move(temporario, arquivo, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            throw new PersistenciaException("Não foi possível gravar o arquivo de dados " + arquivo + ".", e);
        }
    }

    /** Estrutura gravada em disco. */
    static final class Conteudo<E> {
        long proximoId = 1;
        List<E> itens = new ArrayList<>();
    }

    private static final class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
        @Override
        public void write(JsonWriter out, LocalDateTime value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(value.toString());
            }
        }

        @Override
        public LocalDateTime read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            return LocalDateTime.parse(in.nextString());
        }
    }

    private static final class LocalDateAdapter extends TypeAdapter<LocalDate> {
        @Override
        public void write(JsonWriter out, LocalDate value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(value.toString());
            }
        }

        @Override
        public LocalDate read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            return LocalDate.parse(in.nextString());
        }
    }
}
