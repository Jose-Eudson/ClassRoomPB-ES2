package com.classroompb.service;

import java.util.Arrays;
import java.util.Collections;

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
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.classroompb.model.Aluno;
import com.classroompb.model.Coordenador;
import com.classroompb.model.Disciplina;
import com.classroompb.model.Usuario;
import com.classroompb.repository.DisciplinaRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitarios de DisciplinaService")
@SuppressWarnings("PMD.TooManyStaticImports")
public class DisciplinaServiceTest {

    @Mock
    private DisciplinaRepository repository;

    private DisciplinaService service;
    private Usuario coordenador;

    @BeforeEach
    void setUp() {
        service = new DisciplinaService(repository);
        coordenador = new Coordenador("C0001", "Coord", "coord@teste.com", "123");
    }

    @Nested
    @DisplayName("Cadastro de disciplinas")
    class CadastroDeDisciplinas {

        @Test
        @DisplayName("Deve cadastrar disciplina com sucesso")
        void deveCadastrarDisciplinaComSucesso() throws Exception {
            when(repository.existePorCodigo("ES2")).thenReturn(false);
            when(repository.buscarPorCodigo("ES1")).thenReturn(
                    new Disciplina("ES1", "Engenharia de Software 1", 60, 4, Arrays.asList()));
            when(repository.buscarPorCodigo("POO")).thenReturn(
                    new Disciplina("POO", "Programação Orientada a Objetos", 60, 4, Arrays.asList()));
            service.cadastrarDisciplina(coordenador, "ES2", "Engenharia de Software 2", 60, 4, Arrays.asList("ES1", "POO"));
            verify(repository).salvar(any());
        }

        @Test
        @DisplayName("Nao deve cadastrar com usuario nao coordenador")
        void naoDeveCadastrarComUsuarioNaoCoordenador() {
            Usuario aluno = new Aluno("A0001", "Aluno", "aluno@teste.com", "123");
            Exception ex = assertThrows(Exception.class,
                    () -> service.cadastrarDisciplina(aluno, "ES2", "Disciplina", 60, 4, Collections.emptyList()));
            assertEquals("Erro: Apenas coordenadores podem cadastrar disciplinas.", ex.getMessage());
            verify(repository, never()).salvar(any());
        }

        @Test
        @DisplayName("Nao deve cadastrar com codigo duplicado")
        void naoDeveCadastrarComCodigoDuplicado() {
            when(repository.existePorCodigo("ES2")).thenReturn(true);
            Exception ex = assertThrows(Exception.class, () ->
                    service.cadastrarDisciplina(coordenador, "ES2", "Outra", 60, 4, Collections.emptyList()));
            assertEquals("Erro: Ja existe uma disciplina com este codigo.", ex.getMessage());
            verify(repository, never()).salvar(any());
        }

        @Test
        @DisplayName("Nao deve cadastrar com codigo nulo")
        void naoDeveCadastrarComCodigoNulo() {
            Exception ex = assertThrows(Exception.class,
                    () -> service.cadastrarDisciplina(coordenador, null, "Disciplina", 60, 4, Collections.emptyList()));
            assertEquals("Erro: Codigo da disciplina nao pode ser vazio.", ex.getMessage());
        }

        @Test
        @DisplayName("Nao deve cadastrar com codigo vazio")
        void naoDeveCadastrarComCodigoVazio() {
            Exception ex = assertThrows(Exception.class,
                    () -> service.cadastrarDisciplina(coordenador, "", "Disciplina", 60, 4, Collections.emptyList()));
            assertEquals("Erro: Codigo da disciplina nao pode ser vazio.", ex.getMessage());
        }

        @Test
        @DisplayName("Nao deve cadastrar com nome nulo")
        void naoDeveCadastrarComNomeNulo() {
            Exception ex = assertThrows(Exception.class,
                    () -> service.cadastrarDisciplina(coordenador, "ES2", null, 60, 4, Collections.emptyList()));
            assertEquals("Erro: Nome da disciplina nao pode ser vazio.", ex.getMessage());
        }

