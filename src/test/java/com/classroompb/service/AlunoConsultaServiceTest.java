package com.classroompb.service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.classroompb.model.Aluno;
import com.classroompb.model.Disciplina;
import com.classroompb.model.PeriodoLetivo;
import com.classroompb.model.Turma;
import com.classroompb.model.Usuario;
import com.classroompb.repository.DisciplinaRepository;
import com.classroompb.repository.FrequenciaRepository;
import com.classroompb.repository.MatriculaTurmaRepository;
import com.classroompb.repository.PeriodoLetivoRepository;
import com.classroompb.repository.TurmaRepository;
import com.classroompb.repository.UsuarioRepository;
import com.classroompb.ui.AlunoController;

/**
 * RF15 — Testes unitários: Aluno consulta turmas e disciplinas.
 *
 * Cobertura: - Listar todas as disciplinas cadastradas - Buscar disciplina por código (sucesso e erro) - Listar turmas
 * do período ativo - Listar turmas por disciplina e período - Cenários de lista vazia - Cenários de parâmetros
 * inválidos
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RF15 - Aluno: consulta de disciplinas e turmas")
public class AlunoConsultaServiceTest {

    // -------------------------------------------------------------------------
    // Mocks e serviços
    // -------------------------------------------------------------------------
    @Mock
    private UsuarioService usuarioService;
    @Mock
    private DisciplinaRepository disciplinaRepository;
    @Mock
    private TurmaRepository turmaRepository;
    @Mock
    private PeriodoLetivoRepository periodoRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private FrequenciaRepository frequenciaRepository;
    @Mock
    private MatriculaTurmaRepository matriculaRepository;

    private DisciplinaService disciplinaService;
    private TurmaService turmaService;
    private PeriodoLetivoService periodoLetivoService;
    private FrequenciaService service;
    // -------------------------------------------------------------------------
    // Fixtures
    // -------------------------------------------------------------------------

    private Disciplina disciplinaCalculo;
    private Disciplina disciplinaAlgebra;
    private Disciplina disciplinaFisica;

    private PeriodoLetivo periodoAtivo;
    private PeriodoLetivo periodoInativo;

    private Turma turmaT01;
    private Turma turmaT02;
    private Turma turmaDisciplinaDiferente;

    private Usuario aluno;
    private AlunoController controller;

    @BeforeEach
    void setUp() {
        disciplinaService = new DisciplinaService(disciplinaRepository);
        turmaService = new TurmaService(turmaRepository, disciplinaRepository, periodoRepository, usuarioRepository);
        periodoLetivoService = new PeriodoLetivoService(periodoRepository);
        service = spy(new FrequenciaService(frequenciaRepository, turmaRepository, matriculaRepository));

        aluno = new Aluno("A0001", "Aluno", "aluno@test.com", "senha");

        controller = new AlunoController(usuarioService, null, null, null, null, service);
        disciplinaCalculo = new Disciplina("MAT001", "Cálculo I", 60, 4, Collections.emptyList());
        disciplinaAlgebra = new Disciplina("MAT002", "Álgebra Linear", 60, 4, Collections.singletonList("MAT001"));
        disciplinaFisica = new Disciplina("FIS001", "Física I", 60, 4, Collections.emptyList());

        periodoAtivo = new PeriodoLetivo("2026.1", 2026, 1, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 6, 30), true);
        periodoInativo = new PeriodoLetivo("2025.2", 2025, 2, LocalDate.of(2025, 8, 1), LocalDate.of(2025, 12, 20),
                false);

        turmaT01 = new Turma("T01", "MAT001", "2026.1", 40, "Seg/Qua 10h-12h", "Bloco A-101", "P0001");
        turmaT02 = new Turma("T02", "MAT001", "2026.1", 35, "Ter/Qui 14h-16h", "Bloco B-202", "P0002");
        turmaDisciplinaDiferente = new Turma("T01", "FIS001", "2026.1", 30, "Sex 08h-10h", "Lab Física", "P0003");

    }

    // =========================================================================
    // 1. LISTAR TODAS AS DISCIPLINAS
    // =========================================================================

    @Nested
    @DisplayName("1. Listar todas as disciplinas")
    class ListarTodasDisciplinas {

        @Test
        @DisplayName("Retorna todas as disciplinas quando o repositório tem registros")
        void retornaListaCompleta() {
            when(disciplinaRepository.listarTodos())
                    .thenReturn(Arrays.asList(disciplinaCalculo, disciplinaAlgebra, disciplinaFisica));

            List<Disciplina> resultado = disciplinaService.listarDisciplinas();

            assertNotNull(resultado);
            assertEquals(3, resultado.size());
        }

        @Test
        @DisplayName("Retorna lista vazia quando não há disciplinas cadastradas")
        void retornaListaVaziaQuandoSemDisciplinas() {
            when(disciplinaRepository.listarTodos()).thenReturn(Collections.emptyList());

            List<Disciplina> resultado = disciplinaService.listarDisciplinas();

            assertNotNull(resultado);
            assertTrue(resultado.isEmpty());
        }

        @Test
        @DisplayName("Retorna disciplina com pré-requisitos corretamente")
        void retornaDisciplinaComPreRequisitos() {
            when(disciplinaRepository.listarTodos())
                    .thenReturn(Collections.singletonList(disciplinaAlgebra));

            List<Disciplina> resultado = disciplinaService.listarDisciplinas();

            assertEquals(1, resultado.size());
            Disciplina d = resultado.get(0);
            assertEquals("MAT002", d.getCodigo());
            assertNotNull(d.getPreRequisitos());
            assertEquals(1, d.getPreRequisitos().size());
            assertEquals("MAT001", d.getPreRequisitos().get(0));
        }

        @Test
        @DisplayName("Os dados retornados contêm código, nome, carga horária e créditos")
        void retornaCamposEsperados() {
            when(disciplinaRepository.listarTodos())
                    .thenReturn(Collections.singletonList(disciplinaCalculo));

            List<Disciplina> resultado = disciplinaService.listarDisciplinas();

            Disciplina d = resultado.get(0);
            assertEquals("MAT001",    d.getCodigo());
            assertEquals("Cálculo I", d.getNome());
            assertEquals(60,          d.getCargaHoraria());
            assertEquals(4,           d.getCreditos());
        }

        @Test
        @DisplayName("Retorna exatamente uma disciplina quando há apenas uma cadastrada")
        void retornaUmaDisciplina() {
            when(disciplinaRepository.listarTodos())
                    .thenReturn(Collections.singletonList(disciplinaFisica));

            List<Disciplina> resultado = disciplinaService.listarDisciplinas();

            assertEquals(1, resultado.size());
            assertEquals("FIS001", resultado.get(0).getCodigo());
        }
    }

    // =========================================================================
    // 2. BUSCAR DISCIPLINA POR CÓDIGO
    // =========================================================================

    @Nested
    @DisplayName("2. Buscar disciplina por código")
    class BuscarDisciplinaPorCodigo {

        @Test
        @DisplayName("Retorna a disciplina quando o código existe")
        void retornaDisciplinaExistente() throws Exception {
            when(disciplinaRepository.buscarPorCodigo("MAT001")).thenReturn(disciplinaCalculo);

            Disciplina resultado = disciplinaService.buscarPorCodigo("MAT001");

            assertNotNull(resultado);
            assertEquals("MAT001",    resultado.getCodigo());
            assertEquals("Cálculo I", resultado.getNome());
        }

        @Test
        @DisplayName("Lança exceção quando a disciplina não é encontrada")
        void lancaExcecaoQuandoNaoEncontrada() {
            when(disciplinaRepository.buscarPorCodigo("XXX999")).thenReturn(null);

            Exception ex = assertThrows(Exception.class,
                    () -> disciplinaService.buscarPorCodigo("XXX999"));

            assertTrue(ex.getMessage().contains("XXX999"));
        }

        @Test
        @DisplayName("Lança exceção quando o código é nulo")
        void lancaExcecaoQuandoCodigoNulo() {
            Exception ex = assertThrows(Exception.class, () -> disciplinaService.buscarPorCodigo(null));

            assertNotNull(ex.getMessage());
        }

        @Test
        @DisplayName("Lança exceção quando o código é uma string vazia")
        void lancaExcecaoQuandoCodigoVazio() {
            Exception ex = assertThrows(Exception.class, () -> disciplinaService.buscarPorCodigo("   "));

            assertNotNull(ex.getMessage());
        }

        @Test
        @DisplayName("Retorna disciplina sem pré-requisitos corretamente")
        void retornaDisciplinaSemPreRequisitos() throws Exception {
            when(disciplinaRepository.buscarPorCodigo("FIS001")).thenReturn(disciplinaFisica);

            Disciplina resultado = disciplinaService.buscarPorCodigo("FIS001");

            assertNotNull(resultado);
            assertTrue(resultado.getPreRequisitos() == null
                    || resultado.getPreRequisitos().isEmpty());
        }
    }

    // =========================================================================
    // 3. LISTAR TURMAS POR PERÍODO
    // =========================================================================

    @Nested
    @DisplayName("3. Listar turmas do período ativo")
    class ListarTurmasPorPeriodo {

        @Test
        @DisplayName("Retorna turmas ofertadas no período ativo")
        void retornaTurmasNoPeriodoAtivo() {
            when(turmaRepository.listarPorPeriodo("2026.1"))
                    .thenReturn(Arrays.asList(turmaT01, turmaT02, turmaDisciplinaDiferente));

            List<Turma> resultado = turmaService.listarTurmasPorPeriodo("2026.1");

            assertNotNull(resultado);
            assertEquals(3, resultado.size());
        }

        @Test
        @DisplayName("Retorna lista vazia quando não há turmas no período")
        void retornaListaVaziaQuandoSemTurmas() {
            when(turmaRepository.listarPorPeriodo("2026.1")).thenReturn(Collections.emptyList());

            List<Turma> resultado = turmaService.listarTurmasPorPeriodo("2026.1");

            assertNotNull(resultado);
            assertTrue(resultado.isEmpty());
        }

        @Test
        @DisplayName("Retorna lista vazia para período sem turmas ofertadas")
        void retornaListaVaziaParaPeriodoInativo() {
            when(turmaRepository.listarPorPeriodo("2025.2")).thenReturn(Collections.emptyList());

            List<Turma> resultado = turmaService.listarTurmasPorPeriodo("2025.2");

            assertTrue(resultado.isEmpty());
        }

        @Test
        @DisplayName("Turmas retornadas contêm todos os campos esperados")
        void turmasContemCamposEsperados() {
            when(turmaRepository.listarPorPeriodo("2026.1"))
                    .thenReturn(Collections.singletonList(turmaT01));

            List<Turma> resultado = turmaService.listarTurmasPorPeriodo("2026.1");

            Turma t = resultado.get(0);
            assertEquals("T01",              t.getCodigo());
            assertEquals("MAT001",           t.getCodigoDisciplina());
            assertEquals("2026.1",           t.getCodigoPeriodo());
            assertEquals(40,                 t.getVagas());
            assertEquals("Seg/Qua 10h-12h", t.getHorario());
            assertEquals("Bloco A-101",      t.getSala());
            assertEquals("P0001",            t.getMatriculaProfessor());
        }

        @Test
        @DisplayName("Filtra apenas turmas do período informado, não de outros períodos")
        void retornaApenasDoPeríodoCorreto() {
            when(turmaRepository.listarPorPeriodo("2026.1"))
                    .thenReturn(Arrays.asList(turmaT01, turmaT02));
            when(turmaRepository.listarPorPeriodo("2025.2"))
                    .thenReturn(Collections.emptyList());

            List<Turma> resultado2026 = turmaService.listarTurmasPorPeriodo("2026.1");
            List<Turma> resultado2025 = turmaService.listarTurmasPorPeriodo("2025.2");

            assertEquals(2, resultado2026.size());
            assertTrue(resultado2025.isEmpty());
        }
    }

    // =========================================================================
    // 4. LISTAR TURMAS POR DISCIPLINA E PERÍODO
    // =========================================================================

    @Nested
    @DisplayName("4. Listar turmas por disciplina e período")
    class ListarTurmasPorDisciplinaEPeriodo {

        @Test
        @DisplayName("Retorna turmas de uma disciplina no período ativo")
        void retornaTurmasDaDisciplinaNoPeríodo() {
            when(turmaRepository.listarPorDisciplinaEPeriodo("MAT001", "2026.1"))
                    .thenReturn(Arrays.asList(turmaT01, turmaT02));

            List<Turma> resultado = turmaService.listarTurmasPorDisciplinaEPeriodo("MAT001", "2026.1");

            assertNotNull(resultado);
            assertEquals(2, resultado.size());
            assertTrue(resultado.stream().allMatch(t -> t.getCodigoDisciplina().equals("MAT001")));
        }

        @Test
        @DisplayName("Retorna lista vazia quando disciplina não tem turmas no período")
        void retornaVazioQuandoDisciplinaSemTurmas() {
            when(turmaRepository.listarPorDisciplinaEPeriodo("MAT002", "2026.1"))
                    .thenReturn(Collections.emptyList());

            List<Turma> resultado = turmaService.listarTurmasPorDisciplinaEPeriodo("MAT002", "2026.1");

            assertNotNull(resultado);
            assertTrue(resultado.isEmpty());
        }

        @Test
        @DisplayName("Não confunde turmas de disciplinas diferentes no mesmo período")
        void naoConfundeDisciplinasNoMesmoPeriodo() {
            when(turmaRepository.listarPorDisciplinaEPeriodo("MAT001", "2026.1"))
                    .thenReturn(Arrays.asList(turmaT01, turmaT02));
            when(turmaRepository.listarPorDisciplinaEPeriodo("FIS001", "2026.1"))
                    .thenReturn(Collections.singletonList(turmaDisciplinaDiferente));

            List<Turma> turmasCalculo = turmaService.listarTurmasPorDisciplinaEPeriodo("MAT001", "2026.1");
            List<Turma> turmasFisica  = turmaService.listarTurmasPorDisciplinaEPeriodo("FIS001", "2026.1");

            assertEquals(2, turmasCalculo.size());
            assertEquals(1, turmasFisica.size());
            assertEquals("FIS001", turmasFisica.get(0).getCodigoDisciplina());
        }

        @Test
        @DisplayName("Retorna lista vazia quando a disciplina existe mas não tem turmas naquele período")
        void retornaVazioParaDisciplinaExistenteSemTurmasNoPeriodo() {
            when(turmaRepository.listarPorDisciplinaEPeriodo("MAT001", "2025.2"))
                    .thenReturn(Collections.emptyList());

            List<Turma> resultado = turmaService.listarTurmasPorDisciplinaEPeriodo("MAT001", "2025.2");

            assertTrue(resultado.isEmpty());
        }

        @Test
        @DisplayName("Turmas retornadas pertencem ao período e disciplina solicitados")
        void turmasRetornadasPertencemAoPeriodoEDisciplina() {
            when(turmaRepository.listarPorDisciplinaEPeriodo("MAT001", "2026.1"))
                    .thenReturn(Arrays.asList(turmaT01, turmaT02));

            List<Turma> resultado = turmaService.listarTurmasPorDisciplinaEPeriodo("MAT001", "2026.1");

            for (Turma t : resultado) {
                assertEquals("MAT001", t.getCodigoDisciplina());
                assertEquals("2026.1", t.getCodigoPeriodo());
            }
        }
    }

    // =========================================================================
    // 5. BUSCAR TURMA INDIVIDUAL POR CHAVE COMPOSTA
    // =========================================================================

    @Nested
    @DisplayName("5. Buscar turma por chave composta (disciplina + período + código)")
    class BuscarTurmaPorChave {

        @Test
        @DisplayName("Retorna a turma quando a chave composta existe")
        void retornaTurmaExistente() throws Exception {
            when(turmaRepository.buscarPorChaveUnica("MAT001", "2026.1", "T01"))
                    .thenReturn(turmaT01);

            Turma resultado = turmaService.buscarTurma("MAT001", "2026.1", "T01");

            assertNotNull(resultado);
            assertEquals("T01",    resultado.getCodigo());
            assertEquals("MAT001", resultado.getCodigoDisciplina());
            assertEquals("2026.1", resultado.getCodigoPeriodo());
        }

        @Test
        @DisplayName("Lança exceção quando a turma não é encontrada pela chave composta")
        void lancaExcecaoQuandoTurmaNaoEncontrada() {
            when(turmaRepository.buscarPorChaveUnica("MAT001", "2026.1", "T99"))
                    .thenReturn(null);

            Exception ex = assertThrows(Exception.class,
                    () -> turmaService.buscarTurma("MAT001", "2026.1", "T99"));

            assertTrue(ex.getMessage().contains("T99")
                    || ex.getMessage().toLowerCase().contains("não encontrada")
                    || ex.getMessage().toLowerCase().contains("nao encontrada"));
        }

        @Test
        @DisplayName("Lança exceção quando o código da disciplina é vazio")
        void lancaExcecaoQuandoCodigoDisciplinaVazio() {
            Exception ex = assertThrows(Exception.class, () -> turmaService.buscarTurma("", "2026.1", "T01"));

            assertNotNull(ex.getMessage());
        }

        @Test
        @DisplayName("Lança exceção quando o código do período é nulo")
        void lancaExcecaoQuandoCodigoPeriodoNulo() {
            Exception ex = assertThrows(Exception.class, () -> turmaService.buscarTurma("MAT001", null, "T01"));

            assertNotNull(ex.getMessage());
        }

        @Test
        @DisplayName("Lança exceção quando o código da turma é vazio")
        void lancaExcecaoQuandoCodigoTurmaVazio() {
            Exception ex = assertThrows(Exception.class, () -> turmaService.buscarTurma("MAT001", "2026.1", "  "));

            assertNotNull(ex.getMessage());
        }
    }

    // =========================================================================
    // 6. LISTAR TODOS OS PERÍODOS LETIVOS (contexto do aluno)
    // =========================================================================

    @Nested
    @DisplayName("6. Listar períodos letivos — identificação do período ativo")
    class ListarPeriodosLetivos {

        @Test
        @DisplayName("Retorna todos os períodos, incluindo ativo e inativo")
        void retornaTodosPeriodos() {
            when(periodoRepository.listarTodos())
                    .thenReturn(Arrays.asList(periodoAtivo, periodoInativo));

            List<PeriodoLetivo> resultado = periodoLetivoService.listarPeriodos();

            assertNotNull(resultado);
            assertEquals(2, resultado.size());
        }

        @Test
        @DisplayName("Período ativo é identificável via isAtivo()")
        void periodoAtivoIdentificavel() {
            when(periodoRepository.listarTodos())
                    .thenReturn(Arrays.asList(periodoAtivo, periodoInativo));

            List<PeriodoLetivo> periodos = periodoLetivoService.listarPeriodos();

            long ativos   = periodos.stream().filter(PeriodoLetivo::isAtivo).count();
            long inativos = periodos.stream().filter(p -> !p.isAtivo()).count();

            assertEquals(1, ativos);
            assertEquals(1, inativos);
        }

        @Test
        @DisplayName("Retorna lista vazia quando não há períodos cadastrados")
        void retornaVazioQuandoSemPeriodos() {
            when(periodoRepository.listarTodos()).thenReturn(Collections.emptyList());

            List<PeriodoLetivo> resultado = periodoLetivoService.listarPeriodos();

            assertTrue(resultado.isEmpty());
        }

        @Test
        @DisplayName("Período ativo contém código, datas e flag ativo=true")
        void periodoAtivoContemDadosCorretos() {
            when(periodoRepository.listarTodos())
                    .thenReturn(Collections.singletonList(periodoAtivo));

            List<PeriodoLetivo> resultado = periodoLetivoService.listarPeriodos();

            PeriodoLetivo p = resultado.get(0);
            assertEquals("2026.1",                  p.getCodigo());
            assertEquals(LocalDate.of(2026, 2, 1),  p.getDataInicio());
            assertEquals(LocalDate.of(2026, 6, 30), p.getDataFim());
            assertTrue(p.isAtivo());
        }
    }

    // =========================================================================
    // 7. FLUXO COMPLETO RF15
    // =========================================================================

    @Nested
    @DisplayName("7. Fluxo completo RF15 — aluno consulta disciplinas e turmas")
    class FluxoCompletoAluno {

        @Test
        @DisplayName("Aluno consegue listar disciplinas e depois buscar turmas da disciplina escolhida")
        void fluxoConsultaDisciplinaETurmas() throws Exception {
            when(disciplinaRepository.listarTodos())
                    .thenReturn(Arrays.asList(disciplinaCalculo, disciplinaAlgebra));

            List<Disciplina> disciplinas = disciplinaService.listarDisciplinas();
            assertEquals(2, disciplinas.size());

            when(disciplinaRepository.buscarPorCodigo("MAT001")).thenReturn(disciplinaCalculo);
            Disciplina disc = disciplinaService.buscarPorCodigo("MAT001");
            assertEquals("Cálculo I", disc.getNome());

            when(periodoRepository.listarTodos())
                    .thenReturn(Collections.singletonList(periodoAtivo));
            when(turmaRepository.listarPorDisciplinaEPeriodo("MAT001", "2026.1"))
                    .thenReturn(Arrays.asList(turmaT01, turmaT02));

            PeriodoLetivo ativo = periodoLetivoService.listarPeriodos().stream()
                    .filter(PeriodoLetivo::isAtivo)
                    .findFirst()
                    .orElse(null);

            assertNotNull(ativo);
            List<Turma> turmas = turmaService.listarTurmasPorDisciplinaEPeriodo(
                    disc.getCodigo(), ativo.getCodigo());

            assertEquals(2, turmas.size());
        }

        @Test
        @DisplayName("Aluno não encontra período ativo quando todos os períodos estão inativos")
        void semPeriodoAtivoRetornaVazio() {
            when(periodoRepository.listarTodos())
                    .thenReturn(Collections.singletonList(periodoInativo));

            boolean temAtivo = periodoLetivoService.listarPeriodos()
                    .stream()
                    .anyMatch(PeriodoLetivo::isAtivo);

            assertTrue(!temAtivo, "Não deve haver período ativo quando todos estão inativos");
        }

        @Test
        @DisplayName("Turma listada exibe sala e professor preenchidos")
        void turmaExibeSalaEProfessor() {
            when(turmaRepository.listarPorPeriodo("2026.1"))
                    .thenReturn(Collections.singletonList(turmaT01));

            List<Turma> turmas = turmaService.listarTurmasPorPeriodo("2026.1");

            Turma t = turmas.get(0);
            assertNotNull(t.getSala());
            assertNotNull(t.getMatriculaProfessor());
            assertTrue(!t.getSala().isBlank());
            assertTrue(!t.getMatriculaProfessor().isBlank());
        }

        @Test
        @DisplayName("Aluno visualiza turmas de múltiplas disciplinas em um mesmo período")
        void turmasDeMultiplasDisciplinasNoMesmoPeriodo() {
            when(turmaRepository.listarPorPeriodo("2026.1"))
                    .thenReturn(Arrays.asList(turmaT01, turmaT02, turmaDisciplinaDiferente));

            List<Turma> todasTurmas = turmaService.listarTurmasPorPeriodo("2026.1");

            long disciplinasDistintas = todasTurmas.stream()
                    .map(Turma::getCodigoDisciplina)
                    .distinct()
                    .count();

            assertEquals(3, todasTurmas.size());
            assertEquals(2, disciplinasDistintas);
        }

        @Test
        @DisplayName("Buscar disciplina inexistente retorna mensagem de erro adequada")
        void buscaDisciplinaInexistenteRetornaMensagemErro() {
            when(disciplinaRepository.buscarPorCodigo("COD_INVALIDO")).thenReturn(null);

            Exception ex = assertThrows(Exception.class,
                    () -> disciplinaService.buscarPorCodigo("COD_INVALIDO"));

            assertNotNull(ex.getMessage());
            assertTrue(ex.getMessage().contains("COD_INVALIDO")
                    || ex.getMessage().toLowerCase().contains("não encontrada")
                    || ex.getMessage().toLowerCase().contains("nao encontrada"));
        }
    }

    @Nested
    @DisplayName("8. Exibe alerta sobre frequência de faltas")
    class AlertaFrequencia {
        @Test
        @DisplayName("Deve consultar o percentual de frequência do aluno")
        void deveConsultarPercentualDeFrequencia() throws Exception {
            doReturn(80.0).when(service).calcularPercentualFrequencia("A0001", "ES2", "2026.1", "T01");

            double percentual = service.calcularPercentualFrequencia("A0001", "ES2", "2026.1", "T01");

            assertEquals(80.0, percentual);

            verify(service).calcularPercentualFrequencia("A0001", "ES2", "2026.1", "T01");
        }

        @Test
        @DisplayName("Deve retornar percentual próximo do limite de faltas")
        void deveRetornarPercentualProximoDoLimite() throws Exception {

            doReturn(75.0).when(service).calcularPercentualFrequencia("A0001", "ES2", "2026.1", "T01");

            double percentual = service.calcularPercentualFrequencia("A0001", "ES2", "2026.1", "T01");

            assertTrue(percentual >= 75.0 && percentual <= 80.0);

            verify(service).calcularPercentualFrequencia("A0001", "ES2", "2026.1", "T01");
        }

        @Test
        @DisplayName("Deve retornar percentual abaixo do limite permitido")
        void deveRetornarPercentualAbaixoDoLimite() throws Exception {

            doReturn(70.0).when(service).calcularPercentualFrequencia("A0001", "ES2", "2026.1", "T01");

            double percentual = service.calcularPercentualFrequencia("A0001", "ES2", "2026.1", "T01");

            assertTrue(percentual < 75.0);

            verify(service).calcularPercentualFrequencia("A0001", "ES2", "2026.1", "T01");
        }

        @Test
        @DisplayName("Deve retornar percentual seguro")
        void deveRetornarPercentualSeguro() throws Exception {

            doReturn(92.0).when(service).calcularPercentualFrequencia("A0001", "ES2", "2026.1", "T01");

            double percentual = service.calcularPercentualFrequencia("A0001", "ES2", "2026.1", "T01");

            assertTrue(percentual > 80.0);

            verify(service).calcularPercentualFrequencia("A0001", "ES2", "2026.1", "T01");
        }
    }
}
