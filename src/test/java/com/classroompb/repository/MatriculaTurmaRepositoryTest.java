package com.classroompb.repository;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.classroompb.model.MatriculaTurma;
import com.classroompb.model.StatusMatricula;

/**
 * Testes de integração para MatriculaTurmaRepository (RF16).
 *
 * Cobertura: - Construtores (padrão e com caminho) - salvar() e listarTodas() - existeSolicitacaoAtiva() -
 * listarPorAluno() - listarPorAlunoEStatus() - listarPorTurma() - listarListaEsperaPorTurmaOrdenada() -
 * contarConfirmadasPorTurma() - buscarPorChaveUnica() - atualizar() - case-insensitive - Persistência entre instâncias
 */
@DisplayName("Testes de MatriculaTurmaRepository (RF16)")
public class MatriculaTurmaRepositoryTest {

    @TempDir
    Path tempDir;

    private MatriculaTurmaRepository repository;

    private String arquivoTemp() {
        return tempDir.resolve("matriculas_turma_test.json").toString();
    }

    @BeforeEach
    void setUp() {
        repository = new MatriculaTurmaRepository(arquivoTemp());
    }

    // =========================================================================
    // Fixtures
    // =========================================================================

    private MatriculaTurma matriculaA1_MAT_T01() {
        return new MatriculaTurma("A001", "MAT001", "2026.1", "T01");
    }

    private MatriculaTurma matriculaA1_FIS_T01() {
        return new MatriculaTurma("A001", "FIS001", "2026.1", "T01");
    }

    private MatriculaTurma matriculaA2_MAT_T01() {
        return new MatriculaTurma("A002", "MAT001", "2026.1", "T01");
    }

    private MatriculaTurma matriculaA2_MAT_T02() {
        return new MatriculaTurma("A002", "MAT001", "2026.1", "T02");
    }

    private MatriculaTurma matriculaA3_MAT_T01_2025() {
        return new MatriculaTurma("A003", "MAT001", "2025.2", "T01");
    }

    // =========================================================================
    // Construtores e inicialização
    // =========================================================================

    @Nested
    @DisplayName("Construtores e inicialização")
    class ConstrutoresEInicializacao {

        @Test
        @DisplayName("Construtor com caminho deve criar repositório vazio quando arquivo não existe")
        void deveIniciarVazioQuandoArquivoNaoExiste() {
            assertTrue(repository.listarTodas().isEmpty());
        }

        @Test
        @DisplayName("Construtor padrão não deve lançar exceção")
        void construtorPadraoNaoLancaExcecao() {
            assertDoesNotThrow(() -> new MatriculaTurmaRepository());
        }

        @Test
        @DisplayName("carregarDados deve iniciar lista não-nula quando caminho aponta para diretório")
        void carregarDadosComCaminhoInvalidoRetornaListaNaoNula() {
            String caminhoDiretorio = tempDir.toString();
            MatriculaTurmaRepository repo = new MatriculaTurmaRepository(caminhoDiretorio);
            assertNotNull(repo.listarTodas());
        }
    }

    // =========================================================================
    // salvar() e listarTodas()
    // =========================================================================

    @Nested
    @DisplayName("salvar() e listarTodas()")
    class SalvarEListarTodas {

        @Test
        @DisplayName("Deve salvar uma matrícula e recuperá-la via listarTodas")
        void deveSalvarEListarUmaMatricula() {
            repository.salvar(matriculaA1_MAT_T01());
            List<MatriculaTurma> todas = repository.listarTodas();
            assertEquals(1, todas.size());
            assertEquals("A001", todas.get(0).getMatriculaAluno());
        }

        @Test
        @DisplayName("Deve salvar múltiplas matrículas e listá-las todas")
        void deveSalvarMultiplasMatriculas() {
            repository.salvar(matriculaA1_MAT_T01());
            repository.salvar(matriculaA1_FIS_T01());
            repository.salvar(matriculaA2_MAT_T01());
            assertEquals(3, repository.listarTodas().size());
        }

        @Test
        @DisplayName("listarTodas deve retornar cópia defensiva")
        void listarTodasRetornaCopiaDfensiva() {
            repository.salvar(matriculaA1_MAT_T01());
            List<MatriculaTurma> lista = repository.listarTodas();
            lista.clear();
            assertEquals(1, repository.listarTodas().size());
        }

        @Test
        @DisplayName("Nova matrícula deve ter status PENDENTE por padrão")
        void novaMatriculaDeveEstarPendente() {
            repository.salvar(matriculaA1_MAT_T01());
            MatriculaTurma m = repository.listarTodas().get(0);
            assertEquals(StatusMatricula.PENDENTE, m.getStatus());
        }
    }

    // =========================================================================
    // existeSolicitacaoAtiva()
    // =========================================================================

    @Nested
    @DisplayName("existeSolicitacaoAtiva()")
    class ExisteSolicitacaoAtiva {

