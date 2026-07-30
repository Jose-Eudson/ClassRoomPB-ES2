package com.classroompb.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.classroompb.model.Aluno;
import com.classroompb.model.Aula;
import com.classroompb.model.Diario;
import com.classroompb.model.Disciplina;
import com.classroompb.model.Historico;
import com.classroompb.model.MatriculaTurma;
import com.classroompb.model.PeriodoLetivo;
import com.classroompb.model.Professor;
import com.classroompb.model.SituacaoDiario;
import com.classroompb.model.StatusFrequencia;
import com.classroompb.model.StatusMatricula;
import com.classroompb.model.Turma;
import com.classroompb.repository.AulaRepository;
import com.classroompb.repository.DiarioRepository;
import com.classroompb.repository.DisciplinaRepository;
import com.classroompb.repository.FrequenciaRepository;
import com.classroompb.repository.HistoricoRepository;
import com.classroompb.repository.MatriculaTurmaRepository;
import com.classroompb.repository.NotaRepository;
import com.classroompb.repository.PeriodoLetivoRepository;
import com.classroompb.repository.TurmaRepository;
import com.classroompb.repository.UsuarioRepository;

class HistoricoIntegracaoServiceTest {

    @TempDir
    Path tempDir;
    private HistoricoRepository historicoRepository;
    private NotaService notaService;
    private FrequenciaService frequenciaService;
    private HistoricoService consultaService;
    // private DiarioService diarioService;
    private Professor professor;
    private Aluno aluno;
    private PeriodoLetivoRepository periodoRepository;
    private AulaRepository aulaRepository;

    @BeforeEach
    void setUp() {
        HistoricoRepository historicos = new HistoricoRepository(tempDir.resolve("historicos.json").toString());
        historicoRepository = historicos;
        NotaRepository notas = new NotaRepository(tempDir.resolve("notas.json").toString());
        FrequenciaRepository frequencias = new FrequenciaRepository(tempDir.resolve("frequencias.json").toString());
        TurmaRepository turmas = new TurmaRepository(tempDir.resolve("turmas.json").toString());
        DiarioRepository diarios = new DiarioRepository(tempDir.resolve("diarios.json").toString());
        MatriculaTurmaRepository matriculas = new MatriculaTurmaRepository(
                tempDir.resolve("matriculas.json").toString());
        DisciplinaRepository disciplinas = new DisciplinaRepository(tempDir.resolve("disciplinas.json").toString());
        UsuarioRepository usuarios = new UsuarioRepository(tempDir.resolve("usuarios.json").toString());
        aulaRepository = new AulaRepository(tempDir.resolve("aulas.json").toString());
        periodoRepository = new PeriodoLetivoRepository(tempDir.resolve("periodos.json").toString());

        professor = new Professor("P1", "Joao da Silva", "p@teste.com", "123");
        aluno = new Aluno("A1", "Aluno", "a@teste.com", "123", "ES");
        usuarios.salvar(professor);
        usuarios.salvar(aluno);
        disciplinas.salvar(new Disciplina("ES2", "Engenharia de Software II", 60, 4, new ArrayList<>()));
        turmas.salvar(new Turma("T1", "ES2", "2026.1", 30, "Seg", "1", "P1"));
        MatriculaTurma matricula = new MatriculaTurma("A1", "ES2", "2026.1", "T1");
        matricula.setStatus(StatusMatricula.CONFIRMADA);
        matriculas.salvar(matricula);
        periodoRepository.salvar(
                new PeriodoLetivo("2026.1", 2026, 1, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 6, 30), true));
        diarios.salvar(
                new Diario("D1", "T1", "Engenharia de Software II", "P1", "08:00", "Sala 1", 60, SituacaoDiario.ATIVO));
        aulaRepository.salvar(new Aula("A01", "D1", LocalDate.of(2026, 3, 1), "Conteudo", 1));
        frequenciaService = new FrequenciaService(frequencias, turmas, matriculas, historicos, notas, disciplinas,
                usuarios, aulaRepository, diarios);
        notaService = new NotaService(notas, turmas, matriculas, historicos, frequencias, disciplinas, usuarios,
                periodoRepository);
        consultaService = new HistoricoService(historicos, usuarios, periodoRepository);
    }

    @Test
    void notasEFrequenciaDevemCriarEAtualizarMesmoRegistroCompartilhado() throws Exception {
        frequenciaService.registrarFrequencia(professor, "A1", "ES2", "2026.1", "T1", "A01", LocalDate.of(2026, 3, 1),
                StatusFrequencia.PRESENTE);
        notaService.lancarNotas(professor, "A1", "ES2", "2026.1", "T1", 8.0, 9.0);

        assertTrue(consultaService.consultarHistoricoAluno(aluno).isEmpty());
        Historico historico = historicoRepository.buscarPorAluno("A1").get(0);
        assertEquals(8.5, historico.getNotaFinal());
        assertEquals(100.0, historico.getFrequencia());
        assertEquals("APROVADO", historico.getSituacao());
        assertEquals("Engenharia de Software II", historico.getNomeDisciplina());
        assertEquals("Joao da Silva", historico.getNomeProfessor());

        notaService.alterarNotas(professor, "A1", "ES2", "2026.1", "T1", 5.0, 5.0);
        frequenciaService.registrarFrequencia(professor, "A1", "ES2", "2026.1", "T1", "A01", LocalDate.of(2026, 3, 2),
                StatusFrequencia.FALTA);

        PeriodoLetivo periodo = periodoRepository.buscarPorCodigo("2026.1");
        periodo.setAtivo(false);
        periodo.setEncerrado(true);
        periodoRepository.atualizarDados();

        assertEquals(1, historicoRepository.buscarPorAluno("A1").size());
        assertEquals(1, consultaService.consultarHistoricoAluno(aluno).size());
        Historico atualizado = consultaService.consultarHistoricoAluno(aluno).get(0);
        assertEquals(5.0, atualizado.getNotaFinal());
        assertEquals(50.0, atualizado.getFrequencia());
        assertEquals("REPROVADO POR FALTA", atualizado.getSituacao());
    }
}
