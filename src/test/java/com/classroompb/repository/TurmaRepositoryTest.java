package com.classroompb.repository;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import com.classroompb.model.Turma;

/**
 * Testes de integração para TurmaRepository (RF10).
 *
 * Cobertura:
 *   - Construtores (padrão e com caminho)
 *   - carregarDados() — arquivo inexistente e caminho inválido
 *   - salvarDados()   — caminho somente-leitura
 *   - salvar()
 *   - listarTodos()   — cópia defensiva
 *   - existePorChaveUnica() — true, false, case-insensitive
 *   - listarPorPeriodo()
 *   - listarPorDisciplinaEPeriodo()
 *   - buscarPorChaveUnica() — encontrado e não encontrado
 *   - Persistência entre instâncias
 */
@DisplayName("Testes de TurmaRepository (RF10)")
public class TurmaRepositoryTest {

    @TempDir
    Path tempDir;

    private TurmaRepository repository;

    private String arquivoTemp() {
        return tempDir.resolve("turmas_test.json").toString();
    }

    @BeforeEach
    void setUp() {
        repository = new TurmaRepository(arquivoTemp());
    }

    // =========================================================================
    // Fixtures
    // =========================================================================

    private Turma turmaMAT_2026_T01() {
        return new Turma("T01", "MAT001", "2026.1", 40, "Seg/Qua 10h-12h", "Bloco A - 101", "P0001");
    }

    private Turma turmaMAT_2026_T02() {
        return new Turma("T02", "MAT001", "2026.1", 30, "Ter/Qui 14h-16h", "Bloco A - 102", null);
    }

    private Turma turmaFIS_2026_T01() {
        return new Turma("T01", "FIS001", "2026.1", 35, "Sex 08h-12h", "Bloco B - 201", "P0002");
    }

    private Turma turmaMAT_2025_T01() {
        return new Turma("T01", "MAT001", "2025.2", 40, "Seg 10h", "Bloco C - 301", null);
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
            assertTrue(repository.listarTodos().isEmpty());
        }

        @Test
        @DisplayName("Construtor padrão não deve lançar exceção")
        void construtorPadraoNaoLancaExcecao() {
            assertDoesNotThrow(() -> new TurmaRepository());
        }

