package br.com.feedback.receber;

/** Payload validado de POST /avaliacao. */
public record AvaliacaoRequest(String descricao, int nota) {
}
