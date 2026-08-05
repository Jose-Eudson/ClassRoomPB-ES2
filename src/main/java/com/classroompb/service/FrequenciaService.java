package com.classroompb.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.classroompb.model.Aula;
import com.classroompb.model.Diario;
import com.classroompb.model.Disciplina;
import com.classroompb.model.MatriculaTurma;
import com.classroompb.model.Nota;
import com.classroompb.model.RegistroFrequencia;
import com.classroompb.model.SituacaoDiario;
import com.classroompb.model.SituacaoTurma;
import com.classroompb.model.StatusFrequencia;
import com.classroompb.model.StatusMatricula;
import com.classroompb.model.TipoUsuario;
import com.classroompb.model.Turma;
import com.classroompb.model.Usuario;
import com.classroompb.repository.DisciplinaRepository;
import com.classroompb.repository.FrequenciaRepository;
import com.classroompb.repository.HistoricoRepository;
import com.classroompb.repository.MatriculaTurmaRepository;
import com.classroompb.repository.NotaRepository;
import com.classroompb.repository.TurmaRepository;
import com.classroompb.repository.UsuarioRepository;
import com.classroompb.repository.AulaRepository;
import com.classroompb.repository.DiarioRepository;

/**
 * RF27: Servico responsavel pelo registro de presenca/falta dos alunos por aula.
 */
public class FrequenciaService {

    private final FrequenciaRepository frequenciaRepository;
    private final TurmaRepository turmaRepository;
    private final MatriculaTurmaRepository matriculaRepository;
    private final HistoricoService historicoService;
    private final NotaRepository notaRepository;
    private final DisciplinaRepository disciplinaRepository;
    private final UsuarioRepository usuarioRepository;
    private final AulaRepository aulaRepository;
    private final DiarioRepository diarioRepository;

    private static final double FREQUENCIA_MINIMA = 75.0;

    public FrequenciaService(FrequenciaRepository frequenciaRepository, TurmaRepository turmaRepository,
            MatriculaTurmaRepository matriculaRepository) {
        this(frequenciaRepository, turmaRepository, matriculaRepository, null, null, null, null, null, null);
    }

    public FrequenciaService(FrequenciaRepository frequenciaRepository, TurmaRepository turmaRepository,
            MatriculaTurmaRepository matriculaRepository, AulaRepository aulaRepository,
            DiarioRepository diarioRepository) {
        this(frequenciaRepository, turmaRepository, matriculaRepository, null, null, null, null, aulaRepository,
                diarioRepository);
    }

    public FrequenciaService(FrequenciaRepository frequenciaRepository, TurmaRepository turmaRepository,
            MatriculaTurmaRepository matriculaRepository, HistoricoRepository historicoRepository,
            NotaRepository notaRepository, DisciplinaRepository disciplinaRepository,
            UsuarioRepository usuarioRepository, AulaRepository aulaRepository, DiarioRepository diarioRepository) {
        this.frequenciaRepository = frequenciaRepository;
        this.turmaRepository = turmaRepository;
        this.matriculaRepository = matriculaRepository;
        this.historicoService = historicoRepository == null ? null : new HistoricoService(historicoRepository);
        this.notaRepository = notaRepository;
        this.disciplinaRepository = disciplinaRepository;
        this.usuarioRepository = usuarioRepository;
        this.aulaRepository = aulaRepository;
        this.diarioRepository = diarioRepository;
    }

