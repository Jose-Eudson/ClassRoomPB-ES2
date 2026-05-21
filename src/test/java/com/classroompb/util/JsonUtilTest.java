package com.classroompb.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.classroompb.model.Curso;

@DisplayName("Testes de JsonUtil")
public class JsonUtilTest {

    @TempDir
    Path tempDir;

    @Nested
    @DisplayName("Salvar")
    class Salvar {

        @Test
        @DisplayName("Deve salvar lista e criar arquivo")
        void deveSalvarListaECriarArquivo() throws IOException {
            String caminho = tempDir.resolve("cursos.json").toString();
            List<Curso> cursos = Arrays.asList(
                    new Curso("ADS", "Analise", 3200),
                    new Curso("SI", "Sistemas", 3000)
            );
            JsonUtil.salvar(caminho, cursos);
            assertTrue(new File(caminho).exists());
        }

        @Test
        @DisplayName("Deve sobrescrever arquivo existente")
        void deveSobrescreverArquivoExistente() throws IOException {
            String caminho = tempDir.resolve("cursos.json").toString();
            JsonUtil.salvar(caminho, Arrays.asList(new Curso("ADS", "Analise", 3200)));
            JsonUtil.salvar(caminho, Arrays.asList(new Curso("SI", "Sistemas", 3000)));
            List<Curso> resultado = JsonUtil.carregarLista(caminho, Curso.class);
            assertEquals(1, resultado.size());
            assertEquals("SI", resultado.get(0).getCodigo());
        }
    }

    @Nested
    @DisplayName("CarregarLista")
    class CarregarLista {

        @Test
        @DisplayName("Deve retornar lista vazia para arquivo inexistente")
        void deveRetornarListaVaziaParaArquivoInexistente() throws IOException {
            String caminho = tempDir.resolve("inexistente.json").toString();
            List<Curso> resultado = JsonUtil.carregarLista(caminho, Curso.class);
            assertNotNull(resultado);
            assertTrue(resultado.isEmpty());
        }

        @Test
        @DisplayName("Deve carregar lista salva anteriormente")
        void deveCarregarListaSalvaAnteriormente() throws IOException {
            String caminho = tempDir.resolve("cursos.json").toString();
            List<Curso> original = Arrays.asList(
                    new Curso("ADS", "Analise", 3200),
                    new Curso("SI", "Sistemas", 3000)
            );
            JsonUtil.salvar(caminho, original);
            List<Curso> carregado = JsonUtil.carregarLista(caminho, Curso.class);
            assertEquals(2, carregado.size());
            assertEquals("ADS", carregado.get(0).getCodigo());
            assertEquals("SI", carregado.get(1).getCodigo());
        }
    }
}
