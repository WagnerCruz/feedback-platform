package br.com.feedback.common;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Implementação em DynamoDB.
 * <p>
 * Tabela: chave de partição {@code id}. Índice global {@code indice-dia-dataEnvio}
 * (partição {@code dia} = yyyy-MM-dd em UTC, ordenação {@code dataEnvio}) permite consultar por
 * período sem usar Scan e sem criar partição quente.
 */
public class DynamoAvaliacaoRepository implements AvaliacaoRepository {

    public static final String INDICE_DIA = "indice-dia-dataEnvio";

    /** Formato fixo (milissegundos) para que a ordenação lexicográfica coincida com a cronológica. */
    private static final DateTimeFormatter FORMATO_DATA_HORA =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneOffset.UTC);

    private final DynamoDbClient cliente;
    private final String tabela;

    public DynamoAvaliacaoRepository(DynamoDbClient cliente, String tabela) {
        this.cliente = cliente;
        this.tabela = tabela;
    }

    @Override
    public void salvar(Avaliacao a) {
        Map<String, AttributeValue> item = Map.of(
                "id", texto(a.id()),
                "descricao", texto(a.descricao()),
                "nota", AttributeValue.builder().n(Integer.toString(a.nota())).build(),
                "urgencia", texto(a.urgencia().name()),
                "dataEnvio", texto(FORMATO_DATA_HORA.format(a.dataEnvio())),
                "dia", texto(dia(a.dataEnvio())));
        cliente.putItem(PutItemRequest.builder()
                .tableName(tabela)
                .item(item)
                .conditionExpression("attribute_not_exists(id)")
                .build());
    }

    @Override
    public List<Avaliacao> buscarPorPeriodo(Instant inicio, Instant fim) {
        List<Avaliacao> resultado = new ArrayList<>();
        LocalDate ultimoDia = fim.atZone(ZoneOffset.UTC).toLocalDate();
        for (LocalDate d = inicio.atZone(ZoneOffset.UTC).toLocalDate(); !d.isAfter(ultimoDia); d = d.plusDays(1)) {
            QueryRequest consulta = QueryRequest.builder()
                    .tableName(tabela)
                    .indexName(INDICE_DIA)
                    .keyConditionExpression("#dia = :dia AND dataEnvio BETWEEN :ini AND :fim")
                    .expressionAttributeNames(Map.of("#dia", "dia"))
                    .expressionAttributeValues(Map.of(
                            ":dia", texto(d.toString()),
                            ":ini", texto(FORMATO_DATA_HORA.format(inicio)),
                            ":fim", texto(FORMATO_DATA_HORA.format(fim))))
                    .build();
            for (Map<String, AttributeValue> item : cliente.queryPaginator(consulta).items()) {
                resultado.add(converter(item));
            }
        }
        resultado.sort(Comparator.comparing(Avaliacao::dataEnvio));
        return resultado;
    }

    private static Avaliacao converter(Map<String, AttributeValue> item) {
        return new Avaliacao(
                item.get("id").s(),
                item.get("descricao").s(),
                Integer.parseInt(item.get("nota").n()),
                Urgencia.valueOf(item.get("urgencia").s()),
                Instant.parse(item.get("dataEnvio").s()));
    }

    private static String dia(Instant instante) {
        return instante.atZone(ZoneOffset.UTC).toLocalDate().toString();
    }

    private static AttributeValue texto(String valor) {
        return AttributeValue.builder().s(valor).build();
    }
}
