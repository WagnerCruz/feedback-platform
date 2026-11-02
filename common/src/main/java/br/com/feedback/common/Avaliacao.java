package br.com.feedback.common;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Avaliação (feedback) enviada por um estudante.
 *
 * @param id         identificador único (UUID)
 * @param descricao  texto livre do feedback
 * @param nota       nota inteira de 0 a 10
 * @param urgencia   urgência calculada a partir da nota
 * @param dataEnvio  instante do recebimento (UTC, precisão de milissegundos)
 */
public record Avaliacao(String id, String descricao, int nota, Urgencia urgencia, Instant dataEnvio) {

    /** Cria uma nova avaliação classificando a urgência e gerando o identificador. */
    public static Avaliacao nova(String descricao, int nota, Instant agora) {
        return new Avaliacao(
                UUID.randomUUID().toString(),
                descricao,
                nota,
                ClassificadorUrgencia.classificar(nota),
                agora.truncatedTo(ChronoUnit.MILLIS));
    }
}
