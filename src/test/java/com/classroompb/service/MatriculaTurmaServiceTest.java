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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.classroompb.model.Administrador;
import com.classroompb.model.Aluno;
import com.classroompb.model.Coordenador;
import com.classroompb.model.Disciplina;
import com.classroompb.model.Historico;
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
 * RF16: Aluno solicita matrícula em turma. RF20: Matrícula confirmada automaticamente quando houver vaga e critérios
 * atendidos. RF21: Caso não haja vaga, o aluno deve entrar em lista de espera.
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
        service = new MatriculaTurmaService(matriculaRepository, turmaRepository, periodoRepository,
                disciplinaRepository, historicoRepository);

        aluno = new Aluno("A0001", "João Silva", "joao@test.com", "senha123");
        outroAluno = new Aluno("A0002", "Maria Souza", "maria@test.com", "senha456");
        coordenador = new Coordenador("C0001", "Coord", "coord@test.com", "senha123");
        professor = new Professor("P0001", "Prof", "prof@test.com", "senha123");
        administrador = new Administrador("ADM001", "Admin", "adm@test.com", "senha123");

        turma = new Turma(TURMA, DISC, PER, 40, "Seg/Qua 10h-12h", "Bloco A-101", "P0001");

        periodoAtivo = new PeriodoLetivo(PER, 2026, 1, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 6, 30), true);

        periodoInativo = new PeriodoLetivo("2025.2", 2025, 2, LocalDate.of(2025, 8, 1), LocalDate.of(2025, 12, 20),
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

    // Helpers compartilhados pelos grupos 12 e 13 (evita duplicação detectada pelo CPD)
    private MatriculaTurma matriculaPendente(String matriculaAluno) {
        MatriculaTurma m = new MatriculaTurma(matriculaAluno, DISC, PER, TURMA);
        m.setStatus(StatusMatricula.PENDENTE);
        m.setDataSolicitacao(LocalDateTime.now().minusHours(1));
        return m;
    }

    private MatriculaTurma matriculaEmEspera(String matriculaAluno, LocalDateTime dataSolicitacao) {
        MatriculaTurma m = new MatriculaTurma(matriculaAluno, DISC, PER, TURMA);
        m.setStatus(StatusMatricula.LISTA_ESPERA);
        m.setDataSolicitacao(dataSolicitacao);
        return m;
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

            assertEquals(StatusMatricula.CONFIRMADA, captor.getValue().getStatus(),
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

            service.solicitarMatricula(aluno, "  " + DISC + "  ", "  " + PER + "  ", "  " + TURMA + "  ");

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

            when(matriculaRepository.existeSolicitacaoAtiva("A0002", DISC, PER, TURMA)).thenReturn(false);

            when(matriculaRepository.listarPorAluno("A0002")).thenReturn(Collections.emptyList());

            assertDoesNotThrow(() -> service.solicitarMatricula(outroAluno, DISC, PER, TURMA));
        }

        @Test
        @DisplayName("1.7 Aluno pode solicitar matrícula em disciplinas diferentes no mesmo período")
        void podeMatricularEmDisciplinasDiferentes() {
            Turma turmaFis = new Turma("T01", "FIS001", PER, 30, "Ter/Qui 14h-16h", "Lab Fis", "P0002");

            mockCenarioSucesso();

            assertDoesNotThrow(() -> service.solicitarMatricula(aluno, DISC, PER, TURMA));

            when(turmaRepository.buscarPorChaveUnica("FIS001", PER, "T01")).thenReturn(turmaFis);

            when(periodoRepository.buscarPorCodigo(PER)).thenReturn(periodoAtivo);

            when(matriculaRepository.existeSolicitacaoAtiva("A0001", "FIS001", PER, "T01")).thenReturn(false);

            when(matriculaRepository.contarOcupadasPorTurma("FIS001", PER, "T01")).thenReturn(0L);

            when(disciplinaRepository.buscarPorCodigo("FIS001"))
                    .thenReturn(new Disciplina("FIS001", "Física I", 60, 4, Collections.emptyList()));

            when(matriculaRepository.listarPorAluno("A0001")).thenReturn(Collections.emptyList());

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
            Exception ex = assertThrows(Exception.class, () -> service.solicitarMatricula(professor, DISC, PER, TURMA));

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
            Exception ex = assertThrows(Exception.class, () -> service.solicitarMatricula(null, DISC, PER, TURMA));

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
            Exception ex = assertThrows(Exception.class, () -> service.solicitarMatricula(aluno, null, PER, TURMA));

            assertTrue(ex.getMessage().toLowerCase().contains("disciplina"));
            verify(matriculaRepository, never()).salvar(any());
        }

        @Test
        @DisplayName("3.2 Código de disciplina vazio deve lançar exceção")
        void disciplinaVaziaDeveLancar() {
            Exception ex = assertThrows(Exception.class, () -> service.solicitarMatricula(aluno, "", PER, TURMA));

            assertTrue(ex.getMessage().toLowerCase().contains("disciplina"));
            verify(matriculaRepository, never()).salvar(any());
        }

        @Test
        @DisplayName("3.3 Código de disciplina só com espaços deve lançar exceção")
        void disciplinaSoEspacosDeveLancar() {
            Exception ex = assertThrows(Exception.class, () -> service.solicitarMatricula(aluno, "   ", PER, TURMA));

            assertTrue(ex.getMessage().toLowerCase().contains("disciplina"));
            verify(matriculaRepository, never()).salvar(any());
        }

        @Test
        @DisplayName("3.4 Código de período null deve lançar exceção")
        void periodoNuloDeveLancar() {
            Exception ex = assertThrows(Exception.class, () -> service.solicitarMatricula(aluno, DISC, null, TURMA));

            assertTrue(ex.getMessage().toLowerCase().contains("período"));
            verify(matriculaRepository, never()).salvar(any());
        }

        @Test
        @DisplayName("3.5 Código de período vazio deve lançar exceção")
        void periodoVazioDeveLancar() {
            Exception ex = assertThrows(Exception.class, () -> service.solicitarMatricula(aluno, DISC, "", TURMA));

            assertTrue(ex.getMessage().toLowerCase().contains("período"));
            verify(matriculaRepository, never()).salvar(any());
        }

        @Test
        @DisplayName("3.6 Código de período só com espaços deve lançar exceção")
        void periodoSoEspacosDeveLancar() {
            Exception ex = assertThrows(Exception.class, () -> service.solicitarMatricula(aluno, DISC, "   ", TURMA));

            assertTrue(ex.getMessage().toLowerCase().contains("período"));
            verify(matriculaRepository, never()).salvar(any());
        }

        @Test
        @DisplayName("3.7 Código de turma null deve lançar exceção")
        void turmaNulaDeveLancar() {
            Exception ex = assertThrows(Exception.class, () -> service.solicitarMatricula(aluno, DISC, PER, null));

            assertTrue(ex.getMessage().toLowerCase().contains("turma"));
            verify(matriculaRepository, never()).salvar(any());
        }

        @Test
        @DisplayName("3.8 Código de turma vazio deve lançar exceção")
        void turmaVaziaDeveLancar() {
            Exception ex = assertThrows(Exception.class, () -> service.solicitarMatricula(aluno, DISC, PER, ""));

            assertTrue(ex.getMessage().toLowerCase().contains("turma"));
            verify(matriculaRepository, never()).salvar(any());
        }

        @Test
        @DisplayName("3.9 Código de turma só com espaços deve lançar exceção")
        void turmaSoEspacosDeveLancar() {
            Exception ex = assertThrows(Exception.class, () -> service.solicitarMatricula(aluno, DISC, PER, "   "));

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
            Turma turmaPeriodoInativo = new Turma("T01", DISC, "2025.2", 40, "Seg 10h", "A101", "P0001");

            when(turmaRepository.buscarPorChaveUnica(DISC, "2025.2", "T01")).thenReturn(turmaPeriodoInativo);

            when(periodoRepository.buscarPorCodigo("2025.2")).thenReturn(periodoInativo);

            Exception ex = assertThrows(Exception.class,
                    () -> service.solicitarMatricula(aluno, DISC, "2025.2", "T01"));

            assertTrue(ex.getMessage().contains("inativo"));
            verify(matriculaRepository, never()).salvar(any());
        }

        @Test
        @DisplayName("4.3 Período inexistente deve impedir matrícula")
        void periodoNuloNoRepositorioDeveImpedir() {
            Turma turmaSemPeriodo = new Turma("T01", DISC, "2099.1", 40, "Seg 10h", "A101", "P0001");

            when(turmaRepository.buscarPorChaveUnica(DISC, "2099.1", "T01")).thenReturn(turmaSemPeriodo);

            when(periodoRepository.buscarPorCodigo("2099.1")).thenReturn(null);

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

            when(matriculaRepository.existeSolicitacaoAtiva("A0001", DISC, PER, TURMA)).thenReturn(true);

            Exception ex = assertThrows(Exception.class, () -> service.solicitarMatricula(aluno, DISC, PER, TURMA));

            assertTrue(ex.getMessage().contains("já possui uma solicitação ativa"));
            verify(matriculaRepository, never()).salvar(any());
        }

        @Test
        @DisplayName("4.5 Disciplina inexistente deve lançar exceção")
        void disciplinaInexistenteDeveLancar() {
            mockTurmaExiste();
            mockPeriodoAtivo();
            mockSemSolicitacaoAtiva();

            when(disciplinaRepository.buscarPorCodigo(DISC)).thenReturn(null);

            Exception ex = assertThrows(Exception.class, () -> service.solicitarMatricula(aluno, DISC, PER, TURMA));

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

            when(matriculaRepository.buscarPorChaveUnica("A0001", DISC, PER, TURMA)).thenReturn(solicitacao);

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

            when(matriculaRepository.buscarPorChaveUnica("A0001", DISC, PER, TURMA)).thenReturn(solicitacao);

            Exception ex = assertThrows(Exception.class, () -> service.cancelarSolicitacao(aluno, DISC, PER, TURMA));

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
            Exception ex = assertThrows(Exception.class, () -> service.cancelarSolicitacao(null, DISC, PER, TURMA));

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

            when(matriculaRepository.listarPorAluno("A0001")).thenReturn(Arrays.asList(s1, s2));

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
            Exception ex = assertThrows(Exception.class, () -> service.listarMinhasSolicitacoes(coordenador));

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

            when(matriculaRepository.listarTodas()).thenReturn(Arrays.asList(m1, m2));

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

            when(matriculaRepository.buscarPorChaveUnica("A01", "D1", "P1", "T1")).thenReturn(pendente);

            service.aprovarMatricula(coordenador, "A01", "D1", "P1", "T1");

            assertEquals(StatusMatricula.CONFIRMADA, pendente.getStatus());
            verify(matriculaRepository).atualizar(pendente);
        }

        @Test
        @DisplayName("9.4 Coordenador deve negar solicitação pendente")
        void deveNegarSolicitacao() throws Exception {
            MatriculaTurma pendente = new MatriculaTurma("A01", "D1", "P1", "T1");

            when(matriculaRepository.buscarPorChaveUnica("A01", "D1", "P1", "T1")).thenReturn(pendente);

            service.negarMatricula(coordenador, "A01", "D1", "P1", "T1");

            assertEquals(StatusMatricula.REJEITADA, pendente.getStatus());
            verify(matriculaRepository).atualizar(pendente);
        }

        @Test
        @DisplayName("9.5 Não deve aprovar solicitação que não seja PENDENTE")
        void naoDeveAprovarNaoPendente() {
            MatriculaTurma confirmada = new MatriculaTurma("A01", "D1", "P1", "T1");

            confirmada.setStatus(StatusMatricula.CONFIRMADA);

            when(matriculaRepository.buscarPorChaveUnica("A01", "D1", "P1", "T1")).thenReturn(confirmada);

            assertThrows(Exception.class, () -> service.aprovarMatricula(coordenador, "A01", "D1", "P1", "T1"));
        }
    }
    // =========================================================================
    // 10. RF22 — CANCELAMENTO DE MATRÍCULA
    // =========================================================================

    @Nested
    @DisplayName("10. RF22 - Cancelamento de matrícula")
    class CancelamentoMatriculaRF22 {

        private PeriodoLetivo periodoPermitido() {
            return new PeriodoLetivo(PER, 2026, 1, LocalDate.now().minusDays(5), LocalDate.now().plusDays(5), true);
        }

        private PeriodoLetivo periodoForaDoPrazo() {
            return new PeriodoLetivo(PER, 2026, 1, LocalDate.now().minusDays(20), LocalDate.now().minusDays(1), true);
        }

        private PeriodoLetivo periodoInativoAtual() {
            return new PeriodoLetivo(PER, 2026, 1, LocalDate.now().minusDays(5), LocalDate.now().plusDays(5), false);
        }

        private MatriculaTurma matriculaComStatus(StatusMatricula status) {
            MatriculaTurma matricula = new MatriculaTurma("A0001", DISC, PER, TURMA);
            matricula.setStatus(status);
            return matricula;
        }

        private void assertCancelaDentroDoPeriodo(StatusMatricula statusInicial) {
            MatriculaTurma matricula = matriculaComStatus(statusInicial);

            when(matriculaRepository.buscarPorChaveUnica("A0001", DISC, PER, TURMA)).thenReturn(matricula);

            when(periodoRepository.buscarPorCodigo(PER)).thenReturn(periodoPermitido());

            assertDoesNotThrow(() -> service.cancelarMatricula(aluno, DISC, PER, TURMA));

            ArgumentCaptor<MatriculaTurma> captor = ArgumentCaptor.forClass(MatriculaTurma.class);

            verify(matriculaRepository).atualizar(captor.capture());

            assertEquals(StatusMatricula.CANCELADA, captor.getValue().getStatus());
        }

        @Test
        @DisplayName("10.1 Deve cancelar matrícula CONFIRMADA dentro do período permitido")
        void deveCancelarMatriculaConfirmadaDentroDoPeriodoPermitido() {
            assertCancelaDentroDoPeriodo(StatusMatricula.CONFIRMADA);
        }

        @Test
        @DisplayName("10.2 Deve cancelar entrada em LISTA_ESPERA dentro do período permitido")
        void deveCancelarListaEsperaDentroDoPeriodoPermitido() {
            assertCancelaDentroDoPeriodo(StatusMatricula.LISTA_ESPERA);
        }

        @Test
        @DisplayName("10.3 Não deve cancelar matrícula fora do período permitido")
        void naoDeveCancelarMatriculaForaDoPeriodoPermitido() {
            MatriculaTurma matricula = matriculaComStatus(StatusMatricula.CONFIRMADA);

            when(matriculaRepository.buscarPorChaveUnica("A0001", DISC, PER, TURMA)).thenReturn(matricula);

            when(periodoRepository.buscarPorCodigo(PER)).thenReturn(periodoForaDoPrazo());

            Exception ex = assertThrows(Exception.class, () -> service.cancelarMatricula(aluno, DISC, PER, TURMA));

            assertTrue(ex.getMessage().contains("período permitido"));
            verify(matriculaRepository, never()).atualizar(any());
        }

        @Test
        @DisplayName("10.4 Não deve cancelar matrícula em período letivo inativo")
        void naoDeveCancelarMatriculaEmPeriodoInativo() {
            MatriculaTurma matricula = matriculaComStatus(StatusMatricula.CONFIRMADA);

            when(matriculaRepository.buscarPorChaveUnica("A0001", DISC, PER, TURMA)).thenReturn(matricula);

            when(periodoRepository.buscarPorCodigo(PER)).thenReturn(periodoInativoAtual());

            Exception ex = assertThrows(Exception.class, () -> service.cancelarMatricula(aluno, DISC, PER, TURMA));

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

            when(matriculaRepository.buscarPorChaveUnica("A0001", DISC, PER, TURMA)).thenReturn(matricula);

            Exception ex = assertThrows(Exception.class, () -> service.cancelarMatricula(aluno, DISC, PER, TURMA));

            assertTrue(ex.getMessage().contains("já está cancelada"));
            verify(periodoRepository, never()).buscarPorCodigo(any());
            verify(matriculaRepository, never()).atualizar(any());
        }

        @Test
        @DisplayName("10.7 Não deve cancelar matrícula REJEITADA")
        void naoDeveCancelarMatriculaRejeitada() {
            MatriculaTurma matricula = matriculaComStatus(StatusMatricula.REJEITADA);

            when(matriculaRepository.buscarPorChaveUnica("A0001", DISC, PER, TURMA)).thenReturn(matricula);

            Exception ex = assertThrows(Exception.class, () -> service.cancelarMatricula(aluno, DISC, PER, TURMA));

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
    // 11. RF23/RF26 — MANUTENÇÃO E VISUALIZAÇÃO DA LISTA DE ESPERA POR TURMA
    // =========================================================================

    @Nested
    @DisplayName("11. RF23/RF26 - Manutenção e visualização da lista de espera por turma")
    class ManutencaoListaEsperaRF23 {

        private PeriodoLetivo periodoPermitido() {
            return new PeriodoLetivo(PER, 2026, 1, LocalDate.now().minusDays(5), LocalDate.now().plusDays(5), true);
        }

        private MatriculaTurma matriculaComStatus(String matriculaAluno, StatusMatricula status,
                LocalDateTime dataSolicitacao) {
            MatriculaTurma matricula = new MatriculaTurma(matriculaAluno, DISC, PER, TURMA);

            matricula.setStatus(status);
            matricula.setDataSolicitacao(dataSolicitacao);

            return matricula;
        }

        @Test
        @DisplayName("11.1 Deve listar apenas alunos em LISTA_ESPERA de uma turma")
        void deveListarApenasAlunosEmListaEsperaDaTurma() throws Exception {
            MatriculaTurma confirmado = matriculaComStatus("A0001", StatusMatricula.CONFIRMADA,
                    LocalDateTime.now().minusMinutes(30));

            MatriculaTurma espera1 = matriculaComStatus("A0002", StatusMatricula.LISTA_ESPERA,
                    LocalDateTime.now().minusMinutes(20));

            MatriculaTurma espera2 = matriculaComStatus("A0003", StatusMatricula.LISTA_ESPERA,
                    LocalDateTime.now().minusMinutes(10));

            MatriculaTurma cancelado = matriculaComStatus("A0004", StatusMatricula.CANCELADA,
                    LocalDateTime.now().minusMinutes(5));

            when(matriculaRepository.listarListaEsperaPorTurmaOrdenada(DISC, PER, TURMA))
                    .thenReturn(Arrays.asList(confirmado, espera1, espera2, cancelado));

            mockTurmaExiste();

            List<MatriculaTurma> resultado = service.listarListaEsperaPorTurma(coordenador, DISC, PER, TURMA);

            assertEquals(2, resultado.size());
            assertEquals("A0002", resultado.get(0).getMatriculaAluno());
            assertEquals("A0003", resultado.get(1).getMatriculaAluno());
            assertTrue(resultado.stream().allMatch(m -> m.getStatus() == StatusMatricula.LISTA_ESPERA));
        }

        @Test
        @DisplayName("11.2 Lista de espera deve ser ordenada pela data de solicitação")
        void listaEsperaDeveSerOrdenadaPelaDataSolicitacao() throws Exception {
            MatriculaTurma esperaMaisNova = matriculaComStatus("A0003", StatusMatricula.LISTA_ESPERA,
                    LocalDateTime.now().minusMinutes(5));

            MatriculaTurma esperaMaisAntiga = matriculaComStatus("A0002", StatusMatricula.LISTA_ESPERA,
                    LocalDateTime.now().minusMinutes(30));

            when(matriculaRepository.listarListaEsperaPorTurmaOrdenada(DISC, PER, TURMA))
                    .thenReturn(Arrays.asList(esperaMaisNova, esperaMaisAntiga));

            mockTurmaExiste();

            List<MatriculaTurma> resultado = service.listarListaEsperaPorTurma(coordenador, DISC, PER, TURMA);

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
            verify(matriculaRepository, never()).listarListaEsperaPorTurmaOrdenada(any(), any(), any());
        }

        @Test
        @DisplayName("11.4 Turma inexistente não deve listar lista de espera")
        void turmaInexistenteNaoDeveListarListaEspera() {
            when(turmaRepository.buscarPorChaveUnica(DISC, PER, TURMA)).thenReturn(null);

            Exception ex = assertThrows(Exception.class,
                    () -> service.listarListaEsperaPorTurma(coordenador, DISC, PER, TURMA));

            assertTrue(ex.getMessage().contains("Turma"));
            verify(matriculaRepository, never()).listarListaEsperaPorTurmaOrdenada(any(), any(), any());
        }

        private MatriculaTurma configurarCancelamentoComPromoção(List<MatriculaTurma> listaEspera) {
            MatriculaTurma confirmada = matriculaComStatus("A0001", StatusMatricula.CONFIRMADA,
                    LocalDateTime.now().minusHours(1));

            when(matriculaRepository.buscarPorChaveUnica("A0001", DISC, PER, TURMA)).thenReturn(confirmada);
            when(periodoRepository.buscarPorCodigo(PER)).thenReturn(periodoPermitido());
            when(turmaRepository.buscarPorChaveUnica(DISC, PER, TURMA)).thenReturn(turma);
            when(matriculaRepository.contarOcupadasPorTurma(DISC, PER, TURMA)).thenReturn(0L);
            when(matriculaRepository.listarListaEsperaPorTurmaOrdenada(DISC, PER, TURMA)).thenReturn(listaEspera);
            return confirmada;
        }

        @Test
        @DisplayName("11.5 Ao cancelar matrícula CONFIRMADA, deve promover primeiro da lista de espera")
        void aoCancelarConfirmadaDevePromoverPrimeiroDaListaEspera() {
            MatriculaTurma espera = matriculaComStatus("A0002", StatusMatricula.LISTA_ESPERA,
                    LocalDateTime.now().minusMinutes(30));

            configurarCancelamentoComPromoção(Collections.singletonList(espera));

            assertDoesNotThrow(() -> service.cancelarMatricula(aluno, DISC, PER, TURMA));

            ArgumentCaptor<MatriculaTurma> captor = ArgumentCaptor.forClass(MatriculaTurma.class);

            verify(matriculaRepository, times(2)).atualizar(captor.capture());

            assertCanceladaEPromoveuConfirmada(captor, "A0001", "A0002");
        }

        @Test
        @DisplayName("11.6 Se não houver aluno na lista de espera, deve apenas cancelar")
        void seNaoHouverListaEsperaDeveApenasCancelar() {
            configurarCancelamentoComPromoção(Collections.emptyList());

            assertDoesNotThrow(() -> service.cancelarMatricula(aluno, DISC, PER, TURMA));

            verify(matriculaRepository, times(1)).atualizar(any(MatriculaTurma.class));
        }

        @Test
        @DisplayName("11.6 Cancelar aluno da LISTA_ESPERA não deve promover outro aluno")
        void cancelarAlunoDaListaEsperaNaoDevePromoverOutroAluno() {
            MatriculaTurma espera = matriculaComStatus("A0001", StatusMatricula.LISTA_ESPERA,
                    LocalDateTime.now().minusMinutes(30));

            when(matriculaRepository.buscarPorChaveUnica("A0001", DISC, PER, TURMA)).thenReturn(espera);

            when(periodoRepository.buscarPorCodigo(PER)).thenReturn(periodoPermitido());

            assertDoesNotThrow(() -> service.cancelarMatricula(aluno, DISC, PER, TURMA));

            verify(matriculaRepository, times(1)).atualizar(any(MatriculaTurma.class));
            verify(turmaRepository, never()).buscarPorChaveUnica(any(), any(), any());
        }

        @Test
        @DisplayName("11.7 Se a turma ainda estiver cheia, não deve promover aluno da lista de espera")
        void seTurmaAindaEstiverCheiaNaoDevePromoverListaEspera() {
            MatriculaTurma confirmada = matriculaComStatus("A0001", StatusMatricula.CONFIRMADA,
                    LocalDateTime.now().minusHours(1));

            Turma turmaComUmaVaga = new Turma(TURMA, DISC, PER, 1, "Seg/Qua 10h-12h", "Bloco A-101", "P0001");

            when(matriculaRepository.buscarPorChaveUnica("A0001", DISC, PER, TURMA)).thenReturn(confirmada);

            when(periodoRepository.buscarPorCodigo(PER)).thenReturn(periodoPermitido());

            when(turmaRepository.buscarPorChaveUnica(DISC, PER, TURMA)).thenReturn(turmaComUmaVaga);

            when(matriculaRepository.contarOcupadasPorTurma(DISC, PER, TURMA)).thenReturn(1L);

            assertDoesNotThrow(() -> service.cancelarMatricula(aluno, DISC, PER, TURMA));

            verify(matriculaRepository, times(1)).atualizar(any(MatriculaTurma.class));
            verify(matriculaRepository, never()).listarListaEsperaPorTurmaOrdenada(any(), any(), any());
        }
    }
    // =========================================================================
    // 12. RF23 — CHAMADA AUTOMÁTICA AO NEGAR MATRÍCULA (COORDENADOR)
    // =========================================================================

    @Nested
    @DisplayName("12. RF23 - Chamada automática da lista de espera ao negar matrícula")
    class ChamadaAutomaticaAoNegarMatricula {

        @Test
        @DisplayName("12.1 Ao negar matrícula PENDENTE deve promover o primeiro da lista de espera")
        void aoNegarMatriculaDevePromoverPrimeiroDaListaEspera() throws Exception {
            MatriculaTurma solicitacaoNegada = matriculaPendente("A0001");
            MatriculaTurma esperaA0002 = matriculaEmEspera("A0002", LocalDateTime.now().minusMinutes(30));

            when(matriculaRepository.buscarPorChaveUnica("A0001", DISC, PER, TURMA)).thenReturn(solicitacaoNegada);
            when(turmaRepository.buscarPorChaveUnica(DISC, PER, TURMA)).thenReturn(turma);
            when(matriculaRepository.contarOcupadasPorTurma(DISC, PER, TURMA)).thenReturn(0L);
            when(matriculaRepository.listarListaEsperaPorTurmaOrdenada(DISC, PER, TURMA))
                    .thenReturn(Collections.singletonList(esperaA0002));

            service.negarMatricula(coordenador, "A0001", DISC, PER, TURMA);

            ArgumentCaptor<MatriculaTurma> captor = ArgumentCaptor.forClass(MatriculaTurma.class);
            verify(matriculaRepository, times(2)).atualizar(captor.capture());

            List<MatriculaTurma> atualizadas = captor.getAllValues();
            assertEquals(StatusMatricula.REJEITADA, atualizadas.get(0).getStatus());
            assertEquals("A0001", atualizadas.get(0).getMatriculaAluno());

            assertEquals(StatusMatricula.CONFIRMADA, atualizadas.get(1).getStatus());
            assertEquals("A0002", atualizadas.get(1).getMatriculaAluno());
        }

        @Test
        @DisplayName("12.2 Se lista de espera vazia ao negar, deve apenas rejeitar sem promover")
        void aoNegarComListaVaziaDeveApenasRejeitar() throws Exception {
            MatriculaTurma solicitacaoNegada = matriculaPendente("A0001");

            when(matriculaRepository.buscarPorChaveUnica("A0001", DISC, PER, TURMA)).thenReturn(solicitacaoNegada);
            when(turmaRepository.buscarPorChaveUnica(DISC, PER, TURMA)).thenReturn(turma);
            when(matriculaRepository.contarOcupadasPorTurma(DISC, PER, TURMA)).thenReturn(0L);
            when(matriculaRepository.listarListaEsperaPorTurmaOrdenada(DISC, PER, TURMA))
                    .thenReturn(Collections.emptyList());

            service.negarMatricula(coordenador, "A0001", DISC, PER, TURMA);

            verify(matriculaRepository, times(1)).atualizar(any(MatriculaTurma.class));
        }

        @Test
        @DisplayName("12.3 Deve promover o mais antigo quando há vários na lista de espera")
        void devePromoverOMaisAntigoQuandoVariosNaListaDeEspera() throws Exception {
            MatriculaTurma solicitacaoNegada = matriculaPendente("A0001");
            MatriculaTurma esperaMaisAntiga = matriculaEmEspera("A0002", LocalDateTime.now().minusHours(2));
            MatriculaTurma esperaMaisNova = matriculaEmEspera("A0003", LocalDateTime.now().minusMinutes(10));

            when(matriculaRepository.buscarPorChaveUnica("A0001", DISC, PER, TURMA)).thenReturn(solicitacaoNegada);
            when(turmaRepository.buscarPorChaveUnica(DISC, PER, TURMA)).thenReturn(turma);
            when(matriculaRepository.contarOcupadasPorTurma(DISC, PER, TURMA)).thenReturn(0L);
            when(matriculaRepository.listarListaEsperaPorTurmaOrdenada(DISC, PER, TURMA))
                    .thenReturn(Arrays.asList(esperaMaisNova, esperaMaisAntiga));

            service.negarMatricula(coordenador, "A0001", DISC, PER, TURMA);

            assertPromoveuAlunoMaisAntigoDaListaEspera("A0002");
        }

        @Test
        @DisplayName("12.4 Se turma ainda estiver cheia após negar, não deve promover lista de espera")
        void seTurmaAindaEstiverCheiaAposNegarNaoDevePromover() throws Exception {
            MatriculaTurma solicitacaoNegada = matriculaPendente("A0001");
            Turma turmaLlena = new Turma(TURMA, DISC, PER, 1, "Seg 10h", "A101", "P0001");

            when(matriculaRepository.buscarPorChaveUnica("A0001", DISC, PER, TURMA)).thenReturn(solicitacaoNegada);
            when(turmaRepository.buscarPorChaveUnica(DISC, PER, TURMA)).thenReturn(turmaLlena);
            // Após negar, ainda há 1 vaga ocupada (por outra confirmada)
            when(matriculaRepository.contarOcupadasPorTurma(DISC, PER, TURMA)).thenReturn(1L);

            service.negarMatricula(coordenador, "A0001", DISC, PER, TURMA);

            verify(matriculaRepository, times(1)).atualizar(any(MatriculaTurma.class));
            verify(matriculaRepository, never()).listarListaEsperaPorTurmaOrdenada(any(), any(), any());
        }
    }

    // =========================================================================
    // 13. RF23 — CHAMADA AUTOMÁTICA AO CANCELAR SOLICITAÇÃO PENDENTE (ALUNO)
    // =========================================================================

    @Nested
    @DisplayName("13. RF23 - Chamada automática da lista de espera ao cancelar solicitação PENDENTE")
    class ChamadaAutomaticaAoCancelarSolicitacao {

        @Test
        @DisplayName("13.1 Ao cancelar solicitação PENDENTE deve promover o primeiro da lista de espera")
        void aoCancelarSolicitacaoPendenteDevePromoverPrimeiroDaListaEspera() throws Exception {
            MatriculaTurma solicitacao = matriculaPendente("A0001");
            MatriculaTurma esperaA0002 = matriculaEmEspera("A0002", LocalDateTime.now().minusMinutes(30));

            when(matriculaRepository.buscarPorChaveUnica("A0001", DISC, PER, TURMA)).thenReturn(solicitacao);
            when(turmaRepository.buscarPorChaveUnica(DISC, PER, TURMA)).thenReturn(turma);
            when(matriculaRepository.contarOcupadasPorTurma(DISC, PER, TURMA)).thenReturn(0L);
            when(matriculaRepository.listarListaEsperaPorTurmaOrdenada(DISC, PER, TURMA))
                    .thenReturn(Collections.singletonList(esperaA0002));

            service.cancelarSolicitacao(aluno, DISC, PER, TURMA);

            ArgumentCaptor<MatriculaTurma> captor = ArgumentCaptor.forClass(MatriculaTurma.class);
            verify(matriculaRepository, times(2)).atualizar(captor.capture());

            assertCanceladaEPromoveuConfirmada(captor, "A0001", "A0002");
        }

        @Test
        @DisplayName("13.2 Se lista de espera vazia ao cancelar PENDENTE, deve apenas cancelar")
        void aoCancelarPendenteComListaVaziaDeveApenasCancelar() throws Exception {
            MatriculaTurma solicitacao = matriculaPendente("A0001");

            when(matriculaRepository.buscarPorChaveUnica("A0001", DISC, PER, TURMA)).thenReturn(solicitacao);
            when(turmaRepository.buscarPorChaveUnica(DISC, PER, TURMA)).thenReturn(turma);
            when(matriculaRepository.contarOcupadasPorTurma(DISC, PER, TURMA)).thenReturn(0L);
            when(matriculaRepository.listarListaEsperaPorTurmaOrdenada(DISC, PER, TURMA))
                    .thenReturn(Collections.emptyList());

            service.cancelarSolicitacao(aluno, DISC, PER, TURMA);

            verify(matriculaRepository, times(1)).atualizar(any(MatriculaTurma.class));
        }

        @Test
        @DisplayName("13.3 Deve promover o mais antigo quando há vários na lista de espera")
        void devePromoverOMaisAntigoQuandoVariosNaListaDeEspera() throws Exception {
            MatriculaTurma solicitacao = matriculaPendente("A0001");
            MatriculaTurma esperaMaisAntiga = matriculaEmEspera("A0002", LocalDateTime.now().minusHours(3));
            MatriculaTurma esperaMaisNova = matriculaEmEspera("A0003", LocalDateTime.now().minusMinutes(5));

            when(matriculaRepository.buscarPorChaveUnica("A0001", DISC, PER, TURMA)).thenReturn(solicitacao);
            when(turmaRepository.buscarPorChaveUnica(DISC, PER, TURMA)).thenReturn(turma);
            when(matriculaRepository.contarOcupadasPorTurma(DISC, PER, TURMA)).thenReturn(0L);
            when(matriculaRepository.listarListaEsperaPorTurmaOrdenada(DISC, PER, TURMA))
                    .thenReturn(Arrays.asList(esperaMaisNova, esperaMaisAntiga));

            service.cancelarSolicitacao(aluno, DISC, PER, TURMA);

            assertPromoveuAlunoMaisAntigoDaListaEspera("A0002");
        }

        @Test
        @DisplayName("13.4 Se turma ainda cheia após cancelar PENDENTE, não deve promover")
        void seTurmaAindaCheiaAposCancelarPendenteNaoDevePromover() throws Exception {
            MatriculaTurma solicitacao = matriculaPendente("A0001");
            Turma turmaCheia = new Turma(TURMA, DISC, PER, 1, "Seg 10h", "A101", "P0001");

            when(matriculaRepository.buscarPorChaveUnica("A0001", DISC, PER, TURMA)).thenReturn(solicitacao);
            when(turmaRepository.buscarPorChaveUnica(DISC, PER, TURMA)).thenReturn(turmaCheia);
            when(matriculaRepository.contarOcupadasPorTurma(DISC, PER, TURMA)).thenReturn(1L);

            service.cancelarSolicitacao(aluno, DISC, PER, TURMA);

            verify(matriculaRepository, times(1)).atualizar(any(MatriculaTurma.class));
            verify(matriculaRepository, never()).listarListaEsperaPorTurmaOrdenada(any(), any(), any());
        }
    }

    // =========================================================================
    // 14. RF18 — PRÉ-REQUISITOS
    // =========================================================================

    @Nested
    @DisplayName("14. RF18 - Validação de pré-requisitos")
    class ValidacaoPreRequisitos {

        @Test
        @DisplayName("14.1 Não deve matricular aluno que não foi aprovado no pré-requisito")
        void naoDeveMatricularSemPreRequisitoAprovado() {
            mockTurmaExiste();
            mockPeriodoAtivo();
            mockSemSolicitacaoAtiva();

            when(disciplinaRepository.buscarPorCodigo(DISC))
                    .thenReturn(new Disciplina(DISC, "Cálculo II", 60, 4, Collections.singletonList("MAT000")));

            when(historicoRepository.buscarPorAluno("A0001")).thenReturn(Collections.emptyList());

            Exception ex = assertThrows(Exception.class, () -> service.solicitarMatricula(aluno, DISC, PER, TURMA));

            assertTrue(ex.getMessage().contains("MAT000"));
            verify(matriculaRepository, never()).salvar(any());
        }

        @Test
        @DisplayName("14.2 Deve matricular aluno aprovado em todos os pré-requisitos")
        void devePermitirQuandoAprovadoEmTodosPreRequisitos() {
            mockTurmaExiste();
            mockPeriodoAtivo();
            mockSemSolicitacaoAtiva();
            mockVagasOcupadas(0L);
            mockSemChoqueDeHorario();

            when(disciplinaRepository.buscarPorCodigo(DISC))
                    .thenReturn(new Disciplina(DISC, "Cálculo II", 60, 4, Collections.singletonList("MAT000")));

            Historico historico = new Historico("A0001", "MAT000", 8.0, true);
            when(historicoRepository.buscarPorAluno("A0001")).thenReturn(Collections.singletonList(historico));

            assertDoesNotThrow(() -> service.solicitarMatricula(aluno, DISC, PER, TURMA));

            verify(matriculaRepository).salvar(any(MatriculaTurma.class));
        }

        @Test
        @DisplayName("14.3 Não deve matricular se houver múltiplos pré-requisitos e apenas um for cumprido")
        void naoDevePermitirQuandoApenasUmPreRequisitoCumprido() {
            mockTurmaExiste();
            mockPeriodoAtivo();
            mockSemSolicitacaoAtiva();

            when(disciplinaRepository.buscarPorCodigo(DISC))
                    .thenReturn(new Disciplina(DISC, "Cálculo III", 60, 4, Arrays.asList("MAT000", "MAT100")));

            Historico historicoAprovado = new Historico("A0001", "MAT000", 8.0, true);
            when(historicoRepository.buscarPorAluno("A0001")).thenReturn(Collections.singletonList(historicoAprovado));

            Exception ex = assertThrows(Exception.class, () -> service.solicitarMatricula(aluno, DISC, PER, TURMA));

            assertTrue(ex.getMessage().contains("MAT100"));
            verify(matriculaRepository, never()).salvar(any());
        }

        @Test
        @DisplayName("14.4 Não deve matricular se o aluno foi reprovado no pré-requisito")
        void naoDevePermitirQuandoReprovadoNoPreRequisito() {
            mockTurmaExiste();
            mockPeriodoAtivo();
            mockSemSolicitacaoAtiva();

            when(disciplinaRepository.buscarPorCodigo(DISC))
                    .thenReturn(new Disciplina(DISC, "Cálculo II", 60, 4, Collections.singletonList("MAT000")));

            Historico historicoReprovado = new Historico("A0001", "MAT000", 3.0, false);
            when(historicoRepository.buscarPorAluno("A0001")).thenReturn(Collections.singletonList(historicoReprovado));

            Exception ex = assertThrows(Exception.class, () -> service.solicitarMatricula(aluno, DISC, PER, TURMA));

            assertTrue(ex.getMessage().contains("MAT000"));
            verify(matriculaRepository, never()).salvar(any());
        }

        @Test
        @DisplayName("14.5 Lista de pré-requisitos vazia não deve impedir matrícula")
        void listaPreRequisitosVaziaNaoDeveImpedir() {
            mockCenarioSucesso();

            assertDoesNotThrow(() -> service.solicitarMatricula(aluno, DISC, PER, TURMA));

            verify(matriculaRepository).salvar(any(MatriculaTurma.class));
            verify(historicoRepository, never()).buscarPorAluno(any());
        }
    }

    // =========================================================================
    // 15. RF19 — CHOQUE DE HORÁRIO
    // =========================================================================

    @Nested
    @DisplayName("15. RF19 - Validação de choque de horário")
    class ValidacaoChoqueHorario {

        @Test
        @DisplayName("15.1 Não deve matricular se houver choque de horário com turma já matriculada")
        void naoDeveMatricularComChoqueDeHorario() {
            mockTurmaExiste();
            mockPeriodoAtivo();
            mockSemSolicitacaoAtiva();
            mockDisciplinaSemPreRequisitos();

            MatriculaTurma matriculaExistente = new MatriculaTurma("A0001", "FIS001", PER, "T02");
            matriculaExistente.setStatus(StatusMatricula.CONFIRMADA);

            when(matriculaRepository.listarPorAluno("A0001")).thenReturn(Collections.singletonList(matriculaExistente));

            Turma turmaConflitante = new Turma("T02", "FIS001", PER, 30, "Seg/Qua 10h-12h", "Lab Fis", "P0002");
            when(turmaRepository.buscarPorChaveUnica("FIS001", PER, "T02")).thenReturn(turmaConflitante);

            Exception ex = assertThrows(Exception.class, () -> service.solicitarMatricula(aluno, DISC, PER, TURMA));

            assertTrue(ex.getMessage().contains("choque de horário"));
            verify(matriculaRepository, never()).salvar(any());
        }

        @Test
        @DisplayName("15.2 Deve permitir matrícula quando não houver choque de horário")
        void devePermitirQuandoNaoHaChoqueDeHorario() {
            mockCenarioSucesso();

            MatriculaTurma matriculaExistente = new MatriculaTurma("A0001", "FIS001", PER, "T02");
            matriculaExistente.setStatus(StatusMatricula.CONFIRMADA);

            when(matriculaRepository.listarPorAluno("A0001")).thenReturn(Collections.singletonList(matriculaExistente));

            Turma turmaSemConflito = new Turma("T02", "FIS001", PER, 30, "Ter/Qui 14h-16h", "Lab Fis", "P0002");
            when(turmaRepository.buscarPorChaveUnica("FIS001", PER, "T02")).thenReturn(turmaSemConflito);

            assertDoesNotThrow(() -> service.solicitarMatricula(aluno, DISC, PER, TURMA));

            verify(matriculaRepository).salvar(any(MatriculaTurma.class));
        }

        @Test
        @DisplayName("15.3 Matrículas CANCELADA ou REJEITADA não devem ser consideradas no choque de horário")
        void naoDeveConsiderarMatriculasCanceladasOuRejeitadasNoChoque() {
            mockCenarioSucesso();

            MatriculaTurma cancelada = new MatriculaTurma("A0001", "FIS001", PER, "T02");
            cancelada.setStatus(StatusMatricula.CANCELADA);

            MatriculaTurma rejeitada = new MatriculaTurma("A0001", "HIS001", PER, "T03");
            rejeitada.setStatus(StatusMatricula.REJEITADA);

            when(matriculaRepository.listarPorAluno("A0001")).thenReturn(Arrays.asList(cancelada, rejeitada));

            assertDoesNotThrow(() -> service.solicitarMatricula(aluno, DISC, PER, TURMA));

            verify(matriculaRepository).salvar(any(MatriculaTurma.class));
            verify(turmaRepository, never()).buscarPorChaveUnica("FIS001", PER, "T02");
            verify(turmaRepository, never()).buscarPorChaveUnica("HIS001", PER, "T03");
        }

        @Test
        @DisplayName("15.4 Turma de matrícula existente não encontrada deve ser ignorada na verificação de choque")
        void turmaExistenteNaoEncontradaDeveSerIgnorada() {
            mockCenarioSucesso();

            MatriculaTurma matriculaExistente = new MatriculaTurma("A0001", "FIS001", PER, "T02");
            matriculaExistente.setStatus(StatusMatricula.CONFIRMADA);

            when(matriculaRepository.listarPorAluno("A0001")).thenReturn(Collections.singletonList(matriculaExistente));
            when(turmaRepository.buscarPorChaveUnica("FIS001", PER, "T02")).thenReturn(null);

            assertDoesNotThrow(() -> service.solicitarMatricula(aluno, DISC, PER, TURMA));

            verify(matriculaRepository).salvar(any(MatriculaTurma.class));
        }
    }

    // =========================================================================
    // 16. GESTÃO PELO COORDENADOR — LISTAGENS ADICIONAIS E PERMISSÕES
    // =========================================================================

    @Nested
    @DisplayName("16. Gestão pelo coordenador - listagens adicionais e permissões")
    class GestaoCoordenadorAdicional {

        @Test
        @DisplayName("16.1 Coordenador deve listar todas as solicitações independente do status")
        void deveListarTodasAsSolicitacoes() throws Exception {
            MatriculaTurma m1 = new MatriculaTurma("A01", "D1", "P1", "T1");
            MatriculaTurma m2 = new MatriculaTurma("A02", "D1", "P1", "T1");
            m2.setStatus(StatusMatricula.CONFIRMADA);

            when(matriculaRepository.listarTodas()).thenReturn(Arrays.asList(m1, m2));

            List<MatriculaTurma> todas = service.listarTodasSolicitacoes(coordenador);

            assertEquals(2, todas.size());
            verify(matriculaRepository).listarTodas();
        }

        @Test
        @DisplayName("16.2 Não-coordenador não pode listar todas as solicitações")
        void naoCoordenadorNaoPodeListarTodas() {
            assertThrows(Exception.class, () -> service.listarTodasSolicitacoes(aluno));
        }

        @Test
        @DisplayName("16.3 Coordenador deve listar solicitações filtrando por status")
        void deveListarSolicitacoesPorStatus() throws Exception {
            MatriculaTurma pendente = new MatriculaTurma("A01", "D1", "P1", "T1");
            MatriculaTurma confirmada = new MatriculaTurma("A02", "D1", "P1", "T1");
            confirmada.setStatus(StatusMatricula.CONFIRMADA);

            when(matriculaRepository.listarTodas()).thenReturn(Arrays.asList(pendente, confirmada));

            List<MatriculaTurma> confirmadas = service.listarSolicitacoesPorStatus(coordenador,
                    StatusMatricula.CONFIRMADA);

            assertEquals(1, confirmadas.size());
            assertEquals("A02", confirmadas.get(0).getMatriculaAluno());
        }

        @Test
        @DisplayName("16.4 Não-coordenador não pode listar solicitações por status")
        void naoCoordenadorNaoPodeListarPorStatus() {
            assertThrows(Exception.class, () -> service.listarSolicitacoesPorStatus(aluno, StatusMatricula.PENDENTE));
        }

        @Test
        @DisplayName("16.5 Coordenador deve listar solicitações de uma turma específica")
        void deveListarSolicitacoesPorTurma() throws Exception {
            MatriculaTurma m1 = new MatriculaTurma("A01", DISC, PER, TURMA);
            MatriculaTurma m2 = new MatriculaTurma("A02", DISC, PER, TURMA);
            m2.setStatus(StatusMatricula.LISTA_ESPERA);

            mockTurmaExiste();
            when(matriculaRepository.listarPorTurma(DISC, PER, TURMA)).thenReturn(Arrays.asList(m1, m2));

            List<MatriculaTurma> resultado = service.listarSolicitacoesPorTurma(coordenador, DISC, PER, TURMA);

            assertEquals(2, resultado.size());
            verify(matriculaRepository).listarPorTurma(DISC, PER, TURMA);
            verify(matriculaRepository, never()).listarListaEsperaPorTurmaOrdenada(any(), any(), any());
        }

        @Test
        @DisplayName("16.6 Não-coordenador não pode listar solicitações de uma turma")
        void naoCoordenadorNaoPodeListarPorTurma() {
            assertThrows(Exception.class, () -> service.listarSolicitacoesPorTurma(aluno, DISC, PER, TURMA));
        }

        @Test
        @DisplayName("16.7 Turma inexistente não deve listar solicitações")
        void turmaInexistenteNaoDeveListarSolicitacoes() {
            when(turmaRepository.buscarPorChaveUnica(DISC, PER, TURMA)).thenReturn(null);

            assertThrows(Exception.class, () -> service.listarSolicitacoesPorTurma(coordenador, DISC, PER, TURMA));
            verify(matriculaRepository, never()).listarPorTurma(any(), any(), any());
        }

        @Test
        @DisplayName("16.8 Usuário null não pode aprovar matrícula")
        void usuarioNuloNaoPodeAprovar() {
            assertThrows(Exception.class, () -> service.aprovarMatricula(null, "A01", DISC, PER, TURMA));
            verify(matriculaRepository, never()).atualizar(any());
        }

        @Test
        @DisplayName("16.9 Aluno não pode negar matrícula")
        void alunoNaoPodeNegarMatricula() {
            assertThrows(Exception.class, () -> service.negarMatricula(aluno, "A01", DISC, PER, TURMA));
            verify(matriculaRepository, never()).atualizar(any());
        }

        @Test
        @DisplayName("16.10 Não deve negar solicitação inexistente")
        void naoDeveNegarSolicitacaoInexistente() {
            when(matriculaRepository.buscarPorChaveUnica("A99", DISC, PER, TURMA)).thenReturn(null);

            Exception ex = assertThrows(Exception.class,
                    () -> service.negarMatricula(coordenador, "A99", DISC, PER, TURMA));

            assertTrue(ex.getMessage().contains("não encontrada"));
            verify(matriculaRepository, never()).atualizar(any());
        }

        @Test
        @DisplayName("16.11 Não deve negar solicitação que não esteja PENDENTE")
        void naoDeveNegarSolicitacaoNaoPendente() {
            MatriculaTurma confirmada = new MatriculaTurma("A01", DISC, PER, TURMA);
            confirmada.setStatus(StatusMatricula.CONFIRMADA);

            when(matriculaRepository.buscarPorChaveUnica("A01", DISC, PER, TURMA)).thenReturn(confirmada);

            Exception ex = assertThrows(Exception.class,
                    () -> service.negarMatricula(coordenador, "A01", DISC, PER, TURMA));

            assertTrue(ex.getMessage().contains("PENDENTES"));
            verify(matriculaRepository, never()).atualizar(any());
        }
    }

    // =========================================================================
    // 17. RF23 — CHAMADA EXPLICITA DA LISTA DE ESPERA (chamarProximoDaListaEspera)
    // =========================================================================

    @Nested
    @DisplayName("17. RF23 - Chamada explicita da lista de espera")
    class ChamadaExplicitaListaEspera {

        @Test
        @DisplayName("17.1 Deve promover o primeiro da lista de espera quando ha vaga disponivel")
        void devePromoverQuandoHaVagaDisponivel() {
            MatriculaTurma espera = matriculaEmEspera("A0002", LocalDateTime.now().minusMinutes(30));

            when(turmaRepository.buscarPorChaveUnica(DISC, PER, TURMA)).thenReturn(turma);
            when(matriculaRepository.contarOcupadasPorTurma(DISC, PER, TURMA)).thenReturn(0L);
            when(matriculaRepository.listarListaEsperaPorTurmaOrdenada(DISC, PER, TURMA))
                    .thenReturn(Collections.singletonList(espera));

            service.chamarProximoDaListaEspera(DISC, PER, TURMA);

            ArgumentCaptor<MatriculaTurma> captor = ArgumentCaptor.forClass(MatriculaTurma.class);
            verify(matriculaRepository, times(1)).atualizar(captor.capture());
            assertEquals(StatusMatricula.CONFIRMADA, captor.getValue().getStatus());
            assertEquals("A0002", captor.getValue().getMatriculaAluno());
        }

        @Test
        @DisplayName("17.2 Nao deve promover quando turma esta cheia")
        void naoDevePromoverQuandoTurmaCheiaExplicit() {
            when(turmaRepository.buscarPorChaveUnica(DISC, PER, TURMA)).thenReturn(turma);
            when(matriculaRepository.contarOcupadasPorTurma(DISC, PER, TURMA)).thenReturn(40L);

            service.chamarProximoDaListaEspera(DISC, PER, TURMA);

            verify(matriculaRepository, never()).atualizar(any());
            verify(matriculaRepository, never()).listarListaEsperaPorTurmaOrdenada(any(), any(), any());
        }

        @Test
        @DisplayName("17.3 Nao deve lancar excecao quando turma nao existe")
        void naoDeveLancarExcecaoQuandoTurmaNaoExiste() {
            when(turmaRepository.buscarPorChaveUnica(DISC, PER, TURMA)).thenReturn(null);

            assertDoesNotThrow(() -> service.chamarProximoDaListaEspera(DISC, PER, TURMA));
            verify(matriculaRepository, never()).atualizar(any());
        }

        @Test
        @DisplayName("17.4 Deve promover o mais antigo quando ha varios na lista de espera")
        void devePromoverOMaisAntigoExplicit() {
            MatriculaTurma esperaMaisAntiga = matriculaEmEspera("A0002", LocalDateTime.now().minusHours(3));
            MatriculaTurma esperaMaisNova = matriculaEmEspera("A0003", LocalDateTime.now().minusMinutes(10));

            when(turmaRepository.buscarPorChaveUnica(DISC, PER, TURMA)).thenReturn(turma);
            when(matriculaRepository.contarOcupadasPorTurma(DISC, PER, TURMA)).thenReturn(0L);
            when(matriculaRepository.listarListaEsperaPorTurmaOrdenada(DISC, PER, TURMA))
                    .thenReturn(Arrays.asList(esperaMaisNova, esperaMaisAntiga));

            service.chamarProximoDaListaEspera(DISC, PER, TURMA);

            ArgumentCaptor<MatriculaTurma> captor = ArgumentCaptor.forClass(MatriculaTurma.class);
            verify(matriculaRepository, times(1)).atualizar(captor.capture());
            assertEquals("A0002", captor.getValue().getMatriculaAluno(),
                    "Deve promover o aluno com solicitacao mais antiga");
            assertEquals(StatusMatricula.CONFIRMADA, captor.getValue().getStatus());
        }

        @Test
        @DisplayName("17.5 Nao deve fazer nada quando lista de espera esta vazia")
        void naoDeveFazerNadaComListaVaziaExplicit() {
            when(turmaRepository.buscarPorChaveUnica(DISC, PER, TURMA)).thenReturn(turma);
            when(matriculaRepository.contarOcupadasPorTurma(DISC, PER, TURMA)).thenReturn(0L);
            when(matriculaRepository.listarListaEsperaPorTurmaOrdenada(DISC, PER, TURMA)).thenReturn(Collections.emptyList());

            service.chamarProximoDaListaEspera(DISC, PER, TURMA);

            verify(matriculaRepository, never()).atualizar(any());
        }
    }

    /**
     * Verifica que a primeira matrícula capturada foi cancelada e que a segunda foi promovida para CONFIRMADA.
     */
    private void assertCanceladaEPromoveuConfirmada(ArgumentCaptor<MatriculaTurma> captor, String matriculaCancelada,
            String matriculaPromovida) {
        List<MatriculaTurma> atualizadas = captor.getAllValues();

        assertEquals(StatusMatricula.CANCELADA, atualizadas.get(0).getStatus());
        assertEquals(matriculaCancelada, atualizadas.get(0).getMatriculaAluno());

        assertEquals(StatusMatricula.CONFIRMADA, atualizadas.get(1).getStatus());
        assertEquals(matriculaPromovida, atualizadas.get(1).getMatriculaAluno());
    }

    /**
     * Verifica que, ao promover alguém da lista de espera, o aluno com a solicitação mais antiga foi escolhido e teve
     * sua matrícula confirmada.
     */
    private void assertPromoveuAlunoMaisAntigoDaListaEspera(String matriculaAlunoMaisAntigo) {
        ArgumentCaptor<MatriculaTurma> captor = ArgumentCaptor.forClass(MatriculaTurma.class);
        verify(matriculaRepository, times(2)).atualizar(captor.capture());

        MatriculaTurma promovida = captor.getAllValues().get(1);
        assertEquals(matriculaAlunoMaisAntigo, promovida.getMatriculaAluno(),
                "Deve promover o aluno com solicitação mais antiga");
        assertEquals(StatusMatricula.CONFIRMADA, promovida.getStatus());
    }
}
