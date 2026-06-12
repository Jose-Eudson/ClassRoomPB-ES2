package com.classroompb.repository;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
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

/**
 * Testes de integração dedicados à validação de duplicidade no UsuarioRepository.
 *
 * RF04: O sistema deve impedir cadastro duplicado por matrícula ou e-mail.
 *
 * Complementa os testes de PrevenirCadastroDuplicado em UsuarioRepositoryTest cobrindo cenários de borda: persistência
 * entre sessões, todos os tipos de usuário, variações de casing no e-mail e sequências de operações.
 */
@DisplayName("RF04 — Validação de duplicidade no UsuarioRepository")
public class DuplicidadeRepositoryTest {

    @TempDir
    Path tempDir;

    private UsuarioRepository repository;

    private String arquivoTemp() {
        return tempDir.resolve("usuarios_dup_test.json").toString();
    }

    @BeforeEach
    void setUp() {
        repository = new UsuarioRepository(arquivoTemp());
    }

    // =========================================================================
    // UNICIDADE DE MATRÍCULA
    // =========================================================================

    @Nested
    @DisplayName("Unicidade de matrícula — existePorMatricula")
    class UniidadeDeMatricula {

        @Test
        @DisplayName("RF04 — Deve detectar matrícula de Aluno já cadastrada")
        void deveDetectarMatriculaDeAlunoCadastrada() {
            repository.salvar(new Aluno("A0001", "João", "joao@e.com", "123"));
            assertTrue(repository.existePorMatricula("A0001"));
        }

        @Test
        @DisplayName("RF04 — Deve detectar matrícula de Professor já cadastrada")
        void deveDetectarMatriculaDeProfessorCadastrada() {
            repository.salvar(new Professor("P0001", "Maria", "maria@e.com", "123"));
            assertTrue(repository.existePorMatricula("P0001"));
        }

        @Test
        @DisplayName("RF04 — Deve detectar matrícula de Coordenador já cadastrada")
        void deveDetectarMatriculaDeCoordenadorCadastrada() {
            repository.salvar(new Coordenador("C0001", "Ana", "ana@e.com", "123"));
            assertTrue(repository.existePorMatricula("C0001"));
        }

        @Test
        @DisplayName("RF04 — Deve detectar matrícula de Administrador (prefixo AD) já cadastrada")
        void deveDetectarMatriculaDeAdministradorCadastrada() {
            repository.salvar(new Administrador("AD0001", "Root", "root@e.com", "123"));
            assertTrue(repository.existePorMatricula("AD0001"));
        }

        @Test
        @DisplayName("RF04 — Deve retornar false para matrícula inexistente em repositório populado")
        void deveRetornarFalseParaMatriculaInexistenteEmRepositorioPopulado() {
            repository.salvar(new Aluno("A0001", "João", "joao@e.com", "123"));
            repository.salvar(new Professor("P0001", "Maria", "maria@e.com", "456"));

            assertFalse(repository.existePorMatricula("A0002"));
            assertFalse(repository.existePorMatricula("P0002"));
            assertFalse(repository.existePorMatricula("C0001"));
        }

        @Test
        @DisplayName("RF04 — Matrícula não deve ser detectada como duplicata de prefixo semelhante")
        void naoDeveConfundirMatriculasComPrefixosSemelhantes() {
            // "AD0001" começa com 'A', mas não deve ser confundido com "A0001"
            repository.salvar(new Administrador("AD0001", "Admin", "admin@e.com", "123"));

            assertFalse(repository.existePorMatricula("A0001"),
                    "A matrícula A0001 não deve ser detectada como duplicata de AD0001");
            assertTrue(repository.existePorMatricula("AD0001"));
        }

        @Test
        @DisplayName("RF04 — existePorMatricula é sensível a maiúsculas e minúsculas (matrícula exata)")
        void existePorMatriculaEhCaseSensitive() {
            repository.salvar(new Aluno("A0001", "João", "joao@e.com", "123"));

            assertFalse(repository.existePorMatricula("a0001"),
                    "Matrícula deve ser comparada de forma exata (case-sensitive)");
        }

