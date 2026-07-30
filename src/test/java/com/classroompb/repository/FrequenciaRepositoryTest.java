package com.classroompb.repository;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.classroompb.model.RegistroFrequencia;
import com.classroompb.model.StatusFrequencia;

/**
 * RF27: Testes do repositorio de frequencia.
 */
@DisplayName("RF27 - FrequenciaRepository")
@SuppressWarnings("PMD.TooManyStaticImports")
public class FrequenciaRepositoryTest {

    @TempDir
    Path tempDir;

    private FrequenciaRepository repository;

    private static final LocalDate DATA_AULA = LocalDate.of(2026, 4, 10);
    private static final String COD_AULA = "A01";

    @BeforeEach
    void setUp() {
        repository = new FrequenciaRepository(tempDir.resolve("frequencias_test.json").toString());
    }

    private RegistroFrequencia frequencia(String matriculaAluno, StatusFrequencia status) {
        return new RegistroFrequencia(matriculaAluno, "MAT001", "2026.1", "T01", COD_AULA, DATA_AULA, status, "P0001");
    }

    @Test
    @DisplayName("Deve salvar e listar registros de frequencia")
    void deveSalvarEListar() {
        repository.salvar(frequencia("A0001", StatusFrequencia.PRESENTE));

        List<RegistroFrequencia> resultado = repository.listarTodas();

        assertEquals(1, resultado.size());
        assertEquals("A0001", resultado.get(0).getMatriculaAluno());
    }

    @Test
    @DisplayName("Deve buscar registro pela chave unica")
    void deveBuscarPorChaveUnica() {
        repository.salvar(frequencia("A0001", StatusFrequencia.FALTA));

        RegistroFrequencia resultado = repository.buscarPorChaveUnica("A0001", "MAT001", "2026.1", "T01", DATA_AULA,
                COD_AULA);

        assertNotNull(resultado);
        assertEquals(StatusFrequencia.FALTA, resultado.getStatus());
    }

    @Test
    @DisplayName("Deve atualizar registro existente")
    void deveAtualizarRegistro() {
        RegistroFrequencia registro = frequencia("A0001", StatusFrequencia.FALTA);
        repository.salvar(registro);

        registro.setStatus(StatusFrequencia.PRESENTE);
        repository.atualizar(registro);

        RegistroFrequencia atualizado = repository.buscarPorChaveUnica("A0001", "MAT001", "2026.1", "T01", DATA_AULA,
                COD_AULA);
        assertEquals(StatusFrequencia.PRESENTE, atualizado.getStatus());
        assertEquals(1, repository.listarTodas().size());
    }

    @Test
    @DisplayName("Deve listar frequencias por turma e data")
    void deveListarPorTurmaEData() {
        repository.salvar(frequencia("A0001", StatusFrequencia.PRESENTE));
        repository.salvar(frequencia("A0002", StatusFrequencia.FALTA));
        repository.salvar(new RegistroFrequencia("A0003", "MAT001", "2026.1", "T01", COD_AULA, DATA_AULA.plusDays(1),
                StatusFrequencia.PRESENTE, "P0001"));

        List<RegistroFrequencia> resultado = repository.listarPorTurmaEData("MAT001", "2026.1", "T01", DATA_AULA);

        assertEquals(2, resultado.size());
    }

    @Test
    @DisplayName("Deve persistir registros entre instancias")
    void devePersistirEntreInstancias() {
        String caminho = tempDir.resolve("frequencias_persistidas.json").toString();
        FrequenciaRepository primeira = new FrequenciaRepository(caminho);
        primeira.salvar(frequencia("A0001", StatusFrequencia.PRESENTE));

        FrequenciaRepository segunda = new FrequenciaRepository(caminho);

        assertEquals(1, segunda.listarTodas().size());
        assertEquals(StatusFrequencia.PRESENTE, segunda.listarTodas().get(0).getStatus());
    }

    @Test
    @DisplayName("Atualizar registro inexistente deve lancar IllegalArgumentException")
    void atualizarInexistenteDeveLancar() {
        RegistroFrequencia registro = frequencia("A9999", StatusFrequencia.PRESENTE);

        assertThrows(IllegalArgumentException.class, () -> repository.atualizar(registro));
    }

    @Test
    @DisplayName("Deve retornar null ao buscar chave inexistente")
    void deveBuscarPorChaveUnicaRetornaNullQuandoNaoEncontrado() {
        RegistroFrequencia resultado = repository.buscarPorChaveUnica("A9999", "MAT001", "2026.1", "T01", DATA_AULA,
                COD_AULA);
        assertNull(resultado);
    }

