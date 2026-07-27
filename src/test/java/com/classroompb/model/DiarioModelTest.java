package com.classroompb.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class DiarioModelTest {

    @Test
    @DisplayName("Deve criar diário com construtor padrão")
    public void deveCriarDiarioComConstrutorPadrao() {
        Diario diario = new Diario();

        assertNull(diario.getCodigo());
        assertNull(diario.getCodigoTurma());
        assertNull(diario.getDescricao());
        assertNull(diario.getMatriculaProfessor());
        assertNull(diario.getHorario());
        assertNull(diario.getSala());
        assertEquals(0, diario.getCargaHoraria());
        assertNull(diario.getSituacao());
    }

    @Test
    public void deveCriarDiarioComConstrutorCompleto() {
        SituacaoDiario situacao = SituacaoDiario.values()[0];

        Diario diario = new Diario("D001", "T001", "Diário de Matemática", "P001", "08:00 - 10:00", "Sala 101", 80,
                situacao);

        assertEquals("D001", diario.getCodigo());
        assertEquals("T001", diario.getCodigoTurma());
        assertEquals("Diário de Matemática", diario.getDescricao());
        assertEquals("P001", diario.getMatriculaProfessor());
        assertEquals("08:00 - 10:00", diario.getHorario());
        assertEquals("Sala 101", diario.getSala());
        assertEquals(80, diario.getCargaHoraria());
        assertEquals(situacao, diario.getSituacao());
    }

    @Test
    public void deveDefinirValoresComOsSetters() {
        Diario diario = new Diario();
        SituacaoDiario situacao = SituacaoDiario.values()[0];

        diario.setCodigo("D002");
        diario.setCodigoTurma("T002");
        diario.setDescricao("Diário de Português");
        diario.setMatriculaProfessor("P002");
        diario.setHorario("10:00 - 12:00");
        diario.setSala("Sala 202");
        diario.setCargaHoraria(60);
        diario.setSituacao(situacao);

        assertEquals("D002", diario.getCodigo());
        assertEquals("T002", diario.getCodigoTurma());
        assertEquals("Diário de Português", diario.getDescricao());
        assertEquals("P002", diario.getMatriculaProfessor());
        assertEquals("10:00 - 12:00", diario.getHorario());
        assertEquals("Sala 202", diario.getSala());
        assertEquals(60, diario.getCargaHoraria());
        assertEquals(situacao, diario.getSituacao());
    }

    @Test
    public void deveAtualizarValoresDepoisDeCriado() {
        SituacaoDiario situacaoInicial = SituacaoDiario.values()[0];
        Diario diario = new Diario("D003", "T003", "História", "P003", "14:00", "Lab 1", 40, situacaoInicial);

        diario.setCodigo("D999");
        diario.setCodigoTurma("T999");
        diario.setDescricao("Atualizado");
        diario.setMatriculaProfessor("P999");
        diario.setHorario("16:00");
        diario.setSala("Sala 999");
        diario.setCargaHoraria(120);

        assertEquals("D999", diario.getCodigo());
        assertEquals("T999", diario.getCodigoTurma());
        assertEquals("Atualizado", diario.getDescricao());
        assertEquals("P999", diario.getMatriculaProfessor());
        assertEquals("16:00", diario.getHorario());
        assertEquals("Sala 999", diario.getSala());
        assertEquals(120, diario.getCargaHoraria());
        assertEquals(situacaoInicial, diario.getSituacao());
    }
}