    /**
     * Registra ou corrige a frequencia de um aluno em uma aula.
     */
    public RegistroFrequencia registrarFrequencia(Usuario professor, String matriculaAluno, String codigoDisciplina,
            String codigoPeriodo, String codigoTurma, String codigoAula, LocalDate dataAula, StatusFrequencia status)
            throws Exception {
        validarProfessor(professor);
        String alunoNorm = validarCampoObrigatorio(matriculaAluno, "matricula do aluno");
        String discNorm = validarCampoObrigatorio(codigoDisciplina, "codigo da disciplina");
        String periodoNorm = validarCampoObrigatorio(codigoPeriodo, "codigo do periodo letivo");
        String turmaNorm = validarCampoObrigatorio(codigoTurma, "codigo da turma");
        String codAula = validarCampoObrigatorio(codigoAula, "codigo da aula");

        Diario diarioContexto = null;
        if (aulaRepository != null && diarioRepository != null) {
            Aula aula = aulaRepository.buscarPorCodigo(codAula);

            if (aula == null) {
                throw new Exception("Erro: Aula inexistente.");
            }

            Diario diario = diarioRepository.buscarPorCodigo(aula.getCodigoDiario());

            if (diario == null) {
                throw new Exception("Erro: Diário inexistente.");
            }

            if (!diario.getCodigoTurma().equalsIgnoreCase(turmaNorm)) {
                throw new Exception("Erro: A aula não pertence à turma informada.");
            }

            if (!diario.getMatriculaProfessor().equalsIgnoreCase(professor.getMatricula())) {
                throw new Exception("Erro: Somente o professor responsável pelo diário pode registrar frequência.");
            }
            if (diario.getSituacao() == SituacaoDiario.ENCERRADO) {
                throw new Exception("Erro: Diario fechado nao permite registrar frequencia.");
            }
            if (dataAula != null && !dataAula.equals(aula.getData())) {
                throw new Exception("Erro: A data informada nao corresponde a aula selecionada.");
            }
            diarioContexto = diario;
        }

        if (dataAula == null) {
            throw new Exception("Erro: Data da aula nao pode ser vazia.");
        }
        if (status == null) {
            throw new Exception("Erro: Status da frequencia nao pode ser vazio.");
        }

        Turma turma = buscarTurmaOuFalhar(discNorm, periodoNorm, turmaNorm);
        if (turma.getSituacao() == SituacaoTurma.ENCERRADA) {
            throw new Exception("Erro: turma encerrada nao permite registrar frequencia.");
        }

        if (diarioContexto == null) {
            validarProfessorResponsavel(professor, turma);
        }
        validarMatriculaConfirmada(alunoNorm, discNorm, periodoNorm, turmaNorm);

        RegistroFrequencia frequencia = diarioContexto == null
                ? frequenciaRepository.buscarPorChaveUnica(alunoNorm, discNorm, periodoNorm, turmaNorm, dataAula,
                        codAula)
                : frequenciaRepository.buscarPorAlunoDiarioEAula(alunoNorm, diarioContexto.getCodigo(), codAula);

        if (frequencia == null) {
            frequencia = new RegistroFrequencia(alunoNorm, discNorm, periodoNorm, turmaNorm,
                    diarioContexto == null ? null : diarioContexto.getCodigo(), codAula, dataAula, status,
                    professor.getMatricula());
            frequenciaRepository.salvar(frequencia);
        } else {
            frequencia.setStatus(status);
            frequencia.setMatriculaProfessor(professor.getMatricula());
            frequencia.setDataRegistro(LocalDateTime.now());
            frequenciaRepository.atualizar(frequencia);
        }

        if (diarioContexto == null) {
            sincronizarHistorico(alunoNorm, discNorm, periodoNorm, turmaNorm, turma);
        }
        return frequencia;
    }

    /** Registra presenca para um aluno em uma aula. */
    public RegistroFrequencia registrarPresenca(Usuario professor, String matriculaAluno, String codigoDisciplina,
            String codigoPeriodo, String codigoTurma, String codigoAula, LocalDate dataAula) throws Exception {
        String codAula = validarCampoObrigatorio(codigoAula, "codigo da aula");
        return registrarFrequencia(professor, matriculaAluno, codigoDisciplina, codigoPeriodo, codigoTurma, codAula,
                dataAula, StatusFrequencia.PRESENTE);
    }

    /** Registra falta para um aluno em uma aula. */
    public RegistroFrequencia registrarFalta(Usuario professor, String matriculaAluno, String codigoDisciplina,
            String codigoPeriodo, String codigoTurma, String codigoAula, LocalDate dataAula) throws Exception {
        String codAula = validarCampoObrigatorio(codigoAula, "codigo da aula");
        return registrarFrequencia(professor, matriculaAluno, codigoDisciplina, codigoPeriodo, codigoTurma, codAula,
                dataAula, StatusFrequencia.FALTA);
    }

    /** Lista a frequencia lancada para uma turma em uma aula. */
    public List<RegistroFrequencia> listarFrequenciaDaAula(Usuario professor, String codigoDisciplina,
            String codigoPeriodo, String codigoTurma, LocalDate dataAula) throws Exception {
        validarProfessor(professor);
        String discNorm = validarCampoObrigatorio(codigoDisciplina, "codigo da disciplina");
        String periodoNorm = validarCampoObrigatorio(codigoPeriodo, "codigo do periodo letivo");
        String turmaNorm = validarCampoObrigatorio(codigoTurma, "codigo da turma");

        if (dataAula == null) {
            throw new Exception("Erro: Data da aula nao pode ser vazia.");
        }

        Turma turma = buscarTurmaOuFalhar(discNorm, periodoNorm, turmaNorm);
        validarProfessorResponsavel(professor, turma);

        return frequenciaRepository.listarPorTurmaEData(discNorm, periodoNorm, turmaNorm, dataAula);
    }

