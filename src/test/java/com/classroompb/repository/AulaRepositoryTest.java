package com.classroompb.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
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
        assertNotNull(aulas);
        assertTrue(aulas.isEmpty());
    }

    @Test
    @DisplayName("Deve salvar e listar todas as aulas")
    void deveSalvarEListarTodasAsAulas() {
        Aula aula1 = criarAula("A001", "D001", 1);
        Aula aula2 = criarAula("A002", "D001", 2);

        repository.salvar(aula1);
        repository.salvar(aula2);

        List<Aula> aulas = repository.listarTodas();

        assertEquals(2, aulas.size());
        assertTrue(aulas.stream().anyMatch(a -> a.getCodigo().equals("A001")));
        assertTrue(aulas.stream().anyMatch(a -> a.getCodigo().equals("A002")));
    }

    @Test
    @DisplayName("Deve verificar existencia por codigo")
    void deveVerificarExistenciaPorCodigo() {
        repository.salvar(criarAula("A001", "D001", 1));

        assertTrue(repository.existePorCodigo("A001"));
        assertTrue(repository.existePorCodigo("a001"));
        assertFalse(repository.existePorCodigo("A999"));
    }

    @Test
    @DisplayName("Deve buscar aula por codigo")
    void deveBuscarAulaPorCodigo() {
        repository.salvar(criarAula("A001", "D001", 1));

        Aula encontrada = repository.buscarPorCodigo("A001");
        Aula inexistente = repository.buscarPorCodigo("A999");

        assertNotNull(encontrada);
        assertEquals("A001", encontrada.getCodigo());
        assertNull(inexistente);
    }

    @Test
    @DisplayName("Deve buscar aulas por diario")
    void deveBuscarAulasPorDiario() {
        repository.salvar(criarAula("A001", "D001", 1));
        repository.salvar(criarAula("A002", "D001", 2));
        repository.salvar(criarAula("A003", "D002", 1));

        List<Aula> resultados = repository.buscarPorDiario("D001");

        assertEquals(2, resultados.size());
        assertTrue(resultados.stream().allMatch(a -> a.getCodigoDiario().equals("D001")));
    }

    @Test
    @DisplayName("Deve buscar aula por diario e numero")
    void deveBuscarAulaPorDiarioENumero() {
        repository.salvar(criarAula("A001", "D001", 1));
        repository.salvar(criarAula("A002", "D001", 2));

        Aula resultado = repository.buscarPorDiarioENumero("D001", 2);

        assertNotNull(resultado);
        assertEquals("A002", resultado.getCodigo());
        assertEquals(2, resultado.getNumero());
    }

    @Test
    @DisplayName("Deve atualizar aula existente")
    void deveAtualizarAulaExistente() {
        repository.salvar(criarAula("A001", "D001", 1));
        Aula atualizada = new Aula("A001", "D001", criarData(), "Conteudo atualizado", 1);

        repository.atualizar(atualizada);

        Aula encontrada = repository.buscarPorCodigo("A001");
        assertNotNull(encontrada);
        assertEquals("Conteudo atualizado", encontrada.getConteudo());
    }

    @Test
    @DisplayName("Deve lancar excecao ao atualizar aula inexistente")
    void deveLancarExcecaoAoAtualizarAulaInexistente() {
        Aula inexistente = criarAula("A999", "D001", 1);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> repository.atualizar(inexistente));

        assertTrue(exception.getMessage().contains("A999"));
    }

    @Test
    @DisplayName("Deve deletar aula existente")
    void deveDeletarAulaExistente() {
        repository.salvar(criarAula("A001", "D001", 1));

        repository.deletar("A001");

        assertEquals(0, repository.listarTodas().size());
        assertNull(repository.buscarPorCodigo("A001"));
    }

    private Aula criarAula(String codigo, String codigoDiario, int numero) {
        return new Aula(codigo, codigoDiario, criarData(), "Conteudo de teste", numero);
    }

    private java.time.LocalDate criarData() {
        return java.time.LocalDate.of(2026, 8, 1);
    }
}
