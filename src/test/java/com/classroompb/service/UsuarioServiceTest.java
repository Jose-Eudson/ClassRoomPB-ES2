package com.classroompb.service;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.classroompb.model.Administrador;
import com.classroompb.model.Aluno;
import com.classroompb.model.Coordenador;
import com.classroompb.model.Professor;
import com.classroompb.model.TipoUsuario;
import com.classroompb.model.Usuario;
import com.classroompb.repository.UsuarioRepository;

/**
 * Testes unitários completos de UsuarioService.
 * Usa Mockito para isolar o serviço do repositório real.
 * Cobre cadastro, login, edição, deleção e listagem.
 */

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitários de UsuarioService")
public class UsuarioServiceTest {

    @Mock
    private UsuarioRepository repository;

    private UsuarioService service;

    /** Cria o serviço com o repositório mockado antes de cada teste. */
    @BeforeEach
    void setUp() {
        service = new UsuarioService(repository);
    }

    // =========================================================================
    // CADASTRO DE USUÁRIOS
    // =========================================================================

    /** Testes do método cadastrarUsuario e cadastrarUsuarioComMatriculaAutomatica. */
    @Nested
    @DisplayName("Cadastro de usuários")
    class CadastroDeUsuarios {

        @Test
        @DisplayName("Deve cadastrar aluno com sucesso")
        void deveCadastrarAlunoComSucesso() throws Exception {
            when(repository.buscarPorMatricula("A001")).thenReturn(Optional.empty());
            when(repository.buscarPorEmail("aluno@teste.com")).thenReturn(Optional.empty());

            service.cadastrarUsuario("A001", "Carlos", "aluno@teste.com", "senha123", TipoUsuario.ALUNO);

            verify(repository, times(1)).salvar(any(Aluno.class));
        }

        @Test
        @DisplayName("Deve cadastrar professor com sucesso")
        void deveCadastrarProfessorComSucesso() throws Exception {
            when(repository.buscarPorMatricula("P001")).thenReturn(Optional.empty());
            when(repository.buscarPorEmail("prof@teste.com")).thenReturn(Optional.empty());

            service.cadastrarUsuario("P001", "Prof Maria", "prof@teste.com", "senha", TipoUsuario.PROFESSOR);

            verify(repository, times(1)).salvar(any(Professor.class));
        }

        @Test
        @DisplayName("Deve cadastrar coordenador com sucesso")
        void deveCadastrarCoordenadorComSucesso() throws Exception {
            when(repository.buscarPorMatricula("C001")).thenReturn(Optional.empty());
            when(repository.buscarPorEmail("coord@teste.com")).thenReturn(Optional.empty());

            service.cadastrarUsuario("C001", "Coord Ana", "coord@teste.com", "senha", TipoUsuario.COORDENADOR);

            verify(repository, times(1)).salvar(any(Coordenador.class));
        }

        @Test
        @DisplayName("Deve cadastrar administrador com sucesso")
        void deveCadastrarAdministradorComSucesso() throws Exception {
            when(repository.buscarPorMatricula("AD001")).thenReturn(Optional.empty());
            when(repository.buscarPorEmail("admin@teste.com")).thenReturn(Optional.empty());

            service.cadastrarUsuario("AD001", "Admin", "admin@teste.com", "senha", TipoUsuario.ADMINISTRADOR);

            verify(repository, times(1)).salvar(any(Administrador.class));
        }

        @Test
        @DisplayName("Não deve cadastrar usuário com matrícula já existente")
        void naoDeveCadastrarComMatriculaDuplicada() {
            when(repository.buscarPorMatricula("A001")).thenReturn(Optional.of(mock(Usuario.class)));

            Exception ex = assertThrows(Exception.class, () ->
                    service.cadastrarUsuario("A001", "Outro", "outro@teste.com", "senha", TipoUsuario.ALUNO));

            assertEquals("Erro: Já existe um usuário com esta matrícula.", ex.getMessage());
            verify(repository, never()).salvar(any());
        }

        @Test
        @DisplayName("Não deve cadastrar usuário com e-mail já existente")
        void naoDeveCadastrarComEmailDuplicado() {
            when(repository.buscarPorMatricula("A002")).thenReturn(Optional.empty());
            when(repository.buscarPorEmail("duplicado@teste.com")).thenReturn(Optional.of(mock(Usuario.class)));

            Exception ex = assertThrows(Exception.class, () ->
                    service.cadastrarUsuario("A002", "Outro", "duplicado@teste.com", "senha", TipoUsuario.ALUNO));

            assertEquals("Erro: Já existe um usuário com este e-mail.", ex.getMessage());
            verify(repository, never()).salvar(any());
        }

