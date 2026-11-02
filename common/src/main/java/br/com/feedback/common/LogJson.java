package br.com.feedback.common;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Log estruturado em JSON (uma linha por evento) para facilitar consultas no CloudWatch Logs Insights.
 * Regra de segurança: nunca registrar o texto da descrição do feedback, apenas identificadores e metadados.
 */
public final class LogJson {

    private LogJson() {
    }

    public static void info(String evento, Map<String, Object> campos) {
        escrever("INFO", evento, campos, null);
    }

    public static void erro(String evento, Map<String, Object> campos, Throwable causa) {
        escrever("ERROR", evento, campos, causa);
    }

    private static void escrever(String nivel, String evento, Map<String, Object> campos, Throwable causa) {
        Map<String, Object> linha = new LinkedHashMap<>();
        linha.put("timestamp", Instant.now().toString());
        linha.put("nivel", nivel);
        linha.put("evento", evento);
        if (campos != null) {
            linha.putAll(campos);
        }
        if (causa != null) {
            linha.put("excecao", causa.toString());
        }
        System.out.println(JsonSupport.toJson(linha));
    }
}
