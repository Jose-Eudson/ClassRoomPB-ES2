package com.classroompb.repository;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
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
        @DisplayName("Deve salvar periodo letivo e persistir no arquivo")
        void deveSalvarPeriodoLetivo() {
            PeriodoLetivo periodo = new PeriodoLetivo("2026.2", 2026, 2,
                    LocalDate.of(2026, 8, 10), LocalDate.of(2026, 12, 20), true);
            repository.salvar(periodo);
            
            assertEquals(1, repository.listarTodos().size());
            assertEquals("2026.2", repository.listarTodos().get(0).getCodigo());
        }

        @Test
        @DisplayName("Deve persistir múltiplos períodos e manter a ordem")
        void devePersistirMultiplosPeriodos() {
            repository.salvar(new PeriodoLetivo("2026.1", 2026, 1, LocalDate.now(), LocalDate.now().plusMonths(4), false));
            repository.salvar(new PeriodoLetivo("2026.2", 2026, 2, LocalDate.now(), LocalDate.now().plusMonths(4), true));
            
            List<PeriodoLetivo> todos = repository.listarTodos();
            assertEquals(2, todos.size());
            assertEquals("2026.1", todos.get(0).getCodigo());
            assertEquals("2026.2", todos.get(1).getCodigo());
        }

        @Test
        @DisplayName("Deve persistir entre instancias diferentes apontando para o mesmo arquivo")
        void devePersistirEntreInstancias() {
            repository.salvar(new PeriodoLetivo("2026.1", 2026, 1,
                    LocalDate.of(2026, 2, 1), LocalDate.of(2026, 6, 30), false));
            
            PeriodoLetivoRepository novoRepo = new PeriodoLetivoRepository(arquivoTemp());
            assertEquals(1, novoRepo.listarTodos().size());
            assertEquals("2026.1", novoRepo.listarTodos().get(0).getCodigo());
        }

        @Test
        @DisplayName("listarTodos deve retornar copia defensiva (modificar a lista retornada não afeta o repo)")
        void listarTodosRetornaCopiaDefensiva() {
            repository.salvar(new PeriodoLetivo("2026.2", 2026, 2,
                    LocalDate.of(2026, 8, 10), LocalDate.of(2026, 12, 20), true));
            List<PeriodoLetivo> lista = repository.listarTodos();
            lista.clear();
            assertEquals(1, repository.listarTodos().size(), "A lista interna do repositório não deve ser afetada");
        }
    }

    @Nested
    @DisplayName("Busca e existencia por codigo")
    class BuscaEExistencia {

        @Test
        @DisplayName("Deve verificar existencia por codigo de forma exata")
        void deveVerificarExistenciaPorCodigo() {
            repository.salvar(new PeriodoLetivo("2026.2", 2026, 2,
                    LocalDate.of(2026, 8, 10), LocalDate.of(2026, 12, 20), true));
            assertTrue(repository.existePorCodigo("2026.2"));
        }

        @Test
        @DisplayName("existePorCodigo deve ser case-insensitive")
        void existePorCodigoCaseInsensitive() {
            repository.salvar(new PeriodoLetivo("AnoLetivo-A", 2026, 1, LocalDate.now(), LocalDate.now(), true));
            assertTrue(repository.existePorCodigo("anoletivo-a"));
            assertTrue(repository.existePorCodigo("ANOLETIVO-A"));
            assertTrue(repository.existePorCodigo("AnOlEtIvO-a"));
        }

        @Test
        @DisplayName("existePorCodigo deve retornar false para codigo inexistente ou nulo")
        void existePorCodigoFalse() {
            repository.salvar(new PeriodoLetivo("2026.1", 2026, 1, LocalDate.now(), LocalDate.now(), true));
            assertFalse(repository.existePorCodigo("9999.9"));
            assertFalse(repository.existePorCodigo(null));
        }

        @Test
        @DisplayName("buscarPorCodigo deve retornar periodo existente")
        void buscarPorCodigoRetornaExistente() {
            repository.salvar(new PeriodoLetivo("2026.2", 2026, 2,
                    LocalDate.of(2026, 8, 10), LocalDate.of(2026, 12, 20), true));
            PeriodoLetivo p = repository.buscarPorCodigo("2026.2");
            assertNotNull(p);
            assertEquals(2026, p.getAno());
            assertEquals(2, p.getSemestre());
        }

        @Test
        @DisplayName("buscarPorCodigo deve ser case-insensitive")
        void buscarPorCodigoCaseInsensitive() {
            repository.salvar(new PeriodoLetivo("ABC.1", 2026, 1,
                    LocalDate.of(2026, 2, 1), LocalDate.of(2026, 6, 30), true));
            assertNotNull(repository.buscarPorCodigo("abc.1"));
            assertNotNull(repository.buscarPorCodigo("ABC.1"));
            assertEquals("ABC.1", repository.buscarPorCodigo("abc.1").getCodigo());
        }

        @Test
        @DisplayName("buscarPorCodigo deve retornar null para inexistente ou nulo")
        void buscarPorCodigoRetornaNull() {
            assertNull(repository.buscarPorCodigo("9999.9"));
            assertNull(repository.buscarPorCodigo(null));
        }
    }

    @Nested
    @DisplayName("Sincronização e Atualização")
    class AtualizarDados {

        @Test
        @DisplayName("Deve persistir mudanças feitas nos objetos da lista através de atualizarDados()")
        void deveAtualizarDados() {
            repository.salvar(new PeriodoLetivo("2026.2", 2026, 2,
                    LocalDate.of(2026, 8, 10), LocalDate.of(2026, 12, 20), false));
            
            PeriodoLetivo p = repository.buscarPorCodigo("2026.2");
            p.setAtivo(true);
            p.setAno(2027);
            
            repository.atualizarDados(); // Sincroniza a lista em memória com o arquivo

            PeriodoLetivoRepository novoRepo = new PeriodoLetivoRepository(arquivoTemp());
            PeriodoLetivo pRecuperado = novoRepo.buscarPorCodigo("2026.2");
            assertTrue(pRecuperado.isAtivo());
            assertEquals(2027, pRecuperado.getAno());
        }
    }

    @Nested
    @DisplayName("Tratamento de erros e Casos de Borda")
    class TratamentoDeErrosDeIO {

        @Test
        @DisplayName("Construtor padrao deve inicializar usando o caminho padrao sem excecao")
        void construtorPadraoNaoLancaExcecao() {
            assertDoesNotThrow(() -> new PeriodoLetivoRepository());
        }

        @Test
        @DisplayName("Deve lidar com arquivo corrompido ou inválido iniciando lista vazia")
        void deveLidarComArquivoInvalido() throws Exception {
            java.nio.file.Files.write(tempDir.resolve("corrompido.json"), "texto invalido que nao e json".getBytes(java.nio.charset.StandardCharsets.UTF_8));
            PeriodoLetivoRepository repo = new PeriodoLetivoRepository(tempDir.resolve("corrompido.json").toString());
            assertTrue(repo.listarTodos().isEmpty());
        }

        @Test
        @DisplayName("carregarDados deve iniciar lista vazia ao receber caminho que é um diretorio")
        void carregarDadosComCaminhoInvalidoInicialistaVazia() {
            String caminhoInvalido = tempDir.toString();
            PeriodoLetivoRepository repo = new PeriodoLetivoRepository(caminhoInvalido);
            assertNotNull(repo.listarTodos());
            assertTrue(repo.listarTodos().isEmpty());
        }

        @Test
        @DisplayName("salvarDados deve tratar silenciosamente IOException (ex: caminho somente leitura)")
        @DisabledOnOs(OS.WINDOWS)
        void salvarDadosEmCaminhoSomenteLeitura() throws Exception {
            File dirReadOnly = tempDir.resolve("readonly").toFile();
            dirReadOnly.mkdir();
            dirReadOnly.setWritable(false);

            String caminhoArquivo = dirReadOnly.getAbsolutePath() + "/periodos.json";
            PeriodoLetivoRepository repo = new PeriodoLetivoRepository(caminhoArquivo);

            // Não deve lançar exceção mesmo se não conseguir salvar no disco
            assertDoesNotThrow(() -> repo.salvar(new PeriodoLetivo("2026.1", 2026, 1,
                    LocalDate.of(2026, 2, 1), LocalDate.of(2026, 6, 30), true)));
            
            dirReadOnly.setWritable(true);
        }
    }
}
