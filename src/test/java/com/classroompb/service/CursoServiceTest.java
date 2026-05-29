package com.classroompb.service;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.classroompb.model.Administrador;
import com.classroompb.model.Aluno;
import com.classroompb.model.Curso;
import com.classroompb.model.Usuario;
import com.classroompb.repository.CursoRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitarios de CursoService")
public class CursoServiceTest {

    @Mock
    private CursoRepository repository;

    private CursoService service;
    private Usuario admin;
 
    @BeforeEach
    void setUp() {
        service = new CursoService(repository);
        admin = new Administrador("AD0001", "Admin", "admin@teste.com", "123");
    }

    @Nested
    @DisplayName("Cadastro de cursos")
    class CadastroDeCursos {

        @Test
        @DisplayName("Deve cadastrar curso com sucesso")
        void deveCadastrarCursoComSucesso() throws Exception {
            when(repository.existePorCodigo("ADS")).thenReturn(false);
            service.cadastrarCurso(admin, "ADS", "Analise e Desenvolvimento de Sistemas", 3200);
            verify(repository, times(1)).salvar(any(Curso.class));
        }

        @Test
        @DisplayName("Nao deve cadastrar com usuario nao administrador")
        void naoDeveCadastrarComUsuarioNaoAdmin() {
            Usuario aluno = new Aluno("A0001", "Aluno", "aluno@teste.com", "123");
            Exception ex = assertThrows(Exception.class, () ->
                    service.cadastrarCurso(aluno, "ADS", "Curso", 1800));
            assertEquals("Erro: Apenas administradores podem cadastrar cursos.", ex.getMessage());
            verify(repository, never()).salvar(any());
        }

        @Test
        @DisplayName("Nao deve cadastrar com codigo duplicado")
        void naoDeveCadastrarComCodigoDuplicado() {
            when(repository.existePorCodigo("ADS")).thenReturn(true);
            Exception ex = assertThrows(Exception.class, () ->
                    service.cadastrarCurso(admin, "ADS", "Outro Curso", 1800));
            assertEquals("Erro: Já existe um curso com este código.", ex.getMessage());
            verify(repository, never()).salvar(any());
        }

        @Test
        @DisplayName("Nao deve cadastrar com codigo nulo")
        void naoDeveCadastrarComCodigoNulo() {
            Exception ex = assertThrows(Exception.class, () ->
                    service.cadastrarCurso(admin, null, "Curso", 1800));
            assertEquals("Erro: Código do curso não pode ser vazio.", ex.getMessage());
        }

        @Test
        @DisplayName("Nao deve cadastrar com codigo vazio")
        void naoDeveCadastrarComCodigoVazio() {
            Exception ex = assertThrows(Exception.class, () ->
                    service.cadastrarCurso(admin, "   ", "Curso", 1800));
            assertEquals("Erro: Código do curso não pode ser vazio.", ex.getMessage());
        }

        @Test
        @DisplayName("Nao deve cadastrar com nome nulo")
        void naoDeveCadastrarComNomeNulo() {
            Exception ex = assertThrows(Exception.class, () ->
                    service.cadastrarCurso(admin, "ADS", null, 1800));
            assertEquals("Erro: Nome do curso não pode ser vazio.", ex.getMessage());
        }

        @Test
        @DisplayName("Nao deve cadastrar com nome vazio")
        void naoDeveCadastrarComNomeVazio() {
            Exception ex = assertThrows(Exception.class, () ->
                    service.cadastrarCurso(admin, "ADS", "", 1800));
            assertEquals("Erro: Nome do curso não pode ser vazio.", ex.getMessage());
        }

        @Test
        @DisplayName("Nao deve cadastrar com carga horaria zero")
        void naoDeveCadastrarComCargaHorariaZero() {
            Exception ex = assertThrows(Exception.class, () ->
                    service.cadastrarCurso(admin, "ADS", "Curso", 0));
            assertEquals("Erro: Carga horária deve ser maior que zero.", ex.getMessage());
        }

