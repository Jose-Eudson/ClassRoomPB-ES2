package com.classroompb.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.*;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import com.classroompb.exception.CadastroDuplicadoException;
import com.classroompb.model.Administrador;
import com.classroompb.model.Aluno;
import com.classroompb.model.Coordenador;
import com.classroompb.model.Professor;
import com.classroompb.model.TipoUsuario;
import com.classroompb.model.Usuario;
import com.classroompb.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitarios de UsuarioService")
public class UsuarioServiceTest {

    @Mock
    private UsuarioRepository repository;

    private UsuarioService service;

    @BeforeEach
    void setUp() {
        service = new UsuarioService(repository);
    }

    // =========================================================================
    // CADASTRO DE USUARIOS
    // =========================================================================

    @Nested
    @DisplayName("Cadastro de usuarios")
    class CadastroDeUsuarios {

        @Test
        @DisplayName("Deve cadastrar aluno com sucesso")
        void deveCadastrarAlunoComSucesso() throws Exception {
            when(repository.existePorMatricula("A001")).thenReturn(false);
            when(repository.existePorEmail("aluno@teste.com")).thenReturn(false);

            service.cadastrarUsuario("A001", "Carlos", "aluno@teste.com", "senha123", TipoUsuario.ALUNO);

            verify(repository, times(1)).salvar(any(Aluno.class));
        }

        @Test
        @DisplayName("Deve cadastrar professor com sucesso")
        void deveCadastrarProfessorComSucesso() throws Exception {
            when(repository.existePorMatricula("P001")).thenReturn(false);
            when(repository.existePorEmail("prof@teste.com")).thenReturn(false);

            service.cadastrarUsuario("P001", "Prof Maria", "prof@teste.com", "senha", TipoUsuario.PROFESSOR);

            verify(repository, times(1)).salvar(any(Professor.class));
        }

        @Test
        @DisplayName("Deve cadastrar coordenador com sucesso")
        void deveCadastrarCoordenadorComSucesso() throws Exception {
            when(repository.existePorMatricula("C001")).thenReturn(false);
            when(repository.existePorEmail("coord@teste.com")).thenReturn(false);

            service.cadastrarUsuario("C001", "Coord Ana", "coord@teste.com", "senha", TipoUsuario.COORDENADOR);

            verify(repository, times(1)).salvar(any(Coordenador.class));
        }

        @Test
        @DisplayName("Deve cadastrar administrador com sucesso")
        void deveCadastrarAdministradorComSucesso() throws Exception {
            when(repository.existePorMatricula("AD001")).thenReturn(false);
            when(repository.existePorEmail("admin@teste.com")).thenReturn(false);

            service.cadastrarUsuario("AD001", "Admin", "admin@teste.com", "senha", TipoUsuario.ADMINISTRADOR);

            verify(repository, times(1)).salvar(any(Administrador.class));
        }

        @Test
        @DisplayName("RF04 - Nao deve cadastrar usuario com matricula ja existente")
        void naoDeveCadastrarComMatriculaDuplicada() {
            when(repository.existePorMatricula("A001")).thenReturn(true);

            CadastroDuplicadoException ex = assertThrows(CadastroDuplicadoException.class, () ->
                    service.cadastrarUsuario("A001", "Outro", "outro@teste.com", "senha", TipoUsuario.ALUNO));

            assertEquals("Erro: Já existe um usuário com esta matrícula.", ex.getMessage());
            assertEquals(CadastroDuplicadoException.Campo.MATRICULA, ex.getCampo());
            assertEquals("A001", ex.getValorDuplicado());
            verify(repository, never()).salvar(any());
        }

        @Test
        @DisplayName("RF04 - Nao deve cadastrar usuario com e-mail ja existente")
        void naoDeveCadastrarComEmailDuplicado() {
            when(repository.existePorMatricula("A002")).thenReturn(false);
            when(repository.existePorEmail("duplicado@teste.com")).thenReturn(true);

            CadastroDuplicadoException ex = assertThrows(CadastroDuplicadoException.class, () ->
                    service.cadastrarUsuario("A002", "Outro", "duplicado@teste.com", "senha", TipoUsuario.ALUNO));

            assertEquals("Erro: Já existe um usuário com este e-mail.", ex.getMessage());
            assertEquals(CadastroDuplicadoException.Campo.EMAIL, ex.getCampo());
            assertEquals("duplicado@teste.com", ex.getValorDuplicado());
            verify(repository, never()).salvar(any());
        }

