package com.classroompb.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Testes unitários de Nota")
public class NotaModelTest {

    @Test
    @DisplayName("Deve criar nota pelo construtor")
    void deveCriarNotaPeloConstrutor() {

        Nota nota = new Nota("A0001", "ES2", "2026.1", "T01");

        assertEquals("A0001", nota.getMatriculaAluno());
        assertEquals("ES2", nota.getCodigoDisciplina());
        assertEquals("2026.1", nota.getCodigoPeriodo());
        assertEquals("T01", nota.getCodigoTurma());
    }

    @Test
    @DisplayName("Deve gerar chave única corretamente")
    void deveGerarChaveUnica() {

        Nota nota = new Nota("A0001", "ES2", "2026.1", "T01");

        assertEquals("A0001_ES2_2026.1_T01", nota.getChaveUnica());
    }
}