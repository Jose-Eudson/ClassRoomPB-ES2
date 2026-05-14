package com.classroompb.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.classroompb.model.Aluno;
import com.classroompb.model.Professor;
import com.classroompb.model.TipoUsuario;
import com.classroompb.model.Usuario;
import com.classroompb.repository.UsuarioRepository;

/**
 * Testes unitarios para o fluxo de login em UsuarioService.
 * Cobre login por e-mail, login por matricula (RF02) e todos os casos de erro.
 */

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitarios de Login (RF02)")
public class LoginServiceTest {

    @Mock
    private UsuarioRepository repository;

    private UsuarioService service;

    @BeforeEach
    void setUp() {
        service = new UsuarioService(repository);
    }

    // =========================================================================
    // LOGIN POR E-MAIL
    // =========================================================================

    @Nested
    @DisplayName("Login por e-mail")
    class LoginPorEmail {

        @Test
        @DisplayName("Deve fazer login com e-mail e senha corretos")
        void deveFazerLoginPorEmailComSucesso() throws Exception {
            String email = "usuario@teste.com";
            String senha = "senha123";
            Usuario usuarioMock = mock(Usuario.class);
            when(usuarioMock.getSenha()).thenReturn(senha);
            when(repository.buscarPorEmail(email)).thenReturn(Optional.of(usuarioMock));

            Usuario resultado = service.login(email, senha);

            assertNotNull(resultado);
            assertEquals(usuarioMock, resultado);
            verify(repository).buscarPorEmail(email);
        }

        @Test
        @DisplayName("Deve retornar os dados completos do usuario logado por e-mail")
        void deveRetornarDadosDoUsuarioLogadoPorEmail() throws Exception {
            String email = "joao@teste.com";
            String senha = "1234";
            Usuario joao = new Aluno("A0001", "Joao", email, senha);
            when(repository.buscarPorEmail(email)).thenReturn(Optional.of(joao));

            Usuario logado = service.login(email, senha);

            assertEquals("Joao", logado.getNome());
            assertEquals("A0001", logado.getMatricula());
            assertEquals(email, logado.getEmail());
            assertEquals(TipoUsuario.ALUNO, logado.getTipo());
        }

        @Test
        @DisplayName("Nao deve fazer login com e-mail inexistente")
        void deveFalharLoginComEmailInexistente() {
            when(repository.buscarPorEmail("inexistente@teste.com")).thenReturn(Optional.empty());

            Exception ex = assertThrows(Exception.class, () ->
                    service.login("inexistente@teste.com", "qualquer_senha"));

            assertEquals("Erro: Usuário não encontrado.", ex.getMessage());
        }

        @Test
        @DisplayName("Nao deve fazer login com senha incorreta via e-mail")
        void deveFalharLoginComSenhaIncorretaViaEmail() {
            Usuario usuarioMock = mock(Usuario.class);
            when(usuarioMock.getSenha()).thenReturn("correta");
            when(repository.buscarPorEmail("usuario@teste.com")).thenReturn(Optional.of(usuarioMock));

            Exception ex = assertThrows(Exception.class, () ->
                    service.login("usuario@teste.com", "errada"));

            assertEquals("Erro: Senha incorreta.", ex.getMessage());
        }

        @Test
        @DisplayName("Login por e-mail e buscado de forma case-insensitive no repositorio")
        void loginEmailRepassadoAoRepositorioSemAlteracao() throws Exception {
            String emailLogin = "Usuario@Teste.com";
            Usuario usuarioMock = mock(Usuario.class);
            when(usuarioMock.getSenha()).thenReturn("123");
            when(repository.buscarPorEmail(emailLogin)).thenReturn(Optional.of(usuarioMock));

            Usuario resultado = service.login(emailLogin, "123");

            assertNotNull(resultado);
            verify(repository).buscarPorEmail(emailLogin);
        }
    }

    // =========================================================================
    // LOGIN POR MATRICULA (RF02)
    // =========================================================================

    @Nested
    @DisplayName("Login por matricula (RF02)")
    class LoginPorMatricula {

        @Test
        @DisplayName("Deve fazer login com matricula de aluno e senha corretos")
        void deveFazerLoginPorMatriculaDeAluno() throws Exception {
            String matricula = "A0001";
            String senha = "senha123";
            Usuario aluno = new Aluno(matricula, "Carlos", "carlos@teste.com", senha);
            when(repository.buscarPorMatricula(matricula)).thenReturn(Optional.of(aluno));

            Usuario logado = service.login(matricula, senha);

            assertNotNull(logado);
            assertEquals(matricula, logado.getMatricula());
            assertEquals(TipoUsuario.ALUNO, logado.getTipo());
            verify(repository).buscarPorMatricula(matricula);
        }

