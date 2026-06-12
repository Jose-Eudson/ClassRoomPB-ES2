package com.classroompb.repository;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
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
    @DisplayName("Persistencia e listagem")
    class PersistenciaEListagem {

        @Test
        @DisplayName("Deve iniciar vazio quando arquivo nao existe")
        void deveIniciarVazio() {
            assertTrue(repository.listarTodos().isEmpty());
        }

        @Test
        @DisplayName("Deve salvar e listar cursos")
        void deveSalvarEListarCursos() {
            repository.salvar(new Curso("ADS", "Analise e Desenvolvimento de Sistemas", 3200));
            repository.salvar(new Curso("SI", "Sistemas de Informacao", 3000));
            List<Curso> cursos = repository.listarTodos();
            assertEquals(2, cursos.size());
        }

        @Test
        @DisplayName("Deve persistir cursos entre instancias")
        void devePersistirEntreInstancias() {
            repository.salvar(new Curso("ADS", "Analise e Desenvolvimento de Sistemas", 3200));
            CursoRepository novoRepository = new CursoRepository(arquivoTemp());
            assertEquals(1, novoRepository.listarTodos().size());
            assertEquals("ADS", novoRepository.listarTodos().get(0).getCodigo());
        }

        @Test
        @DisplayName("listarTodos deve retornar copia defensiva")
        void listarTodosRetornaCopiaDefensiva() {
            repository.salvar(new Curso("ADS", "Analise", 3200));
            List<Curso> lista = repository.listarTodos();
            lista.clear();
            assertEquals(1, repository.listarTodos().size());
        }
    }

    @Nested
    @DisplayName("Busca e existencia por codigo")
    class BuscaEExistencia {

        @Test
        @DisplayName("existePorCodigo deve retornar true para codigo existente")
        void existePorCodigoRetornaTrue() {
            repository.salvar(new Curso("ADS", "Analise", 3200));
            assertTrue(repository.existePorCodigo("ADS"));
        }

        @Test
        @DisplayName("existePorCodigo deve ser case-insensitive")
        void existePorCodigoCaseInsensitive() {
            repository.salvar(new Curso("ADS", "Analise", 3200));
            assertTrue(repository.existePorCodigo("ads"));
            assertTrue(repository.existePorCodigo("Ads"));
        }

        @Test
        @DisplayName("existePorCodigo deve retornar false para codigo inexistente")
        void existePorCodigoRetornaFalse() {
            assertFalse(repository.existePorCodigo("EC"));
        }

        @Test
        @DisplayName("buscarPorCodigo deve retornar curso existente")
        void buscarPorCodigoRetornaExistente() {
            repository.salvar(new Curso("ADS", "Analise", 3200));
            Curso c = repository.buscarPorCodigo("ADS");
            assertNotNull(c);
            assertEquals("Analise", c.getNome());
        }

        @Test
        @DisplayName("buscarPorCodigo deve retornar null para inexistente")
        void buscarPorCodigoRetornaNull() {
            assertNull(repository.buscarPorCodigo("XPTO"));
        }
    }

    @Nested
    @DisplayName("Atualizacao e delecao")
    class AtualizacaoEDelecao {

        @Test
        @DisplayName("Deve atualizar curso existente")
        void deveAtualizarCurso() {
            repository.salvar(new Curso("ADS", "Antigo", 3200));
            Curso c = repository.buscarPorCodigo("ADS");
            c.setNome("Novo Nome");
            repository.atualizar(c);
            assertEquals("Novo Nome", repository.buscarPorCodigo("ADS").getNome());
        }

        @Test
        @DisplayName("atualizar deve lancar excecao para codigo inexistente")
        void atualizarLancaExcecaoParaInexistente() {
            Curso c = new Curso("NAO_EXISTE", "Teste", 1000);
            assertThrows(IllegalArgumentException.class, () -> repository.atualizar(c));
        }

        @Test
        @DisplayName("Deve deletar curso existente")
        void deveDeletarCurso() {
            repository.salvar(new Curso("ADS", "Analise", 3200));
            repository.deletar("ADS");
            assertFalse(repository.existePorCodigo("ADS"));
        }

        @Test
        @DisplayName("deletar deve lancar excecao para codigo inexistente")
        void deletarLancaExcecaoParaInexistente() {
            assertThrows(IllegalArgumentException.class, () -> repository.deletar("INEXISTENTE"));
        }
    }

    @Nested
    @DisplayName("Tratamento de erros de IO")
    class TratamentoDeErrosDeIO {

        @Test
        @DisplayName("Construtor padrao deve inicializar sem excecao")
        void construtorPadraoNaoLancaExcecao() {
            // Exercita CursoRepository() -> this(CAMINHO_PADRAO) -> carregarDados()
            // O arquivo padrao nao existe no ambiente de testes, entao inicia vazio
            assertDoesNotThrow(() -> new CursoRepository());
        }

        @Test
        @DisplayName("carregarDados deve iniciar lista vazia ao receber caminho invalido (diretorio)")
        void carregarDadosComCaminhoInvalidoInicialistaVazia() {
            // Um diretorio no lugar de arquivo faz JsonUtil lancar IOException
            // O catch deve tratar silenciosamente e iniciar com lista vazia
            String caminhoInvalido = tempDir.toString(); // diretorio, nao arquivo
            CursoRepository repo = new CursoRepository(caminhoInvalido);
            assertNotNull(repo.listarTodos());
            assertTrue(repo.listarTodos().isEmpty());
        }

        @Test
        @DisplayName("salvarDados deve tratar silenciosamente IOException em caminho somente leitura")
        @DisabledOnOs(OS.WINDOWS)
        void salvarDadosEmCaminhoSomenteLeitura() throws Exception {
            // Cria subdiretorio somente leitura para forcar IOException no salvar
            File dirReadOnly = tempDir.resolve("readonly").toFile();
            dirReadOnly.mkdir();
            dirReadOnly.setWritable(false);

            String caminhoInvalido = dirReadOnly.getAbsolutePath() + "/cursos.json";
            CursoRepository repo = new CursoRepository(caminhoInvalido);

            // salvar chama salvarDados() internamente; nao deve lancar excecao para o chamador
            assertDoesNotThrow(() -> repo.salvar(new Curso("ADS", "Analise", 3200)));
        }
    }
}
