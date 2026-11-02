package br.com.feedback.relatorio;

import br.com.feedback.common.Avaliacao;
import br.com.feedback.common.Urgencia;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Resultado consolidado do período.
 *
 * @param inicio       início do período (inclusivo)
 * @param fim          fim do período (exclusivo)
 * @param total        quantidade de avaliações
 * @param media        média das notas (2 casas decimais)
 * @param porDia       quantidade de avaliações por dia (fuso de Brasília)
 * @param porUrgencia  quantidade de avaliações por urgência
 * @param avaliacoes   avaliações do período
 */
public record RelatorioSemanal(Instant inicio, Instant fim, int total, double media,
                               Map<LocalDate, Long> porDia, Map<Urgencia, Long> porUrgencia,
                               List<Avaliacao> avaliacoes) {
}
