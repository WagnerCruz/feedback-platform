package br.com.feedback.notificar;

import br.com.feedback.common.Ambiente;
import br.com.feedback.common.Avaliacao;
import br.com.feedback.common.Clientes;
import br.com.feedback.common.EmailService;
import br.com.feedback.common.JsonSupport;
import br.com.feedback.common.LogJson;
import br.com.feedback.common.SesEmailService;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;

import java.util.Map;

/**
 * Função 2 - Notificar crítico.
 * <p>
 * Acionada pelo tópico SNS "avaliacao-critica". Única responsabilidade: enviar o e-mail de urgência aos
 * administradores. Em caso de falha, a exceção é propagada: o Lambda refaz a tentativa e, esgotadas as
 * tentativas, a mensagem vai para a DLQ (que possui alarme).
 */
public class NotificarCriticoHandler implements RequestHandler<SNSEvent, Void> {

    private final EmailService email;

    /** Construtor usado pelo runtime do Lambda. */
    public NotificarCriticoHandler() {
        this(new SesEmailService(Clientes.ses(),
                Ambiente.exigir("EMAIL_REMETENTE"),
                SesEmailService.listar(Ambiente.exigir("EMAILS_ADMIN"))));
    }

    /** Construtor para testes. */
    public NotificarCriticoHandler(EmailService email) {
        this.email = email;
    }

    @Override
    public Void handleRequest(SNSEvent evento, Context contexto) {
        String requestId = contexto != null ? contexto.getAwsRequestId() : "local";
        for (SNSEvent.SNSRecord registro : evento.getRecords()) {
            Avaliacao avaliacao = JsonSupport.fromJson(registro.getSNS().getMessage(), Avaliacao.class);
            email.enviar(MontadorEmailCritico.assunto(avaliacao), MontadorEmailCritico.corpo(avaliacao), null);
            LogJson.info("email_critico_enviado", Map.of(
                    "requestId", requestId,
                    "avaliacaoId", avaliacao.id(),
                    "urgencia", avaliacao.urgencia().name()));
        }
        return null;
    }
}
