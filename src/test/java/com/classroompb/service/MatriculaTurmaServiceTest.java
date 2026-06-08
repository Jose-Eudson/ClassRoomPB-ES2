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
import com.classroompb.repository.MatriculaTurmaRepository;
import com.classroompb.repository.PeriodoLetivoRepository;
import com.classroompb.repository.TurmaRepository;

/**
 * RF16 — Testes unitários: Aluno solicita matrícula em turma.
 *
 * Cobertura:
 *   1. Solicitar matrícula — casos de sucesso
 *   2. Controle de permissão (todos os perfis não-aluno + null)
 *   3. Validação de campos obrigatórios (null, vazio, só espaços)
 *   4. Regras de negócio: turma inexistente, período inativo/nulo,
 *      solicitação duplicada, vagas esgotadas, exatamente uma vaga
 *   5. Normalização de entradas (trim de espaços)
 *   6. Conteúdo e integridade do objeto salvo
 *   7. Cancelar solicitação — casos de sucesso e falhas
 *   8. Listar solicitações do aluno
 *   9. Consultar vagas disponíveis
 *   10. 
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RF16 - Aluno solicita matrícula em turma")
public class MatriculaTurmaServiceTest {

    @Mock private MatriculaTurmaRepository matriculaRepository;
    @Mock private TurmaRepository turmaRepository;
    @Mock private PeriodoLetivoRepository periodoRepository;
    @Mock private DisciplinaRepository disciplinaRepository;
    @Mock private HistoricoService historicoService;

    private MatriculaTurmaService service;

    // -------------------------------------------------------------------------
    // Fixtures compartilhadas
    // -------------------------------------------------------------------------

    private Aluno aluno;
    private Aluno outroAluno;
    private Coordenador coordenador;
    private Professor professor;
    private Administrador administrador;

    private Turma turma;
    private PeriodoLetivo periodoAtivo;
    private PeriodoLetivo periodoInativo;

    private static final String DISC  = "MAT001";
    private static final String PER   = "2026.1";
    private static final String TURMA = "T01";


    @BeforeEach
    void setUp() {
        service = new MatriculaTurmaService(matriculaRepository, turmaRepository, periodoRepository, disciplinaRepository, historicoService);

        aluno         = new Aluno("A0001", "João Silva",  "joao@test.com",  "senha123");
        outroAluno    = new Aluno("A0002", "Maria Souza", "maria@test.com", "senha456");
        coordenador   = new Coordenador("C0001", "Coord", "coord@test.com", "senha123");
        professor     = new Professor("P0001",  "Prof",   "prof@test.com",  "senha123");
        administrador = new Administrador("ADM001", "Admin", "adm@test.com", "senha123");

        turma = new Turma(TURMA, DISC, PER, 40, "Seg/Qua 10h-12h", "Bloco A-101", "P0001");

        periodoAtivo   = new PeriodoLetivo(PER,    2026, 1,
                LocalDate.of(2026, 2, 1),  LocalDate.of(2026, 6, 30), true);
        periodoInativo = new PeriodoLetivo("2025.2", 2025, 2,
                LocalDate.of(2025, 8, 1),  LocalDate.of(2025, 12, 20), false);
    }

    // helpers para não repetir o setup de mocks de sucesso
    private void mockTurmaExiste() {
        when(turmaRepository.buscarPorChaveUnica(DISC, PER, TURMA)).thenReturn(turma);
    }

    private void mockPeriodoAtivo() {
        when(periodoRepository.buscarPorCodigo(PER)).thenReturn(periodoAtivo);
    }

    private void mockSemSolicitacaoAtiva() {
        when(matriculaRepository.existeSolicitacaoAtiva("A0001", DISC, PER, TURMA)).thenReturn(false);
    }

    private void mockVagasDisponiveis(long confirmadas) {
        when(matriculaRepository.contarOcupadasPorTurma(DISC, PER, TURMA)).thenReturn(confirmadas);
    }

    private void mockCenarioSucesso() {
        mockTurmaExiste();
        mockPeriodoAtivo();
        mockSemSolicitacaoAtiva();
        mockVagasDisponiveis(0L);
    }

    // =========================================================================
    // 1. SOLICITAR MATRÍCULA — SUCESSO
    // =========================================================================

    @Nested
    @DisplayName("1. Solicitar matrícula — casos de sucesso")
    class SolicitarMatriculaSucesso {

        @Test
        @DisplayName("1.1 Deve registrar solicitação com status PENDENTE")
        void deveSalvarSolicitacaoComStatusPendente() {
            mockCenarioSucesso();

            assertDoesNotThrow(() -> service.solicitarMatricula(aluno, DISC, PER, TURMA));

            ArgumentCaptor<MatriculaTurma> captor = ArgumentCaptor.forClass(MatriculaTurma.class);
            verify(matriculaRepository).salvar(captor.capture());
            assertEquals(StatusMatricula.PENDENTE, captor.getValue().getStatus(),
                    "Status inicial deve ser PENDENTE");
        }

        @Test
        @DisplayName("1.2 O objeto salvo deve conter os dados corretos do aluno e da turma")
        void objetoSalvoDeveConterDadosCorretos() throws Exception {
            mockCenarioSucesso();

            service.solicitarMatricula(aluno, DISC, PER, TURMA);

            ArgumentCaptor<MatriculaTurma> captor = ArgumentCaptor.forClass(MatriculaTurma.class);
            verify(matriculaRepository).salvar(captor.capture());

            MatriculaTurma salva = captor.getValue();
            assertNotNull(salva,                                  "Objeto salvo não pode ser nulo");
            assertEquals("A0001", salva.getMatriculaAluno(),     "Matrícula do aluno");
            assertEquals(DISC,    salva.getCodigoDisciplina(),   "Código da disciplina");
            assertEquals(PER,     salva.getCodigoPeriodo(),      "Código do período");
            assertEquals(TURMA,   salva.getCodigoTurma(),        "Código da turma");
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
            assertNotNull(dataSolicitacao, "dataSolicitacao não pode ser nula");
            assertFalse(dataSolicitacao.isBefore(antes),
                    "dataSolicitacao deve ser >= momento anterior à chamada");
        }

        @Test
        @DisplayName("1.4 Deve aceitar solicitação quando resta exatamente uma vaga")
        void deveAceitarComExatamenteUmaVagaRestante() {
            mockTurmaExiste();
            mockPeriodoAtivo();
            mockSemSolicitacaoAtiva();
            mockVagasDisponiveis(39L); // turma tem 40 vagas; 39 confirmadas → 1 disponível

            assertDoesNotThrow(() -> service.solicitarMatricula(aluno, DISC, PER, TURMA));
            verify(matriculaRepository).salvar(any(MatriculaTurma.class));
        }

        @Test
        @DisplayName("1.5 Aluno pode solicitar matrícula em disciplinas diferentes no mesmo período")
        void podeMatricularEmDisciplinasDiferentes() {
            Turma turmaFis = new Turma("T01", "FIS001", PER, 30, "Ter/Qui 14h-16h", "Lab Fis", "P0002");

            mockCenarioSucesso();
            assertDoesNotThrow(() -> service.solicitarMatricula(aluno, DISC, PER, TURMA));

            when(turmaRepository.buscarPorChaveUnica("FIS001", PER, "T01")).thenReturn(turmaFis);
            when(matriculaRepository.existeSolicitacaoAtiva("A0001", "FIS001", PER, "T01")).thenReturn(false);
            when(matriculaRepository.contarOcupadasPorTurma("FIS001", PER, "T01")).thenReturn(0L);

            assertDoesNotThrow(() -> service.solicitarMatricula(aluno, "FIS001", PER, "T01"));
        }

        @Test
        @DisplayName("1.6 Alunos diferentes podem solicitar matrícula na mesma turma")
        void alunosDiferentesPodemSolicitarMesmaTurma() {
            mockCenarioSucesso();
            assertDoesNotThrow(() -> service.solicitarMatricula(aluno, DISC, PER, TURMA));

            when(matriculaRepository.existeSolicitacaoAtiva("A0002", DISC, PER, TURMA)).thenReturn(false);
            assertDoesNotThrow(() -> service.solicitarMatricula(outroAluno, DISC, PER, TURMA));
        }

        @Test
        @DisplayName("1.7 Entradas com espaços extras devem ser normalizadas (trim)")
        void deveNormalizarEspacosNaEntrada() throws Exception {
            mockCenarioSucesso(); // mocks para códigos SEM espaço

            service.solicitarMatricula(aluno, "  " + DISC + "  ", "  " + PER + "  ", "  " + TURMA + "  ");

            ArgumentCaptor<MatriculaTurma> captor = ArgumentCaptor.forClass(MatriculaTurma.class);
            verify(matriculaRepository).salvar(captor.capture());

            MatriculaTurma salva = captor.getValue();
            assertEquals(DISC,  salva.getCodigoDisciplina(), "Disciplina deve ser trimada");
            assertEquals(PER,   salva.getCodigoPeriodo(),    "Período deve ser trimado");
            assertEquals(TURMA, salva.getCodigoTurma(),      "Turma deve ser trimada");
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
            Exception ex = assertThrows(Exception.class, () ->
                    service.solicitarMatricula(coordenador, DISC, PER, TURMA));
            assertTrue(ex.getMessage().contains("Apenas alunos"),
                    "Mensagem deve mencionar 'Apenas alunos'");
            verify(matriculaRepository, never()).salvar(any());
        }

        @Test
        @DisplayName("2.2 Professor não pode solicitar matrícula")
        void professorNaoPodeSolicitar() {
            Exception ex = assertThrows(Exception.class, () ->
                    service.solicitarMatricula(professor, DISC, PER, TURMA));
            assertTrue(ex.getMessage().contains("Apenas alunos"));
            verify(matriculaRepository, never()).salvar(any());
        }

        @Test
        @DisplayName("2.3 Administrador não pode solicitar matrícula")
        void adminNaoPodeSolicitar() {
            Exception ex = assertThrows(Exception.class, () ->
                    service.solicitarMatricula(administrador, DISC, PER, TURMA));
            assertTrue(ex.getMessage().contains("Apenas alunos"));
            verify(matriculaRepository, never()).salvar(any());
        }

        @Test
        @DisplayName("2.4 Usuário null deve lançar exceção antes de qualquer consulta ao repositório")
        void usuarioNuloDeveLancarExcecaoImediatamente() {
            Exception ex = assertThrows(Exception.class, () ->
                    service.solicitarMatricula(null, DISC, PER, TURMA));
            assertTrue(ex.getMessage().contains("Apenas alunos"));
            verify(turmaRepository, never()).buscarPorChaveUnica(any(), any(), any());
            verify(matriculaRepository, never()).salvar(any());
        }
    }

    // =========================================================================
    // 3. VALIDAÇÃO DE CAMPOS OBRIGATÓRIOS
    // =========================================================================

    @Nested
    @DisplayName("3. Validação de campos obrigatórios")
    class ValidacaoCampos {

        // --- Código de disciplina ---

        @Test
        @DisplayName("3.1 Código de disciplina null deve lançar exceção")
        void disciplinaNulaDeveLancar() {
            Exception ex = assertThrows(Exception.class, () ->
                    service.solicitarMatricula(aluno, null, PER, TURMA));
            assertTrue(ex.getMessage().toLowerCase().contains("disciplina"));
            verify(matriculaRepository, never()).salvar(any());
        }

        @Test
        @DisplayName("3.2 Código de disciplina vazio deve lançar exceção")
        void disciplinaVaziaDeveLancar() {
            Exception ex = assertThrows(Exception.class, () ->
                    service.solicitarMatricula(aluno, "", PER, TURMA));
            assertTrue(ex.getMessage().toLowerCase().contains("disciplina"));
            verify(matriculaRepository, never()).salvar(any());
        }

        @Test
        @DisplayName("3.3 Código de disciplina só com espaços deve lançar exceção")
        void disciplinaSoEspacosDeveLancar() {
            Exception ex = assertThrows(Exception.class, () ->
                    service.solicitarMatricula(aluno, "   ", PER, TURMA));
            assertTrue(ex.getMessage().toLowerCase().contains("disciplina"));
            verify(matriculaRepository, never()).salvar(any());
        }

        // --- Código de período ---

        @Test
        @DisplayName("3.4 Código de período null deve lançar exceção")
        void periodoNuloDeveLancar() {
            Exception ex = assertThrows(Exception.class, () ->
                    service.solicitarMatricula(aluno, DISC, null, TURMA));
            assertTrue(ex.getMessage().toLowerCase().contains("período"));
            verify(matriculaRepository, never()).salvar(any());
        }

        @Test
        @DisplayName("3.5 Código de período vazio deve lançar exceção")
        void periodoVazioDeveLancar() {
            Exception ex = assertThrows(Exception.class, () ->
                    service.solicitarMatricula(aluno, DISC, "", TURMA));
            assertTrue(ex.getMessage().toLowerCase().contains("período"));
            verify(matriculaRepository, never()).salvar(any());
        }

        @Test
        @DisplayName("3.6 Código de período só com espaços deve lançar exceção")
        void periodoSoEspacosDeveLancar() {
            Exception ex = assertThrows(Exception.class, () ->
                    service.solicitarMatricula(aluno, DISC, "   ", TURMA));
            assertTrue(ex.getMessage().toLowerCase().contains("período"));
            verify(matriculaRepository, never()).salvar(any());
        }

        // --- Código de turma ---

        @Test
        @DisplayName("3.7 Código de turma null deve lançar exceção")
        void turmaNulaDeveLancar() {
            Exception ex = assertThrows(Exception.class, () ->
                    service.solicitarMatricula(aluno, DISC, PER, null));
            assertTrue(ex.getMessage().toLowerCase().contains("turma"));
            verify(matriculaRepository, never()).salvar(any());
        }

        @Test
        @DisplayName("3.8 Código de turma vazio deve lançar exceção")
        void turmaVaziaDeveLancar() {
            Exception ex = assertThrows(Exception.class, () ->
                    service.solicitarMatricula(aluno, DISC, PER, ""));
            assertTrue(ex.getMessage().toLowerCase().contains("turma"));
            verify(matriculaRepository, never()).salvar(any());
        }

        @Test
        @DisplayName("3.9 Código de turma só com espaços deve lançar exceção")
        void turmaSoEspacosDeveLancar() {
            Exception ex = assertThrows(Exception.class, () ->
                    service.solicitarMatricula(aluno, DISC, PER, "   "));
            assertTrue(ex.getMessage().toLowerCase().contains("turma"));
            verify(matriculaRepository, never()).salvar(any());
        }

        @Test
        @DisplayName("3.10 Validação de campos ocorre antes de consultar repositórios")
        void validacaoCamposOcorreAntesDosRepositorios() {
            assertThrows(Exception.class, () ->
                    service.solicitarMatricula(aluno, null, PER, TURMA));
            verify(turmaRepository, never()).buscarPorChaveUnica(any(), any(), any());
            verify(periodoRepository, never()).buscarPorCodigo(any());
        }
    }

    // =========================================================================
    // 4. REGRAS DE NEGÓCIO — IMPEDIMENTOS
    // =========================================================================

    @Nested
    @DisplayName("4. Regras de negócio — impedimentos")
    class RegrasNegocio {

        @Test
        @DisplayName("4.1 Turma inexistente deve lançar exceção")
        void turmaInexistenteDeveLancar() {
            when(turmaRepository.buscarPorChaveUnica(DISC, PER, "T99")).thenReturn(null);

            Exception ex = assertThrows(Exception.class, () ->
                    service.solicitarMatricula(aluno, DISC, PER, "T99"));

            assertTrue(ex.getMessage().contains("não encontrada"),
                    "Mensagem deve indicar que a turma não foi encontrada");
            verify(matriculaRepository, never()).salvar(any());
        }

        @Test
        @DisplayName("4.2 Mensagem de turma não encontrada deve citar os identificadores informados")
        void mensagemTurmaInexistenteDeveConterIdentificadores() {
            when(turmaRepository.buscarPorChaveUnica("FIS999", PER, "T99")).thenReturn(null);

            Exception ex = assertThrows(Exception.class, () ->
                    service.solicitarMatricula(aluno, "FIS999", PER, "T99"));

            String msg = ex.getMessage();
            assertTrue(msg.contains("T99") || msg.contains("FIS999"),
                    "Mensagem deve citar pelo menos um identificador da turma não encontrada");
        }

        @Test
        @DisplayName("4.3 Período letivo inativo deve impedir solicitação")
        void periodoInativoDeveImpedir() {
            Turma turmaPeriodoInativo = new Turma("T01", DISC, "2025.2", 40, "Seg 10h", "A101", "P0001");
            when(turmaRepository.buscarPorChaveUnica(DISC, "2025.2", "T01")).thenReturn(turmaPeriodoInativo);
            when(periodoRepository.buscarPorCodigo("2025.2")).thenReturn(periodoInativo);

            Exception ex = assertThrows(Exception.class, () ->
                    service.solicitarMatricula(aluno, DISC, "2025.2", "T01"));

            assertTrue(ex.getMessage().contains("inativo"),
                    "Mensagem deve mencionar 'inativo'");
            verify(matriculaRepository, never()).salvar(any());
        }

        @Test
        @DisplayName("4.4 Período letivo inexistente (null no repositório) deve impedir solicitação")
        void periodoNuloNoRepositorioDeveImpedir() {
            Turma turmaSemPeriodo = new Turma("T01", DISC, "2099.1", 40, "Seg 10h", "A101", "P0001");
            when(turmaRepository.buscarPorChaveUnica(DISC, "2099.1", "T01")).thenReturn(turmaSemPeriodo);
            when(periodoRepository.buscarPorCodigo("2099.1")).thenReturn(null);

            Exception ex = assertThrows(Exception.class, () ->
                    service.solicitarMatricula(aluno, DISC, "2099.1", "T01"));

            assertNotNull(ex.getMessage(), "Deve lançar exceção com mensagem");
            verify(matriculaRepository, never()).salvar(any());
        }

        @Test
        @DisplayName("4.5 Solicitação duplicada (status PENDENTE ou CONFIRMADA) deve lançar exceção")
        void solicitacaoDuplicadaDeveLancar() {
            mockTurmaExiste();
            mockPeriodoAtivo();
            when(matriculaRepository.existeSolicitacaoAtiva("A0001", DISC, PER, TURMA)).thenReturn(true);

            Exception ex = assertThrows(Exception.class, () ->
                    service.solicitarMatricula(aluno, DISC, PER, TURMA));

            assertTrue(ex.getMessage().contains("já possui uma solicitação ativa"),
                    "Mensagem deve informar sobre solicitação ativa existente");
            verify(matriculaRepository, never()).salvar(any());
        }

        @Test
        @DisplayName("4.6 Vagas completamente esgotadas devem impedir nova solicitação")
        void vagasEsgotadasDevemImpedir() {
            mockTurmaExiste();
            mockPeriodoAtivo();
            mockSemSolicitacaoAtiva();
            mockVagasDisponiveis(40L); // turma cheia (40/40)

            Exception ex = assertThrows(Exception.class, () ->
                    service.solicitarMatricula(aluno, DISC, PER, TURMA));

            assertTrue(ex.getMessage().contains("vagas"),
                    "Mensagem deve mencionar 'vagas'");
            verify(matriculaRepository, never()).salvar(any());
        }

        @Test
        @DisplayName("4.7 Excedente de confirmadas (> vagas) também deve impedir solicitação")
        void excessoDeConfirmadasTambemDeveImpedir() {
            mockTurmaExiste();
            mockPeriodoAtivo();
            mockSemSolicitacaoAtiva();
            mockVagasDisponiveis(50L); // situação anômala: 50 confirmadas em turma de 40

            assertThrows(Exception.class, () ->
                    service.solicitarMatricula(aluno, DISC, PER, TURMA));
            verify(matriculaRepository, never()).salvar(any());
        }
    }

    // =========================================================================
    // 5. CANCELAR SOLICITAÇÃO — SUCESSO
    // =========================================================================

    @Nested
    @DisplayName("5. Cancelar solicitação — sucesso")
    class CancelarSolicitacaoSucesso {

        @Test
        @DisplayName("5.1 Deve cancelar solicitação PENDENTE com sucesso")
        void deveCancelarSolicitacaoPendente() {
            MatriculaTurma solicitacao = new MatriculaTurma("A0001", DISC, PER, TURMA);
            solicitacao.setStatus(StatusMatricula.PENDENTE);
            when(matriculaRepository.buscarPorChaveUnica("A0001", DISC, PER, TURMA))
                    .thenReturn(solicitacao);

            assertDoesNotThrow(() -> service.cancelarSolicitacao(aluno, DISC, PER, TURMA));

            ArgumentCaptor<MatriculaTurma> captor = ArgumentCaptor.forClass(MatriculaTurma.class);
            verify(matriculaRepository).atualizar(captor.capture());
            assertEquals(StatusMatricula.CANCELADA, captor.getValue().getStatus(),
                    "Status após cancelamento deve ser CANCELADA");
        }

        @Test
        @DisplayName("5.2 Após cancelamento o repositório deve ser atualizado exatamente uma vez")
        void atualizacaoDeveSerChamadaUmaVez() throws Exception {
            MatriculaTurma solicitacao = new MatriculaTurma("A0001", DISC, PER, TURMA);
            when(matriculaRepository.buscarPorChaveUnica("A0001", DISC, PER, TURMA))
                    .thenReturn(solicitacao);

            service.cancelarSolicitacao(aluno, DISC, PER, TURMA);

            verify(matriculaRepository).atualizar(any(MatriculaTurma.class));
        }
    }

    // =========================================================================
    // 6. CANCELAR SOLICITAÇÃO — FALHAS
    // =========================================================================

    @Nested
    @DisplayName("6. Cancelar solicitação — falhas")
    class CancelarSolicitacaoFalhas {

        @Test
        @DisplayName("6.1 Não pode cancelar solicitação com status CONFIRMADA")
        void naoPodeCancelarConfirmada() {
            MatriculaTurma solicitacao = new MatriculaTurma("A0001", DISC, PER, TURMA);
            solicitacao.setStatus(StatusMatricula.CONFIRMADA);
            when(matriculaRepository.buscarPorChaveUnica("A0001", DISC, PER, TURMA))
                    .thenReturn(solicitacao);

            Exception ex = assertThrows(Exception.class, () ->
                    service.cancelarSolicitacao(aluno, DISC, PER, TURMA));

            assertTrue(ex.getMessage().contains("PENDENTE"),
                    "Mensagem deve informar que apenas PENDENTE pode ser cancelada");
            verify(matriculaRepository, never()).atualizar(any());
        }

        @Test
        @DisplayName("6.2 Mensagem deve exibir o status atual ao tentar cancelar CONFIRMADA")
        void mensagemDeveMostrarStatusAtualAoCancelarConfirmada() {
            MatriculaTurma solicitacao = new MatriculaTurma("A0001", DISC, PER, TURMA);
            solicitacao.setStatus(StatusMatricula.CONFIRMADA);
            when(matriculaRepository.buscarPorChaveUnica("A0001", DISC, PER, TURMA))
                    .thenReturn(solicitacao);

            Exception ex = assertThrows(Exception.class, () ->
                    service.cancelarSolicitacao(aluno, DISC, PER, TURMA));

            assertTrue(ex.getMessage().contains("CONFIRMADA"),
                    "Mensagem deve citar o status atual (CONFIRMADA)");
        }

        @Test
        @DisplayName("6.3 Não pode cancelar solicitação que já está CANCELADA")
        void naoPodeCancelarJaCancelada() {
            MatriculaTurma solicitacao = new MatriculaTurma("A0001", DISC, PER, TURMA);
            solicitacao.setStatus(StatusMatricula.CANCELADA);
            when(matriculaRepository.buscarPorChaveUnica("A0001", DISC, PER, TURMA))
                    .thenReturn(solicitacao);

            Exception ex = assertThrows(Exception.class, () ->
                    service.cancelarSolicitacao(aluno, DISC, PER, TURMA));

            assertTrue(ex.getMessage().contains("PENDENTE"));
            verify(matriculaRepository, never()).atualizar(any());
        }

        @Test
        @DisplayName("6.4 Cancelar solicitação inexistente deve lançar exceção")
        void cancelarInexistenteDeveLancar() {
            when(matriculaRepository.buscarPorChaveUnica("A0001", DISC, PER, "T99"))
                    .thenReturn(null);

            Exception ex = assertThrows(Exception.class, () ->
                    service.cancelarSolicitacao(aluno, DISC, PER, "T99"));

            assertTrue(ex.getMessage().contains("Nenhuma solicitação encontrada"),
                    "Mensagem deve indicar que não há solicitação para cancelar");
            verify(matriculaRepository, never()).atualizar(any());
        }

        @Test
        @DisplayName("6.5 Coordenador não pode cancelar solicitação")
        void coordenadorNaoPodeCancelar() {
            Exception ex = assertThrows(Exception.class, () ->
                    service.cancelarSolicitacao(coordenador, DISC, PER, TURMA));
            assertTrue(ex.getMessage().contains("Apenas alunos"));
            verify(matriculaRepository, never()).atualizar(any());
        }

        @Test
        @DisplayName("6.6 Professor não pode cancelar solicitação")
        void professorNaoPodeCancelar() {
            Exception ex = assertThrows(Exception.class, () ->
                    service.cancelarSolicitacao(professor, DISC, PER, TURMA));
            assertTrue(ex.getMessage().contains("Apenas alunos"));
        }

        @Test
        @DisplayName("6.7 Usuário null não pode cancelar solicitação")
        void usuarioNuloNaoPodeCancelar() {
            Exception ex = assertThrows(Exception.class, () ->
                    service.cancelarSolicitacao(null, DISC, PER, TURMA));
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
            MatriculaTurma s1 = new MatriculaTurma("A0001", DISC,     PER, "T01");
            MatriculaTurma s2 = new MatriculaTurma("A0001", "FIS001", PER, "T01");
            when(matriculaRepository.listarPorAluno("A0001")).thenReturn(Arrays.asList(s1, s2));

            List<MatriculaTurma> resultado = service.listarMinhasSolicitacoes(aluno);

            assertEquals(2, resultado.size(), "Deve retornar as 2 solicitações do aluno");
        }

        @Test
        @DisplayName("7.2 Lista vazia deve ser retornada quando não há solicitações")
        void deveRetornarListaVaziaQuandoSemSolicitacoes() throws Exception {
            when(matriculaRepository.listarPorAluno("A0001")).thenReturn(Collections.emptyList());

            List<MatriculaTurma> resultado = service.listarMinhasSolicitacoes(aluno);

            assertNotNull(resultado, "Lista não pode ser nula");
            assertTrue(resultado.isEmpty(), "Lista deve estar vazia");
        }

        @Test
        @DisplayName("7.3 Lista retornada não deve conter solicitações de outros alunos")
        void listaNaoDeveConterSolicitacoesDeOutrosAlunos() throws Exception {
            MatriculaTurma s1 = new MatriculaTurma("A0001", DISC, PER, TURMA);
            when(matriculaRepository.listarPorAluno("A0001")).thenReturn(Collections.singletonList(s1));

            List<MatriculaTurma> resultado = service.listarMinhasSolicitacoes(aluno);

            resultado.forEach(s ->
                    assertEquals("A0001", s.getMatriculaAluno(),
                            "Todas as solicitações devem pertencer ao aluno A0001"));
        }

        @Test
        @DisplayName("7.4 Deve delegar a busca com a matrícula correta do aluno")
        void deveBuscarComMatriculaCorretaDoAluno() throws Exception {
            when(matriculaRepository.listarPorAluno("A0001")).thenReturn(Collections.emptyList());

            service.listarMinhasSolicitacoes(aluno);

            verify(matriculaRepository).listarPorAluno("A0001");
        }

        @Test
        @DisplayName("7.5 Coordenador não pode listar solicitações")
        void coordenadorNaoPodeListar() {
            Exception ex = assertThrows(Exception.class, () ->
                    service.listarMinhasSolicitacoes(coordenador));
            assertTrue(ex.getMessage().contains("Apenas alunos"));
        }

        @Test
        @DisplayName("7.6 Professor não pode listar solicitações")
        void professorNaoPodeListar() {
            Exception ex = assertThrows(Exception.class, () ->
                    service.listarMinhasSolicitacoes(professor));
            assertTrue(ex.getMessage().contains("Apenas alunos"));
        }

        @Test
        @DisplayName("7.7 Usuário null não pode listar solicitações")
        void usuarioNuloNaoPodeListar() {
            Exception ex = assertThrows(Exception.class, () ->
                    service.listarMinhasSolicitacoes(null));
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
            when(matriculaRepository.contarOcupadasPorTurma(DISC, PER, TURMA)).thenReturn(0L);

            long vagas = service.vagasDisponiveis(turma);

            assertEquals(40L, vagas, "Com 0 ocupadas, todas as 40 vagas estão disponíveis");
        }

        @Test
        @DisplayName("8.2 Deve retornar zero quando todas as vagas estão ocupadas")
        void deveRetornarZeroQuandoTurmaCheia() {
            when(matriculaRepository.contarOcupadasPorTurma(DISC, PER, TURMA)).thenReturn(40L);

            long vagas = service.vagasDisponiveis(turma);

            assertEquals(0L, vagas, "Com 40 ocupadas e 40 vagas, resultado deve ser 0");
        }

        @Test
        @DisplayName("8.3 Deve retornar a diferença correta entre vagas e ocupadas")
        void deveRetornarDiferencaCorreta() {
            when(matriculaRepository.contarOcupadasPorTurma(DISC, PER, TURMA)).thenReturn(15L);

            long vagas = service.vagasDisponiveis(turma);

            assertEquals(25L, vagas, "40 vagas - 15 ocupadas = 25 disponíveis");
        }

        @Test
        @DisplayName("8.4 Deve retornar zero (não negativo) quando ocupadas excedem vagas")
        void naoDeveRetornarNegativoEmExcesso() {
            when(matriculaRepository.contarOcupadasPorTurma(DISC, PER, TURMA)).thenReturn(50L);

            long vagas = service.vagasDisponiveis(turma);

            assertEquals(0L, vagas, "Não deve retornar valor negativo mesmo com dados inconsistentes");
        }

        @Test
        @DisplayName("8.5 Turma null deve retornar zero sem lançar exceção")
        void turmaNulaDeveRetornarZero() {
            long vagas = service.vagasDisponiveis(null);

            assertEquals(0L, vagas, "Turma null deve retornar 0 vagas disponíveis");
        }

        @Test
        @DisplayName("8.6 Deve consultar repositório com os códigos corretos da turma")
        void deveConsultarRepositorioComChavesDaTurma() {
            when(matriculaRepository.contarOcupadasPorTurma(DISC, PER, TURMA)).thenReturn(0L);

            service.vagasDisponiveis(turma);

            verify(matriculaRepository).contarOcupadasPorTurma(DISC, PER, TURMA);
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

            assertThrows(Exception.class, () -> 
                service.aprovarMatricula(coordenador, "A01", "D1", "P1", "T1"));
        }
    }

    class PreRequisitos {
        @Test
        @DisplayName("Deve permitir matricula quando disciplina nao possui pre-requisitos")
        void devePermitirMatriculaSemPreRequisitos() throws Exception {

            Disciplina disciplina = new Disciplina(
                    "ES2",
                    "Engenharia de Software 2",
                    60,
                    4,
                    List.of()
            );

            when(disciplinaRepository.buscarPorCodigo("ES2"))
                    .thenReturn(disciplina);

            assertDoesNotThrow(() ->
                    service.validarPreRequisitos(aluno, disciplina)
            );
        }


        @Test
        @DisplayName("Deve permitir matricula quando todos os pre-requisitos foram cumpridos")
        void devePermitirQuandoTodosPreRequisitosForamCumpridos() throws Exception {

            Disciplina disciplina = new Disciplina(
                    "ES2",
                    "Engenharia de Software 2",
                    60,
                    4,
                    List.of("ES1", "POO")
            );

            when(historicoService.alunoFoiAprovado(
                    aluno.getMatricula(),
                    "ES1"))
                    .thenReturn(true);

            when(historicoService.alunoFoiAprovado(
                    aluno.getMatricula(),
                    "POO"))
                    .thenReturn(true);

            assertDoesNotThrow(() ->
                    service.validarPreRequisitos(aluno, disciplina)
            );
        }

        @Test
        @DisplayName("Nao deve permitir matricula quando faltar um pre-requisito")
        void naoDevePermitirQuandoFaltarUmPreRequisito() {

            Disciplina disciplina = new Disciplina(
                    "ES2",
                    "Engenharia de Software 2",
                    60,
                    4,
                    List.of("ES1", "POO")
            );

            when(historicoService.alunoFoiAprovado(
                    aluno.getMatricula(),
                    "ES1"))
                    .thenReturn(true);

            when(historicoService.alunoFoiAprovado(
                    aluno.getMatricula(),
                    "POO"))
                    .thenReturn(false);

            Exception ex = assertThrows(
                    Exception.class,
                    () -> service.validarPreRequisitos(aluno, disciplina)
            );

            assertEquals(
                    "Erro: O aluno nao foi aprovado no pre-requisito POO.",
                    ex.getMessage()
            );
        }

        @Test
        @DisplayName("Nao deve permitir matricula quando nenhum pre-requisito foi cumprido")
        void naoDevePermitirQuandoNenhumPreRequisitoFoiCumprido() {

            Disciplina disciplina = new Disciplina(
                    "ES2",
                    "Engenharia de Software 2",
                    60,
                    4,
                    List.of("ES1")
            );

            when(historicoService.alunoFoiAprovado(
                    aluno.getMatricula(),
                    "ES1"))
                    .thenReturn(false);

            Exception ex = assertThrows(
                    Exception.class,
                    () -> service.validarPreRequisitos(aluno, disciplina)
            );

            assertEquals(
                    "Erro: O aluno nao foi aprovado no pre-requisito ES1.",
                    ex.getMessage()
            );
        }
    }
}