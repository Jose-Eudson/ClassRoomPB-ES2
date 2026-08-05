package com.classroompb.service;

import com.classroompb.model.Avaliacao;
import com.classroompb.model.Diario;
import com.classroompb.model.MatriculaTurma;
import com.classroompb.model.Nota;
import com.classroompb.model.PeriodoLetivo;
import com.classroompb.model.StatusMatricula;
import com.classroompb.model.SituacaoDiario;
import com.classroompb.model.SituacaoTurma;
import com.classroompb.model.TipoUsuario;
import com.classroompb.model.Turma;
import com.classroompb.model.Usuario;
import com.classroompb.repository.DisciplinaRepository;
import com.classroompb.repository.AvaliacaoRepository;
import com.classroompb.repository.DiarioRepository;
import com.classroompb.repository.FrequenciaRepository;
import com.classroompb.repository.HistoricoRepository;
import com.classroompb.repository.MatriculaTurmaRepository;
import com.classroompb.repository.NotaRepository;
import com.classroompb.repository.PeriodoLetivoRepository;
import com.classroompb.repository.TurmaRepository;
import com.classroompb.repository.UsuarioRepository;

public class NotaService {

    private final NotaRepository notaRepository;
    private final TurmaRepository turmaRepository;
    private final MatriculaTurmaRepository matriculaRepository;
    private final HistoricoRepository historicoRepository;
    private final FrequenciaRepository frequenciaRepository;
    private final DisciplinaRepository disciplinaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PeriodoLetivoRepository periodoRepository;
    private AvaliacaoRepository avaliacaoRepository;
    private DiarioRepository diarioRepository;

    public NotaService(NotaRepository notaRepository, TurmaRepository turmaRepository,
            MatriculaTurmaRepository matriculaRepository) {

        this(notaRepository, turmaRepository, matriculaRepository, null);
    }

    public NotaService(NotaRepository notaRepository, TurmaRepository turmaRepository,
            MatriculaTurmaRepository matriculaRepository, HistoricoRepository historicoRepository) {

        this(notaRepository, turmaRepository, matriculaRepository, historicoRepository, null, null, null, null);
    }

    public NotaService(NotaRepository notaRepository, TurmaRepository turmaRepository,
            MatriculaTurmaRepository matriculaRepository, HistoricoRepository historicoRepository,
            FrequenciaRepository frequenciaRepository, DisciplinaRepository disciplinaRepository,
            UsuarioRepository usuarioRepository) {

        this(notaRepository, turmaRepository, matriculaRepository, historicoRepository, frequenciaRepository,
                disciplinaRepository, usuarioRepository, null);
    }

    @SuppressWarnings("checkstyle:ParameterNumber")
    public NotaService(NotaRepository notaRepository, TurmaRepository turmaRepository,
            MatriculaTurmaRepository matriculaRepository, HistoricoRepository historicoRepository,
            FrequenciaRepository frequenciaRepository, DisciplinaRepository disciplinaRepository,
            UsuarioRepository usuarioRepository, PeriodoLetivoRepository periodoRepository) {

        this.notaRepository = notaRepository;
        this.turmaRepository = turmaRepository;
        this.matriculaRepository = matriculaRepository;
        this.historicoRepository = historicoRepository;
        this.frequenciaRepository = frequenciaRepository;
        this.disciplinaRepository = disciplinaRepository;
        this.usuarioRepository = usuarioRepository;
        this.periodoRepository = periodoRepository;
    }

    @SuppressWarnings("checkstyle:ParameterNumber")
    public NotaService(NotaRepository notaRepository, TurmaRepository turmaRepository,
            MatriculaTurmaRepository matriculaRepository, HistoricoRepository historicoRepository,
            FrequenciaRepository frequenciaRepository, DisciplinaRepository disciplinaRepository,
            UsuarioRepository usuarioRepository, PeriodoLetivoRepository periodoRepository,
            AvaliacaoRepository avaliacaoRepository, DiarioRepository diarioRepository) {
        this(notaRepository, turmaRepository, matriculaRepository, historicoRepository, frequenciaRepository,
                disciplinaRepository, usuarioRepository, periodoRepository);
        this.avaliacaoRepository = avaliacaoRepository;
        this.diarioRepository = diarioRepository;
    }

