package com.classroompb.service;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.classroompb.model.Avaliacao;
import com.classroompb.model.Coordenador;
import com.classroompb.model.Diario;
import com.classroompb.model.Professor;
import com.classroompb.model.SituacaoDiario;
import com.classroompb.model.SituacaoTurma;
import com.classroompb.model.Turma;
import com.classroompb.repository.AvaliacaoRepository;
import com.classroompb.repository.DiarioRepository;
import com.classroompb.repository.TurmaRepository;

class AvaliacaoServiceTest {
    private AvaliacaoRepository avaliacaoRepository;
    private DiarioRepository diarioRepository;
    private TurmaRepository turmaRepository;
    private AvaliacaoService service;
    private Professor professor;
    private Diario diario;
    private Turma turma;

    @BeforeEach
    void preparar() {
        avaliacaoRepository = Mockito.mock(AvaliacaoRepository.class);
        diarioRepository = Mockito.mock(DiarioRepository.class);
        turmaRepository = Mockito.mock(TurmaRepository.class);
        service = new AvaliacaoService(avaliacaoRepository, diarioRepository, turmaRepository);
        professor = new Professor("P1", "Prof", "prof@teste.com", "senha");
        diario = new Diario("D1", "ESW2", "2026.1", "T1", "Teoria", "P1", "SEG 8-10", "S1", 20,
                SituacaoDiario.ATIVO);
        turma = new Turma("T1", "ESW2", "2026.1", 20, null, null, null);
        Mockito.when(diarioRepository.buscarPorCodigo("D1")).thenReturn(diario);
        Mockito.when(turmaRepository.buscarPorChaveUnica("ESW2", "2026.1", "T1")).thenReturn(turma);
    }

    @Test
    void deveCadastrarAvaliacaoCompleta() throws Exception {
        Avaliacao criada = service.cadastrar(professor, " AV1 ", "D1", " Prova ", " E1 ", 2.0, 20.0);

        Assertions.assertEquals("AV1", criada.getCodigo());
        Assertions.assertEquals("ESW2", criada.getCodigoDisciplina());
        Mockito.verify(avaliacaoRepository).salvar(criada);
    }

    @Test
    void deveEditarListarBuscarERemover() throws Exception {
        Avaliacao avaliacao = avaliacao();
        Mockito.when(avaliacaoRepository.buscarPorCodigo("AV1")).thenReturn(avaliacao);
        Mockito.when(avaliacaoRepository.listarPorDiario("D1")).thenReturn(List.of(avaliacao));
        avaliacao.setDescricao("Projeto final");

        service.editar(professor, avaliacao);
        Assertions.assertSame(avaliacao, service.buscarPorCodigo("AV1"));
        Assertions.assertEquals(1, service.listarPorDiario("D1").size());
        service.remover(professor, "AV1");

        Mockito.verify(avaliacaoRepository).atualizar(avaliacao);
        Mockito.verify(avaliacaoRepository).deletar("AV1");
    }

    @Test
    void deveRejeitarCodigoDuplicadoECamposInvalidos() {
        Mockito.when(avaliacaoRepository.buscarPorCodigo("AV1")).thenReturn(avaliacao());
        Assertions.assertThrows(Exception.class,
                () -> service.cadastrar(professor, "AV1", "D1", "Prova", "E1", 1.0, 10.0));
        Assertions.assertThrows(Exception.class,
                () -> service.cadastrar(professor, "AV2", "D1", " ", "E1", 1.0, 10.0));
        Assertions.assertThrows(Exception.class,
                () -> service.cadastrar(professor, "AV2", "D1", "Prova", " ", 1.0, 10.0));
        Assertions.assertThrows(Exception.class,
                () -> service.cadastrar(professor, "AV2", "D1", "Prova", "E1", 0.0, 10.0));
        Assertions.assertThrows(Exception.class,
                () -> service.cadastrar(professor, "AV2", "D1", "Prova", "E1", 1.0, Double.NaN));
    }

    @Test
    void deveRejeitarUsuarioDiarioEProfessorInvalidos() {
        Assertions.assertThrows(Exception.class,
                () -> service.cadastrar(null, "AV1", "D1", "Prova", "E1", 1.0, 10.0));
        Coordenador coordenador = new Coordenador("C1", "Coord", "c@teste.com", "senha");
        Assertions.assertThrows(Exception.class,
                () -> service.cadastrar(coordenador, "AV1", "D1", "Prova", "E1", 1.0, 10.0));
        Mockito.when(diarioRepository.buscarPorCodigo("OUTRO")).thenReturn(null);
        Assertions.assertThrows(Exception.class,
                () -> service.cadastrar(professor, "AV1", "OUTRO", "Prova", "E1", 1.0, 10.0));
        diario.setMatriculaProfessor("P2");
        Assertions.assertThrows(Exception.class,
                () -> service.cadastrar(professor, "AV1", "D1", "Prova", "E1", 1.0, 10.0));
    }

    @Test
    void deveBloquearDiarioOuTurmaEncerrados() {
        diario.setSituacao(SituacaoDiario.ENCERRADO);
        Assertions.assertThrows(Exception.class,
                () -> service.cadastrar(professor, "AV1", "D1", "Prova", "E1", 1.0, 10.0));
        diario.setSituacao(SituacaoDiario.ATIVO);
        turma.setSituacao(SituacaoTurma.ENCERRADA);
        Assertions.assertThrows(Exception.class,
                () -> service.cadastrar(professor, "AV1", "D1", "Prova", "E1", 1.0, 10.0));
    }

    @Test
    void deveRejeitarEdicaoInexistenteOuMovida() {
        Assertions.assertThrows(Exception.class, () -> service.editar(professor, null));
        Avaliacao avaliacao = avaliacao();
        Assertions.assertThrows(Exception.class, () -> service.editar(professor, avaliacao));
        Mockito.when(avaliacaoRepository.buscarPorCodigo("AV1")).thenReturn(avaliacao);
        Avaliacao movida = avaliacao();
        movida.setCodigoDiario("D2");
        Assertions.assertThrows(Exception.class, () -> service.editar(professor, movida));
    }

    @Test
    void deveRejeitarBuscaInexistente() {
        Assertions.assertThrows(Exception.class, () -> service.buscarPorCodigo(" "));
        Assertions.assertThrows(Exception.class, () -> service.buscarPorCodigo("AVX"));
    }

    @Test
    void construtorDeCopiaDeveCompartilharRepositorios() throws Exception {
        AvaliacaoService copia = new AvaliacaoService(service);
        copia.cadastrar(professor, "AV1", "D1", "Prova", "E1", 1.0, 10.0);
        Mockito.verify(avaliacaoRepository).salvar(Mockito.any(Avaliacao.class));
    }

    private Avaliacao avaliacao() {
        return new Avaliacao("AV1", "D1", "ESW2", "2026.1", "T1", "Prova", "E1", 1.0, 10.0);
    }
}
