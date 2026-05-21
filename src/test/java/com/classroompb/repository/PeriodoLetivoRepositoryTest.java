package com.classroompb.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.classroompb.model.PeriodoLetivo;

@DisplayName("Testes unitarios de PeriodoLetivoRepository")
public class PeriodoLetivoRepositoryTest {

    private PeriodoLetivoRepository repository;

    private static final String ARQUIVO_TESTE =
            "periodos_teste.json";

    @BeforeEach
    void setUp() {

        File arquivo = new File(ARQUIVO_TESTE);

        if (arquivo.exists()) {
            arquivo.delete();
        }

        repository = new PeriodoLetivoRepository(ARQUIVO_TESTE);
    }

    @Test
    @DisplayName("Deve salvar periodo letivo")
    void deveSalvarPeriodoLetivo() {

        PeriodoLetivo periodo =
                new PeriodoLetivo(
                        "2026.2",
                        2026,
                        2,
                        LocalDate.of(2026, 8, 10),
                        LocalDate.of(2026, 12, 20),
                        true
                );

        repository.salvar(periodo);

        assertEquals(
                1,
                repository.listarTodos().size()
        );
    }

    @Test
    @DisplayName("Deve verificar existencia por codigo")
    void deveVerificarExistenciaPorCodigo() {

        repository.salvar(
                new PeriodoLetivo(
                        "2026.2",
                        2026,
                        2,
                        LocalDate.of(2026, 8, 10),
                        LocalDate.of(2026, 12, 20),
                        true
                )
        );

        assertTrue(repository.existePorCodigo("2026.2"));
    }
}