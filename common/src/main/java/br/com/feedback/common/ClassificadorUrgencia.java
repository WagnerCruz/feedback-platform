package br.com.feedback.common;

/**
 * Regra de negócio única que converte a nota (0 a 10) em {@link Urgencia}.
 * <ul>
 *   <li>0 a 3  -> ALTA (crítica)</li>
 *   <li>4 a 6  -> MEDIA</li>
 *   <li>7 a 10 -> BAIXA</li>
 * </ul>
 */
public final class ClassificadorUrgencia {

    public static final int NOTA_MINIMA = 0;
    public static final int NOTA_MAXIMA = 10;
    public static final int LIMITE_ALTA = 3;
    public static final int LIMITE_MEDIA = 6;

    private ClassificadorUrgencia() {
    }

    public static Urgencia classificar(int nota) {
        if (nota < NOTA_MINIMA || nota > NOTA_MAXIMA) {
            throw new IllegalArgumentException("Nota deve estar entre 0 e 10: " + nota);
        }
        if (nota <= LIMITE_ALTA) {
            return Urgencia.ALTA;
        }
        if (nota <= LIMITE_MEDIA) {
            return Urgencia.MEDIA;
        }
        return Urgencia.BAIXA;
    }
}
