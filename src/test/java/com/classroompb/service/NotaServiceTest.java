package com.classroompb.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.classroompb.model.Aluno;
import com.classroompb.model.Coordenador;
import com.classroompb.model.MatriculaTurma;
import com.classroompb.model.Nota;
import com.classroompb.model.Professor;
import com.classroompb.model.StatusMatricula;
import com.classroompb.model.TipoUsuario;
import com.classroompb.model.Turma;
import com.classroompb.model.Usuario;
import com.classroompb.repository.MatriculaTurmaRepository;
import com.classroompb.repository.NotaRepository;
import com.classroompb.repository.TurmaRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do NotaService")
class NotaServiceTest {

    @Mock
    private NotaRepository notaRepository;

    @Mock
    private TurmaRepository turmaRepository;

    @Mock
    private MatriculaTurmaRepository matriculaRepository;

    private NotaService service;

    private Professor professor;
    private Coordenador coordenador;
    private Aluno aluno;
    private Turma turma;

    private MatriculaTurma matricula;

    @BeforeEach
    void setup() {

        service = new NotaService(notaRepository, turmaRepository, matriculaRepository);

        professor = new Professor("P0001", "Prof Lima", "lima@email.com", "789");
        professor.setMatricula("P0001");
        professor.setTipo(TipoUsuario.PROFESSOR);

        coordenador = new Coordenador("C0001", "Coord Silva", "coord@email.com", "123");
        coordenador.setMatricula("C0001");
        coordenador.setTipo(TipoUsuario.COORDENADOR);

        aluno = new Aluno("A0001", "João", "joao@email.com", "456");

        turma = new Turma("T01", "ES2", "2026.1", 40, "Seg/Qua 10h-12h", "B101", "P0001");

        matricula = new MatriculaTurma("A0001", "ES2", "2026.1", "T01");

        matricula.setStatus(StatusMatricula.CONFIRMADA);
    }

    @Nested
    @DisplayName("Lançamento de notas")
    class LancamentoNotas {

        @Test
        @DisplayName("Deve lançar notas quando ainda não existir registro")
        void deveSalvarNovaNota() throws Exception {

            when(turmaRepository.buscarPorChaveUnica(
                    "ES2",
                    "2026.1",
                    "T01"))
                    .thenReturn(turma);

            when(matriculaRepository.buscarPorChaveUnica(
                    "A0001",
                    "ES2",
                    "2026.1",
                    "T01"))
                    .thenReturn(matricula);

            when(notaRepository.buscarPorChaveUnica(
                    "A0001",
                    "ES2",
                    "2026.1",
                    "T01"))
                    .thenReturn(null);

            service.lancarNotas(
                    professor,
                    "A0001",
                    "ES2",
                    "2026.1",
                    "T01",
                    8.5,
                    9.0);

            ArgumentCaptor<Nota> captor =
                    ArgumentCaptor.forClass(Nota.class);

            verify(notaRepository).salvar(captor.capture());

            Nota salva = captor.getValue();

            assertEquals("A0001", salva.getMatriculaAluno());
            assertEquals(8.5, salva.getEtapa1());
            assertEquals(9.0, salva.getEtapa2());
        }

        @Test
        @DisplayName("Deve atualizar notas existentes")
        void deveAtualizarNotaExistente() throws Exception {

            Nota nota = new Nota("A0001", "ES2", "2026.1", "T01");

            when(turmaRepository.buscarPorChaveUnica(anyString(), anyString(), anyString())).thenReturn(turma);

            when(matriculaRepository.buscarPorChaveUnica(anyString(), anyString(), anyString(), anyString()))
                    .thenReturn(matricula);

            when(notaRepository.buscarPorChaveUnica(anyString(), anyString(), anyString(), anyString()))
                    .thenReturn(nota);

            service.lancarNotas(professor, "A0001", "ES2", "2026.1", "T01", 7.0, 6.5);

            ArgumentCaptor<Nota> captor = ArgumentCaptor.forClass(Nota.class);

            verify(notaRepository).atualizar(captor.capture());

            assertEquals(7.0, captor.getValue().getEtapa1());
            assertEquals(6.5, captor.getValue().getEtapa2());

            verify(notaRepository, never()).salvar(any());
        }