        @Test
        @DisplayName("RF04 - Matricula duplicada tem precedencia sobre e-mail na validacao")
        void matriculaDuplicadaTemPrecedencia() {
            when(repository.existePorMatricula("A001")).thenReturn(true);

            CadastroDuplicadoException ex = assertThrows(CadastroDuplicadoException.class, () ->
                    service.cadastrarUsuario("A001", "Outro", "outro@teste.com", "senha", TipoUsuario.ALUNO));

            assertEquals(CadastroDuplicadoException.Campo.MATRICULA, ex.getCampo());
            verify(repository, never()).salvar(any());
        }

        @Test
        @DisplayName("RF04 - Nao deve cadastrar com matricula automatica e e-mail duplicado")
        void naoDeveCadastrarMatriculaAutomaticaComEmailDuplicado() {
            when(repository.existePorEmail("dup@teste.com")).thenReturn(true);

            CadastroDuplicadoException ex = assertThrows(CadastroDuplicadoException.class, () ->
                    service.cadastrarUsuarioComMatriculaAutomatica("Nome", "dup@teste.com", "senha", TipoUsuario.ALUNO));

            assertEquals("Erro: Já existe um usuário com este e-mail.", ex.getMessage());
            assertEquals(CadastroDuplicadoException.Campo.EMAIL, ex.getCampo());
        }

        @Test
        @DisplayName("Nao deve cadastrar com matricula nula")
        void naoDeveCadastrarComMatriculaNula() {
            Exception ex = assertThrows(Exception.class,
                    () -> service.cadastrarUsuario(null, "Nome", "email@teste.com", "senha", TipoUsuario.ALUNO));
            assertEquals("Erro: Matrícula não pode ser vazia.", ex.getMessage());
        }

        @Test
        @DisplayName("Nao deve cadastrar com matricula vazia")
        void naoDeveCadastrarComMatriculaVazia() {
            Exception ex = assertThrows(Exception.class,
                    () -> service.cadastrarUsuario("   ", "Nome", "email@teste.com", "senha", TipoUsuario.ALUNO));
            assertEquals("Erro: Matrícula não pode ser vazia.", ex.getMessage());
        }

        @Test
        @DisplayName("Nao deve cadastrar com nome nulo")
        void naoDeveCadastrarComNomeNulo() {
            Exception ex = assertThrows(Exception.class,
                    () -> service.cadastrarUsuario("A001", null, "email@teste.com", "senha", TipoUsuario.ALUNO));
            assertEquals("Erro: Nome não pode ser vazio.", ex.getMessage());
        }

        @Test
        @DisplayName("Nao deve cadastrar com nome vazio")
        void naoDeveCadastrarComNomeVazio() {
            Exception ex = assertThrows(Exception.class,
                    () -> service.cadastrarUsuario("A001", "", "email@teste.com", "senha", TipoUsuario.ALUNO));
            assertEquals("Erro: Nome não pode ser vazio.", ex.getMessage());
        }

        @Test
        @DisplayName("Nao deve cadastrar com e-mail nulo")
        void naoDeveCadastrarComEmailNulo() {
            Exception ex = assertThrows(Exception.class,
                    () -> service.cadastrarUsuario("A001", "Nome", null, "senha", TipoUsuario.ALUNO));
            assertEquals("Erro: E-mail não pode ser vazio.", ex.getMessage());
        }

        @Test
        @DisplayName("Nao deve cadastrar com e-mail vazio")
        void naoDeveCadastrarComEmailVazio() {
            Exception ex = assertThrows(Exception.class,
                    () -> service.cadastrarUsuario("A001", "Nome", "", "senha", TipoUsuario.ALUNO));
            assertEquals("Erro: E-mail não pode ser vazio.", ex.getMessage());
        }

        @Test
        @DisplayName("Nao deve cadastrar com senha nula")
        void naoDeveCadastrarComSenhaNula() {
            Exception ex = assertThrows(Exception.class,
                    () -> service.cadastrarUsuario("A001", "Nome", "email@teste.com", null, TipoUsuario.ALUNO));
            assertEquals("Erro: Senha não pode ser vazia.", ex.getMessage());
        }

