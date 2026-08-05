package com.classroompb.service;

import java.util.Collections;
import java.util.List;

import com.classroompb.model.Aula;
import com.classroompb.model.Avaliacao;
import com.classroompb.model.Diario;
import com.classroompb.model.MatriculaTurma;
import com.classroompb.model.SituacaoDiario;
import com.classroompb.model.SituacaoTurma;
import com.classroompb.model.StatusMatricula;
import com.classroompb.model.TipoUsuario;
import com.classroompb.model.Turma;
import com.classroompb.model.Usuario;
import com.classroompb.repository.AulaRepository;
import com.classroompb.repository.AvaliacaoRepository;
import com.classroompb.repository.DiarioRepository;
import com.classroompb.repository.FrequenciaRepository;
import com.classroompb.repository.MatriculaTurmaRepository;
import com.classroompb.repository.NotaRepository;
import com.classroompb.repository.TurmaRepository;
import com.classroompb.repository.UsuarioRepository;

public class DiarioService {
    private final DiarioRepository diarioRepository;
    private final TurmaRepository turmaRepository;
    private final UsuarioRepository usuarioRepository;
    private final AulaRepository aulaRepository;
    private final FrequenciaRepository frequenciaRepository;
    private final AvaliacaoRepository avaliacaoRepository;
    private final NotaRepository notaRepository;
    private final MatriculaTurmaRepository matriculaRepository;

    public DiarioService(DiarioRepository diarioRepository, TurmaRepository turmaRepository,
            UsuarioRepository usuarioRepository) {
        this(diarioRepository, turmaRepository, usuarioRepository, null, null, null, null, null);
    }

    @SuppressWarnings("checkstyle:ParameterNumber")
    public DiarioService(DiarioRepository diarioRepository, TurmaRepository turmaRepository,
            UsuarioRepository usuarioRepository, AulaRepository aulaRepository,
            FrequenciaRepository frequenciaRepository, AvaliacaoRepository avaliacaoRepository,
            NotaRepository notaRepository,
            MatriculaTurmaRepository matriculaRepository) {
        this.diarioRepository = diarioRepository;
        this.turmaRepository = turmaRepository;
        this.usuarioRepository = usuarioRepository;
        this.aulaRepository = aulaRepository;
        this.frequenciaRepository = frequenciaRepository;
        this.avaliacaoRepository = avaliacaoRepository;
        this.notaRepository = notaRepository;
        this.matriculaRepository = matriculaRepository;
    }

    /** Compatibilidade com Releases 1-3. Novos fluxos devem informar o coordenador. */
    public void cadastrarDiario(String codigo, String codigoTurma, String codigoDisciplina, String codigoPeriodo,
            String descricao, String matriculaProfessor, String horario, String sala, int cargaHoraria)
            throws Exception {
        cadastrarInterno(codigo, codigoTurma, codigoDisciplina, codigoPeriodo, descricao, matriculaProfessor, horario,
                sala, cargaHoraria);
    }

    public void cadastrarDiario(Usuario coordenador, String codigo, String codigoTurma, String codigoDisciplina,
            String codigoPeriodo, String descricao, String matriculaProfessor, String horario, String sala,
            int cargaHoraria) throws Exception {
        validarCoordenador(coordenador);
        cadastrarInterno(codigo, codigoTurma, codigoDisciplina, codigoPeriodo, descricao, matriculaProfessor, horario,
                sala, cargaHoraria);
    }

