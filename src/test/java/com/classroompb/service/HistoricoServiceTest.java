package com.classroompb.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.classroompb.model.Historico;
import com.classroompb.repository.HistoricoRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitarios de HistoricoService")
class HistoricoServiceTest {

    @Mock
    private HistoricoRepository repository;

    private HistoricoService service;

    @BeforeEach
    void setUp() {
        service = new HistoricoService(repository);
    }

    @Test
    @DisplayName("Deve retornar verdadeiro quando aluno foi aprovado")
    void deveRetornarVerdadeiroQuandoAprovado() {

        Historico historico = new Historico("2023001", "ES1", 8.5, true);

        when(repository.buscarPorAluno("2023001")).thenReturn(List.of(historico));

        boolean resultado = service.alunoFoiAprovado("2023001", "ES1");

        assertTrue(resultado);
    }

    @Test
    @DisplayName("Deve retornar falso quando aluno foi reprovado")
    void deveRetornarFalsoQuandoReprovado() {

        Historico historico = new Historico("2023001", "ES1", 3.0, false);

        when(repository.buscarPorAluno("2023001")).thenReturn(List.of(historico));

        boolean resultado = service.alunoFoiAprovado("2023001", "ES1");

        assertFalse(resultado);
    }

    @Test
    @DisplayName("Deve retornar falso quando disciplina nao existir no historico")
    void deveRetornarFalsoQuandoDisciplinaNaoExistir() {

        when(repository.buscarPorAluno("2023001"))
                .thenReturn(List.of());

        boolean resultado =
                service.alunoFoiAprovado(
                        "2023001",
                        "ES1"
                );

        assertFalse(resultado);
    }
}
