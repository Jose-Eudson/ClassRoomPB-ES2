package com.classroompb.repository;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import com.classroompb.model.Disciplina;

@DisplayName("Testes unitarios de DisciplinaRepository")
public class DisciplinaRepositoryTest {

    @TempDir
    Path tempDir;

    private DisciplinaRepository repository;

    private String arquivoTemp() {
        return tempDir.resolve("disciplinas_test.json").toString();
    }

    @BeforeEach
    void setUp() {
        repository = new DisciplinaRepository(arquivoTemp());
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
        @DisplayName("Deve salvar disciplina")
        void deveSalvarDisciplina() {
            repository.salvar(new Disciplina("BD", "Banco de Dados", 60, 4, Arrays.asList("POO")));
            assertEquals(1, repository.listarTodos().size());
        }

        @Test
        @DisplayName("Deve persistir entre instancias")
        void devePersistirEntreInstancias() {
            repository.salvar(new Disciplina("ES2", "Engenharia de Software 2", 60, 4, Arrays.asList("ES1")));
            DisciplinaRepository novo = new DisciplinaRepository(arquivoTemp());
            assertEquals(1, novo.listarTodos().size());
            assertEquals("ES2", novo.listarTodos().get(0).getCodigo());
        }

        @Test
        @DisplayName("listarTodos deve retornar copia defensiva")
        void listarTodosRetornaCopiaDefensiva() {
            repository.salvar(new Disciplina("BD", "Banco de Dados", 60, 4, Arrays.asList()));
            List<Disciplina> lista = repository.listarTodos();
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
            repository.salvar(new Disciplina("LP2", "Linguagem de Programacao 2", 80, 5, Arrays.asList("LP1")));
            assertTrue(repository.existePorCodigo("LP2"));
        }

        @Test
        @DisplayName("existePorCodigo deve ser case-insensitive")
        void existePorCodigoCaseInsensitive() {
            repository.salvar(new Disciplina("ES2", "Eng SW 2", 60, 4, Arrays.asList()));
            assertTrue(repository.existePorCodigo("es2"));
            assertTrue(repository.existePorCodigo("ES2"));
        }

        @Test
        @DisplayName("existePorCodigo deve retornar false para codigo inexistente")
        void existePorCodigoFalse() {
            assertFalse(repository.existePorCodigo("INEXISTENTE"));
        }

        @Test
        @DisplayName("buscarPorCodigo deve retornar disciplina existente")
        void buscarPorCodigoRetornaExistente() {
            repository.salvar(new Disciplina("BD", "Banco de Dados", 60, 4, Arrays.asList()));
            Disciplina d = repository.buscarPorCodigo("BD");
            assertNotNull(d);
            assertEquals("Banco de Dados", d.getNome());
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
        @DisplayName("Deve atualizar disciplina existente")
        void deveAtualizarDisciplina() {
            repository.salvar(new Disciplina("ES2", "Eng SW 2", 60, 4, Arrays.asList()));
            Disciplina d = repository.buscarPorCodigo("ES2");
            d.setNome("Engenharia de Software 2 Atualizada");
            repository.atualizar(d);
            assertEquals("Engenharia de Software 2 Atualizada", repository.buscarPorCodigo("ES2").getNome());
        }

        @Test
        @DisplayName("atualizar deve lancar excecao para codigo inexistente")
        void atualizarLancaExcecaoParaInexistente() {
            Disciplina d = new Disciplina("NAO_EXISTE", "Teste", 60, 4, Arrays.asList());
            assertThrows(IllegalArgumentException.class, () -> repository.atualizar(d));
        }

        @Test
        @DisplayName("Deve deletar disciplina existente")
        void deveDeletarDisciplina() {
            repository.salvar(new Disciplina("BD", "Banco de Dados", 60, 4, Arrays.asList()));
            repository.deletar("BD");
            assertFalse(repository.existePorCodigo("BD"));
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
            assertDoesNotThrow(() -> new DisciplinaRepository());
        }

        @Test
        @DisplayName("carregarDados deve iniciar lista vazia ao receber caminho invalido (diretorio)")
        void carregarDadosComCaminhoInvalidoInicialistaVazia() {
            String caminhoInvalido = tempDir.toString();
            DisciplinaRepository repo = new DisciplinaRepository(caminhoInvalido);
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

            String caminhoInvalido = dirReadOnly.getAbsolutePath() + "/disciplinas.json";
            DisciplinaRepository repo = new DisciplinaRepository(caminhoInvalido);

            assertDoesNotThrow(() -> repo.salvar(new Disciplina("BD", "Banco de Dados", 60, 4, Arrays.asList())));
        }
    }
}
