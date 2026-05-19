package com.classroompb.repository;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.classroompb.model.Curso;

@DisplayName("Testes de CursoRepository")
public class CursoRepositoryTest {

    @TempDir
    Path tempDir;

    private CursoRepository repository;

    private String arquivoTemp() {
        return tempDir.resolve("cursos_test.json").toString();
    }

    @BeforeEach
    void setUp() {
        repository = new CursoRepository(arquivoTemp());
    }

    @Nested
    @DisplayName("Persistência e listagem")
    class PersistenciaEListagem {

        @Test
        @DisplayName("Deve iniciar vazio quando arquivo não existe")
        void deveIniciarVazio() {
            assertTrue(repository.listarTodos().isEmpty());
        }

        @Test
        @DisplayName("Deve salvar e listar cursos")
        void deveSalvarEListarCursos() {
            repository.salvar(new Curso("ADS", "Análise e Desenvolvimento de Sistemas", 3200));
            repository.salvar(new Curso("SI", "Sistemas de Informação", 3000));

            List<Curso> cursos = repository.listarTodos();
            assertEquals(2, cursos.size());
            assertEquals("ADS", cursos.get(0).getCodigo());
            assertEquals("SI", cursos.get(1).getCodigo());
        }

        @Test
        @DisplayName("Deve persistir cursos entre instâncias")
        void devePersistirEntreInstancias() {
            repository.salvar(new Curso("ADS", "Análise e Desenvolvimento de Sistemas", 3200));

            CursoRepository novoRepository = new CursoRepository(arquivoTemp());
            List<Curso> cursos = novoRepository.listarTodos();

            assertEquals(1, cursos.size());
            assertEquals("ADS", cursos.get(0).getCodigo());
            assertEquals("Análise e Desenvolvimento de Sistemas", cursos.get(0).getNome());
        }
    }

    @Nested
    @DisplayName("Duplicidade por código")
    class DuplicidadePorCodigo {

        @Test
        @DisplayName("existePorCodigo deve retornar true para código existente")
        void existePorCodigoRetornaTrue() {
            repository.salvar(new Curso("ADS", "Análise e Desenvolvimento de Sistemas", 3200));
            assertTrue(repository.existePorCodigo("ADS"));
        }

        @Test
        @DisplayName("existePorCodigo deve ser case-insensitive")
        void existePorCodigoCaseInsensitive() {
            repository.salvar(new Curso("ADS", "Análise e Desenvolvimento de Sistemas", 3200));
            assertTrue(repository.existePorCodigo("ads"));
            assertTrue(repository.existePorCodigo("Ads"));
        }

        @Test
        @DisplayName("existePorCodigo deve retornar false para código inexistente")
        void existePorCodigoRetornaFalse() {
            assertFalse(repository.existePorCodigo("EC"));
        }
    }
}
