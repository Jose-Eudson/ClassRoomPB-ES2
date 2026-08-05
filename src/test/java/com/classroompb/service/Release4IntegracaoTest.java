package com.classroompb.service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.classroompb.model.Aluno;
import com.classroompb.model.Aula;
import com.classroompb.model.Avaliacao;
import com.classroompb.model.Coordenador;
import com.classroompb.model.Diario;
import com.classroompb.model.Disciplina;
import com.classroompb.model.Historico;
import com.classroompb.model.MatriculaTurma;
import com.classroompb.model.PeriodoLetivo;
import com.classroompb.model.Professor;
import com.classroompb.model.SituacaoDiario;
import com.classroompb.model.SituacaoTurma;
import com.classroompb.model.StatusFrequencia;
import com.classroompb.model.StatusMatricula;
import com.classroompb.model.Turma;
import com.classroompb.repository.AulaRepository;
import com.classroompb.repository.AvaliacaoRepository;
import com.classroompb.repository.DiarioRepository;
import com.classroompb.repository.DisciplinaRepository;
import com.classroompb.repository.FrequenciaRepository;
import com.classroompb.repository.HistoricoRepository;
import com.classroompb.repository.MatriculaTurmaRepository;
import com.classroompb.repository.NotaRepository;
import com.classroompb.repository.PeriodoLetivoRepository;
import com.classroompb.repository.TurmaRepository;
import com.classroompb.repository.UsuarioRepository;

class Release4IntegracaoTest {
    private static final String DISCIPLINA = "ESW2";
    private static final String PERIODO = "2026.1";
    private static final String TURMA = "T01";

    @TempDir
    Path tempDir;

    private AulaRepository aulaRepository;
    private AvaliacaoRepository avaliacaoRepository;
    private DiarioRepository diarioRepository;
    private FrequenciaRepository frequenciaRepository;
    private HistoricoRepository historicoRepository;
    private MatriculaTurmaRepository matriculaRepository;
    private NotaRepository notaRepository;
    private PeriodoLetivoRepository periodoRepository;
    private TurmaRepository turmaRepository;
    private DiarioService diarioService;
    private AulaService aulaService;
    private AvaliacaoService avaliacaoService;
    private FrequenciaService frequenciaService;
    private NotaService notaService;
    private ConsultaAcademicaService consultaService;
    private ConsolidacaoAcademicaService consolidacaoService;
    private PeriodoLetivoService periodoService;
    private TurmaService turmaService;
    private Professor professorUm;
    private Professor professorDois;
    private Coordenador coordenador;
    private Aluno aluno;

    @BeforeEach
    void prepararCenario() {
        aulaRepository = new AulaRepository(arquivo("aulas.json"));
        avaliacaoRepository = new AvaliacaoRepository(arquivo("avaliacoes.json"));
        diarioRepository = new DiarioRepository(arquivo("diarios.json"));
        DisciplinaRepository disciplinaRepository = new DisciplinaRepository(arquivo("disciplinas.json"));
        frequenciaRepository = new FrequenciaRepository(arquivo("frequencias.json"));
        historicoRepository = new HistoricoRepository(arquivo("historicos.json"));
        matriculaRepository = new MatriculaTurmaRepository(arquivo("matriculas.json"));
        notaRepository = new NotaRepository(arquivo("notas.json"));
        periodoRepository = new PeriodoLetivoRepository(arquivo("periodos.json"));
        turmaRepository = new TurmaRepository(arquivo("turmas.json"));
        UsuarioRepository usuarioRepository = new UsuarioRepository(arquivo("usuarios.json"));

        professorUm = new Professor("P0001", "Ada", "ada@teste.com", "senha");
        professorDois = new Professor("P0002", "Grace", "grace@teste.com", "senha");
        coordenador = new Coordenador("C0001", "Coord", "coord@teste.com", "senha");
        aluno = new Aluno("A0001", "Aluno", "aluno@teste.com", "senha");
        usuarioRepository.salvar(professorUm);
        usuarioRepository.salvar(professorDois);
        usuarioRepository.salvar(coordenador);
        usuarioRepository.salvar(aluno);

        disciplinaRepository.salvar(new Disciplina(DISCIPLINA, "Engenharia de Software II", 60, 4, List.of()));
        periodoRepository.salvar(new PeriodoLetivo(PERIODO, 2026, 1, LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 6, 30), true));
        turmaRepository.salvar(new Turma(TURMA, DISCIPLINA, PERIODO, 30, null, null, null));
        MatriculaTurma matricula = new MatriculaTurma(aluno.getMatricula(), DISCIPLINA, PERIODO, TURMA);
        matricula.setStatus(StatusMatricula.CONFIRMADA);
        matriculaRepository.salvar(matricula);

