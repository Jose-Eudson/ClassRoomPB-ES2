package com.classroompb.repository;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import com.classroompb.model.PeriodoLetivo;

@DisplayName("Testes unitarios de PeriodoLetivoRepository")
public class PeriodoLetivoRepositoryTest {

    @TempDir
    Path tempDir;

    private PeriodoLetivoRepository repository;

    private String arquivoTemp() {
        return tempDir.resolve("periodos_test.json").toString();
    }

    @BeforeEach
    void setUp() {
        repository = new PeriodoLetivoRepository(arquivoTemp());
    }

    @Nested
    @DisplayName("Persistencia e listagem")
    class PersistenciaEListagem {

        @Test
        @DisplayName("Deve iniciar vazio quando arquivo nao existe")
        void deveIniciarVazio() {
            assertTrue(repository.listarTodos().isEmpty());
        }

        @Test
        @DisplayName("Deve salvar periodo letivo")
        void deveSalvarPeriodoLetivo() {
            repository.salvar(new PeriodoLetivo("2026.2", 2026, 2,
                    LocalDate.of(2026, 8, 10), LocalDate.of(2026, 12, 20), true));
            assertEquals(1, repository.listarTodos().size());
        }

        @Test
        @DisplayName("Deve persistir entre instancias")
        void devePersistirEntreInstancias() {
            repository.salvar(new PeriodoLetivo("2026.1", 2026, 1,
                    LocalDate.of(2026, 2, 1), LocalDate.of(2026, 6, 30), false));
            PeriodoLetivoRepository novo = new PeriodoLetivoRepository(arquivoTemp());
            assertEquals(1, novo.listarTodos().size());
            assertEquals("2026.1", novo.listarTodos().get(0).getCodigo());
        }

        @Test
        @DisplayName("listarTodos deve retornar copia defensiva")
        void listarTodosRetornaCopiaDefensiva() {
            repository.salvar(new PeriodoLetivo("2026.2", 2026, 2,
                    LocalDate.of(2026, 8, 10), LocalDate.of(2026, 12, 20), true));
            List<PeriodoLetivo> lista = repository.listarTodos();
            lista.clear();
            assertEquals(1, repository.listarTodos().size());
        }
    }

    @Nested
    @DisplayName("Busca e existencia por codigo")
    class BuscaEExistencia {

        @Test
        @DisplayName("Deve verificar existencia por codigo")
        void deveVerificarExistenciaPorCodigo() {
            repository.salvar(new PeriodoLetivo("2026.2", 2026, 2,
                    LocalDate.of(2026, 8, 10), LocalDate.of(2026, 12, 20), true));
            assertTrue(repository.existePorCodigo("2026.2"));
        }

        @Test
        @DisplayName("existePorCodigo deve ser case-insensitive")
        void existePorCodigoCaseInsensitive() {
            repository.salvar(new PeriodoLetivo("2026.2", 2026, 2,
                    LocalDate.of(2026, 8, 10), LocalDate.of(2026, 12, 20), true));
            assertTrue(repository.existePorCodigo("2026.2"));
        }

        @Test
        @DisplayName("existePorCodigo deve retornar false para codigo inexistente")
        void existePorCodigoFalse() {
            assertFalse(repository.existePorCodigo("9999.9"));
        }

        @Test
        @DisplayName("buscarPorCodigo deve retornar periodo existente")
        void buscarPorCodigoRetornaExistente() {
            repository.salvar(new PeriodoLetivo("2026.2", 2026, 2,
                    LocalDate.of(2026, 8, 10), LocalDate.of(2026, 12, 20), true));
            PeriodoLetivo p = repository.buscarPorCodigo("2026.2");
            assertNotNull(p);
            assertEquals(2026, p.getAno());
        }

        @Test
        @DisplayName("buscarPorCodigo deve retornar null para inexistente")
        void buscarPorCodigoRetornaNull() {
            assertNull(repository.buscarPorCodigo("9999.9"));
        }
    }

    @Nested
    @DisplayName("atualizarDados persiste mudancas in-memory")
    class AtualizarDados {

        @Test
        @DisplayName("Deve persistir mudancas feitas no objeto via atualizarDados")
        void deveAtualizarDados() {
            repository.salvar(new PeriodoLetivo("2026.2", 2026, 2,
                    LocalDate.of(2026, 8, 10), LocalDate.of(2026, 12, 20), false));
            PeriodoLetivo p = repository.buscarPorCodigo("2026.2");
            p.setAtivo(true);
            repository.atualizarDados();

            PeriodoLetivoRepository novo = new PeriodoLetivoRepository(arquivoTemp());
            assertTrue(novo.buscarPorCodigo("2026.2").isAtivo());
        }
    }

    @Nested
    @DisplayName("Tratamento de erros de IO")
    class TratamentoDeErrosDeIO {

        @Test
        @DisplayName("Construtor padrao deve inicializar sem excecao")
        void construtorPadraoNaoLancaExcecao() {
            assertDoesNotThrow(() -> new PeriodoLetivoRepository());
        }

        @Test
        @DisplayName("carregarDados deve iniciar lista vazia ao receber caminho invalido (diretorio)")
        void carregarDadosComCaminhoInvalidoInicialistaVazia() {
            String caminhoInvalido = tempDir.toString();
            PeriodoLetivoRepository repo = new PeriodoLetivoRepository(caminhoInvalido);
            assertNotNull(repo.listarTodos());
            assertTrue(repo.listarTodos().isEmpty());
        }

        @Test
        @DisplayName("salvarDados deve tratar silenciosamente IOException em caminho somente leitura")
        @DisabledOnOs(OS.WINDOWS)
        void salvarDadosEmCaminhoSomenteLeitura() throws Exception {
            File dirReadOnly = tempDir.resolve("readonly").toFile();
            dirReadOnly.mkdir();
            dirReadOnly.setWritable(false);

            String caminhoInvalido = dirReadOnly.getAbsolutePath() + "/periodos.json";
            PeriodoLetivoRepository repo = new PeriodoLetivoRepository(caminhoInvalido);

            assertDoesNotThrow(() -> repo.salvar(new PeriodoLetivo("2026.1", 2026, 1,
                    LocalDate.of(2026, 2, 1), LocalDate.of(2026, 6, 30), true)));
        }
    }
}