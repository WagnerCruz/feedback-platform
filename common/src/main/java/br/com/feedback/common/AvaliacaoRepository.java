package br.com.feedback.common;

import java.time.Instant;
import java.util.List;

/** Porta de persistência de avaliações. */
public interface AvaliacaoRepository {

    /** Persiste uma nova avaliação. */
    void salvar(Avaliacao avaliacao);

    /** Busca avaliações com dataEnvio no intervalo [inicio, fim] (inclusivo), ordenadas por data. */
    List<Avaliacao> buscarPorPeriodo(Instant inicio, Instant fim);
}
