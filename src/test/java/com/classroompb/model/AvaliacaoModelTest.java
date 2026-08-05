package com.classroompb.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AvaliacaoModelTest {

    @Test
    void deveCriarAvaliacaoCompleta() {
        Avaliacao avaliacao = new Avaliacao("AV1", "D1", "ESW2", "2026.1", "T1", "Prova", "E1", 2.0,
                20.0);

        Assertions.assertAll(() -> Assertions.assertEquals("AV1", avaliacao.getCodigo()),
                () -> Assertions.assertEquals("D1", avaliacao.getCodigoDiario()),
                () -> Assertions.assertEquals("ESW2", avaliacao.getCodigoDisciplina()),
                () -> Assertions.assertEquals("2026.1", avaliacao.getCodigoPeriodo()),
                () -> Assertions.assertEquals("T1", avaliacao.getCodigoTurma()),
                () -> Assertions.assertEquals("Prova", avaliacao.getDescricao()),
                () -> Assertions.assertEquals("E1", avaliacao.getEtapa()),
                () -> Assertions.assertEquals(2.0, avaliacao.getPeso()),
                () -> Assertions.assertEquals(20.0, avaliacao.getNotaMaxima()));
    }

    @Test
    void devePermitirDesserializacaoESetters() {
        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setCodigo("AV2");
        avaliacao.setCodigoDiario("D2");
        avaliacao.setCodigoDisciplina("BD");
        avaliacao.setCodigoPeriodo("2026.2");
        avaliacao.setCodigoTurma("T2");
        avaliacao.setDescricao("Projeto");
        avaliacao.setEtapa("E2");
        avaliacao.setPeso(3.0);
        avaliacao.setNotaMaxima(10.0);

        Assertions.assertAll(() -> Assertions.assertEquals("AV2", avaliacao.getCodigo()),
                () -> Assertions.assertEquals("D2", avaliacao.getCodigoDiario()),
                () -> Assertions.assertEquals("BD", avaliacao.getCodigoDisciplina()),
                () -> Assertions.assertEquals("2026.2", avaliacao.getCodigoPeriodo()),
                () -> Assertions.assertEquals("T2", avaliacao.getCodigoTurma()),
                () -> Assertions.assertEquals("Projeto", avaliacao.getDescricao()),
                () -> Assertions.assertEquals("E2", avaliacao.getEtapa()),
                () -> Assertions.assertEquals(3.0, avaliacao.getPeso()),
                () -> Assertions.assertEquals(10.0, avaliacao.getNotaMaxima()));
    }
}
