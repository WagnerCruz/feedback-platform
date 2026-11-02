package br.com.feedback.notificar;

import br.com.feedback.common.Avaliacao;
import br.com.feedback.common.JsonSupport;
import br.com.feedback.common.Urgencia;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotificarCriticoHandlerTest {

    private static final Avaliacao CRITICA = new Avaliacao(
            "id-1", "Plataforma fora do ar", 1, Urgencia.ALTA, Instant.parse("2026-11-02T13:30:00.000Z"));

    @Test
    void emailContemDescricaoUrgenciaEData() {
        String corpo = MontadorEmailCritico.corpo(CRITICA);
        assertTrue(corpo.contains("Plataforma fora do ar"));
        assertTrue(corpo.contains("ALTA"));
        assertTrue(corpo.contains("02/11/2026 10:30:00"));   // UTC-3
        assertTrue(MontadorEmailCritico.assunto(CRITICA).contains("URGENTE"));
    }

    @Test
    void enviaUmEmailPorRegistroSns() {
        List<String> assuntos = new ArrayList<>();
        NotificarCriticoHandler handler = new NotificarCriticoHandler((assunto, texto, html) -> assuntos.add(assunto));

        SNSEvent.SNS sns = new SNSEvent.SNS();
        sns.setMessage(JsonSupport.toJson(CRITICA));
        SNSEvent.SNSRecord registro = new SNSEvent.SNSRecord();
        registro.setSns(sns);
        SNSEvent evento = new SNSEvent();
        evento.setRecords(List.of(registro));

        handler.handleRequest(evento, null);
        assertEquals(1, assuntos.size());
    }
}
