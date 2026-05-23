package com.classroompb.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Testes unitários para a entidade Turma (RF10 / RF11 - Task 1.2.4).
 *
 * Cobre todos os métodos com 0% de cobertura identificados no relatório JaCoCo:
 *   - Construtor padrão Turma()
 *   - Construtor completo Turma(String, String, String, int, String, String, String)
 *   - getChaveUnica()
 *   - toString() — ramo com professor, sem professor, com sala e sem sala
 *   - Todos os setters (setCodigo, setCodigoDisciplina, setCodigoPeriodo,
 *                       setVagas, setHorario, setSala, setMatriculaProfessor)
 *   - Todos os getters (getCodigo, getCodigoDisciplina, getCodigoPeriodo,
 *                       getVagas, getHorario, getSala, getMatriculaProfessor)
 */
@DisplayName("Testes do modelo Turma")
public class TurmaModelTest {

    // =========================================================================
    // Construtores
    // =========================================================================

    @Nested
    @DisplayName("Construtores")
    class Construtores {

        @Test
        @DisplayName("Construtor padrão deve criar objeto não nulo")
        void construtorPadraoDeveCriarObjeto() {
            Turma turma = new Turma();
            assertNotNull(turma);
        }

        @Test
        @DisplayName("Construtor padrão deve deixar campos com valores padrão")
        void construtorPadraoDeveInicializarCamposNulos() {
            Turma turma = new Turma();
            assertNull(turma.getCodigo());
            assertNull(turma.getCodigoDisciplina());
            assertNull(turma.getCodigoPeriodo());
            assertEquals(0, turma.getVagas());
            assertNull(turma.getHorario());
            assertNull(turma.getSala());
            assertNull(turma.getMatriculaProfessor());
        }

        @Test
        @DisplayName("Construtor completo deve atribuir todos os campos corretamente")
        void construtorCompletoDeveAtribuirTodosOsCampos() {
            Turma turma = new Turma("T01", "MAT001", "2026.1", 40, "Seg/Qua 10h-12h", "Bloco A - 101", "P0001");

            assertEquals("T01",              turma.getCodigo());
            assertEquals("MAT001",           turma.getCodigoDisciplina());
            assertEquals("2026.1",           turma.getCodigoPeriodo());
            assertEquals(40,                 turma.getVagas());
            assertEquals("Seg/Qua 10h-12h", turma.getHorario());
            assertEquals("Bloco A - 101",   turma.getSala());
            assertEquals("P0001",            turma.getMatriculaProfessor());
        }

        @Test
        @DisplayName("Construtor completo deve aceitar professor nulo")
        void construtorCompletoDeveAceitarProfessorNulo() {
            Turma turma = new Turma("T01", "MAT001", "2026.1", 30, "Ter 14h-16h", "Bloco B - 202", null);
            assertNull(turma.getMatriculaProfessor());
        }
    }

    // =========================================================================
    // Getters e Setters
    // =========================================================================

    @Nested
    @DisplayName("Getters e Setters")
    class GettersSetters {

        private Turma turma;

        @BeforeEach
        void setUp() {
            turma = new Turma();
        }

        @Test
        @DisplayName("setCodigo e getCodigo devem funcionar corretamente")
        void setGetCodigo() {
            turma.setCodigo("T02");
            assertEquals("T02", turma.getCodigo());
        }

        @Test
        @DisplayName("setCodigoDisciplina e getCodigoDisciplina devem funcionar corretamente")
        void setGetCodigoDisciplina() {
            turma.setCodigoDisciplina("FIS001");
            assertEquals("FIS001", turma.getCodigoDisciplina());
        }

        @Test
        @DisplayName("setCodigoPeriodo e getCodigoPeriodo devem funcionar corretamente")
        void setGetCodigoPeriodo() {
            turma.setCodigoPeriodo("2025.2");
            assertEquals("2025.2", turma.getCodigoPeriodo());
        }

        @Test
        @DisplayName("setVagas e getVagas devem funcionar corretamente")
        void setGetVagas() {
            turma.setVagas(50);
            assertEquals(50, turma.getVagas());
        }

        @Test
        @DisplayName("setHorario e getHorario devem funcionar corretamente")
        void setGetHorario() {
            turma.setHorario("Sex 08h-12h");
            assertEquals("Sex 08h-12h", turma.getHorario());
        }

        @Test
        @DisplayName("setSala e getSala devem funcionar corretamente (RF11)")
        void setGetSala() {
            turma.setSala("Bloco C - 305");
            assertEquals("Bloco C - 305", turma.getSala());
        }

        @Test
        @DisplayName("setSala deve aceitar null")
        void setSalaNula() {
            turma.setSala("Bloco A - 101");
            turma.setSala(null);
            assertNull(turma.getSala());
        }

        @Test
        @DisplayName("setMatriculaProfessor e getMatriculaProfessor devem funcionar corretamente")
        void setGetMatriculaProfessor() {
            turma.setMatriculaProfessor("P0042");
            assertEquals("P0042", turma.getMatriculaProfessor());
        }

        @Test
        @DisplayName("setMatriculaProfessor deve aceitar null")
        void setMatriculaProfessorNulo() {
            turma.setMatriculaProfessor("P0001");
            turma.setMatriculaProfessor(null);
            assertNull(turma.getMatriculaProfessor());
        }
    }

    // =========================================================================
    // getChaveUnica()
    // =========================================================================

    @Nested
    @DisplayName("getChaveUnica()")
    class ChaveUnica {

