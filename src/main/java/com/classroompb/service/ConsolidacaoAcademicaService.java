package com.classroompb.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.classroompb.model.Avaliacao;
import com.classroompb.model.Diario;
import com.classroompb.model.Disciplina;
import com.classroompb.model.Historico;
import com.classroompb.model.MatriculaTurma;
import com.classroompb.model.Nota;
import com.classroompb.model.RegistroFrequencia;
import com.classroompb.model.SituacaoDiario;
import com.classroompb.model.StatusFrequencia;
import com.classroompb.model.StatusMatricula;
import com.classroompb.model.Turma;
import com.classroompb.model.Usuario;
import com.classroompb.repository.AvaliacaoRepository;
import com.classroompb.repository.DiarioRepository;
import com.classroompb.repository.DisciplinaRepository;
import com.classroompb.repository.FrequenciaRepository;
import com.classroompb.repository.MatriculaTurmaRepository;
import com.classroompb.repository.NotaRepository;
import com.classroompb.repository.TurmaRepository;
import com.classroompb.repository.UsuarioRepository;

public class ConsolidacaoAcademicaService {
    private final TurmaRepository turmaRepository;
    private final DiarioRepository diarioRepository;
    private final MatriculaTurmaRepository matriculaRepository;
    private final AvaliacaoRepository avaliacaoRepository;
    private final NotaRepository notaRepository;
    private final FrequenciaRepository frequenciaRepository;
    private final DisciplinaRepository disciplinaRepository;
    private final UsuarioRepository usuarioRepository;
    private final HistoricoService historicoService;

    @SuppressWarnings("checkstyle:ParameterNumber")
    public ConsolidacaoAcademicaService(TurmaRepository turmaRepository, DiarioRepository diarioRepository,
            MatriculaTurmaRepository matriculaRepository, AvaliacaoRepository avaliacaoRepository,
            NotaRepository notaRepository, FrequenciaRepository frequenciaRepository,
            DisciplinaRepository disciplinaRepository, UsuarioRepository usuarioRepository,
            HistoricoService historicoService) {
        this.turmaRepository = turmaRepository;
        this.diarioRepository = diarioRepository;
        this.matriculaRepository = matriculaRepository;
        this.avaliacaoRepository = avaliacaoRepository;
        this.notaRepository = notaRepository;
        this.frequenciaRepository = frequenciaRepository;
        this.disciplinaRepository = disciplinaRepository;
        this.usuarioRepository = usuarioRepository;
        this.historicoService = historicoService;
    }

    public void consolidarPeriodo(String codigoPeriodo) throws Exception {
        for (Turma turma : turmaRepository.listarPorPeriodo(codigoPeriodo)) {
            consolidarTurma(turma);
        }
    }

    public void consolidarTurma(Turma turma) throws Exception {
        if (turma == null) {
            throw new Exception("Erro: turma obrigatória para consolidação.");
        }
        List<Diario> diarios = diarioRepository.buscarPorTurma(turma.getCodigoDisciplina(), turma.getCodigoPeriodo(),
                turma.getCodigo());
        if (diarios.isEmpty()) {
            throw new Exception("Erro: RN15 - turma sem diário não pode ser encerrada.");
        }
        if (diarios.stream().anyMatch(d -> d.getSituacao() != SituacaoDiario.ENCERRADO)) {
            throw new Exception("Erro: todos os diários da turma devem estar fechados.");
        }
        if (diarios.stream().anyMatch(d -> d.getCargaHoraria() <= 0)) {
            throw new Exception("Erro: diário com carga horária inválida impede consolidação.");
        }
        List<MatriculaTurma> matriculas = matriculaRepository
                .listarPorTurma(turma.getCodigoDisciplina(), turma.getCodigoPeriodo(), turma.getCodigo()).stream()
                .filter(m -> m.getStatus() == StatusMatricula.CONFIRMADA).toList();

        List<ResultadoConsolidado> resultados = new ArrayList<>();
        List<String> alunosEmRecuperacao = new ArrayList<>();
        for (MatriculaTurma matricula : matriculas) {
            ResultadoConsolidado resultado = calcularResultado(turma, diarios, matricula.getMatriculaAluno());
            if ("RECUPERACAO".equals(resultado.historico.getSituacao())) {
                alunosEmRecuperacao.add(matricula.getMatriculaAluno());
            } else {
                resultados.add(resultado);
            }
        }
        if (!alunosEmRecuperacao.isEmpty()) {
            throw new Exception("Erro: turma possui alunos com resultado pendente de recuperação: "
                    + String.join(", ", alunosEmRecuperacao) + ".");
        }
        for (ResultadoConsolidado resultado : resultados) {
            historicoService.registrarHistorico(resultado.historico);
        }
    }