        diarioService = new DiarioService(diarioRepository, turmaRepository, usuarioRepository, aulaRepository,
                frequenciaRepository, avaliacaoRepository, notaRepository, matriculaRepository);
        aulaService = new AulaService(aulaRepository, diarioRepository, turmaRepository);
        avaliacaoService = new AvaliacaoService(avaliacaoRepository, diarioRepository, turmaRepository);
        frequenciaService = new FrequenciaService(frequenciaRepository, turmaRepository, matriculaRepository,
                historicoRepository, notaRepository, disciplinaRepository, usuarioRepository, aulaRepository,
                diarioRepository);
        notaService = new NotaService(notaRepository, turmaRepository, matriculaRepository, historicoRepository,
                frequenciaRepository, disciplinaRepository, usuarioRepository, periodoRepository,
                avaliacaoRepository, diarioRepository);
        Assertions.assertTrue(notaService.possuiContextoLegadoCompleto());
        consultaService = new ConsultaAcademicaService(matriculaRepository, diarioRepository, frequenciaRepository,
                avaliacaoRepository, notaRepository, notaService);
        HistoricoService historicoService = new HistoricoService(historicoRepository, usuarioRepository,
                periodoRepository);
        consolidacaoService = new ConsolidacaoAcademicaService(turmaRepository,
                diarioRepository, matriculaRepository, avaliacaoRepository, notaRepository, frequenciaRepository,
                disciplinaRepository, usuarioRepository, historicoService);
        turmaService = new TurmaService(turmaRepository, disciplinaRepository, periodoRepository, usuarioRepository,
                diarioRepository, consolidacaoService);
        periodoService = new PeriodoLetivoService(periodoRepository, consolidacaoService);
    }

    @Test
    void deveExecutarCenarioDeAceitacaoComDoisDiariosEConsolidacaoUnica() throws Exception {
        cadastrarDiarios();
        aulaService.cadastrarAula(professorUm, "AU01", "D01", LocalDate.of(2026, 3, 2), "Modelagem", 30.0);
        aulaService.cadastrarAula(professorDois, "AU02", "D02", LocalDate.of(2026, 3, 3), "Testes", 30.0);
        avaliacaoService.cadastrar(professorUm, "AV01", "D01", "Projeto", "E1", 2.0, 10.0);
        avaliacaoService.cadastrar(professorUm, "AV02", "D01", "Prova", "E2", 1.0, 20.0);
        avaliacaoService.cadastrar(professorDois, "AV03", "D02", "Seminario", "E1", 1.0, 10.0);

        frequenciaService.registrarFrequencia(professorUm, aluno.getMatricula(), DISCIPLINA, PERIODO, TURMA,
                "AU01", LocalDate.of(2026, 3, 2), StatusFrequencia.PRESENTE);
        frequenciaService.registrarFrequencia(professorDois, aluno.getMatricula(), DISCIPLINA, PERIODO, TURMA,
                "AU02", LocalDate.of(2026, 3, 3), StatusFrequencia.FALTA);
        notaService.lancarNotaAvaliacao(professorUm, aluno.getMatricula(), "AV01", 8.0);
        notaService.lancarNotaAvaliacao(professorUm, aluno.getMatricula(), "AV02", 15.0);
        notaService.lancarNotaAvaliacao(professorDois, aluno.getMatricula(), "AV03", 9.0);

        Assertions.assertEquals((8.0 * 2.0 + 7.5) / 3.0,
                notaService.calcularMediaParcial(aluno.getMatricula(), "D01"), 0.0001);
        Assertions.assertTrue(historicoRepository.listarTodos().isEmpty());
        Assertions.assertEquals(2, consultaService.listarDiariosDoAluno(aluno).size());
        Assertions.assertEquals(1, consultaService.listarFrequencia(aluno, "D01").size());
        Assertions.assertEquals(2, consultaService.listarAvaliacoes(aluno, "D01").size());
        Assertions.assertEquals(2, consultaService.listarNotas(aluno, "D01").size());
        Assertions.assertEquals((8.0 * 2.0 + 7.5) / 3.0, consultaService.calcularMediaParcial(aluno, "D01"),
                0.0001);

        diarioService.fecharDiario(professorUm, "D01");
        diarioService.fecharDiario(professorDois, "D02");
        turmaService.encerrarTurma(coordenador, DISCIPLINA, PERIODO, TURMA);
        periodoService.encerrarPeriodo(coordenador, PERIODO);

        Assertions.assertEquals(SituacaoDiario.ENCERRADO, diarioRepository.buscarPorCodigo("D01").getSituacao());
        Assertions.assertEquals(SituacaoTurma.ENCERRADA,
                turmaRepository.buscarPorChaveUnica(DISCIPLINA, PERIODO, TURMA).getSituacao());
        Assertions.assertTrue(periodoRepository.buscarPorCodigo(PERIODO).isEncerrado());
        Assertions.assertEquals(1, historicoRepository.listarTodos().size());
        Historico historico = historicoRepository.listarTodos().get(0);
        double mediaD1 = (8.0 * 2.0 + 7.5) / 3.0;
        Assertions.assertEquals((mediaD1 + 9.0) / 2.0, historico.getNotaFinal(), 0.0001);
        Assertions.assertEquals(50.0, historico.getFrequencia(), 0.0001);
        Assertions.assertEquals("Multiplos professores", historico.getNomeProfessor());

        periodoService.encerrarPeriodo(coordenador, PERIODO);
        Assertions.assertEquals(1, historicoRepository.listarTodos().size());
    }

    @Test
    void devePersistirIdentidadeCompostaEIsolarDadosPorDiario() throws Exception {
        cadastrarDiarios();
        diarioService.cadastrarDiario(coordenador, "D03", TURMA, DISCIPLINA, PERIODO, "Exercicios",
                professorUm.getMatricula(), "QUA 08:00-10:00", "Sala 2", 15);
        turmaRepository.salvar(new Turma(TURMA, DISCIPLINA, "2026.2", 30, null, null, null));
        diarioService.cadastrarDiario(coordenador, "D04", TURMA, DISCIPLINA, "2026.2", "Nova oferta",
                professorUm.getMatricula(), "SEG 08:00-10:00", "Sala 4", 30);

        Assertions.assertEquals(3, diarioService.consultarDiariosDaTurma(coordenador, DISCIPLINA, PERIODO, TURMA)
                .size());
        Assertions.assertEquals(3, diarioService.listarPorProfessor(professorUm).size());
        Assertions.assertTrue(diarioService.turmaPossuiDiario(DISCIPLINA, PERIODO, TURMA));

        DiarioRepository recarregado = new DiarioRepository(arquivo("diarios.json"));
        Diario diario = recarregado.buscarPorCodigo("D01");
        Assertions.assertEquals(DISCIPLINA, diario.getCodigoDisciplina());
        Assertions.assertEquals(PERIODO, diario.getCodigoPeriodo());
        Assertions.assertEquals(TURMA, diario.getCodigoTurma());
        Assertions.assertEquals(3, recarregado.buscarPorTurma(DISCIPLINA, PERIODO, TURMA).size());
        Assertions.assertEquals(1, recarregado.buscarPorTurma(DISCIPLINA, "2026.2", TURMA).size());
    }

    @Test
    void devePersistirEditarEExcluirAvaliacao() throws Exception {
        cadastrarUmDiario();
        Avaliacao avaliacao = avaliacaoService.cadastrar(professorUm, "AV01", "D01", "Projeto", "E1", 2.0, 20.0);
        Assertions.assertThrows(Exception.class,
                () -> avaliacaoService.cadastrar(professorUm, "AV01", "D01", "Duplicada", "E1", 1.0, 10.0));

        AvaliacaoRepository recarregado = new AvaliacaoRepository(arquivo("avaliacoes.json"));
        Assertions.assertEquals(20.0, recarregado.buscarPorCodigo("AV01").getNotaMaxima());
        avaliacao.setDescricao("Projeto final");
        avaliacao.setEtapa("E2");
        avaliacao.setPeso(3.0);
        avaliacaoService.editar(professorUm, avaliacao);
        Assertions.assertEquals(3.0, new AvaliacaoRepository(arquivo("avaliacoes.json")).buscarPorCodigo("AV01")
                .getPeso());

        Avaliacao movida = new Avaliacao("AV01", "OUTRO", DISCIPLINA, PERIODO, TURMA, "Projeto", "E2", 1.0, 10.0);
        Assertions.assertThrows(Exception.class, () -> avaliacaoService.editar(professorUm, movida));
        avaliacaoService.remover(professorUm, "AV01");
        Assertions.assertTrue(new AvaliacaoRepository(arquivo("avaliacoes.json")).listarTodas().isEmpty());
    }

    @Test
    void deveLerJsonsLegadosSemOsNovosCampos() throws Exception {
        Path diarios = tempDir.resolve("diarios-legados.json");
        Files.writeString(diarios,
                "[{\"codigo\":\"DL1\",\"codigoTurma\":\"T01\",\"descricao\":\"Legado\","
                        + "\"matriculaProfessor\":\"P0001\",\"horario\":\"08:00\",\"sala\":\"S1\","
                        + "\"cargaHoraria\":30,\"situacao\":\"ATIVO\"}]",
                StandardCharsets.UTF_8);
        Diario legado = new DiarioRepository(diarios.toString()).buscarPorCodigo("DL1");
        Assertions.assertEquals("T01", legado.getCodigoTurma());
        Assertions.assertNull(legado.getCodigoDisciplina());

        Path frequencias = tempDir.resolve("frequencias-legadas.json");
        Files.writeString(frequencias,
                "[{\"matriculaAluno\":\"A0001\",\"codigoDisciplina\":\"ESW2\","
                        + "\"codigoPeriodo\":\"2026.1\",\"codigoTurma\":\"T01\",\"codigoAula\":\"AU1\","
                        + "\"dataAula\":\"2026-03-02\",\"status\":\"PRESENTE\","
                        + "\"matriculaProfessor\":\"P0001\"}]",
                StandardCharsets.UTF_8);
        Assertions.assertNull(new FrequenciaRepository(frequencias.toString()).listarTodas().get(0).getCodigoDiario());

        Path notas = tempDir.resolve("notas-legadas.json");
        Files.writeString(notas,
                "[{\"matriculaAluno\":\"A0001\",\"codigoDisciplina\":\"ESW2\","
                        + "\"codigoPeriodo\":\"2026.1\",\"codigoTurma\":\"T01\","
                        + "\"etapa1\":8.0,\"etapa2\":9.0}]",
                StandardCharsets.UTF_8);
        Assertions.assertEquals(8.0, new NotaRepository(notas.toString()).listarTodas().get(0).getEtapa1());
    }

    @Test
    void deveValidarCadastroEdicaoEChoqueDeHorarioDoDiario() throws Exception {
        diarioService.cadastrarDiario(coordenador, "D01", TURMA, DISCIPLINA, PERIODO, "Teoria",
                professorUm.getMatricula(), "SEG 08:00-10:00", "Sala 1", 30);

        Assertions.assertThrows(Exception.class,
                () -> diarioService.cadastrarDiario(aluno, "D02", TURMA, DISCIPLINA, PERIODO, "Pratica",
                        professorDois.getMatricula(), "TER 08:00-10:00", "Lab", 30));
        Assertions.assertThrows(Exception.class,
                () -> diarioService.cadastrarDiario(coordenador, "D01", TURMA, DISCIPLINA, PERIODO, "Duplicado",
                        professorDois.getMatricula(), "TER 08:00-10:00", "Lab", 30));
        Assertions.assertThrows(Exception.class,
                () -> diarioService.cadastrarDiario(coordenador, "D03", TURMA, DISCIPLINA, PERIODO, "Conflito",
                        professorUm.getMatricula(), "SEG 09:00-11:00", "Sala 2", 30));

        Diario diario = diarioRepository.buscarPorCodigo("D01");
        diario.setDescricao("Teoria atualizada");
        diario.setHorario("QUA 08:00-10:00");
        diario.setSala("Sala 3");
        diarioService.editarDiario(coordenador, diario);
        Assertions.assertEquals("Teoria atualizada", diarioRepository.buscarPorCodigo("D01").getDescricao());

        diario.setMatriculaProfessor("INEXISTENTE");
        Assertions.assertThrows(Exception.class, () -> diarioService.editarDiario(coordenador, diario));
    }

    @Test
    void deveRejeitarFechamentoEnquantoExistiremPendencias() throws Exception {
        cadastrarUmDiario();
        Assertions.assertThrows(Exception.class, () -> diarioService.fecharDiario(professorUm, "D01"));

        aulaService.cadastrarAula(professorUm, "AU01", "D01", LocalDate.of(2026, 3, 2), "Modelagem", 30.0);
        Assertions.assertThrows(Exception.class, () -> diarioService.fecharDiario(professorUm, "D01"));

        avaliacaoService.cadastrar(professorUm, "AV01", "D01", "Projeto", "E1", 1.0, 10.0);
        Assertions.assertThrows(Exception.class, () -> diarioService.fecharDiario(professorUm, "D01"));

        frequenciaService.registrarPresenca(professorUm, aluno.getMatricula(), DISCIPLINA, PERIODO, TURMA, "AU01",
                LocalDate.of(2026, 3, 2));
        Assertions.assertThrows(Exception.class, () -> diarioService.fecharDiario(professorUm, "D01"));

        notaService.lancarNotaAvaliacao(professorUm, aluno.getMatricula(), "AV01", 7.0);
        Assertions.assertThrows(Exception.class, () -> diarioService.fecharDiario(professorDois, "D01"));
        diarioService.fecharDiario(professorUm, "D01");
        Assertions.assertThrows(Exception.class, () -> diarioService.fecharDiario(professorUm, "D01"));
    }

    @Test
    void deveBloquearTodasAsAlteracoesDepoisDoFechamento() throws Exception {
        cadastrarUmDiario();
        aulaService.cadastrarAula(professorUm, "AU01", "D01", LocalDate.of(2026, 3, 2), "Modelagem", 30.0);
        avaliacaoService.cadastrar(professorUm, "AV01", "D01", "Projeto", "E1", 1.0, 10.0);
        frequenciaService.registrarFalta(professorUm, aluno.getMatricula(), DISCIPLINA, PERIODO, TURMA, "AU01",
                LocalDate.of(2026, 3, 2));
        notaService.lancarNotaAvaliacao(professorUm, aluno.getMatricula(), "AV01", 7.0);
        diarioService.fecharDiario(professorUm, "D01");

        Assertions.assertThrows(Exception.class, () -> aulaService.cadastrarAula(professorUm, "AU02", "D01",
                LocalDate.of(2026, 3, 3), "Testes", 1.0));
        Aula aula = aulaRepository.buscarPorCodigo("AU01");
        aula.setConteudo("Alterado");
        Assertions.assertThrows(Exception.class, () -> aulaService.atualizarAula(professorUm, aula));
        Assertions.assertThrows(Exception.class, () -> aulaService.removerAula(professorUm, "AU01"));
        Assertions.assertThrows(Exception.class,
                () -> avaliacaoService.cadastrar(professorUm, "AV02", "D01", "Prova", "E2", 1.0, 10.0));
        Assertions.assertThrows(Exception.class, () -> avaliacaoService.remover(professorUm, "AV01"));
        Assertions.assertThrows(Exception.class, () -> frequenciaService.registrarPresenca(professorUm,
                aluno.getMatricula(), DISCIPLINA, PERIODO, TURMA, "AU01", LocalDate.of(2026, 3, 2)));
        Assertions.assertThrows(Exception.class,
                () -> notaService.alterarNotaAvaliacao(professorUm, aluno.getMatricula(), "AV01", 8.0));
        Assertions.assertThrows(Exception.class,
                () -> diarioService.editarDiario(coordenador, diarioRepository.buscarPorCodigo("D01")));
    }

    @Test
    void deveValidarAutorizacaoEManterNotasSemDuplicidade() throws Exception {
        cadastrarUmDiario();
        Assertions.assertThrows(Exception.class, () -> aulaService.cadastrarAula(professorDois, "AU01", "D01",
                LocalDate.of(2026, 3, 2), "Modelagem", 1.0));
        aulaService.cadastrarAula(professorUm, "AU01", "D01", LocalDate.of(2026, 3, 2), "Modelagem", 1.0);
        Assertions.assertThrows(Exception.class,
                () -> avaliacaoService.cadastrar(professorDois, "AV01", "D01", "Projeto", "E1", 1.0, 10.0));
        Avaliacao avaliacao = avaliacaoService.cadastrar(professorUm, "AV01", "D01", "Projeto", "E1", 2.0, 10.0);
        avaliacao.setDescricao("Projeto final");
        avaliacaoService.editar(professorUm, avaliacao);
        Assertions.assertEquals("Projeto final", avaliacaoService.buscarPorCodigo("AV01").getDescricao());

        Assertions.assertThrows(Exception.class,
                () -> notaService.lancarNotaAvaliacao(professorDois, aluno.getMatricula(), "AV01", 8.0));
        Assertions.assertThrows(Exception.class,
                () -> notaService.lancarNotaAvaliacao(professorUm, aluno.getMatricula(), "AV01", 11.0));
        notaService.lancarNotaAvaliacao(professorUm, aluno.getMatricula(), "AV01", 8.0);
        notaService.alterarNotaAvaliacao(professorUm, aluno.getMatricula(), "AV01", 9.0);
        Assertions.assertEquals(1, notaRepository.listarPorAlunoEDiario(aluno.getMatricula(), "D01").size());
        Assertions.assertEquals(9.0,
                notaRepository.buscarPorAlunoEAvaliacao(aluno.getMatricula(), "AV01").getValor());
    }

    @Test
    void deveIsolarFrequenciasDeAulasNaMesmaData() throws Exception {
        cadastrarDiarios();
        LocalDate data = LocalDate.of(2026, 3, 2);
        aulaService.cadastrarAula(professorUm, "AU01", "D01", data, "Teoria", 1.0);
        aulaService.cadastrarAula(professorDois, "AU02", "D02", data, "Laboratorio", 1.0);
        frequenciaService.registrarPresenca(professorUm, aluno.getMatricula(), DISCIPLINA, PERIODO, TURMA, "AU01",
                data);
        frequenciaService.registrarFalta(professorDois, aluno.getMatricula(), DISCIPLINA, PERIODO, TURMA, "AU02",
                data);

        Assertions.assertEquals(StatusFrequencia.PRESENTE,
                frequenciaService.listarFrequenciaDaAula(professorUm, "D01", "AU01").get(0).getStatus());
        Assertions.assertEquals(StatusFrequencia.FALTA,
                frequenciaService.listarFrequenciaDaAula(professorDois, "D02", "AU02").get(0).getStatus());
        Assertions.assertThrows(Exception.class, () -> frequenciaService.registrarPresenca(professorUm,
                aluno.getMatricula(), DISCIPLINA, PERIODO, TURMA, "AU01", data.plusDays(1)));
        Assertions.assertThrows(Exception.class,
                () -> frequenciaService.listarFrequenciaDaAula(professorDois, "D01", "AU01"));
    }

    @Test
    void deveRestringirConsultasAAlunoComMatriculaConfirmada() throws Exception {
        cadastrarUmDiario();
        Assertions.assertThrows(Exception.class, () -> consultaService.listarDiariosDoAluno(professorUm));
        Assertions.assertThrows(Exception.class, () -> consultaService.listarNotas(aluno, "INEXISTENTE"));

        MatriculaTurma matricula = matriculaRepository.buscarPorChaveUnica(aluno.getMatricula(), DISCIPLINA, PERIODO,
                TURMA);
        matricula.setStatus(StatusMatricula.CANCELADA);
        matriculaRepository.atualizar(matricula);
        Assertions.assertTrue(consultaService.listarDiariosDoAluno(aluno).isEmpty());
        Assertions.assertThrows(Exception.class, () -> consultaService.listarAvaliacoes(aluno, "D01"));
    }

    @Test
    void deveImpedirConsolidacaoSemDiarioOuComDiarioAberto() throws Exception {
        Turma turma = turmaRepository.buscarPorChaveUnica(DISCIPLINA, PERIODO, TURMA);
        Assertions.assertThrows(Exception.class, () -> consolidacaoService.consolidarTurma(turma));
        cadastrarUmDiario();
        Assertions.assertThrows(Exception.class, () -> consolidacaoService.consolidarTurma(turma));
    }

    @Test
    void deveManterRecuperacaoParcialSemEncerrarTurmaOuGerarHistorico() throws Exception {
        cadastrarUmDiario();
        aulaService.cadastrarAula(professorUm, "AU01", "D01", LocalDate.of(2026, 3, 2), "Modelagem", 30.0);
        avaliacaoService.cadastrar(professorUm, "AV01", "D01", "Prova", "E1", 1.0, 10.0);
        frequenciaService.registrarPresenca(professorUm, aluno.getMatricula(), DISCIPLINA, PERIODO, TURMA, "AU01",
                LocalDate.of(2026, 3, 2));
        notaService.lancarNotaAvaliacao(professorUm, aluno.getMatricula(), "AV01", 5.0);

        Assertions.assertEquals(5.0, notaService.calcularMediaParcial(aluno.getMatricula(), "D01"));
        diarioService.fecharDiario(professorUm, "D01");
        Exception erro = Assertions.assertThrows(Exception.class,
                () -> turmaService.encerrarTurma(coordenador, DISCIPLINA, PERIODO, TURMA));
        Assertions.assertTrue(erro.getMessage().contains(aluno.getMatricula()));
        Assertions.assertTrue(historicoRepository.listarTodos().isEmpty());
        Assertions.assertEquals(SituacaoTurma.ABERTA,
                turmaRepository.buscarPorChaveUnica(DISCIPLINA, PERIODO, TURMA).getSituacao());
    }

    private void cadastrarDiarios() throws Exception {
        diarioService.cadastrarDiario(coordenador, "D01", TURMA, DISCIPLINA, PERIODO, "Parte teorica",
                professorUm.getMatricula(), "SEG 08:00-10:00", "Sala 1", 30);
        diarioService.cadastrarDiario(coordenador, "D02", TURMA, DISCIPLINA, PERIODO, "Parte pratica",
                professorDois.getMatricula(), "TER 08:00-10:00", "Lab 1", 30);
    }

    private void cadastrarUmDiario() throws Exception {
        diarioService.cadastrarDiario(coordenador, "D01", TURMA, DISCIPLINA, PERIODO, "Parte teorica",
                professorUm.getMatricula(), "SEG 08:00-10:00", "Sala 1", 30);
    }

    private String arquivo(String nome) {
        return tempDir.resolve(nome).toString();
    }
}
