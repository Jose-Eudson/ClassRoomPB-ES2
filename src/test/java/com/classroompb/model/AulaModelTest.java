package com.classroompb.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class AulaModelTest {

    @Test
    @DisplayName("Deve criar aula com construtor completo")
    public void deveCriarAulaComConstrutorCompleto() {
        LocalDate data = LocalDate.of(2026, 8, 1);
        Aula aula = new Aula("A001", "D001", data, "Conteúdo introdutório", 1);

        assertEquals("A001", aula.getCodigo());
        assertEquals("D001", aula.getCodigoDiario());
        assertEquals(data, aula.getData());
        assertEquals("Conteúdo introdutório", aula.getConteudo());
        assertEquals(1, aula.getNumero());
    }

    @Test
    @DisplayName("Deve atualizar valores da aula usando setters")
    public void deveAtualizarValoresDaAulaUsandoSetters() {
        LocalDate dataOriginal = LocalDate.of(2026, 8, 1);
        Aula aula = new Aula("A001", "D001", dataOriginal, "Conteúdo introdutório", 1);

        LocalDate novaData = LocalDate.of(2026, 8, 2);
        aula.setCodigo("A002");
        aula.setCodigoDiario("D002");
        aula.setData(novaData);
        aula.setConteudo("Conteúdo avançado");
        aula.setNumero(2);

        assertEquals("A002", aula.getCodigo());
        assertEquals("D002", aula.getCodigoDiario());
        assertEquals(novaData, aula.getData());
        assertEquals("Conteúdo avançado", aula.getConteudo());
        assertEquals(2, aula.getNumero());
    }
}
