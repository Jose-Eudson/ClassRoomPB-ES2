package com.classroompb.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

    @Test
    @DisplayName("Deve registrar historico quando disciplina ainda nao existir")
    void deveRegistrarHistoricoQuandoDisciplinaAindaNaoExistir() {

        when(repository.buscarPorAluno("2023001")).thenReturn(List.of());

        service.registrarHistorico("2023001", "ES1", 8.5, true);

        ArgumentCaptor<Historico> captor = ArgumentCaptor.forClass(Historico.class);
        verify(repository).salvar(captor.capture());

        Historico salvo = captor.getValue();
        assertEquals("2023001", salvo.getMatriculaAluno());
        assertEquals("ES1", salvo.getCodigoDisciplina());
        assertEquals(8.5, salvo.getNotaFinal());
        assertTrue(salvo.isAprovado());
    }

    @Test
    @DisplayName("Deve atualizar historico existente da disciplina")
    void deveAtualizarHistoricoExistenteDaDisciplina() {

        Historico historico = new Historico("2023001", "ES1", 6.0, false);

        when(repository.buscarPorAluno("2023001")).thenReturn(List.of(historico));

        service.registrarHistorico("2023001", "ES1", 7.5, true);

        ArgumentCaptor<Historico> captor = ArgumentCaptor.forClass(Historico.class);
        verify(repository).atualizar(captor.capture());

        Historico atualizado = captor.getValue();
        assertEquals(7.5, atualizado.getNotaFinal());
        assertTrue(atualizado.isAprovado());
        assertEquals("ES1", atualizado.getCodigoDisciplina());
    }
}
