package com.classroompb.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.classroompb.model.Aluno;
import com.classroompb.model.Coordenador;
import com.classroompb.model.PeriodoLetivo;
import com.classroompb.repository.PeriodoLetivoRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes de PeriodoLetivoService")
public class PeriodoLetivoServiceTest {

    @Mock
    private PeriodoLetivoRepository repository;

    private PeriodoLetivoService service;

    private Coordenador coordenador;

    @BeforeEach
    void setUp() {
        service = new PeriodoLetivoService(repository);
        coordenador = new Coordenador("C0001", "Rui", "rui@email.com", "123");
    }

    @Nested
    @DisplayName("Cadastro de periodos")
    class CadastroDePeriodos {

        @Test
        @DisplayName("Deve cadastrar periodo com sucesso")
        void deveCadastrarPeriodo() throws Exception {
            when(repository.existePorCodigo("2026.2")).thenReturn(false);
            service.cadastrarPeriodo("2026.2", 2026, 2,
                    LocalDate.of(2026, 8, 10), LocalDate.of(2026, 12, 20), true);
            verify(repository).salvar(any());
        }

        @Test
        @DisplayName("Nao deve cadastrar com codigo duplicado")
        void naoDeveCadastrarPeriodoDuplicado() {
            when(repository.existePorCodigo("2026.2")).thenReturn(true);
            assertThrows(Exception.class, () ->
                    service.cadastrarPeriodo("2026.2", 2026, 2,
                            LocalDate.now(), LocalDate.now().plusMonths(4), true));
        }

        @Test
        @DisplayName("Nao deve cadastrar com codigo nulo")
        void naoDeveCadastrarComCodigoNulo() {
            assertThrows(Exception.class, () -> service.cadastrarPeriodo(null, 2026, 2, LocalDate.now(),
                    LocalDate.now().plusMonths(4), true));
        }

        @Test
        @DisplayName("Nao deve cadastrar com codigo vazio")
        void naoDeveCadastrarComCodigoVazio() {
            assertThrows(Exception.class, () -> service.cadastrarPeriodo("  ", 2026, 2, LocalDate.now(),
                    LocalDate.now().plusMonths(4), true));
        }

        @Test
        @DisplayName("Nao deve cadastrar com semestre invalido (0)")
        void naoDeveCadastrarComSemestreZero() {
            assertThrows(Exception.class, () -> service.cadastrarPeriodo("2026.0", 2026, 0, LocalDate.now(),
                    LocalDate.now().plusMonths(4), true));
        }

        @Test
        @DisplayName("Nao deve cadastrar com semestre invalido (3)")
        void naoDeveCadastrarComSemestreTres() {
            assertThrows(Exception.class, () -> service.cadastrarPeriodo("2026.3", 2026, 3, LocalDate.now(),
                    LocalDate.now().plusMonths(4), true));
        }

        @Test
        @DisplayName("Nao deve cadastrar quando data inicio e posterior a data fim")
        void naoDeveCadastrarComDataInicioMaiorQueFim() {
            assertThrows(Exception.class, () -> service.cadastrarPeriodo("2026.2", 2026, 2, LocalDate.of(2026, 12, 20),
                    LocalDate.of(2026, 8, 10), true));
        }
    }

    @Nested
    @DisplayName("Ativacao de periodos")
    class AtivacaoDePeriodos {

        @Test
        @DisplayName("Deve ativar periodo quando usuario e coordenador")
        void deveAtivarPeriodo() throws Exception {
            PeriodoLetivo periodo = new PeriodoLetivo();
            periodo.setAtivo(false);
            when(repository.buscarPorCodigo("2026.2")).thenReturn(periodo);
            when(repository.listarTodos()).thenReturn(Collections.emptyList());
            service.ativarPeriodo(coordenador, "2026.2");
            assertTrue(periodo.isAtivo());
            verify(repository).atualizarDados();
        }

        @Test
        @DisplayName("Deve desativar outros periodos ao ativar um novo")
        void deveDesativarOutrosPeriodosAoAtivar() throws Exception {
            PeriodoLetivo p1 = new PeriodoLetivo("2026.1", 2026, 1, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 6, 30),
                    true);
            PeriodoLetivo p2 = new PeriodoLetivo("2026.2", 2026, 2, LocalDate.of(2026, 8, 10),
                    LocalDate.of(2026, 12, 20), false);
            when(repository.buscarPorCodigo("2026.2")).thenReturn(p2);
            when(repository.listarTodos()).thenReturn(Arrays.asList(p1, p2));
            service.ativarPeriodo(coordenador, "2026.2");
            assertFalse(p1.isAtivo());
            assertTrue(p2.isAtivo());
            verify(repository).atualizarDados();
        }

