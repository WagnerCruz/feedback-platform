package br.com.feedback.common;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonSupportTest {

    @Test
    void serializaEDesserializaAvaliacao() {
        Avaliacao original = new Avaliacao("id-1", "Ótimo curso", 9, Urgencia.BAIXA,
                Instant.parse("2026-11-02T10:15:30.123Z"));
        String json = JsonSupport.toJson(original);
        assertTrue(json.contains("2026-11-02T10:15:30.123Z"));
        assertEquals(original, JsonSupport.fromJson(json, Avaliacao.class));
    }
}
