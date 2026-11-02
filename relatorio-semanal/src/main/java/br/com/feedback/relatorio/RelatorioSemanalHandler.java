package br.com.feedback.relatorio;

import br.com.feedback.common.Ambiente;
import br.com.feedback.common.Avaliacao;
import br.com.feedback.common.AvaliacaoRepository;
import br.com.feedback.common.Clientes;
import br.com.feedback.common.DynamoAvaliacaoRepository;
import br.com.feedback.common.EmailService;
import br.com.feedback.common.LogJson;
import br.com.feedback.common.SesEmailService;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

/**
 * Função 3 - Gerar relatório semanal.
 * <p>
 * Acionada por regra agendada do EventBridge (segundas, 08:00 de Brasília). Única responsabilidade:
 * consultar os últimos 7 dias completos, consolidar as métricas e enviar o relatório por e-mail.
 */
public class RelatorioSemanalHandler implements RequestHandler<ScheduledEvent, String> {

    private final AvaliacaoRepository repositorio;
    private final EmailService email;
    private final Clock relogio;
    private final GeradorRelatorio gerador = new GeradorRelatorio();
    private final RelatorioRenderer renderer = new RelatorioRenderer();

    /** Construtor usado pelo runtime do Lambda. */
    public RelatorioSemanalHandler() {
        this(new DynamoAvaliacaoRepository(Clientes.dynamo(), Ambiente.exigir("TABELA_AVALIACOES")),
                new SesEmailService(Clientes.ses(),
                        Ambiente.exigir("EMAIL_REMETENTE"),
                        SesEmailService.listar(Ambiente.exigir("EMAILS_ADMIN"))),
                Clock.systemUTC());
    }

    /** Construtor para testes. */
    public RelatorioSemanalHandler(AvaliacaoRepository repositorio, EmailService email, Clock relogio) {
        this.repositorio = repositorio;
        this.email = email;
        this.relogio = relogio;
    }

    /**
     * Instante usado como "agora". Por padrão é o relógio do sistema; para demonstrações/reprocessamento é
     * possível invocar manualmente com {"detail": {"referencia": "2026-11-09T11:00:00Z"}}.
     */
    private Instant referencia(ScheduledEvent evento) {
        if (evento != null && evento.getDetail() != null && evento.getDetail().get("referencia") instanceof String texto) {
            return Instant.parse(texto);
        }
        return relogio.instant();
    }

    @Override
    public String handleRequest(ScheduledEvent evento, Context contexto) {
        String requestId = contexto != null ? contexto.getAwsRequestId() : "local";

        // Janela: 7 dias completos anteriores à meia-noite (Brasília) de hoje. Fim é exclusivo.
        Instant fim = referencia(evento).atZone(GeradorRelatorio.FUSO).toLocalDate()
                .atStartOfDay(GeradorRelatorio.FUSO).toInstant();
        Instant inicio = fim.minus(7, ChronoUnit.DAYS);

        List<Avaliacao> avaliacoes = repositorio.buscarPorPeriodo(inicio, fim.minusMillis(1));
        RelatorioSemanal relatorio = gerador.gerar(avaliacoes, inicio, fim);
        email.enviar(renderer.assunto(relatorio), renderer.texto(relatorio), renderer.html(relatorio));

        LogJson.info("relatorio_semanal_enviado", Map.of(
                "requestId", requestId,
                "total", relatorio.total(),
                "media", relatorio.media()));
        return "Relatório enviado: " + relatorio.total() + " avaliações, média " + relatorio.media();
    }
}