    public List<RegistroFrequencia> listarFrequenciaDaAula(Usuario professor, String codigoDiario, String codigoAula)
            throws Exception {
        validarProfessor(professor);
        String diarioCodigo = validarCampoObrigatorio(codigoDiario, "codigo do diario");
        String aulaCodigo = validarCampoObrigatorio(codigoAula, "codigo da aula");
        if (diarioRepository == null || aulaRepository == null) {
            throw new Exception("Erro: Consulta por diario indisponivel.");
        }
        Diario diario = diarioRepository.buscarPorCodigo(diarioCodigo);
        Aula aula = aulaRepository.buscarPorCodigo(aulaCodigo);
        if (diario == null || aula == null || !diario.getCodigo().equalsIgnoreCase(aula.getCodigoDiario())) {
            throw new Exception("Erro: Aula nao pertence ao diario informado.");
        }
        if (!diario.getMatriculaProfessor().equalsIgnoreCase(professor.getMatricula())) {
            throw new Exception("Erro: Diario pertence a outro professor.");
        }
        return frequenciaRepository.listarPorDiarioEAula(diarioCodigo, aulaCodigo);
    }

    public List<RegistroFrequencia> listarPorAlunoEDiario(String matriculaAluno, String codigoDiario) {
        return frequenciaRepository.listarPorAlunoEDiario(matriculaAluno, codigoDiario);
    }

    /** Lista as turmas sob responsabilidade do professor informado. */
    public List<Turma> listarTurmasDoProfessor(Usuario professor) throws Exception {
        validarProfessor(professor);
        String matriculaProfessor = professor.getMatricula();
        if (matriculaProfessor == null || matriculaProfessor.trim().isEmpty()) {
            throw new Exception("Erro: Matricula do professor nao pode ser vazia.");
        }

        return turmaRepository.listarTodos().stream()
                .filter(t -> t.getMatriculaProfessor() != null
                        && t.getMatriculaProfessor().trim().equalsIgnoreCase(matriculaProfessor.trim()))
                .collect(Collectors.toList());
    }

    /** Lista as matriculas confirmadas de uma turma do professor. */
    public List<MatriculaTurma> listarMatriculasConfirmadasDaTurma(Usuario professor, String codigoDisciplina,
            String codigoPeriodo, String codigoTurma) throws Exception {
        validarProfessor(professor);
        String discNorm = validarCampoObrigatorio(codigoDisciplina, "codigo da disciplina");
        String periodoNorm = validarCampoObrigatorio(codigoPeriodo, "codigo do periodo letivo");
        String turmaNorm = validarCampoObrigatorio(codigoTurma, "codigo da turma");

        Turma turma = buscarTurmaOuFalhar(discNorm, periodoNorm, turmaNorm);
        validarProfessorResponsavel(professor, turma);

        return matriculaRepository.listarPorTurma(discNorm, periodoNorm, turmaNorm).stream()
                .filter(m -> m.getStatus() == StatusMatricula.CONFIRMADA).collect(Collectors.toList());
    }

    public List<MatriculaTurma> listarMatriculasConfirmadasDoDiario(Usuario professor, String codigoDiario)
            throws Exception {
        validarProfessor(professor);
        if (diarioRepository == null) {
            throw new Exception("Erro: Consulta por diario indisponivel.");
        }
        Diario diario = diarioRepository.buscarPorCodigo(validarCampoObrigatorio(codigoDiario, "codigo do diario"));
        if (diario == null || !professor.getMatricula().equalsIgnoreCase(diario.getMatriculaProfessor())) {
            throw new Exception("Erro: Diario pertence a outro professor.");
        }
        return matriculaRepository
                .listarPorTurma(diario.getCodigoDisciplina(), diario.getCodigoPeriodo(), diario.getCodigoTurma())
                .stream().filter(m -> m.getStatus() == StatusMatricula.CONFIRMADA).collect(Collectors.toList());
    }

    public String obterAlertaFrequencia(double percentual) {
        if (percentual > FREQUENCIA_MINIMA && percentual <= 80.0) {
            return "Aviso: sua frequência de faltas está próxima do limite permitido (" + FREQUENCIA_MINIMA + ").";
        } else if (percentual == FREQUENCIA_MINIMA) {
            return "Atenção: você atingiu o limite mínimo de frequência permitido (" + FREQUENCIA_MINIMA
                    + "). Evite novas faltas.";
        } else if (percentual < FREQUENCIA_MINIMA) {
            return "Você está abaixo da frequência mínima exigida (" + FREQUENCIA_MINIMA
                    + ") e ultrapassou o limite de faltas.";
        }
        return null;
    }

