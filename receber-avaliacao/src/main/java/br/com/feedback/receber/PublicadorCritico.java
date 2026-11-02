package br.com.feedback.receber;

import br.com.feedback.common.Avaliacao;

/** Porta para publicar avaliações críticas (desacopla recebimento de notificação). */
public interface PublicadorCritico {
    void publicar(Avaliacao avaliacao);
}