        @Test
        @DisplayName("Não deve cadastrar com matrícula nula")
        void naoDeveCadastrarComMatriculaNula() {
            Exception ex = assertThrows(Exception.class, () ->
                    service.cadastrarUsuario(null, "Nome", "email@teste.com", "senha", TipoUsuario.ALUNO));
            assertEquals("Erro: Matrícula não pode ser vazia.", ex.getMessage());
        }

        @Test
        @DisplayName("Não deve cadastrar com matrícula vazia")
        void naoDeveCadastrarComMatriculaVazia() {
            Exception ex = assertThrows(Exception.class, () ->
                    service.cadastrarUsuario("   ", "Nome", "email@teste.com", "senha", TipoUsuario.ALUNO));
            assertEquals("Erro: Matrícula não pode ser vazia.", ex.getMessage());
        }

        @Test
        @DisplayName("Não deve cadastrar com nome nulo")
        void naoDeveCadastrarComNomeNulo() {
            Exception ex = assertThrows(Exception.class, () ->
                    service.cadastrarUsuario("A001", null, "email@teste.com", "senha", TipoUsuario.ALUNO));
            assertEquals("Erro: Nome não pode ser vazio.", ex.getMessage());
        }

        @Test
        @DisplayName("Não deve cadastrar com nome vazio")
        void naoDeveCadastrarComNomeVazio() {
            Exception ex = assertThrows(Exception.class, () ->
                    service.cadastrarUsuario("A001", "", "email@teste.com", "senha", TipoUsuario.ALUNO));
            assertEquals("Erro: Nome não pode ser vazio.", ex.getMessage());
        }

        @Test
        @DisplayName("Não deve cadastrar com e-mail nulo")
        void naoDeveCadastrarComEmailNulo() {
            Exception ex = assertThrows(Exception.class, () ->
                    service.cadastrarUsuario("A001", "Nome", null, "senha", TipoUsuario.ALUNO));
            assertEquals("Erro: E-mail não pode ser vazio.", ex.getMessage());
        }

        @Test
        @DisplayName("Não deve cadastrar com e-mail vazio")
        void naoDeveCadastrarComEmailVazio() {
            Exception ex = assertThrows(Exception.class, () ->
                    service.cadastrarUsuario("A001", "Nome", "", "senha", TipoUsuario.ALUNO));
            assertEquals("Erro: E-mail não pode ser vazio.", ex.getMessage());
        }

        @Test
        @DisplayName("Não deve cadastrar com senha nula")
        void naoDeveCadastrarComSenhaNula() {
            Exception ex = assertThrows(Exception.class, () ->
                    service.cadastrarUsuario("A001", "Nome", "email@teste.com", null, TipoUsuario.ALUNO));
            assertEquals("Erro: Senha não pode ser vazia.", ex.getMessage());
        }

        @Test
        @DisplayName("Não deve cadastrar com senha vazia")
        void naoDeveCadastrarComSenhaVazia() {
            Exception ex = assertThrows(Exception.class, () ->
                    service.cadastrarUsuario("A001", "Nome", "email@teste.com", "", TipoUsuario.ALUNO));
            assertEquals("Erro: Senha não pode ser vazia.", ex.getMessage());
        }

        @Test
        @DisplayName("Não deve cadastrar com tipo nulo")
        void naoDeveCadastrarComTipoNulo() {
            Exception ex = assertThrows(Exception.class, () ->
                    service.cadastrarUsuario("A001", "Nome", "email@teste.com", "senha", null));
            assertEquals("Erro: Tipo de usuário não pode ser nulo.", ex.getMessage());
        }

        @Test
        @DisplayName("Deve cadastrar com matrícula automática para aluno")
        void deveCadastrarComMatriculaAutomaticaAluno() throws Exception {
            when(repository.buscarPorEmail("novo@teste.com")).thenReturn(Optional.empty());
            when(repository.listarTodos()).thenReturn(new ArrayList<>());

            String matricula = service.cadastrarUsuarioComMatriculaAutomatica("Novo", "novo@teste.com", "senha", TipoUsuario.ALUNO);

            assertNotNull(matricula);
            assertTrue(matricula.startsWith("A"));
            verify(repository, times(1)).salvar(any(Usuario.class));
        }

        @Test
        @DisplayName("Não deve cadastrar com matrícula automática e e-mail duplicado")
        void naoDeveCadastrarMatriculaAutomaticaComEmailDuplicado() {
            when(repository.buscarPorEmail("dup@teste.com")).thenReturn(Optional.of(mock(Usuario.class)));

            Exception ex = assertThrows(Exception.class, () ->
                    service.cadastrarUsuarioComMatriculaAutomatica("Nome", "dup@teste.com", "senha", TipoUsuario.ALUNO));

            assertEquals("Erro: Já existe um usuário com este e-mail.", ex.getMessage());
        }
    }

