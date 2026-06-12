package com.classroompb.service;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import com.classroompb.exception.CadastroDuplicadoException;
import com.classroompb.model.Aluno;
import com.classroompb.model.TipoUsuario;
import com.classroompb.model.Usuario;
import com.classroompb.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("RF04 - Validacao de duplicidade no UsuarioService")
public class DuplicidadeServiceTest {

    @Mock
    private UsuarioRepository repository;

    private UsuarioService service;

    @BeforeEach
    void setUp() {
        service = new UsuarioService(repository);
    }

    @Nested
    @DisplayName("cadastrarUsuario - matricula manual")
    class CadastroComMatriculaManual {

        @Test
        @DisplayName("RF04 - Deve lancar excecao com campo MATRICULA ao duplicar matricula")
        void deveLancarExcecaoComCampoMatriculaAoDuplicar() {
            when(repository.existePorMatricula("A0001")).thenReturn(true);

            CadastroDuplicadoException ex = assertThrows(CadastroDuplicadoException.class, () ->
                    service.cadastrarUsuario("A0001", "Carlos", "carlos@teste.com", "senha", TipoUsuario.ALUNO));

            assertEquals(CadastroDuplicadoException.Campo.MATRICULA, ex.getCampo());
            assertEquals("A0001", ex.getValorDuplicado());
            assertEquals("Erro: Já existe um usuário com esta matrícula.", ex.getMessage());
        }

        @Test
        @DisplayName("RF04 - Nao deve chamar salvar ao duplicar matricula")
        void naoDeveSalvarAoDuplicarMatricula() {
            when(repository.existePorMatricula("A0001")).thenReturn(true);

            assertThrows(CadastroDuplicadoException.class, () ->
                    service.cadastrarUsuario("A0001", "Carlos", "carlos@teste.com", "senha", TipoUsuario.ALUNO));

            verify(repository, never()).salvar(any());
        }

        @Test
        @DisplayName("RF04 - Deve lancar excecao com campo EMAIL ao duplicar e-mail")
        void deveLancarExcecaoComCampoEmailAoDuplicar() {
            when(repository.existePorMatricula("A0002")).thenReturn(false);
            when(repository.existePorEmail("duplicado@teste.com")).thenReturn(true);

            CadastroDuplicadoException ex = assertThrows(CadastroDuplicadoException.class, () ->
                    service.cadastrarUsuario("A0002", "Maria", "duplicado@teste.com", "senha", TipoUsuario.ALUNO));

            assertEquals(CadastroDuplicadoException.Campo.EMAIL, ex.getCampo());
            assertEquals("duplicado@teste.com", ex.getValorDuplicado());
            assertEquals("Erro: Já existe um usuário com este e-mail.", ex.getMessage());
        }

        @Test
        @DisplayName("RF04 - Nao deve chamar salvar ao duplicar e-mail")
        void naoDeveSalvarAoDuplicarEmail() {
            when(repository.existePorMatricula("A0002")).thenReturn(false);
            when(repository.existePorEmail("duplicado@teste.com")).thenReturn(true);

            assertThrows(CadastroDuplicadoException.class, () ->
                    service.cadastrarUsuario("A0002", "Maria", "duplicado@teste.com", "senha", TipoUsuario.ALUNO));

            verify(repository, never()).salvar(any());
        }

        @Test
        @DisplayName("RF04 - Verificacao de matricula ocorre antes da verificacao de e-mail")
        void verificacaoDeMatriculaTemPrecedenciasobreEmail() {
            when(repository.existePorMatricula("A0001")).thenReturn(true);

            CadastroDuplicadoException ex = assertThrows(CadastroDuplicadoException.class, () ->
                    service.cadastrarUsuario("A0001", "Outro", "duplicado@teste.com", "senha", TipoUsuario.ALUNO));

            assertEquals(CadastroDuplicadoException.Campo.MATRICULA, ex.getCampo(),
                    "O campo MATRICULA deve ser reportado antes do campo EMAIL");
        }

        @Test
        @DisplayName("RF04 - Deve cadastrar com sucesso quando matricula e e-mail sao unicos")
        void deveCadastrarQuandoNaoHaDuplicata() throws Exception {
            when(repository.existePorMatricula("A0010")).thenReturn(false);
            when(repository.existePorEmail("novo@teste.com")).thenReturn(false);

            service.cadastrarUsuario("A0010", "Novo", "novo@teste.com", "senha", TipoUsuario.ALUNO);

            verify(repository).salvar(any(Aluno.class));
        }