        @Test
        @DisplayName("Deve retornar chave no formato disciplina_periodo_codigo")
        void deveRetornarChaveNoFormatoCorreto() {
            Turma turma = new Turma("T01", "MAT001", "2026.1", 40, "Seg 10h", "A-101", null);
            assertEquals("MAT001_2026.1_T01", turma.getChaveUnica());
        }

        @Test
        @DisplayName("Chave deve separar os três campos com underscore")
        void chaveDeveUsarUnderscoreComoSeparador() {
            Turma turma = new Turma("T02", "FIS001", "2025.2", 30, "Ter 14h", "B-202", null);
            String chave = turma.getChaveUnica();
            String[] partes = chave.split("_");
            assertEquals(3, partes.length);
            assertEquals("FIS001", partes[0]);
            assertEquals("2025.2", partes[1]);
            assertEquals("T02",    partes[2]);
        }

        @Test
        @DisplayName("Turmas com mesma disciplina e período mas códigos distintos devem ter chaves diferentes")
        void turmasComCodigosDiferentesDevemTerChavesDiferentes() {
            Turma t1 = new Turma("T01", "MAT001", "2026.1", 40, "Seg 10h", "A-101", null);
            Turma t2 = new Turma("T02", "MAT001", "2026.1", 30, "Qua 10h", "A-102", null);
            assertTrue(!t1.getChaveUnica().equals(t2.getChaveUnica()));
        }

        @Test
        @DisplayName("Turmas com mesma disciplina e código mas períodos distintos devem ter chaves diferentes")
        void turmasEmPeriodosDiferentesDevemTerChavesDiferentes() {
            Turma t1 = new Turma("T01", "MAT001", "2026.1", 40, "Seg 10h", "A-101", null);
            Turma t2 = new Turma("T01", "MAT001", "2026.2", 40, "Seg 10h", "A-101", null);
            assertTrue(!t1.getChaveUnica().equals(t2.getChaveUnica()));
        }

        @Test
        @DisplayName("Chave deve refletir atualização via setter")
        void chaveDeveReflitirAtualizacaoViaSetter() {
            Turma turma = new Turma("T01", "MAT001", "2026.1", 40, "Seg 10h", "A-101", null);
            turma.setCodigo("T99");
            assertTrue(turma.getChaveUnica().endsWith("_T99"));
        }
    }

    // =========================================================================
    // toString()
    // =========================================================================

    @Nested
    @DisplayName("toString()")
    class ToStringTests {

        @Test
        @DisplayName("toString com professor deve conter todos os campos")
        void toStringComProfessorDeveConterTodosOsCampos() {
            Turma turma = new Turma("T01", "MAT001", "2026.1", 40, "Seg/Qua 10h-12h", "Bloco A - 101", "P0001");
            String str = turma.toString();

            assertTrue(str.contains("T01"),              "deve conter código");
            assertTrue(str.contains("MAT001"),           "deve conter código da disciplina");
            assertTrue(str.contains("2026.1"),           "deve conter código do período");
            assertTrue(str.contains("40"),               "deve conter número de vagas");
            assertTrue(str.contains("Seg/Qua 10h-12h"), "deve conter horário");
            assertTrue(str.contains("Bloco A - 101"),   "deve conter sala");
            assertTrue(str.contains("P0001"),            "deve conter matrícula do professor");
        }

        @Test
        @DisplayName("toString sem professor (nulo) deve exibir 'sem professor'")
        void toStringComProfessorNuloDeveExibirSemProfessor() {
            Turma turma = new Turma("T01", "MAT001", "2026.1", 30, "Ter 14h", "Bloco B - 202", null);
            String str = turma.toString();
            assertTrue(str.contains("sem professor"));
        }

        @Test
        @DisplayName("toString sem professor (string vazia) deve exibir 'sem professor'")
        void toStringComProfessorVazioDeveExibirSemProfessor() {
            Turma turma = new Turma("T01", "MAT001", "2026.1", 30, "Ter 14h", "Bloco B - 202", "  ");
            String str = turma.toString();
            assertTrue(str.contains("sem professor"));
        }

        @Test
        @DisplayName("toString com sala nula deve exibir 'sem sala'")
        void toStringComSalaNulaDeveExibirSemSala() {
            Turma turma = new Turma("T01", "MAT001", "2026.1", 30, "Ter 14h", null, null);
            String str = turma.toString();
            assertTrue(str.contains("sem sala"));
        }

        @Test
        @DisplayName("toString com sala vazia deve exibir 'sem sala'")
        void toStringComSalaVaziaDeveExibirSemSala() {
            Turma turma = new Turma("T01", "MAT001", "2026.1", 30, "Ter 14h", "  ", null);
            String str = turma.toString();
            assertTrue(str.contains("sem sala"));
        }

        @Test
        @DisplayName("toString deve conter a tag TURMA")
        void toStringDeveConterTagTurma() {
            Turma turma = new Turma("T01", "MAT001", "2026.1", 40, "Seg 10h", "A-101", "P0001");
            assertTrue(turma.toString().contains("TURMA"));
        }

        @Test
        @DisplayName("toString deve conter 'Sala:' com o valor da sala")
        void toStringDeveConterLabelSala() {
            Turma turma = new Turma("T01", "MAT001", "2026.1", 40, "Seg 10h", "Bloco C - 305", "P0001");
            assertTrue(turma.toString().contains("Sala:"));
            assertTrue(turma.toString().contains("Bloco C - 305"));
        }
    }
}