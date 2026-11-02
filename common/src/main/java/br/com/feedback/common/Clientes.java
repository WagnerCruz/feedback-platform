package br.com.feedback.common;

import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.ses.SesClient;

/** Fábrica de clientes AWS leves (HTTP via URLConnection; região vem do ambiente do Lambda). */
public final class Clientes {

    private Clientes() {
    }

    public static DynamoDbClient dynamo() {
        return DynamoDbClient.builder().httpClient(UrlConnectionHttpClient.create()).build();
    }

    public static SesClient ses() {
        return SesClient.builder().httpClient(UrlConnectionHttpClient.create()).build();
    }
}