    @Test
    @DisplayName("Deve listar frequencias por aluno")
    void deveListarPorAluno() {
        repository.salvar(frequencia("A0001", StatusFrequencia.PRESENTE));
        repository.salvar(new RegistroFrequencia("A0002", "MAT001", "2026.1", "T01", COD_AULA, DATA_AULA,
                StatusFrequencia.FALTA, "P0001"));

        List<RegistroFrequencia> resultado = repository.listarPorAluno("A0001");

        assertEquals(1, resultado.size());
        assertEquals("A0001", resultado.get(0).getMatriculaAluno());
    }

    @Test
    @DisplayName("Deve listar frequencias por aluno ignorando case")
    void deveListarPorAlunoCaseInsensitive() {
        repository.salvar(frequencia("A0001", StatusFrequencia.PRESENTE));

        List<RegistroFrequencia> resultado = repository.listarPorAluno("a0001");

        assertEquals(1, resultado.size());
    }

    @Test
    @DisplayName("Deve listar frequencias por aluno e turma")
    void deveListarPorAlunoETurma() {
        repository.salvar(frequencia("A0001", StatusFrequencia.PRESENTE));
        repository.salvar(new RegistroFrequencia("A0001", "FIS001", "2026.1", "T01", COD_AULA, DATA_AULA,
                StatusFrequencia.FALTA, "P0001"));

        List<RegistroFrequencia> resultado = repository.listarPorAlunoETurma("A0001", "MAT001", "2026.1", "T01");

        assertEquals(1, resultado.size());
        assertEquals(StatusFrequencia.PRESENTE, resultado.get(0).getStatus());
    }

