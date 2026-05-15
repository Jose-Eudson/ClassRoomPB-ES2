package com.classroompb.repository;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.classroompb.model.Administrador;
import com.classroompb.model.Aluno;
import com.classroompb.model.Coordenador;
import com.classroompb.model.Professor;
import com.classroompb.model.Usuario;

/**
 * Testes de integração para UsuarioRepository.
 *
 * Usa @TempDir do JUnit 5: o JUnit cria uma pasta temporária exclusiva para cada
 * teste e a apaga automaticamente ao final. O arquivo "usuarios.json" real nunca
 * é tocado durante os testes.
 */

@DisplayName("Testes de UsuarioRepository")
public class UsuarioRepositoryTest {

    @TempDir
    Path tempDir;

    private UsuarioRepository repository;

    /** Retorna o caminho do arquivo JSON temporário isolado por teste. */
    private String arquivoTemp() {
        return tempDir.resolve("usuarios_test.json").toString();
    }

    /** Cria um repositório novo apontando para o arquivo temporário antes de cada teste. */
    @BeforeEach
    void setUp() {
        repository = new UsuarioRepository(arquivoTemp());
    }

    // =========================================================================
    // PERSISTÊNCIA
    // =========================================================================

    /** Verifica que os dados gravados por uma instância são lidos corretamente por outra (persistência real em disco). */
    @Nested
    @DisplayName("Persistência em disco")
    class Persistencia {

        @Test
        @DisplayName("Deve salvar e recuperar usuário em nova instância do repositório")
        void deveSalvarEPersistirUsuario() {
            Usuario aluno = new Aluno("A001", "João", "joao@email.com", "123");
            repository.salvar(aluno);

            UsuarioRepository novoRepo = new UsuarioRepository(arquivoTemp());
            Optional<Usuario> buscado = novoRepo.buscarPorMatricula("A001");

            assertTrue(buscado.isPresent());
            assertEquals("João", buscado.get().getNome());
        }

        @Test
        @DisplayName("Deve persistir múltiplos usuários e recuperá-los corretamente")
        void devePersistirMultiplosUsuarios() {
            repository.salvar(new Aluno("A001", "João", "joao@email.com", "123"));
            repository.salvar(new Professor("P001", "Maria", "maria@email.com", "456"));
            repository.salvar(new Coordenador("C001", "Carlos", "carlos@email.com", "789"));
            repository.salvar(new Administrador("AD001", "Admin", "admin@email.com", "000"));

            UsuarioRepository novoRepo = new UsuarioRepository(arquivoTemp());
            List<Usuario> todos = novoRepo.listarTodos();

            assertEquals(4, todos.size());
        }

        @Test
        @DisplayName("Deve iniciar com lista vazia quando não existe arquivo")
        void deveIniciarVazioSemArquivo() {
            assertTrue(repository.listarTodos().isEmpty());
        }
    }

    // =========================================================================
    // CRUD
    // =========================================================================

    @Nested
    @DisplayName("Salvar")
    class Salvar {

        @Test
        @DisplayName("Deve salvar e listar dois usuários")
        void deveSalvarDoisUsuarios() {
            repository.salvar(new Aluno("A001", "João", "joao@email.com", "123"));
            repository.salvar(new Aluno("A002", "Maria", "maria@email.com", "456"));

            assertEquals(2, repository.listarTodos().size());
        }
    }

    @Nested
    @DisplayName("Busca")
    class Busca {

        @Test
        @DisplayName("Deve buscar por matrícula existente")
        void deveBuscarPorMatriculaExistente() {
            repository.salvar(new Aluno("A001", "João", "joao@email.com", "123"));

            Optional<Usuario> resultado = repository.buscarPorMatricula("A001");

            assertTrue(resultado.isPresent());
            assertEquals("A001", resultado.get().getMatricula());
        }

        @Test
        @DisplayName("Deve retornar vazio para matrícula inexistente")
        void deveRetornarVazioParaMatriculaInexistente() {
            assertTrue(!repository.buscarPorMatricula("X999").isPresent());
        }

        @Test
        @DisplayName("Deve buscar por e-mail de forma case-insensitive")
        void deveBuscarPorEmailCaseInsensitive() {
            repository.salvar(new Aluno("A001", "João", "joao@email.com", "123"));

            Optional<Usuario> resultado = repository.buscarPorEmail("JOAO@EMAIL.COM");

            assertTrue(resultado.isPresent());
            assertEquals("A001", resultado.get().getMatricula());
        }

        @Test
        @DisplayName("Deve retornar vazio para e-mail inexistente")
        void deveRetornarVazioParaEmailInexistente() {
            assertTrue(!repository.buscarPorEmail("naoexiste@email.com").isPresent());
        }
    }

    @Nested
    @DisplayName("Atualizar")
    class Atualizar {

        @Test
        @DisplayName("Deve atualizar usuário e persistir a mudança")
        void deveAtualizarUsuario() {
            Usuario aluno = new Aluno("A001", "João", "joao@email.com", "123");
            repository.salvar(aluno);

            aluno.setNome("João Silva");
            repository.atualizar(aluno);

            UsuarioRepository novoRepo = new UsuarioRepository(arquivoTemp());
            assertEquals("João Silva", novoRepo.buscarPorMatricula("A001").get().getNome());
        }