        @Test
        @DisplayName("Não deve salvar e atualizar ao mesmo tempo")
        void naoDeveSalvarEAtualizarAoMesmoTempo() throws Exception {

            when(turmaRepository.buscarPorChaveUnica(
                    anyString(),
                    anyString(),
                    anyString()))
                    .thenReturn(turma);

            when(matriculaRepository.buscarPorChaveUnica(
                    anyString(),
                    anyString(),
                    anyString(),
                    anyString()))
                    .thenReturn(matricula);

            when(notaRepository.buscarPorChaveUnica(
                    anyString(),
                    anyString(),
                    anyString(),
                    anyString()))
                    .thenReturn(null);

            service.lancarNotas(
                    professor,
                    "A0001",
                    "ES2",
                    "2026.1",
                    "T01",
                    10.0,
                    10.0);

            verify(notaRepository).salvar(any());
            verify(notaRepository, never()).atualizar(any());
        }
    }

    @Nested
    @DisplayName("Validação do Professor")
    class ValidacaoProfessor {

        @Test
        @DisplayName("Deve lançar exceção quando usuário for nulo")
        void deveLancarExcecaoQuandoProfessorForNulo() {

            Exception ex = assertThrows(Exception.class,
                    () -> service.lancarNotas(null, aluno.getMatricula(), "ES2", "2026.1", "T01", 8.0, 9.0));

            assertEquals("Erro: Apenas professores podem lançar notas.", ex.getMessage());

            verifyNoInteractions(turmaRepository, matriculaRepository, notaRepository);
        }

        @Test
        @DisplayName("Deve lançar exceção quando usuário não for professor")
        void deveLancarExcecaoQuandoUsuarioNaoForProfessor() {

            Usuario alunoLogado = new Aluno();
            alunoLogado.setTipo(TipoUsuario.ALUNO);

            Exception ex = assertThrows(Exception.class,
                    () -> service.lancarNotas(alunoLogado, aluno.getMatricula(), "ES2", "2026.1", "T01", 7.0, 8.0));

            assertEquals("Erro: Apenas professores podem lançar notas.", ex.getMessage());

            verifyNoInteractions(turmaRepository, matriculaRepository, notaRepository);
        }

        @Test
        @DisplayName("Deve impedir professor que não é responsável pela turma")
        void deveImpedirProfessorNaoResponsavel() {

            Usuario outroProfessor = new Professor();
            outroProfessor.setTipo(TipoUsuario.PROFESSOR);
            outroProfessor.setMatricula("P999");

            when(turmaRepository.buscarPorChaveUnica("ES2", "2026.1", "T01")).thenReturn(turma);

            Exception ex = assertThrows(Exception.class,
                    () -> service.lancarNotas(outroProfessor, aluno.getMatricula(), "ES2", "2026.1", "T01", 8.0, 9.0));

            assertEquals("Erro: Apenas o professor responsável pode lançar notas.", ex.getMessage());

            verify(turmaRepository).buscarPorChaveUnica("ES2", "2026.1", "T01");

            verifyNoMoreInteractions(turmaRepository);
            verifyNoInteractions(matriculaRepository, notaRepository);
        }
    }

    @Nested
    @DisplayName("Validação da Matrícula")
    class ValidacaoMatricula {

        @Test
        @DisplayName("Deve lançar exceção quando matrícula não existir")
        void deveLancarExcecaoQuandoMatriculaNaoExistir() {

            when(turmaRepository.buscarPorChaveUnica(
                    "ES2",
                    "2026.1",
                    "T01"))
                    .thenReturn(turma);

            when(matriculaRepository.buscarPorChaveUnica(
                    aluno.getMatricula(),
                    "ES2",
                    "2026.1",
                    "T01"))
                    .thenReturn(null);

            Exception ex = assertThrows(Exception.class, () ->
                    service.lancarNotas(
                            professor,
                            aluno.getMatricula(),
                            "ES2",
                            "2026.1",
                            "T01",
                            8.0,
                            9.0));

            assertEquals(
                    "Erro: O aluno não possui matrícula confirmada.",
                    ex.getMessage());

            verify(turmaRepository).buscarPorChaveUnica(
                    "ES2",
                    "2026.1",
                    "T01");

            verify(matriculaRepository).buscarPorChaveUnica(
                    aluno.getMatricula(),
                    "ES2",
                    "2026.1",
                    "T01");

            verifyNoInteractions(notaRepository);
        }

