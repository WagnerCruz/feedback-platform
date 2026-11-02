package br.com.feedback.receber;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AvaliacaoRequestValidatorTest {

    @Test
    void aceitaPayloadValido() {
        AvaliacaoRequest r = AvaliacaoRequestValidator.validar("{\"descricao\":\"  Bom curso \",\"nota\":8}");
        assertEquals("Bom curso", r.descricao());
        assertEquals(8, r.nota());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            "nao-e-json",
            "[]",
            "{\"nota\":5}",
            "{\"descricao\":\"\",\"nota\":5}",
            "{\"descricao\":\"x\"}",
            "{\"descricao\":\"x\",\"nota\":11}",
            "{\"descricao\":\"x\",\"nota\":-1}",
            "{\"descricao\":\"x\",\"nota\":5.5}",
            "{\"descricao\":\"x\",\"nota\":\"5\"}",
            "{\"descricao\":123,\"nota\":5}"
    })
    void rejeitaPayloadInvalido(String corpo) {
        assertThrows(ValidacaoException.class, () -> AvaliacaoRequestValidator.validar(corpo));
    }
}