        @Test
        @DisplayName("Deve retornar true para solicitação PENDENTE existente")
        void deveRetornarTrueParaPendente() {
            repository.salvar(matriculaA1_MAT_T01());
            assertTrue(repository.existeSolicitacaoAtiva("A001", "MAT001", "2026.1", "T01"));
        }

        @Test
        @DisplayName("Deve retornar true para solicitação CONFIRMADA existente")
        void deveRetornarTrueParaConfirmada() {
            MatriculaTurma m = matriculaA1_MAT_T01();
            m.setStatus(StatusMatricula.CONFIRMADA);
            repository.salvar(m);
            assertTrue(repository.existeSolicitacaoAtiva("A001", "MAT001", "2026.1", "T01"));
        }

        @Test
        @DisplayName("Deve retornar false para solicitação CANCELADA")
        void deveRetornarFalseParaCancelada() {
            MatriculaTurma m = matriculaA1_MAT_T01();
            m.setStatus(StatusMatricula.CANCELADA);
            repository.salvar(m);
            assertFalse(repository.existeSolicitacaoAtiva("A001", "MAT001", "2026.1", "T01"));
        }

        @Test
        @DisplayName("Deve retornar false quando não existe nenhuma solicitação")
        void deveRetornarFalseQuandoNaoExiste() {
            assertFalse(repository.existeSolicitacaoAtiva("A001", "MAT001", "2026.1", "T01"));
        }

        @Test
        @DisplayName("Deve ser case-insensitive na chave composta")
        void deveSerCaseInsensitive() {
            repository.salvar(matriculaA1_MAT_T01());
            assertTrue(repository.existeSolicitacaoAtiva("a001", "mat001", "2026.1", "t01"));
        }

        @Test
        @DisplayName("Deve retornar true para solicitação em LISTA_ESPERA (impede duplicata na fila)")
        void deveRetornarTrueParaListaEspera() {
            MatriculaTurma m = matriculaA1_MAT_T01();
            m.setStatus(StatusMatricula.LISTA_ESPERA);
            repository.salvar(m);
            assertTrue(repository.existeSolicitacaoAtiva("A001", "MAT001", "2026.1", "T01"),
                    "Aluno em lista de espera não pode re-solicitar a mesma turma");
        }

        @Test
        @DisplayName("Deve retornar true para solicitação REJEITADA (bloqueia re-tentativa imediata)")
        void deveRetornarTrueParaRejeitada() {
            MatriculaTurma m = matriculaA1_MAT_T01();
            m.setStatus(StatusMatricula.REJEITADA);
            repository.salvar(m);
            assertTrue(repository.existeSolicitacaoAtiva("A001", "MAT001", "2026.1", "T01"),
                    "Solicitação rejeitada ainda consta no histórico e bloqueia nova tentativa");
        }

        @Test
        @DisplayName("Deve permitir nova solicitação após cancelamento de LISTA_ESPERA")
        void devePermitirNovaSolicitacaoAposCancelamentoDeListaEspera() {
            MatriculaTurma m = matriculaA1_MAT_T01();
            m.setStatus(StatusMatricula.LISTA_ESPERA);
            repository.salvar(m);

            m.setStatus(StatusMatricula.CANCELADA);
            repository.atualizar(m);

            assertFalse(repository.existeSolicitacaoAtiva("A001", "MAT001", "2026.1", "T01"),
                    "Após cancelar da lista de espera, aluno deve poder solicitar novamente");
        }
    }

    // =========================================================================
    // listarPorAluno()
    // =========================================================================

    @Nested
    @DisplayName("listarPorAluno()")
    class ListarPorAluno {

