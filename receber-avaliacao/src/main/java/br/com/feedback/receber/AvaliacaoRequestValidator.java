package br.com.feedback.receber;

import br.com.feedback.common.ClassificadorUrgencia;
import br.com.feedback.common.JsonSupport;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

/** Valida o corpo JSON: {"descricao": string, "nota": int (0 a 10)}. */
public final class AvaliacaoRequestValidator {

    public static final int TAMANHO_MAXIMO_DESCRICAO = 2000;

    private AvaliacaoRequestValidator() {
    }

    public static AvaliacaoRequest validar(String corpo) {
        if (corpo == null || corpo.isBlank()) {
            throw new ValidacaoException("O corpo da requisição é obrigatório.");
        }
        JsonNode raiz;
        try {
            raiz = JsonSupport.MAPPER.readTree(corpo);
        } catch (JsonProcessingException e) {
            throw new ValidacaoException("JSON inválido.");
        }
        if (raiz == null || !raiz.isObject()) {
            throw new ValidacaoException("O corpo deve ser um objeto JSON.");
        }

        JsonNode descricao = raiz.get("descricao");
        if (descricao == null || !descricao.isTextual() || descricao.asText().isBlank()) {
            throw new ValidacaoException("O campo 'descricao' é obrigatório e deve ser um texto.");
        }
        if (descricao.asText().length() > TAMANHO_MAXIMO_DESCRICAO) {
            throw new ValidacaoException("O campo 'descricao' excede " + TAMANHO_MAXIMO_DESCRICAO + " caracteres.");
        }

        JsonNode nota = raiz.get("nota");
        if (nota == null || !nota.isIntegralNumber() || !nota.canConvertToInt()) {
            throw new ValidacaoException("O campo 'nota' é obrigatório e deve ser um número inteiro.");
        }
        int valor = nota.intValue();
        if (valor < ClassificadorUrgencia.NOTA_MINIMA || valor > ClassificadorUrgencia.NOTA_MAXIMA) {
            throw new ValidacaoException("O campo 'nota' deve estar entre 0 e 10.");
        }
        return new AvaliacaoRequest(descricao.asText().trim(), valor);
    }
}
