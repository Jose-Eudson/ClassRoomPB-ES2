package com.classroompb.repository;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import com.classroompb.model.Administrador;
import com.classroompb.model.Aluno;
import com.classroompb.model.Coordenador;
import com.classroompb.model.Professor;
import com.classroompb.model.Usuario;

@DisplayName("Testes de UsuarioRepository")
public class UsuarioRepositoryTest {

    @TempDir
    Path tempDir;

    private UsuarioRepository repository;

    private String arquivoTemp() {
        return tempDir.resolve("usuarios_test.json").toString();
    }

    @BeforeEach
    void setUp() {
        repository = new UsuarioRepository(arquivoTemp());
    }

    @Nested
    @DisplayName("Persistencia em disco")
    class Persistencia {

        @Test
        @DisplayName("Deve salvar e recuperar usuario em nova instancia do repositorio")
        void deveSalvarEPersistirUsuario() {
            Usuario aluno = new Aluno("A001", "Joao", "joao@email.com", "123");
            repository.salvar(aluno);

            UsuarioRepository novoRepo = new UsuarioRepository(arquivoTemp());
            Optional<Usuario> buscado = novoRepo.buscarPorMatricula("A001");

            assertTrue(buscado.isPresent());
            assertEquals("Joao", buscado.get().getNome());
        }

        @Test
        @DisplayName("Deve persistir multiplos usuarios e recupera-los corretamente")
        void devePersistirMultiplosUsuarios() {
            repository.salvar(new Aluno("A001", "Joao", "joao@email.com", "123"));
            repository.salvar(new Professor("P001", "Maria", "maria@email.com", "456"));
            repository.salvar(new Coordenador("C001", "Carlos", "carlos@email.com", "789"));
            repository.salvar(new Administrador("AD001", "Admin", "admin@email.com", "000"));

            UsuarioRepository novoRepo = new UsuarioRepository(arquivoTemp());
            List<Usuario> todos = novoRepo.listarTodos();

            assertEquals(4, todos.size());
        }

        @Test
        @DisplayName("Deve iniciar com lista vazia quando nao existe arquivo")
        void deveIniciarVazioSemArquivo() {
            assertTrue(repository.listarTodos().isEmpty());
        }
    }

    @Nested
    @DisplayName("Salvar")
    class Salvar {

        @Test
        @DisplayName("Deve salvar e listar dois usuarios")
        void deveSalvarDoisUsuarios() {
            repository.salvar(new Aluno("A001", "Joao", "joao@email.com", "123"));
            repository.salvar(new Aluno("A002", "Maria", "maria@email.com", "456"));

            assertEquals(2, repository.listarTodos().size());
        }
    }

    @Nested
    @DisplayName("Busca")
    class Busca {

        @Test
        @DisplayName("Deve buscar por matricula existente")
        void deveBuscarPorMatriculaExistente() {
            repository.salvar(new Aluno("A001", "Joao", "joao@email.com", "123"));

            Optional<Usuario> resultado = repository.buscarPorMatricula("A001");

            assertTrue(resultado.isPresent());
            assertEquals("A001", resultado.get().getMatricula());
        }

        @Test
        @DisplayName("Deve retornar vazio para matricula inexistente")
        void deveRetornarVazioParaMatriculaInexistente() {
            assertTrue(!repository.buscarPorMatricula("X999").isPresent());
        }

        @Test
        @DisplayName("Deve buscar por e-mail de forma case-insensitive")
        void deveBuscarPorEmailCaseInsensitive() {
            repository.salvar(new Aluno("A001", "Joao", "joao@email.com", "123"));

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
        @DisplayName("Deve atualizar usuario e persistir a mudanca")
        void deveAtualizarUsuario() {
            Usuario aluno = new Aluno("A001", "Joao", "joao@email.com", "123");
            repository.salvar(aluno);

            aluno.setNome("Joao Silva");
            repository.atualizar(aluno);

            UsuarioRepository novoRepo = new UsuarioRepository(arquivoTemp());
            assertEquals("Joao Silva", novoRepo.buscarPorMatricula("A001").get().getNome());
        }

        @Test
        @DisplayName("Nao deve atualizar usuario inexistente - deve lancar excecao")
        void deveLancarExcecaoAoAtualizarInexistente() {
            Usuario fantasma = new Aluno("999", "Fantasma", "fantasma@email.com", "senha");

            assertThrows(IllegalArgumentException.class, () -> repository.atualizar(fantasma));
        }
    }

    @Nested
    @DisplayName("Delecao de usuario")
    class Deletar {

        @Test
        @DisplayName("Deve deletar usuario e confirmar remocao")
        void deveDeletarUsuario() {
            repository.salvar(new Aluno("A001", "Joao", "joao@email.com", "123"));
            repository.deletar("A001");

            assertTrue(repository.listarTodos().isEmpty());
        }

