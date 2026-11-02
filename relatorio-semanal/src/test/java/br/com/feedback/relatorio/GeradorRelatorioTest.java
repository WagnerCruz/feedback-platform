package br.com.feedback.relatorio;

import br.com.feedback.common.Avaliacao;
import br.com.feedback.common.Urgencia;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeradorRelatorioTest {

    // Semana de 26/10/2026 (seg) a 01/11/2026 (dom), meia-noite de Brasília (UTC-3).
    private static final Instant INICIO = Instant.parse("2026-10-26T03:00:00Z");
    private static final Instant FIM = Instant.parse("2026-11-02T03:00:00Z");

    private static Avaliacao av(String id, int nota, Urgencia u, String data) {
        return new Avaliacao(id, "desc " + id, nota, u, Instant.parse(data));
    }

    @Test
    void calculaMediaEContagens() {
        List<Avaliacao> lista = List.of(
                av("1", 2, Urgencia.ALTA, "2026-10-26T15:00:00Z"),
                av("2", 8, Urgencia.BAIXA, "2026-10-26T18:00:00Z"),
                av("3", 5, Urgencia.MEDIA, "2026-10-28T12:00:00Z"));

        RelatorioSemanal r = new GeradorRelatorio().gerar(lista, INICIO, FIM);

        assertEquals(3, r.total());
        assertEquals(5.0, r.media());
        assertEquals(7, r.porDia().size());
        assertEquals(2L, r.porDia().get(LocalDate.of(2026, 10, 26)));
        assertEquals(1L, r.porDia().get(LocalDate.of(2026, 10, 28)));
        assertEquals(0L, r.porDia().get(LocalDate.of(2026, 10, 30)));
        assertEquals(1L, r.porUrgencia().get(Urgencia.ALTA));
        assertEquals(1L, r.porUrgencia().get(Urgencia.MEDIA));
        assertEquals(1L, r.porUrgencia().get(Urgencia.BAIXA));
    }

    @Test
    void semAvaliacoesMediaZero() {
        RelatorioSemanal r = new GeradorRelatorio().gerar(List.of(), INICIO, FIM);
        assertEquals(0, r.total());
        assertEquals(0.0, r.media());
        assertEquals(7, r.porDia().size());
    }

    @Test
    void agrupaPorDiaNoFusoDeBrasilia() {
        // 27/10 01:30 UTC ainda é 26/10 22:30 em Brasília
        RelatorioSemanal r = new GeradorRelatorio().gerar(
                List.of(av("1", 9, Urgencia.BAIXA, "2026-10-27T01:30:00Z")), INICIO, FIM);
        assertEquals(1L, r.porDia().get(LocalDate.of(2026, 10, 26)));
    }

    @Test
    void htmlEscapaConteudoDoUsuario() {
        RelatorioSemanal r = new GeradorRelatorio().gerar(
                List.of(av("1", 1, Urgencia.ALTA, "2026-10-27T12:00:00Z")).stream()
                        .map(a -> new Avaliacao(a.id(), "<script>x</script>", a.nota(), a.urgencia(), a.dataEnvio()))
                        .toList(), INICIO, FIM);
        String html = new RelatorioRenderer().html(r);
        assertFalse(html.contains("<script>"));
        assertTrue(html.contains("&lt;script&gt;"));
        assertTrue(new RelatorioRenderer().texto(r).contains("Média das notas: 1.00"));
    }
}