        @Test
        @DisplayName("Nao deve cadastrar com senha vazia")
        void naoDeveCadastrarComSenhaVazia() {
            Exception ex = assertThrows(Exception.class,
                    () -> service.cadastrarUsuario("A001", "Nome", "email@teste.com", "", TipoUsuario.ALUNO));
            assertEquals("Erro: Senha não pode ser vazia.", ex.getMessage());
        }

        @Test
        @DisplayName("Nao deve cadastrar com tipo nulo")
        void naoDeveCadastrarComTipoNulo() {
            Exception ex = assertThrows(Exception.class,
                    () -> service.cadastrarUsuario("A001", "Nome", "email@teste.com", "senha", null));
            assertEquals("Erro: Tipo de usuário não pode ser nulo.", ex.getMessage());
        }

        @Test
        @DisplayName("Deve cadastrar com matricula automatica para aluno")
        void deveCadastrarComMatriculaAutomaticaAluno() throws Exception {
            when(repository.existePorEmail("novo@teste.com")).thenReturn(false);
            when(repository.listarTodos()).thenReturn(new ArrayList<>());

            String matricula = service.cadastrarUsuarioComMatriculaAutomatica("Novo", "novo@teste.com", "senha", TipoUsuario.ALUNO);

            assertNotNull(matricula);
            assertTrue(matricula.startsWith("A"));
            verify(repository, times(1)).salvar(any(Usuario.class));
        }

        @Test
        @DisplayName("Deve cadastrar com matricula automatica para professor")
        void deveCadastrarComMatriculaAutomaticaProfessor() throws Exception {
            when(repository.existePorEmail("prof@teste.com")).thenReturn(false);
            when(repository.listarTodos()).thenReturn(new ArrayList<>());

            String matricula = service.cadastrarUsuarioComMatriculaAutomatica("Prof", "prof@teste.com", "senha", TipoUsuario.PROFESSOR);

            assertNotNull(matricula);
            assertTrue(matricula.startsWith("P"));
        }

        @Test
        @DisplayName("Deve cadastrar com matricula automatica para coordenador")
        void deveCadastrarComMatriculaAutomaticaCoordenador() throws Exception {
            when(repository.existePorEmail("coord@teste.com")).thenReturn(false);
            when(repository.listarTodos()).thenReturn(new ArrayList<>());

            String matricula = service.cadastrarUsuarioComMatriculaAutomatica("Coord", "coord@teste.com", "senha", TipoUsuario.COORDENADOR);

            assertNotNull(matricula);
            assertTrue(matricula.startsWith("C"));
        }

        @Test
        @DisplayName("Deve cadastrar com matricula automatica para administrador")
        void deveCadastrarComMatriculaAutomaticaAdministrador() throws Exception {
            when(repository.existePorEmail("admin2@teste.com")).thenReturn(false);
            when(repository.listarTodos()).thenReturn(new ArrayList<>());

            String matricula = service.cadastrarUsuarioComMatriculaAutomatica("Admin2", "admin2@teste.com", "senha", TipoUsuario.ADMINISTRADOR);

            assertNotNull(matricula);
            assertTrue(matricula.startsWith("AD"));
        }

        @Test
        @DisplayName("Nao deve cadastrar com matricula automatica com nome nulo")
        void naoDeveCadastrarAutomaticoComNomeNulo() {
            Exception ex = assertThrows(Exception.class, () -> service.cadastrarUsuarioComMatriculaAutomatica(null,
                    "email@teste.com", "senha", TipoUsuario.ALUNO));
            assertEquals("Erro: Nome não pode ser vazio.", ex.getMessage());
        }

        @Test
        @DisplayName("Nao deve cadastrar com matricula automatica com e-mail nulo")
        void naoDeveCadastrarAutomaticoComEmailNulo() {
            Exception ex = assertThrows(Exception.class,
                    () -> service.cadastrarUsuarioComMatriculaAutomatica("Nome", null, "senha", TipoUsuario.ALUNO));
            assertEquals("Erro: E-mail não pode ser vazio.", ex.getMessage());
        }

        @Test
        @DisplayName("Nao deve cadastrar com matricula automatica com senha nula")
        void naoDeveCadastrarAutomaticoComSenhaNula() {
            Exception ex = assertThrows(Exception.class, () -> service.cadastrarUsuarioComMatriculaAutomatica("Nome",
                    "email@teste.com", null, TipoUsuario.ALUNO));
            assertEquals("Erro: Senha não pode ser vazia.", ex.getMessage());
        }