    @Test
    @DisplayName("Deve retornar lista vazia ao listar por aluno sem registros")
    void deveRetornarListaVaziaAoListarPorAlunoSemRegistros() {
        List<RegistroFrequencia> resultado = repository.listarPorAluno("A9999");
        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("Deve buscar por chave unica ignorando case")
    void deveBuscarPorChaveUnicaCaseInsensitive() {
        repository.salvar(frequencia("A0001", StatusFrequencia.PRESENTE));

        RegistroFrequencia resultado = repository.buscarPorChaveUnica("a0001", "mat001", "2026.1", "t01", DATA_AULA,
                COD_AULA);

        assertNotNull(resultado);
        assertEquals(StatusFrequencia.PRESENTE, resultado.getStatus());
    }

    @Test
    @DisplayName("Deve listar por turma e data ignorando case")
    void deveListarPorTurmaEDataCaseInsensitive() {
        repository.salvar(frequencia("A0001", StatusFrequencia.FALTA));

        List<RegistroFrequencia> resultado = repository.listarPorTurmaEData("mat001", "2026.1", "t01", DATA_AULA);

        assertEquals(1, resultado.size());
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando data nao tem registros")
    void deveRetornarVazioParaDataSemRegistros() {
        repository.salvar(frequencia("A0001", StatusFrequencia.PRESENTE));

        List<RegistroFrequencia> resultado = repository.listarPorTurmaEData("MAT001", "2026.1", "T01",
                DATA_AULA.plusDays(10));

        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("Deve iniciar lista nao nula quando arquivo de dados e invalido")
    void deveIniciarListaNaoNulaComCaminhoInvalido(@TempDir Path dir) {
        FrequenciaRepository repo = new FrequenciaRepository(dir.toString()); // diretório, não arquivo
        assertNotNull(repo.listarTodas());
    }

    // =========================================================================
    // Branches do operador && em listarPorTurmaEData / listarPorAlunoETurma
    // =========================================================================

    @Nested
    @DisplayName("Branches dos filtros compostos (curto-circuito do &&)")
    class BranchesDosFiltrosCompostos {

        @BeforeEach
        void cenarioComUmRegistroDeReferencia() {
            repository.salvar(frequencia("A0001", StatusFrequencia.PRESENTE));
        }

        @Test
        @DisplayName("listarPorTurmaEData: disciplina diferente deve resultar em lista vazia")
        void listarPorTurmaEDataComDisciplinaDiferente() {
            List<RegistroFrequencia> resultado = repository.listarPorTurmaEData("OUTRA_DISC", "2026.1", "T01",
                    DATA_AULA);
            assertTrue(resultado.isEmpty());
        }

        @Test
        @DisplayName("listarPorTurmaEData: periodo diferente (disciplina igual) deve resultar em lista vazia")
        void listarPorTurmaEDataComPeriodoDiferente() {
            List<RegistroFrequencia> resultado = repository.listarPorTurmaEData("MAT001", "2099.9", "T01", DATA_AULA);
            assertTrue(resultado.isEmpty());
        }

        @Test
        @DisplayName("listarPorTurmaEData: turma diferente (disciplina e periodo iguais) deve resultar em lista vazia")
        void listarPorTurmaEDataComTurmaDiferente() {
            List<RegistroFrequencia> resultado = repository.listarPorTurmaEData("MAT001", "2026.1", "T99", DATA_AULA);
            assertTrue(resultado.isEmpty());
        }

        @Test
        @DisplayName("listarPorAlunoETurma: matricula diferente deve resultar em lista vazia")
        void listarPorAlunoETurmaComMatriculaDiferente() {
            List<RegistroFrequencia> resultado = repository.listarPorAlunoETurma("A9999", "MAT001", "2026.1", "T01");
            assertTrue(resultado.isEmpty());
        }

        @Test
        @DisplayName("listarPorAlunoETurma: periodo diferente (matricula e disciplina iguais) deve resultar em lista vazia")
        void listarPorAlunoETurmaComPeriodoDiferente() {
            List<RegistroFrequencia> resultado = repository.listarPorAlunoETurma("A0001", "MAT001", "2099.9", "T01");
            assertTrue(resultado.isEmpty());
        }

        @Test
        @DisplayName("listarPorAlunoETurma: turma diferente (matricula, disciplina e periodo iguais) deve resultar em lista vazia")
        void listarPorAlunoETurmaComTurmaDiferente() {
            List<RegistroFrequencia> resultado = repository.listarPorAlunoETurma("A0001", "MAT001", "2026.1", "T99");
            assertTrue(resultado.isEmpty());
        }
    }

    // =========================================================================
    // Branch do loop de atualizar() quando há itens não correspondentes
    // =========================================================================

    @Test
    @DisplayName("atualizar deve percorrer registros existentes nao correspondentes antes de lancar excecao")
    void atualizarDevePercorrerRegistrosNaoCorrespondentesAntesDeLancar() {
        // Garante que o "if" dentro do loop seja avaliado como falso ao menos uma vez
        // (lista não vazia, porém sem nenhuma chave correspondente) antes da exceção.
        repository.salvar(frequencia("A0001", StatusFrequencia.PRESENTE));

        RegistroFrequencia naoExistente = frequencia("A9999", StatusFrequencia.FALTA);

        assertThrows(IllegalArgumentException.class, () -> repository.atualizar(naoExistente));
        assertEquals(1, repository.listarTodas().size());
    }

    // =========================================================================
    // Construtor padrão e branches de IOException
    // =========================================================================

    @Nested
    @DisplayName("Construtor padrao e branches de IO")
    class ConstrutorPadraoEIO {

        @Test
        @DisplayName("Construtor padrao deve criar repositorio com lista nao nula")
        void construtorPadraoDeveCriarRepositorioComListaNaoNula() {
            // Exercita o construtor padrão (linha 23-24)
            FrequenciaRepository repo = new FrequenciaRepository();
            assertNotNull(repo.listarTodas());
        }

        @Test
        @DisplayName("Construtor com caminho inexistente deve iniciar com lista vazia (catch IOException)")
        void construtorComCaminhoInexistenteDeveIniciarVazio() {
            // Exercita o catch de IOException em carregarDados (linhas 34-36)
            FrequenciaRepository repo = new FrequenciaRepository("/caminho/que/nao/existe/frequencias.json");
            assertNotNull(repo.listarTodas());
            assertTrue(repo.listarTodas().isEmpty());
        }

        @Test
        @DisplayName("salvarDados deve lidar com falha silenciosamente (diretorio como caminho)")
        void salvarDadosDeveLidarComFalhaSilenciosamente(@TempDir Path dir) {
            // Exercita o catch de IOException em salvarDados (linhas 43-44)
            // Usar subdiretório inexistente força IOException ao salvar
            FrequenciaRepository repo = new FrequenciaRepository(
                    dir.resolve("subdir_nao_existe").resolve("frequencias.json").toString());
            // Não deve lançar exceção; internamente captura a IOException
            assertDoesNotThrow(() -> repo.salvar(frequencia("A0001", StatusFrequencia.PRESENTE)));
        }

        @Test
        @DisplayName("Construtor com arquivo JSON valido deve carregar os dados existentes")
        void construtorComArquivoValidoDeveCarregarDados(@TempDir Path dir) throws Exception {
            // Primeiro cria um repositório e salva um registro no arquivo
            Path arquivo = dir.resolve("freq.json");
            FrequenciaRepository repo1 = new FrequenciaRepository(arquivo.toString());
            repo1.salvar(frequencia("A0001", StatusFrequencia.PRESENTE));

            // Depois cria outro repositório apontando pro mesmo arquivo — deve recarregar
            FrequenciaRepository repo2 = new FrequenciaRepository(arquivo.toString());
            assertEquals(1, repo2.listarTodas().size());
            assertEquals("A0001", repo2.listarTodas().get(0).getMatriculaAluno());
        }
    }
}
