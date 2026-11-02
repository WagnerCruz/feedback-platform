package br.com.feedback.notificar;

import br.com.feedback.common.Avaliacao;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/** Monta o conteúdo do e-mail de urgência (descrição, urgência e data de envio). */
public final class MontadorEmailCritico {

    private static final DateTimeFormatter DATA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").withZone(ZoneId.of("America/Sao_Paulo"));

    private MontadorEmailCritico() {
    }

    public static String assunto(Avaliacao a) {
        return "[URGENTE] Nova avaliação crítica (nota " + a.nota() + ")";
    }

    public static String corpo(Avaliacao a) {
        return "Uma avaliação crítica foi recebida.\n\n"
                + "Descrição: " + a.descricao() + "\n"
                + "Urgência: " + a.urgencia() + "\n"
                + "Data de envio: " + DATA_HORA.format(a.dataEnvio()) + " (horário de Brasília)\n";
    }
}
