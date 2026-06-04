package com.classroompb.repository;

import java.nio.file.Path;
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
 * Cobertura:
 *   - Construtores (padrão e com caminho)
 *   - salvar() e listarTodas()
 *   - existeSolicitacaoAtiva()
 *   - listarPorAluno()
 *   - listarPorAlunoEStatus()
 *   - listarPorTurma()
 *   - contarConfirmadasPorTurma()
 *   - buscarPorChaveUnica()
 *   - atualizar()
 *   - case-insensitive
 *   - Persistência entre instâncias
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
            repository.salvar(matriculaA1_MAT_T01());  // T01
            repository.salvar(matriculaA2_MAT_T02());  // T02

            List<MatriculaTurma> resultado = repository.listarPorTurma("MAT001", "2026.1", "T01");
            assertEquals(1, resultado.size());
            assertEquals("T01", resultado.get(0).getCodigoTurma());
        }

        @Test
        @DisplayName("Não deve incluir matrículas de outro período")
        void naoDeveIncluirOutroPeriodo() {
            repository.salvar(matriculaA1_MAT_T01());       // 2026.1
            repository.salvar(matriculaA3_MAT_T01_2025());  // 2025.2

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
    // contarConfirmadasPorTurma()
    // =========================================================================

    @Nested
    @DisplayName("contarConfirmadasPorTurma()")
    class ContarConfirmadasPorTurma {

        @Test
        @DisplayName("Deve retornar 0 quando não há confirmadas")
        void deveRetornarZeroQuandoNaoHaConfirmadas() {
            repository.salvar(matriculaA1_MAT_T01()); // PENDENTE
            assertEquals(0L, repository.contarConfirmadasPorTurma("MAT001", "2026.1", "T01"));
        }

        @Test
        @DisplayName("Deve contar apenas matrículas com status CONFIRMADA")
        void deveContarApenasConfirmadas() {
            MatriculaTurma m1 = matriculaA1_MAT_T01();
            m1.setStatus(StatusMatricula.CONFIRMADA);
            repository.salvar(m1);
            repository.salvar(matriculaA2_MAT_T01()); // PENDENTE

            assertEquals(1L, repository.contarConfirmadasPorTurma("MAT001", "2026.1", "T01"));
        }

        @Test
        @DisplayName("Não deve contar confirmadas de outra turma")
        void naoDeveContarOutraTurma() {
            MatriculaTurma confirmada = matriculaA1_MAT_T01();
            confirmada.setStatus(StatusMatricula.CONFIRMADA);
            repository.salvar(confirmada);

            assertEquals(0L, repository.contarConfirmadasPorTurma("MAT001", "2026.1", "T02"));
        }

        @Test
        @DisplayName("Deve retornar 0 quando repositório está vazio")
        void deveRetornarZeroParaRepositorioVazio() {
            assertEquals(0L, repository.contarConfirmadasPorTurma("MAT001", "2026.1", "T01"));
        }

        @Test
        @DisplayName("Não deve contar matrículas CANCELADAS")
        void naoDeveContarCanceladas() {
            MatriculaTurma cancelada = matriculaA1_MAT_T01();
            cancelada.setStatus(StatusMatricula.CANCELADA);
            repository.salvar(cancelada);

            assertEquals(0L, repository.contarConfirmadasPorTurma("MAT001", "2026.1", "T01"));
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
            assertEquals("A001",   encontrada.getMatriculaAluno());
            assertEquals("MAT001", encontrada.getCodigoDisciplina());
            assertEquals("2026.1", encontrada.getCodigoPeriodo());
            assertEquals("T01",    encontrada.getCodigoTurma());
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
        @DisplayName("contarConfirmadasPorTurma deve retornar valor correto após reload")
        void contarConfirmadasAposReload() {
            MatriculaTurma m1 = matriculaA1_MAT_T01();
            m1.setStatus(StatusMatricula.CONFIRMADA);
            MatriculaTurma m2 = matriculaA2_MAT_T01();
            m2.setStatus(StatusMatricula.CONFIRMADA);

            repository.salvar(m1);
            repository.salvar(m2);

            MatriculaTurmaRepository novaInstancia = new MatriculaTurmaRepository(arquivoTemp());
            assertEquals(2L, novaInstancia.contarConfirmadasPorTurma("MAT001", "2026.1", "T01"));
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
}