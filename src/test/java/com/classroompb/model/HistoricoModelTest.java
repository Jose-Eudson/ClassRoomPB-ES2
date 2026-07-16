package com.classroompb.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class HistoricoModelTest {

    @Test
    void deveRepresentarTodosOsDadosDoHistorico() {
        Historico historico = new Historico("A1", "2026.1", "ES2", "Engenharia de Software II", "T1", "P1",
                "Joao da Silva", 8.5, 90.0, "APROVADO");
        assertEquals("A1", historico.getMatriculaAluno());
        assertEquals("2026.1", historico.getCodigoPeriodo());
        assertEquals("ES2", historico.getCodigoDisciplina());
        assertEquals("Engenharia de Software II", historico.getNomeDisciplina());
        assertEquals("T1", historico.getCodigoTurma());
        assertEquals("P1", historico.getMatriculaProfessor());
        assertEquals("Joao da Silva", historico.getNomeProfessor());
        assertEquals(8.5, historico.getNotaFinal());
        assertEquals(90.0, historico.getFrequencia());
        assertEquals("APROVADO", historico.getSituacao());
        assertTrue(historico.isAprovado());
    }

    @Test
    void construtorVazioEGettersSettersDevemSerCompativeisComJackson() {
        Historico historico = new Historico();
        assertNotNull(historico);
        historico.setCodigoPeriodo("2026.2");
        historico.setNomeDisciplina("Testes");
        historico.setCodigoTurma("T2");
        historico.setMatriculaProfessor("P2");
        historico.setNomeProfessor("Maria");
        historico.setFrequencia(70.0);
        historico.setSituacao("REPROVADO POR FALTA");
        assertEquals("2026.2", historico.getCodigoPeriodo());
        assertEquals("Testes", historico.getNomeDisciplina());
        assertEquals("T2", historico.getCodigoTurma());
        assertEquals("P2", historico.getMatriculaProfessor());
        assertEquals("Maria", historico.getNomeProfessor());
        assertEquals(70.0, historico.getFrequencia());
        assertEquals("REPROVADO POR FALTA", historico.getSituacao());
        assertFalse(historico.isAprovado());
    }
}
