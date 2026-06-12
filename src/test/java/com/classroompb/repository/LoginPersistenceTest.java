package com.classroompb.repository;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.classroompb.model.Aluno;
import com.classroompb.model.Professor;
import com.classroompb.model.Usuario;
import com.classroompb.service.UsuarioService;

/**
 * Testes de persistência do fluxo de login.
 *
 * Simula o ciclo "fechar e reabrir o programa" criando novas instâncias de UsuarioRepository que leem do mesmo arquivo
 * temporário. O @TempDir garante isolamento total entre testes.
 */

@DisplayName("Testes de persistência de Login")
public class LoginPersistenceTest {

    @TempDir
    Path tempDir;

    private UsuarioRepository repository;

    /** Retorna o caminho do arquivo JSON temporário isolado por teste. */
    private String arquivoTemp() {
        return tempDir.resolve("usuarios_test.json").toString();
    }

    /** Inicializa repositório e serviço com o arquivo temporário antes de cada teste. */
    @BeforeEach
    void setUp() {
        repository = new UsuarioRepository(arquivoTemp());
    }

    /** Testes que simulam o comportamento após fechar e reabrir o programa. */
    @Nested
    @DisplayName("Login após reinicialização")
    class LoginAposReinicio {

        @Test
        @DisplayName("Deve permitir login após fechar e reabrir o programa")
        void devePersistirUsuarioEPermitirLoginAposReiniciarRepositorio() throws Exception {
            String email = "persist@teste.com";
            String senha = "123";
            repository.salvar(new Aluno("A123", "Persistente", email, senha));

            // Simula fechamento e reabertura do programa
            UsuarioRepository novoRepo = new UsuarioRepository(arquivoTemp());
            UsuarioService novoService = new UsuarioService(novoRepo);

            Usuario logado = novoService.login(email, senha);

            assertNotNull(logado);
            assertEquals("A123", logado.getMatricula());
            assertEquals("Persistente", logado.getNome());
        }

        @Test
        @DisplayName("Não deve permitir login com dados que nunca foram salvos")
        void naoDevePermitirLoginSeDadosNaoForamPersistidos() {
            UsuarioRepository novoRepo = new UsuarioRepository(arquivoTemp());
            UsuarioService novoService = new UsuarioService(novoRepo);

            assertThrows(Exception.class, () -> novoService.login("naoexiste@teste.com", "123"));
        }

        @Test
        @DisplayName("Não deve encontrar usuário deletado após reinicialização")
        void naoDeveEncontrarUsuarioDeletadoAposReinicio() throws Exception {
            repository.salvar(new Aluno("A001", "João", "joao@teste.com", "senha"));
            repository.deletar("A001");

            UsuarioRepository novoRepo = new UsuarioRepository(arquivoTemp());
            UsuarioService novoService = new UsuarioService(novoRepo);

            assertThrows(Exception.class, () -> novoService.login("joao@teste.com", "senha"));
        }

        @Test
        @DisplayName("Deve persistir atualização de senha e refletir no login")
        void devePersistirAtualizacaoDeSenha() throws Exception {
            Usuario aluno = new Aluno("A001", "João", "joao@teste.com", "senhaAntiga");
            repository.salvar(aluno);

            aluno.setSenha("senhaNova");
            repository.atualizar(aluno);

            UsuarioRepository novoRepo = new UsuarioRepository(arquivoTemp());
            UsuarioService novoService = new UsuarioService(novoRepo);

            // Nova senha deve funcionar
            assertNotNull(novoService.login("joao@teste.com", "senhaNova"));

            // Senha antiga não deve mais funcionar
            assertThrows(Exception.class, () -> novoService.login("joao@teste.com", "senhaAntiga"));
        }
    }

    /** Testes com múltiplos usuários salvos — garante que credenciais não se misturam. */
    @Nested
    @DisplayName("Múltiplos usuários persistidos")
    class MultiplosUsuarios {

        @Test
        @DisplayName("Deve permitir login de cada usuário individualmente após reinício")
        void devePermitirLoginDeMultiplosUsuariosAposReinicio() throws Exception {
            repository.salvar(new Aluno("A001", "Alice", "alice@teste.com", "pass1"));
            repository.salvar(new Professor("P001", "Bob", "bob@teste.com", "pass2"));

            UsuarioRepository novoRepo = new UsuarioRepository(arquivoTemp());
            UsuarioService novoService = new UsuarioService(novoRepo);

            Usuario alice = novoService.login("alice@teste.com", "pass1");
            Usuario bob = novoService.login("bob@teste.com", "pass2");

            assertEquals("A001", alice.getMatricula());
            assertEquals("P001", bob.getMatricula());
        }

        @Test
        @DisplayName("Não deve misturar senhas entre usuários distintos")
        void naoDeveMisturarSenhasEntreUsuarios() throws Exception {
            repository.salvar(new Aluno("A001", "Alice", "alice@teste.com", "senhaAlice"));
            repository.salvar(new Aluno("A002", "Bob", "bob@teste.com", "senhaBob"));

            UsuarioRepository novoRepo = new UsuarioRepository(arquivoTemp());
            UsuarioService novoService = new UsuarioService(novoRepo);

            // Alice não pode logar com a senha de Bob e vice-versa
            assertThrows(Exception.class, () -> novoService.login("alice@teste.com", "senhaBob"));
            assertThrows(Exception.class, () -> novoService.login("bob@teste.com", "senhaAlice"));
        }
    }

    /** Testa persistencia do login por matricula apos fechar e reabrir o programa. */
    @Nested
    @DisplayName("Login por matricula apos reinicializacao (RF02)")
    class LoginPorMatriculaAposReinicio {

        @Test
        @DisplayName("Deve permitir login por matricula apos persistencia")
        void devePermitirLoginPorMatriculaAposReinicio() throws Exception {
            repository.salvar(new Aluno("A0001", "Joao", "joao@teste.com", "senha"));

            UsuarioRepository novoRepo = new UsuarioRepository(arquivoTemp());
            UsuarioService novoService = new UsuarioService(novoRepo);

            Usuario logado = novoService.login("A0001", "senha");

            assertNotNull(logado);
            assertEquals("A0001", logado.getMatricula());
        }

        @Test
        @DisplayName("Nao deve fazer login com matricula incorreta apos persistencia")
        void naoDevePermitirLoginComMatriculaErradaAposReinicio() {
            repository.salvar(new Aluno("A0001", "Joao", "joao@teste.com", "senha"));

            UsuarioRepository novoRepo = new UsuarioRepository(arquivoTemp());
            UsuarioService novoService = new UsuarioService(novoRepo);

            assertThrows(Exception.class, () -> novoService.login("A9999", "senha"));
        }
    }
}