        @Test
        @DisplayName("Deve retornar todas as matrículas do aluno")
        void deveRetornarTodasDoAluno() {
            repository.salvar(matriculaA1_MAT_T01());
            repository.salvar(matriculaA1_FIS_T01());
            repository.salvar(matriculaA2_MAT_T01());

            List<MatriculaTurma> resultado = repository.listarPorAluno("A001");
            assertEquals(2, resultado.size());
            assertTrue(resultado.stream().allMatch(m -> m.getMatriculaAluno().equalsIgnoreCase("A001")));
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando aluno não tem matrículas")
        void deveRetornarVaziaQuandoAlunoNaoTemMatriculas() {
            repository.salvar(matriculaA2_MAT_T01());
            assertTrue(repository.listarPorAluno("A001").isEmpty());
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando repositório está vazio")
        void deveRetornarVaziaParaRepositorioVazio() {
            assertTrue(repository.listarPorAluno("A001").isEmpty());
        }

        @Test
        @DisplayName("Deve ser case-insensitive na matrícula do aluno")
        void deveSerCaseInsensitive() {
            repository.salvar(matriculaA1_MAT_T01());
            assertFalse(repository.listarPorAluno("a001").isEmpty());
        }
    }

    // =========================================================================
    // listarPorAlunoEStatus()
    // =========================================================================

    @Nested
    @DisplayName("listarPorAlunoEStatus()")
    class ListarPorAlunoEStatus {

        @Test
        @DisplayName("Deve retornar apenas matrículas do aluno com o status especificado")
        void deveRetornarApenasMesmoStatus() {
            MatriculaTurma pendente = matriculaA1_MAT_T01();
            MatriculaTurma confirmada = matriculaA1_FIS_T01();
            confirmada.setStatus(StatusMatricula.CONFIRMADA);

            repository.salvar(pendente);
            repository.salvar(confirmada);

            List<MatriculaTurma> resultado = repository.listarPorAlunoEStatus("A001", StatusMatricula.PENDENTE);
            assertEquals(1, resultado.size());
            assertEquals(StatusMatricula.PENDENTE, resultado.get(0).getStatus());
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando aluno não tem matrículas com esse status")
        void deveRetornarVaziaQuandoStatusNaoCorresponde() {
            repository.salvar(matriculaA1_MAT_T01()); // PENDENTE
            assertTrue(repository.listarPorAlunoEStatus("A001", StatusMatricula.CONFIRMADA).isEmpty());
        }

        @Test
        @DisplayName("Deve retornar matrículas CANCELADAS quando solicitado")
        void deveListarCanceladas() {
            MatriculaTurma cancelada = matriculaA1_MAT_T01();
            cancelada.setStatus(StatusMatricula.CANCELADA);
            repository.salvar(cancelada);

            List<MatriculaTurma> resultado = repository.listarPorAlunoEStatus("A001", StatusMatricula.CANCELADA);
            assertEquals(1, resultado.size());
        }

        @Test
        @DisplayName("Deve ser case-insensitive na matrícula do aluno")
        void deveSerCaseInsensitive() {
            repository.salvar(matriculaA1_MAT_T01());
            assertFalse(repository.listarPorAlunoEStatus("a001", StatusMatricula.PENDENTE).isEmpty());
        }
    }

    // =========================================================================
    // listarPorTurma()
    // =========================================================================

    @Nested
    @DisplayName("listarPorTurma()")
    class ListarPorTurma {

        @Test
        @DisplayName("Deve retornar todas as matrículas da turma")
        void deveRetornarTodasDaTurma() {
            repository.salvar(matriculaA1_MAT_T01());
            repository.salvar(matriculaA2_MAT_T01());
            repository.salvar(matriculaA1_FIS_T01()); // outra disciplina

            List<MatriculaTurma> resultado = repository.listarPorTurma("MAT001", "2026.1", "T01");
            assertEquals(2, resultado.size());
        }

        @Test
        @DisplayName("Deve retornar lista vazia para turma sem matrículas")
        void deveRetornarVaziaParaTurmaSemMatriculas() {
            repository.salvar(matriculaA1_MAT_T01());
            assertTrue(repository.listarPorTurma("FIS001", "2026.1", "T01").isEmpty());
        }

        @Test
        @DisplayName("Não deve incluir matrículas de outra turma da mesma disciplina")
        void naoDeveIncluirOutraTurma() {
            repository.salvar(matriculaA1_MAT_T01()); // T01
            repository.salvar(matriculaA2_MAT_T02()); // T02

            List<MatriculaTurma> resultado = repository.listarPorTurma("MAT001", "2026.1", "T01");
            assertEquals(1, resultado.size());
            assertEquals("T01", resultado.get(0).getCodigoTurma());
        }

        @Test
        @DisplayName("Não deve incluir matrículas de outro período")
        void naoDeveIncluirOutroPeriodo() {
            repository.salvar(matriculaA1_MAT_T01()); // 2026.1
            repository.salvar(matriculaA3_MAT_T01_2025()); // 2025.2

            List<MatriculaTurma> resultado = repository.listarPorTurma("MAT001", "2026.1", "T01");
            assertEquals(1, resultado.size());
        }

        @Test
        @DisplayName("Deve ser case-insensitive nos parâmetros")
        void deveSerCaseInsensitive() {
            repository.salvar(matriculaA1_MAT_T01());
            assertFalse(repository.listarPorTurma("mat001", "2026.1", "t01").isEmpty());
        }
    }

    // =========================================================================
    // listarListaEsperaPorTurmaOrdenada()
    // =========================================================================

    @Nested
    @DisplayName("listarListaEsperaPorTurmaOrdenada() — RF23: ordem de solicitação")
    class ListarListaEsperaPorTurmaOrdenada {

        @Test
        @DisplayName("Deve retornar apenas alunos em LISTA_ESPERA, do mais antigo ao mais novo")
        void deveRetornarListaEsperaOrdenadaPorDataSolicitacao() {
            MatriculaTurma maisNova = new MatriculaTurma("A001", "MAT001", "2026.1", "T01");
            maisNova.setStatus(StatusMatricula.LISTA_ESPERA);
            maisNova.setDataSolicitacao(LocalDateTime.now().minusMinutes(5));

            MatriculaTurma maisAntiga = new MatriculaTurma("A002", "MAT001", "2026.1", "T01");
            maisAntiga.setStatus(StatusMatricula.LISTA_ESPERA);
            maisAntiga.setDataSolicitacao(LocalDateTime.now().minusHours(2));

            repository.salvar(maisNova);
            repository.salvar(maisAntiga);

            List<MatriculaTurma> resultado = repository.listarListaEsperaPorTurmaOrdenada("MAT001", "2026.1", "T01");

            assertEquals(2, resultado.size());
            assertEquals("A002", resultado.get(0).getMatriculaAluno(),
                    "O aluno com solicitação mais antiga deve aparecer primeiro");
            assertEquals("A001", resultado.get(1).getMatriculaAluno(),
                    "O aluno com solicitação mais recente deve aparecer por último");
        }

        @Test
        @DisplayName("Deve excluir matrículas CONFIRMADAS e PENDENTES da lista de espera")
        void deveExcluirStatusDiferentesDeListaEspera() {
            MatriculaTurma confirmada = new MatriculaTurma("A001", "MAT001", "2026.1", "T01");
            confirmada.setStatus(StatusMatricula.CONFIRMADA);
            confirmada.setDataSolicitacao(LocalDateTime.now().minusHours(3));

            MatriculaTurma espera = new MatriculaTurma("A002", "MAT001", "2026.1", "T01");
            espera.setStatus(StatusMatricula.LISTA_ESPERA);
            espera.setDataSolicitacao(LocalDateTime.now().minusMinutes(30));

            repository.salvar(confirmada);
            repository.salvar(espera);

            List<MatriculaTurma> resultado = repository.listarListaEsperaPorTurmaOrdenada("MAT001", "2026.1", "T01");

            assertEquals(1, resultado.size());
            assertEquals("A002", resultado.get(0).getMatriculaAluno());
            assertEquals(StatusMatricula.LISTA_ESPERA, resultado.get(0).getStatus());
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há ninguém em espera")
        void deveRetornarVaziaQuandoSemListaEspera() {
            repository.salvar(matriculaA1_MAT_T01()); // PENDENTE

            List<MatriculaTurma> resultado = repository.listarListaEsperaPorTurmaOrdenada("MAT001", "2026.1", "T01");

            assertTrue(resultado.isEmpty());
        }

        @Test
        @DisplayName("Deve ser case-insensitive nos parâmetros")
        void deveSerCaseInsensitive() {
            MatriculaTurma espera = new MatriculaTurma("A001", "MAT001", "2026.1", "T01");
            espera.setStatus(StatusMatricula.LISTA_ESPERA);
            espera.setDataSolicitacao(LocalDateTime.now());
            repository.salvar(espera);

            assertFalse(repository.listarListaEsperaPorTurmaOrdenada("mat001", "2026.1", "t01").isEmpty());
        }

        @Test
        @DisplayName("Deve preservar a ordem correta com três alunos em espera inseridos fora de ordem")
        void devePreservarOrdemComTresAlunos() {
            MatriculaTurma terceiro = new MatriculaTurma("A003", "MAT001", "2026.1", "T01");
            terceiro.setStatus(StatusMatricula.LISTA_ESPERA);
            terceiro.setDataSolicitacao(LocalDateTime.now().minusMinutes(1));

            MatriculaTurma primeiro = new MatriculaTurma("A001", "MAT001", "2026.1", "T01");
            primeiro.setStatus(StatusMatricula.LISTA_ESPERA);
            primeiro.setDataSolicitacao(LocalDateTime.now().minusHours(3));

            MatriculaTurma segundo = new MatriculaTurma("A002", "MAT001", "2026.1", "T01");
            segundo.setStatus(StatusMatricula.LISTA_ESPERA);
            segundo.setDataSolicitacao(LocalDateTime.now().minusHours(1));

            // Inseridos fora de ordem para garantir que a ordenação é por data, não por inserção
            repository.salvar(terceiro);
            repository.salvar(primeiro);
            repository.salvar(segundo);

            List<MatriculaTurma> resultado = repository.listarListaEsperaPorTurmaOrdenada("MAT001", "2026.1", "T01");

            assertEquals(3, resultado.size());
            assertEquals("A001", resultado.get(0).getMatriculaAluno(), "1º: solicitação mais antiga");
            assertEquals("A002", resultado.get(1).getMatriculaAluno(), "2º: solicitação intermediária");
            assertEquals("A003", resultado.get(2).getMatriculaAluno(), "3º: solicitação mais recente");
        }
    }

    // =========================================================================
    // contarConfirmadasPorTurma()
    // =========================================================================

    @Nested
    @DisplayName("contarOcupadasPorTurma()")
    class ContarOcupadasPorTurma {

        @Test
        @DisplayName("Deve contar matrículas PENDENTES como ocupadas")
        void deveContarPendentes() {
            repository.salvar(matriculaA1_MAT_T01()); // PENDENTE
            assertEquals(1L, repository.contarOcupadasPorTurma("MAT001", "2026.1", "T01"));
        }

        @Test
        @DisplayName("Deve contar matrículas CONFIRMADAS como ocupadas")
        void deveContarConfirmadas() {
            MatriculaTurma m1 = matriculaA1_MAT_T01();
            m1.setStatus(StatusMatricula.CONFIRMADA);
            repository.salvar(m1);

            assertEquals(1L, repository.contarOcupadasPorTurma("MAT001", "2026.1", "T01"));
        }

        @Test
        @DisplayName("Deve somar PENDENTES e CONFIRMADAS")
        void deveSomarPendentesEConfirmadas() {
            MatriculaTurma m1 = matriculaA1_MAT_T01();
            m1.setStatus(StatusMatricula.CONFIRMADA);
            repository.salvar(m1);
            repository.salvar(matriculaA2_MAT_T01()); // PENDENTE

            assertEquals(2L, repository.contarOcupadasPorTurma("MAT001", "2026.1", "T01"));
        }

        @Test
        @DisplayName("Não deve contar ocupadas de outra turma")
        void naoDeveContarOutraTurma() {
            repository.salvar(matriculaA1_MAT_T01());
            assertEquals(0L, repository.contarOcupadasPorTurma("MAT001", "2026.1", "T02"));
        }

        @Test
        @DisplayName("Deve retornar 0 quando repositório está vazio")
        void deveRetornarZeroParaRepositorioVazio() {
            assertEquals(0L, repository.contarOcupadasPorTurma("MAT001", "2026.1", "T01"));
        }

        @Test
        @DisplayName("Não deve contar matrículas CANCELADAS")
        void naoDeveContarCanceladas() {
            MatriculaTurma cancelada = matriculaA1_MAT_T01();
            cancelada.setStatus(StatusMatricula.CANCELADA);
            repository.salvar(cancelada);

            assertEquals(0L, repository.contarOcupadasPorTurma("MAT001", "2026.1", "T01"));
        }

        @Test
        @DisplayName("Não deve contar alunos em LISTA_ESPERA como vagas ocupadas")
        void naoDeveContarListaEsperaComoOcupada() {
            // Aluno em lista de espera ainda não ocupa vaga — a vaga é reservada
            // somente ao ser promovido para CONFIRMADA
            MatriculaTurma espera = matriculaA1_MAT_T01();
            espera.setStatus(StatusMatricula.LISTA_ESPERA);
            repository.salvar(espera);

            assertEquals(0L, repository.contarOcupadasPorTurma("MAT001", "2026.1", "T01"),
                    "LISTA_ESPERA não deve ocupar vaga na turma");
        }

        @Test
        @DisplayName("Deve contar apenas CONFIRMADAS e PENDENTES quando há mix de status")
        void deveContarApenasConfirmadasEPendentesComMixDeStatus() {
            MatriculaTurma confirmada = matriculaA1_MAT_T01();
            confirmada.setStatus(StatusMatricula.CONFIRMADA);
            repository.salvar(confirmada);

            MatriculaTurma espera = matriculaA2_MAT_T01();
            espera.setStatus(StatusMatricula.LISTA_ESPERA);
            repository.salvar(espera);

            // Apenas a CONFIRMADA conta como vaga ocupada; LISTA_ESPERA não
            assertEquals(1L, repository.contarOcupadasPorTurma("MAT001", "2026.1", "T01"),
                    "Apenas CONFIRMADA deve ser contada; LISTA_ESPERA não ocupa vaga");
        }

        @Test
        @DisplayName("Após promoção de LISTA_ESPERA para CONFIRMADA, deve incrementar contagem")
        void aposPromocaoDeEsperaParaConfirmadaDeveIncrementarContagem() {
            MatriculaTurma espera = matriculaA1_MAT_T01();
            espera.setStatus(StatusMatricula.LISTA_ESPERA);
            repository.salvar(espera);

            assertEquals(0L, repository.contarOcupadasPorTurma("MAT001", "2026.1", "T01"),
                    "Antes da promoção: LISTA_ESPERA não conta");

            // Simula promoção automática: atualiza status para CONFIRMADA
            espera.setStatus(StatusMatricula.CONFIRMADA);
            repository.atualizar(espera);

            assertEquals(1L, repository.contarOcupadasPorTurma("MAT001", "2026.1", "T01"),
                    "Após promoção: CONFIRMADA deve ser contada como vaga ocupada");
        }
    }

    // =========================================================================
    // buscarPorChaveUnica()
    // =========================================================================

    @Nested
    @DisplayName("buscarPorChaveUnica()")
    class BuscarPorChaveUnica {

        @Test
        @DisplayName("Deve retornar a matrícula quando a chave existe")
        void deveRetornarMatriculaExistente() {
            repository.salvar(matriculaA1_MAT_T01());
            MatriculaTurma encontrada = repository.buscarPorChaveUnica("A001", "MAT001", "2026.1", "T01");
            assertNotNull(encontrada);
            assertEquals("A001", encontrada.getMatriculaAluno());
            assertEquals("MAT001", encontrada.getCodigoDisciplina());
            assertEquals("2026.1", encontrada.getCodigoPeriodo());
            assertEquals("T01", encontrada.getCodigoTurma());
        }

        @Test
        @DisplayName("Deve retornar null quando a chave não existe")
        void deveRetornarNullParaChaveInexistente() {
            assertNull(repository.buscarPorChaveUnica("A999", "XXX", "9999.9", "T99"));
        }

        @Test
        @DisplayName("Deve ser case-insensitive na chave composta")
        void deveSerCaseInsensitive() {
            repository.salvar(matriculaA1_MAT_T01());
            assertNotNull(repository.buscarPorChaveUnica("a001", "mat001", "2026.1", "t01"));
        }

        @Test
        @DisplayName("Deve retornar null em repositório vazio")
        void deveRetornarNullEmRepositorioVazio() {
            assertNull(repository.buscarPorChaveUnica("A001", "MAT001", "2026.1", "T01"));
        }
    }

    // =========================================================================
    // atualizar()
    // =========================================================================

    @Nested
    @DisplayName("atualizar()")
    class Atualizar {

        @Test
        @DisplayName("Deve atualizar o status de uma matrícula existente")
        void deveAtualizarStatusExistente() {
            repository.salvar(matriculaA1_MAT_T01());

            MatriculaTurma m = repository.buscarPorChaveUnica("A001", "MAT001", "2026.1", "T01");
            m.setStatus(StatusMatricula.CONFIRMADA);
            repository.atualizar(m);

            MatriculaTurma atualizada = repository.buscarPorChaveUnica("A001", "MAT001", "2026.1", "T01");
            assertEquals(StatusMatricula.CONFIRMADA, atualizada.getStatus());
        }

        @Test
        @DisplayName("Deve manter a quantidade total de matrículas após atualização")
        void deveManterQuantidadeAposAtualizar() {
            repository.salvar(matriculaA1_MAT_T01());
            repository.salvar(matriculaA2_MAT_T01());

            MatriculaTurma m = repository.buscarPorChaveUnica("A001", "MAT001", "2026.1", "T01");
            m.setStatus(StatusMatricula.CANCELADA);
            repository.atualizar(m);

            assertEquals(2, repository.listarTodas().size());
        }

        @Test
        @DisplayName("Atualizar matrícula inexistente deve lançar IllegalArgumentException")
        void atualizarInexistenteLancaExcecao() {
            MatriculaTurma fantasma = new MatriculaTurma("A999", "XXX", "9999.9", "T99");
            assertThrows(IllegalArgumentException.class, () -> repository.atualizar(fantasma));
        }

        @Test
        @DisplayName("Deve persistir a atualização em nova instância do repositório")
        void devePersistirAtualizacaoEntreInstancias() {
            repository.salvar(matriculaA1_MAT_T01());

            MatriculaTurma m = repository.buscarPorChaveUnica("A001", "MAT001", "2026.1", "T01");
            m.setStatus(StatusMatricula.CONFIRMADA);
            repository.atualizar(m);

            MatriculaTurmaRepository novaInstancia = new MatriculaTurmaRepository(arquivoTemp());
            MatriculaTurma recuperada = novaInstancia.buscarPorChaveUnica("A001", "MAT001", "2026.1", "T01");
            assertNotNull(recuperada);
            assertEquals(StatusMatricula.CONFIRMADA, recuperada.getStatus());
        }

        @Test
        @DisplayName("Deve cancelar matrícula e impactar existeSolicitacaoAtiva")
        void cancelarMatriculaDeveDesativarSolicitacao() {
            repository.salvar(matriculaA1_MAT_T01());
            assertTrue(repository.existeSolicitacaoAtiva("A001", "MAT001", "2026.1", "T01"));

            MatriculaTurma m = repository.buscarPorChaveUnica("A001", "MAT001", "2026.1", "T01");
            m.setStatus(StatusMatricula.CANCELADA);
            repository.atualizar(m);

            assertFalse(repository.existeSolicitacaoAtiva("A001", "MAT001", "2026.1", "T01"));
        }
    }

    // =========================================================================
    // Persistência entre instâncias
    // =========================================================================

    @Nested
    @DisplayName("Persistência entre instâncias")
    class PersistenciaEntreInstancias {

        @Test
        @DisplayName("Matrículas salvas devem ser visíveis em nova instância do repositório")
        void matriculasSalvasDevemPersistir() {
            repository.salvar(matriculaA1_MAT_T01());
            repository.salvar(matriculaA2_MAT_T01());

            MatriculaTurmaRepository novaInstancia = new MatriculaTurmaRepository(arquivoTemp());
            assertEquals(2, novaInstancia.listarTodas().size());
        }

        @Test
        @DisplayName("Status atualizado deve persistir entre instâncias")
        void statusAtualizadoDevePerdurar() {
            repository.salvar(matriculaA1_MAT_T01());

            MatriculaTurma m = repository.buscarPorChaveUnica("A001", "MAT001", "2026.1", "T01");
            m.setStatus(StatusMatricula.CANCELADA);
            repository.atualizar(m);

            MatriculaTurmaRepository novaInstancia = new MatriculaTurmaRepository(arquivoTemp());
            assertEquals(StatusMatricula.CANCELADA,
                    novaInstancia.buscarPorChaveUnica("A001", "MAT001", "2026.1", "T01").getStatus());
        }

        @Test
        @DisplayName("existeSolicitacaoAtiva deve refletir dados persistidos em nova instância")
        void existeSolicitacaoAtivaRefleteNovInstancia() {
            MatriculaTurma m = matriculaA1_MAT_T01();
            m.setStatus(StatusMatricula.CANCELADA);
            repository.salvar(m);

            MatriculaTurmaRepository novaInstancia = new MatriculaTurmaRepository(arquivoTemp());
            assertFalse(novaInstancia.existeSolicitacaoAtiva("A001", "MAT001", "2026.1", "T01"));
        }

        @Test
        @DisplayName("contarOcupadasPorTurma deve retornar valor correto após reload")
        void contarOcupadasAposReload() {
            MatriculaTurma m1 = matriculaA1_MAT_T01();
            m1.setStatus(StatusMatricula.CONFIRMADA);
            MatriculaTurma m2 = matriculaA2_MAT_T01();
            m2.setStatus(StatusMatricula.PENDENTE);

            repository.salvar(m1);
            repository.salvar(m2);

            MatriculaTurmaRepository novaInstancia = new MatriculaTurmaRepository(arquivoTemp());
            assertEquals(2L, novaInstancia.contarOcupadasPorTurma("MAT001", "2026.1", "T01"));
        }

        @Test
        @DisplayName("listarPorAluno deve retornar dados corretos após reload")
        void listarPorAlunoAposReload() {
            repository.salvar(matriculaA1_MAT_T01());
            repository.salvar(matriculaA1_FIS_T01());

            MatriculaTurmaRepository novaInstancia = new MatriculaTurmaRepository(arquivoTemp());
            assertEquals(2, novaInstancia.listarPorAluno("A001").size());
        }
    }

    // =========================================================================
    // salvarDados — branch de IOException
    // =========================================================================

    @Nested
    @DisplayName("salvarDados — branch de IOException")
    class SalvarDadosIOException {

        @Test
        @DisplayName("salvar nao deve lancar excecao mesmo quando caminho de arquivo e invalido")
        void salvarNaoDeveLancarExcecaoComCaminhoInvalido() {
            // Força IOException em salvarDados ao usar subdiretório inexistente
            MatriculaTurmaRepository repo = new MatriculaTurmaRepository(
                    tempDir.resolve("subdir_inexistente").resolve("matriculas.json").toString());
            MatriculaTurma m = matriculaA1_MAT_T01();
            // Não deve propagar exceção; salvarDados captura silenciosamente a IOException
            assertDoesNotThrow(() -> repo.salvar(m));
        }

        @Test
        @DisplayName("atualizar nao deve lancar IOExcecao mesmo quando caminho de arquivo e invalido")
        void atualizarNaoDeveLancarExcecaoDeIOComCaminhoInvalido() {
            // Primeiro salva com repo válido
            MatriculaTurma m = matriculaA1_MAT_T01();
            repository.salvar(m);

            // Cria repo apontando para caminho inválido mas com a matrícula já na memória
            MatriculaTurmaRepository repoInvalido = new MatriculaTurmaRepository(
                    tempDir.resolve("subdir_inexistente").resolve("matriculas.json").toString());
            MatriculaTurma novaMatricula = matriculaA1_MAT_T01();
            repoInvalido.salvar(novaMatricula); // Não lança

            novaMatricula.setStatus(StatusMatricula.CONFIRMADA);
            assertDoesNotThrow(() -> repoInvalido.atualizar(novaMatricula));
        }
    }

    // =========================================================================
    // listarListaEsperaPorTurmaOrdenada — branch nullsLast
    // =========================================================================

    @Nested
    @DisplayName("listarListaEsperaPorTurmaOrdenada — nullsLast e casos de borda")
    class ListaEsperaNullsLast {

        @Test
        @DisplayName("Deve colocar entradas com dataSolicitacao nula ao final da ordenacao")
        void deveColocarDataNulaAoFinal() {
            MatriculaTurma comData = matriculaA1_MAT_T01();
            comData.setStatus(StatusMatricula.LISTA_ESPERA);
            comData.setDataSolicitacao(java.time.LocalDateTime.of(2026, 3, 10, 9, 0));

            MatriculaTurma semData = matriculaA2_MAT_T01();
            semData.setStatus(StatusMatricula.LISTA_ESPERA);
            semData.setDataSolicitacao(null); // Exercita o Comparator.nullsLast

            repository.salvar(semData); // salvo antes para garantir ordem invertida sem sort
            repository.salvar(comData);

            List<MatriculaTurma> resultado = repository.listarListaEsperaPorTurmaOrdenada("MAT001", "2026.1", "T01");

            assertEquals(2, resultado.size());
            // Com data deve vir primeiro; sem data (null) deve ser o último
            assertEquals("A001", resultado.get(0).getMatriculaAluno());
            assertNull(resultado.get(1).getDataSolicitacao());
        }

        @Test
        @DisplayName("Deve ordenar corretamente quando todas as datas sao nulas")
        void deveOrdenarQuandoTodasAsDatasSaoNulas() {
            MatriculaTurma m1 = matriculaA1_MAT_T01();
            m1.setStatus(StatusMatricula.LISTA_ESPERA);
            m1.setDataSolicitacao(null);

            MatriculaTurma m2 = matriculaA2_MAT_T01();
            m2.setStatus(StatusMatricula.LISTA_ESPERA);
            m2.setDataSolicitacao(null);

            repository.salvar(m1);
            repository.salvar(m2);

            List<MatriculaTurma> resultado = repository.listarListaEsperaPorTurmaOrdenada("MAT001", "2026.1", "T01");

            assertEquals(2, resultado.size());
            // Ambas com null — apenas verifica que não lança exceção e retorna as duas
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando turma nao tem alunos em espera")
        void deveRetornarVaziaParaTurmaSemEspera() {
            MatriculaTurma m = matriculaA1_MAT_T01();
            m.setStatus(StatusMatricula.CONFIRMADA); // Não é LISTA_ESPERA

            repository.salvar(m);

            List<MatriculaTurma> resultado = repository.listarListaEsperaPorTurmaOrdenada("MAT001", "2026.1", "T01");

            assertTrue(resultado.isEmpty());
        }
    }

    // =========================================================================
    // contarOcupadasPorTurma — branches adicionais
    // =========================================================================

    @Nested
    @DisplayName("contarOcupadasPorTurma — status REJEITADA nao conta")
    class ContarOcupadasAdicionais {

        @Test
        @DisplayName("Nao deve contar matricula REJEITADA como vaga ocupada")
        void naoDeveContarRejeitada() {
            MatriculaTurma m = matriculaA1_MAT_T01();
            m.setStatus(StatusMatricula.REJEITADA);
            repository.salvar(m);

            assertEquals(0L, repository.contarOcupadasPorTurma("MAT001", "2026.1", "T01"));
        }

        @Test
        @DisplayName("Deve contar PENDENTE e CONFIRMADA mas nao LISTA_ESPERA nem REJEITADA")
        void deveContarApenasAtivasNaoEspera() {
            MatriculaTurma confirmada = matriculaA1_MAT_T01();
            confirmada.setStatus(StatusMatricula.CONFIRMADA);

            MatriculaTurma pendente = matriculaA2_MAT_T01();
            pendente.setStatus(StatusMatricula.PENDENTE);

            MatriculaTurma espera = new MatriculaTurma("A003", "MAT001", "2026.1", "T01");
            espera.setStatus(StatusMatricula.LISTA_ESPERA);

            MatriculaTurma rejeitada = new MatriculaTurma("A004", "MAT001", "2026.1", "T01");
            rejeitada.setStatus(StatusMatricula.REJEITADA);

            MatriculaTurma cancelada = new MatriculaTurma("A005", "MAT001", "2026.1", "T01");
            cancelada.setStatus(StatusMatricula.CANCELADA);

            repository.salvar(confirmada);
            repository.salvar(pendente);
            repository.salvar(espera);
            repository.salvar(rejeitada);
            repository.salvar(cancelada);

            assertEquals(2L, repository.contarOcupadasPorTurma("MAT001", "2026.1", "T01"),
                    "Apenas CONFIRMADA e PENDENTE devem contar como vagas ocupadas");
        }
    }

    // =========================================================================
    // listarPorAlunoEStatus — status adicionais
    // =========================================================================

    @Nested
    @DisplayName("listarPorAlunoEStatus — REJEITADA e LISTA_ESPERA")
    class ListarPorAlunoEStatusAdicionais {

        @Test
        @DisplayName("Deve retornar matriculas REJEITADAS do aluno")
        void deveRetornarRejeitadas() {
            MatriculaTurma m = matriculaA1_MAT_T01();
            m.setStatus(StatusMatricula.REJEITADA);
            repository.salvar(m);

            List<MatriculaTurma> resultado = repository.listarPorAlunoEStatus("A001", StatusMatricula.REJEITADA);

            assertEquals(1, resultado.size());
            assertEquals(StatusMatricula.REJEITADA, resultado.get(0).getStatus());
        }

        @Test
        @DisplayName("Deve retornar vazia quando aluno nao tem REJEITADA")
        void deveRetornarVaziaParaRejeitadaInexistente() {
            repository.salvar(matriculaA1_MAT_T01()); // Status PENDENTE
            assertTrue(repository.listarPorAlunoEStatus("A001", StatusMatricula.REJEITADA).isEmpty());
        }

        @Test
        @DisplayName("Deve retornar matriculas em LISTA_ESPERA do aluno")
        void deveRetornarListaEspera() {
            MatriculaTurma m = matriculaA1_FIS_T01();
            m.setStatus(StatusMatricula.LISTA_ESPERA);
            repository.salvar(m);

            List<MatriculaTurma> resultado = repository.listarPorAlunoEStatus("A001", StatusMatricula.LISTA_ESPERA);

            assertEquals(1, resultado.size());
            assertEquals(StatusMatricula.LISTA_ESPERA, resultado.get(0).getStatus());
        }
    }
}