    public double calcularPercentualFrequencia(String matriculaAluno, String codigoDisciplina, String codigoPeriodo,
            String codigoTurma) {

        List<RegistroFrequencia> registros = frequenciaRepository.listarPorAlunoETurma(matriculaAluno, codigoDisciplina,
                codigoPeriodo, codigoTurma);

        if (registros.isEmpty()) {
            return 0.0;
        }

        long presentes = registros.stream().filter(r -> r.getStatus() == StatusFrequencia.PRESENTE).count();

        return (presentes * 100.0) / registros.size();
    }

    private void sincronizarHistorico(String matriculaAluno, String codigoDisciplina, String codigoPeriodo,
            String codigoTurma, Turma turma) {
        if (historicoService == null || notaRepository == null || disciplinaRepository == null
                || usuarioRepository == null) {
            return;
        }
        Nota nota = notaRepository.buscarPorChaveUnica(matriculaAluno, codigoDisciplina, codigoPeriodo, codigoTurma);
        if (nota == null || nota.getEtapa1() == null || nota.getEtapa2() == null) {
            return;
        }

        double media = (nota.getEtapa1() + nota.getEtapa2()) / 2.0;
        double frequencia = calcularPercentualFrequencia(matriculaAluno, codigoDisciplina, codigoPeriodo, codigoTurma);
        String situacao = NotaService.calcularSituacao(media, frequencia);
        Disciplina disciplina = disciplinaRepository.buscarPorCodigo(codigoDisciplina);
        Usuario professor = usuarioRepository.buscarPorMatricula(turma.getMatriculaProfessor()).orElse(null);
        historicoService.registrarHistoricoCompleto(matriculaAluno, codigoPeriodo, codigoDisciplina, codigoTurma, turma,
                disciplina, professor, media, frequencia, situacao);
    }

    private void validarProfessor(Usuario professor) throws Exception {
        if (professor == null || professor.getTipo() != TipoUsuario.PROFESSOR) {
            throw new Exception("Erro: Apenas professores podem registrar frequencia.");
        }
    }

    private String validarCampoObrigatorio(String valor, String nomeCampo) throws Exception {
        if (valor == null || valor.trim().isEmpty()) {
            throw new Exception("Erro: " + nomeCampo + " nao pode ser vazio.");
        }
        return valor.trim();
    }

    private Turma buscarTurmaOuFalhar(String codigoDisciplina, String codigoPeriodo, String codigoTurma)
            throws Exception {
        Turma turma = turmaRepository.buscarPorChaveUnica(codigoDisciplina, codigoPeriodo, codigoTurma);
        if (turma == null) {
            throw new Exception("Erro: Turma '" + codigoTurma + "' da disciplina '" + codigoDisciplina
                    + "' no periodo '" + codigoPeriodo + "' nao encontrada.");
        }
        return turma;
    }

    private void validarProfessorResponsavel(Usuario professor, Turma turma) throws Exception {
        String responsavel = turma.getMatriculaProfessor();
        if (responsavel == null || responsavel.trim().isEmpty()
                || !responsavel.trim().equalsIgnoreCase(professor.getMatricula())) {
            throw new Exception("Erro: Apenas o professor responsavel pela turma pode registrar frequencia.");
        }
    }

    private void validarMatriculaConfirmada(String matriculaAluno, String codigoDisciplina, String codigoPeriodo,
            String codigoTurma) throws Exception {
        MatriculaTurma matricula = matriculaRepository.buscarPorChaveUnica(matriculaAluno, codigoDisciplina,
                codigoPeriodo, codigoTurma);

        if (matricula == null || matricula.getStatus() != StatusMatricula.CONFIRMADA) {
            throw new Exception("Erro: Frequencia so pode ser registrada para alunos com matricula confirmada.");
        }
    }

    /**
     * RF29: Retorna todos os registros de frequencia de um aluno em uma determinada turma/disciplina.
     */
    public List<RegistroFrequencia> obterFrequenciaAluno(String matriculaAluno, String codigoDisciplina,
            String codigoPeriodo, String codigoTurma) throws Exception {

        validarCampoObrigatorio(matriculaAluno, "matricula do aluno");
        validarCampoObrigatorio(codigoDisciplina, "codigo da disciplina");
        validarCampoObrigatorio(codigoPeriodo, "codigo do periodo");
        validarCampoObrigatorio(codigoTurma, "codigo da turma");

        List<RegistroFrequencia> registros = frequenciaRepository.listarPorAlunoETurma(matriculaAluno.trim(),
                codigoDisciplina.trim(), codigoPeriodo.trim(), codigoTurma.trim());

        if (registros.isEmpty()) {
            throw new Exception("Nenhum registro de frequência encontrado para os dados informados.");
        }

        return registros;
    }
}
