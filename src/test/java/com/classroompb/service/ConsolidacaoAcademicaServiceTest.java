package com.classroompb.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.classroompb.model.Avaliacao;
import com.classroompb.model.Diario;
import com.classroompb.model.Disciplina;
import com.classroompb.model.Historico;
import com.classroompb.model.MatriculaTurma;
import com.classroompb.model.Nota;
import com.classroompb.model.Professor;
import com.classroompb.model.RegistroFrequencia;
import com.classroompb.model.SituacaoDiario;
import com.classroompb.model.StatusFrequencia;
import com.classroompb.model.StatusMatricula;
import com.classroompb.model.Turma;
import com.classroompb.repository.AvaliacaoRepository;
import com.classroompb.repository.DiarioRepository;
import com.classroompb.repository.DisciplinaRepository;
import com.classroompb.repository.FrequenciaRepository;
import com.classroompb.repository.MatriculaTurmaRepository;
import com.classroompb.repository.NotaRepository;
import com.classroompb.repository.TurmaRepository;
import com.classroompb.repository.UsuarioRepository;

class ConsolidacaoAcademicaServiceTest {
    private TurmaRepository turmaRepository;
    private DiarioRepository diarioRepository;
    private MatriculaTurmaRepository matriculaRepository;
    private AvaliacaoRepository avaliacaoRepository;
    private NotaRepository notaRepository;
    private FrequenciaRepository frequenciaRepository;
    private DisciplinaRepository disciplinaRepository;
    private UsuarioRepository usuarioRepository;
    private HistoricoService historicoService;
    private ConsolidacaoAcademicaService service;
    private Turma turma;
    private Diario diario60;
    private Diario diario20;
    private MatriculaTurma matricula;

    @BeforeEach
    void preparar() {
        turmaRepository = Mockito.mock(TurmaRepository.class);
        diarioRepository = Mockito.mock(DiarioRepository.class);
        matriculaRepository = Mockito.mock(MatriculaTurmaRepository.class);
        avaliacaoRepository = Mockito.mock(AvaliacaoRepository.class);
        notaRepository = Mockito.mock(NotaRepository.class);
        frequenciaRepository = Mockito.mock(FrequenciaRepository.class);
        disciplinaRepository = Mockito.mock(DisciplinaRepository.class);
        usuarioRepository = Mockito.mock(UsuarioRepository.class);
        historicoService = Mockito.mock(HistoricoService.class);
        service = new ConsolidacaoAcademicaService(turmaRepository, diarioRepository, matriculaRepository,
                avaliacaoRepository, notaRepository, frequenciaRepository, disciplinaRepository, usuarioRepository,
                historicoService);
        turma = new Turma("T1", "ESW2", "2026.1", 20, null, null, null);
        diario60 = diario("D60", "P1", 60);
        diario20 = diario("D20", "P2", 20);
        matricula = new MatriculaTurma("A1", "ESW2", "2026.1", "T1");
        matricula.setStatus(StatusMatricula.CONFIRMADA);
        Mockito.when(diarioRepository.buscarPorTurma("ESW2", "2026.1", "T1"))
                .thenReturn(List.of(diario60, diario20));
        Mockito.when(matriculaRepository.listarPorTurma("ESW2", "2026.1", "T1"))
                .thenReturn(List.of(matricula));
        Mockito.when(avaliacaoRepository.listarPorDiario("D60")).thenReturn(List.of(avaliacao("AV60", "D60")));
        Mockito.when(avaliacaoRepository.listarPorDiario("D20")).thenReturn(List.of(avaliacao("AV20", "D20")));
        Mockito.when(disciplinaRepository.buscarPorCodigo("ESW2"))
                .thenReturn(new Disciplina("ESW2", "Engenharia", 80, 4, List.of()));
        Mockito.when(usuarioRepository.buscarPorMatricula("P1"))
                .thenReturn(Optional.of(new Professor("P1", "Prof 1", "p1@teste.com", "senha")));
        configurarNota("AV60", "D60", 8.0);
        configurarNota("AV20", "D20", 6.0);
        Mockito.when(frequenciaRepository.listarPorAlunoEDiario("A1", "D60"))
                .thenReturn(List.of(frequencia("D60", StatusFrequencia.PRESENTE)));
        Mockito.when(frequenciaRepository.listarPorAlunoEDiario("A1", "D20"))
                .thenReturn(List.of(frequencia("D20", StatusFrequencia.PRESENTE)));
    }

