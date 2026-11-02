package br.com.feedback.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ClassificadorUrgenciaTest {

    @ParameterizedTest
    @CsvSource({"0,ALTA", "3,ALTA", "4,MEDIA", "6,MEDIA", "7,BAIXA", "10,BAIXA"})
    void classificaPorFaixaDeNota(int nota, Urgencia esperada) {
        assertEquals(esperada, ClassificadorUrgencia.classificar(nota));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 11, 100})
    void rejeitaNotaForaDoIntervalo(int nota) {
        assertThrows(IllegalArgumentException.class, () -> ClassificadorUrgencia.classificar(nota));
    }

    @Test
    void novaAvaliacaoCalculaUrgenciaEId() {
        Avaliacao a = Avaliacao.nova("Conteúdo confuso", 2, java.time.Instant.parse("2026-11-02T10:00:00Z"));
        assertEquals(Urgencia.ALTA, a.urgencia());
        assertEquals(36, a.id().length());
    }
}