        @Test
        @DisplayName("Nao deve cadastrar com matricula automatica com tipo nulo")
        void naoDeveCadastrarAutomaticoComTipoNulo() {
            Exception ex = assertThrows(Exception.class,
                    () -> service.cadastrarUsuarioComMatriculaAutomatica("Nome", "email@teste.com", "senha", null));
            assertEquals("Erro: Tipo de usuário não pode ser nulo.", ex.getMessage());
        }
    }

    // =========================================================================
    // LOGIN
    // =========================================================================

    @Nested
    @DisplayName("Login de usuarios")
    class LoginDeUsuarios {

        @Test
        @DisplayName("Deve realizar login por e-mail com sucesso")
        void deveFazerLoginComSucesso() throws Exception {
            Usuario usuario = new Aluno("A001", "Login", "login@teste.com", "senha123");
            when(repository.buscarPorEmail("login@teste.com")).thenReturn(Optional.of(usuario));

            Usuario logado = service.login("login@teste.com", "senha123");

            assertNotNull(logado);
            assertEquals(usuario, logado);
        }

        @Test
        @DisplayName("Deve realizar login por matricula de aluno com sucesso")
        void deveFazerLoginPorMatriculaAluno() throws Exception {
            Usuario usuario = new Aluno("A0001", "Carlos", "carlos@teste.com", "senha123");
            when(repository.buscarPorMatricula("A0001")).thenReturn(Optional.of(usuario));

            Usuario logado = service.login("A0001", "senha123");

            assertNotNull(logado);
            assertEquals("A0001", logado.getMatricula());
        }

        @Test
        @DisplayName("Deve realizar login por matricula de professor")
        void deveFazerLoginPorMatriculaProfessor() throws Exception {
            Usuario usuario = new Professor("P0001", "Maria", "maria@teste.com", "senha");
            when(repository.buscarPorMatricula("P0001")).thenReturn(Optional.of(usuario));

            assertNotNull(service.login("P0001", "senha"));
        }

        @Test
        @DisplayName("Deve realizar login por matricula de coordenador")
        void deveFazerLoginPorMatriculaCoordenador() throws Exception {
            Usuario usuario = new Coordenador("C0001", "Ana", "ana@teste.com", "senha");
            when(repository.buscarPorMatricula("C0001")).thenReturn(Optional.of(usuario));

            assertNotNull(service.login("C0001", "senha"));
        }

        @Test
        @DisplayName("Deve realizar login por matricula de administrador")
        void deveFazerLoginPorMatriculaAdministrador() throws Exception {
            Usuario usuario = new Administrador("AD0001", "Root", "root@teste.com", "admin");
            when(repository.buscarPorMatricula("AD0001")).thenReturn(Optional.of(usuario));

            assertNotNull(service.login("AD0001", "admin"));
        }

        @Test
        @DisplayName("Nao deve fazer login com usuario inexistente")
        void naoDeveFazerLoginComUsuarioInexistente() {
            when(repository.buscarPorEmail(anyString())).thenReturn(Optional.empty());

            Exception ex = assertThrows(Exception.class, () ->
                    service.login("naoexiste@teste.com", "senha"));

            assertEquals("Erro: Usuário não encontrado.", ex.getMessage());
        }

        @Test
        @DisplayName("Nao deve fazer login com matricula inexistente")
        void naoDeveFazerLoginComMatriculaInexistente() {
            when(repository.buscarPorMatricula("A9999")).thenReturn(Optional.empty());

            Exception ex = assertThrows(Exception.class, () ->
                    service.login("A9999", "senha"));

            assertEquals("Erro: Usuário não encontrado.", ex.getMessage());
        }

        @Test
        @DisplayName("Nao deve fazer login com senha incorreta")
        void naoDeveFazerLoginComSenhaIncorreta() {
            Usuario usuario = new Aluno("A001", "Login", "login@teste.com", "correta");
            when(repository.buscarPorEmail("login@teste.com")).thenReturn(Optional.of(usuario));

            Exception ex = assertThrows(Exception.class, () -> service.login("login@teste.com", "errada"));

            assertEquals("Erro: Senha incorreta.", ex.getMessage());
        }