    @SuppressWarnings("checkstyle:ParameterNumber")
    private void cadastrarInterno(String codigo, String codigoTurma, String codigoDisciplina, String codigoPeriodo,
            String descricao, String matriculaProfessor, String horario, String sala, int cargaHoraria)
            throws Exception {
        String codigoNormalizado = obrigatorio(codigo, "codigo");
        if (diarioRepository.buscarPorCodigo(codigoNormalizado) != null) {
            throw new Exception("Erro: já existe um diário com esse código.");
        }
        Turma turma = buscarTurma(codigoDisciplina, codigoPeriodo, codigoTurma);
        validarTurmaAberta(turma);
        String professor = validarProfessor(matriculaProfessor);
        String descricaoNormalizada = obrigatorio(descricao, "descricao");
        String horarioNormalizado = obrigatorio(horario, "horario");
        String salaNormalizada = obrigatorio(sala, "sala");
        validarCargaHoraria(cargaHoraria);
        validarChoqueHorario(professor, horarioNormalizado, codigoPeriodo, null);

        String disciplinaDiario = turma.getCodigoDisciplina() == null ? codigoDisciplina.trim()
                : turma.getCodigoDisciplina();
        String periodoDiario = turma.getCodigoPeriodo() == null ? codigoPeriodo.trim() : turma.getCodigoPeriodo();
        String turmaDiario = turma.getCodigo() == null ? codigoTurma.trim() : turma.getCodigo();
        Diario diario = new Diario(codigoNormalizado, disciplinaDiario, periodoDiario, turmaDiario,
                descricaoNormalizada, professor, horarioNormalizado, salaNormalizada, cargaHoraria,
                SituacaoDiario.ATIVO);
        diarioRepository.salvar(diario);
    }

    /** Compatibilidade: preserva a assinatura antiga para chamadas anteriores. */
    public void editarDiario(Diario diario) throws Exception {
        Diario existente = diario == null ? null : diarioRepository.buscarPorCodigo(diario.getCodigo());
        if (existente == null) {
            throw new Exception("Erro: diário não encontrado.");
        }
        validarAberto(existente);
        if (existente.getCodigoDisciplina() != null && existente.getCodigoPeriodo() != null
                && existente.getCodigoTurma() != null) {
            validarTurmaAberta(buscarTurma(existente.getCodigoDisciplina(), existente.getCodigoPeriodo(),
                    existente.getCodigoTurma()));
        }
        diarioRepository.atualizar(diario);
    }

    public void editarDiario(Usuario coordenador, Diario diario) throws Exception {
        validarCoordenador(coordenador);
        if (diario == null) {
            throw new Exception("Erro: diário obrigatório.");
        }
        Diario existente = diarioRepository.buscarPorCodigo(obrigatorio(diario.getCodigo(), "codigo"));
        if (existente == null) {
            throw new Exception("Erro: diário não encontrado.");
        }
        validarAberto(existente);
        validarTurmaAberta(buscarTurma(existente.getCodigoDisciplina(), existente.getCodigoPeriodo(),
                existente.getCodigoTurma()));
        Turma turma = buscarTurma(diario.getCodigoDisciplina(), diario.getCodigoPeriodo(), diario.getCodigoTurma());
        validarTurmaAberta(turma);
        diario.setCodigoDisciplina(turma.getCodigoDisciplina());
        diario.setCodigoPeriodo(turma.getCodigoPeriodo());
        diario.setCodigoTurma(turma.getCodigo());
        diario.setDescricao(obrigatorio(diario.getDescricao(), "descricao"));
        diario.setMatriculaProfessor(validarProfessor(diario.getMatriculaProfessor()));
        diario.setHorario(obrigatorio(diario.getHorario(), "horario"));
        diario.setSala(obrigatorio(diario.getSala(), "sala"));
        validarCargaHoraria(diario.getCargaHoraria());
        if (diario.getSituacao() == null) {
            throw new Exception("Erro: situação obrigatória.");
        }
        validarChoqueHorario(diario.getMatriculaProfessor(), diario.getHorario(), diario.getCodigoPeriodo(),
                diario.getCodigo());
        diarioRepository.atualizar(diario);
    }

    public Diario buscarPorCodigo(String codigo) {
        return diarioRepository.buscarPorCodigo(codigo);
    }

    public List<Diario> listarDiarios() {
        return diarioRepository.listarTodos();
    }

    public List<Diario> buscarPorTurma(String codigoTurma) {
        return diarioRepository.buscarPorTurma(codigoTurma);
    }

    public List<Diario> buscarPorTurma(String codigoDisciplina, String codigoPeriodo, String codigoTurma) {
        return diarioRepository.buscarPorTurma(codigoDisciplina, codigoPeriodo, codigoTurma);
    }