        @Test
        @DisplayName("Nao deve cadastrar com nome vazio")
        void naoDeveCadastrarComNomeVazio() {
            Exception ex = assertThrows(Exception.class,
                    () -> service.cadastrarDisciplina(coordenador, "ES2", "", 60, 4, Collections.emptyList()));
            assertEquals("Erro: Nome da disciplina nao pode ser vazio.", ex.getMessage());
        }

        @Test
        @DisplayName("Nao deve cadastrar com creditos invalidos")
        void naoDeveCadastrarComCreditosInvalidos() {
            Exception ex = assertThrows(Exception.class, () -> service.cadastrarDisciplina(coordenador, "ES2",
                    "Disciplina", 60, 0, Collections.emptyList()));
            assertEquals("Erro: Creditos devem ser maiores que zero.", ex.getMessage());
        }

        @Test
        @DisplayName("Nao deve cadastrar com creditos negativos")
        void naoDeveCadastrarComCreditosNegativos() {
            Exception ex = assertThrows(Exception.class, () -> service.cadastrarDisciplina(coordenador, "ES2",
                    "Disciplina", 60, -1, Collections.emptyList()));
            assertEquals("Erro: Creditos devem ser maiores que zero.", ex.getMessage());
        }

        @Test
        @DisplayName("Nao deve cadastrar com carga horaria zero")
        void naoDeveCadastrarComCargaHorariaZero() {
            Exception ex = assertThrows(Exception.class,
                    () -> service.cadastrarDisciplina(coordenador, "ES2", "Disciplina", 0, 4, Collections.emptyList()));
            assertEquals("Erro: Carga horaria deve ser maior que zero.", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("Listagem de disciplinas")
    class ListagemDeDisciplinas {

        @Test
        @DisplayName("Deve listar todas as disciplinas")
        void deveListarTodasDisciplinas() {
            when(repository.listarTodos()).thenReturn(Arrays.asList(
                    new Disciplina("ES2", "Eng SW 2", 60, 4, Arrays.asList("ES1")),
                    new Disciplina("BD", "Banco de Dados", 60, 4, Arrays.asList())
            ));
            assertEquals(2, service.listarDisciplinas().size());
        }
    }

    @Nested
    @DisplayName("Busca por codigo")
    class BuscaPorCodigo {

        @Test
        @DisplayName("Deve buscar disciplina existente por codigo")
        void deveBuscarDisciplinaExistente() throws Exception {
            Disciplina d = new Disciplina("ES2", "Eng SW 2", 60, 4, Arrays.asList());
            when(repository.buscarPorCodigo("ES2")).thenReturn(d);
            assertNotNull(service.buscarPorCodigo("ES2"));
        }

        @Test
        @DisplayName("buscarPorCodigo deve lancar excecao para codigo nulo")
        void buscarPorCodigoLancaExcecaoParaNulo() {
            Exception ex = assertThrows(Exception.class, () -> service.buscarPorCodigo(null));
            assertEquals("Erro: Codigo nao pode ser vazio.", ex.getMessage());
        }

        @Test
        @DisplayName("buscarPorCodigo deve lancar excecao para codigo vazio")
        void buscarPorCodigoLancaExcecaoParaVazio() {
            Exception ex = assertThrows(Exception.class, () -> service.buscarPorCodigo("  "));
            assertEquals("Erro: Codigo nao pode ser vazio.", ex.getMessage());
        }

        @Test
        @DisplayName("buscarPorCodigo deve lancar excecao para disciplina inexistente")
        void buscarPorCodigoLancaExcecaoParaInexistente() {
            when(repository.buscarPorCodigo("XPTO")).thenReturn(null);
            Exception ex = assertThrows(Exception.class, () -> service.buscarPorCodigo("XPTO"));
            assertEquals("Erro: Disciplina com codigo XPTO nao encontrada.", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("Edicao de disciplinas")
    class EdicaoDeDisciplinas {

        @Test
        @DisplayName("Deve editar disciplina com sucesso")
        void deveEditarDisciplinaComSucesso() throws Exception {
            Disciplina d = new Disciplina("ES2", "Antigo", 60, 4, Arrays.asList());
            when(repository.buscarPorCodigo("ES2")).thenReturn(d);
            when(repository.buscarPorCodigo("ES1"))
                    .thenReturn(new Disciplina("ES1", "Engenharia de Software 1", 60, 4, Arrays.asList()));
            service.editarDisciplina(coordenador, "ES2", "Novo Nome", 80, 5, Arrays.asList("ES1"));
            verify(repository).atualizar(any());
        }

        @Test
        @DisplayName("editarDisciplina deve lancar excecao para nome vazio")
        void editarDisciplinaLancaExcecaoParaNomeVazio() {
            Disciplina d = new Disciplina("ES2", "Antigo", 60, 4, Arrays.asList());
            when(repository.buscarPorCodigo("ES2")).thenReturn(d);
            Exception ex = assertThrows(Exception.class,
                    () -> service.editarDisciplina(coordenador, "ES2", "", 60, 4, Arrays.asList()));
            assertEquals("Erro: Nome da disciplina nao pode ser vazio.", ex.getMessage());
        }

        @Test
        @DisplayName("editarDisciplina deve lancar excecao para carga horaria invalida")
        void editarDisciplinaLancaExcecaoParaCargaHorariaInvalida() {
            Disciplina d = new Disciplina("ES2", "Antigo", 60, 4, Arrays.asList());
            when(repository.buscarPorCodigo("ES2")).thenReturn(d);
            Exception ex = assertThrows(Exception.class,
                    () -> service.editarDisciplina(coordenador, "ES2", "Nome", 0, 4, Arrays.asList()));
            assertEquals("Erro: Carga horaria deve ser maior que zero.", ex.getMessage());
        }

        @Test
        @DisplayName("editarDisciplina deve lancar excecao para creditos invalidos")
        void editarDisciplinaLancaExcecaoParaCreditosInvalidos() {
            Disciplina d = new Disciplina("ES2", "Antigo", 60, 4, Arrays.asList());
            when(repository.buscarPorCodigo("ES2")).thenReturn(d);
            Exception ex = assertThrows(Exception.class,
                    () -> service.editarDisciplina(coordenador, "ES2", "Nome", 60, 0, Arrays.asList()));
            assertEquals("Erro: Creditos devem ser maiores que zero.", ex.getMessage());
        }

        @Test
        @DisplayName("editarDisciplina deve lancar excecao para disciplina inexistente")
        void editarDisciplinaLancaExcecaoParaInexistente() {
            when(repository.buscarPorCodigo("XPTO")).thenReturn(null);
            assertThrows(Exception.class, () ->
                    service.editarDisciplina(coordenador, "XPTO", "Nome", 60, 4, Arrays.asList()));
        }
    }

    @Nested
    @DisplayName("Delecao de disciplinas")
    class DelecaoDeDisciplinas {

        @Test
        @DisplayName("Deve deletar disciplina existente")
        void deveDeletarDisciplinaExistente() throws Exception {
            Disciplina d = new Disciplina("ES2", "Eng SW 2", 60, 4, Arrays.asList());
            when(repository.buscarPorCodigo("ES2")).thenReturn(d);
            service.deletarDisciplina(coordenador, "ES2");
            verify(repository).deletar("ES2");
        }

        @Test
        @DisplayName("deletarDisciplina deve lancar excecao para disciplina inexistente")
        void deletarDisciplinaLancaExcecaoParaInexistente() {
            when(repository.buscarPorCodigo("XPTO")).thenReturn(null);
            assertThrows(Exception.class, () -> service.deletarDisciplina(coordenador, "XPTO"));
        }

        @Test
        @DisplayName("deletarDisciplina deve lancar excecao quando coordenador e nulo")
        void deletarDisciplinaLancaExcecaoParaCoordenadorNulo() {
            Exception ex = assertThrows(Exception.class, () -> service.deletarDisciplina(null, "ES2"));
            assertEquals("Erro: Apenas coordenadores podem deletar disciplinas.", ex.getMessage());
        }

        @Test
        @DisplayName("deletarDisciplina deve lancar excecao quando usuario nao e coordenador")
        void deletarDisciplinaLancaExcecaoParaNaoCoordenador() {
            Usuario aluno = new Aluno("A0001", "Aluno", "aluno@teste.com", "123");
            Exception ex = assertThrows(Exception.class, () -> service.deletarDisciplina(aluno, "ES2"));
            assertEquals("Erro: Apenas coordenadores podem deletar disciplinas.", ex.getMessage());
        }

        @Test
        @DisplayName("deletarDisciplina deve lancar excecao quando codigo e nulo")
        void deletarDisciplinaLancaExcecaoParaCodigoNulo() {
            Exception ex = assertThrows(Exception.class, () -> service.deletarDisciplina(coordenador, null));
            assertTrue(ex.getMessage().contains("nao pode ser vazio"));
        }
    }

    @Nested
    @DisplayName("Edicao de disciplinas - casos adicionais")
    class EdicaoDisciplinasAdicionais {

        @Test
        @DisplayName("editarDisciplina deve lancar excecao quando coordenador e nulo")
        void editarDisciplinaLancaExcecaoParaCoordenadorNulo() throws Exception {
            Exception ex = assertThrows(Exception.class,
                    () -> service.editarDisciplina(null, "ES2", "Nome", 60, 4, Collections.emptyList()));
            assertEquals("Erro: Apenas coordenadores podem editar disciplinas.", ex.getMessage());
        }

        @Test
        @DisplayName("editarDisciplina deve lancar excecao quando usuario nao e coordenador")
        void editarDisciplinaLancaExcecaoParaNaoCoordenador() {
            Usuario aluno = new Aluno("A0001", "Aluno", "aluno@teste.com", "123");
            Exception ex = assertThrows(Exception.class,
                    () -> service.editarDisciplina(aluno, "ES2", "Nome", 60, 4, Collections.emptyList()));
            assertEquals("Erro: Apenas coordenadores podem editar disciplinas.", ex.getMessage());
        }

        @Test
        @DisplayName("editarDisciplina deve lancar excecao quando prerequisito e a propria disciplina")
        void editarDisciplinaLancaExcecaoPreReqEhAPropriaDiscip() {
            Disciplina d = new Disciplina("ES2", "Antigo", 60, 4, Collections.emptyList());
            when(repository.buscarPorCodigo("ES2")).thenReturn(d);

            Exception ex = assertThrows(Exception.class,
                    () -> service.editarDisciplina(coordenador, "ES2", "Nome", 60, 4, Arrays.asList("ES2")));
            assertTrue(ex.getMessage().contains("pré-requisito de si mesma"));
        }

        @Test
        @DisplayName("editarDisciplina deve lancar excecao quando prerequisito nao existe no sistema")
        void editarDisciplinaLancaExcecaoPreReqInexistente() {
            Disciplina d = new Disciplina("ES2", "Antigo", 60, 4, Collections.emptyList());
            when(repository.buscarPorCodigo("ES2")).thenReturn(d);
            when(repository.buscarPorCodigo("XPTO")).thenReturn(null);

            Exception ex = assertThrows(Exception.class,
                    () -> service.editarDisciplina(coordenador, "ES2", "Nome", 60, 4, Arrays.asList("XPTO")));
            assertTrue(ex.getMessage().contains("Pré-requisito 'XPTO' não encontrado"));
        }

        @Test
        @DisplayName("editarDisciplina deve lancar excecao quando creditos sao negativos")
        void editarDisciplinaLancaExcecaoParaCreditosNegativos() {
            Disciplina d = new Disciplina("ES2", "Antigo", 60, 4, Collections.emptyList());
            when(repository.buscarPorCodigo("ES2")).thenReturn(d);

            Exception ex = assertThrows(Exception.class,
                    () -> service.editarDisciplina(coordenador, "ES2", "Nome", 60, -1, Collections.emptyList()));
            assertEquals("Erro: Creditos devem ser maiores que zero.", ex.getMessage());
        }

        @Test
        @DisplayName("editarDisciplina deve lancar excecao quando nome e nulo")
        void editarDisciplinaLancaExcecaoParaNomeNulo() {
            Disciplina d = new Disciplina("ES2", "Antigo", 60, 4, Collections.emptyList());
            when(repository.buscarPorCodigo("ES2")).thenReturn(d);

            Exception ex = assertThrows(Exception.class,
                    () -> service.editarDisciplina(coordenador, "ES2", null, 60, 4, Collections.emptyList()));
            assertEquals("Erro: Nome da disciplina nao pode ser vazio.", ex.getMessage());
        }

        @Test
        @DisplayName("editarDisciplina deve ignorar prereqs com codigo vazio na lista")
        void editarDisciplinaIgnoraPreReqVazio() throws Exception {
            Disciplina d = new Disciplina("ES2", "Antigo", 60, 4, Collections.emptyList());
            when(repository.buscarPorCodigo("ES2")).thenReturn(d);

            service.editarDisciplina(coordenador, "ES2", "Nome", 60, 4, Arrays.asList("  "));

            verify(repository).atualizar(d);
        }
    }

    @Nested
    @DisplayName("Cadastro de disciplinas - casos adicionais")
    class CadastroDisciplinasAdicionais {

        @Test
        @DisplayName("cadastrarDisciplina deve lancar excecao quando prerequisito nao existe")
        void cadastrarDisciplinaLancaExcecaoPreReqInexistente() {
            when(repository.existePorCodigo("ES2")).thenReturn(false);
            when(repository.buscarPorCodigo("XPTO")).thenReturn(null);

            Exception ex = assertThrows(Exception.class,
                    () -> service.cadastrarDisciplina(coordenador, "ES2", "Nome", 60, 4, Arrays.asList("XPTO")));
            assertTrue(ex.getMessage().contains("XPTO"));
        }

        @Test
        @DisplayName("cadastrarDisciplina deve ignorar prereqs com codigo vazio na lista")
        void cadastrarDisciplinaIgnoraPreReqVazio() throws Exception {
            when(repository.existePorCodigo("ES2")).thenReturn(false);

            service.cadastrarDisciplina(coordenador, "ES2", "Nome", 60, 4, Arrays.asList("  "));

            verify(repository).salvar(any());
        }

        @Test
        @DisplayName("cadastrarDisciplina deve aceitar lista de prereqs nula")
        void cadastrarDisciplinaAceitaPreReqNulo() throws Exception {
            when(repository.existePorCodigo("ES2")).thenReturn(false);

            service.cadastrarDisciplina(coordenador, "ES2", "Nome", 60, 4, null);

            verify(repository).salvar(any());
        }

        @Test
        @DisplayName("cadastrarDisciplina deve lancar excecao quando coordenador e nulo")
        void cadastrarDisciplinaLancaExcecaoParaCoordenadorNulo() {
            Exception ex = assertThrows(Exception.class,
                    () -> service.cadastrarDisciplina(null, "ES2", "Nome", 60, 4, Collections.emptyList()));
            assertEquals("Erro: Apenas coordenadores podem cadastrar disciplinas.", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("Listagem de disciplinas - lista vazia")
    class ListagemVazia {

        @Test
        @DisplayName("Deve retornar lista vazia quando nao ha disciplinas cadastradas")
        void deveRetornarListaVazia() {
            when(repository.listarTodos()).thenReturn(Collections.emptyList());
            assertTrue(service.listarDisciplinas().isEmpty());
        }
    }
}
