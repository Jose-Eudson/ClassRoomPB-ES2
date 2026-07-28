package com.classroompb.service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.classroompb.model.Diario;
import com.classroompb.model.SituacaoDiario;
import com.classroompb.model.TipoUsuario;
import com.classroompb.model.Turma;
import com.classroompb.model.Usuario;
import com.classroompb.repository.DiarioRepository;
import com.classroompb.repository.TurmaRepository;
import com.classroompb.repository.UsuarioRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DiarioServiceTest {

    private DiarioService service;
    private DiarioRepository diarioRepository;
    private TurmaRepository turmaRepository;
    private UsuarioRepository usuarioRepository;

    @BeforeEach
    void setUp() {
        diarioRepository = mock(DiarioRepository.class);
        turmaRepository = mock(TurmaRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        service = new DiarioService(diarioRepository, turmaRepository, usuarioRepository);
    }

    @Test
    @DisplayName("Deve cadastrar diário com dados válidos")
    void deveCadastrarDiarioComDadosValidos() throws Exception {
        when(diarioRepository.buscarPorCodigo("D001")).thenReturn(null);
        when(turmaRepository.buscarPorChaveUnica("DISC001", "PERIODO1", "T001"))
                .thenReturn(mock(Turma.class));

        Usuario professor = mock(Usuario.class);
        when(professor.getTipo()).thenReturn(TipoUsuario.PROFESSOR);
        when(usuarioRepository.buscarPorMatricula("P001"))
                .thenReturn(Optional.of(professor));

        service.cadastrarDiario(
                "D001",
                "T001",
                "DISC001",
                "PERIODO1",
                "Descrição do diário",
                "P001",
                "08:00",
                "Sala 101",
                80
        );

        ArgumentCaptor<Diario> captor = ArgumentCaptor.forClass(Diario.class);
        verify(diarioRepository).salvar(captor.capture());

        Diario diarioSalvo = captor.getValue();
        assertEquals("D001", diarioSalvo.getCodigo());
        assertEquals("T001", diarioSalvo.getCodigoTurma());
        assertEquals("Descrição do diário", diarioSalvo.getDescricao());
        assertEquals("P001", diarioSalvo.getMatriculaProfessor());
        assertEquals("08:00", diarioSalvo.getHorario());
        assertEquals("Sala 101", diarioSalvo.getSala());
        assertEquals(80, diarioSalvo.getCargaHoraria());
        assertEquals(SituacaoDiario.ATIVO, diarioSalvo.getSituacao());
    }

    @Test
    @DisplayName("Deve lançar exceção quando o código for nulo")
    void deveLancarExcecaoQuandoCodigoEhNulo() {
        Exception exception = assertThrows(Exception.class, () -> service.cadastrarDiario(null, "T001", "DISC001",
                "PERIODO1", "Descrição", "P001", "08:00", "Sala 101", 80));

        assertNotNull(exception.getMessage());
        verify(diarioRepository, never()).salvar(any(Diario.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando o código estiver em branco")
    void deveLancarExcecaoQuandoCodigoEhEmBranco() {
        Exception exception = assertThrows(Exception.class, () -> service.cadastrarDiario("   ", "T001", "DISC001",
                "PERIODO1", "Descrição", "P001", "08:00", "Sala 101", 80));

        assertNotNull(exception.getMessage());
        verify(diarioRepository, never()).salvar(any(Diario.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando o código já existir")
    void deveLancarExcecaoQuandoCodigoJaExiste() {
        when(diarioRepository.buscarPorCodigo("D001")).thenReturn(mock(Diario.class));

        Exception exception = assertThrows(
                Exception.class,
                () -> service.cadastrarDiario(
                        "D001",
                        "T001",
                        "DISC001",
                        "PERIODO1",
                        "Descrição",
                        "P001",
                        "08:00",
                        "Sala 101",
                        80
                )
        );

        assertTrue(exception.getMessage().contains("código"));
        verify(diarioRepository, never()).salvar(any(Diario.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando a turma não existir")
    void deveLancarExcecaoQuandoTurmaNaoExiste() {
        when(diarioRepository.buscarPorCodigo("D001")).thenReturn(null);
        when(turmaRepository.buscarPorChaveUnica("DISC001", "PERIODO1", "T001"))
                .thenReturn(null);

        Exception exception = assertThrows(
                Exception.class,
                () -> service.cadastrarDiario(
                        "D001",
                        "T001",
                        "DISC001",
                        "PERIODO1",
                        "Descrição",
                        "P001",
                        "08:00",
                        "Sala 101",
                        80
                )
        );

        assertTrue(exception.getMessage().contains("turma"));
        verify(diarioRepository, never()).salvar(any(Diario.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando o professor não existir")
    void deveLancarExcecaoQuandoProfessorNaoExiste() {
        when(diarioRepository.buscarPorCodigo("D001")).thenReturn(null);
        when(turmaRepository.buscarPorChaveUnica("DISC001", "PERIODO1", "T001"))
                .thenReturn(mock(Turma.class));
        when(usuarioRepository.buscarPorMatricula("P001")).thenReturn(Optional.empty());

        Exception exception = assertThrows(
                Exception.class,
                () -> service.cadastrarDiario(
                        "D001",
                        "T001",
                        "DISC001",
                        "PERIODO1",
                        "Descrição",
                        "P001",
                        "08:00",
                        "Sala 101",
                        80
                )
        );

        assertTrue(exception.getMessage().contains("Professor"));
        verify(diarioRepository, never()).salvar(any(Diario.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando o usuário não for professor")
    void deveLancarExcecaoQuandoUsuarioNaoEhProfessor() {
        when(diarioRepository.buscarPorCodigo("D001")).thenReturn(null);
        when(turmaRepository.buscarPorChaveUnica("DISC001", "PERIODO1", "T001"))
                .thenReturn(mock(Turma.class));

        Usuario usuario = mock(Usuario.class);
        when(usuario.getTipo()).thenReturn(TipoUsuario.ALUNO);
        when(usuarioRepository.buscarPorMatricula("P001"))
                .thenReturn(Optional.of(usuario));

        Exception exception = assertThrows(
                Exception.class,
                () -> service.cadastrarDiario(
                        "D001",
                        "T001",
                        "DISC001",
                        "PERIODO1",
                        "Descrição",
                        "P001",
                        "08:00",
                        "Sala 101",
                        80
                )
        );

        assertTrue(exception.getMessage().contains("professor"));
        verify(diarioRepository, never()).salvar(any(Diario.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando a carga horária for inválida")
    void deveLancarExcecaoQuandoCargaHorariaEhInvalida() {
        when(diarioRepository.buscarPorCodigo("D001")).thenReturn(null);
        when(turmaRepository.buscarPorChaveUnica("DISC001", "PERIODO1", "T001"))
                .thenReturn(mock(Turma.class));

        Usuario professor = mock(Usuario.class);
        when(professor.getTipo()).thenReturn(TipoUsuario.PROFESSOR);
        when(usuarioRepository.buscarPorMatricula("P001"))
                .thenReturn(Optional.of(professor));

        Exception exception = assertThrows(
                Exception.class,
                () -> service.cadastrarDiario(
                        "D001",
                        "T001",
                        "DISC001",
                        "PERIODO1",
                        "Descrição",
                        "P001",
                        "08:00",
                        "Sala 101",
                        0
                )
        );

        assertTrue(exception.getMessage().contains("carga"));
        verify(diarioRepository, never()).salvar(any(Diario.class));
    }

    @Test
    @DisplayName("Deve editar um diário quando ele existir")
    void deveEditarDiarioQuandoExistir() throws Exception {
        Diario diario = criarDiario("D001");
        when(diarioRepository.buscarPorCodigo("D001")).thenReturn(diario);

        service.editarDiario(diario);

        verify(diarioRepository).atualizar(diario);
    }

    @Test
    @DisplayName("Deve lançar exceção ao editar um diário inexistente")
    void deveLancarExcecaoAoEditarDiarioInexistente() {
        Diario diario = criarDiario("D999");
        when(diarioRepository.buscarPorCodigo("D999")).thenReturn(null);

        Exception exception = assertThrows(Exception.class, () -> service.editarDiario(diario));

        assertNotNull(exception.getMessage());
        verify(diarioRepository, never()).atualizar(any(Diario.class));
    }

    @Test
    @DisplayName("Deve buscar um diário por código")
    void deveBuscarDiarioPorCodigo() {
        Diario diario = criarDiario("D001");
        when(diarioRepository.buscarPorCodigo("D001")).thenReturn(diario);

        Diario resultado = service.buscarPorCodigo("D001");

        assertSame(diario, resultado);
    }

    @Test
    @DisplayName("Deve listar todos os diários")
    void deveListarTodosOsDiarios() {
        List<Diario> diarios = Arrays.asList(criarDiario("D001"), criarDiario("D002"));
        when(diarioRepository.listarTodos()).thenReturn(diarios);

        List<Diario> resultado = service.listarDiarios();

        assertEquals(diarios, resultado);
    }

    @Test
    @DisplayName("Deve listar diários por turma")
    void deveListarDiariosPorTurma() {
        List<Diario> diarios = Arrays.asList(criarDiario("D001"), criarDiario("D002"));
        when(diarioRepository.buscarPorTurma("T001")).thenReturn(diarios);

        List<Diario> resultado = service.buscarPorTurma("T001");

        assertEquals(diarios, resultado);
    }

    @Test
    @DisplayName("Deve listar diários por professor")
    void deveListarDiariosPorProfessor() {
        List<Diario> diarios = Arrays.asList(criarDiario("D001"), criarDiario("D002"));
        when(diarioRepository.buscarPorProfessor("P001")).thenReturn(diarios);

        List<Diario> resultado = service.listarPorProfessor("P001");

        assertEquals(diarios, resultado);
    }

    @Test
    @DisplayName("Deve permitir cadastrar vários diários para a mesma turma")
    void devePermitirVariosDiariosParaMesmaTurma() throws Exception {

        Turma turma = mock(Turma.class);
        Usuario professor = mock(Usuario.class);

        when(turmaRepository.buscarPorChaveUnica("RDSC", "2026.1", "T01")).thenReturn(turma);

        when(usuarioRepository.buscarPorMatricula("P0001")).thenReturn(Optional.of(professor));

        when(professor.getTipo()).thenReturn(TipoUsuario.PROFESSOR);

        when(diarioRepository.buscarPorCodigo("D01")).thenReturn(null);

        when(diarioRepository.buscarPorCodigo("D02")).thenReturn(null);

        service.cadastrarDiario("D01", "T01", "RDSC", "2026.1", "Diário A", "P0001", "Seg 08h", "Lab 01", 60);

        service.cadastrarDiario("D02", "T01", "RDSC", "2026.1", "Diário B", "P0001", "Qua 10h", "Lab 02", 60);

        verify(diarioRepository, times(2)).salvar(any(Diario.class));
    }

    @Test
    @DisplayName("Deve listar todos os diários de uma turma")
    void deveListarDiariosDaTurma() {

        Diario d1 = mock(Diario.class);
        Diario d2 = mock(Diario.class);

        when(diarioRepository.buscarPorTurma("T01")).thenReturn(List.of(d1, d2));

        List<Diario> diarios = service.buscarPorTurma("T01");

        assertEquals(2, diarios.size());
    }

    @Test
    @DisplayName("Professor pode ser responsável por vários diários")
    void deveListarDiariosDoProfessor() {

        Diario d1 = mock(Diario.class);
        Diario d2 = mock(Diario.class);
        Diario d3 = mock(Diario.class);

        when(diarioRepository.buscarPorProfessor("P0001")).thenReturn(List.of(d1, d2, d3));

        List<Diario> diarios = service.listarPorProfessor("P0001");

        assertEquals(3, diarios.size());
    }

    @Test
    @DisplayName("Não deve permitir cadastrar diário sem professor responsável")
    void naoDevePermitirDiarioSemProfessor() {

        Turma turma = mock(Turma.class);

        when(diarioRepository.buscarPorCodigo("D01")).thenReturn(null);

        when(turmaRepository.buscarPorChaveUnica("RDSC", "2026.1", "T01")).thenReturn(turma);

        Exception ex = assertThrows(Exception.class,
                () -> service.cadastrarDiario("D01", "T01", "RDSC", "2026.1", "Diário", "", "Seg 08h", "Sala 01", 60));

        assertEquals("Erro: professor responsável obrigatório.", ex.getMessage());

        verify(diarioRepository, never()).salvar(any());
    }

    private Diario criarDiario(String codigo) {
        return new Diario(codigo, "T001", "Descrição", "P001", "08:00", "Sala 101", 80, SituacaoDiario.ATIVO);
    }
}