    @Test
    void deveCalcularMediaPonderadaPelaCargaHoraria() throws Exception {
        service.consolidarTurma(turma);

        ArgumentCaptor<Historico> captor = ArgumentCaptor.forClass(Historico.class);
        Mockito.verify(historicoService).registrarHistorico(captor.capture());
        Assertions.assertEquals(7.5, captor.getValue().getNotaFinal(), 0.0001);
        Assertions.assertEquals("APROVADO", captor.getValue().getSituacao());
        Assertions.assertEquals("Multiplos professores", captor.getValue().getNomeProfessor());
    }

    @Test
    void deveCalcularMediaAritmeticaQuandoCargasForemIguais() throws Exception {
        diario60.setCargaHoraria(20);
        service.consolidarTurma(turma);

        ArgumentCaptor<Historico> captor = ArgumentCaptor.forClass(Historico.class);
        Mockito.verify(historicoService).registrarHistorico(captor.capture());
        Assertions.assertEquals(7.0, captor.getValue().getNotaFinal(), 0.0001);
    }

    @Test
    void deveBloquearRecuperacaoSemGravarHistorico() {
        configurarNota("AV60", "D60", 5.0);
        configurarNota("AV20", "D20", 5.0);
        Exception erro = Assertions.assertThrows(Exception.class, () -> service.consolidarTurma(turma));
        Assertions.assertTrue(erro.getMessage().contains("A1"));
        Mockito.verifyNoInteractions(historicoService);
    }

    @Test
    void deveAceitarTurmaVaziaSemGerarHistorico() throws Exception {
        Mockito.when(matriculaRepository.listarPorTurma("ESW2", "2026.1", "T1")).thenReturn(List.of());
        service.consolidarTurma(turma);
        Mockito.verifyNoInteractions(historicoService);
    }

    @Test
    void deveValidarTurmaDiariosECargaHoraria() {
        Assertions.assertThrows(Exception.class, () -> service.consolidarTurma(null));
        Mockito.when(diarioRepository.buscarPorTurma("ESW2", "2026.1", "T1")).thenReturn(List.of());
        Assertions.assertThrows(Exception.class, () -> service.consolidarTurma(turma));
        Mockito.when(diarioRepository.buscarPorTurma("ESW2", "2026.1", "T1")).thenReturn(List.of(diario60));
        diario60.setSituacao(SituacaoDiario.ATIVO);
        Assertions.assertThrows(Exception.class, () -> service.consolidarTurma(turma));
        diario60.setSituacao(SituacaoDiario.ENCERRADO);
        diario60.setCargaHoraria(0);
        Assertions.assertThrows(Exception.class, () -> service.consolidarTurma(turma));
    }

    @Test
    void deveValidarAvaliacaoENotaPendentes() {
        Mockito.when(avaliacaoRepository.listarPorDiario("D60")).thenReturn(List.of());
        Assertions.assertThrows(Exception.class, () -> service.consolidarTurma(turma));
        Mockito.when(avaliacaoRepository.listarPorDiario("D60")).thenReturn(List.of(avaliacao("AV60", "D60")));
        Mockito.when(notaRepository.buscarPorAlunoEAvaliacao("A1", "AV60")).thenReturn(null);
        Assertions.assertThrows(Exception.class, () -> service.consolidarTurma(turma));
    }

    @Test
    void deveConsolidarTodasAsTurmasDoPeriodo() throws Exception {
        Mockito.when(turmaRepository.listarPorPeriodo("2026.1")).thenReturn(List.of(turma));
        service.consolidarPeriodo("2026.1");
        Mockito.verify(historicoService).registrarHistorico(Mockito.any(Historico.class));
        Assertions.assertSame(turmaRepository, service.getTurmaRepository());
    }

    private Diario diario(String codigo, String professor, int carga) {
        return new Diario(codigo, "ESW2", "2026.1", "T1", "Diario", professor, "SEG", "S1", carga,
                SituacaoDiario.ENCERRADO);
    }

    private Avaliacao avaliacao(String codigo, String diario) {
        return new Avaliacao(codigo, diario, "ESW2", "2026.1", "T1", "Prova", "E1", 1.0, 10.0);
    }

    private void configurarNota(String avaliacao, String diario, double valor) {
        Nota nota = new Nota("A1", "ESW2", "2026.1", "T1", diario, avaliacao, valor, "P1");
        Mockito.when(notaRepository.buscarPorAlunoEAvaliacao("A1", avaliacao)).thenReturn(nota);
    }

    private RegistroFrequencia frequencia(String diario, StatusFrequencia status) {
        return new RegistroFrequencia("A1", "ESW2", "2026.1", "T1", diario, "AU1", LocalDate.now(), status,
                "P1");
    }
}