        @Test
        @DisplayName("Nao deve permitir login com tipo e matricula inconsistentes (coordenador com prefixo de aluno)")
        void naoDevePermitirLoginComTipoEMatriculaInconsistentes() {
            Usuario adulterado = new Coordenador("A0001", "Fake", "fake@teste.com", "admin");
            when(repository.buscarPorMatricula("A0001")).thenReturn(Optional.of(adulterado));

            Exception ex = assertThrows(Exception.class, () -> service.login("A0001", "admin"));

            assertEquals("Erro: Dados de acesso inválidos para o perfil do usuário.", ex.getMessage());
        }

        @Test
        @DisplayName("Nao deve fazer login com e-mail nulo")
        void naoDeveFazerLoginComEmailNulo() {
            Exception ex = assertThrows(Exception.class, () -> service.login(null, "senha"));
            assertEquals("Erro: Matrícula ou e-mail não pode ser vazio.", ex.getMessage());
        }

        @Test
        @DisplayName("Nao deve fazer login com e-mail vazio")
        void naoDeveFazerLoginComEmailVazio() {
            Exception ex = assertThrows(Exception.class, () -> service.login("", "senha"));
            assertEquals("Erro: Matrícula ou e-mail não pode ser vazio.", ex.getMessage());
        }

        @Test
        @DisplayName("Nao deve fazer login com senha nula")
        void naoDeveFazerLoginComSenhaNula() {
            Exception ex = assertThrows(Exception.class, () -> service.login("email@teste.com", null));
            assertEquals("Erro: Senha não pode ser vazia.", ex.getMessage());
        }

        @Test
        @DisplayName("Nao deve fazer login com senha vazia")
        void naoDeveFazerLoginComSenhaVazia() {
            Exception ex = assertThrows(Exception.class, () -> service.login("email@teste.com", ""));
            assertEquals("Erro: Senha não pode ser vazia.", ex.getMessage());
        }

        @Test
        @DisplayName("Senha e case-sensitive no login")
        void senhaEhCaseSensitive() {
            Usuario usuario = new Aluno("A0001", "Carlos", "carlos@teste.com", "Senha123");
            when(repository.buscarPorMatricula("A0001")).thenReturn(Optional.of(usuario));

            Exception ex = assertThrows(Exception.class, () -> service.login("A0001", "senha123"));

            assertEquals("Erro: Senha incorreta.", ex.getMessage());
        }
    }

    // =========================================================================
    // EDICAO DE USUARIOS
    // =========================================================================

    @Nested
    @DisplayName("Edicao de usuarios")
    class EdicaoDeUsuarios {

        @Test
        @DisplayName("Deve editar usuario com sucesso alterando e-mail")
        void deveEditarUsuarioComSucesso() throws Exception {
            Usuario usuario = mock(Usuario.class);
            when(usuario.getEmail()).thenReturn("velho@teste.com");
            when(repository.buscarPorMatricula("A001")).thenReturn(Optional.of(usuario));
            when(repository.existePorEmail("novo@teste.com")).thenReturn(false);

            service.editarUsuario("A001", "Novo Nome", "novo@teste.com", "novaSenha");

            verify(usuario).setNome("Novo Nome");
            verify(usuario).setEmail("novo@teste.com");
            verify(usuario).setSenha("novaSenha");
            verify(repository).atualizar(usuario);
        }

        @Test
        @DisplayName("Nao deve editar usuario inexistente")
        void naoDeveEditarUsuarioInexistente() {
            when(repository.buscarPorMatricula("999")).thenReturn(Optional.empty());

            Exception ex = assertThrows(Exception.class, () ->
                    service.editarUsuario("999", "Nome", "email@teste.com", "senha"));

            assertEquals("Erro: Usuário com matrícula 999 não encontrado.", ex.getMessage());
        }

        @Test
        @DisplayName("RF04 - Nao deve editar com e-mail ja usado por outro usuario")
        void naoDeveEditarComEmailDuplicado() {
            Usuario usuario = new Aluno("A001", "Nome", "email@teste.com", "senha");
            when(repository.buscarPorMatricula("A001")).thenReturn(Optional.of(usuario));
            when(repository.existePorEmail("outro@teste.com")).thenReturn(true);

            CadastroDuplicadoException ex = assertThrows(CadastroDuplicadoException.class,
                    () -> service.editarUsuario("A001", "Novo", "outro@teste.com", "senha"));

            assertEquals(CadastroDuplicadoException.Campo.EMAIL, ex.getCampo());
            assertEquals("outro@teste.com", ex.getValorDuplicado());
        }

