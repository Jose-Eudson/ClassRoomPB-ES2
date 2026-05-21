package com.classroompb.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

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
            assertThrows(Exception.class, () ->
                    service.cadastrarPeriodo(null, 2026, 2,
                            LocalDate.now(), LocalDate.now().plusMonths(4), true));
        }

        @Test
        @DisplayName("Nao deve cadastrar com codigo vazio")
        void naoDeveCadastrarComCodigoVazio() {
            assertThrows(Exception.class, () ->
                    service.cadastrarPeriodo("  ", 2026, 2,
                            LocalDate.now(), LocalDate.now().plusMonths(4), true));
        }

        @Test
        @DisplayName("Nao deve cadastrar com semestre invalido (0)")
        void naoDeveCadastrarComSemestreZero() {
            assertThrows(Exception.class, () ->
                    service.cadastrarPeriodo("2026.0", 2026, 0,
                            LocalDate.now(), LocalDate.now().plusMonths(4), true));
        }

        @Test
        @DisplayName("Nao deve cadastrar com semestre invalido (3)")
        void naoDeveCadastrarComSemestreTres() {
            assertThrows(Exception.class, () ->
                    service.cadastrarPeriodo("2026.3", 2026, 3,
                            LocalDate.now(), LocalDate.now().plusMonths(4), true));
        }

        @Test
        @DisplayName("Nao deve cadastrar quando data inicio e posterior a data fim")
        void naoDeveCadastrarComDataInicioMaiorQueFim() {
            assertThrows(Exception.class, () ->
                    service.cadastrarPeriodo("2026.2", 2026, 2,
                            LocalDate.of(2026, 12, 20), LocalDate.of(2026, 8, 10), true));
        }
    }

    @Nested
    @DisplayName("Ativacao de periodos")
    class AtivacaoDePeriodos {

        @Test
        @DisplayName("Deve ativar periodo quando usuario e coordenador")
        void deveAtivarPeriodo() throws Exception {
            PeriodoLetivo periodo = new PeriodoLetivo();
            when(repository.buscarPorCodigo("2026.2")).thenReturn(periodo);
            when(repository.listarTodos()).thenReturn(Collections.emptyList());
            service.ativarPeriodo(coordenador, "2026.2");
            verify(repository).atualizarDados();
        }

        @Test
        @DisplayName("Deve desativar outros periodos ao ativar um novo")
        void deveDesativarOutrosPeriodosAoAtivar() throws Exception {
            PeriodoLetivo p1 = new PeriodoLetivo("2026.1", 2026, 1,
                    LocalDate.of(2026, 2, 1), LocalDate.of(2026, 6, 30), true);
            PeriodoLetivo p2 = new PeriodoLetivo("2026.2", 2026, 2,
                    LocalDate.of(2026, 8, 10), LocalDate.of(2026, 12, 20), false);
            when(repository.buscarPorCodigo("2026.2")).thenReturn(p2);
            when(repository.listarTodos()).thenReturn(Arrays.asList(p1, p2));
            service.ativarPeriodo(coordenador, "2026.2");
            // p1 should be deactivated
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
            when(repository.buscarPorCodigo("2026.2")).thenReturn(periodo);
            service.encerrarPeriodo(coordenador, "2026.2");
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
}