    public Nota lancarNotaAvaliacao(Usuario professor, String matriculaAluno, String codigoAvaliacao, Double valor)
            throws Exception {
        return salvarNotaAvaliacao(professor, matriculaAluno, codigoAvaliacao, valor, false);
    }

    public Nota alterarNotaAvaliacao(Usuario professor, String matriculaAluno, String codigoAvaliacao, Double valor)
            throws Exception {
        return salvarNotaAvaliacao(professor, matriculaAluno, codigoAvaliacao, valor, true);
    }

    public java.util.List<Nota> listarNotasPorAlunoEDiario(String matriculaAluno, String codigoDiario) {
        return notaRepository.listarPorAlunoEDiario(matriculaAluno, codigoDiario);
    }

    /**
     * Indica se o construtor legado recebeu o contexto acadêmico completo, sem consolidar histórico antecipadamente.
     */
    public boolean possuiContextoLegadoCompleto() {
        return historicoRepository != null && frequenciaRepository != null && disciplinaRepository != null
                && usuarioRepository != null;
    }

    /**
     * Media parcial em escala 0-10: soma((valor/notaMaxima)*10*peso) / soma(pesos lancados).
     */
    public double calcularMediaParcial(String matriculaAluno, String codigoDiario) throws Exception {
        validarDependenciasRelease4();
        java.util.List<Avaliacao> avaliacoes = avaliacaoRepository.listarPorDiario(codigoDiario);
        double somaPonderada = 0.0;
        double somaPesos = 0.0;
        for (Avaliacao avaliacao : avaliacoes) {
            Nota nota = notaRepository.buscarPorAlunoEAvaliacao(matriculaAluno, avaliacao.getCodigo());
            if (nota != null && nota.getValor() != null) {
                somaPonderada += (nota.getValor() / avaliacao.getNotaMaxima()) * 10.0 * avaliacao.getPeso();
                somaPesos += avaliacao.getPeso();
            }
        }
        if (somaPesos == 0.0) {
            throw new Exception("Erro: nenhuma nota lancada para o diario.");
        }
        return somaPonderada / somaPesos;
    }

    private Nota salvarNotaAvaliacao(Usuario professor, String matriculaAluno, String codigoAvaliacao, Double valor,
            boolean exigirExistente) throws Exception {
        validarDependenciasRelease4();
        validarProfessor(professor);
        String aluno = validarCampoObrigatorio(matriculaAluno, "matricula do aluno");
        String codigo = validarCampoObrigatorio(codigoAvaliacao, "codigo da avaliacao");
        Avaliacao avaliacao = avaliacaoRepository.buscarPorCodigo(codigo);
        if (avaliacao == null) {
            throw new Exception("Erro: avaliacao nao encontrada.");
        }
        Diario diario = diarioRepository.buscarPorCodigo(avaliacao.getCodigoDiario());
        if (diario == null || !diario.getCodigo().equalsIgnoreCase(avaliacao.getCodigoDiario())) {
            throw new Exception("Erro: avaliacao sem diario valido.");
        }
        if (!diario.getMatriculaProfessor().equalsIgnoreCase(professor.getMatricula())) {
            throw new Exception("Erro: apenas o professor responsavel pelo diario pode lancar notas.");
        }
        if (diario.getSituacao() == SituacaoDiario.ENCERRADO) {
            throw new Exception("Erro: diario fechado nao permite lancar ou alterar notas.");
        }
        Turma turma = turmaRepository.buscarPorChaveUnica(diario.getCodigoDisciplina(), diario.getCodigoPeriodo(),
                diario.getCodigoTurma());
        if (turma != null && turma.getSituacao() == SituacaoTurma.ENCERRADA) {
            throw new Exception("Erro: turma encerrada nao permite lancar ou alterar notas.");
        }
        if (valor == null || valor < 0.0 || valor > avaliacao.getNotaMaxima()) {
            throw new Exception("Erro: nota deve estar entre zero e a nota maxima da avaliacao.");
        }
        MatriculaTurma matricula = matriculaRepository.buscarPorChaveUnica(aluno, diario.getCodigoDisciplina(),
                diario.getCodigoPeriodo(), diario.getCodigoTurma());
        if (matricula == null || matricula.getStatus() != StatusMatricula.CONFIRMADA) {
            throw new Exception("Erro: o aluno nao possui matricula confirmada na turma do diario.");
        }
        Nota nota = notaRepository.buscarPorAlunoEAvaliacao(aluno, codigo);
        if (exigirExistente && nota == null) {
            throw new Exception("Erro: nota nao encontrada.");
        }
        if (nota == null) {
            nota = new Nota(aluno, diario.getCodigoDisciplina(), diario.getCodigoPeriodo(), diario.getCodigoTurma(),
                    diario.getCodigo(), avaliacao.getCodigo(), valor, professor.getMatricula());
            notaRepository.salvar(nota);
        } else {
            nota.setValor(valor);
            nota.setMatriculaProfessor(professor.getMatricula());
            notaRepository.atualizar(nota);
        }
        return nota;
    }

