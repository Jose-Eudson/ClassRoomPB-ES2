package com.classroompb.service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
    private Aluno aluno, outroAluno;
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
        outroAluno = new Aluno("A0005", "Aluno5", "aluno5@test.com", "123");
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

    @Nested
    class CalculoPercentualFrequencia {

        @Test
        @DisplayName("Deve calcular 100 porcento de frequência")
        void deveCalcular100PorCentoDeFrequencia() {

            List<RegistroFrequencia> registros = List.of(

                    new RegistroFrequencia("A0001", "ES2", "2026.1", "T01", LocalDate.now(), StatusFrequencia.PRESENTE,
                            "P0001"),

                    new RegistroFrequencia("A0001", "ES2", "2026.1", "T01", LocalDate.now().plusDays(1),
                            StatusFrequencia.PRESENTE, "P0001"));

            when(frequenciaRepository.listarPorAlunoETurma("A0001", "ES2", "2026.1", "T01")).thenReturn(registros);

            double percentual = service.calcularPercentualFrequencia("A0001", "ES2", "2026.1", "T01");

            assertEquals(100.0, percentual);

            verify(frequenciaRepository).listarPorAlunoETurma("A0001", "ES2", "2026.1", "T01");
        }
    }

    @Test
    @DisplayName("Deve calcular 75% de frequência")
    void deveCalcular75PorCentoDeFrequencia() {

        List<RegistroFrequencia> registros = List.of(
                new RegistroFrequencia(aluno.getMatricula(), "ES2", "2026.1", "T01", LocalDate.of(2026, 3, 2),
                        StatusFrequencia.PRESENTE, professor.getMatricula()),

                new RegistroFrequencia(aluno.getMatricula(), "ES2", "2026.1", "T01", LocalDate.of(2026, 3, 9),
                        StatusFrequencia.PRESENTE, professor.getMatricula()),

                new RegistroFrequencia(aluno.getMatricula(), "ES2", "2026.1", "T01", LocalDate.of(2026, 3, 16),
                        StatusFrequencia.PRESENTE, professor.getMatricula()),

                new RegistroFrequencia(aluno.getMatricula(), "ES2", "2026.1", "T01", LocalDate.of(2026, 3, 23),
                        StatusFrequencia.FALTA, professor.getMatricula()));

        when(frequenciaRepository.listarPorAlunoETurma(aluno.getMatricula(), "ES2", "2026.1", "T01"))
                .thenReturn(registros);

        double percentual = service.calcularPercentualFrequencia(aluno.getMatricula(), "ES2", "2026.1", "T01");

        assertEquals(75.0, percentual);

        verify(frequenciaRepository).listarPorAlunoETurma(aluno.getMatricula(), "ES2", "2026.1", "T01");
    }

    @Test
    @DisplayName("Deve ignorar registros de outras turmas")
    void deveIgnorarRegistrosDeOutrasTurmas() {

        List<RegistroFrequencia> registros = List.of(

                new RegistroFrequencia(aluno.getMatricula(), "ES2", "2026.1", "T01", LocalDate.of(2026, 3, 2),
                        StatusFrequencia.PRESENTE, professor.getMatricula()),

                new RegistroFrequencia(aluno.getMatricula(), "SO", "2026.1", "T03", LocalDate.of(2026, 3, 2),
                        StatusFrequencia.FALTA, professor.getMatricula()));

        when(frequenciaRepository.listarPorAlunoETurma(aluno.getMatricula(), "ES2", "2026.1", "T01"))
                .thenReturn(List.of(registros.get(0)));

        double percentual = service.calcularPercentualFrequencia(aluno.getMatricula(), "ES2", "2026.1", "T01");

        assertEquals(100.0, percentual);

        verify(frequenciaRepository).listarPorAlunoETurma(aluno.getMatricula(), "ES2", "2026.1", "T01");
    }

    @Test
    @DisplayName("Deve ignorar registros de outros alunos")
    void deveIgnorarRegistrosDeOutrosAlunos() {

        List<RegistroFrequencia> registros = List.of(

                new RegistroFrequencia(aluno.getMatricula(), "ES2", "2026.1", "T01", LocalDate.of(2026, 3, 2),
                        StatusFrequencia.PRESENTE, professor.getMatricula()),

                new RegistroFrequencia(outroAluno.getMatricula(), "ES2", "2026.1", "T01", LocalDate.of(2026, 3, 2),
                        StatusFrequencia.FALTA, professor.getMatricula()));

        when(frequenciaRepository.listarPorAlunoETurma(aluno.getMatricula(), "ES2", "2026.1", "T01"))
                .thenReturn(List.of(registros.get(0)));

        double percentual = service.calcularPercentualFrequencia(aluno.getMatricula(), "ES2", "2026.1", "T01");

        assertEquals(100.0, percentual);

        verify(frequenciaRepository).listarPorAlunoETurma(aluno.getMatricula(), "ES2", "2026.1", "T01");
    }

    @Test
    @DisplayName("Deve recalcular percentual quando professor corrigir frequência")
    void deveRecalcularPercentualQuandoProfessorCorrigirFrequencia() {

        List<RegistroFrequencia> antes = List.of(new RegistroFrequencia(aluno.getMatricula(), "ES2", "2026.1", "T01",
                LocalDate.of(2026, 3, 2), StatusFrequencia.FALTA, professor.getMatricula()));

        List<RegistroFrequencia> depois = List.of(new RegistroFrequencia(aluno.getMatricula(), "ES2", "2026.1", "T01",
                LocalDate.of(2026, 3, 2), StatusFrequencia.PRESENTE, professor.getMatricula()));

        when(frequenciaRepository.listarPorAlunoETurma(aluno.getMatricula(), "ES2", "2026.1", "T01")).thenReturn(antes)
                .thenReturn(depois);

        double percentual = service.calcularPercentualFrequencia(aluno.getMatricula(), "ES2", "2026.1", "T01");

        assertEquals(0.0, percentual);

        percentual = service.calcularPercentualFrequencia(aluno.getMatricula(), "ES2", "2026.1", "T01");

        assertEquals(100.0, percentual);

        verify(frequenciaRepository, times(2)).listarPorAlunoETurma(aluno.getMatricula(), "ES2", "2026.1", "T01");
    }

    // ===== obterAlertaFrequencia =====

    @Nested
    @DisplayName("Alerta de frequencia")
    class AlertaFrequencia {

        @Test
        @DisplayName("Deve retornar aviso quando frequencia esta entre 75 e 80 porcento exclusive")
        void deveRetornarAvisoEntreMinimoe80() {
            String msg = service.obterAlertaFrequencia(78.0);
            assertNotNull(msg);
            assertTrue(msg.contains("próxima do limite"));
        }

        @Test
        @DisplayName("Deve retornar atencao quando frequencia esta exatamente no limite minimo de 75 porcento")
        void deveRetornarAtencaoNoLimiteMinimo() {
            String msg = service.obterAlertaFrequencia(75.0);
            assertNotNull(msg);
            assertTrue(msg.contains("atingiu o limite mínimo"));
        }

        @Test
        @DisplayName("Deve retornar mensagem de reprovacao quando frequencia esta abaixo de 75 porcento")
        void deveRetornarReprovacaoAbaixoDoMinimo() {
            String msg = service.obterAlertaFrequencia(60.0);
            assertNotNull(msg);
            assertTrue(msg.contains("abaixo da frequência mínima"));
        }

        @Test
        @DisplayName("Deve retornar null quando frequencia esta acima de 80 porcento")
        void deveRetornarNullAcimaDoLimite() {
            assertNull(service.obterAlertaFrequencia(85.0));
        }

        @Test
        @DisplayName("Deve retornar null quando frequencia e 100 porcento")
        void deveRetornarNullCom100Porcento() {
            assertNull(service.obterAlertaFrequencia(100.0));
        }

        @Test
        @DisplayName("Deve retornar null exatamente para 80 porcento pois e o limite superior do aviso")
        void deveRetornarNullPara80Porcento() {
            // 80.0 não satisfaz "percentual <= 80.0 && percentual > 75.0" pois 80.0 == 80.0 → verdadeiro, mas != 75.0
            // Então cai no primeiro branch: percentual > 75 && <= 80 → retorna aviso
            String msg = service.obterAlertaFrequencia(80.0);
            assertNotNull(msg);
            assertTrue(msg.contains("próxima do limite"));
        }
    }

    // ===== obterFrequenciaAluno =====

    @Nested
    @DisplayName("Consulta de frequencia do aluno (RF29)")
    class ConsultaFrequenciaAluno {

        @Test
        @DisplayName("Deve retornar lista de registros quando aluno possui frequencia")
        void deveRetornarRegistros() throws Exception {
            List<RegistroFrequencia> registros = List.of(new RegistroFrequencia(aluno.getMatricula(), DISC, PER, TURMA,
                    DATA_AULA, StatusFrequencia.PRESENTE, professor.getMatricula()));
            when(frequenciaRepository.listarPorAlunoETurma(aluno.getMatricula(), DISC, PER, TURMA))
                    .thenReturn(registros);

            List<RegistroFrequencia> resultado = service.obterFrequenciaAluno(aluno.getMatricula(), DISC, PER, TURMA);

            assertEquals(1, resultado.size());
            assertEquals(StatusFrequencia.PRESENTE, resultado.get(0).getStatus());
        }

        @Test
        @DisplayName("Deve lancar excecao quando lista de registros esta vazia")
        void deveLancarExcecaoListaVazia() {
            when(frequenciaRepository.listarPorAlunoETurma(aluno.getMatricula(), DISC, PER, TURMA))
                    .thenReturn(Collections.emptyList());

            Exception ex = assertThrows(Exception.class,
                    () -> service.obterFrequenciaAluno(aluno.getMatricula(), DISC, PER, TURMA));
            assertTrue(ex.getMessage().contains("Nenhum registro"));
        }

        @Test
        @DisplayName("Deve lancar excecao quando matricula do aluno e nula")
        void deveLancarExcecaoMatriculaAlunaNula() {
            Exception ex = assertThrows(Exception.class, () -> service.obterFrequenciaAluno(null, DISC, PER, TURMA));
            assertTrue(ex.getMessage().contains("nao pode ser vazio"));
        }

        @Test
        @DisplayName("Deve lancar excecao quando codigo da disciplina e vazio")
        void deveLancarExcecaoDiscipVazia() {
            Exception ex = assertThrows(Exception.class,
                    () -> service.obterFrequenciaAluno(aluno.getMatricula(), "  ", PER, TURMA));
            assertTrue(ex.getMessage().contains("nao pode ser vazio"));
        }

        @Test
        @DisplayName("Deve lancar excecao quando codigo do periodo e nulo")
        void deveLancarExcecaoPeriodoNulo() {
            Exception ex = assertThrows(Exception.class,
                    () -> service.obterFrequenciaAluno(aluno.getMatricula(), DISC, null, TURMA));
            assertTrue(ex.getMessage().contains("nao pode ser vazio"));
        }

        @Test
        @DisplayName("Deve lancar excecao quando codigo da turma e string vazia")
        void deveLancarExcecaoTurmaVazia() {
            Exception ex = assertThrows(Exception.class,
                    () -> service.obterFrequenciaAluno(aluno.getMatricula(), DISC, PER, ""));
            assertTrue(ex.getMessage().contains("nao pode ser vazio"));
        }

        @Test
        @DisplayName("Deve fazer trim nos parametros antes de consultar o repositorio")
        void deveFazerTrimNosParametros() throws Exception {
            List<RegistroFrequencia> registros = List.of(new RegistroFrequencia(aluno.getMatricula(), DISC, PER, TURMA,
                    DATA_AULA, StatusFrequencia.FALTA, professor.getMatricula()));
            when(frequenciaRepository.listarPorAlunoETurma(aluno.getMatricula(), DISC, PER, TURMA))
                    .thenReturn(registros);

            List<RegistroFrequencia> resultado = service.obterFrequenciaAluno("  " + aluno.getMatricula() + "  ",
                    "  " + DISC + "  ", "  " + PER + "  ", "  " + TURMA + "  ");

            assertEquals(1, resultado.size());
            verify(frequenciaRepository).listarPorAlunoETurma(aluno.getMatricula(), DISC, PER, TURMA);
        }
    }

    // ===== listarFrequenciaDaAula - casos de erro =====

    @Nested
    @DisplayName("Listagem de frequencia da aula - validacoes")
    class ListagemFrequenciaAulaValidacoes {

        @Test
        @DisplayName("Deve lancar excecao quando professor e nulo ao listar aula")
        void deveLancarExcecaoProfessorNuloAoListar() {
            Exception ex = assertThrows(Exception.class,
                    () -> service.listarFrequenciaDaAula(null, DISC, PER, TURMA, DATA_AULA));
            assertTrue(ex.getMessage().contains("Apenas professores"));
        }

        @Test
        @DisplayName("Deve lancar excecao quando usuario nao e professor ao listar aula")
        void deveLancarExcecaoNaoProfessorAoListar() {
            Exception ex = assertThrows(Exception.class,
                    () -> service.listarFrequenciaDaAula(coordenador, DISC, PER, TURMA, DATA_AULA));
            assertTrue(ex.getMessage().contains("Apenas professores"));
        }

        @Test
        @DisplayName("Deve lancar excecao quando codigo da disciplina e nulo ao listar")
        void deveLancarExcecaoDiscipNulaAoListar() {
            Exception ex = assertThrows(Exception.class,
                    () -> service.listarFrequenciaDaAula(professor, null, PER, TURMA, DATA_AULA));
            assertTrue(ex.getMessage().contains("nao pode ser vazio"));
        }

        @Test
        @DisplayName("Deve lancar excecao quando codigo do periodo e vazio ao listar")
        void deveLancarExcecaoPeriodoVazioAoListar() {
            Exception ex = assertThrows(Exception.class,
                    () -> service.listarFrequenciaDaAula(professor, DISC, "", TURMA, DATA_AULA));
            assertTrue(ex.getMessage().contains("nao pode ser vazio"));
        }

        @Test
        @DisplayName("Deve lancar excecao quando codigo da turma e nulo ao listar")
        void deveLancarExcecaoTurmaNulaAoListar() {
            Exception ex = assertThrows(Exception.class,
                    () -> service.listarFrequenciaDaAula(professor, DISC, PER, null, DATA_AULA));
            assertTrue(ex.getMessage().contains("nao pode ser vazio"));
        }

        @Test
        @DisplayName("Deve lancar excecao quando data da aula e nula ao listar")
        void deveLancarExcecaoDataNulaAoListar() {
            Exception ex = assertThrows(Exception.class,
                    () -> service.listarFrequenciaDaAula(professor, DISC, PER, TURMA, null));
            assertTrue(ex.getMessage().contains("Data da aula"));
        }

        @Test
        @DisplayName("Deve lancar excecao quando turma nao e encontrada ao listar")
        void deveLancarExcecaoTurmaInexistenteAoListar() {
            when(turmaRepository.buscarPorChaveUnica(DISC, PER, TURMA)).thenReturn(null);

            Exception ex = assertThrows(Exception.class,
                    () -> service.listarFrequenciaDaAula(professor, DISC, PER, TURMA, DATA_AULA));
            assertTrue(ex.getMessage().contains("nao encontrada"));
        }

        @Test
        @DisplayName("Deve lancar excecao quando professor nao e responsavel pela turma ao listar")
        void deveLancarExcecaoProfessorNaoResponsavelAoListar() {
            when(turmaRepository.buscarPorChaveUnica(DISC, PER, TURMA)).thenReturn(turma);

            Exception ex = assertThrows(Exception.class,
                    () -> service.listarFrequenciaDaAula(outroProfessor, DISC, PER, TURMA, DATA_AULA));
            assertTrue(ex.getMessage().contains("professor responsavel"));
        }
    }

    // ===== calcularPercentualFrequencia - lista vazia =====

    @Test
    @DisplayName("Deve retornar zero quando nao ha registros de frequencia para o aluno")
    void deveRetornarZeroSemRegistros() {
        when(frequenciaRepository.listarPorAlunoETurma(aluno.getMatricula(), DISC, PER, TURMA))
                .thenReturn(Collections.emptyList());

        double percentual = service.calcularPercentualFrequencia(aluno.getMatricula(), DISC, PER, TURMA);

        assertEquals(0.0, percentual);
    }

    // ===== registrarFrequencia - campos obrigatorios adicionais =====

    @Nested
    @DisplayName("Validacao de campos obrigatorios no registro de frequencia")
    class ValidacaoCamposObrigatoriosRegistro {

        @Test
        @DisplayName("Deve lancar excecao quando matricula do aluno e nula no registro")
        void deveLancarExcecaoMatriculaAlunaNula() {
            Exception ex = assertThrows(Exception.class,
                    () -> service.registrarPresenca(professor, null, DISC, PER, TURMA, DATA_AULA));
            assertTrue(ex.getMessage().contains("nao pode ser vazio"));
        }

        @Test
        @DisplayName("Deve lancar excecao quando codigo da disciplina e vazio no registro")
        void deveLancarExcecaoDiscipVaziaNoRegistro() {
            Exception ex = assertThrows(Exception.class,
                    () -> service.registrarPresenca(professor, aluno.getMatricula(), "   ", PER, TURMA, DATA_AULA));
            assertTrue(ex.getMessage().contains("nao pode ser vazio"));
        }

        @Test
        @DisplayName("Deve lancar excecao quando codigo do periodo e nulo no registro")
        void deveLancarExcecaoPeriodoNuloNoRegistro() {
            Exception ex = assertThrows(Exception.class,
                    () -> service.registrarPresenca(professor, aluno.getMatricula(), DISC, null, TURMA, DATA_AULA));
            assertTrue(ex.getMessage().contains("nao pode ser vazio"));
        }

        @Test
        @DisplayName("Deve lancar excecao quando codigo da turma e vazio no registro")
        void deveLancarExcecaoTurmaVaziaNoRegistro() {
            Exception ex = assertThrows(Exception.class,
                    () -> service.registrarPresenca(professor, aluno.getMatricula(), DISC, PER, "", DATA_AULA));
            assertTrue(ex.getMessage().contains("nao pode ser vazio"));
        }

        @Test
        @DisplayName("Deve lancar excecao quando matricula nao existe no repositorio")
        void deveLancarExcecaoMatriculaInexistenteNoRepositorio() {
            when(turmaRepository.buscarPorChaveUnica(DISC, PER, TURMA)).thenReturn(turma);
            when(matriculaRepository.buscarPorChaveUnica(aluno.getMatricula(), DISC, PER, TURMA)).thenReturn(null);

            Exception ex = assertThrows(Exception.class,
                    () -> service.registrarPresenca(professor, aluno.getMatricula(), DISC, PER, TURMA, DATA_AULA));
            assertTrue(ex.getMessage().contains("matricula confirmada"));
        }
    }

    @Nested
    @DisplayName("Apoio ao menu do professor")
    class ApoioMenuProfessor {

        @Test
        @DisplayName("Deve listar apenas turmas do professor logado")
        void deveListarApenasTurmasDoProfessorLogado() throws Exception {
            Turma turmaOutroProfessor = new Turma("T02", "MAT002", PER, 30, "Ter 08h-10h", "B-202",
                    outroProfessor.getMatricula());
            when(turmaRepository.listarTodos()).thenReturn(Arrays.asList(turma, turmaOutroProfessor));

            List<Turma> resultado = service.listarTurmasDoProfessor(professor);

            assertEquals(1, resultado.size());
            assertEquals(TURMA, resultado.get(0).getCodigo());
            assertEquals(professor.getMatricula(), resultado.get(0).getMatriculaProfessor());
        }

        @Test
        @DisplayName("Deve listar apenas matriculas confirmadas da turma do professor")
        void deveListarApenasMatriculasConfirmadasDaTurmaDoProfessor() throws Exception {
            MatriculaTurma pendente = new MatriculaTurma(outroAluno.getMatricula(), DISC, PER, TURMA);
            pendente.setStatus(StatusMatricula.PENDENTE);
            when(turmaRepository.buscarPorChaveUnica(DISC, PER, TURMA)).thenReturn(turma);
            when(matriculaRepository.listarPorTurma(DISC, PER, TURMA))
                    .thenReturn(Arrays.asList(matriculaConfirmada, pendente));

            List<MatriculaTurma> resultado = service.listarMatriculasConfirmadasDaTurma(professor, DISC, PER, TURMA);

            assertEquals(1, resultado.size());
            assertEquals(aluno.getMatricula(), resultado.get(0).getMatriculaAluno());
            assertEquals(StatusMatricula.CONFIRMADA, resultado.get(0).getStatus());
        }

        @Test
        @DisplayName("Nao deve listar matriculas quando professor nao e responsavel pela turma")
        void naoDeveListarMatriculasQuandoProfessorNaoResponsavel() {
            when(turmaRepository.buscarPorChaveUnica(DISC, PER, TURMA)).thenReturn(turma);

            Exception ex = assertThrows(Exception.class,
                    () -> service.listarMatriculasConfirmadasDaTurma(outroProfessor, DISC, PER, TURMA));

            assertTrue(ex.getMessage().contains("professor responsavel"));
        }
    }

}
