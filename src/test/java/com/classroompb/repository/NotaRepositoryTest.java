package com.classroompb.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.classroompb.model.Nota;

class NotaRepositoryTest {

    private static final String ARQUIVO_TESTE = "notas_test.json";

    @AfterEach
    void limparArquivo() {

        File arquivo = new File(ARQUIVO_TESTE);

        if (arquivo.exists()) {
            arquivo.delete();
        }
    }

    @Test
    @DisplayName("Deve salvar uma nota")
    void deveSalvarNota() {

        NotaRepository repository = new NotaRepository(ARQUIVO_TESTE);

        Nota nota = new Nota("A0001", "ES2", "2026.1", "T01");

        nota.setEtapa1(8.5);
        nota.setEtapa2(9.0);

        repository.salvar(nota);

        List<Nota> notas = repository.listarTodas();

        assertEquals(1, notas.size());
        assertEquals(nota.getChaveUnica(), notas.get(0).getChaveUnica());
    }

    @Test
    @DisplayName("Deve buscar nota pela chave única")
    void deveBuscarPorChaveUnica() {

        NotaRepository repository = new NotaRepository(ARQUIVO_TESTE);

        Nota nota = new Nota("A0001", "ES2", "2026.1", "T01");

        repository.salvar(nota);

        Nota encontrada = repository.buscarPorChaveUnica("A0001", "ES2", "2026.1", "T01");

        assertNotNull(encontrada);
        assertEquals(nota.getChaveUnica(), encontrada.getChaveUnica());
    }

    @Test
    @DisplayName("Deve retornar null quando nota não existir")
    void deveRetornarNullQuandoNotaNaoExistir() {

        NotaRepository repository = new NotaRepository(ARQUIVO_TESTE);

        Nota encontrada = repository.buscarPorChaveUnica("A9999", "ES2", "2026.1", "T01");

        assertNull(encontrada);
    }

    @Test
    @DisplayName("Deve atualizar nota existente")
    void deveAtualizarNotaExistente() {

        NotaRepository repository = new NotaRepository(ARQUIVO_TESTE);

        Nota nota = new Nota("A0001", "ES2", "2026.1", "T01");

        nota.setEtapa1(5.0);
        nota.setEtapa2(6.0);

        repository.salvar(nota);

        nota.setEtapa1(9.5);
        nota.setEtapa2(10.0);

        repository.atualizar(nota);

        Nota atualizada = repository.buscarPorChaveUnica("A0001", "ES2", "2026.1", "T01");

        assertEquals(9.5, atualizada.getEtapa1());
        assertEquals(10.0, atualizada.getEtapa2());
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar nota inexistente")
    void deveLancarExcecaoAoAtualizarNotaInexistente() {

        NotaRepository repository = new NotaRepository(ARQUIVO_TESTE);

        Nota nota = new Nota("A0001", "ES2", "2026.1", "T01");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> repository.atualizar(nota));

        assertEquals("Nota não encontrada.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve listar todas as notas")
    void deveListarTodasAsNotas() {

        NotaRepository repository = new NotaRepository(ARQUIVO_TESTE);

        repository.salvar(new Nota("A0001", "ES2", "2026.1", "T01"));

        repository.salvar(new Nota("A0002", "SO", "2026.1", "T03"));

        List<Nota> notas = repository.listarTodas();

        assertEquals(2, notas.size());
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não houver notas")
    void deveRetornarListaVazia() {

        NotaRepository repository = new NotaRepository(ARQUIVO_TESTE);

        assertTrue(repository.listarTodas().isEmpty());
    }

    @Test
    @DisplayName("Deve salvar múltiplas notas")
    void deveSalvarMultiplasNotas() {

        NotaRepository repository = new NotaRepository(ARQUIVO_TESTE);

        for (int i = 1; i <= 5; i++) {

            repository.salvar(new Nota("A000" + i, "ES2", "2026.1", "T01"));
        }

        assertEquals(5, repository.listarTodas().size());
    }

    @Test
    @DisplayName("Busca deve ignorar diferença entre maiúsculas e minúsculas")
    void buscaDeveIgnorarMaiusculasEMinusculas() {

        NotaRepository repository = new NotaRepository(ARQUIVO_TESTE);

        Nota nota = new Nota("A0001", "ES2", "2026.1", "T01");

        repository.salvar(nota);

        Nota encontrada = repository.buscarPorChaveUnica("a0001", "es2", "2026.1", "t01");

        assertNotNull(encontrada);
    }

    @Test
    @DisplayName("Atualização deve preservar chave única")
    void atualizacaoDevePreservarChaveUnica() {

        NotaRepository repository = new NotaRepository(ARQUIVO_TESTE);

        Nota nota = new Nota("A0001", "ES2", "2026.1", "T01");

        repository.salvar(nota);

        String chave = nota.getChaveUnica();

        nota.setEtapa1(8.0);
        nota.setEtapa2(9.0);

        repository.atualizar(nota);

        Nota encontrada = repository.buscarPorChaveUnica("A0001", "ES2", "2026.1", "T01");

        assertEquals(chave, encontrada.getChaveUnica());
    }

    @Test
    @DisplayName("Deve persistir etapas corretamente")
    void devePersistirEtapasCorretamente() {

        NotaRepository repository = new NotaRepository(ARQUIVO_TESTE);

        Nota nota = new Nota("A0001", "ES2", "2026.1", "T01");

        nota.setEtapa1(7.3);
        nota.setEtapa2(8.8);

        repository.salvar(nota);

        Nota encontrada = repository.buscarPorChaveUnica("A0001", "ES2", "2026.1", "T01");

        assertEquals(7.3, encontrada.getEtapa1());
        assertEquals(8.8, encontrada.getEtapa2());
    }
}