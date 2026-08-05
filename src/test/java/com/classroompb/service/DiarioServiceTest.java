package com.classroompb.service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import com.classroompb.model.Diario;
import com.classroompb.model.Aula;
import com.classroompb.model.Avaliacao;
import com.classroompb.model.Professor;
import com.classroompb.model.SituacaoDiario;
import com.classroompb.model.TipoUsuario;
import com.classroompb.model.Turma;
import com.classroompb.model.Usuario;
import com.classroompb.repository.DiarioRepository;
import com.classroompb.repository.AulaRepository;
import com.classroompb.repository.AvaliacaoRepository;
import com.classroompb.repository.FrequenciaRepository;
import com.classroompb.repository.MatriculaTurmaRepository;
import com.classroompb.repository.NotaRepository;
import com.classroompb.repository.TurmaRepository;
import com.classroompb.repository.UsuarioRepository;


public class DiarioServiceTest {

    private DiarioService service;
    private DiarioRepository diarioRepository;
    private TurmaRepository turmaRepository;
    private UsuarioRepository usuarioRepository;
    private AulaRepository aulaRepository;
    private FrequenciaRepository frequenciaRepository;
    private AvaliacaoRepository avaliacaoRepository;
    private NotaRepository notaRepository;
    private MatriculaTurmaRepository matriculaRepository;

    @BeforeEach
    void setUp() {
        diarioRepository = Mockito.mock(DiarioRepository.class);
        turmaRepository = Mockito.mock(TurmaRepository.class);
        usuarioRepository = Mockito.mock(UsuarioRepository.class);
        aulaRepository = Mockito.mock(AulaRepository.class);
        frequenciaRepository = Mockito.mock(FrequenciaRepository.class);
        avaliacaoRepository = Mockito.mock(AvaliacaoRepository.class);
        notaRepository = Mockito.mock(NotaRepository.class);
        matriculaRepository = Mockito.mock(MatriculaTurmaRepository.class);
        service = new DiarioService(diarioRepository, turmaRepository, usuarioRepository, aulaRepository,
                frequenciaRepository, avaliacaoRepository, notaRepository, matriculaRepository);
    }

    private Usuario configurarProfessorValido() {
        Usuario professor = Mockito.mock(Usuario.class);
        Mockito.when(professor.getTipo()).thenReturn(TipoUsuario.PROFESSOR);
        Mockito.when(usuarioRepository.buscarPorMatricula("P001")).thenReturn(Optional.of(professor));
        return professor;
    }

    @Test
    @DisplayName("Deve cadastrar diário com dados válidos")
    void deveCadastrarDiarioComDadosValidos() throws Exception {
        Mockito.when(diarioRepository.buscarPorCodigo("D001")).thenReturn(null);
        Mockito.when(turmaRepository.buscarPorChaveUnica("DISC001", "PERIODO1", "T001"))
                .thenReturn(Mockito.mock(Turma.class));

        configurarProfessorValido();

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
        Mockito.verify(diarioRepository).salvar(captor.capture());

        Diario diarioSalvo = captor.getValue();
        Assertions.assertEquals("D001", diarioSalvo.getCodigo());
        Assertions.assertEquals("T001", diarioSalvo.getCodigoTurma());
        Assertions.assertEquals("Descrição do diário", diarioSalvo.getDescricao());
        Assertions.assertEquals("P001", diarioSalvo.getMatriculaProfessor());
        Assertions.assertEquals("08:00", diarioSalvo.getHorario());
        Assertions.assertEquals("Sala 101", diarioSalvo.getSala());
        Assertions.assertEquals(80, diarioSalvo.getCargaHoraria());
        Assertions.assertEquals(SituacaoDiario.ATIVO, diarioSalvo.getSituacao());
    }

