package com.classroompb.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.classroompb.model.Diario;
import com.classroompb.model.SituacaoDiario;

public class DiarioRepositoryTest {

    private Path arquivoTemporario;
    private DiarioRepository repository;

    @BeforeEach
    void setUp() throws Exception {
        arquivoTemporario = Files.createTempFile("diarios-test", ".json");
        repository = new DiarioRepository(arquivoTemporario.toString());
    }

    @AfterEach
    void tearDown() throws Exception {
        Files.deleteIfExists(arquivoTemporario);
    }

    @Test
    void deveSalvarEListarTodosOsDiarios() {
        Diario diario1 = criarDiario("D001", "T001", "P001", "Diário 1");
        Diario diario2 = criarDiario("D002", "T002", "P002", "Diário 2");

        repository.salvar(diario1);
        repository.salvar(diario2);

        List<Diario> diarios = repository.listarTodos();

        assertEquals(2, diarios.size());
        assertTrue(diarios.stream().anyMatch(d -> d.getCodigo().equals("D001")));
        assertTrue(diarios.stream().anyMatch(d -> d.getCodigo().equals("D002")));
    }

    @Test
    void deveVerificarSeCodigoJaExiste() {
        repository.salvar(criarDiario("D001", "T001", "P001", "Diário 1"));

        assertTrue(repository.existePorCodigo("D001"));
        assertFalse(repository.existePorCodigo("D999"));
    }

    @Test
    void deveBuscarDiarioPorCodigo() {
        repository.salvar(criarDiario("D001", "T001", "P001", "Diário 1"));

        Diario encontrado = repository.buscarPorCodigo("D001");
        Diario inexistente = repository.buscarPorCodigo("D999");

        assertNotNull(encontrado);
        assertEquals("D001", encontrado.getCodigo());
        assertNull(inexistente);
    }

    @Test
    void deveBuscarDiariosPorTurma() {
        repository.salvar(criarDiario("D001", "T001", "P001", "Diário 1"));
        repository.salvar(criarDiario("D002", "T001", "P002", "Diário 2"));
        repository.salvar(criarDiario("D003", "T002", "P003", "Diário 3"));

        List<Diario> encontrados = repository.buscarPorTurma("T001");

        assertEquals(2, encontrados.size());
        assertTrue(encontrados.stream().allMatch(d -> d.getCodigoTurma().equals("T001")));
    }

    @Test
    void deveBuscarDiariosPorProfessor() {
        repository.salvar(criarDiario("D001", "T001", "P001", "Diário 1"));
        repository.salvar(criarDiario("D002", "T002", "P001", "Diário 2"));
        repository.salvar(criarDiario("D003", "T003", "P003", "Diário 3"));

        List<Diario> encontrados = repository.buscarPorProfessor("P001");

        assertEquals(2, encontrados.size());
        assertTrue(encontrados.stream().allMatch(d -> d.getMatriculaProfessor().equals("P001")));
    }

    @Test
    void deveAtualizarDiarioExistente() {
        repository.salvar(criarDiario("D001", "T001", "P001", "Diário antigo"));

        Diario atualizado = criarDiario("D001", "T999", "P999", "Diário atualizado");
        repository.atualizar(atualizado);

        Diario encontrado = repository.buscarPorCodigo("D001");

        assertNotNull(encontrado);
        assertEquals("Diário atualizado", encontrado.getDescricao());
        assertEquals("T999", encontrado.getCodigoTurma());
        assertEquals("P999", encontrado.getMatriculaProfessor());
    }

    @Test
    void deveLancarExcecaoAoAtualizarDiarioInexistente() {
        Diario inexistente = criarDiario("D999", "T999", "P999", "Inexistente");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> repository.atualizar(inexistente));

        assertTrue(exception.getMessage().contains("D999"));
    }

    @Test
    void deveDeletarDiarioExistente() {
        repository.salvar(criarDiario("D001", "T001", "P001", "Diário 1"));

        repository.deletar("D001");

        assertEquals(0, repository.listarTodos().size());
        assertNull(repository.buscarPorCodigo("D001"));
    }

    @Test
    void deveLancarExcecaoAoDeletarDiarioInexistente() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> repository.deletar("D999"));

        assertTrue(exception.getMessage().contains("D999"));
    }

    private Diario criarDiario(String codigo, String codigoTurma, String matriculaProfessor, String descricao) {
        return new Diario(codigo, codigoTurma, descricao, matriculaProfessor, "08:00 - 10:00", "Sala 101", 80,
                SituacaoDiario.values()[0]);
    }
}