    private ResultadoConsolidado calcularResultado(Turma turma, List<Diario> diarios, String matriculaAluno)
            throws Exception {
        double somaMediasPonderadas = 0.0;
        double somaCargasHorarias = 0.0;
        long presencas = 0;
        long totalFrequencias = 0;
        Set<String> professores = new HashSet<>();
        for (Diario diario : diarios) {
            double mediaDiario = calcularMediaDiario(matriculaAluno, diario.getCodigo());
            somaMediasPonderadas += mediaDiario * diario.getCargaHoraria();
            somaCargasHorarias += diario.getCargaHoraria();
            List<RegistroFrequencia> frequencias = frequenciaRepository.listarPorAlunoEDiario(matriculaAluno,
                    diario.getCodigo());
            presencas += frequencias.stream().filter(f -> f.getStatus() == StatusFrequencia.PRESENTE).count();
            totalFrequencias += frequencias.size();
            professores.add(diario.getMatriculaProfessor());
        }
        if (somaCargasHorarias <= 0.0) {
            throw new Exception("Erro: soma das cargas horárias deve ser maior que zero.");
        }
        // Média final = soma(média do diário x carga horária) / soma das cargas horárias.
        double mediaFinal = somaMediasPonderadas / somaCargasHorarias;
        double frequencia = totalFrequencias == 0 ? 0.0 : presencas * 100.0 / totalFrequencias;
        String situacao = NotaService.calcularSituacao(mediaFinal, frequencia);
        Disciplina disciplina = disciplinaRepository.buscarPorCodigo(turma.getCodigoDisciplina());
        String matriculaProfessor = professores.size() == 1 ? professores.iterator().next() : null;
        String nomeProfessor = professores.size() == 1 ? nomeProfessor(matriculaProfessor) : "Multiplos professores";
        String nomeDisciplina = disciplina == null ? turma.getCodigoDisciplina() : disciplina.getNome();
        Historico historico = new Historico(matriculaAluno, turma.getCodigoPeriodo(), turma.getCodigoDisciplina(),
                nomeDisciplina, turma.getCodigo(), matriculaProfessor, nomeProfessor, mediaFinal, frequencia, situacao);
        return new ResultadoConsolidado(historico);
    }

    private double calcularMediaDiario(String matriculaAluno, String codigoDiario) throws Exception {
        List<Avaliacao> avaliacoes = avaliacaoRepository.listarPorDiario(codigoDiario);
        double soma = 0.0;
        double pesos = 0.0;
        for (Avaliacao avaliacao : avaliacoes) {
            Nota nota = notaRepository.buscarPorAlunoEAvaliacao(matriculaAluno, avaliacao.getCodigo());
            if (nota == null || nota.getValor() == null) {
                throw new Exception("Erro: nota pendente impede consolidação.");
            }
            soma += (nota.getValor() / avaliacao.getNotaMaxima()) * 10.0 * avaliacao.getPeso();
            pesos += avaliacao.getPeso();
        }
        if (pesos == 0.0) {
            throw new Exception("Erro: diário sem avaliações não pode ser consolidado.");
        }
        return soma / pesos;
    }

    private String nomeProfessor(String matricula) {
        Usuario professor = usuarioRepository.buscarPorMatricula(matricula).orElse(null);
        return professor == null ? matricula : professor.getNome();
    }

    TurmaRepository getTurmaRepository() {
        return turmaRepository;
    }

    private static final class ResultadoConsolidado {
        private final Historico historico;

        private ResultadoConsolidado(Historico historico) {
            this.historico = historico;
        }
    }
}