    @Test
    @DisplayName("Deve lançar exceção quando o código for nulo")
    void deveLancarExcecaoQuandoCodigoEhNulo() {
        Exception exception = Assertions.assertThrows(Exception.class, () -> service.cadastrarDiario(null, "T001", "DISC001",
                "PERIODO1", "Descrição", "P001", "08:00", "Sala 101", 80));

        Assertions.assertNotNull(exception.getMessage());
        Mockito.verify(diarioRepository, Mockito.never()).salvar(ArgumentMatchers.any(Diario.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando o código estiver em branco")
    void deveLancarExcecaoQuandoCodigoEhEmBranco() {
        Exception exception = Assertions.assertThrows(Exception.class, () -> service.cadastrarDiario("   ", "T001", "DISC001",
                "PERIODO1", "Descrição", "P001", "08:00", "Sala 101", 80));

        Assertions.assertNotNull(exception.getMessage());
        Mockito.verify(diarioRepository, Mockito.never()).salvar(ArgumentMatchers.any(Diario.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando o código já existir")
    void deveLancarExcecaoQuandoCodigoJaExiste() {
        Mockito.when(diarioRepository.buscarPorCodigo("D001")).thenReturn(Mockito.mock(Diario.class));

        Exception exception = Assertions.assertThrows(
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

        Assertions.assertTrue(exception.getMessage().contains("código"));
        Mockito.verify(diarioRepository, Mockito.never()).salvar(ArgumentMatchers.any(Diario.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando a turma não existir")
    void deveLancarExcecaoQuandoTurmaNaoExiste() {
        Mockito.when(diarioRepository.buscarPorCodigo("D001")).thenReturn(null);
        Mockito.when(turmaRepository.buscarPorChaveUnica("DISC001", "PERIODO1", "T001"))
                .thenReturn(null);

        Exception exception = Assertions.assertThrows(
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

        Assertions.assertTrue(exception.getMessage().contains("turma"));
        Mockito.verify(diarioRepository, Mockito.never()).salvar(ArgumentMatchers.any(Diario.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando o professor não existir")
    void deveLancarExcecaoQuandoProfessorNaoExiste() {
        Mockito.when(diarioRepository.buscarPorCodigo("D001")).thenReturn(null);
        Mockito.when(turmaRepository.buscarPorChaveUnica("DISC001", "PERIODO1", "T001"))
                .thenReturn(Mockito.mock(Turma.class));
        Mockito.when(usuarioRepository.buscarPorMatricula("P001")).thenReturn(Optional.empty());

        Exception exception = Assertions.assertThrows(
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

        Assertions.assertTrue(exception.getMessage().contains("Professor"));
        Mockito.verify(diarioRepository, Mockito.never()).salvar(ArgumentMatchers.any(Diario.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando o usuário não for professor")
    void deveLancarExcecaoQuandoUsuarioNaoEhProfessor() {
        Mockito.when(diarioRepository.buscarPorCodigo("D001")).thenReturn(null);
        Mockito.when(turmaRepository.buscarPorChaveUnica("DISC001", "PERIODO1", "T001"))
                .thenReturn(Mockito.mock(Turma.class));

        Usuario usuario = Mockito.mock(Usuario.class);
        Mockito.when(usuario.getTipo()).thenReturn(TipoUsuario.ALUNO);
        Mockito.when(usuarioRepository.buscarPorMatricula("P001"))
                .thenReturn(Optional.of(usuario));

        Exception exception = Assertions.assertThrows(
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

        Assertions.assertTrue(exception.getMessage().contains("professor"));
        Mockito.verify(diarioRepository, Mockito.never()).salvar(ArgumentMatchers.any(Diario.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando a carga horária for inválida")
    void deveLancarExcecaoQuandoCargaHorariaEhInvalida() {
        Mockito.when(diarioRepository.buscarPorCodigo("D001")).thenReturn(null);
        Mockito.when(turmaRepository.buscarPorChaveUnica("DISC001", "PERIODO1", "T001"))
                .thenReturn(Mockito.mock(Turma.class));

        configurarProfessorValido();

        Exception exception = Assertions.assertThrows(
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

        Assertions.assertTrue(exception.getMessage().contains("carga"));
        Mockito.verify(diarioRepository, Mockito.never()).salvar(ArgumentMatchers.any(Diario.class));
    }

    @Test
    @DisplayName("Deve editar um diário quando ele existir")
    void deveEditarDiarioQuandoExistir() throws Exception {
        Diario diario = criarDiario("D001");
        Mockito.when(diarioRepository.buscarPorCodigo("D001")).thenReturn(diario);

        service.editarDiario(diario);

        Mockito.verify(diarioRepository).atualizar(diario);
    }

    @Test
    @DisplayName("Deve lançar exceção ao editar um diário inexistente")
    void deveLancarExcecaoAoEditarDiarioInexistente() {
        Diario diario = criarDiario("D999");
        Mockito.when(diarioRepository.buscarPorCodigo("D999")).thenReturn(null);

        Exception exception = Assertions.assertThrows(Exception.class, () -> service.editarDiario(diario));

        Assertions.assertNotNull(exception.getMessage());
        Mockito.verify(diarioRepository, Mockito.never()).atualizar(ArgumentMatchers.any(Diario.class));
    }

    @Test
    @DisplayName("Deve buscar um diário por código")
    void deveBuscarDiarioPorCodigo() {
        Diario diario = criarDiario("D001");
        Mockito.when(diarioRepository.buscarPorCodigo("D001")).thenReturn(diario);

        Diario resultado = service.buscarPorCodigo("D001");

        Assertions.assertSame(diario, resultado);
    }

    @Test
    @DisplayName("Deve listar todos os diários")
    void deveListarTodosOsDiarios() {
        List<Diario> diarios = Arrays.asList(criarDiario("D001"), criarDiario("D002"));
        Mockito.when(diarioRepository.listarTodos()).thenReturn(diarios);

        List<Diario> resultado = service.listarDiarios();

        Assertions.assertEquals(diarios, resultado);
    }

    @Test
    @DisplayName("Deve listar diários por turma")
    void deveListarDiariosPorTurma() {
        List<Diario> diarios = Arrays.asList(criarDiario("D001"), criarDiario("D002"));
        Mockito.when(diarioRepository.buscarPorTurma("T001")).thenReturn(diarios);

        List<Diario> resultado = service.buscarPorTurma("T001");

        Assertions.assertEquals(diarios, resultado);
    }

    @Test
    @DisplayName("Deve listar diários por professor")
    void deveListarDiariosPorProfessor() {
        List<Diario> diarios = Arrays.asList(criarDiario("D001"), criarDiario("D002"));
        Mockito.when(diarioRepository.buscarPorProfessor("P001")).thenReturn(diarios);

        List<Diario> resultado = service.listarPorProfessor("P001");

        Assertions.assertEquals(diarios, resultado);
    }

    @Test
    @DisplayName("Deve permitir cadastrar vários diários para a mesma turma")
    void devePermitirVariosDiariosParaMesmaTurma() throws Exception {

        Turma turma = Mockito.mock(Turma.class);
        Usuario professor = Mockito.mock(Usuario.class);

        Mockito.when(turmaRepository.buscarPorChaveUnica("RDSC", "2026.1", "T01")).thenReturn(turma);

        Mockito.when(usuarioRepository.buscarPorMatricula("P0001")).thenReturn(Optional.of(professor));

        Mockito.when(professor.getTipo()).thenReturn(TipoUsuario.PROFESSOR);

        Mockito.when(diarioRepository.buscarPorCodigo("D01")).thenReturn(null);

        Mockito.when(diarioRepository.buscarPorCodigo("D02")).thenReturn(null);

        service.cadastrarDiario("D01", "T01", "RDSC", "2026.1", "Diário A", "P0001", "Seg 08h", "Lab 01", 60);

        service.cadastrarDiario("D02", "T01", "RDSC", "2026.1", "Diário B", "P0001", "Qua 10h", "Lab 02", 60);

        Mockito.verify(diarioRepository, Mockito.times(2)).salvar(ArgumentMatchers.any(Diario.class));
    }

    @Test
    @DisplayName("Deve listar todos os diários de uma turma")
    void deveListarDiariosDaTurma() {

        Diario d1 = Mockito.mock(Diario.class);
        Diario d2 = Mockito.mock(Diario.class);

        Mockito.when(diarioRepository.buscarPorTurma("T01")).thenReturn(List.of(d1, d2));

        List<Diario> diarios = service.buscarPorTurma("T01");

        Assertions.assertEquals(2, diarios.size());
    }

    @Test
    @DisplayName("Professor pode ser responsável por vários diários")
    void deveListarDiariosDoProfessor() {

        Diario d1 = Mockito.mock(Diario.class);
        Diario d2 = Mockito.mock(Diario.class);
        Diario d3 = Mockito.mock(Diario.class);

        Mockito.when(diarioRepository.buscarPorProfessor("P0001")).thenReturn(List.of(d1, d2, d3));

        List<Diario> diarios = service.listarPorProfessor("P0001");

        Assertions.assertEquals(3, diarios.size());
    }

    @Test
    @DisplayName("Não deve permitir cadastrar diário sem professor responsável")
    void naoDevePermitirDiarioSemProfessor() {

        Turma turma = Mockito.mock(Turma.class);

        Mockito.when(diarioRepository.buscarPorCodigo("D01")).thenReturn(null);

        Mockito.when(turmaRepository.buscarPorChaveUnica("RDSC", "2026.1", "T01")).thenReturn(turma);

        Exception ex = Assertions.assertThrows(Exception.class,
                () -> service.cadastrarDiario("D01", "T01", "RDSC", "2026.1", "Diário", "", "Seg 08h", "Sala 01", 60));

        Assertions.assertEquals("Erro: professor responsável obrigatório.", ex.getMessage());

        Mockito.verify(diarioRepository, Mockito.never()).salvar(ArgumentMatchers.any());
    }

    private Diario criarDiario(String codigo) {
        return new Diario(codigo, "T001", "Descrição", "P001", "08:00", "Sala 101", 80, SituacaoDiario.ATIVO);
    }

    @Test
    void naoDeveFecharComCargaHorariaInferior() {
        Professor professor = prepararFechamento(List.of(aula("A1", 29.5)));
        Exception erro = Assertions.assertThrows(Exception.class, () -> service.fecharDiario(professor, "D001"));
        Assertions.assertTrue(erro.getMessage().contains("30,00") || erro.getMessage().contains("30.00"));
        Mockito.verify(diarioRepository, Mockito.never()).atualizar(Mockito.any());
    }

    @Test
    void deveFecharComCargaHorariaIgual() throws Exception {
        Professor professor = prepararFechamento(List.of(aula("A1", 30.0)));
        service.fecharDiario(professor, "D001");
        Mockito.verify(diarioRepository).atualizar(ArgumentMatchers.argThat(
                d -> d.getSituacao() == SituacaoDiario.ENCERRADO));
    }

    @Test
    void deveFecharComCargaHorariaSuperior() throws Exception {
        Professor professor = prepararFechamento(List.of(aula("A1", 20.0), aula("A2", 15.0)));
        service.fecharDiario(professor, "D001");
        Mockito.verify(diarioRepository).atualizar(ArgumentMatchers.argThat(
                d -> d.getSituacao() == SituacaoDiario.ENCERRADO));
    }

    private Professor prepararFechamento(List<Aula> aulas) {
        Professor professor = new Professor("P001", "Prof", "p@teste.com", "senha");
        Turma turma = new Turma("T001", "DISC001", "2026.1", 20, null, null, null);
        Diario diario = new Diario("D001", "DISC001", "2026.1", "T001", "Teoria", "P001", "SEG", "S1", 30,
                SituacaoDiario.ATIVO);
        Mockito.when(diarioRepository.buscarPorCodigo("D001")).thenReturn(diario);
        Mockito.when(turmaRepository.buscarPorChaveUnica("DISC001", "2026.1", "T001")).thenReturn(turma);
        Mockito.when(aulaRepository.buscarPorDiario("D001")).thenReturn(aulas);
        Mockito.when(avaliacaoRepository.listarPorDiario("D001"))
                .thenReturn(List.of(new Avaliacao("AV1", "D001", "DISC001", "2026.1", "T001", "Prova", "E1",
                        1.0, 10.0)));
        Mockito.when(matriculaRepository.listarPorTurma("DISC001", "2026.1", "T001")).thenReturn(List.of());
        return professor;
    }

    private Aula aula(String codigo, double duracao) {
        return new Aula(codigo, "D001", LocalDate.now(), "Conteudo", 1, duracao);
    }
}
