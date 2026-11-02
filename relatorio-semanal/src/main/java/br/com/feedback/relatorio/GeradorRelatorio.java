package br.com.feedback.relatorio;

import br.com.feedback.common.Avaliacao;
import br.com.feedback.common.Urgencia;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Cálculo puro (sem I/O) das métricas do relatório: média, quantidade por dia e por urgência. */
public class GeradorRelatorio {

    public static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");

    /** @param fim limite exclusivo do período */
    public RelatorioSemanal gerar(List<Avaliacao> avaliacoes, Instant inicio, Instant fim) {
        Map<LocalDate, Long> porDia = new TreeMap<>();
        LocalDate ultimoDia = fim.minusMillis(1).atZone(FUSO).toLocalDate();
        for (LocalDate d = inicio.atZone(FUSO).toLocalDate(); !d.isAfter(ultimoDia); d = d.plusDays(1)) {
            porDia.put(d, 0L);
        }
        Map<Urgencia, Long> porUrgencia = new EnumMap<>(Urgencia.class);
        for (Urgencia u : Urgencia.values()) {
            porUrgencia.put(u, 0L);
        }

        double soma = 0;
        for (Avaliacao a : avaliacoes) {
            porDia.merge(a.dataEnvio().atZone(FUSO).toLocalDate(), 1L, Long::sum);
            porUrgencia.merge(a.urgencia(), 1L, Long::sum);
            soma += a.nota();
        }
        double media = avaliacoes.isEmpty() ? 0 : Math.round(soma / avaliacoes.size() * 100.0) / 100.0;
        return new RelatorioSemanal(inicio, fim, avaliacoes.size(), media, porDia, porUrgencia,
                List.copyOf(avaliacoes));
    }
}