        @Test
        @DisplayName("Nao deve editar com matricula nula")
        void naoDeveEditarComMatriculaNula() {
            Exception ex = assertThrows(Exception.class,
                    () -> service.editarUsuario(null, "Nome", "email@teste.com", "senha"));
            assertEquals("Erro: Matrícula não pode ser vazia.", ex.getMessage());
        }

        @Test
        @DisplayName("Deve editar tipo de usuario: aluno para professor")
        void deveEditarTipoDeUsuario() throws Exception {
            String matricula = "A0001";
            Usuario usuarioAntigo = new Aluno(matricula, "Velho", "velho@email.com", "123");
            when(repository.buscarPorMatricula(matricula)).thenReturn(Optional.of(usuarioAntigo));
            when(repository.existePorEmail("novo@email.com")).thenReturn(false);
            when(repository.listarTodos()).thenReturn(new ArrayList<>());

            service.editarUsuarioComTipo(matricula, "Novo", "novo@email.com", "456", TipoUsuario.PROFESSOR);

            verify(repository).deletar(matricula);
            verify(repository).salvar(any(Professor.class));
        }

        private <T extends Usuario> void assertEditaTipoAluno(TipoUsuario novoTipo, Class<T> classeEsperada)
                throws Exception {
            Usuario usuarioAntigo = new Aluno("A0001", "Velho", "velho@email.com", "123");
            when(repository.buscarPorMatricula("A0001")).thenReturn(Optional.of(usuarioAntigo));
            when(repository.existePorEmail("novo@email.com")).thenReturn(false);
            when(repository.listarTodos()).thenReturn(new ArrayList<>());

            service.editarUsuarioComTipo("A0001", "Novo", "novo@email.com", "123", novoTipo);

            verify(repository).deletar("A0001");
            verify(repository).salvar(any(classeEsperada));
        }

        @Test
        @DisplayName("Deve editar tipo de usuario: aluno para coordenador")
        void deveEditarTipoAlunoParaCoordenador() throws Exception {
            assertEditaTipoAluno(TipoUsuario.COORDENADOR, Coordenador.class);
        }

        @Test
        @DisplayName("Deve editar tipo de usuario: aluno para administrador")
        void deveEditarTipoAlunoParaAdministrador() throws Exception {
            assertEditaTipoAluno(TipoUsuario.ADMINISTRADOR, Administrador.class);
        }

        @Test
        @DisplayName("Deve editar usuario com mesmo tipo - usa atualizar, nao deletar/salvar")
        void deveEditarUsuarioMesmoTipo() throws Exception {
            Usuario usuarioAntigo = new Aluno("A0001", "Carlos", "carlos@email.com", "123");
            when(repository.buscarPorMatricula("A0001")).thenReturn(Optional.of(usuarioAntigo));
            when(repository.existePorEmail("novo@email.com")).thenReturn(false);

            service.editarUsuarioComTipo("A0001", "Carlos Novo", "novo@email.com", "456", TipoUsuario.ALUNO);

            verify(repository, never()).deletar(any());
            verify(repository, never()).salvar(any());
            verify(repository).atualizar(any());
        }

        @Test
        @DisplayName("Nao deve editar com tipo nulo")
        void naoDeveEditarComTipoNulo() {
            Exception ex = assertThrows(Exception.class,
                    () -> service.editarUsuarioComTipo("A001", "Nome", "email@teste.com", "senha", null));
            assertEquals("Erro: Tipo de usuário não pode ser nulo.", ex.getMessage());
        }

        @Test
        @DisplayName("Nao deve editar usuario inexistente com tipo")
        void naoDeveEditarUsuarioInexistenteComTipo() {
            when(repository.buscarPorMatricula("Z999")).thenReturn(Optional.empty());

            Exception ex = assertThrows(Exception.class, () ->
                    service.editarUsuarioComTipo("Z999", "Nome", "email@teste.com", "senha", TipoUsuario.ALUNO));

            assertEquals("Erro: Usuário com matrícula Z999 não encontrado.", ex.getMessage());
        }
    }

    // =========================================================================
    // DELECAO DE USUARIOS
    // =========================================================================

