package br.com.feedback.receber;

import br.com.feedback.common.Avaliacao;
import br.com.feedback.common.AvaliacaoRepository;
import br.com.feedback.common.Urgencia;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReceberAvaliacaoHandlerTest {

    private final List<Avaliacao> salvas = new ArrayList<>();
    private final List<Avaliacao> publicadas = new ArrayList<>();
    private ReceberAvaliacaoHandler handler;

    @BeforeEach
    void preparar() {
        AvaliacaoRepository repo = new AvaliacaoRepository() {
            @Override public void salvar(Avaliacao a) { salvas.add(a); }
            @Override public List<Avaliacao> buscarPorPeriodo(Instant i, Instant f) { return List.of(); }
        };
        Clock relogio = Clock.fixed(Instant.parse("2026-11-02T10:00:00Z"), ZoneOffset.UTC);
        handler = new ReceberAvaliacaoHandler(repo, publicadas::add, relogio);
    }

    private APIGatewayProxyResponseEvent enviar(String corpo) {
        return handler.handleRequest(new APIGatewayProxyRequestEvent().withBody(corpo), null);
    }

    @Test
    void notaCriticaSalvaEPublica() {
        APIGatewayProxyResponseEvent resposta = enviar("{\"descricao\":\"Aulas ruins\",\"nota\":2}");
        assertEquals(201, resposta.getStatusCode());
        assertEquals(1, salvas.size());
        assertEquals(Urgencia.ALTA, salvas.get(0).urgencia());
        assertEquals(1, publicadas.size());
        assertTrue(resposta.getBody().contains("ALTA"));
    }

    @Test
    void notaBoaSalvaSemPublicar() {
        APIGatewayProxyResponseEvent resposta = enviar("{\"descricao\":\"Excelente\",\"nota\":9}");
        assertEquals(201, resposta.getStatusCode());
        assertEquals(1, salvas.size());
        assertTrue(publicadas.isEmpty());
    }

    @Test
    void payloadInvalidoRetorna400SemPersistir() {
        APIGatewayProxyResponseEvent resposta = enviar("{\"descricao\":\"x\",\"nota\":42}");
        assertEquals(400, resposta.getStatusCode());
        assertTrue(salvas.isEmpty());
        assertTrue(publicadas.isEmpty());
    }
}
