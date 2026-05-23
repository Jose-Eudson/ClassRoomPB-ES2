package com.classroompb.model;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Testes unitarios de PeriodoLetivo")
public class PeriodoLetivoModelTest {

    @Test
    @DisplayName("Deve criar periodo letivo corretamente")
    void deveCriarPeriodoLetivoCorretamente() {

        PeriodoLetivo periodo =
            new PeriodoLetivo(
                        "2026.2",
                        2026,
                        2,
                        LocalDate.of(2026, 8, 10),
                        LocalDate.of(2026, 12, 20),
                        true
                );

        assertEquals("2026.2", periodo.getCodigo());
        assertEquals(2026, periodo.getAno());
        assertEquals(2, periodo.getSemestre());
        assertEquals(LocalDate.of(2026, 8, 10), periodo.getDataInicio());
        assertEquals(LocalDate.of(2026, 12, 20), periodo.getDataFim());
        assertTrue(periodo.isAtivo());
    }

    @Test
    @DisplayName("Deve alterar atributos corretamente")
    void deveAlterarAtributos() {

        PeriodoLetivo periodo = new PeriodoLetivo();

        periodo.setCodigo("2027.1");
        periodo.setAno(2027);
        periodo.setSemestre(1);
        periodo.setDataInicio(LocalDate.of(2027, 2, 1));
        periodo.setDataFim(LocalDate.of(2027, 6, 30));
        periodo.setAtivo(false);

        assertEquals("2027.1", periodo.getCodigo());
        assertEquals(2027, periodo.getAno());
        assertEquals(1, periodo.getSemestre());
        assertEquals(LocalDate.of(2027, 2, 1), periodo.getDataInicio());
        assertEquals(LocalDate.of(2027, 6, 30), periodo.getDataFim());
        assertFalse(periodo.isAtivo());
    }

    @Test
    @DisplayName("Deve retornar toString correto para periodo ativo")
    void deveRetornarToStringParaPeriodoAtivo() {

        PeriodoLetivo periodo = new PeriodoLetivo(
                "2026.1",
                2026,
                1,
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 6, 30),
                true
        );

        // Corrigindo o formato esperado de acordo com a implementação: [PERIODO LETIVO] codigo - ano.semestre | inicio ate fim | status
        String esperado = "[PERIODO LETIVO] 2026.1 - 2026.1 | 2026-02-01 ate 2026-06-30 | ATIVO";

        assertEquals(esperado, periodo.toString());
    }

    @Test
    @DisplayName("Deve retornar toString correto para periodo inativo")
    void deveRetornarToStringParaPeriodoInativo() {

        PeriodoLetivo periodo = new PeriodoLetivo(
                "2025.2",
                2025,
                2,
                LocalDate.of(2025, 8, 10),
                LocalDate.of(2025, 12, 20),
                false
        );

        String esperado = "[PERIODO LETIVO] 2025.2 - 2025.2 | 2025-08-10 ate 2025-12-20 | INATIVO";

        assertEquals(esperado, periodo.toString());
    }
}