        @Test
        @DisplayName("Não deve atualizar usuário inexistente — deve lançar exceção")
        void deveLancarExcecaoAoAtualizarInexistente() {
            Usuario fantasma = new Aluno("999", "Fantasma", "fantasma@email.com", "senha");

            assertThrows(IllegalArgumentException.class, () -> repository.atualizar(fantasma));
        }
    }

    @Nested
    @DisplayName("Deleção de usuário")
    class Deletar {

        @Test
        @DisplayName("Deve deletar usuário e confirmar remoção")
        void deveDeletarUsuario() {
            repository.salvar(new Aluno("A001", "João", "joao@email.com", "123"));
            repository.deletar("A001");

            assertTrue(repository.listarTodos().isEmpty());
        }

        @Test
        @DisplayName("Deleção deve ser persistida — nova instância não deve encontrar o usuário")
        void devePersistirDelecao() {
            repository.salvar(new Aluno("A001", "João", "joao@email.com", "123"));
            repository.deletar("A001");

            UsuarioRepository novoRepo = new UsuarioRepository(arquivoTemp());
            assertTrue(novoRepo.listarTodos().isEmpty());
        }

        @Test
        @DisplayName("Não deve deletar matrícula inexistente — deve lançar exceção")
        void deveLancarExcecaoAoDeletarInexistente() {
            assertThrows(IllegalArgumentException.class, () -> repository.deletar("999"));
        }

        @Test
        @DisplayName("Não deve deletar usuário já removido — deve lançar exceção")
        void naoDevePermitirDuplaDeleção() {
            repository.salvar(new Aluno("A001", "João", "joao@email.com", "123"));
            repository.deletar("A001");

            assertThrows(IllegalArgumentException.class, () -> repository.deletar("A001"));
        }
    }

    // =========================================================================
    // RF04: PREVENÇÃO DE CADASTRO DUPLICADO
    // =========================================================================

    @Nested
    @DisplayName("RF04 — Prevenção de cadastro duplicado")
    class PrevenirCadastroDuplicado {

        @Test
        @DisplayName("existePorMatricula deve retornar true para matrícula já cadastrada")
        void existePorMatriculaRetornaTrueParaDuplicata() {
            repository.salvar(new Aluno("A001", "João", "joao@email.com", "123"));
            assertTrue(repository.existePorMatricula("A001"));
        }

        @Test
        @DisplayName("existePorMatricula deve retornar false para matrícula inexistente")
        void existePorMatriculaRetornaFalseParaInexistente() {
            org.junit.jupiter.api.Assertions.assertFalse(repository.existePorMatricula("A999"));
        }

        @Test
        @DisplayName("existePorEmail deve retornar true para e-mail já cadastrado")
        void existePorEmailRetornaTrueParaDuplicata() {
            repository.salvar(new Aluno("A001", "João", "joao@email.com", "123"));
            assertTrue(repository.existePorEmail("joao@email.com"));
        }

        @Test
        @DisplayName("existePorEmail é case-insensitive")
        void existePorEmailEhCaseInsensitive() {
            repository.salvar(new Aluno("A001", "João", "joao@email.com", "123"));
            assertTrue(repository.existePorEmail("JOAO@EMAIL.COM"));
            assertTrue(repository.existePorEmail("Joao@Email.Com"));
        }

        @Test
        @DisplayName("existePorEmail deve retornar false para e-mail inexistente")
        void existePorEmailRetornaFalseParaInexistente() {
            org.junit.jupiter.api.Assertions.assertFalse(repository.existePorEmail("naoexiste@email.com"));
        }

        @Test
        @DisplayName("RF04 — Repositório vazio: métodos de existência retornam false")
        void retornamFalseEmRepositorioVazio() {
            org.junit.jupiter.api.Assertions.assertFalse(repository.existePorMatricula("A001"));
            org.junit.jupiter.api.Assertions.assertFalse(repository.existePorEmail("qualquer@email.com"));
        }

        @Test
        @DisplayName("RF04 — Múltiplos usuários: cada matrícula deve ser detectada individualmente")
        void multiplosUsuariosMatriculasUnicas() {
            repository.salvar(new Aluno("A001", "João", "joao@email.com", "123"));
            repository.salvar(new Professor("P001", "Maria", "maria@email.com", "456"));
            repository.salvar(new Coordenador("C001", "Carlos", "carlos@email.com", "789"));

            assertTrue(repository.existePorMatricula("A001"));
            assertTrue(repository.existePorMatricula("P001"));
            assertTrue(repository.existePorMatricula("C001"));
            org.junit.jupiter.api.Assertions.assertFalse(repository.existePorMatricula("A002"));
        }

        @Test
        @DisplayName("RF04 — Após deleção, matrícula e e-mail não devem mais existir")
        void aposDelecaoMatriculaNaoExisteMais() {
            repository.salvar(new Aluno("A001", "João", "joao@email.com", "123"));
            repository.deletar("A001");

            org.junit.jupiter.api.Assertions.assertFalse(repository.existePorMatricula("A001"));
            org.junit.jupiter.api.Assertions.assertFalse(repository.existePorEmail("joao@email.com"));
        }
    }
}
