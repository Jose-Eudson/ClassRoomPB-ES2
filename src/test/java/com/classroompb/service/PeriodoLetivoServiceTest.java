// ==========================
// PeriodoLetivoServiceTest.java
// ==========================

package com.classroompb.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import com.classroompb.model.Coordenador;
import com.classroompb.model.PeriodoLetivo;
import com.classroompb.repository.PeriodoLetivoRepository;

@ExtendWith(MockitoExtension.class)
public class PeriodoLetivoServiceTest {

    @Mock
    private PeriodoLetivoRepository repository;

    private PeriodoLetivoService service;

    private Coordenador coordenador;

    @BeforeEach
    void setUp() {

        service =
                new PeriodoLetivoService(
                        repository
                );

        coordenador =
                new Coordenador(
                        "2023001",
                        "Rui",
                        "rui@email.com",
                        "123"
                );
    }

    @Test
    void deveCadastrarPeriodo()
            throws Exception {

        when(repository.existePorCodigo("2026.2"))
                .thenReturn(false);

        service.cadastrarPeriodo(
                "2026.2",
                2026,
                2,
                LocalDate.of(2026, 8, 10),
                LocalDate.of(2026, 12, 20),
                true
        );

        verify(repository)
                .salvar(any());
    }

    @Test
    void deveAtivarPeriodo()
            throws Exception {

        PeriodoLetivo periodo =
                new PeriodoLetivo();

        when(repository.buscarPorCodigo("2026.2"))
                .thenReturn(periodo);

        service.ativarPeriodo(
                coordenador,
                "2026.2"
        );

        verify(repository)
                .atualizarDados();
    }

    @Test
    void deveEncerrarPeriodo()
            throws Exception {

        PeriodoLetivo periodo =
                new PeriodoLetivo();

        when(repository.buscarPorCodigo("2026.2"))
                .thenReturn(periodo);

        service.encerrarPeriodo(
                coordenador,
                "2026.2"
        );

        verify(repository)
                .atualizarDados();
    }

    @Test
    void naoDeveCadastrarPeriodoDuplicado() {

        when(repository.existePorCodigo("2026.2"))
                .thenReturn(true);

        assertThrows(
                Exception.class,
                () -> service.cadastrarPeriodo(

                        "2026.2",
                        2026,
                        2,
                        LocalDate.now(),
                        LocalDate.now().plusMonths(4),
                        true
                )
        );
    }
}