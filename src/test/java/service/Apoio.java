package service;

import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/** Utilidades compartilhadas pelos testes. */
public final class Apoio {

    public static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");
    /** 17/11/2026 12:00 no horário de Brasília. */
    public static final Clock RELOGIO = Clock.fixed(Instant.parse("2026-11-17T15:00:00Z"), FUSO);
    public static final LocalDateTime AGORA = LocalDateTime.now(RELOGIO);

    private Apoio() {
    }

    public static ServicosSMField servicos(Path pasta) {
        return new ServicosSMField(pasta, RELOGIO);
    }
}
