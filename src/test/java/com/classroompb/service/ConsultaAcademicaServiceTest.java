package com.classroompb.service;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.classroompb.model.Aluno;
import com.classroompb.model.Avaliacao;
import com.classroompb.model.Diario;
import com.classroompb.model.MatriculaTurma;
import com.classroompb.model.Nota;
import com.classroompb.model.Professor;
import com.classroompb.model.RegistroFrequencia;
import com.classroompb.model.SituacaoDiario;
import com.classroompb.model.StatusMatricula;
import com.classroompb.repository.AvaliacaoRepository;
import com.classroompb.repository.DiarioRepository;
import com.classroompb.repository.FrequenciaRepository;
import com.classroompb.repository.MatriculaTurmaRepository;
import com.classroompb.repository.NotaRepository;

class ConsultaAcademicaServiceTest {
    private MatriculaTurmaRepository matriculaRepository;
    private DiarioRepository diarioRepository;
    private FrequenciaRepository frequenciaRepository;
    private AvaliacaoRepository avaliacaoRepository;
    private NotaRepository notaRepository;
    private NotaService notaService;
    private ConsultaAcademicaService service;
    private Aluno aluno;
    private Diario diario;
    private MatriculaTurma matricula;

    @BeforeEach
    void preparar() {
        matriculaRepository = Mockito.mock(MatriculaTurmaRepository.class);
        diarioRepository = Mockito.mock(DiarioRepository.class);
        frequenciaRepository = Mockito.mock(FrequenciaRepository.class);
        avaliacaoRepository = Mockito.mock(AvaliacaoRepository.class);
        notaRepository = Mockito.mock(NotaRepository.class);
        notaService = Mockito.mock(NotaService.class);
        service = new ConsultaAcademicaService(matriculaRepository, diarioRepository, frequenciaRepository,
                avaliacaoRepository, notaRepository, notaService);
        aluno = new Aluno("A1", "Aluno", "a@teste.com", "senha");
        diario = new Diario("D1", "ESW2", "2026.1", "T1", "Teoria", "P1", "SEG", "S1", 20,
                SituacaoDiario.ATIVO);
        matricula = new MatriculaTurma("A1", "ESW2", "2026.1", "T1");
        matricula.setStatus(StatusMatricula.CONFIRMADA);
        Mockito.when(diarioRepository.buscarPorCodigo("D1")).thenReturn(diario);
        Mockito.when(matriculaRepository.buscarPorChaveUnica("A1", "ESW2", "2026.1", "T1"))
                .thenReturn(matricula);
    }

    @Test
    void deveListarDiariosUnicosDeMatriculasConfirmadas() throws Exception {
        MatriculaTurma cancelada = new MatriculaTurma("A1", "BD", "2026.1", "T2");
        cancelada.setStatus(StatusMatricula.CANCELADA);
        Mockito.when(matriculaRepository.listarPorAluno("A1")).thenReturn(List.of(matricula, matricula, cancelada));
        Mockito.when(diarioRepository.buscarPorTurma("ESW2", "2026.1", "T1")).thenReturn(List.of(diario));

        Assertions.assertEquals(List.of(diario), service.listarDiariosDoAluno(aluno));
    }

    @Test
    void deveConsultarFrequenciaAvaliacoesNotasEMedia() throws Exception {
        RegistroFrequencia frequencia = Mockito.mock(RegistroFrequencia.class);
        Avaliacao avaliacao = Mockito.mock(Avaliacao.class);
        Nota nota = Mockito.mock(Nota.class);
        Mockito.when(frequenciaRepository.listarPorAlunoEDiario("A1", "D1")).thenReturn(List.of(frequencia));
        Mockito.when(avaliacaoRepository.listarPorDiario("D1")).thenReturn(List.of(avaliacao));
        Mockito.when(notaRepository.listarPorAlunoEDiario("A1", "D1")).thenReturn(List.of(nota));
        Mockito.when(notaService.calcularMediaParcial("A1", "D1")).thenReturn(8.5);

        Assertions.assertEquals(1, service.listarFrequencia(aluno, "D1").size());
        Assertions.assertEquals(1, service.listarAvaliacoes(aluno, "D1").size());
        Assertions.assertEquals(1, service.listarNotas(aluno, "D1").size());
        Assertions.assertEquals(8.5, service.calcularMediaParcial(aluno, "D1"));
    }

    @Test
    void deveRejeitarUsuarioQueNaoSejaAluno() {
        Professor professor = new Professor("P1", "Prof", "p@teste.com", "senha");
        Assertions.assertThrows(Exception.class, () -> service.listarDiariosDoAluno(null));
        Assertions.assertThrows(Exception.class, () -> service.listarDiariosDoAluno(professor));
    }

    @Test
    void deveRejeitarDiarioInexistente() {
        Assertions.assertThrows(Exception.class, () -> service.listarNotas(aluno, "OUTRO"));
    }

    @Test
    void deveRejeitarAlunoSemMatriculaConfirmada() {
        matricula.setStatus(StatusMatricula.CANCELADA);
        Assertions.assertThrows(Exception.class, () -> service.listarAvaliacoes(aluno, "D1"));
        Mockito.when(matriculaRepository.buscarPorChaveUnica("A1", "ESW2", "2026.1", "T1")).thenReturn(null);
        Assertions.assertThrows(Exception.class, () -> service.listarFrequencia(aluno, "D1"));
    }
}
