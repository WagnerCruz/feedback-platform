package br.com.feedback.common;

/** Acesso a variáveis de ambiente obrigatórias (configuração das funções). */
public final class Ambiente {

    private Ambiente() {
    }

    public static String exigir(String nome) {
        String valor = System.getenv(nome);
        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException("Variável de ambiente obrigatória ausente: " + nome);
        }
        return valor;
    }
}
