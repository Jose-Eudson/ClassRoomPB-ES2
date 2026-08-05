package com.classroompb.repository;


import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.classroompb.model.Aula;

@DisplayName("Testes de AulaRepository")
public class AulaRepositoryTest {

    private Path arquivoTemporario;
    private AulaRepository repository;

    @BeforeEach
    void setUp() throws Exception {
        arquivoTemporario = Files.createTempFile("aulas-test", ".json");
        repository = new AulaRepository(arquivoTemporario.toString());
    }

    @AfterEach
    void tearDown() throws Exception {
        Files.deleteIfExists(arquivoTemporario);
    }

    @Test
    @DisplayName("Deve iniciar repositorio vazio")
    void deveIniciarRepositorioVazio() {
        List<Aula> aulas = repository.listarTodas();
        Assertions.assertNotNull(aulas);
        Assertions.assertTrue(aulas.isEmpty());
    }

    @Test
    @DisplayName("Deve salvar e listar todas as aulas")
    void deveSalvarEListarTodasAsAulas() {
        Aula aula1 = criarAula("A001", "D001", 1);
        Aula aula2 = criarAula("A002", "D001", 2);

        repository.salvar(aula1);
        repository.salvar(aula2);

        List<Aula> aulas = repository.listarTodas();

        Assertions.assertEquals(2, aulas.size());
        Assertions.assertTrue(aulas.stream().anyMatch(a -> a.getCodigo().equals("A001")));
        Assertions.assertTrue(aulas.stream().anyMatch(a -> a.getCodigo().equals("A002")));
    }

    @Test
    @DisplayName("Deve verificar existencia por codigo")
    void deveVerificarExistenciaPorCodigo() {
        repository.salvar(criarAula("A001", "D001", 1));

        Assertions.assertTrue(repository.existePorCodigo("A001"));
        Assertions.assertTrue(repository.existePorCodigo("a001"));
        Assertions.assertFalse(repository.existePorCodigo("A999"));
    }

    @Test
    @DisplayName("Deve buscar aula por codigo")
    void deveBuscarAulaPorCodigo() {
        repository.salvar(criarAula("A001", "D001", 1));

        Aula encontrada = repository.buscarPorCodigo("A001");
        Aula inexistente = repository.buscarPorCodigo("A999");

        Assertions.assertNotNull(encontrada);
        Assertions.assertEquals("A001", encontrada.getCodigo());
        Assertions.assertNull(inexistente);
    }

    @Test
    @DisplayName("Deve buscar aulas por diario")
    void deveBuscarAulasPorDiario() {
        repository.salvar(criarAula("A001", "D001", 1));
        repository.salvar(criarAula("A002", "D001", 2));
        repository.salvar(criarAula("A003", "D002", 1));

        List<Aula> resultados = repository.buscarPorDiario("D001");

        Assertions.assertEquals(2, resultados.size());
        Assertions.assertTrue(resultados.stream().allMatch(a -> a.getCodigoDiario().equals("D001")));
    }

    @Test
    @DisplayName("Deve buscar aula por diario e numero")
    void deveBuscarAulaPorDiarioENumero() {
        repository.salvar(criarAula("A001", "D001", 1));
        repository.salvar(criarAula("A002", "D001", 2));

        Aula resultado = repository.buscarPorDiarioENumero("D001", 2);

        Assertions.assertNotNull(resultado);
        Assertions.assertEquals("A002", resultado.getCodigo());
        Assertions.assertEquals(2, resultado.getNumero());
    }

    @Test
    @DisplayName("Deve atualizar aula existente")
    void deveAtualizarAulaExistente() {
        repository.salvar(criarAula("A001", "D001", 1));
        Aula atualizada = new Aula("A001", "D001", criarData(), "Conteudo atualizado", 1);

        repository.atualizar(atualizada);

        Aula encontrada = repository.buscarPorCodigo("A001");
        Assertions.assertNotNull(encontrada);
        Assertions.assertEquals("Conteudo atualizado", encontrada.getConteudo());
    }

    @Test
    @DisplayName("Deve lancar excecao ao atualizar aula inexistente")
    void deveLancarExcecaoAoAtualizarAulaInexistente() {
        Aula inexistente = criarAula("A999", "D001", 1);

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> repository.atualizar(inexistente));

        Assertions.assertTrue(exception.getMessage().contains("A999"));
    }

    @Test
    @DisplayName("Deve deletar aula existente")
    void deveDeletarAulaExistente() {
        repository.salvar(criarAula("A001", "D001", 1));

        repository.deletar("A001");

        Assertions.assertEquals(0, repository.listarTodas().size());
        Assertions.assertNull(repository.buscarPorCodigo("A001"));
    }

    @Test
    void devePersistirDuracaoDaAula() {
        repository.salvar(new Aula("A001", "D001", criarData(), "Conteudo", 1, 2.5));
        AulaRepository recarregado = new AulaRepository(arquivoTemporario.toString());
        Assertions.assertEquals(2.5, recarregado.buscarPorCodigo("A001").getDuracaoHoras());
    }

    @Test
    void deveLerJsonAntigoSemDuracao() throws Exception {
        Files.writeString(arquivoTemporario,
                "[{\"codigo\":\"A001\",\"codigoDiario\":\"D001\",\"data\":\"2026-08-01\","
                        + "\"conteudo\":\"Legado\",\"numero\":1}]",
                StandardCharsets.UTF_8);
        AulaRepository legado = new AulaRepository(arquivoTemporario.toString());
        Assertions.assertEquals(0.0, legado.buscarPorCodigo("A001").getDuracaoHoras());
    }

    private Aula criarAula(String codigo, String codigoDiario, int numero) {
        return new Aula(codigo, codigoDiario, criarData(), "Conteudo de teste", numero);
    }

    private java.time.LocalDate criarData() {
        return java.time.LocalDate.of(2026, 8, 1);
    }
}
