package br.com.feedback.receber;

import br.com.feedback.common.Ambiente;
import br.com.feedback.common.Avaliacao;
import br.com.feedback.common.AvaliacaoRepository;
import br.com.feedback.common.Clientes;
import br.com.feedback.common.DynamoAvaliacaoRepository;
import br.com.feedback.common.JsonSupport;
import br.com.feedback.common.LogJson;
import br.com.feedback.common.Urgencia;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import java.time.Clock;
import java.util.Map;

/**
 * Função 1 - Receber avaliação (POST /avaliacao).
 * <p>
 * Única responsabilidade: validar, classificar, persistir e, se a urgência for ALTA, publicar o evento
 * crítico. O envio do e-mail NÃO é feito aqui (é responsabilidade da função notificar-critico).
 * <p>
 * Respostas: 201 (criada), 400 (payload inválido). Falhas de infraestrutura propagam exceção (HTTP 500)
 * e disparam o alarme de erros.
 */
public class ReceberAvaliacaoHandler
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final AvaliacaoRepository repositorio;
    private final PublicadorCritico publicador;
    private final Clock relogio;

    /** Construtor usado pelo runtime do Lambda. */
    public ReceberAvaliacaoHandler() {
        this(new DynamoAvaliacaoRepository(Clientes.dynamo(), Ambiente.exigir("TABELA_AVALIACOES")),
                SnsPublicadorCritico.criar(Ambiente.exigir("TOPICO_CRITICO_ARN")),
                Clock.systemUTC());
    }

    /** Construtor para testes (injeção de dependências). */
    public ReceberAvaliacaoHandler(AvaliacaoRepository repositorio, PublicadorCritico publicador, Clock relogio) {
        this.repositorio = repositorio;
        this.publicador = publicador;
        this.relogio = relogio;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent evento, Context contexto) {
        String requestId = contexto != null ? contexto.getAwsRequestId() : "local";
        try {
            AvaliacaoRequest requisicao = AvaliacaoRequestValidator.validar(evento.getBody());
            Avaliacao avaliacao = Avaliacao.nova(requisicao.descricao(), requisicao.nota(), relogio.instant());

            repositorio.salvar(avaliacao);
            if (avaliacao.urgencia() == Urgencia.ALTA) {
                publicador.publicar(avaliacao);
            }

            LogJson.info("avaliacao_recebida", Map.of(
                    "requestId", requestId,
                    "avaliacaoId", avaliacao.id(),
                    "nota", avaliacao.nota(),
                    "urgencia", avaliacao.urgencia().name()));
            return resposta(201, Map.of("id", avaliacao.id(), "urgencia", avaliacao.urgencia()));
        } catch (ValidacaoException e) {
            LogJson.info("avaliacao_invalida", Map.of("requestId", requestId, "motivo", e.getMessage()));
            return resposta(400, Map.of("erro", e.getMessage()));
        }
    }

    private static APIGatewayProxyResponseEvent resposta(int status, Object corpo) {
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(status)
                .withHeaders(Map.of("Content-Type", "application/json"))
                .withBody(JsonSupport.toJson(corpo));
    }
}
