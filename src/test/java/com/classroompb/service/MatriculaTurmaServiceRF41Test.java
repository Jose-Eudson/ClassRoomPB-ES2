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

import com.classroompb.model.Administrador;
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

@DisplayName("RF41 - Relatório de ocupação de vagas por período")
public class MatriculaTurmaServiceRF41Test {

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
        HistoricoRepository historicoRepository = new HistoricoRepository(
                tempDir.resolve("historicos.json").toString());

        service = new MatriculaTurmaService(matriculaRepository, turmaRepository, periodoRepository,
                disciplinaRepository, historicoRepository);

        coordenador = new Coordenador("C001", "Ana", "ana@test.com", "senha");
    }

    @Nested
    @DisplayName("listarTurmasComOcupacaoPorPeriodo()")
    class ListarOcupacaoVagas {

        @Test
        @DisplayName("Deve retornar todas as turmas do período")
        void deveRetornarTurmasDoPeriodo() throws Exception {
            turmaRepository.salvar(new Turma("T01", "MAT001", "2026.1", 30, "Seg 10h", "Bloco A", "P001"));
            turmaRepository.salvar(new Turma("T01", "FIS001", "2026.1", 20, "Ter 14h", "Bloco B", "P002"));
            turmaRepository.salvar(new Turma("T01", "MAT001", "2025.2", 25, "Qua 8h", "Bloco C", "P001"));

            List<Turma> resultado = service.listarTurmasComOcupacaoPorPeriodo(coordenador, "2026.1");

            assertEquals(2, resultado.size());
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há turmas no período")
        void deveRetornarVaziaParaPeriodoSemTurmas() throws Exception {
            List<Turma> resultado = service.listarTurmasComOcupacaoPorPeriodo(coordenador, "2099.1");
            assertTrue(resultado.isEmpty());
        }

        @Test
        @DisplayName("Deve calcular vagas disponíveis corretamente")
        void deveCalcularVagasDisponiveis() throws Exception {
            turmaRepository.salvar(new Turma("T01", "MAT001", "2026.1", 30, "Seg 10h", "Bloco A", "P001"));

            MatriculaTurma m1 = new MatriculaTurma("A001", "MAT001", "2026.1", "T01");
            m1.setStatus(StatusMatricula.CONFIRMADA);
            MatriculaTurma m2 = new MatriculaTurma("A002", "MAT001", "2026.1", "T01");
            m2.setStatus(StatusMatricula.CONFIRMADA);
            matriculaRepository.salvar(m1);
            matriculaRepository.salvar(m2);

            List<Turma> turmas = service.listarTurmasComOcupacaoPorPeriodo(coordenador, "2026.1");
            Turma turma = turmas.get(0);

            assertEquals(28, service.vagasDisponiveis(turma));
        }

        @Test
        @DisplayName("Deve lançar exceção quando usuário não é coordenador")
        void deveLancarExcecaoParaNaoCoordenador() {
            Usuario admin = new Administrador("AD001", "Admin", "admin@test.com", "senha");
            assertThrows(Exception.class, () -> service.listarTurmasComOcupacaoPorPeriodo(admin, "2026.1"));
        }

        @Test
        @DisplayName("Deve lançar exceção para período vazio")
        void deveLancarExcecaoParaPeriodoVazio() {
            assertThrows(Exception.class, () -> service.listarTurmasComOcupacaoPorPeriodo(coordenador, ""));
        }

        @Test
        @DisplayName("Deve lançar exceção para período nulo")
        void deveLancarExcecaoParaPeriodoNulo() {
            assertThrows(Exception.class, () -> service.listarTurmasComOcupacaoPorPeriodo(coordenador, null));
        }
    }
}
