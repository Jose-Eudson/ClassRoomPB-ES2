package com.classroompb.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.classroompb.model.Aula;
import com.classroompb.model.Diario;
import com.classroompb.model.SituacaoDiario;
import com.classroompb.repository.AulaRepository;
import com.classroompb.repository.DiarioRepository;

class AulaServiceTest {

    private AulaRepository aulaRepository;
    private DiarioRepository diarioRepository;
    private AulaService service;

    @BeforeEach
    void setup() {
        aulaRepository = mock(AulaRepository.class);
        diarioRepository = mock(DiarioRepository.class);

        service = new AulaService(aulaRepository, diarioRepository);
    }

    @Test
    @DisplayName("Deve cadastrar aula com sucesso")
    void deveCadastrarAula() throws Exception {

        Diario diario = new Diario("D01", "T01", "Diário ES2", "P001", "08:00", "Lab01", 60, SituacaoDiario.ATIVO);

        when(aulaRepository.buscarPorCodigo("A01")).thenReturn(null);

        when(diarioRepository.buscarPorCodigo("D01")).thenReturn(diario);

        when(aulaRepository.buscarPorDiario("D01")).thenReturn(List.of());

        service.cadastrarAula("A01", "D01", LocalDate.now(), "Introdução");

        verify(aulaRepository).salvar(any(Aula.class));
    }

    @Test
    @DisplayName("Deve numerar automaticamente as aulas")
    void deveNumerarAutomaticamente() throws Exception {

        Diario diario = new Diario("D01", "T01", "", "P001", "", "", 60, SituacaoDiario.ATIVO);

        Aula aulaExistente = new Aula("A01", "D01", LocalDate.now(), "Primeira", 1);

        when(aulaRepository.buscarPorCodigo("A02")).thenReturn(null);

        when(diarioRepository.buscarPorCodigo("D01")).thenReturn(diario);

        when(aulaRepository.buscarPorDiario("D01")).thenReturn(List.of(aulaExistente));

        service.cadastrarAula("A02", "D01", LocalDate.now(), "Segunda");

        verify(aulaRepository).salvar(argThat(a -> a.getNumero() == 2));
    }

    @Test
    @DisplayName("Não deve cadastrar aula para diário inexistente")
    void naoDeveCadastrarSemDiario() {

        when(aulaRepository.buscarPorCodigo("A01"))
                .thenReturn(null);

        when(diarioRepository.buscarPorCodigo("D01"))
                .thenReturn(null);

        Exception ex = assertThrows(Exception.class,
                () -> service.cadastrarAula(
                        "A01",
                        "D01",
                        LocalDate.now(),
                        "Conteúdo"));

        assertEquals(
                "Erro: diário inexistente.",
                ex.getMessage());
    }

    @Test
    @DisplayName("Deve listar aulas do diário")
    void deveListarPorDiario() {

        when(aulaRepository.buscarPorDiario("D01"))
                .thenReturn(List.of(
                        new Aula(
                                "A01",
                                "D01",
                                LocalDate.now(),
                                "Conteúdo",
                                1)));

        List<Aula> aulas = service.listarPorDiario("D01");

        assertEquals(1, aulas.size());

        verify(aulaRepository).buscarPorDiario("D01");
    }

    @Test
    @DisplayName("Deve informar que diário possui aulas")
    void deveInformarQueDiarioPossuiAulas() {

        when(aulaRepository.buscarPorDiario("D01"))
                .thenReturn(List.of(mock(Aula.class)));

        assertTrue(service.diarioPossuiAulas("D01"));
    }

    @Test
    @DisplayName("Deve retornar quantidade de aulas")
    void deveRetornarQuantidadeDeAulas() {

        when(aulaRepository.buscarPorDiario("D01"))
                .thenReturn(List.of(
                        mock(Aula.class),
                        mock(Aula.class),
                        mock(Aula.class)));

        assertEquals(3, service.quantidadeAulas("D01"));
    }

}