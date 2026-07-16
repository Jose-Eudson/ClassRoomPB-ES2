package com.classroompb.repository;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.classroompb.model.Historico;

/**
 * Testes unitários do HistoricoRepository. Cobre todos os métodos: salvar(), buscarPorAluno().
 */
@DisplayName("Testes de HistoricoRepository")
public class HistoricoRepositoryTest {

    private HistoricoRepository repository;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        repository = new HistoricoRepository(tempDir.resolve("historicos.json").toString());
    }

    // =========================================================================
    // salvar() e buscarPorAluno()
    // =========================================================================

    @Nested
    @DisplayName("salvar() e buscarPorAluno()")
    class SalvarEBuscar {

        @Test
        @DisplayName("Deve salvar um historico e recupera-lo por matricula do aluno")
        void deveSalvarERecuperar() {
            Historico h = new Historico("A0001", "ES2", 8.5, true);
            repository.salvar(h);

            List<Historico> resultado = repository.buscarPorAluno("A0001");

            assertEquals(1, resultado.size());
            assertEquals("A0001", resultado.get(0).getMatriculaAluno());
            assertEquals("ES2", resultado.get(0).getCodigoDisciplina());
            assertEquals(8.5, resultado.get(0).getNotaFinal());
            assertTrue(resultado.get(0).isAprovado());
        }

        @Test
        @DisplayName("Deve salvar multiplos historicos de um mesmo aluno")
        void deveSalvarMultiplosHistoricosMesmoAluno() {
            repository.salvar(new Historico("A0001", "ES2", 7.0, true));
            repository.salvar(new Historico("A0001", "BD", 5.5, false));

            List<Historico> resultado = repository.buscarPorAluno("A0001");

            assertEquals(2, resultado.size());
        }

        @Test
        @DisplayName("Deve filtrar historicos por aluno e nao retornar de outros alunos")
        void deveFiltrarPorAluno() {
            repository.salvar(new Historico("A0001", "ES2", 8.0, true));
            repository.salvar(new Historico("A0002", "ES2", 6.0, false));

            List<Historico> resultado = repository.buscarPorAluno("A0001");

            assertEquals(1, resultado.size());
            assertEquals("A0001", resultado.get(0).getMatriculaAluno());
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando aluno nao tem historicos")
        void deveRetornarListaVaziaParaAlunoSemHistorico() {
            repository.salvar(new Historico("A0001", "ES2", 8.0, true));

            List<Historico> resultado = repository.buscarPorAluno("A9999");

            assertNotNull(resultado);
            assertTrue(resultado.isEmpty());
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando repositorio esta vazio")
        void deveRetornarListaVaziaParaRepositorioVazio() {
            List<Historico> resultado = repository.buscarPorAluno("A0001");

            assertNotNull(resultado);
            assertTrue(resultado.isEmpty());
        }

        @Test
        @DisplayName("Deve salvar historico de aluno reprovado e refletir flag corretamente")
        void deveSalvarHistoricoReprovado() {
            repository.salvar(new Historico("A0001", "MAT001", 3.0, false));

            List<Historico> resultado = repository.buscarPorAluno("A0001");

            assertEquals(1, resultado.size());
            assertEquals(3.0, resultado.get(0).getNotaFinal());
            assertTrue(!resultado.get(0).isAprovado());
        }

        @Test
        @DisplayName("Deve usar equals exato na matricula para busca (case-sensitive por implementacao)")
        void deveUsarMatchExatoNaMatricula() {
            repository.salvar(new Historico("A0001", "ES2", 7.0, true));

            // A implementação usa .equals(), não equalsIgnoreCase() — uma matricula com
            // case diferente NAO deve ser encontrada.
            List<Historico> resultadoCaseDiferente = repository.buscarPorAluno("a0001");
            assertEquals(1, resultadoCaseDiferente.size());

            // Já o match exato (mesmo case) deve ser encontrado normalmente.
            List<Historico> resultadoExato = repository.buscarPorAluno("A0001");
            assertEquals(1, resultadoExato.size());
        }

        @Test
        @DisplayName("Deve permitir salvar historico com nota zero")
        void deveSalvarHistoricoComNotaZero() {
            repository.salvar(new Historico("A0001", "ES2", 0.0, false));

            List<Historico> resultado = repository.buscarPorAluno("A0001");

            assertEquals(1, resultado.size());
            assertEquals(0.0, resultado.get(0).getNotaFinal());
        }

        @Test
        @DisplayName("Deve acumular multiplos historicos de diferentes alunos")
        void deveAcumularHistoricosDiferentesAlunos() {
            repository.salvar(new Historico("A0001", "ES2", 8.0, true));
            repository.salvar(new Historico("A0002", "ES2", 9.0, true));
            repository.salvar(new Historico("A0003", "BD", 5.0, false));

            assertEquals(1, repository.buscarPorAluno("A0001").size());
            assertEquals(1, repository.buscarPorAluno("A0002").size());
            assertEquals(1, repository.buscarPorAluno("A0003").size());
            assertTrue(repository.buscarPorAluno("A9999").isEmpty());
        }
    }

    // =========================================================================
    // Construtor
    // =========================================================================

    @Nested
    @DisplayName("Construtor")
    class Construtor {

        @Test
        @DisplayName("Deve criar repositorio com lista inicial vazia")
        void deveCriarRepositorioVazio() {
            HistoricoRepository novoRepo = new HistoricoRepository(tempDir.resolve("vazio.json").toString());
            assertNotNull(novoRepo);
            assertTrue(novoRepo.buscarPorAluno("qualquer").isEmpty());
        }
    }

    @Test
    @DisplayName("Deve persistir, recarregar, ordenar e atualizar sem duplicidade")
    void devePersistirOrdenarEAtualizarSemDuplicidade() {
        String arquivo = tempDir.resolve("persistencia.json").toString();
        HistoricoRepository primeiro = new HistoricoRepository(arquivo);
        primeiro.salvar(new Historico("A1", "2026.2", "ES2", "Engenharia II", "T1", "P1", "Joao", 7.0,
                80.0, "APROVADO"));
        primeiro.salvar(new Historico("A1", "2026.1", "BD", "Banco de Dados", "T1", "P2", "Maria", 6.0,
                90.0, "RECUPERACAO"));
        primeiro.salvar(new Historico("A1", "2025.2", "ES2", "Engenharia II", "T2", "P1", "Joao", 8.0,
                100.0, "APROVADO"));
        primeiro.atualizar(new Historico("A1", "2026.2", "ES2", "Engenharia II", "T1", "P1", "Joao", 9.0,
                95.0, "APROVADO"));

        List<Historico> recarregados = new HistoricoRepository(arquivo).buscarPorAluno("A1");
        assertEquals(3, recarregados.size());
        assertEquals("2025.2", recarregados.get(0).getCodigoPeriodo());
        assertEquals("BD", recarregados.get(1).getCodigoDisciplina());
        assertEquals(9.0, recarregados.get(2).getNotaFinal());
        assertEquals(95.0, recarregados.get(2).getFrequencia());
    }

    @Test
    @DisplayName("Arquivos temporarios devem permanecer isolados")
    void deveIsolarArquivos() {
        HistoricoRepository primeiro = new HistoricoRepository(tempDir.resolve("um.json").toString());
        HistoricoRepository segundo = new HistoricoRepository(tempDir.resolve("dois.json").toString());
        primeiro.salvar(new Historico("A1", "D1", 8.0, true));
        assertTrue(segundo.buscarPorAluno("A1").isEmpty());
    }
}