    // =========================================================================
    // LOGIN
    // =========================================================================

    /** Testes do método login. */
    @Nested
    @DisplayName("Login de usuários")
    class LoginDeUsuarios {

        @Test
        @DisplayName("Deve realizar login com sucesso")
        void deveFazerLoginComSucesso() throws Exception {
            Usuario usuario = mock(Usuario.class);
            when(usuario.getSenha()).thenReturn("senha123");
            when(repository.buscarPorEmail("login@teste.com")).thenReturn(Optional.of(usuario));

            Usuario logado = service.login("login@teste.com", "senha123");

            assertNotNull(logado);
            assertEquals(usuario, logado);
        }

        @Test
        @DisplayName("Não deve fazer login com usuário inexistente")
        void naoDeveFazerLoginComUsuarioInexistente() {
            when(repository.buscarPorEmail(anyString())).thenReturn(Optional.empty());

            Exception ex = assertThrows(Exception.class, () ->
                    service.login("naoexiste@teste.com", "senha"));

            assertEquals("Erro: Usuário não encontrado.", ex.getMessage());
        }

        @Test
        @DisplayName("Não deve fazer login com senha incorreta")
        void naoDeveFazerLoginComSenhaIncorreta() {
            Usuario usuario = mock(Usuario.class);
            when(usuario.getSenha()).thenReturn("correta");
            when(repository.buscarPorEmail("login@teste.com")).thenReturn(Optional.of(usuario));

            Exception ex = assertThrows(Exception.class, () ->
                    service.login("login@teste.com", "errada"));

            assertEquals("Erro: Senha incorreta.", ex.getMessage());
        }

        @Test
        @DisplayName("Não deve fazer login com e-mail nulo")
        void naoDeveFazerLoginComEmailNulo() {
            Exception ex = assertThrows(Exception.class, () -> service.login(null, "senha"));
            assertEquals("Erro: Matrícula ou e-mail não pode ser vazio.", ex.getMessage());
        }

        @Test
        @DisplayName("Não deve fazer login com e-mail vazio")
        void naoDeveFazerLoginComEmailVazio() {
            Exception ex = assertThrows(Exception.class, () -> service.login("", "senha"));
            assertEquals("Erro: Matrícula ou e-mail não pode ser vazio.", ex.getMessage());
        }

        @Test
        @DisplayName("Não deve fazer login com senha nula")
        void naoDeveFazerLoginComSenhaNula() {
            Exception ex = assertThrows(Exception.class, () -> service.login("email@teste.com", null));
            assertEquals("Erro: Senha não pode ser vazia.", ex.getMessage());
        }

        @Test
        @DisplayName("Não deve fazer login com senha vazia")
        void naoDeveFazerLoginComSenhaVazia() {
            Exception ex = assertThrows(Exception.class, () -> service.login("email@teste.com", ""));
            assertEquals("Erro: Senha não pode ser vazia.", ex.getMessage());
        }

        @Test
        @DisplayName("Login deve ser case-insensitive para o e-mail")
        void loginDeveSerCaseInsensitiveParaEmail() throws Exception {
            Usuario usuario = mock(Usuario.class);
            when(usuario.getSenha()).thenReturn("123");
            when(repository.buscarPorEmail("usuario@teste.com")).thenReturn(Optional.of(usuario));

            Usuario logado = service.login("usuario@teste.com", "123");

            assertNotNull(logado);
            verify(repository).buscarPorEmail("usuario@teste.com");
        }
    }

    // =========================================================================
    // EDIÇÃO E DELEÇÃO
    // =========================================================================

    /** Testes dos métodos editarUsuario e editarUsuarioComTipo. */
    @Nested
    @DisplayName("Edição de usuários")
    class EdicaoDeUsuarios {

        @Test
        @DisplayName("Deve editar usuário com sucesso")
        void deveEditarUsuarioComSucesso() throws Exception {
            Usuario usuario = mock(Usuario.class);
            when(usuario.getEmail()).thenReturn("velho@teste.com");
            when(repository.buscarPorMatricula("A001")).thenReturn(Optional.of(usuario));
            when(repository.buscarPorEmail("novo@teste.com")).thenReturn(Optional.empty());

            service.editarUsuario("A001", "Novo Nome", "novo@teste.com", "novaSenha");

            verify(usuario).setNome("Novo Nome");
            verify(usuario).setEmail("novo@teste.com");
            verify(usuario).setSenha("novaSenha");
            verify(repository).atualizar(usuario);
        }

