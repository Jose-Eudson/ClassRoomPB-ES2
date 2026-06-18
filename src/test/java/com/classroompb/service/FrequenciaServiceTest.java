package com.classroompb.service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.classroompb.model.Aluno;
import com.classroompb.model.Coordenador;
import com.classroompb.model.MatriculaTurma;
import com.classroompb.model.Professor;
import com.classroompb.model.RegistroFrequencia;
import com.classroompb.model.StatusFrequencia;
import com.classroompb.model.StatusMatricula;
import com.classroompb.model.Turma;
import com.classroompb.repository.FrequenciaRepository;
import com.classroompb.repository.MatriculaTurmaRepository;
import com.classroompb.repository.TurmaRepository;

/**
 * RF27: Testes unitarios do registro de presenca/falta por aula.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RF27 - Registro de frequencia")
public class FrequenciaServiceTest {

    @Mock
    private FrequenciaRepository frequenciaRepository;

    @Mock
    private TurmaRepository turmaRepository;

    @Mock
    private MatriculaTurmaRepository matriculaRepository;

    private FrequenciaService service;

    private Professor professor;
    private Professor outroProfessor;
    private Aluno aluno;
    private Coordenador coordenador;
    private Turma turma;
    private MatriculaTurma matriculaConfirmada;

    private static final String DISC = "MAT001";
    private static final String PER = "2026.1";
    private static final String TURMA = "T01";
    private static final LocalDate DATA_AULA = LocalDate.of(2026, 4, 10);

    @BeforeEach
    void setUp() {
        service = new FrequenciaService(frequenciaRepository, turmaRepository, matriculaRepository);

        professor = new Professor("P0001", "Prof", "prof@test.com", "senha");
        outroProfessor = new Professor("P0002", "Outro Prof", "outro@test.com", "senha");
        aluno = new Aluno("A0001", "Aluno", "aluno@test.com", "senha");
        coordenador = new Coordenador("C0001", "Coord", "coord@test.com", "senha");

        turma = new Turma(TURMA, DISC, PER, 40, "Seg/Qua 10h-12h", "A-101", professor.getMatricula());
        matriculaConfirmada = new MatriculaTurma(aluno.getMatricula(), DISC, PER, TURMA);
        matriculaConfirmada.setStatus(StatusMatricula.CONFIRMADA);
    }

    private void mockCenarioValido() {
        when(turmaRepository.buscarPorChaveUnica(DISC, PER, TURMA)).thenReturn(turma);
        when(matriculaRepository.buscarPorChaveUnica(aluno.getMatricula(), DISC, PER, TURMA))
                .thenReturn(matriculaConfirmada);
        when(frequenciaRepository.buscarPorChaveUnica(aluno.getMatricula(), DISC, PER, TURMA, DATA_AULA))
                .thenReturn(null);
    }

    @Nested
    @DisplayName("Registro com sucesso")
    class RegistroComSucesso {

        @Test
        @DisplayName("Deve registrar presenca para aluno com matricula confirmada")
        void deveRegistrarPresenca() throws Exception {
            mockCenarioValido();

            RegistroFrequencia resultado = service.registrarPresenca(professor, aluno.getMatricula(), DISC, PER, TURMA,
                    DATA_AULA);

            ArgumentCaptor<RegistroFrequencia> captor = ArgumentCaptor.forClass(RegistroFrequencia.class);
            verify(frequenciaRepository).salvar(captor.capture());

            RegistroFrequencia salva = captor.getValue();
            assertEquals(StatusFrequencia.PRESENTE, salva.getStatus());
            assertEquals(aluno.getMatricula(), salva.getMatriculaAluno());
            assertEquals(professor.getMatricula(), salva.getMatriculaProfessor());
            assertEquals(DATA_AULA, salva.getDataAula());
            assertEquals(salva, resultado);
        }

        @Test
        @DisplayName("Deve registrar falta para aluno com matricula confirmada")
        void deveRegistrarFalta() throws Exception {
            mockCenarioValido();

            RegistroFrequencia resultado = service.registrarFalta(professor, aluno.getMatricula(), DISC, PER, TURMA,
                    DATA_AULA);

            ArgumentCaptor<RegistroFrequencia> captor = ArgumentCaptor.forClass(RegistroFrequencia.class);
            verify(frequenciaRepository).salvar(captor.capture());

            assertEquals(StatusFrequencia.FALTA, captor.getValue().getStatus());
            assertEquals(StatusFrequencia.FALTA, resultado.getStatus());
        }

        @Test
        @DisplayName("Deve normalizar espacos nas entradas antes de salvar")
        void deveNormalizarEspacos() throws Exception {
            mockCenarioValido();

            service.registrarFrequencia(professor, "  A0001  ", "  MAT001  ", "  2026.1  ", "  T01  ", DATA_AULA,
                    StatusFrequencia.PRESENTE);

            ArgumentCaptor<RegistroFrequencia> captor = ArgumentCaptor.forClass(RegistroFrequencia.class);
            verify(frequenciaRepository).salvar(captor.capture());

            assertEquals("A0001", captor.getValue().getMatriculaAluno());
            assertEquals(DISC, captor.getValue().getCodigoDisciplina());
            assertEquals(PER, captor.getValue().getCodigoPeriodo());
            assertEquals(TURMA, captor.getValue().getCodigoTurma());
        }

        @Test
        @DisplayName("Deve atualizar registro existente ao corrigir presenca/falta")
        void deveAtualizarRegistroExistente() throws Exception {
            RegistroFrequencia existente = new RegistroFrequencia(aluno.getMatricula(), DISC, PER, TURMA, DATA_AULA,
                    StatusFrequencia.FALTA, professor.getMatricula());

            when(turmaRepository.buscarPorChaveUnica(DISC, PER, TURMA)).thenReturn(turma);
            when(matriculaRepository.buscarPorChaveUnica(aluno.getMatricula(), DISC, PER, TURMA))
                    .thenReturn(matriculaConfirmada);
            when(frequenciaRepository.buscarPorChaveUnica(aluno.getMatricula(), DISC, PER, TURMA, DATA_AULA))
                    .thenReturn(existente);

            RegistroFrequencia resultado = service.registrarPresenca(professor, aluno.getMatricula(), DISC, PER, TURMA,
                    DATA_AULA);

            assertEquals(StatusFrequencia.PRESENTE, resultado.getStatus());
            verify(frequenciaRepository).atualizar(existente);
            verify(frequenciaRepository, never()).salvar(any());
        }
    }

    @Nested
    @DisplayName("Regras de permissao e negocio")
    class RegrasDePermissaoENegocio {

        @Test
        @DisplayName("Nao deve permitir usuario que nao seja professor")
        void naoDevePermitirNaoProfessor() {
            Exception ex = assertThrows(Exception.class,
                    () -> service.registrarPresenca(coordenador, aluno.getMatricula(), DISC, PER, TURMA, DATA_AULA));

            assertTrue(ex.getMessage().contains("Apenas professores"));
            verify(frequenciaRepository, never()).salvar(any());
        }

        @Test
        @DisplayName("Nao deve permitir professor que nao e responsavel pela turma")
        void naoDevePermitirProfessorNaoResponsavel() {
            when(turmaRepository.buscarPorChaveUnica(DISC, PER, TURMA)).thenReturn(turma);

            Exception ex = assertThrows(Exception.class,
                    () -> service.registrarPresenca(outroProfessor, aluno.getMatricula(), DISC, PER, TURMA, DATA_AULA));

            assertTrue(ex.getMessage().contains("professor responsavel"));
            verify(matriculaRepository, never()).buscarPorChaveUnica(any(), any(), any(), any());
            verify(frequenciaRepository, never()).salvar(any());
        }

        @Test
        @DisplayName("Nao deve registrar frequencia para turma inexistente")
        void naoDeveRegistrarTurmaInexistente() {
            when(turmaRepository.buscarPorChaveUnica(DISC, PER, TURMA)).thenReturn(null);

            Exception ex = assertThrows(Exception.class,
                    () -> service.registrarPresenca(professor, aluno.getMatricula(), DISC, PER, TURMA, DATA_AULA));

            assertTrue(ex.getMessage().contains("nao encontrada"));
            verify(frequenciaRepository, never()).salvar(any());
        }

        @Test
        @DisplayName("Nao deve registrar frequencia para aluno sem matricula confirmada")
        void naoDeveRegistrarAlunoSemMatriculaConfirmada() {
            MatriculaTurma listaEspera = new MatriculaTurma(aluno.getMatricula(), DISC, PER, TURMA);
            listaEspera.setStatus(StatusMatricula.LISTA_ESPERA);

            when(turmaRepository.buscarPorChaveUnica(DISC, PER, TURMA)).thenReturn(turma);
            when(matriculaRepository.buscarPorChaveUnica(aluno.getMatricula(), DISC, PER, TURMA))
                    .thenReturn(listaEspera);

            Exception ex = assertThrows(Exception.class,
                    () -> service.registrarPresenca(professor, aluno.getMatricula(), DISC, PER, TURMA, DATA_AULA));

            assertTrue(ex.getMessage().contains("matricula confirmada"));
            verify(frequenciaRepository, never()).salvar(any());
        }

        @Test
        @DisplayName("Nao deve registrar frequencia quando data da aula e nula")
        void naoDeveRegistrarDataNula() {
            Exception ex = assertThrows(Exception.class,
                    () -> service.registrarPresenca(professor, aluno.getMatricula(), DISC, PER, TURMA, null));

            assertTrue(ex.getMessage().contains("Data da aula"));
            verify(turmaRepository, never()).buscarPorChaveUnica(any(), any(), any());
        }

        @Test
        @DisplayName("Nao deve registrar frequencia quando status e nulo")
        void naoDeveRegistrarStatusNulo() {
            Exception ex = assertThrows(Exception.class, () -> service.registrarFrequencia(professor,
                    aluno.getMatricula(), DISC, PER, TURMA, DATA_AULA, null));

            assertTrue(ex.getMessage().contains("Status"));
            verify(turmaRepository, never()).buscarPorChaveUnica(any(), any(), any());
        }
    }

    @Test
    @DisplayName("Professor responsavel deve listar frequencia registrada da aula")
    void deveListarFrequenciaDaAula() throws Exception {
        RegistroFrequencia registro = new RegistroFrequencia(aluno.getMatricula(), DISC, PER, TURMA, DATA_AULA,
                StatusFrequencia.PRESENTE, professor.getMatricula());

        when(turmaRepository.buscarPorChaveUnica(DISC, PER, TURMA)).thenReturn(turma);
        when(frequenciaRepository.listarPorTurmaEData(DISC, PER, TURMA, DATA_AULA))
                .thenReturn(Collections.singletonList(registro));

        List<RegistroFrequencia> resultado = service.listarFrequenciaDaAula(professor, DISC, PER, TURMA, DATA_AULA);

        assertEquals(1, resultado.size());
        assertEquals(StatusFrequencia.PRESENTE, resultado.get(0).getStatus());
        verify(frequenciaRepository).listarPorTurmaEData(DISC, PER, TURMA, DATA_AULA);
    }
}