        @Test
        @DisplayName("Nao deve cadastrar com carga horaria negativa")
        void naoDeveCadastrarComCargaHorariaNegativa() {
            Exception ex = assertThrows(Exception.class, () ->
                    service.cadastrarCurso(admin, "ADS", "Curso", -10));
            assertEquals("Erro: Carga horária deve ser maior que zero.", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("Listagem de cursos")
    class ListagemDeCursos {

        @Test
        @DisplayName("Deve obter todos os cursos")
        void deveObterTodosCursos() {
            when(repository.listarTodos()).thenReturn(Arrays.asList(
                    new Curso("ADS", "Analise", 3200),
                    new Curso("SI", "Sistemas", 3000)
            ));
            assertEquals(2, service.obterTodosCursos().size());
        }
    }

    @Nested
    @DisplayName("Busca por codigo")
    class BuscaPorCodigo {

        @Test
        @DisplayName("Deve buscar curso existente por codigo")
        void deveBuscarCursoExistente() throws Exception {
            Curso curso = new Curso("ADS", "Analise", 3200);
            when(repository.buscarPorCodigo("ADS")).thenReturn(curso);
            Curso encontrado = service.buscarPorCodigo("ADS");
            assertNotNull(encontrado);
            assertEquals("ADS", encontrado.getCodigo());
        }

        @Test
        @DisplayName("buscarPorCodigo deve lancar excecao para codigo nulo")
        void buscarPorCodigoLancaExcecaoParaNulo() {
            Exception ex = assertThrows(Exception.class, () -> service.buscarPorCodigo(null));
            assertEquals("Erro: Código não pode ser vazio.", ex.getMessage());
        }

        @Test
        @DisplayName("buscarPorCodigo deve lancar excecao para codigo vazio")
        void buscarPorCodigoLancaExcecaoParaVazio() {
            Exception ex = assertThrows(Exception.class, () -> service.buscarPorCodigo("  "));
            assertEquals("Erro: Código não pode ser vazio.", ex.getMessage());
        }

        @Test
        @DisplayName("buscarPorCodigo deve lancar excecao para curso inexistente")
        void buscarPorCodigoLancaExcecaoParaInexistente() {
            when(repository.buscarPorCodigo("XPTO")).thenReturn(null);
            Exception ex = assertThrows(Exception.class, () -> service.buscarPorCodigo("XPTO"));
            assertEquals("Erro: Curso com código XPTO não encontrado.", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("Edicao de cursos")
    class EdicaoDeCursos {

        @Test
        @DisplayName("Deve editar curso com sucesso")
        void deveEditarCursoComSucesso() throws Exception {
            Curso curso = new Curso("ADS", "Antigo", 3200);
            when(repository.buscarPorCodigo("ADS")).thenReturn(curso);
            service.editarCurso(admin, "ADS", "Novo Nome", 4000);
            verify(repository).atualizar(any(Curso.class));
        }

        @Test
        @DisplayName("editarCurso deve lancar excecao para nome vazio")
        void editarCursoLancaExcecaoParaNomeVazio() {
            Curso curso = new Curso("ADS", "Antigo", 3200);
            when(repository.buscarPorCodigo("ADS")).thenReturn(curso);
            Exception ex = assertThrows(Exception.class, () ->
                    service.editarCurso(admin, "ADS", "", 4000));
            assertEquals("Erro: Nome do curso não pode ser vazio.", ex.getMessage());
        }

        @Test
        @DisplayName("editarCurso deve lancar excecao para nome nulo")
        void editarCursoLancaExcecaoParaNomeNulo() {
            Curso curso = new Curso("ADS", "Antigo", 3200);
            when(repository.buscarPorCodigo("ADS")).thenReturn(curso);
            Exception ex = assertThrows(Exception.class, () ->
                    service.editarCurso(admin, "ADS", null, 4000));
            assertEquals("Erro: Nome do curso não pode ser vazio.", ex.getMessage());
        }

        @Test
        @DisplayName("editarCurso deve lancar excecao para carga horaria invalida")
        void editarCursoLancaExcecaoParaCargaHorariaInvalida() {
            Curso curso = new Curso("ADS", "Antigo", 3200);
            when(repository.buscarPorCodigo("ADS")).thenReturn(curso);
            Exception ex = assertThrows(Exception.class, () ->
                    service.editarCurso(admin, "ADS", "Novo", 0));
            assertEquals("Erro: Carga horária deve ser maior que zero.", ex.getMessage());
        }

        @Test
        @DisplayName("editarCurso deve lancar excecao para curso inexistente")
        void editarCursoLancaExcecaoParaInexistente() {
            when(repository.buscarPorCodigo("XPTO")).thenReturn(null);
            assertThrows(Exception.class, () -> service.editarCurso(admin, "XPTO", "Nome", 1000));
        }
    }

    @Nested
    @DisplayName("Delecao de cursos")
    class DelecaoDeCursos {

        @Test
        @DisplayName("Deve deletar curso existente")
        void deveDeletarCursoExistente() throws Exception {
            Curso curso = new Curso("ADS", "Analise", 3200);
            when(repository.buscarPorCodigo("ADS")).thenReturn(curso);
            service.deletarCurso(admin, "ADS");
            verify(repository).deletar("ADS");
        }

        @Test
        @DisplayName("deletarCurso deve lancar excecao para curso inexistente")
        void deletarCursoLancaExcecaoParaInexistente() {
            when(repository.buscarPorCodigo("XPTO")).thenReturn(null);
            assertThrows(Exception.class, () -> service.deletarCurso(admin, "XPTO"));
        }
    }
}