    private void validarDependenciasRelease4() {
        if (avaliacaoRepository == null || diarioRepository == null) {
            throw new IllegalStateException("Dependencias da Release 4 indisponiveis.");
        }
    }

    public void lancarNotas(Usuario professor, String matriculaAluno, String codigoDisciplina, String codigoPeriodo,
            String codigoTurma, Double etapa1, Double etapa2) throws Exception {
        String[] dados = validarOperacaoLegada(professor, matriculaAluno, codigoDisciplina, codigoPeriodo, codigoTurma,
                etapa1, etapa2, "lançar");
        matriculaAluno = dados[0];
        codigoDisciplina = dados[1];
        codigoPeriodo = dados[2];
        codigoTurma = dados[3];

        Nota nota = notaRepository.buscarPorChaveUnica(matriculaAluno, codigoDisciplina, codigoPeriodo, codigoTurma);

        if (nota == null) {

            nota = new Nota(matriculaAluno, codigoDisciplina, codigoPeriodo, codigoTurma);

            nota.setEtapa1(etapa1);
            nota.setEtapa2(etapa2);

            notaRepository.salvar(nota);

        } else {

            nota.setEtapa1(etapa1);
            nota.setEtapa2(etapa2);

            notaRepository.atualizar(nota);
        }

    }

    public void alterarNotas(Usuario professor, String matriculaAluno, String codigoDisciplina, String codigoPeriodo,
            String codigoTurma, Double etapa1, Double etapa2) throws Exception {
        String[] dados = validarOperacaoLegada(professor, matriculaAluno, codigoDisciplina, codigoPeriodo, codigoTurma,
                etapa1, etapa2, "alterar");
        matriculaAluno = dados[0];
        codigoDisciplina = dados[1];
        codigoPeriodo = dados[2];
        codigoTurma = dados[3];

        Nota nota = notaRepository.buscarPorChaveUnica(matriculaAluno, codigoDisciplina, codigoPeriodo, codigoTurma);

        if (nota == null) {
            throw new Exception("Erro: Nenhuma nota lançada para este aluno.");
        }

        nota.setEtapa1(etapa1);
        nota.setEtapa2(etapa2);
        notaRepository.atualizar(nota);

    }

    @SuppressWarnings("checkstyle:ParameterNumber")
    private String[] validarOperacaoLegada(Usuario professor, String matriculaAluno, String codigoDisciplina,
            String codigoPeriodo, String codigoTurma, Double etapa1, Double etapa2, String acao) throws Exception {
        validarProfessor(professor);
        String aluno = validarCampoObrigatorio(matriculaAluno, "matrícula do aluno");
        String disciplina = validarCampoObrigatorio(codigoDisciplina, "código da disciplina");
        String periodo = validarCampoObrigatorio(codigoPeriodo, "código do período");
        String turmaCodigo = validarCampoObrigatorio(codigoTurma, "código da turma");
        validarPeriodoNaoEncerrado(periodo);
        validarNota(etapa1, "Etapa 1");
        validarNota(etapa2, "Etapa 2");
        Turma turma = turmaRepository.buscarPorChaveUnica(disciplina, periodo, turmaCodigo);
        if (turma == null) {
            throw new Exception("Erro: Turma não encontrada.");
        }
        if (!turma.getMatriculaProfessor().equalsIgnoreCase(professor.getMatricula())) {
            throw new Exception("Erro: Apenas o professor responsável pode " + acao + " notas.");
        }
        MatriculaTurma matricula = matriculaRepository.buscarPorChaveUnica(aluno, disciplina, periodo, turmaCodigo);
        if (matricula == null || matricula.getStatus() != StatusMatricula.CONFIRMADA) {
            throw new Exception("Erro: O aluno não possui matrícula confirmada.");
        }
        return new String[] { aluno, disciplina, periodo, turmaCodigo };
    }