        @Test
        @DisplayName("carregarDados deve iniciar lista vazia quando caminho aponta para diretório")
        void carregarDadosComCaminhoInvalidoInicialistaVazia() {
            // Um diretório no lugar de arquivo faz JsonUtil lançar IOException;
            // o catch deve tratar silenciosamente e iniciar com lista vazia
            String caminhoInvalido = tempDir.toString();
            TurmaRepository repo = new TurmaRepository(caminhoInvalido);
            assertNotNull(repo.listarTodos());
            assertTrue(repo.listarTodos().isEmpty());
        }
    }

    // =========================================================================
    // salvar() e listarTodos()
    // =========================================================================

    @Nested
    @DisplayName("salvar() e listarTodos()")
    class SalvarEListar {

        @Test
        @DisplayName("Deve salvar e listar uma turma")
        void deveSalvarEListarUmaTurma() {
            repository.salvar(turmaMAT_2026_T01());
            List<Turma> resultado = repository.listarTodos();
            assertEquals(1, resultado.size());
            assertEquals("T01", resultado.get(0).getCodigo());
        }

        @Test
        @DisplayName("Deve salvar múltiplas turmas")
        void deveSalvarMultiplasTurmas() {
            repository.salvar(turmaMAT_2026_T01());
            repository.salvar(turmaMAT_2026_T02());
            repository.salvar(turmaFIS_2026_T01());
            assertEquals(3, repository.listarTodos().size());
        }

        @Test
        @DisplayName("listarTodos deve retornar cópia defensiva — modificação externa não afeta o repositório")
        void listarTodosRetornaCopiaDefensiva() {
            repository.salvar(turmaMAT_2026_T01());
            List<Turma> lista = repository.listarTodos();
            lista.clear();
            assertEquals(1, repository.listarTodos().size());
        }

        @Test
        @DisplayName("Deve salvar turma sem professor (professor nulo)")
        void deveSalvarTurmaSemProfessor() {
            Turma semProf = new Turma("T03", "MAT001", "2026.1", 20, "Qui 08h", "Bloco D - 401", null);
            repository.salvar(semProf);
            Turma recuperada = repository.buscarPorChaveUnica("MAT001", "2026.1", "T03");
            assertNotNull(recuperada);
            assertNull(recuperada.getMatriculaProfessor());
        }

        @Test
        @DisplayName("salvarDados deve tratar silenciosamente IOException em caminho somente leitura")
        @DisabledOnOs(OS.WINDOWS)
        void salvarEmCaminhoSomenteLeitura() throws Exception {
            File dirReadOnly = tempDir.resolve("readonly").toFile();
            dirReadOnly.mkdir();
            dirReadOnly.setWritable(false);

            String caminhoInvalido = dirReadOnly.getAbsolutePath() + "/turmas.json";
            TurmaRepository repo = new TurmaRepository(caminhoInvalido);

            assertDoesNotThrow(() -> repo.salvar(turmaMAT_2026_T01()));
        }
    }

    // =========================================================================
    // Persistência entre instâncias
    // =========================================================================

    @Nested
    @DisplayName("Persistência entre instâncias")
    class PersistenciaEntreInstancias {

        @Test
        @DisplayName("Deve persistir turma salva e recuperá-la em nova instância")
        void devePersistirEntreInstancias() {
            repository.salvar(turmaMAT_2026_T01());

            TurmaRepository novaInstancia = new TurmaRepository(arquivoTemp());
            List<Turma> turmas = novaInstancia.listarTodos();

            assertEquals(1, turmas.size());
            assertEquals("T01",    turmas.get(0).getCodigo());
            assertEquals("MAT001", turmas.get(0).getCodigoDisciplina());
            assertEquals("2026.1", turmas.get(0).getCodigoPeriodo());
        }

        @Test
        @DisplayName("Deve persistir todos os campos da turma, incluindo vagas, horário e professor")
        void devePersistirTodosCampos() {
            repository.salvar(turmaMAT_2026_T01());

            TurmaRepository novaInstancia = new TurmaRepository(arquivoTemp());
            Turma recuperada = novaInstancia.listarTodos().get(0);

            assertEquals(40,               recuperada.getVagas());
            assertEquals("Seg/Qua 10h-12h", recuperada.getHorario());
            assertEquals("P0001",          recuperada.getMatriculaProfessor());
        }

        @Test
        @DisplayName("Deve persistir múltiplas turmas entre instâncias")
        void devePersistirMultiplasTurmasEntreInstancias() {
            repository.salvar(turmaMAT_2026_T01());
            repository.salvar(turmaFIS_2026_T01());

            TurmaRepository novaInstancia = new TurmaRepository(arquivoTemp());
            assertEquals(2, novaInstancia.listarTodos().size());
        }
    }

    // =========================================================================
    // existePorChaveUnica()
    // =========================================================================

    @Nested
    @DisplayName("existePorChaveUnica()")
    class ExistePorChaveUnica {

        @Test
        @DisplayName("Deve retornar true para turma existente")
        void deveRetornarTrueParaTurmaExistente() {
            repository.salvar(turmaMAT_2026_T01());
            assertTrue(repository.existePorChaveUnica("MAT001", "2026.1", "T01"));
        }

        @Test
        @DisplayName("Deve retornar false para turma inexistente")
        void deveRetornarFalseParaTurmaInexistente() {
            assertFalse(repository.existePorChaveUnica("MAT001", "2026.1", "T99"));
        }

        @Test
        @DisplayName("Deve retornar false quando repositório está vazio")
        void deveRetornarFalseParaRepositorioVazio() {
            assertFalse(repository.existePorChaveUnica("MAT001", "2026.1", "T01"));
        }

        @Test
        @DisplayName("existePorChaveUnica deve ser case-insensitive")
        void deveSerCaseInsensitive() {
            repository.salvar(turmaMAT_2026_T01());
            assertTrue(repository.existePorChaveUnica("mat001", "2026.1", "t01"));
            assertTrue(repository.existePorChaveUnica("MAT001", "2026.1", "T01"));
            assertTrue(repository.existePorChaveUnica("Mat001", "2026.1", "T01"));
        }

        @Test
        @DisplayName("Deve retornar false para mesmo código em disciplina diferente")
        void deveRetornarFalseParaDisciplinaDiferente() {
            repository.salvar(turmaMAT_2026_T01());
            assertFalse(repository.existePorChaveUnica("FIS001", "2026.1", "T01"));
        }

        @Test
        @DisplayName("Deve retornar false para mesma disciplina e código em período diferente")
        void deveRetornarFalseParaPeriodoDiferente() {
            repository.salvar(turmaMAT_2026_T01());
            assertFalse(repository.existePorChaveUnica("MAT001", "2025.2", "T01"));
        }

        @Test
        @DisplayName("Deve detectar corretamente entre múltiplas turmas cadastradas")
        void deveDetectarEntreMultiplasTurmas() {
            repository.salvar(turmaMAT_2026_T01());
            repository.salvar(turmaMAT_2026_T02());
            repository.salvar(turmaFIS_2026_T01());

            assertTrue(repository.existePorChaveUnica("MAT001", "2026.1", "T01"));
            assertTrue(repository.existePorChaveUnica("MAT001", "2026.1", "T02"));
            assertTrue(repository.existePorChaveUnica("FIS001", "2026.1", "T01"));
            assertFalse(repository.existePorChaveUnica("MAT001", "2026.1", "T03"));
        }
    }

    // =========================================================================
    // buscarPorChaveUnica()
    // =========================================================================

    @Nested
    @DisplayName("buscarPorChaveUnica()")
    class BuscarPorChaveUnica {

        @Test
        @DisplayName("Deve retornar a turma correta quando encontrada")
        void deveRetornarTurmaEncontrada() {
            repository.salvar(turmaMAT_2026_T01());
            Turma resultado = repository.buscarPorChaveUnica("MAT001", "2026.1", "T01");

            assertNotNull(resultado);
            assertEquals("T01",    resultado.getCodigo());
            assertEquals("MAT001", resultado.getCodigoDisciplina());
            assertEquals("2026.1", resultado.getCodigoPeriodo());
        }

        @Test
        @DisplayName("Deve retornar null para turma inexistente")
        void deveRetornarNullParaInexistente() {
            assertNull(repository.buscarPorChaveUnica("MAT001", "2026.1", "T99"));
        }

        @Test
        @DisplayName("Deve retornar null quando repositório está vazio")
        void deveRetornarNullParaRepositorioVazio() {
            assertNull(repository.buscarPorChaveUnica("MAT001", "2026.1", "T01"));
        }

        @Test
        @DisplayName("Deve ser case-insensitive na busca")
        void deveSerCaseInsensitive() {
            repository.salvar(turmaMAT_2026_T01());
            assertNotNull(repository.buscarPorChaveUnica("mat001", "2026.1", "t01"));
        }

        @Test
        @DisplayName("Deve retornar a turma correta entre múltiplas cadastradas")
        void deveRetornarTurmaCorretaEntreMultiplas() {
            repository.salvar(turmaMAT_2026_T01());
            repository.salvar(turmaFIS_2026_T01());
            repository.salvar(turmaMAT_2025_T01());

            Turma resultado = repository.buscarPorChaveUnica("FIS001", "2026.1", "T01");
            assertNotNull(resultado);
            assertEquals("FIS001", resultado.getCodigoDisciplina());
            assertEquals("2026.1", resultado.getCodigoPeriodo());
        }
    }

    // =========================================================================
    // listarPorPeriodo()
    // =========================================================================

    @Nested
    @DisplayName("listarPorPeriodo()")
    class ListarPorPeriodo {

        @Test
        @DisplayName("Deve retornar todas as turmas do período informado")
        void deveListarTurmasDeUmPeriodo() {
            repository.salvar(turmaMAT_2026_T01());
            repository.salvar(turmaMAT_2026_T02());
            repository.salvar(turmaFIS_2026_T01());
            repository.salvar(turmaMAT_2025_T01()); // outro período

            List<Turma> resultado = repository.listarPorPeriodo("2026.1");
            assertEquals(3, resultado.size());
        }

        @Test
        @DisplayName("Deve retornar lista vazia para período sem turmas")
        void deveRetornarListaVaziaParaPeriodoSemTurmas() {
            repository.salvar(turmaMAT_2026_T01());
            List<Turma> resultado = repository.listarPorPeriodo("2099.1");
            assertTrue(resultado.isEmpty());
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando repositório está vazio")
        void deveRetornarListaVaziaParaRepositorioVazio() {
            assertTrue(repository.listarPorPeriodo("2026.1").isEmpty());
        }

        @Test
        @DisplayName("listarPorPeriodo deve ser case-insensitive")
        void deveSerCaseInsensitive() {
            repository.salvar(turmaMAT_2026_T01());
            assertFalse(repository.listarPorPeriodo("2026.1").isEmpty());
        }

        @Test
        @DisplayName("Não deve incluir turmas de outros períodos")
        void naoDeveIncluirTurmasDeOutrosPeriodos() {
            repository.salvar(turmaMAT_2026_T01()); // 2026.1
            repository.salvar(turmaMAT_2025_T01()); // 2025.2

            List<Turma> resultado = repository.listarPorPeriodo("2026.1");
            assertEquals(1, resultado.size());
            assertEquals("2026.1", resultado.get(0).getCodigoPeriodo());
        }
    }

    // =========================================================================
    // listarPorDisciplinaEPeriodo()
    // =========================================================================

    @Nested
    @DisplayName("listarPorDisciplinaEPeriodo()")
    class ListarPorDisciplinaEPeriodo {

        @Test
        @DisplayName("Deve retornar todas as turmas da disciplina no período")
        void deveListarTurmasDaDisciplinaNoPeriodo() {
            repository.salvar(turmaMAT_2026_T01());
            repository.salvar(turmaMAT_2026_T02());
            repository.salvar(turmaFIS_2026_T01()); // outra disciplina

            List<Turma> resultado = repository.listarPorDisciplinaEPeriodo("MAT001", "2026.1");
            assertEquals(2, resultado.size());
        }

        @Test
        @DisplayName("Deve retornar lista vazia para disciplina sem turmas no período")
        void deveRetornarListaVaziaParaDisciplinaSemTurmas() {
            repository.salvar(turmaMAT_2026_T01());
            List<Turma> resultado = repository.listarPorDisciplinaEPeriodo("FIS001", "2026.1");
            assertTrue(resultado.isEmpty());
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando repositório está vazio")
        void deveRetornarListaVaziaParaRepositorioVazio() {
            assertTrue(repository.listarPorDisciplinaEPeriodo("MAT001", "2026.1").isEmpty());
        }

        @Test
        @DisplayName("Deve ser case-insensitive na disciplina e no período")
        void deveSerCaseInsensitive() {
            repository.salvar(turmaMAT_2026_T01());
            assertFalse(repository.listarPorDisciplinaEPeriodo("mat001", "2026.1").isEmpty());
        }

        @Test
        @DisplayName("Não deve incluir turmas da mesma disciplina em período diferente")
        void naoDeveIncluirTurmasDeOutroPeriodo() {
            repository.salvar(turmaMAT_2026_T01()); // MAT001 / 2026.1
            repository.salvar(turmaMAT_2025_T01()); // MAT001 / 2025.2

            List<Turma> resultado = repository.listarPorDisciplinaEPeriodo("MAT001", "2026.1");
            assertEquals(1, resultado.size());
            assertEquals("2026.1", resultado.get(0).getCodigoPeriodo());
        }

        @Test
        @DisplayName("Não deve incluir turmas de outra disciplina no mesmo período")
        void naoDeveIncluirTurmasDeOutraDisciplina() {
            repository.salvar(turmaMAT_2026_T01()); // MAT001
            repository.salvar(turmaFIS_2026_T01()); // FIS001

            List<Turma> resultado = repository.listarPorDisciplinaEPeriodo("MAT001", "2026.1");
            assertEquals(1, resultado.size());
            assertEquals("MAT001", resultado.get(0).getCodigoDisciplina());
        }
    }

    // =========================================================================
    // atualizar()
    // =========================================================================

    @Nested
    @DisplayName("atualizar()")
    class Atualizar {

        @Test
        @DisplayName("Deve atualizar os campos de uma turma existente")
        void deveAtualizarTurmaExistente() {
            repository.salvar(turmaMAT_2026_T01());

            Turma atualizada = repository.buscarPorChaveUnica("MAT001", "2026.1", "T01");
            atualizada.setVagas(99);
            atualizada.setHorario("Sex 14h-18h");
            atualizada.setSala("Bloco Z - 999");
            atualizada.setMatriculaProfessor("P9999");

            repository.atualizar(atualizada);

            Turma recuperada = repository.buscarPorChaveUnica("MAT001", "2026.1", "T01");
            assertEquals(99,             recuperada.getVagas());
            assertEquals("Sex 14h-18h", recuperada.getHorario());
            assertEquals("Bloco Z - 999", recuperada.getSala());
            assertEquals("P9999",        recuperada.getMatriculaProfessor());
        }

        @Test
        @DisplayName("Deve persistir a atualização em nova instância do repositório")
        void devePersistirAtualizacaoEntreInstancias() {
            repository.salvar(turmaMAT_2026_T01());

            Turma atualizada = repository.buscarPorChaveUnica("MAT001", "2026.1", "T01");
            atualizada.setVagas(55);
            repository.atualizar(atualizada);

            TurmaRepository novaInstancia = new TurmaRepository(arquivoTemp());
            assertEquals(55, novaInstancia.buscarPorChaveUnica("MAT001", "2026.1", "T01").getVagas());
        }

        @Test
        @DisplayName("Atualizar turma inexistente deve lançar IllegalArgumentException")
        void atualizarInexistenteLancaExcecao() {
            Turma fantasma = new Turma("T99", "XXX", "9999.9", 10, "Seg 10h", "A-000", null);
            assertThrows(IllegalArgumentException.class, () -> repository.atualizar(fantasma));
        }

        @Test
        @DisplayName("Deve manter o número total de turmas após atualização")
        void deveManterQuantidadeTurmasAposAtualizar() {
            repository.salvar(turmaMAT_2026_T01());
            repository.salvar(turmaMAT_2026_T02());

            Turma atualizada = repository.buscarPorChaveUnica("MAT001", "2026.1", "T01");
            atualizada.setVagas(88);
            repository.atualizar(atualizada);

            assertEquals(2, repository.listarTodos().size());
        }
    }

    // =========================================================================
    // deletar()
    // =========================================================================

    @Nested
    @DisplayName("deletar()")
    class Deletar {

        @Test
        @DisplayName("Deve remover a turma existente")
        void deveRemoverTurmaExistente() {
            repository.salvar(turmaMAT_2026_T01());
            repository.deletar("MAT001", "2026.1", "T01");
            assertNull(repository.buscarPorChaveUnica("MAT001", "2026.1", "T01"));
            assertTrue(repository.listarTodos().isEmpty());
        }

        @Test
        @DisplayName("Deve persistir a remoção em nova instância do repositório")
        void devePersistirRemocaoEntreInstancias() {
            repository.salvar(turmaMAT_2026_T01());
            repository.deletar("MAT001", "2026.1", "T01");

            TurmaRepository novaInstancia = new TurmaRepository(arquivoTemp());
            assertTrue(novaInstancia.listarTodos().isEmpty());
        }

        @Test
        @DisplayName("Deletar turma inexistente deve lançar IllegalArgumentException")
        void deletarInexistenteLancaExcecao() {
            assertThrows(IllegalArgumentException.class,
                    () -> repository.deletar("INEXISTENTE", "9999.9", "T99"));
        }

        @Test
        @DisplayName("Deve remover apenas a turma indicada, preservando as demais")
        void deveRemoverApenasATurmaIndicada() {
            repository.salvar(turmaMAT_2026_T01());
            repository.salvar(turmaMAT_2026_T02());
            repository.salvar(turmaFIS_2026_T01());

            repository.deletar("MAT001", "2026.1", "T01");

            assertEquals(2, repository.listarTodos().size());
            assertNull(repository.buscarPorChaveUnica("MAT001", "2026.1", "T01"));
            assertNotNull(repository.buscarPorChaveUnica("MAT001", "2026.1", "T02"));
            assertNotNull(repository.buscarPorChaveUnica("FIS001", "2026.1", "T01"));
        }

        @Test
        @DisplayName("deletar deve ser case-insensitive na chave")
        void deveSerCaseInsensitive() {
            repository.salvar(turmaMAT_2026_T01());
            assertDoesNotThrow(() -> repository.deletar("mat001", "2026.1", "t01"));
            assertTrue(repository.listarTodos().isEmpty());
        }
    }
}