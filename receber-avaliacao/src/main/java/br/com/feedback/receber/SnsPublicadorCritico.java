package br.com.feedback.receber;

import br.com.feedback.common.Avaliacao;
import br.com.feedback.common.JsonSupport;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

/** Publica a avaliação crítica (JSON) em um tópico SNS. */
public class SnsPublicadorCritico implements PublicadorCritico {

    private final SnsClient cliente;
    private final String topicoArn;

    public SnsPublicadorCritico(SnsClient cliente, String topicoArn) {
        this.cliente = cliente;
        this.topicoArn = topicoArn;
    }

    public static SnsPublicadorCritico criar(String topicoArn) {
        return new SnsPublicadorCritico(
                SnsClient.builder().httpClient(UrlConnectionHttpClient.create()).build(), topicoArn);
    }

    @Override
    public void publicar(Avaliacao avaliacao) {
        cliente.publish(PublishRequest.builder()
                .topicArn(topicoArn)
                .message(JsonSupport.toJson(avaliacao))
                .build());
    }
}