        @Test
        @DisplayName("RF04 — Após deleção, matrícula deve deixar de ser detectada como duplicata")
        void aposDelecaoMatriculaNaoDeveSerDetectada() {
            repository.salvar(new Aluno("A0001", "João", "joao@e.com", "123"));
            repository.deletar("A0001");

            assertFalse(repository.existePorMatricula("A0001"));
        }

        @Test
        @DisplayName("RF04 — Detecção de matrícula deve ser preservada após reinício do repositório")
        void deveDetectarDuplicataAposReinicioDoRepositorio() {
            repository.salvar(new Aluno("A0001", "João", "joao@e.com", "123"));

            // Simula fechamento e reabertura do programa
            UsuarioRepository novoRepo = new UsuarioRepository(arquivoTemp());

            assertTrue(novoRepo.existePorMatricula("A0001"),
                    "Duplicata de matrícula deve ser detectada mesmo após reinício");
        }
    }

    // =========================================================================
    // UNICIDADE DE E-MAIL
    // =========================================================================

    @Nested
    @DisplayName("Unicidade de e-mail — existePorEmail")
    class UnicidadeDeEmail {

        @Test
        @DisplayName("RF04 — Deve detectar e-mail já cadastrado (mesmo casing)")
        void deveDetectarEmailDuplicadoMesmoCasing() {
            repository.salvar(new Aluno("A0001", "João", "joao@escola.com", "123"));
            assertTrue(repository.existePorEmail("joao@escola.com"));
        }

        @Test
        @DisplayName("RF04 — Deve detectar e-mail duplicado em maiúsculas (case-insensitive)")
        void deveDetectarEmailDuplicadoEmMaiusculas() {
            repository.salvar(new Aluno("A0001", "João", "joao@escola.com", "123"));
            assertTrue(repository.existePorEmail("JOAO@ESCOLA.COM"));
        }

        @Test
        @DisplayName("RF04 — Deve detectar e-mail duplicado em casing misto")
        void deveDetectarEmailDuplicadoEmCasingMisto() {
            repository.salvar(new Aluno("A0001", "João", "joao@escola.com", "123"));
            assertTrue(repository.existePorEmail("Joao@Escola.Com"));
            assertTrue(repository.existePorEmail("JOAO@escola.COM"));
        }

        @Test
        @DisplayName("RF04 — Deve retornar false para e-mail inexistente em repositório populado")
        void deveRetornarFalseParaEmailInexistente() {
            repository.salvar(new Aluno("A0001", "João", "joao@escola.com", "123"));

            assertFalse(repository.existePorEmail("maria@escola.com"));
            assertFalse(repository.existePorEmail("joao@outraescola.com"));
        }

        @Test
        @DisplayName("RF04 — Deve detectar e-mail duplicado para todos os tipos de usuário")
        void deveDetectarEmailDuplicadoParaTodosOsTipos() {
            repository.salvar(new Aluno("A0001", "Aluno", "aluno@e.com", "123"));
            repository.salvar(new Professor("P0001", "Prof", "prof@e.com", "123"));
            repository.salvar(new Coordenador("C0001", "Coord", "coord@e.com", "123"));
            repository.salvar(new Administrador("AD0001", "Admin", "admin@e.com", "123"));

            assertTrue(repository.existePorEmail("aluno@e.com"));
            assertTrue(repository.existePorEmail("prof@e.com"));
            assertTrue(repository.existePorEmail("coord@e.com"));
            assertTrue(repository.existePorEmail("admin@e.com"));
        }

        @Test
        @DisplayName("RF04 — Após deleção, e-mail deve deixar de ser detectado como duplicata")
        void aposDelecaoEmailNaoDeveSerDetectado() {
            repository.salvar(new Aluno("A0001", "João", "joao@escola.com", "123"));
            repository.deletar("A0001");

            assertFalse(repository.existePorEmail("joao@escola.com"));
            assertFalse(repository.existePorEmail("JOAO@ESCOLA.COM"));
        }

