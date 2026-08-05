package com.classroompb.repository;

import java.nio.file.Path;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.classroompb.model.Avaliacao;

class AvaliacaoRepositoryTest {
    @TempDir
    Path tempDir;

    private String arquivo;
    private AvaliacaoRepository repository;

    @BeforeEach
    void preparar() {
        arquivo = tempDir.resolve("avaliacoes.json").toString();
        repository = new AvaliacaoRepository(arquivo);
    }

    @Test
    void deveSalvarBuscarListarERecarregar() {
        repository.salvar(avaliacao("AV1", "D1"));
        repository.salvar(avaliacao("AV2", "D2"));

        AvaliacaoRepository recarregado = new AvaliacaoRepository(arquivo);
        Assertions.assertEquals(2, recarregado.listarTodas().size());
        Assertions.assertEquals("AV1", recarregado.buscarPorCodigo("av1").getCodigo());
        Assertions.assertEquals(1, recarregado.listarPorDiario("d2").size());
        Assertions.assertNull(recarregado.buscarPorCodigo(null));
        Assertions.assertNull(recarregado.buscarPorCodigo("inexistente"));
    }

    @Test
    void deveAtualizarEExcluir() {
        Avaliacao avaliacao = avaliacao("AV1", "D1");
        repository.salvar(avaliacao);
        avaliacao.setDescricao("Projeto final");
        repository.atualizar(avaliacao);
        Assertions.assertEquals("Projeto final", repository.buscarPorCodigo("AV1").getDescricao());

        repository.deletar("av1");
        Assertions.assertTrue(repository.listarTodas().isEmpty());
    }

    @Test
    void deveRejeitarDuplicidadeEOperacoesInexistentes() {
        repository.salvar(avaliacao("AV1", "D1"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> repository.salvar(avaliacao("av1", "D2")));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> repository.atualizar(avaliacao("OUTRA", "D1")));
        Assertions.assertThrows(IllegalArgumentException.class, () -> repository.deletar("OUTRA"));
    }

    @Test
    void deveFalharAoSalvarEmCaminhoInvalido() {
        AvaliacaoRepository invalido = new AvaliacaoRepository(tempDir.resolve("inexistente/avaliacoes.json").toString());
        Assertions.assertThrows(IllegalStateException.class, () -> invalido.salvar(avaliacao("AV1", "D1")));
    }

    private Avaliacao avaliacao(String codigo, String diario) {
        return new Avaliacao(codigo, diario, "ESW2", "2026.1", "T1", "Prova", "E1", 1.0, 10.0);
    }
}
