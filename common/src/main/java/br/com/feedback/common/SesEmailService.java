package br.com.feedback.common;

import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;

import java.util.Arrays;
import java.util.List;

/** Envio de e-mail via Amazon SES. O remetente e os destinatários devem estar verificados (sandbox). */
public class SesEmailService implements EmailService {

    private final SesClient cliente;
    private final String remetente;
    private final List<String> destinatarios;

    public SesEmailService(SesClient cliente, String remetente, List<String> destinatarios) {
        this.cliente = cliente;
        this.remetente = remetente;
        this.destinatarios = destinatarios;
    }

    /** Converte "a@x.com, b@y.com" em lista, ignorando espaços e vazios. */
    public static List<String> listar(String csv) {
        return Arrays.stream(csv.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
    }

    @Override
    public void enviar(String assunto, String corpoTexto, String corpoHtml) {
        Body.Builder corpo = Body.builder().text(conteudo(corpoTexto));
        if (corpoHtml != null) {
            corpo.html(conteudo(corpoHtml));
        }
        cliente.sendEmail(SendEmailRequest.builder()
                .source(remetente)
                .destination(Destination.builder().toAddresses(destinatarios).build())
                .message(Message.builder().subject(conteudo(assunto)).body(corpo.build()).build())
                .build());
    }

    private static Content conteudo(String dados) {
        return Content.builder().data(dados).charset("UTF-8").build();
    }
}