    public Nota consultarNotas(String matriculaAluno, String codigoDisciplina, String codigoPeriodo, String codigoTurma)
            throws Exception {

        Nota nota = notaRepository.buscarPorChaveUnica(matriculaAluno, codigoDisciplina, codigoPeriodo, codigoTurma);

        if (nota == null) {
            throw new Exception("Erro: nenhuma nota encontrada");
        }

        return nota;
    }

    public double calcularMediaFinal(String matriculaAluno, String codigoDisciplina, String codigoPeriodo,
            String codigoTurma) throws Exception {

        Nota nota = notaRepository.buscarPorChaveUnica(matriculaAluno, codigoDisciplina, codigoPeriodo, codigoTurma);

        if (nota == null) {
            throw new Exception("Erro: Notas não encontradas.");
        }

        if (nota.getEtapa1() == null || nota.getEtapa2() == null) {
            throw new Exception("Erro: As duas notas devem estar lançadas.");
        }

        return (nota.getEtapa1() + nota.getEtapa2()) / 2.0;
    }

    public String calcularSituacaoFinal(String matriculaAluno, String codigoDisciplina, String codigoPeriodo,
            String codigoTurma) throws Exception {

        return calcularSituacaoFinal(matriculaAluno, codigoDisciplina, codigoPeriodo, codigoTurma, Double.NaN);
    }

    public String calcularSituacaoFinal(String matriculaAluno, String codigoDisciplina, String codigoPeriodo,
            String codigoTurma, double percentualFrequencia) throws Exception {

        double media = calcularMediaFinal(matriculaAluno, codigoDisciplina, codigoPeriodo, codigoTurma);

        return calcularSituacao(media, percentualFrequencia);
    }

    public static String calcularSituacao(double media, double percentualFrequencia) {
        if (!Double.isNaN(percentualFrequencia) && percentualFrequencia < 75.0) {
            return "REPROVADO POR FALTA";
        }

        if (media >= 7.0) {
            return "APROVADO";
        }

        if (media >= 4.0) {
            return "RECUPERACAO";
        }

        return "REPROVADO POR NOTA";
    }

    private void validarProfessor(Usuario professor) throws Exception {

        if (professor == null || professor.getTipo() != TipoUsuario.PROFESSOR) {

            throw new Exception("Erro: Apenas professores podem lançar notas.");
        }
    }

    private String validarCampoObrigatorio(String valor, String campo) throws Exception {

        if (valor == null || valor.trim().isEmpty()) {

            throw new Exception("Erro: " + campo + " não pode ser vazio.");
        }

        return valor.trim();
    }

    private void validarNota(Double nota, String etapa) throws Exception {

        if (nota == null) {

            throw new Exception(etapa + " não pode ser nula.");
        }

        if (nota < 0 || nota > 10) {

            throw new Exception(etapa + " deve estar entre 0 e 10.");
        }
    }

    private void validarPeriodoNaoEncerrado(String codigoPeriodo) throws Exception {
        if (periodoRepository == null) {
            return;
        }

        PeriodoLetivo periodo = periodoRepository.buscarPorCodigo(codigoPeriodo);
        if (periodo == null) {
            throw new Exception("Erro: Período letivo não encontrado.");
        }
        if (periodo.isEncerrado()) {
            throw new Exception("Erro: O período letivo está encerrado. Não é permitido lançar ou alterar notas.");
        }
    }
}
