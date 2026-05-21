package com.classroompb.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import com.classroompb.repository.DisciplinaRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitarios de DisciplinaService")
public class DisciplinaServiceTest {

    @Mock
    private DisciplinaRepository repository;

    private DisciplinaService service;

    @BeforeEach
    void setUp() {

        service =
                new DisciplinaService(
                        repository
                );
    }

    @Test
    @DisplayName("Deve cadastrar disciplina com sucesso")
    void deveCadastrarDisciplinaComSucesso()
            throws Exception {

        when(repository.existePorCodigo("ES2"))
                .thenReturn(false);

        service.cadastrarDisciplina(
                "ES2",
                "Engenharia de Software 2",
                60,
                4,
                List.of(
                        "ES1",
                        "POO"
                )
        );

        verify(repository)
                .salvar(any());
    }

    @Test
    @DisplayName("Nao deve cadastrar com codigo duplicado")
    void naoDeveCadastrarComCodigoDuplicado() {

        when(repository.existePorCodigo("ES2"))
                .thenReturn(true);

        Exception ex =
                assertThrows(
                        Exception.class,
                        () -> service.cadastrarDisciplina(
                                "ES2",
                                "Outra",
                                60,
                                4,
                                List.of()
                        )
                );

        assertEquals(
                "Erro: Ja existe uma disciplina com este codigo.",
                ex.getMessage()
        );

        verify(repository, never())
                .salvar(any());
    }

    @Test
    @DisplayName("Nao deve cadastrar com creditos invalidos")
    void naoDeveCadastrarComCreditosInvalidos() {

        Exception ex =
                assertThrows(
                        Exception.class,
                        () -> service.cadastrarDisciplina(
                                "ES2",
                                "Disciplina",
                                60,
                                0,
                                List.of()
                        )
                );

        assertEquals(
                "Erro: Creditos devem ser maiores que zero.",
                ex.getMessage()
        );

        verify(repository, never())
                .salvar(any());
    }
}