        @Test
        @DisplayName("RF04 - Deve impedir duplicidade em todos os tipos de usuario")
        void deveImpedirDuplicidadeParaTodosOsTipos() {
            TipoUsuario[] tipos = TipoUsuario.values();
            String[] matriculas = { "A0001", "P0001", "C0001", "AD0001" };

            for (int i = 0; i < tipos.length; i++) {
                final String matricula = matriculas[i];
                final TipoUsuario tipo = tipos[i];
                final int index = i;
                when(repository.existePorMatricula(matricula)).thenReturn(true);

                CadastroDuplicadoException ex = assertThrows(CadastroDuplicadoException.class, () -> service
                        .cadastrarUsuario(matricula, "Nome", "email" + index + "@teste.com", "senha", tipo));

                assertEquals(CadastroDuplicadoException.Campo.MATRICULA, ex.getCampo());
            }
        }
    }

    @Nested
    @DisplayName("cadastrarUsuarioComMatriculaAutomatica - e-mail unico obrigatorio")
    class CadastroComMatriculaAutomatica {

        @Test
        @DisplayName("RF04 - Deve lancar excecao para e-mail duplicado")
        void deveLancarExcecaoParaEmailDuplicado() {
            when(repository.existePorEmail("dup@escola.com")).thenReturn(true);

            CadastroDuplicadoException ex = assertThrows(CadastroDuplicadoException.class, () ->
                    service.cadastrarUsuarioComMatriculaAutomatica("Ana", "dup@escola.com", "senha", TipoUsuario.ALUNO));

            assertEquals(CadastroDuplicadoException.Campo.EMAIL, ex.getCampo());
            assertEquals("dup@escola.com", ex.getValorDuplicado());
        }

        @Test
        @DisplayName("RF04 - Nao deve gerar matricula nem salvar para e-mail duplicado")
        void naoDeveGerarMatriculaNemSalvarParaEmailDuplicado() {
            when(repository.existePorEmail("dup@escola.com")).thenReturn(true);

            assertThrows(CadastroDuplicadoException.class, () ->
                    service.cadastrarUsuarioComMatriculaAutomatica("Ana", "dup@escola.com", "senha", TipoUsuario.ALUNO));

            verify(repository, never()).listarTodos();
            verify(repository, never()).salvar(any());
        }

        @Test
        @DisplayName("RF04 - Deve cadastrar quando e-mail e unico")
        void deveCadastrarComEmailUnico() throws Exception {
            when(repository.existePorEmail("unico@escola.com")).thenReturn(false);
            when(repository.listarTodos()).thenReturn(new ArrayList<>());

            String matricula = service.cadastrarUsuarioComMatriculaAutomatica(
                    "Beatriz", "unico@escola.com", "senha", TipoUsuario.ALUNO);

            assertNotNull(matricula);
            verify(repository).salvar(any());
        }
    }

    @Nested
    @DisplayName("editarUsuario - rejeitar e-mail de outro usuario")
    class EdicaoComEmailDuplicado {

        @Test
        @DisplayName("RF04 - Deve lancar excecao ao tentar usar e-mail de outro usuario")
        void deveLancarExcecaoAoTentarUsarEmailDeOutroUsuario() {
            Usuario usuario = new Aluno("A0001", "Carlos", "carlos@teste.com", "senha");
            when(repository.buscarPorMatricula("A0001")).thenReturn(Optional.of(usuario));
            when(repository.existePorEmail("ocupado@teste.com")).thenReturn(true);

            CadastroDuplicadoException ex = assertThrows(CadastroDuplicadoException.class,
                    () -> service.editarUsuario("A0001", "Carlos", "ocupado@teste.com", "senha"));

            assertEquals(CadastroDuplicadoException.Campo.EMAIL, ex.getCampo());
            assertEquals("ocupado@teste.com", ex.getValorDuplicado());
        }

