package com.classroompb.service;

import java.util.List;
import java.util.stream.Collectors;

import com.classroompb.model.Aluno;
import com.classroompb.model.Coordenador;
import com.classroompb.model.Disciplina;
import com.classroompb.model.Historico;
import com.classroompb.model.PeriodoLetivo;
import com.classroompb.model.TipoUsuario;
import com.classroompb.model.Turma;
import com.classroompb.model.Usuario;
import com.classroompb.repository.HistoricoRepository;
import com.classroompb.repository.PeriodoLetivoRepository;
import com.classroompb.repository.UsuarioRepository;

public class HistoricoService {

    private final HistoricoRepository repository;
    private final UsuarioRepository usuarioRepository;
    private final PeriodoLetivoRepository periodoRepository;

    public HistoricoService(HistoricoRepository repository) {
        this(repository, null, null);
    }

    public HistoricoService(HistoricoRepository repository, UsuarioRepository usuarioRepository) {
        this(repository, usuarioRepository, null);
    }

    public HistoricoService(HistoricoRepository repository, UsuarioRepository usuarioRepository,
            PeriodoLetivoRepository periodoRepository) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
        this.periodoRepository = periodoRepository;
    }

    public boolean alunoFoiAprovado(String matriculaAluno, String codigoDisciplina) {

        List<Historico> historicos = repository.buscarPorAluno(matriculaAluno);

        return historicos.stream()
                .anyMatch(h -> h.getCodigoDisciplina().equalsIgnoreCase(codigoDisciplina) && h.isAprovado());
    }

    public List<Historico> listarHistoricoDoAluno(String matriculaAluno) {

        String matricula = validarCampoObrigatorio(matriculaAluno, "matrícula do aluno");
        return filtrarDisciplinasConcluidas(repository.buscarPorAluno(matricula));
    }

    public List<Historico> consultarHistoricoAluno(Usuario solicitante) throws Exception {
        if (solicitante == null || solicitante.getTipo() != TipoUsuario.ALUNO) {
            throw new Exception("Erro: Apenas alunos podem consultar o próprio histórico.");
        }
        return filtrarDisciplinasConcluidas(repository.buscarPorAluno(solicitante.getMatricula()));
    }

    public List<Historico> consultarHistoricoAlunoPeloCoordenador(Usuario solicitante, String matriculaAluno)
            throws Exception {
        if (solicitante == null || solicitante.getTipo() != TipoUsuario.COORDENADOR
                || !(solicitante instanceof Coordenador)) {
            throw new Exception("Erro: Apenas coordenadores podem consultar históricos de alunos.");
        }
        if (usuarioRepository == null) {
            throw new Exception("Erro: Consulta de usuário indisponível.");
        }

        String matricula = validarCampoObrigatorio(matriculaAluno, "matrícula do aluno");
        Usuario usuario = usuarioRepository.buscarPorMatricula(matricula)
                .orElseThrow(() -> new Exception("Erro: Aluno não encontrado."));
        if (!(usuario instanceof Aluno) || usuario.getTipo() != TipoUsuario.ALUNO) {
            throw new Exception("Erro: Aluno não encontrado.");
        }

        String cursoCoordenador = ((Coordenador) solicitante).getCodigoCurso();
        String cursoAluno = ((Aluno) usuario).getCodigoCurso();
        if (cursoCoordenador == null || cursoAluno == null || !cursoCoordenador.equalsIgnoreCase(cursoAluno)) {
            throw new Exception("Erro: Acesso negado. O aluno pertence a outro curso.");
        }
        return filtrarDisciplinasConcluidas(repository.buscarPorAluno(matricula));
    }

    public List<Historico> listarReprovadosPorDisciplina(Usuario coordenador, String codigoDisciplina)
            throws Exception {
        if (coordenador == null || coordenador.getTipo() != TipoUsuario.COORDENADOR) {
            throw new Exception("Erro: Apenas coordenadores podem gerar este relatório.");
        }
        String codigo = validarCampoObrigatorio(codigoDisciplina, "código da disciplina");
        return repository.listarTodos().stream()
                .filter(this::periodoEncerrado)
                .filter(h -> h.getCodigoDisciplina().equalsIgnoreCase(codigo))
                .filter(h -> h.getSituacao() != null
                        && h.getSituacao().trim().toUpperCase().startsWith("REPROVADO"))
                .collect(Collectors.toList());
    }

    public void registrarHistorico(String matriculaAluno, String codigoDisciplina, double notaFinal, boolean aprovado) {

        final String matricula = validarCampoObrigatorio(matriculaAluno, "matrícula do aluno");
        final String disciplina = validarCampoObrigatorio(codigoDisciplina, "código da disciplina");

        List<Historico> historicos = repository.buscarPorAluno(matricula);

        Historico existente = historicos.stream().filter(h -> h.getCodigoDisciplina().equalsIgnoreCase(disciplina))
                .findFirst().orElse(null);

        if (existente != null) {
            existente.setNotaFinal(notaFinal);
            existente.setAprovado(aprovado);
            repository.atualizar(existente);
            return;
        }

        repository.salvar(new Historico(matricula, disciplina, notaFinal, aprovado));
    }

    public void registrarHistorico(Historico historico) {
        if (historico == null) {
            throw new IllegalArgumentException("Erro: histórico não pode ser nulo.");
        }
        repository.atualizar(historico);
    }

    @SuppressWarnings("checkstyle:ParameterNumber")
    public void registrarHistoricoCompleto(String matriculaAluno, String codigoPeriodo, String codigoDisciplina,
            String codigoTurma, Turma turma, Disciplina disciplina, Usuario professor, double notaFinal,
            double frequencia, String situacao) {
        String nomeDisciplina = disciplina == null ? codigoDisciplina : disciplina.getNome();
        String nomeProfessor = professor == null ? turma.getMatriculaProfessor() : professor.getNome();
        registrarHistorico(new Historico(matriculaAluno, codigoPeriodo, codigoDisciplina, nomeDisciplina, codigoTurma,
                turma.getMatriculaProfessor(), nomeProfessor, notaFinal, frequencia, situacao));
    }

    private String validarCampoObrigatorio(String valor, String campo) {

        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException("Erro: " + campo + " não pode ser vazio.");
        }

        return valor.trim();
    }

    private List<Historico> filtrarDisciplinasConcluidas(List<Historico> historicos) {
        return historicos.stream().filter(this::periodoEncerrado).collect(Collectors.toList());
    }

    private boolean periodoEncerrado(Historico historico) {
        if (periodoRepository == null) {
            return true;
        }
        if (historico == null || historico.getCodigoPeriodo() == null) {
            return false;
        }
        PeriodoLetivo periodo = periodoRepository.buscarPorCodigo(historico.getCodigoPeriodo());
        return periodo != null && periodo.isEncerrado();
    }
}