        @Test
        @DisplayName("Não deve editar usuário inexistente")
        void naoDeveEditarUsuarioInexistente() {
            when(repository.buscarPorMatricula("999")).thenReturn(Optional.empty());

            Exception ex = assertThrows(Exception.class, () ->
                    service.editarUsuario("999", "Nome", "email@teste.com", "senha"));

            assertEquals("Erro: Usuário com matrícula 999 não encontrado.", ex.getMessage());
        }

        @Test
        @DisplayName("Não deve editar com e-mail já usado por outro usuário")
        void naoDeveEditarComEmailDuplicado() {
            Usuario usuario = new Aluno("A001", "Nome", "email@teste.com", "senha");
            when(repository.buscarPorMatricula("A001")).thenReturn(Optional.of(usuario));
            when(repository.buscarPorEmail("outro@teste.com")).thenReturn(Optional.of(mock(Usuario.class)));

            assertThrows(Exception.class, () ->
                    service.editarUsuario("A001", "Novo", "outro@teste.com", "senha"));
        }

        @Test
        @DisplayName("Deve editar tipo de usuário com sucesso")
        void deveEditarTipoDeUsuario() throws Exception {
            String matricula = "A0001";
            Usuario usuarioAntigo = new Aluno(matricula, "Velho", "velho@email.com", "123");
            when(repository.buscarPorMatricula(matricula)).thenReturn(Optional.of(usuarioAntigo));
            when(repository.buscarPorEmail("novo@email.com")).thenReturn(Optional.empty());
            when(repository.listarTodos()).thenReturn(new ArrayList<>());

            service.editarUsuarioComTipo(matricula, "Novo", "novo@email.com", "456", TipoUsuario.PROFESSOR);

            // Quando o tipo muda, deve deletar o antigo e salvar um novo com a classe correta
            verify(repository).deletar(matricula);
            verify(repository).salvar(any(Professor.class));
        }
    }

    /** Testes do método deletarUsuario. */
    @Nested
    @DisplayName("Deleção de usuários")
    class DelecaoDeUsuarios {

        @Test
        @DisplayName("Deve deletar usuário com sucesso")
        void deveDeletarUsuarioComSucesso() throws Exception {
            Usuario usuario = mock(Usuario.class);
            when(usuario.getNome()).thenReturn("João");
            when(repository.buscarPorMatricula("A001")).thenReturn(Optional.of(usuario));

            service.deletarUsuario("A001");

            verify(repository).deletar("A001");
        }

        @Test
        @DisplayName("Não deve deletar usuário inexistente")
        void naoDeveDeletarUsuarioInexistente() {
            when(repository.buscarPorMatricula("999")).thenReturn(Optional.empty());

            Exception ex = assertThrows(Exception.class, () ->
                    service.deletarUsuario("999"));

            assertEquals("Erro: Usuário com matrícula 999 não encontrado.", ex.getMessage());
        }

        @Test
        @DisplayName("Não deve deletar com matrícula nula")
        void naoDeveDeletarComMatriculaNula() {
            Exception ex = assertThrows(Exception.class, () ->
                    service.deletarUsuario(null));
            assertEquals("Erro: Matrícula não pode ser vazia.", ex.getMessage());
        }
    }

    // =========================================================================
    // BUSCA E LISTAGEM
    // =========================================================================

    /** Testes dos métodos buscarUsuarioPorMatricula, listarUsuarios e obterTodosUsuarios. */
    @Nested
    @DisplayName("Busca e listagem")
    class BuscaEListagem {

        @Test
        @DisplayName("Deve buscar usuário por matrícula com sucesso")
        void deveBuscarPorMatriculaComSucesso() throws Exception {
            Usuario usuario = mock(Usuario.class);
            when(repository.buscarPorMatricula("A001")).thenReturn(Optional.of(usuario));

            assertEquals(usuario, service.buscarUsuarioPorMatricula("A001"));
        }

        @Test
        @DisplayName("Não deve buscar com matrícula inexistente")
        void naoDeveBuscarMatriculaInexistente() {
            when(repository.buscarPorMatricula("999")).thenReturn(Optional.empty());

            assertThrows(Exception.class, () -> service.buscarUsuarioPorMatricula("999"));
        }

        @Test
        @DisplayName("Deve listar todos os usuários")
        void deveListarTodosUsuarios() {
            service.listarUsuarios();
            verify(repository).listarTodos();
        }

        @Test
        @DisplayName("Deve obter todos os usuários")
        void deveObterTodosUsuarios() {
            service.obterTodosUsuarios();
            verify(repository).listarTodos();
        }
    }
}