package com.classroompb.util;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Utilitário para leitura e escrita de dados em formato JSON usando Jackson.
 * Centraliza a configuração do ObjectMapper (suporte a datas Java 8 e saída indentada).
 */

public class JsonUtil {
    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .enable(SerializationFeature.INDENT_OUTPUT);

    /**
     * Serializa e salva um objeto no arquivo JSON indicado.
     * Sobrescreve o arquivo caso já exista.
     */
    public static void salvar(String caminho, Object dados) throws IOException {
        mapper.writeValue(new File(caminho), dados);
    }

    /**
     * Lê o arquivo JSON e retorna uma lista de objetos do tipo informado.
     * Retorna lista vazia se o arquivo ainda não existir.
     */
    public static <T> List<T> carregarLista(String caminho, Class<T> classe) throws IOException {
        File arquivo = new File(caminho);
        if (!arquivo.exists()) {
            return new java.util.ArrayList<>();
        }
        return mapper.readValue(arquivo, mapper.getTypeFactory().constructCollectionType(List.class, classe));
    }
}