    public List<Diario> consultarDiariosDaTurma(Usuario coordenador, String codigoDisciplina, String codigoPeriodo,
            String codigoTurma) throws Exception {
        validarCoordenador(coordenador);
        buscarTurma(codigoDisciplina, codigoPeriodo, codigoTurma);
        return buscarPorTurma(codigoDisciplina, codigoPeriodo, codigoTurma);
    }

    public List<Diario> listarPorProfessor(String matriculaProfessor) {
        return diarioRepository.buscarPorProfessor(matriculaProfessor);
    }

    public List<Diario> listarPorProfessor(Usuario professor) throws Exception {
        validarUsuarioProfessor(professor);
        return listarPorProfessor(professor.getMatricula());
    }

    public boolean turmaPossuiDiario(String codigoTurma) {
        return !diarioRepository.buscarPorTurma(codigoTurma).isEmpty();
    }

    public boolean turmaPossuiDiario(String codigoDisciplina, String codigoPeriodo, String codigoTurma) {
        return !diarioRepository.buscarPorTurma(codigoDisciplina, codigoPeriodo, codigoTurma).isEmpty();
    }

    public void fecharDiario(Usuario professor, String codigoDiario) throws Exception {
        exigirDependenciasFechamento();
        validarUsuarioProfessor(professor);
        Diario diario = diarioRepository.buscarPorCodigo(obrigatorio(codigoDiario, "codigo do diario"));
        if (diario == null) {
            throw new Exception("Erro: diario não encontrado.");
        }
        validarResponsavel(professor, diario);
        validarAberto(diario);
        validarTurmaAberta(buscarTurma(diario.getCodigoDisciplina(), diario.getCodigoPeriodo(),
                diario.getCodigoTurma()));

        List<Aula> aulas = aulaRepository.buscarPorDiario(diario.getCodigo());
        if (aulas.isEmpty()) {
            throw new Exception("Erro: o diário deve possuir pelo menos uma aula.");
        }
        double cargaRegistrada = aulas.stream().mapToDouble(Aula::getDuracaoHoras).sum();
        if (cargaRegistrada < diario.getCargaHoraria()) {
            throw new Exception(String.format(
                    "Erro: carga horária insuficiente. Prevista: %.2f horas; registrada: %.2f horas.",
                    (double) diario.getCargaHoraria(), cargaRegistrada));
        }
        List<Avaliacao> avaliacoes = avaliacaoRepository.listarPorDiario(diario.getCodigo());
        if (avaliacoes.isEmpty()) {
            throw new Exception("Erro: o diario deve possuir pelo menos uma avaliacao.");
        }
        List<MatriculaTurma> matriculas = matriculaRepository
                .listarPorTurma(diario.getCodigoDisciplina(), diario.getCodigoPeriodo(), diario.getCodigoTurma())
                .stream().filter(m -> m.getStatus() == StatusMatricula.CONFIRMADA).toList();
        for (MatriculaTurma matricula : matriculas) {
            validarFrequenciasCompletas(matricula, diario, aulas);
            validarNotasCompletas(matricula, avaliacoes);
        }
        diario.setSituacao(SituacaoDiario.ENCERRADO);
        diarioRepository.atualizar(diario);
    }

    public void validarResponsavel(Usuario professor, Diario diario) throws Exception {
        validarUsuarioProfessor(professor);
        if (diario == null || diario.getMatriculaProfessor() == null
                || !diario.getMatriculaProfessor().equalsIgnoreCase(professor.getMatricula())) {
            throw new Exception("Erro: apenas o professor responsavel pelo diario pode realizar esta operacao.");
        }
    }

    public void validarAberto(Diario diario) throws Exception {
        if (diario == null || diario.getSituacao() == SituacaoDiario.ENCERRADO) {
            throw new Exception("Erro: diario fechado nao permite alteracoes.");
        }
    }

    private void validarFrequenciasCompletas(MatriculaTurma matricula, Diario diario, List<Aula> aulas)
            throws Exception {
        for (Aula aula : aulas) {
            if (frequenciaRepository.buscarPorAlunoDiarioEAula(matricula.getMatriculaAluno(), diario.getCodigo(),
                    aula.getCodigo()) == null) {
                throw new Exception("Erro: existem frequencias pendentes.");
            }
        }
    }

