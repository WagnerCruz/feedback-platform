package br.com.feedback.relatorio;

import br.com.feedback.common.Avaliacao;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Converte o {@link RelatorioSemanal} em texto puro e HTML para o corpo do e-mail. */
public class RelatorioRenderer {

    static final int MAX_ITENS = 200;
    private static final DateTimeFormatter DIA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").withZone(GeradorRelatorio.FUSO);

    public String assunto(RelatorioSemanal r) {
        return "Relatório semanal de avaliações (" + periodo(r) + ")";
    }

    public String texto(RelatorioSemanal r) {
        StringBuilder sb = new StringBuilder();
        sb.append("Relatório semanal de avaliações\nPeríodo: ").append(periodo(r)).append("\n\n");
        sb.append("Total de avaliações: ").append(r.total()).append('\n');
        sb.append("Média das notas: ").append(String.format(Locale.US, "%.2f", r.media())).append("\n\n");
        sb.append("Avaliações por dia:\n");
        r.porDia().forEach((dia, qtd) -> sb.append("  ").append(DIA.format(dia)).append(": ").append(qtd).append('\n'));
        sb.append("\nAvaliações por urgência:\n");
        r.porUrgencia().forEach((u, qtd) -> sb.append("  ").append(u).append(": ").append(qtd).append('\n'));
        sb.append("\nDetalhamento (descrição | urgência | data de envio):\n");
        for (Avaliacao a : limitadas(r)) {
            sb.append("  - ").append(a.descricao()).append(" | ").append(a.urgencia())
                    .append(" | ").append(DATA_HORA.format(a.dataEnvio())).append('\n');
        }
        if (r.avaliacoes().size() > MAX_ITENS) {
            sb.append("  ... e mais ").append(r.avaliacoes().size() - MAX_ITENS).append(" avaliações.\n");
        }
        return sb.toString();
    }

    public String html(RelatorioSemanal r) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style=\"font-family:Arial,sans-serif\">");
        sb.append("<h2>Relatório semanal de avaliações</h2><p>Período: ").append(periodo(r)).append("</p>");
        sb.append("<p><b>Total:</b> ").append(r.total()).append(" &nbsp; <b>Média das notas:</b> ")
                .append(String.format(Locale.US, "%.2f", r.media())).append("</p>");
        sb.append(tabela("Avaliações por dia", "Dia", r.porDia(), DIA::format));
        sb.append(tabela("Avaliações por urgência", "Urgência", r.porUrgencia(), Object::toString));
        sb.append("<h3>Detalhamento</h3><table border=\"1\" cellpadding=\"6\" cellspacing=\"0\">")
                .append("<tr><th>Descrição</th><th>Urgência</th><th>Data de envio</th></tr>");
        for (Avaliacao a : limitadas(r)) {
            sb.append("<tr><td>").append(escapar(a.descricao())).append("</td><td>").append(a.urgencia())
                    .append("</td><td>").append(DATA_HORA.format(a.dataEnvio())).append("</td></tr>");
        }
        sb.append("</table>");
        if (r.avaliacoes().size() > MAX_ITENS) {
            sb.append("<p>... e mais ").append(r.avaliacoes().size() - MAX_ITENS).append(" avaliações.</p>");
        }
        return sb.append("</body></html>").toString();
    }

    private static <K> String tabela(String titulo, String cabecalho, Map<K, Long> dados,
                                     java.util.function.Function<K, String> rotulo) {
        StringBuilder sb = new StringBuilder("<h3>" + titulo + "</h3>"
                + "<table border=\"1\" cellpadding=\"6\" cellspacing=\"0\"><tr><th>" + cabecalho + "</th><th>Quantidade</th></tr>");
        dados.forEach((k, v) -> sb.append("<tr><td>").append(rotulo.apply(k)).append("</td><td>").append(v).append("</td></tr>"));
        return sb.append("</table>").toString();
    }

    private static List<Avaliacao> limitadas(RelatorioSemanal r) {
        return r.avaliacoes().size() > MAX_ITENS ? r.avaliacoes().subList(0, MAX_ITENS) : r.avaliacoes();
    }

    private static String periodo(RelatorioSemanal r) {
        LocalDate ini = r.inicio().atZone(GeradorRelatorio.FUSO).toLocalDate();
        LocalDate fim = r.fim().minusMillis(1).atZone(GeradorRelatorio.FUSO).toLocalDate();
        return DIA.format(ini) + " a " + DIA.format(fim);
    }

    static String escapar(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