        @Test
        @DisplayName("RF04 — Detecção de e-mail duplicado deve ser preservada após reinício do repositório")
        void deveDetectarEmailDuplicadoAposReinicioDoRepositorio() {
            repository.salvar(new Aluno("A0001", "João", "joao@escola.com", "123"));

            // Simula fechamento e reabertura do programa
            UsuarioRepository novoRepo = new UsuarioRepository(arquivoTemp());

            assertTrue(novoRepo.existePorEmail("joao@escola.com"),
                    "Duplicata de e-mail deve ser detectada mesmo após reinício");
            assertTrue(novoRepo.existePorEmail("JOAO@ESCOLA.COM"),
                    "Detecção case-insensitive deve funcionar após reinício");
        }
    }

    // =========================================================================
    // SEQUÊNCIAS DE OPERAÇÕES
    // =========================================================================

    @Nested
    @DisplayName("Sequências de operações — integridade da unicidade")
    class SequenciasDeOperacoes {

        @Test
        @DisplayName("RF04 — Cadastrar, deletar e recadastrar mesma matrícula deve funcionar")
        void deveCadastrarAposDelecaoComMesmaMatricula() {
            repository.salvar(new Aluno("A0001", "João", "joao@e.com", "123"));
            repository.deletar("A0001");

            // Nova instância com mesma matrícula não deve ser barrada
            assertFalse(repository.existePorMatricula("A0001"),
                    "Após deleção, matrícula deve estar disponível para reutilização");
        }

        @Test
        @DisplayName("RF04 — Atualização de e-mail libera e-mail antigo para outros usuários")
        void atualizacaoDeEmailLiberaEmailAntigo() {
            Aluno joao = new Aluno("A0001", "João", "joao@e.com", "123");
            repository.salvar(joao);

            joao.setEmail("novoemail@e.com");
            repository.atualizar(joao);

            // E-mail antigo não deve mais ser detectado como duplicata
            assertFalse(repository.existePorEmail("joao@e.com"),
                    "E-mail antigo deve estar disponível após atualização");
            // Novo e-mail deve ser detectado
            assertTrue(repository.existePorEmail("novoemail@e.com"));
        }

        @Test
        @DisplayName("RF04 — Dois usuários com e-mails distintos não se interferem na detecção de duplicidade")
        void doisUsuariosComEmailsDistintosNaoSeInterferem() {
            repository.salvar(new Aluno("A0001", "Alice", "alice@e.com", "123"));
            repository.salvar(new Aluno("A0002", "Bob", "bob@e.com", "456"));

            assertTrue(repository.existePorEmail("alice@e.com"));
            assertTrue(repository.existePorEmail("bob@e.com"));
            assertFalse(repository.existePorEmail("carlos@e.com"));

            // Deleção de um não afeta o outro
            repository.deletar("A0001");
            assertFalse(repository.existePorEmail("alice@e.com"));
            assertTrue(repository.existePorEmail("bob@e.com"));
        }

        @Test
        @DisplayName("RF04 — Estado de duplicidade é consistente entre instâncias do repositório")
        void estadoDeDuplicidadeEConsistenteEntreInstancias() {
            repository.salvar(new Aluno("A0001", "Alice", "alice@e.com", "123"));
            repository.salvar(new Professor("P0001", "Bob", "bob@e.com", "456"));
            repository.deletar("A0001");

            UsuarioRepository outraInstancia = new UsuarioRepository(arquivoTemp());

            assertFalse(outraInstancia.existePorMatricula("A0001"));
            assertFalse(outraInstancia.existePorEmail("alice@e.com"));
            assertTrue(outraInstancia.existePorMatricula("P0001"));
            assertTrue(outraInstancia.existePorEmail("bob@e.com"));
        }
    }
}