        @Test
        @DisplayName("Deve lançar exceção quando matrícula estiver pendente")
        void deveLancarExcecaoQuandoMatriculaPendente() {

            matricula.setStatus(StatusMatricula.PENDENTE);

            when(turmaRepository.buscarPorChaveUnica("ES2", "2026.1", "T01")).thenReturn(turma);

            when(matriculaRepository.buscarPorChaveUnica(aluno.getMatricula(), "ES2", "2026.1", "T01"))
                    .thenReturn(matricula);

            Exception ex = assertThrows(Exception.class,
                    () -> service.lancarNotas(professor, aluno.getMatricula(), "ES2", "2026.1", "T01", 8.0, 9.0));

            assertEquals("Erro: O aluno não possui matrícula confirmada.", ex.getMessage());

            verify(turmaRepository).buscarPorChaveUnica("ES2", "2026.1", "T01");

            verify(matriculaRepository).buscarPorChaveUnica(aluno.getMatricula(), "ES2", "2026.1", "T01");

            verifyNoInteractions(notaRepository);
        }

        @Test
        @DisplayName("Deve permitir lançamento quando matrícula estiver confirmada")
        void devePermitirQuandoMatriculaConfirmada() throws Exception {

            when(turmaRepository.buscarPorChaveUnica(
                    "ES2",
                    "2026.1",
                    "T01"))
                    .thenReturn(turma);

            when(matriculaRepository.buscarPorChaveUnica(
                    aluno.getMatricula(),
                    "ES2",
                    "2026.1",
                    "T01"))
                    .thenReturn(matricula);

            when(notaRepository.buscarPorChaveUnica(
                    aluno.getMatricula(),
                    "ES2",
                    "2026.1",
                    "T01"))
                    .thenReturn(null);

            service.lancarNotas(
                    professor,
                    aluno.getMatricula(),
                    "ES2",
                    "2026.1",
                    "T01",
                    9.0,
                    8.5);

            verify(notaRepository).salvar(any(Nota.class));
        }
    }

    @Nested
    @DisplayName("Consulta de Notas")
    class ConsultaNotas {

        @Test
        @DisplayName("Deve consultar notas existentes")
        void deveConsultarNotasExistentes() {

            Nota nota = new Nota(aluno.getMatricula(), "ES2", "2026.1", "T01");

            nota.setEtapa1(8.5);
            nota.setEtapa2(9.0);

            when(notaRepository.buscarPorChaveUnica(aluno.getMatricula(), "ES2", "2026.1", "T01")).thenReturn(nota);

            Nota resultado = service.consultarNotas(aluno.getMatricula(), "ES2", "2026.1", "T01");

            assertNotNull(resultado);
            assertEquals(8.5, resultado.getEtapa1());
            assertEquals(9.0, resultado.getEtapa2());

            verify(notaRepository).buscarPorChaveUnica(aluno.getMatricula(), "ES2", "2026.1", "T01");
        }

        @Test
        @DisplayName("Deve retornar null quando não houver notas")
        void deveRetornarNullQuandoNaoExistirNota() {

            when(notaRepository.buscarPorChaveUnica(
                    aluno.getMatricula(),
                    "ES2",
                    "2026.1",
                    "T01"))
                    .thenReturn(null);

            Nota resultado = service.consultarNotas(
                    aluno.getMatricula(),
                    "ES2",
                    "2026.1",
                    "T01");

            assertNull(resultado);

            verify(notaRepository).buscarPorChaveUnica(
                    aluno.getMatricula(),
                    "ES2",
                    "2026.1",
                    "T01");
        }
    }

    @Nested
    @DisplayName("Atualização de Notas")
    class AtualizacaoNotas {

