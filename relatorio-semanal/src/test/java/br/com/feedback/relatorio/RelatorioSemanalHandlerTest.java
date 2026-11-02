package br.com.feedback.relatorio;

import br.com.feedback.common.Avaliacao;
import br.com.feedback.common.AvaliacaoRepository;
import br.com.feedback.common.Urgencia;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RelatorioSemanalHandlerTest {

    @Test
    void consultaJanelaDeSeteDiasEEnviaEmail() {
        List<Instant[]> consultas = new ArrayList<>();
        List<String> assuntos = new ArrayList<>();
        AvaliacaoRepository repo = new AvaliacaoRepository() {
            @Override public void salvar(Avaliacao a) { }
            @Override public List<Avaliacao> buscarPorPeriodo(Instant i, Instant f) {
                consultas.add(new Instant[]{i, f});
                return List.of(new Avaliacao("1", "ok", 8, Urgencia.BAIXA, Instant.parse("2026-10-28T12:00:00Z")));
            }
        };
        // Segunda 02/11/2026 11:00 UTC = 08:00 em Brasília
        Clock relogio = Clock.fixed(Instant.parse("2026-11-02T11:00:00Z"), ZoneOffset.UTC);
        RelatorioSemanalHandler handler = new RelatorioSemanalHandler(repo, (a, t, h) -> assuntos.add(a), relogio);

        String resumo = handler.handleRequest(null, null);

        assertEquals(Instant.parse("2026-10-26T03:00:00Z"), consultas.get(0)[0]);
        assertEquals(Instant.parse("2026-11-02T02:59:59.999Z"), consultas.get(0)[1]);
        assertEquals(1, assuntos.size());
        assertTrue(assuntos.get(0).contains("26/10/2026 a 01/11/2026"));
        assertTrue(resumo.contains("média 8.0"));
    }
}