        @Test
        @DisplayName("RF04 - Nao deve atualizar repositorio com e-mail duplicado")
        void naoDeveAtualizarRepositorioComEmailDuplicado() {
            Usuario usuario = new Aluno("A0001", "Carlos", "carlos@teste.com", "senha");
            when(repository.buscarPorMatricula("A0001")).thenReturn(Optional.of(usuario));
            when(repository.existePorEmail("ocupado@teste.com")).thenReturn(true);

            assertThrows(CadastroDuplicadoException.class,
                    () -> service.editarUsuario("A0001", "Carlos", "ocupado@teste.com", "senha"));

            verify(repository, never()).atualizar(any());
        }

        @Test
        @DisplayName("RF04 - Deve permitir edicao mantendo o mesmo e-mail")
        void devePermitirEdicaoMantenhoMesmoEmail() throws Exception {
            String email = "carlos@teste.com";
            Usuario usuario = new Aluno("A0001", "Carlos", email, "senha");
            when(repository.buscarPorMatricula("A0001")).thenReturn(Optional.of(usuario));

            service.editarUsuario("A0001", "Carlos Atualizado", email, "novaSenha");

            verify(repository).atualizar(usuario);
        }

        @Test
        @DisplayName("RF04 - Deve permitir edicao com e-mail novo e unico")
        void devePermitirEdicaoComEmailNovoEUnico() throws Exception {
            Usuario usuario = new Aluno("A0001", "Carlos", "carlos@teste.com", "senha");
            when(repository.buscarPorMatricula("A0001")).thenReturn(Optional.of(usuario));
            when(repository.existePorEmail("novo@teste.com")).thenReturn(false);

            service.editarUsuario("A0001", "Carlos", "novo@teste.com", "senha");

            verify(repository).atualizar(usuario);
        }

        @Test
        @DisplayName("RF04 - Deve detectar e-mail duplicado com casing diferente na edicao")
        void deveDetectarEmailDuplicadoComCasingDiferenteNaEdicao() {
            Usuario usuario = new Aluno("A0001", "Carlos", "carlos@teste.com", "senha");
            when(repository.buscarPorMatricula("A0001")).thenReturn(Optional.of(usuario));
            when(repository.existePorEmail("OCUPADO@TESTE.COM")).thenReturn(true);

            CadastroDuplicadoException ex = assertThrows(CadastroDuplicadoException.class,
                    () -> service.editarUsuario("A0001", "Carlos", "OCUPADO@TESTE.COM", "senha"));

            assertEquals(CadastroDuplicadoException.Campo.EMAIL, ex.getCampo());
        }
    }

    @Nested
    @DisplayName("editarUsuarioComTipo - rejeitar e-mail duplicado ao alterar tipo")
    class EdicaoComTipoEmailDuplicado {

        @Test
        @DisplayName("RF04 - Deve lancar excecao ao editar tipo com e-mail de outro usuario")
        void deveLancarExcecaoAoEditarTipoComEmailDuplicado() {
            Usuario usuarioAtual = new Aluno("A0001", "Carlos", "carlos@teste.com", "senha");
            when(repository.buscarPorMatricula("A0001")).thenReturn(Optional.of(usuarioAtual));
            when(repository.existePorEmail("ocupado@teste.com")).thenReturn(true);

            CadastroDuplicadoException ex = assertThrows(CadastroDuplicadoException.class, () -> service
                    .editarUsuarioComTipo("A0001", "Carlos", "ocupado@teste.com", "senha", TipoUsuario.PROFESSOR));

            assertEquals(CadastroDuplicadoException.Campo.EMAIL, ex.getCampo());
            assertEquals("ocupado@teste.com", ex.getValorDuplicado());
        }

        @Test
        @DisplayName("RF04 - Nao deve deletar nem salvar com e-mail duplicado na mudanca de tipo, verificando antes de qualquer mutacao")
        void naoDeveDeletarNemSalvarComEmailDuplicadoNaMudancaDeTipo() {
            Usuario usuarioAtual = new Aluno("A0001", "Carlos", "carlos@teste.com", "senha");
            when(repository.buscarPorMatricula("A0001")).thenReturn(Optional.of(usuarioAtual));
            when(repository.existePorEmail("ocupado@teste.com")).thenReturn(true);

            assertThrows(CadastroDuplicadoException.class, () -> service.editarUsuarioComTipo("A0001", "Carlos",
                    "ocupado@teste.com", "senha", TipoUsuario.PROFESSOR));

            verify(repository, never()).deletar(any());
            verify(repository, never()).salvar(any());
            verify(repository, never()).atualizar(any());
        }
    }
}