        @Test
        @DisplayName("Deve fazer login com matricula de professor")
        void deveFazerLoginPorMatriculaDeProfessor() throws Exception {
            String matricula = "P0001";
            String senha = "prof123";
            Usuario prof = new Professor(matricula, "Maria", "maria@teste.com", senha);
            when(repository.buscarPorMatricula(matricula)).thenReturn(Optional.of(prof));

            Usuario logado = service.login(matricula, senha);

            assertNotNull(logado);
            assertEquals(TipoUsuario.PROFESSOR, logado.getTipo());
            verify(repository).buscarPorMatricula(matricula);
        }

        @Test
        @DisplayName("Deve fazer login com matricula de coordenador")
        void deveFazerLoginPorMatriculaDeCoordenador() throws Exception {
            String matricula = "C0001";
            Usuario mock = mock(Usuario.class);
            when(mock.getSenha()).thenReturn("coord");
            when(repository.buscarPorMatricula(matricula)).thenReturn(Optional.of(mock));

            assertNotNull(service.login(matricula, "coord"));
        }

        @Test
        @DisplayName("Deve fazer login com matricula de administrador")
        void deveFazerLoginPorMatriculaDeAdministrador() throws Exception {
            String matricula = "AD0001";
            Usuario mock = mock(Usuario.class);
            when(mock.getSenha()).thenReturn("admin");
            when(repository.buscarPorMatricula(matricula)).thenReturn(Optional.of(mock));

            assertNotNull(service.login(matricula, "admin"));
        }

        @Test
        @DisplayName("Nao deve fazer login com matricula inexistente")
        void deveFalharLoginComMatriculaInexistente() {
            when(repository.buscarPorMatricula("A9999")).thenReturn(Optional.empty());

            Exception ex = assertThrows(Exception.class, () ->
                    service.login("A9999", "qualquer_senha"));

            assertEquals("Erro: Usuário não encontrado.", ex.getMessage());
        }

        @Test
        @DisplayName("Nao deve fazer login com senha incorreta via matricula")
        void deveFalharLoginComSenhaIncorretaViaMatricula() {
            Usuario usuarioMock = mock(Usuario.class);
            when(usuarioMock.getSenha()).thenReturn("correta");
            when(repository.buscarPorMatricula("A0001")).thenReturn(Optional.of(usuarioMock));

            Exception ex = assertThrows(Exception.class, () ->
                    service.login("A0001", "errada"));

            assertEquals("Erro: Senha incorreta.", ex.getMessage());
        }

        @Test
        @DisplayName("Senha e case-sensitive no login por matricula")
        void senhaECaseSensitiveNoLoginPorMatricula() {
            Usuario usuarioMock = mock(Usuario.class);
            when(usuarioMock.getSenha()).thenReturn("Senha123");
            when(repository.buscarPorMatricula("A0001")).thenReturn(Optional.of(usuarioMock));

            Exception ex = assertThrows(Exception.class, () ->
                    service.login("A0001", "senha123"));

            assertEquals("Erro: Senha incorreta.", ex.getMessage());
        }
    }

    // =========================================================================
    // IDENTIFICADOR VAZIO OU NULO
    // =========================================================================

    @Nested
    @DisplayName("Identificador ou senha vazios/nulos")
    class CamposVazios {

        @Test
        @DisplayName("Nao deve fazer login com identificador nulo")
        void deveFalharComIdentificadorNulo() {
            Exception ex = assertThrows(Exception.class, () -> service.login(null, "senha"));
            assertEquals("Erro: Matrícula ou e-mail não pode ser vazio.", ex.getMessage());
        }

        @Test
        @DisplayName("Nao deve fazer login com identificador vazio")
        void deveFalharComIdentificadorVazio() {
            Exception ex = assertThrows(Exception.class, () -> service.login("", "senha"));
            assertEquals("Erro: Matrícula ou e-mail não pode ser vazio.", ex.getMessage());
        }

        @Test
        @DisplayName("Nao deve fazer login com identificador apenas espacos")
        void deveFalharComIdentificadorApenasEspacos() {
            Exception ex = assertThrows(Exception.class, () -> service.login("   ", "senha"));
            assertEquals("Erro: Matrícula ou e-mail não pode ser vazio.", ex.getMessage());
        }

        @Test
        @DisplayName("Nao deve fazer login com senha nula")
        void deveFalharComSenhaNula() {
            Exception ex = assertThrows(Exception.class, () -> service.login("email@teste.com", null));
            assertEquals("Erro: Senha não pode ser vazia.", ex.getMessage());
        }

        @Test
        @DisplayName("Nao deve fazer login com senha vazia")
        void deveFalharComSenhaVazia() {
            Exception ex = assertThrows(Exception.class, () -> service.login("email@teste.com", ""));
            assertEquals("Erro: Senha não pode ser vazia.", ex.getMessage());
        }

        @Test
        @DisplayName("Nao deve fazer login com senha apenas espacos")
        void deveFalharComSenhaApenasEspacos() {
            Exception ex = assertThrows(Exception.class, () -> service.login("A0001", "   "));
            assertEquals("Erro: Senha não pode ser vazia.", ex.getMessage());
        }
    }
}