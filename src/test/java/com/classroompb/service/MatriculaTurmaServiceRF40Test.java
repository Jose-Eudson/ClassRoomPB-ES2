package com.classroompb.service;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.classroompb.model.Coordenador;
import com.classroompb.model.MatriculaTurma;
import com.classroompb.model.StatusMatricula;
import com.classroompb.model.Turma;
import com.classroompb.model.Usuario;
import com.classroompb.repository.DisciplinaRepository;
import com.classroompb.repository.HistoricoRepository;
import com.classroompb.repository.MatriculaTurmaRepository;
import com.classroompb.repository.PeriodoLetivoRepository;
import com.classroompb.repository.TurmaRepository;

/**
 * Testes unitários para RF40: relatório de alunos matriculados por turma.
 */
@DisplayName("RF40 - Relatório de alunos matriculados por turma")
public class MatriculaTurmaServiceRF40Test {

    @TempDir
    Path tempDir;

    private MatriculaTurmaService service;
    private MatriculaTurmaRepository matriculaRepository;
    private TurmaRepository turmaRepository;
    private Usuario coordenador;

    @BeforeEach
    void setUp() {
        matriculaRepository = new MatriculaTurmaRepository(tempDir.resolve("matriculas.json").toString());
        turmaRepository = new TurmaRepository(tempDir.resolve("turmas.json").toString());
        PeriodoLetivoRepository periodoRepository = new PeriodoLetivoRepository(
                tempDir.resolve("periodos.json").toString());
        DisciplinaRepository disciplinaRepository = new DisciplinaRepository(
                tempDir.resolve("disciplinas.json").toString());
        HistoricoRepository historicoRepository = new HistoricoRepository();

        service = new MatriculaTurmaService(matriculaRepository, turmaRepository, periodoRepository,
                disciplinaRepository, historicoRepository);

        coordenador = new Coordenador("C001", "Ana", "ana@test.com", "senha");

        turmaRepository.salvar(new Turma("T01", "MAT001", "2026.1", 30, "Seg/Qua 10h-12h", "Bloco A", "P001"));
    }

    private MatriculaTurma matriculaConfirmada(String matriculaAluno) {
        MatriculaTurma m = new MatriculaTurma(matriculaAluno, "MAT001", "2026.1", "T01");
        m.setStatus(StatusMatricula.CONFIRMADA);
        return m;
    }

    @Nested
    @DisplayName("listarAlunosMatriculadosPorTurma()")
    class ListarAlunosMatriculados {

        @Test
        @DisplayName("Deve retornar apenas alunos CONFIRMADOS na turma")
        void deveRetornarApenasConfirmados() throws Exception {
            matriculaRepository.salvar(matriculaConfirmada("A001"));
            matriculaRepository.salvar(matriculaConfirmada("A002"));

            MatriculaTurma pendente = new MatriculaTurma("A003", "MAT001", "2026.1", "T01");
            pendente.setStatus(StatusMatricula.PENDENTE);
            matriculaRepository.salvar(pendente);

            List<MatriculaTurma> resultado = service.listarAlunosMatriculadosPorTurma(coordenador, "MAT001", "2026.1",
                    "T01");

            assertEquals(2, resultado.size());
            assertTrue(resultado.stream().allMatch(m -> m.getStatus() == StatusMatricula.CONFIRMADA));
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há alunos confirmados")
        void deveRetornarVaziaQuandoSemConfirmados() throws Exception {
            MatriculaTurma pendente = new MatriculaTurma("A001", "MAT001", "2026.1", "T01");
            pendente.setStatus(StatusMatricula.PENDENTE);
            matriculaRepository.salvar(pendente);

            List<MatriculaTurma> resultado = service.listarAlunosMatriculadosPorTurma(coordenador, "MAT001", "2026.1",
                    "T01");

            assertTrue(resultado.isEmpty());
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando turma não tem nenhuma matrícula")
        void deveRetornarVaziaParaTurmaSemMatriculas() throws Exception {
            List<MatriculaTurma> resultado = service.listarAlunosMatriculadosPorTurma(coordenador, "MAT001", "2026.1",
                    "T01");
            assertTrue(resultado.isEmpty());
        }

        @Test
        @DisplayName("Deve lançar exceção quando turma não existe")
        void deveLancarExcecaoParaTurmaInexistente() {
            assertThrows(Exception.class,
                    () -> service.listarAlunosMatriculadosPorTurma(coordenador, "XXX999", "2026.1", "T99"));
        }

        @Test
        @DisplayName("Deve lançar exceção quando usuário não é coordenador")
        void deveLancarExcecaoParaNaoCoordenador() {
            Usuario aluno = new com.classroompb.model.Aluno("A001", "João", "joao@test.com", "senha");
            assertThrows(Exception.class,
                    () -> service.listarAlunosMatriculadosPorTurma(aluno, "MAT001", "2026.1", "T01"));
        }

        @Test
        @DisplayName("Não deve incluir alunos cancelados ou em lista de espera")
        void naoDeveIncluirOutrosStatus() throws Exception {
            MatriculaTurma cancelada = new MatriculaTurma("A001", "MAT001", "2026.1", "T01");
            cancelada.setStatus(StatusMatricula.CANCELADA);

            MatriculaTurma espera = new MatriculaTurma("A002", "MAT001", "2026.1", "T01");
            espera.setStatus(StatusMatricula.LISTA_ESPERA);

            matriculaRepository.salvar(cancelada);
            matriculaRepository.salvar(espera);

            List<MatriculaTurma> resultado = service.listarAlunosMatriculadosPorTurma(coordenador, "MAT001", "2026.1",
                    "T01");

            assertTrue(resultado.isEmpty());
        }
    }
}