    @Nested
    @DisplayName("Delecao de usuarios")
    class DelecaoDeUsuarios {

        @Test
        @DisplayName("Deve deletar usuario com sucesso")
        void deveDeletarUsuarioComSucesso() throws Exception {
            Usuario usuario = mock(Usuario.class);
            when(usuario.getNome()).thenReturn("Joao");
            when(repository.buscarPorMatricula("A001")).thenReturn(Optional.of(usuario));

            service.deletarUsuario("A001");

            verify(repository).deletar("A001");
        }

        @Test
        @DisplayName("Nao deve deletar usuario inexistente")
        void naoDeveDeletarUsuarioInexistente() {
            when(repository.buscarPorMatricula("999")).thenReturn(Optional.empty());

            Exception ex = assertThrows(Exception.class, () ->
                    service.deletarUsuario("999"));

            assertEquals("Erro: Usuário com matrícula 999 não encontrado.", ex.getMessage());
        }

        @Test
        @DisplayName("Nao deve deletar com matricula nula")
        void naoDeveDeletarComMatriculaNula() {
            Exception ex = assertThrows(Exception.class, () -> service.deletarUsuario(null));
            assertEquals("Erro: Matrícula não pode ser vazia.", ex.getMessage());
        }

        @Test
        @DisplayName("Nao deve deletar com matricula vazia")
        void naoDeveDeletarComMatriculaVazia() {
            Exception ex = assertThrows(Exception.class, () -> service.deletarUsuario(""));
            assertEquals("Erro: Matrícula não pode ser vazia.", ex.getMessage());
        }

        @Test
        @DisplayName("Nao deve deletar com matricula apenas espacos")
        void naoDeveDeletarComMatriculaApenasEspacos() {
            Exception ex = assertThrows(Exception.class, () -> service.deletarUsuario("   "));
            assertEquals("Erro: Matrícula não pode ser vazia.", ex.getMessage());
        }
    }

    // =========================================================================
    // BUSCA E LISTAGEM
    // =========================================================================

    @Nested
    @DisplayName("Busca e listagem")
    class BuscaEListagem {

        @Test
        @DisplayName("Deve buscar usuario por matricula com sucesso")
        void deveBuscarPorMatriculaComSucesso() throws Exception {
            Usuario usuario = mock(Usuario.class);
            when(repository.buscarPorMatricula("A001")).thenReturn(Optional.of(usuario));

            assertEquals(usuario, service.buscarUsuarioPorMatricula("A001"));
        }

        @Test
        @DisplayName("Nao deve buscar com matricula inexistente")
        void naoDeveBuscarMatriculaInexistente() {
            when(repository.buscarPorMatricula("999")).thenReturn(Optional.empty());

            Exception ex = assertThrows(Exception.class, () ->
                    service.buscarUsuarioPorMatricula("999"));
            assertEquals("Erro: Usuário com matrícula 999 não encontrado.", ex.getMessage());
        }

        @Test
        @DisplayName("Nao deve buscar com matricula nula")
        void naoDeveBuscarComMatriculaNula() {
            Exception ex = assertThrows(Exception.class, () -> service.buscarUsuarioPorMatricula(null));
            assertEquals("Erro: Matrícula não pode ser vazia.", ex.getMessage());
        }

        @Test
        @DisplayName("Nao deve buscar com matricula vazia")
        void naoDeveBuscarComMatriculaVazia() {
            Exception ex = assertThrows(Exception.class, () -> service.buscarUsuarioPorMatricula(""));
            assertEquals("Erro: Matrícula não pode ser vazia.", ex.getMessage());
        }

        @Test
        @DisplayName("Deve listar todos os usuarios")
        void deveListarTodosUsuarios() {
            when(repository.listarTodos()).thenReturn(new ArrayList<>());
            service.listarUsuarios();
            verify(repository).listarTodos();
        }

        @Test
        @DisplayName("Deve obter todos os usuarios via obterTodosUsuarios")
        void deveObterTodosUsuarios() {
            List<Usuario> lista = Arrays.asList(new Aluno("A001", "Alice", "alice@teste.com", "123"),
                    new Professor("P001", "Bob", "bob@teste.com", "456"));
            when(repository.listarTodos()).thenReturn(lista);

            List<Usuario> resultado = service.obterTodosUsuarios();

            assertEquals(2, resultado.size());
            verify(repository).listarTodos();
        }
    }
}