        @Test
        @DisplayName("Deve atualizar notas quando já existirem")
        void deveAtualizarNotasExistentes() throws Exception {

            Nota nota = new Nota(aluno.getMatricula(), "ES2", "2026.1", "T01");

            nota.setEtapa1(5.0);
            nota.setEtapa2(6.0);

            when(turmaRepository.buscarPorChaveUnica("ES2", "2026.1", "T01")).thenReturn(turma);

            when(matriculaRepository.buscarPorChaveUnica(aluno.getMatricula(), "ES2", "2026.1", "T01"))
                    .thenReturn(matricula);

            when(notaRepository.buscarPorChaveUnica(aluno.getMatricula(), "ES2", "2026.1", "T01")).thenReturn(nota);

            service.lancarNotas(professor, aluno.getMatricula(), "ES2", "2026.1", "T01", 9.0, 8.5);

            assertEquals(9.0, nota.getEtapa1());
            assertEquals(8.5, nota.getEtapa2());

            verify(notaRepository).atualizar(nota);
            verify(notaRepository, never()).salvar(any());
        }

        @Test
        @DisplayName("Não deve salvar nova nota quando já existir")
        void naoDeveSalvarNovaNotaQuandoJaExistir() throws Exception {

            Nota nota = new Nota(aluno.getMatricula(), "ES2", "2026.1", "T01");

            when(turmaRepository.buscarPorChaveUnica("ES2", "2026.1", "T01")).thenReturn(turma);

            when(matriculaRepository.buscarPorChaveUnica(aluno.getMatricula(), "ES2", "2026.1", "T01"))
                    .thenReturn(matricula);

            when(notaRepository.buscarPorChaveUnica(aluno.getMatricula(), "ES2", "2026.1", "T01")).thenReturn(nota);

            service.lancarNotas(professor, aluno.getMatricula(), "ES2", "2026.1", "T01", 7.0, 8.0);

            verify(notaRepository, never()).salvar(any());
            verify(notaRepository).atualizar(any(Nota.class));
        }

        @Test
        @DisplayName("Deve salvar nova nota quando ainda não existir")
        void deveSalvarNovaNotaQuandoNaoExistir() throws Exception {

            when(turmaRepository.buscarPorChaveUnica(
                    "ES2",
                    "2026.1",
                    "T01"))
                    .thenReturn(turma);

            when(matriculaRepository.buscarPorChaveUnica(
                    aluno.getMatricula(),
                    "ES2",
                    "2026.1",
                    "T01"))
                    .thenReturn(matricula);

            when(notaRepository.buscarPorChaveUnica(
                    aluno.getMatricula(),
                    "ES2",
                    "2026.1",
                    "T01"))
                    .thenReturn(null);

            service.lancarNotas(
                    professor,
                    aluno.getMatricula(),
                    "ES2",
                    "2026.1",
                    "T01",
                    10.0,
                    10.0);

            verify(notaRepository).salvar(any(Nota.class));
            verify(notaRepository, never()).atualizar(any());
        }

        @Test
        @DisplayName("Deve atualizar corretamente etapa1 e etapa2")
        void deveAtualizarCorretamenteEtapas() throws Exception {

            Nota nota = new Nota(aluno.getMatricula(), "ES2", "2026.1", "T01");

            nota.setEtapa1(3.0);
            nota.setEtapa2(4.0);

            when(turmaRepository.buscarPorChaveUnica("ES2", "2026.1", "T01")).thenReturn(turma);

            when(matriculaRepository.buscarPorChaveUnica(aluno.getMatricula(), "ES2", "2026.1", "T01"))
                    .thenReturn(matricula);

            when(notaRepository.buscarPorChaveUnica(aluno.getMatricula(), "ES2", "2026.1", "T01")).thenReturn(nota);

            service.lancarNotas(professor, aluno.getMatricula(), "ES2", "2026.1", "T01", 8.75, 9.25);

            assertAll(() -> assertEquals(8.75, nota.getEtapa1()), () -> assertEquals(9.25, nota.getEtapa2()));

            verify(notaRepository).atualizar(nota);
        }
    }
}