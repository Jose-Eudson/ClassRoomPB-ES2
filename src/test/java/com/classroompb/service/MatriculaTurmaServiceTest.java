package com.classroompb.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.times;

import com.classroompb.model.Administrador;
import com.classroompb.model.Aluno;
import com.classroompb.model.Coordenador;
import com.classroompb.model.Disciplina;
import com.classroompb.model.MatriculaTurma;
import com.classroompb.model.PeriodoLetivo;
import com.classroompb.model.Professor;
import com.classroompb.model.StatusMatricula;
import com.classroompb.model.Turma;
import com.classroompb.repository.DisciplinaRepository;
import com.classroompb.repository.HistoricoRepository;
import com.classroompb.repository.MatriculaTurmaRepository;
import com.classroompb.repository.PeriodoLetivoRepository;
import com.classroompb.repository.TurmaRepository;

/**
 * RF16/RF20/RF21 — Testes unitários: matrícula em turma.
 *
 * RF16: Aluno solicita matrícula em turma.
 * RF20: Matrícula confirmada automaticamente quando houver vaga e critérios
 * atendidos.
 * RF21: Caso não haja vaga, o aluno deve entrar em lista de espera.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RF16/RF20/RF21 - Matrícula em turma")
public class MatriculaTurmaServiceTest {

        @Mock
        private MatriculaTurmaRepository matriculaRepository;

        @Mock
        private TurmaRepository turmaRepository;

        @Mock
        private PeriodoLetivoRepository periodoRepository;

        @Mock
        private DisciplinaRepository disciplinaRepository;

        @Mock
        private HistoricoRepository historicoRepository;

        private MatriculaTurmaService service;

        private Aluno aluno;
        private Aluno outroAluno;
        private Coordenador coordenador;
        private Professor professor;
        private Administrador administrador;

        private Turma turma;
        private PeriodoLetivo periodoAtivo;
        private PeriodoLetivo periodoInativo;

        private static final String DISC = "MAT001";
        private static final String PER = "2026.1";
        private static final String TURMA = "T01";

        @BeforeEach
        void setUp() {
                service = new MatriculaTurmaService(
                                matriculaRepository,
                                turmaRepository,
                                periodoRepository,
                                disciplinaRepository,
                                historicoRepository);

                aluno = new Aluno("A0001", "João Silva", "joao@test.com", "senha123");
                outroAluno = new Aluno("A0002", "Maria Souza", "maria@test.com", "senha456");
                coordenador = new Coordenador("C0001", "Coord", "coord@test.com", "senha123");
                professor = new Professor("P0001", "Prof", "prof@test.com", "senha123");
                administrador = new Administrador("ADM001", "Admin", "adm@test.com", "senha123");

                turma = new Turma(
                                TURMA,
                                DISC,
                                PER,
                                40,
                                "Seg/Qua 10h-12h",
                                "Bloco A-101",
                                "P0001");

                periodoAtivo = new PeriodoLetivo(
                                PER,
                                2026,
                                1,
                                LocalDate.of(2026, 2, 1),
                                LocalDate.of(2026, 6, 30),
                                true);

                periodoInativo = new PeriodoLetivo(
                                "2025.2",
                                2025,
                                2,
                                LocalDate.of(2025, 8, 1),
                                LocalDate.of(2025, 12, 20),
                                false);
        }

        // =========================================================================
        // Helpers
        // =========================================================================

        private void mockTurmaExiste() {
                when(turmaRepository.buscarPorChaveUnica(DISC, PER, TURMA))
                                .thenReturn(turma);
        }

        private void mockPeriodoAtivo() {
                when(periodoRepository.buscarPorCodigo(PER))
                                .thenReturn(periodoAtivo);
        }

        private void mockSemSolicitacaoAtiva() {
                when(matriculaRepository.existeSolicitacaoAtiva("A0001", DISC, PER, TURMA))
                                .thenReturn(false);
        }

        private void mockVagasOcupadas(long ocupadas) {
                when(matriculaRepository.contarOcupadasPorTurma(DISC, PER, TURMA))
                                .thenReturn(ocupadas);
        }

        private void mockDisciplinaSemPreRequisitos() {
                when(disciplinaRepository.buscarPorCodigo(DISC))
                                .thenReturn(new Disciplina(
                                                DISC,
                                                "Cálculo I",
                                                60,
                                                4,
                                                Collections.emptyList()));
        }

        private void mockSemChoqueDeHorario() {
                when(matriculaRepository.listarPorAluno("A0001"))
                                .thenReturn(Collections.emptyList());
        }

        private void mockCenarioSucesso() {
                mockTurmaExiste();
                mockPeriodoAtivo();
                mockSemSolicitacaoAtiva();
                mockVagasOcupadas(0L);
                mockDisciplinaSemPreRequisitos();
                mockSemChoqueDeHorario();
        }

        // =========================================================================
        // 1. SOLICITAR MATRÍCULA — SUCESSO
        // =========================================================================

        @Nested
        @DisplayName("1. Solicitar matrícula — sucesso")
        class SolicitarMatriculaSucesso {

                @Test
                @DisplayName("1.1 Deve registrar matrícula com status CONFIRMADA quando houver vaga")
                void deveSalvarMatriculaComStatusConfirmada() {
                        mockCenarioSucesso();

                        assertDoesNotThrow(() -> service.solicitarMatricula(aluno, DISC, PER, TURMA));

                        ArgumentCaptor<MatriculaTurma> captor = ArgumentCaptor.forClass(MatriculaTurma.class);

                        verify(matriculaRepository).salvar(captor.capture());

                        assertEquals(
                                        StatusMatricula.CONFIRMADA,
                                        captor.getValue().getStatus(),
                                        "Quando há vaga, a matrícula deve ser confirmada automaticamente");
                }

                @Test
                @DisplayName("1.2 O objeto salvo deve conter os dados corretos do aluno e da turma")
                void objetoSalvoDeveConterDadosCorretos() throws Exception {
                        mockCenarioSucesso();

                        service.solicitarMatricula(aluno, DISC, PER, TURMA);

                        ArgumentCaptor<MatriculaTurma> captor = ArgumentCaptor.forClass(MatriculaTurma.class);

                        verify(matriculaRepository).salvar(captor.capture());

                        MatriculaTurma salva = captor.getValue();

                        assertNotNull(salva);
                        assertEquals("A0001", salva.getMatriculaAluno());
                        assertEquals(DISC, salva.getCodigoDisciplina());
                        assertEquals(PER, salva.getCodigoPeriodo());
                        assertEquals(TURMA, salva.getCodigoTurma());
                        assertEquals(StatusMatricula.CONFIRMADA, salva.getStatus());
                }

                @Test
                @DisplayName("1.3 Data da solicitação deve ser preenchida automaticamente")
                void dataSolicitacaoDeveSerPreenchida() throws Exception {
                        mockCenarioSucesso();

                        LocalDateTime antes = LocalDateTime.now().minusSeconds(1);

                        service.solicitarMatricula(aluno, DISC, PER, TURMA);

                        ArgumentCaptor<MatriculaTurma> captor = ArgumentCaptor.forClass(MatriculaTurma.class);

                        verify(matriculaRepository).salvar(captor.capture());

                        LocalDateTime dataSolicitacao = captor.getValue().getDataSolicitacao();

                        assertNotNull(dataSolicitacao);
                        assertFalse(dataSolicitacao.isBefore(antes));
                }

                @Test
                @DisplayName("1.4 Deve aceitar matrícula quando resta exatamente uma vaga")
                void deveAceitarComExatamenteUmaVagaRestante() {
                        mockTurmaExiste();
                        mockPeriodoAtivo();
                        mockSemSolicitacaoAtiva();
                        mockVagasOcupadas(39L);
                        mockDisciplinaSemPreRequisitos();
                        mockSemChoqueDeHorario();

                        assertDoesNotThrow(() -> service.solicitarMatricula(aluno, DISC, PER, TURMA));

                        verify(matriculaRepository).salvar(any(MatriculaTurma.class));
                }

                @Test
                @DisplayName("1.5 Entradas com espaços extras devem ser normalizadas")
                void deveNormalizarEspacosNaEntrada() throws Exception {
                        mockCenarioSucesso();

                        service.solicitarMatricula(
                                        aluno,
                                        "  " + DISC + "  ",
                                        "  " + PER + "  ",
                                        "  " + TURMA + "  ");

                        ArgumentCaptor<MatriculaTurma> captor = ArgumentCaptor.forClass(MatriculaTurma.class);

                        verify(matriculaRepository).salvar(captor.capture());

                        MatriculaTurma salva = captor.getValue();

                        assertEquals(DISC, salva.getCodigoDisciplina());
                        assertEquals(PER, salva.getCodigoPeriodo());
                        assertEquals(TURMA, salva.getCodigoTurma());
                }

                @Test
                @DisplayName("1.6 Alunos diferentes podem solicitar matrícula na mesma turma")
                void alunosDiferentesPodemSolicitarMesmaTurma() {
                        mockCenarioSucesso();

                        assertDoesNotThrow(() -> service.solicitarMatricula(aluno, DISC, PER, TURMA));

                        when(matriculaRepository.existeSolicitacaoAtiva("A0002", DISC, PER, TURMA))
                                        .thenReturn(false);

                        when(matriculaRepository.listarPorAluno("A0002"))
                                        .thenReturn(Collections.emptyList());

                        assertDoesNotThrow(() -> service.solicitarMatricula(outroAluno, DISC, PER, TURMA));
                }

                @Test
                @DisplayName("1.7 Aluno pode solicitar matrícula em disciplinas diferentes no mesmo período")
                void podeMatricularEmDisciplinasDiferentes() {
                        Turma turmaFis = new Turma(
                                        "T01",
                                        "FIS001",
                                        PER,
                                        30,
                                        "Ter/Qui 14h-16h",
                                        "Lab Fis",
                                        "P0002");

                        mockCenarioSucesso();

                        assertDoesNotThrow(() -> service.solicitarMatricula(aluno, DISC, PER, TURMA));

                        when(turmaRepository.buscarPorChaveUnica("FIS001", PER, "T01"))
                                        .thenReturn(turmaFis);

                        when(periodoRepository.buscarPorCodigo(PER))
                                        .thenReturn(periodoAtivo);

                        when(matriculaRepository.existeSolicitacaoAtiva("A0001", "FIS001", PER, "T01"))
                                        .thenReturn(false);

                        when(matriculaRepository.contarOcupadasPorTurma("FIS001", PER, "T01"))
                                        .thenReturn(0L);

                        when(disciplinaRepository.buscarPorCodigo("FIS001"))
                                        .thenReturn(new Disciplina(
                                                        "FIS001",
                                                        "Física I",
                                                        60,
                                                        4,
                                                        Collections.emptyList()));

                        when(matriculaRepository.listarPorAluno("A0001"))
                                        .thenReturn(Collections.emptyList());

                        assertDoesNotThrow(() -> service.solicitarMatricula(aluno, "FIS001", PER, "T01"));
                }
        }

        // =========================================================================
        // 2. CONTROLE DE PERMISSÃO
        // =========================================================================

        @Nested
        @DisplayName("2. Controle de permissão")
        class ControlePermissao {

                @Test
                @DisplayName("2.1 Coordenador não pode solicitar matrícula")
                void coordenadorNaoPodeSolicitar() {
                        Exception ex = assertThrows(Exception.class,
                                        () -> service.solicitarMatricula(coordenador, DISC, PER, TURMA));

                        assertTrue(ex.getMessage().contains("Apenas alunos"));
                        verify(matriculaRepository, never()).salvar(any());
                }

                @Test
                @DisplayName("2.2 Professor não pode solicitar matrícula")
                void professorNaoPodeSolicitar() {
                        Exception ex = assertThrows(Exception.class,
                                        () -> service.solicitarMatricula(professor, DISC, PER, TURMA));

                        assertTrue(ex.getMessage().contains("Apenas alunos"));
                        verify(matriculaRepository, never()).salvar(any());
                }

                @Test
                @DisplayName("2.3 Administrador não pode solicitar matrícula")
                void adminNaoPodeSolicitar() {
                        Exception ex = assertThrows(Exception.class,
                                        () -> service.solicitarMatricula(administrador, DISC, PER, TURMA));

                        assertTrue(ex.getMessage().contains("Apenas alunos"));
                        verify(matriculaRepository, never()).salvar(any());
                }

                @Test
                @DisplayName("2.4 Usuário null deve lançar exceção")
                void usuarioNuloDeveLancarExcecao() {
                        Exception ex = assertThrows(Exception.class,
                                        () -> service.solicitarMatricula(null, DISC, PER, TURMA));

                        assertTrue(ex.getMessage().contains("Apenas alunos"));
                        verify(turmaRepository, never()).buscarPorChaveUnica(any(), any(), any());
                        verify(matriculaRepository, never()).salvar(any());
                }
        }

        // =========================================================================
        // 3. VALIDAÇÃO DE CAMPOS
        // =========================================================================

        @Nested
        @DisplayName("3. Validação de campos obrigatórios")
        class ValidacaoCampos {

                @Test
                @DisplayName("3.1 Código de disciplina null deve lançar exceção")
                void disciplinaNulaDeveLancar() {
                        Exception ex = assertThrows(Exception.class,
                                        () -> service.solicitarMatricula(aluno, null, PER, TURMA));

                        assertTrue(ex.getMessage().toLowerCase().contains("disciplina"));
                        verify(matriculaRepository, never()).salvar(any());
                }

                @Test
                @DisplayName("3.2 Código de disciplina vazio deve lançar exceção")
                void disciplinaVaziaDeveLancar() {
                        Exception ex = assertThrows(Exception.class,
                                        () -> service.solicitarMatricula(aluno, "", PER, TURMA));

                        assertTrue(ex.getMessage().toLowerCase().contains("disciplina"));
                        verify(matriculaRepository, never()).salvar(any());
                }

                @Test
                @DisplayName("3.3 Código de disciplina só com espaços deve lançar exceção")
                void disciplinaSoEspacosDeveLancar() {
                        Exception ex = assertThrows(Exception.class,
                                        () -> service.solicitarMatricula(aluno, "   ", PER, TURMA));

                        assertTrue(ex.getMessage().toLowerCase().contains("disciplina"));
                        verify(matriculaRepository, never()).salvar(any());
                }

                @Test
                @DisplayName("3.4 Código de período null deve lançar exceção")
                void periodoNuloDeveLancar() {
                        Exception ex = assertThrows(Exception.class,
                                        () -> service.solicitarMatricula(aluno, DISC, null, TURMA));

                        assertTrue(ex.getMessage().toLowerCase().contains("período"));
                        verify(matriculaRepository, never()).salvar(any());
                }

                @Test
                @DisplayName("3.5 Código de período vazio deve lançar exceção")
                void periodoVazioDeveLancar() {
                        Exception ex = assertThrows(Exception.class,
                                        () -> service.solicitarMatricula(aluno, DISC, "", TURMA));

                        assertTrue(ex.getMessage().toLowerCase().contains("período"));
                        verify(matriculaRepository, never()).salvar(any());
                }

                @Test
                @DisplayName("3.6 Código de período só com espaços deve lançar exceção")
                void periodoSoEspacosDeveLancar() {
                        Exception ex = assertThrows(Exception.class,
                                        () -> service.solicitarMatricula(aluno, DISC, "   ", TURMA));

                        assertTrue(ex.getMessage().toLowerCase().contains("período"));
                        verify(matriculaRepository, never()).salvar(any());
                }

                @Test
                @DisplayName("3.7 Código de turma null deve lançar exceção")
                void turmaNulaDeveLancar() {
                        Exception ex = assertThrows(Exception.class,
                                        () -> service.solicitarMatricula(aluno, DISC, PER, null));

                        assertTrue(ex.getMessage().toLowerCase().contains("turma"));
                        verify(matriculaRepository, never()).salvar(any());
                }

                @Test
                @DisplayName("3.8 Código de turma vazio deve lançar exceção")
                void turmaVaziaDeveLancar() {
                        Exception ex = assertThrows(Exception.class,
                                        () -> service.solicitarMatricula(aluno, DISC, PER, ""));

                        assertTrue(ex.getMessage().toLowerCase().contains("turma"));
                        verify(matriculaRepository, never()).salvar(any());
                }

                @Test
                @DisplayName("3.9 Código de turma só com espaços deve lançar exceção")
                void turmaSoEspacosDeveLancar() {
                        Exception ex = assertThrows(Exception.class,
                                        () -> service.solicitarMatricula(aluno, DISC, PER, "   "));

                        assertTrue(ex.getMessage().toLowerCase().contains("turma"));
                        verify(matriculaRepository, never()).salvar(any());
                }

                @Test
                @DisplayName("3.10 Validação ocorre antes de consultar repositórios")
                void validacaoCamposOcorreAntesDosRepositorios() {
                        assertThrows(Exception.class, () -> service.solicitarMatricula(aluno, null, PER, TURMA));

                        verify(turmaRepository, never()).buscarPorChaveUnica(any(), any(), any());
                        verify(periodoRepository, never()).buscarPorCodigo(any());
                }
        }

        // =========================================================================
        // 4. REGRAS DE NEGÓCIO
        // =========================================================================

        @Nested
        @DisplayName("4. Regras de negócio")
        class RegrasNegocio {

                @Test
                @DisplayName("4.1 Turma inexistente deve lançar exceção")
                void turmaInexistenteDeveLancar() {
                        when(turmaRepository.buscarPorChaveUnica(DISC, PER, "T99"))
                                        .thenReturn(null);

                        Exception ex = assertThrows(Exception.class,
                                        () -> service.solicitarMatricula(aluno, DISC, PER, "T99"));

                        assertTrue(ex.getMessage().contains("não encontrada"));
                        verify(matriculaRepository, never()).salvar(any());
                }

                @Test
                @DisplayName("4.2 Período letivo inativo deve impedir matrícula")
                void periodoInativoDeveImpedir() {
                        Turma turmaPeriodoInativo = new Turma(
                                        "T01",
                                        DISC,
                                        "2025.2",
                                        40,
                                        "Seg 10h",
                                        "A101",
                                        "P0001");

                        when(turmaRepository.buscarPorChaveUnica(DISC, "2025.2", "T01"))
                                        .thenReturn(turmaPeriodoInativo);

                        when(periodoRepository.buscarPorCodigo("2025.2"))
                                        .thenReturn(periodoInativo);

                        Exception ex = assertThrows(Exception.class,
                                        () -> service.solicitarMatricula(aluno, DISC, "2025.2", "T01"));

                        assertTrue(ex.getMessage().contains("inativo"));
                        verify(matriculaRepository, never()).salvar(any());
                }

                @Test
                @DisplayName("4.3 Período inexistente deve impedir matrícula")
                void periodoNuloNoRepositorioDeveImpedir() {
                        Turma turmaSemPeriodo = new Turma(
                                        "T01",
                                        DISC,
                                        "2099.1",
                                        40,
                                        "Seg 10h",
                                        "A101",
                                        "P0001");

                        when(turmaRepository.buscarPorChaveUnica(DISC, "2099.1", "T01"))
                                        .thenReturn(turmaSemPeriodo);

                        when(periodoRepository.buscarPorCodigo("2099.1"))
                                        .thenReturn(null);

                        Exception ex = assertThrows(Exception.class,
                                        () -> service.solicitarMatricula(aluno, DISC, "2099.1", "T01"));

                        assertNotNull(ex.getMessage());
                        verify(matriculaRepository, never()).salvar(any());
                }

                @Test
                @DisplayName("4.4 Solicitação duplicada deve lançar exceção")
                void solicitacaoDuplicadaDeveLancar() {
                        mockTurmaExiste();
                        mockPeriodoAtivo();

                        when(matriculaRepository.existeSolicitacaoAtiva("A0001", DISC, PER, TURMA))
                                        .thenReturn(true);

                        Exception ex = assertThrows(Exception.class,
                                        () -> service.solicitarMatricula(aluno, DISC, PER, TURMA));

                        assertTrue(ex.getMessage().contains("já possui uma solicitação ativa"));
                        verify(matriculaRepository, never()).salvar(any());
                }

                @Test
                @DisplayName("4.5 Disciplina inexistente deve lançar exceção")
                void disciplinaInexistenteDeveLancar() {
                        mockTurmaExiste();
                        mockPeriodoAtivo();
                        mockSemSolicitacaoAtiva();

                        when(disciplinaRepository.buscarPorCodigo(DISC))
                                        .thenReturn(null);

                        Exception ex = assertThrows(Exception.class,
                                        () -> service.solicitarMatricula(aluno, DISC, PER, TURMA));

                        assertTrue(ex.getMessage().contains("Disciplina"));
                        verify(matriculaRepository, never()).salvar(any());
                }
        }

        // =========================================================================
        // 5. RF21 — LISTA DE ESPERA
        // =========================================================================

        @Nested
        @DisplayName("5. RF21 - Lista de espera")
        class ListaDeEspera {

                @Test
                @DisplayName("5.1 Deve adicionar aluno à lista de espera quando não houver vaga")
                void deveAdicionarAlunoListaEsperaQuandoNaoHouverVaga() {
                        mockTurmaExiste();
                        mockPeriodoAtivo();
                        mockSemSolicitacaoAtiva();
                        mockVagasOcupadas(40L);
                        mockDisciplinaSemPreRequisitos();
                        mockSemChoqueDeHorario();

                        assertDoesNotThrow(() -> service.solicitarMatricula(aluno, DISC, PER, TURMA));

                        ArgumentCaptor<MatriculaTurma> captor = ArgumentCaptor.forClass(MatriculaTurma.class);

                        verify(matriculaRepository).salvar(captor.capture());

                        MatriculaTurma salva = captor.getValue();

                        assertEquals(StatusMatricula.LISTA_ESPERA, salva.getStatus());
                        assertEquals("A0001", salva.getMatriculaAluno());
                        assertEquals(DISC, salva.getCodigoDisciplina());
                        assertEquals(PER, salva.getCodigoPeriodo());
                        assertEquals(TURMA, salva.getCodigoTurma());
                }

                @Test
                @DisplayName("5.2 Excedente de ocupadas também deve adicionar aluno à lista de espera")
                void excessoDeOcupadasTambemDeveAdicionarListaEspera() {
                        mockTurmaExiste();
                        mockPeriodoAtivo();
                        mockSemSolicitacaoAtiva();
                        mockVagasOcupadas(50L);
                        mockDisciplinaSemPreRequisitos();
                        mockSemChoqueDeHorario();

                        assertDoesNotThrow(() -> service.solicitarMatricula(aluno, DISC, PER, TURMA));

                        ArgumentCaptor<MatriculaTurma> captor = ArgumentCaptor.forClass(MatriculaTurma.class);

                        verify(matriculaRepository).salvar(captor.capture());

                        assertEquals(StatusMatricula.LISTA_ESPERA, captor.getValue().getStatus());
                }

                @Test
                @DisplayName("5.3 Aluno em lista de espera deve manter os dados corretos")
                void alunoEmListaEsperaDeveManterDadosCorretos() {
                        mockTurmaExiste();
                        mockPeriodoAtivo();
                        mockSemSolicitacaoAtiva();
                        mockVagasOcupadas(40L);
                        mockDisciplinaSemPreRequisitos();
                        mockSemChoqueDeHorario();

                        assertDoesNotThrow(() -> service.solicitarMatricula(aluno, DISC, PER, TURMA));

                        ArgumentCaptor<MatriculaTurma> captor = ArgumentCaptor.forClass(MatriculaTurma.class);

                        verify(matriculaRepository).salvar(captor.capture());

                        MatriculaTurma salva = captor.getValue();

                        assertNotNull(salva);
                        assertNotNull(salva.getDataSolicitacao());
                        assertEquals("A0001", salva.getMatriculaAluno());
                        assertEquals(DISC, salva.getCodigoDisciplina());
                        assertEquals(PER, salva.getCodigoPeriodo());
                        assertEquals(TURMA, salva.getCodigoTurma());
                        assertEquals(StatusMatricula.LISTA_ESPERA, salva.getStatus());
                }

                @Test
                @DisplayName("5.4 Deve confirmar matrícula quando ainda houver vaga")
                void deveConfirmarMatriculaQuandoHouverVaga() {
                        mockTurmaExiste();
                        mockPeriodoAtivo();
                        mockSemSolicitacaoAtiva();
                        mockVagasOcupadas(39L);
                        mockDisciplinaSemPreRequisitos();
                        mockSemChoqueDeHorario();

                        assertDoesNotThrow(() -> service.solicitarMatricula(aluno, DISC, PER, TURMA));

                        ArgumentCaptor<MatriculaTurma> captor = ArgumentCaptor.forClass(MatriculaTurma.class);

                        verify(matriculaRepository).salvar(captor.capture());

                        assertEquals(StatusMatricula.CONFIRMADA, captor.getValue().getStatus());
                }
        }

        // =========================================================================
        // 6. CANCELAR SOLICITAÇÃO
        // =========================================================================

        @Nested
        @DisplayName("6. Cancelar solicitação")
        class CancelarSolicitacao {

                @Test
                @DisplayName("6.1 Deve cancelar solicitação PENDENTE")
                void deveCancelarSolicitacaoPendente() {
                        MatriculaTurma solicitacao = new MatriculaTurma("A0001", DISC, PER, TURMA);

                        solicitacao.setStatus(StatusMatricula.PENDENTE);

                        when(matriculaRepository.buscarPorChaveUnica("A0001", DISC, PER, TURMA))
                                        .thenReturn(solicitacao);

                        assertDoesNotThrow(() -> service.cancelarSolicitacao(aluno, DISC, PER, TURMA));

                        ArgumentCaptor<MatriculaTurma> captor = ArgumentCaptor.forClass(MatriculaTurma.class);

                        verify(matriculaRepository).atualizar(captor.capture());

                        assertEquals(StatusMatricula.CANCELADA, captor.getValue().getStatus());
                }

                @Test
                @DisplayName("6.2 Não pode cancelar solicitação CONFIRMADA")
                void naoPodeCancelarConfirmada() {
                        MatriculaTurma solicitacao = new MatriculaTurma("A0001", DISC, PER, TURMA);

                        solicitacao.setStatus(StatusMatricula.CONFIRMADA);

                        when(matriculaRepository.buscarPorChaveUnica("A0001", DISC, PER, TURMA))
                                        .thenReturn(solicitacao);

                        Exception ex = assertThrows(Exception.class,
                                        () -> service.cancelarSolicitacao(aluno, DISC, PER, TURMA));

                        assertTrue(ex.getMessage().contains("PENDENTE"));
                        verify(matriculaRepository, never()).atualizar(any());
                }

                @Test
                @DisplayName("6.3 Não pode cancelar solicitação inexistente")
                void cancelarInexistenteDeveLancar() {
                        when(matriculaRepository.buscarPorChaveUnica("A0001", DISC, PER, "T99"))
                                        .thenReturn(null);

                        Exception ex = assertThrows(Exception.class,
                                        () -> service.cancelarSolicitacao(aluno, DISC, PER, "T99"));

                        assertTrue(ex.getMessage().contains("Nenhuma solicitação encontrada"));
                        verify(matriculaRepository, never()).atualizar(any());
                }

                @Test
                @DisplayName("6.4 Coordenador não pode cancelar solicitação")
                void coordenadorNaoPodeCancelar() {
                        Exception ex = assertThrows(Exception.class,
                                        () -> service.cancelarSolicitacao(coordenador, DISC, PER, TURMA));

                        assertTrue(ex.getMessage().contains("Apenas alunos"));
                        verify(matriculaRepository, never()).atualizar(any());
                }

                @Test
                @DisplayName("6.5 Usuário null não pode cancelar solicitação")
                void usuarioNuloNaoPodeCancelar() {
                        Exception ex = assertThrows(Exception.class,
                                        () -> service.cancelarSolicitacao(null, DISC, PER, TURMA));

                        assertTrue(ex.getMessage().contains("Apenas alunos"));
                        verify(matriculaRepository, never()).atualizar(any());
                }
        }

        // =========================================================================
        // 7. LISTAR SOLICITAÇÕES DO ALUNO
        // =========================================================================

        @Nested
        @DisplayName("7. Listar solicitações do aluno")
        class ListarSolicitacoes {

                @Test
                @DisplayName("7.1 Deve retornar todas as solicitações do aluno")
                void deveRetornarListaCompletaDeSolicitacoes() throws Exception {
                        MatriculaTurma s1 = new MatriculaTurma("A0001", DISC, PER, "T01");

                        MatriculaTurma s2 = new MatriculaTurma("A0001", "FIS001", PER, "T01");

                        when(matriculaRepository.listarPorAluno("A0001"))
                                        .thenReturn(Arrays.asList(s1, s2));

                        List<MatriculaTurma> resultado = service.listarMinhasSolicitacoes(aluno);

                        assertEquals(2, resultado.size());
                }

                @Test
                @DisplayName("7.2 Lista vazia deve ser retornada quando não há solicitações")
                void deveRetornarListaVaziaQuandoSemSolicitacoes() throws Exception {
                        when(matriculaRepository.listarPorAluno("A0001"))
                                        .thenReturn(Collections.emptyList());

                        List<MatriculaTurma> resultado = service.listarMinhasSolicitacoes(aluno);

                        assertNotNull(resultado);
                        assertTrue(resultado.isEmpty());
                }

                @Test
                @DisplayName("7.3 Coordenador não pode listar solicitações do aluno")
                void coordenadorNaoPodeListar() {
                        Exception ex = assertThrows(Exception.class,
                                        () -> service.listarMinhasSolicitacoes(coordenador));

                        assertTrue(ex.getMessage().contains("Apenas alunos"));
                }

                @Test
                @DisplayName("7.4 Usuário null não pode listar solicitações")
                void usuarioNuloNaoPodeListar() {
                        Exception ex = assertThrows(Exception.class, () -> service.listarMinhasSolicitacoes(null));

                        assertTrue(ex.getMessage().contains("Apenas alunos"));
                }
        }

        // =========================================================================
        // 8. VAGAS DISPONÍVEIS
        // =========================================================================

        @Nested
        @DisplayName("8. Vagas disponíveis")
        class VagasDisponiveis {

                @Test
                @DisplayName("8.1 Deve retornar total de vagas quando não há ocupadas")
                void deveRetornarTotalQuandoSemOcupadas() {
                        when(matriculaRepository.contarOcupadasPorTurma(DISC, PER, TURMA))
                                        .thenReturn(0L);

                        long vagas = service.vagasDisponiveis(turma);

                        assertEquals(40L, vagas);
                }

                @Test
                @DisplayName("8.2 Deve retornar zero quando todas as vagas estão ocupadas")
                void deveRetornarZeroQuandoTurmaCheia() {
                        when(matriculaRepository.contarOcupadasPorTurma(DISC, PER, TURMA))
                                        .thenReturn(40L);

                        long vagas = service.vagasDisponiveis(turma);

                        assertEquals(0L, vagas);
                }

                @Test
                @DisplayName("8.3 Deve retornar diferença correta entre vagas e ocupadas")
                void deveRetornarDiferencaCorreta() {
                        when(matriculaRepository.contarOcupadasPorTurma(DISC, PER, TURMA))
                                        .thenReturn(15L);

                        long vagas = service.vagasDisponiveis(turma);

                        assertEquals(25L, vagas);
                }

                @Test
                @DisplayName("8.4 Nunca deve retornar valor negativo")
                void naoDeveRetornarNegativoEmExcesso() {
                        when(matriculaRepository.contarOcupadasPorTurma(DISC, PER, TURMA))
                                        .thenReturn(50L);

                        long vagas = service.vagasDisponiveis(turma);

                        assertEquals(0L, vagas);
                }

                @Test
                @DisplayName("8.5 Turma null deve retornar zero")
                void turmaNulaDeveRetornarZero() {
                        long vagas = service.vagasDisponiveis(null);

                        assertEquals(0L, vagas);
                }
        }

        // =========================================================================
        // 9. GESTÃO PELO COORDENADOR
        // =========================================================================

        @Nested
        @DisplayName("9. Gestão pelo coordenador")
        class GestaoCoordenador {

                @Test
                @DisplayName("9.1 Coordenador deve listar solicitações pendentes")
                void deveListarPendentes() throws Exception {
                        MatriculaTurma m1 = new MatriculaTurma("A01", "D1", "P1", "T1");

                        MatriculaTurma m2 = new MatriculaTurma("A02", "D1", "P1", "T1");

                        m2.setStatus(StatusMatricula.CONFIRMADA);

                        when(matriculaRepository.listarTodas())
                                        .thenReturn(Arrays.asList(m1, m2));

                        List<MatriculaTurma> pendentes = service.listarSolicitacoesPendentes(coordenador);

                        assertEquals(1, pendentes.size());
                        assertEquals("A01", pendentes.get(0).getMatriculaAluno());
                }

                @Test
                @DisplayName("9.2 Não-coordenador não pode listar solicitações")
                void naoCoordenadorNaoPodeListar() {
                        assertThrows(Exception.class, () -> service.listarSolicitacoesPendentes(aluno));
                }

                @Test
                @DisplayName("9.3 Coordenador deve aprovar solicitação pendente")
                void deveAprovarSolicitacao() throws Exception {
                        MatriculaTurma pendente = new MatriculaTurma("A01", "D1", "P1", "T1");

                        when(matriculaRepository.buscarPorChaveUnica("A01", "D1", "P1", "T1"))
                                        .thenReturn(pendente);

                        service.aprovarMatricula(coordenador, "A01", "D1", "P1", "T1");

                        assertEquals(StatusMatricula.CONFIRMADA, pendente.getStatus());
                        verify(matriculaRepository).atualizar(pendente);
                }

                @Test
                @DisplayName("9.4 Coordenador deve negar solicitação pendente")
                void deveNegarSolicitacao() throws Exception {
                        MatriculaTurma pendente = new MatriculaTurma("A01", "D1", "P1", "T1");

                        when(matriculaRepository.buscarPorChaveUnica("A01", "D1", "P1", "T1"))
                                        .thenReturn(pendente);

                        service.negarMatricula(coordenador, "A01", "D1", "P1", "T1");

                        assertEquals(StatusMatricula.REJEITADA, pendente.getStatus());
                        verify(matriculaRepository).atualizar(pendente);
                }

                @Test
                @DisplayName("9.5 Não deve aprovar solicitação que não seja PENDENTE")
                void naoDeveAprovarNaoPendente() {
                        MatriculaTurma confirmada = new MatriculaTurma("A01", "D1", "P1", "T1");

                        confirmada.setStatus(StatusMatricula.CONFIRMADA);

                        when(matriculaRepository.buscarPorChaveUnica("A01", "D1", "P1", "T1"))
                                        .thenReturn(confirmada);

                        assertThrows(Exception.class,
                                        () -> service.aprovarMatricula(coordenador, "A01", "D1", "P1", "T1"));
                }
        }
        // =========================================================================
        // 10. RF22 — CANCELAMENTO DE MATRÍCULA
        // =========================================================================

        @Nested
        @DisplayName("10. RF22 - Cancelamento de matrícula")
        class CancelamentoMatriculaRF22 {

                private PeriodoLetivo periodoPermitido() {
                        return new PeriodoLetivo(
                                        PER,
                                        2026,
                                        1,
                                        LocalDate.now().minusDays(5),
                                        LocalDate.now().plusDays(5),
                                        true);
                }

                private PeriodoLetivo periodoForaDoPrazo() {
                        return new PeriodoLetivo(
                                        PER,
                                        2026,
                                        1,
                                        LocalDate.now().minusDays(20),
                                        LocalDate.now().minusDays(1),
                                        true);
                }

                private PeriodoLetivo periodoInativoAtual() {
                        return new PeriodoLetivo(
                                        PER,
                                        2026,
                                        1,
                                        LocalDate.now().minusDays(5),
                                        LocalDate.now().plusDays(5),
                                        false);
                }

                private MatriculaTurma matriculaComStatus(StatusMatricula status) {
                        MatriculaTurma matricula = new MatriculaTurma("A0001", DISC, PER, TURMA);
                        matricula.setStatus(status);
                        return matricula;
                }

                @Test
                @DisplayName("10.1 Deve cancelar matrícula CONFIRMADA dentro do período permitido")
                void deveCancelarMatriculaConfirmadaDentroDoPeriodoPermitido() {
                        MatriculaTurma matricula = matriculaComStatus(StatusMatricula.CONFIRMADA);

                        when(matriculaRepository.buscarPorChaveUnica("A0001", DISC, PER, TURMA))
                                        .thenReturn(matricula);

                        when(periodoRepository.buscarPorCodigo(PER))
                                        .thenReturn(periodoPermitido());

                        assertDoesNotThrow(() -> service.cancelarMatricula(aluno, DISC, PER, TURMA));

                        ArgumentCaptor<MatriculaTurma> captor = ArgumentCaptor.forClass(MatriculaTurma.class);

                        verify(matriculaRepository).atualizar(captor.capture());

                        assertEquals(StatusMatricula.CANCELADA, captor.getValue().getStatus());
                }

                @Test
                @DisplayName("10.2 Deve cancelar entrada em LISTA_ESPERA dentro do período permitido")
                void deveCancelarListaEsperaDentroDoPeriodoPermitido() {
                        MatriculaTurma matricula = matriculaComStatus(StatusMatricula.LISTA_ESPERA);

                        when(matriculaRepository.buscarPorChaveUnica("A0001", DISC, PER, TURMA))
                                        .thenReturn(matricula);

                        when(periodoRepository.buscarPorCodigo(PER))
                                        .thenReturn(periodoPermitido());

                        assertDoesNotThrow(() -> service.cancelarMatricula(aluno, DISC, PER, TURMA));

                        ArgumentCaptor<MatriculaTurma> captor = ArgumentCaptor.forClass(MatriculaTurma.class);

                        verify(matriculaRepository).atualizar(captor.capture());

                        assertEquals(StatusMatricula.CANCELADA, captor.getValue().getStatus());
                }

                @Test
                @DisplayName("10.3 Não deve cancelar matrícula fora do período permitido")
                void naoDeveCancelarMatriculaForaDoPeriodoPermitido() {
                        MatriculaTurma matricula = matriculaComStatus(StatusMatricula.CONFIRMADA);

                        when(matriculaRepository.buscarPorChaveUnica("A0001", DISC, PER, TURMA))
                                        .thenReturn(matricula);

                        when(periodoRepository.buscarPorCodigo(PER))
                                        .thenReturn(periodoForaDoPrazo());

                        Exception ex = assertThrows(Exception.class,
                                        () -> service.cancelarMatricula(aluno, DISC, PER, TURMA));

                        assertTrue(ex.getMessage().contains("período permitido"));
                        verify(matriculaRepository, never()).atualizar(any());
                }

                @Test
                @DisplayName("10.4 Não deve cancelar matrícula em período letivo inativo")
                void naoDeveCancelarMatriculaEmPeriodoInativo() {
                        MatriculaTurma matricula = matriculaComStatus(StatusMatricula.CONFIRMADA);

                        when(matriculaRepository.buscarPorChaveUnica("A0001", DISC, PER, TURMA))
                                        .thenReturn(matricula);

                        when(periodoRepository.buscarPorCodigo(PER))
                                        .thenReturn(periodoInativoAtual());

                        Exception ex = assertThrows(Exception.class,
                                        () -> service.cancelarMatricula(aluno, DISC, PER, TURMA));

                        assertTrue(ex.getMessage().contains("inativo"));
                        verify(matriculaRepository, never()).atualizar(any());
                }

                @Test
                @DisplayName("10.5 Não deve cancelar matrícula inexistente")
                void naoDeveCancelarMatriculaInexistente() {
                        when(matriculaRepository.buscarPorChaveUnica("A0001", DISC, PER, TURMA))
                                        .thenReturn(null);

                        Exception ex = assertThrows(Exception.class,
                                        () -> service.cancelarMatricula(aluno, DISC, PER, TURMA));

                        assertTrue(ex.getMessage().contains("Nenhuma matrícula encontrada"));
                        verify(periodoRepository, never()).buscarPorCodigo(any());
                        verify(matriculaRepository, never()).atualizar(any());
                }

                @Test
                @DisplayName("10.6 Não deve cancelar matrícula já CANCELADA")
                void naoDeveCancelarMatriculaJaCancelada() {
                        MatriculaTurma matricula = matriculaComStatus(StatusMatricula.CANCELADA);

                        when(matriculaRepository.buscarPorChaveUnica("A0001", DISC, PER, TURMA))
                                        .thenReturn(matricula);

                        Exception ex = assertThrows(Exception.class,
                                        () -> service.cancelarMatricula(aluno, DISC, PER, TURMA));

                        assertTrue(ex.getMessage().contains("já está cancelada"));
                        verify(periodoRepository, never()).buscarPorCodigo(any());
                        verify(matriculaRepository, never()).atualizar(any());
                }

                @Test
                @DisplayName("10.7 Não deve cancelar matrícula REJEITADA")
                void naoDeveCancelarMatriculaRejeitada() {
                        MatriculaTurma matricula = matriculaComStatus(StatusMatricula.REJEITADA);

                        when(matriculaRepository.buscarPorChaveUnica("A0001", DISC, PER, TURMA))
                                        .thenReturn(matricula);

                        Exception ex = assertThrows(Exception.class,
                                        () -> service.cancelarMatricula(aluno, DISC, PER, TURMA));

                        assertTrue(ex.getMessage().contains("rejeitadas"));
                        verify(periodoRepository, never()).buscarPorCodigo(any());
                        verify(matriculaRepository, never()).atualizar(any());
                }

                @Test
                @DisplayName("10.8 Apenas aluno pode cancelar matrícula")
                void apenasAlunoPodeCancelarMatricula() {
                        Exception ex = assertThrows(Exception.class,
                                        () -> service.cancelarMatricula(coordenador, DISC, PER, TURMA));

                        assertTrue(ex.getMessage().contains("Apenas alunos"));
                        verify(matriculaRepository, never()).buscarPorChaveUnica(any(), any(), any(), any());
                        verify(matriculaRepository, never()).atualizar(any());
                }
        }
        // =========================================================================
        // 11. RF23 — MANUTENÇÃO DA LISTA DE ESPERA POR TURMA
        // =========================================================================

        @Nested
        @DisplayName("11. RF23 - Manutenção da lista de espera por turma")
        class ManutencaoListaEsperaRF23 {

                private PeriodoLetivo periodoPermitido() {
                        return new PeriodoLetivo(
                                        PER,
                                        2026,
                                        1,
                                        LocalDate.now().minusDays(5),
                                        LocalDate.now().plusDays(5),
                                        true);
                }

                private MatriculaTurma matriculaComStatus(
                                String matriculaAluno,
                                StatusMatricula status,
                                LocalDateTime dataSolicitacao) {
                        MatriculaTurma matricula = new MatriculaTurma(
                                        matriculaAluno,
                                        DISC,
                                        PER,
                                        TURMA);

                        matricula.setStatus(status);
                        matricula.setDataSolicitacao(dataSolicitacao);

                        return matricula;
                }

                @Test
                @DisplayName("11.1 Deve listar apenas alunos em LISTA_ESPERA de uma turma")
                void deveListarApenasAlunosEmListaEsperaDaTurma() throws Exception {
                        MatriculaTurma confirmado = matriculaComStatus(
                                        "A0001",
                                        StatusMatricula.CONFIRMADA,
                                        LocalDateTime.now().minusMinutes(30));

                        MatriculaTurma espera1 = matriculaComStatus(
                                        "A0002",
                                        StatusMatricula.LISTA_ESPERA,
                                        LocalDateTime.now().minusMinutes(20));

                        MatriculaTurma espera2 = matriculaComStatus(
                                        "A0003",
                                        StatusMatricula.LISTA_ESPERA,
                                        LocalDateTime.now().minusMinutes(10));

                        MatriculaTurma cancelado = matriculaComStatus(
                                        "A0004",
                                        StatusMatricula.CANCELADA,
                                        LocalDateTime.now().minusMinutes(5));

                        when(matriculaRepository.listarPorTurma(DISC, PER, TURMA))
                                        .thenReturn(Arrays.asList(confirmado, espera1, espera2, cancelado));

                        mockTurmaExiste();

                        List<MatriculaTurma> resultado = service.listarListaEsperaPorTurma(coordenador, DISC, PER,
                                        TURMA);

                        assertEquals(2, resultado.size());
                        assertEquals("A0002", resultado.get(0).getMatriculaAluno());
                        assertEquals("A0003", resultado.get(1).getMatriculaAluno());
                        assertTrue(resultado.stream()
                                        .allMatch(m -> m.getStatus() == StatusMatricula.LISTA_ESPERA));
                }

                @Test
                @DisplayName("11.2 Lista de espera deve ser ordenada pela data de solicitação")
                void listaEsperaDeveSerOrdenadaPelaDataSolicitacao() throws Exception {
                        MatriculaTurma esperaMaisNova = matriculaComStatus(
                                        "A0003",
                                        StatusMatricula.LISTA_ESPERA,
                                        LocalDateTime.now().minusMinutes(5));

                        MatriculaTurma esperaMaisAntiga = matriculaComStatus(
                                        "A0002",
                                        StatusMatricula.LISTA_ESPERA,
                                        LocalDateTime.now().minusMinutes(30));

                        when(matriculaRepository.listarPorTurma(DISC, PER, TURMA))
                                        .thenReturn(Arrays.asList(esperaMaisNova, esperaMaisAntiga));

                        mockTurmaExiste();

                        List<MatriculaTurma> resultado = service.listarListaEsperaPorTurma(coordenador, DISC, PER,
                                        TURMA);

                        assertEquals(2, resultado.size());
                        assertEquals("A0002", resultado.get(0).getMatriculaAluno());
                        assertEquals("A0003", resultado.get(1).getMatriculaAluno());
                }

                @Test
                @DisplayName("11.3 Não-coordenador não pode listar lista de espera da turma")
                void naoCoordenadorNaoPodeListarListaEspera() {
                        Exception ex = assertThrows(Exception.class,
                                        () -> service.listarListaEsperaPorTurma(aluno, DISC, PER, TURMA));

                        assertTrue(ex.getMessage().contains("Apenas coordenadores"));
                        verify(matriculaRepository, never()).listarPorTurma(any(), any(), any());
                }

                @Test
                @DisplayName("11.4 Ao cancelar matrícula CONFIRMADA, deve promover primeiro da lista de espera")
                void aoCancelarConfirmadaDevePromoverPrimeiroDaListaEspera() {
                        MatriculaTurma confirmada = matriculaComStatus(
                                        "A0001",
                                        StatusMatricula.CONFIRMADA,
                                        LocalDateTime.now().minusHours(1));

                        MatriculaTurma espera = matriculaComStatus(
                                        "A0002",
                                        StatusMatricula.LISTA_ESPERA,
                                        LocalDateTime.now().minusMinutes(30));

                        when(matriculaRepository.buscarPorChaveUnica("A0001", DISC, PER, TURMA))
                                        .thenReturn(confirmada);

                        when(periodoRepository.buscarPorCodigo(PER))
                                        .thenReturn(periodoPermitido());

                        when(turmaRepository.buscarPorChaveUnica(DISC, PER, TURMA))
                                        .thenReturn(turma);

                        when(matriculaRepository.contarOcupadasPorTurma(DISC, PER, TURMA))
                                        .thenReturn(0L);

                        when(matriculaRepository.listarPorTurma(DISC, PER, TURMA))
                                        .thenReturn(Collections.singletonList(espera));

                        assertDoesNotThrow(() -> service.cancelarMatricula(aluno, DISC, PER, TURMA));

                        ArgumentCaptor<MatriculaTurma> captor = ArgumentCaptor.forClass(MatriculaTurma.class);

                        verify(matriculaRepository, times(2)).atualizar(captor.capture());

                        List<MatriculaTurma> atualizadas = captor.getAllValues();

                        assertEquals(StatusMatricula.CANCELADA, atualizadas.get(0).getStatus());
                        assertEquals("A0001", atualizadas.get(0).getMatriculaAluno());

                        assertEquals(StatusMatricula.CONFIRMADA, atualizadas.get(1).getStatus());
                        assertEquals("A0002", atualizadas.get(1).getMatriculaAluno());
                }

                @Test
                @DisplayName("11.5 Se não houver aluno na lista de espera, deve apenas cancelar")
                void seNaoHouverListaEsperaDeveApenasCancelar() {
                        MatriculaTurma confirmada = matriculaComStatus(
                                        "A0001",
                                        StatusMatricula.CONFIRMADA,
                                        LocalDateTime.now().minusHours(1));

                        when(matriculaRepository.buscarPorChaveUnica("A0001", DISC, PER, TURMA))
                                        .thenReturn(confirmada);

                        when(periodoRepository.buscarPorCodigo(PER))
                                        .thenReturn(periodoPermitido());

                        when(turmaRepository.buscarPorChaveUnica(DISC, PER, TURMA))
                                        .thenReturn(turma);

                        when(matriculaRepository.contarOcupadasPorTurma(DISC, PER, TURMA))
                                        .thenReturn(0L);

                        when(matriculaRepository.listarPorTurma(DISC, PER, TURMA))
                                        .thenReturn(Collections.emptyList());

                        assertDoesNotThrow(() -> service.cancelarMatricula(aluno, DISC, PER, TURMA));

                        verify(matriculaRepository, times(1)).atualizar(any(MatriculaTurma.class));
                }

                @Test
                @DisplayName("11.6 Cancelar aluno da LISTA_ESPERA não deve promover outro aluno")
                void cancelarAlunoDaListaEsperaNaoDevePromoverOutroAluno() {
                        MatriculaTurma espera = matriculaComStatus(
                                        "A0001",
                                        StatusMatricula.LISTA_ESPERA,
                                        LocalDateTime.now().minusMinutes(30));

                        when(matriculaRepository.buscarPorChaveUnica("A0001", DISC, PER, TURMA))
                                        .thenReturn(espera);

                        when(periodoRepository.buscarPorCodigo(PER))
                                        .thenReturn(periodoPermitido());

                        assertDoesNotThrow(() -> service.cancelarMatricula(aluno, DISC, PER, TURMA));

                        verify(matriculaRepository, times(1)).atualizar(any(MatriculaTurma.class));
                        verify(turmaRepository, never()).buscarPorChaveUnica(any(), any(), any());
                }

                @Test
                @DisplayName("11.7 Se a turma ainda estiver cheia, não deve promover aluno da lista de espera")
                void seTurmaAindaEstiverCheiaNaoDevePromoverListaEspera() {
                        MatriculaTurma confirmada = matriculaComStatus(
                                        "A0001",
                                        StatusMatricula.CONFIRMADA,
                                        LocalDateTime.now().minusHours(1));

                        Turma turmaComUmaVaga = new Turma(
                                        TURMA,
                                        DISC,
                                        PER,
                                        1,
                                        "Seg/Qua 10h-12h",
                                        "Bloco A-101",
                                        "P0001");

                        when(matriculaRepository.buscarPorChaveUnica("A0001", DISC, PER, TURMA))
                                        .thenReturn(confirmada);

                        when(periodoRepository.buscarPorCodigo(PER))
                                        .thenReturn(periodoPermitido());

                        when(turmaRepository.buscarPorChaveUnica(DISC, PER, TURMA))
                                        .thenReturn(turmaComUmaVaga);

                        when(matriculaRepository.contarOcupadasPorTurma(DISC, PER, TURMA))
                                        .thenReturn(1L);

                        assertDoesNotThrow(() -> service.cancelarMatricula(aluno, DISC, PER, TURMA));

                        verify(matriculaRepository, times(1)).atualizar(any(MatriculaTurma.class));
                        verify(matriculaRepository, never()).listarPorTurma(any(), any(), any());
                }
        }
}