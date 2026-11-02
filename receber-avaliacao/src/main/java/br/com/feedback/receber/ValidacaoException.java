package br.com.feedback.receber;

/** Erro de validação do payload de entrada (resulta em HTTP 400). */
public class ValidacaoException extends RuntimeException {
    public ValidacaoException(String mensagem) {
        super(mensagem);
    }
}