        @Test
        @DisplayName("Nao deve ativar periodo se usuario nao for coordenador")
        void naoDeveAtivarPeriodoSeNaoCoordenador() {
            Aluno aluno = new Aluno("A0001", "Carlos", "carlos@email.com", "123");
            assertThrows(Exception.class, () -> service.ativarPeriodo(aluno, "2026.2"));
            verify(repository, never()).atualizarDados();
        }

        @Test
        @DisplayName("Nao deve ativar periodo inexistente")
        void naoDeveAtivarPeriodoInexistente() {
            when(repository.buscarPorCodigo("XPTO")).thenReturn(null);
            assertThrows(Exception.class, () -> service.ativarPeriodo(coordenador, "XPTO"));
        }
    }

    @Nested
    @DisplayName("Encerramento de periodos")
    class EncerramentoDePeriodos {

        @Test
        @DisplayName("Deve encerrar periodo quando usuario e coordenador")
        void deveEncerrarPeriodo() throws Exception {
            PeriodoLetivo periodo = new PeriodoLetivo();
            periodo.setAtivo(true);
            when(repository.buscarPorCodigo("2026.2")).thenReturn(periodo);
            service.encerrarPeriodo(coordenador, "2026.2");
            assertFalse(periodo.isAtivo());
            verify(repository).atualizarDados();
        }

        @Test
        @DisplayName("Nao deve encerrar periodo se usuario nao for coordenador")
        void naoDeveEncerrarPeriodoSeNaoCoordenador() {
            Aluno aluno = new Aluno("A0001", "Carlos", "carlos@email.com", "123");
            assertThrows(Exception.class, () -> service.encerrarPeriodo(aluno, "2026.2"));
            verify(repository, never()).atualizarDados();
        }

        @Test
        @DisplayName("Nao deve encerrar periodo inexistente")
        void naoDeveEncerrarPeriodoInexistente() {
            when(repository.buscarPorCodigo("XPTO")).thenReturn(null);
            assertThrows(Exception.class, () -> service.encerrarPeriodo(coordenador, "XPTO"));
        }
    }

    @Nested
    @DisplayName("Listagem e Busca")
    class ListagemEBusca {

        @Test
        @DisplayName("Deve listar todos os periodos")
        void deveListarPeriodos() {
            List<PeriodoLetivo> lista = Arrays.asList(new PeriodoLetivo(), new PeriodoLetivo());
            when(repository.listarTodos()).thenReturn(lista);

            List<PeriodoLetivo> resultado = service.listarPeriodos();
            assertEquals(2, resultado.size());
            verify(repository).listarTodos();
        }

        @Test
        @DisplayName("Deve buscar periodo por codigo com sucesso")
        void deveBuscarPorCodigo() throws Exception {
            PeriodoLetivo periodo = new PeriodoLetivo();
            periodo.setCodigo("2026.1");
            when(repository.buscarPorCodigo("2026.1")).thenReturn(periodo);

            PeriodoLetivo resultado = service.buscarPorCodigo("2026.1");
            assertNotNull(resultado);
            assertEquals("2026.1", resultado.getCodigo());
        }

        @Test
        @DisplayName("Deve lancar excecao ao buscar periodo inexistente")
        void deveLancarExcecaoAoBuscarInexistente() {
            when(repository.buscarPorCodigo("XPTO")).thenReturn(null);
            assertThrows(Exception.class, () -> service.buscarPorCodigo("XPTO"));
        }
    }

    @Nested
    @DisplayName("Edicao de periodos")
    class EdicaoDePeriodos {

        @Test
        @DisplayName("Deve editar periodo com sucesso")
        void deveEditarPeriodo() throws Exception {
            PeriodoLetivo periodo = new PeriodoLetivo("2026.1", 2026, 1, LocalDate.of(2026, 2, 1),
                    LocalDate.of(2026, 6, 30), true);
            when(repository.buscarPorCodigo("2026.1")).thenReturn(periodo);

            LocalDate novaInicio = LocalDate.of(2026, 2, 15);
            LocalDate novaFim = LocalDate.of(2026, 7, 10);

            service.editarPeriodo("2026.1", novaInicio, novaFim);

            assertEquals(novaInicio, periodo.getDataInicio());
            assertEquals(novaFim, periodo.getDataFim());
            verify(repository).atualizarDados();
        }

        @Test
        @DisplayName("Nao deve editar periodo inexistente")
        void naoDeveEditarInexistente() {
            when(repository.buscarPorCodigo("XPTO")).thenReturn(null);
            assertThrows(Exception.class, () ->
                service.editarPeriodo("XPTO", LocalDate.now(), LocalDate.now().plusMonths(1)));
        }

        @Test
        @DisplayName("Nao deve editar com data inicio posterior a data fim")
        void naoDeveEditarComDataInvalida() {
            PeriodoLetivo periodo = new PeriodoLetivo();
            when(repository.buscarPorCodigo("2026.1")).thenReturn(periodo);

            assertThrows(Exception.class,
                    () -> service.editarPeriodo("2026.1", LocalDate.now().plusDays(1), LocalDate.now()));
        }
    }
}
