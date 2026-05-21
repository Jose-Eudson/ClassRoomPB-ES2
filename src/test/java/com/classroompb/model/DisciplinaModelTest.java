package com.classroompb.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Testes unitarios de Disciplina")
public class DisciplinaModelTest {
        @Test
        @DisplayName("Deve criar disciplina corretamente")
        void deveCriarDisciplinaCorretamente() {

                Disciplina disciplina =
                        new Disciplina(
                        "ES2",
                        "Engenharia de Software 2",
                        60,
                        4,
                        List.of(
                                "ES1",
                                "POO"
                        )
                );

                assertEquals(
                        "ES2",
                        disciplina.getCodigo()
                );

                assertEquals(
                        "Engenharia de Software 2",
                        disciplina.getNome()
                );

                assertEquals(
                        60,
                        disciplina.getCargaHoraria()
                );

                assertEquals(
                        4,
                        disciplina.getCreditos()
                );

                assertEquals(
                        2,
                        disciplina.getPreRequisitos().size()
                );
        }
}