    private void validarNotasCompletas(MatriculaTurma matricula, List<Avaliacao> avaliacoes) throws Exception {
        for (Avaliacao avaliacao : avaliacoes) {
            if (notaRepository.buscarPorAlunoEAvaliacao(matricula.getMatriculaAluno(), avaliacao.getCodigo()) == null) {
                throw new Exception("Erro: existem notas pendentes.");
            }
        }
    }

    private Turma buscarTurma(String codigoDisciplina, String codigoPeriodo, String codigoTurma) throws Exception {
        String disciplina = obrigatorio(codigoDisciplina, "codigo da disciplina");
        String periodo = obrigatorio(codigoPeriodo, "codigo do periodo");
        String turma = obrigatorio(codigoTurma, "codigo da turma");
        Turma encontrada = turmaRepository.buscarPorChaveUnica(disciplina, periodo, turma);
        if (encontrada == null) {
            throw new Exception("Erro: turma inexistente.");
        }
        return encontrada;
    }

    private void validarTurmaAberta(Turma turma) throws Exception {
        if (turma != null && turma.getSituacao() == SituacaoTurma.ENCERRADA) {
            throw new Exception("Erro: turma encerrada nao permite alteracoes academicas.");
        }
    }

    private String validarProfessor(String matriculaProfessor) throws Exception {
        String matricula = obrigatorioComMensagem(matriculaProfessor, "Erro: professor responsável obrigatório.");
        Usuario professor = usuarioRepository.buscarPorMatricula(matricula)
                .orElseThrow(() -> new Exception("Erro: Professor com matricula '" + matricula + "' nao encontrado."));
        if (professor.getTipo() != TipoUsuario.PROFESSOR) {
            throw new Exception("Erro: o usuario informado nao e professor.");
        }
        return matricula;
    }

    private void validarChoqueHorario(String matriculaProfessor, String horario, String codigoPeriodo,
            String codigoIgnorado) throws Exception {
        List<Diario> diarios = diarioRepository.buscarPorProfessor(matriculaProfessor);
        if (diarios == null) {
            diarios = Collections.emptyList();
        }
        for (Diario outro : diarios) {
            if (codigoIgnorado != null && codigoIgnorado.equalsIgnoreCase(outro.getCodigo())) {
                continue;
            }
            boolean mesmoPeriodo = codigoPeriodo == null || outro.getCodigoPeriodo() == null
                    || codigoPeriodo.equalsIgnoreCase(outro.getCodigoPeriodo());
            if (mesmoPeriodo && outro.getSituacao() != SituacaoDiario.ENCERRADO
                    && TurmaService.horariosConflitam(horario, outro.getHorario())) {
                throw new Exception("Erro: o professor ja possui diario em horario conflitante.");
            }
        }
    }

    private String obrigatorio(String valor, String campo) throws Exception {
        return obrigatorioComMensagem(valor, "Erro: " + campo + " obrigatorio.");
    }

    private String obrigatorioComMensagem(String valor, String mensagem) throws Exception {
        if (valor == null || valor.trim().isEmpty()) {
            throw new Exception(mensagem);
        }
        return valor.trim();
    }

    private void validarCargaHoraria(int cargaHoraria) throws Exception {
        if (cargaHoraria <= 0) {
            throw new Exception("Erro: carga horaria invalida.");
        }
    }

    private void validarCoordenador(Usuario coordenador) throws Exception {
        if (coordenador == null || coordenador.getTipo() != TipoUsuario.COORDENADOR) {
            throw new Exception("Erro: apenas coordenadores podem gerenciar diarios.");
        }
    }

    private void validarUsuarioProfessor(Usuario professor) throws Exception {
        if (professor == null || professor.getTipo() != TipoUsuario.PROFESSOR) {
            throw new Exception("Erro: apenas professores podem realizar esta operacao.");
        }
    }

    private void exigirDependenciasFechamento() {
        if (aulaRepository == null || frequenciaRepository == null || avaliacaoRepository == null
                || notaRepository == null || matriculaRepository == null) {
            throw new IllegalStateException("Dependencias de fechamento indisponiveis.");
        }
    }
}
