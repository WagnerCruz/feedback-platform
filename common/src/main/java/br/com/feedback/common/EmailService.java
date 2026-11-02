package br.com.feedback.common;

/** Porta para envio de e-mails aos administradores. */
public interface EmailService {

    /**
     * @param assunto    assunto do e-mail
     * @param corpoTexto corpo em texto puro (obrigatório)
     * @param corpoHtml  corpo em HTML (opcional, pode ser null)
     */
    void enviar(String assunto, String corpoTexto, String corpoHtml);
}