        @Test
        @DisplayName("Delecao deve ser persistida - nova instancia nao deve encontrar o usuario")
        void devePersistirDelecao() {
            repository.salvar(new Aluno("A001", "Joao", "joao@email.com", "123"));
            repository.deletar("A001");

            UsuarioRepository novoRepo = new UsuarioRepository(arquivoTemp());
            assertTrue(novoRepo.listarTodos().isEmpty());
        }

        @Test
        @DisplayName("Nao deve deletar matricula inexistente - deve lancar excecao")
        void deveLancarExcecaoAoDeletarInexistente() {
            assertThrows(IllegalArgumentException.class, () -> repository.deletar("999"));
        }

        @Test
        @DisplayName("Nao deve deletar usuario ja removido - deve lancar excecao")
        void naoDevePermitirDuplaDeleção() {
            repository.salvar(new Aluno("A001", "Joao", "joao@email.com", "123"));
            repository.deletar("A001");

            assertThrows(IllegalArgumentException.class, () -> repository.deletar("A001"));
        }
    }

    @Nested
    @DisplayName("RF04 - Prevencao de cadastro duplicado")
    class PrevenirCadastroDuplicado {

        @Test
        @DisplayName("existePorMatricula deve retornar true para matricula ja cadastrada")
        void existePorMatriculaRetornaTrueParaDuplicata() {
            repository.salvar(new Aluno("A001", "Joao", "joao@email.com", "123"));
            assertTrue(repository.existePorMatricula("A001"));
        }

        @Test
        @DisplayName("existePorMatricula deve retornar false para matricula inexistente")
        void existePorMatriculaRetornaFalseParaInexistente() {
            assertFalse(repository.existePorMatricula("A999"));
        }

        @Test
        @DisplayName("existePorEmail deve retornar true para e-mail ja cadastrado")
        void existePorEmailRetornaTrueParaDuplicata() {
            repository.salvar(new Aluno("A001", "Joao", "joao@email.com", "123"));
            assertTrue(repository.existePorEmail("joao@email.com"));
        }

        @Test
        @DisplayName("existePorEmail eh case-insensitive")
        void existePorEmailEhCaseInsensitive() {
            repository.salvar(new Aluno("A001", "Joao", "joao@email.com", "123"));
            assertTrue(repository.existePorEmail("JOAO@EMAIL.COM"));
            assertTrue(repository.existePorEmail("Joao@Email.Com"));
        }

        @Test
        @DisplayName("existePorEmail deve retornar false para e-mail inexistente")
        void existePorEmailRetornaFalseParaInexistente() {
            assertFalse(repository.existePorEmail("naoexiste@email.com"));
        }

        @Test
        @DisplayName("RF04 - Repositorio vazio: metodos de existencia retornam false")
        void retornamFalseEmRepositorioVazio() {
            assertFalse(repository.existePorMatricula("A001"));
            assertFalse(repository.existePorEmail("qualquer@email.com"));
        }

        @Test
        @DisplayName("RF04 - Multiplos usuarios: cada matricula deve ser detectada individualmente")
        void multiplosUsuariosMatriculasUnicas() {
            repository.salvar(new Aluno("A001", "Joao", "joao@email.com", "123"));
            repository.salvar(new Professor("P001", "Maria", "maria@email.com", "456"));
            repository.salvar(new Coordenador("C001", "Carlos", "carlos@email.com", "789"));

            assertTrue(repository.existePorMatricula("A001"));
            assertTrue(repository.existePorMatricula("P001"));
            assertTrue(repository.existePorMatricula("C001"));
            assertFalse(repository.existePorMatricula("A002"));
        }

        @Test
        @DisplayName("RF04 - Apos delecao, matricula e e-mail nao devem mais existir")
        void aposDelecaoMatriculaNaoExisteMais() {
            repository.salvar(new Aluno("A001", "Joao", "joao@email.com", "123"));
            repository.deletar("A001");

            assertFalse(repository.existePorMatricula("A001"));
            assertFalse(repository.existePorEmail("joao@email.com"));
        }
    }

    @Nested
    @DisplayName("Tratamento de erros de IO")
    class TratamentoDeErrosDeIO {

        @Test
        @DisplayName("Construtor padrao deve inicializar sem excecao")
        void construtorPadraoNaoLancaExcecao() {
            assertDoesNotThrow(() -> new UsuarioRepository());
        }

        @Test
        @DisplayName("carregarDados deve iniciar lista vazia ao receber caminho invalido (diretorio)")
        void carregarDadosComCaminhoInvalidoInicialistaVazia() {
            String caminhoInvalido = tempDir.toString();
            UsuarioRepository repo = new UsuarioRepository(caminhoInvalido);
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

            String caminhoInvalido = dirReadOnly.getAbsolutePath() + "/usuarios.json";
            UsuarioRepository repo = new UsuarioRepository(caminhoInvalido);

            assertDoesNotThrow(() -> repo.salvar(new Aluno("A001", "Joao", "joao@email.com", "123")));
        }
    }
}