package com.classroompb.repository;

import java.io.File;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.classroompb.model.Disciplina;

@DisplayName("Testes unitarios de DisciplinaRepository")
public class DisciplinaRepositoryTest {

    private DisciplinaRepository repository;

    private static final String ARQUIVO_TESTE =
            "disciplinas_teste.json";

    @BeforeEach
    void setUp() {

        File arquivo =
                new File(ARQUIVO_TESTE);

        if (arquivo.exists()) {
            arquivo.delete();
        }

        repository =
                new DisciplinaRepository(
                        ARQUIVO_TESTE
                );
    }

    @Test
    @DisplayName("Deve salvar disciplina")
    void deveSalvarDisciplina() {

        Disciplina disciplina =
                new Disciplina(
                        "BD",
                        "Banco de Dados",
                        60,
                        4,
                        Arrays.asList("POO")
                );

        repository.salvar(disciplina);

        assertEquals(
                1,
                repository.listarTodos().size()
        );
    }

    @Test
    @DisplayName("Deve verificar existencia por codigo")
    void deveVerificarExistenciaPorCodigo() {

        repository.salvar(
                new Disciplina(
                        "LP2",
                        "Linguagem de Programacao 2",
                        80,
                        5,
                        Arrays.asList("LP1")
                )
        );

        assertTrue(
                repository.existePorCodigo("LP2")
        );
    }
}