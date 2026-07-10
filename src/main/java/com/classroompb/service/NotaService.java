package com.classroompb.service;

import com.classroompb.model.MatriculaTurma;
import com.classroompb.model.Nota;
import com.classroompb.model.StatusMatricula;
import com.classroompb.model.TipoUsuario;
import com.classroompb.model.Turma;
import com.classroompb.model.Usuario;
import com.classroompb.repository.HistoricoRepository;
import com.classroompb.repository.MatriculaTurmaRepository;
import com.classroompb.repository.NotaRepository;
import com.classroompb.repository.TurmaRepository;

public class NotaService {

    private final NotaRepository notaRepository;
    private final TurmaRepository turmaRepository;
    private final MatriculaTurmaRepository matriculaRepository;
    private final HistoricoService historicoService;

    public NotaService(NotaRepository notaRepository, TurmaRepository turmaRepository,
            MatriculaTurmaRepository matriculaRepository) {

        this(notaRepository, turmaRepository, matriculaRepository, null);
    }

    public NotaService(NotaRepository notaRepository, TurmaRepository turmaRepository,
            MatriculaTurmaRepository matriculaRepository, HistoricoRepository historicoRepository) {

        this.notaRepository = notaRepository;
        this.turmaRepository = turmaRepository;
        this.matriculaRepository = matriculaRepository;
        this.historicoService = new HistoricoService(
                historicoRepository != null ? historicoRepository : new HistoricoRepository());
    }

    public void lancarNotas(Usuario professor, String matriculaAluno, String codigoDisciplina, String codigoPeriodo,
            String codigoTurma, Double etapa1, Double etapa2) throws Exception {

        validarProfessor(professor);

        matriculaAluno = validarCampoObrigatorio(matriculaAluno, "matrícula do aluno");
        codigoDisciplina = validarCampoObrigatorio(codigoDisciplina, "código da disciplina");
        codigoPeriodo = validarCampoObrigatorio(codigoPeriodo, "código do período");
        codigoTurma = validarCampoObrigatorio(codigoTurma, "código da turma");

        validarNota(etapa1, "Etapa 1");
        validarNota(etapa2, "Etapa 2");

        Turma turma = turmaRepository.buscarPorChaveUnica(codigoDisciplina, codigoPeriodo, codigoTurma);

        if (turma == null) {
            throw new Exception("Erro: Turma não encontrada.");
        }

        if (!turma.getMatriculaProfessor().equalsIgnoreCase(professor.getMatricula())) {

            throw new Exception("Erro: Apenas o professor responsável pode lançar notas.");
        }

        MatriculaTurma matricula = matriculaRepository.buscarPorChaveUnica(matriculaAluno, codigoDisciplina,
                codigoPeriodo, codigoTurma);

        if (matricula == null || matricula.getStatus() != StatusMatricula.CONFIRMADA) {

            throw new Exception("Erro: O aluno não possui matrícula confirmada.");
        }

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

        if (nota.getEtapa1() != null && nota.getEtapa2() != null) {
            double media = (nota.getEtapa1() + nota.getEtapa2()) / 2.0;
            String situacao = media >= 7.0 ? "APROVADO" : media >= 4.0 ? "RECUPERACAO" : "REPROVADO POR NOTA";
            historicoService.registrarHistorico(matriculaAluno, codigoDisciplina, media,
                    "APROVADO".equalsIgnoreCase(situacao));
        }
    }

    public void alterarNotas(Usuario professor, String matriculaAluno, String codigoDisciplina, String codigoPeriodo,
            String codigoTurma, Double etapa1, Double etapa2) throws Exception {

        validarProfessor(professor);

        matriculaAluno = validarCampoObrigatorio(matriculaAluno, "matrícula do aluno");
        codigoDisciplina = validarCampoObrigatorio(codigoDisciplina, "código da disciplina");
        codigoPeriodo = validarCampoObrigatorio(codigoPeriodo, "código do período");
        codigoTurma = validarCampoObrigatorio(codigoTurma, "código da turma");

        validarNota(etapa1, "Etapa 1");
        validarNota(etapa2, "Etapa 2");

        Turma turma = turmaRepository.buscarPorChaveUnica(codigoDisciplina, codigoPeriodo, codigoTurma);

        if (turma == null) {
            throw new Exception("Erro: Turma não encontrada.");
        }

        if (!turma.getMatriculaProfessor().equalsIgnoreCase(professor.getMatricula())) {
            throw new Exception("Erro: Apenas o professor responsável pode alterar notas.");
        }

        MatriculaTurma matricula = matriculaRepository.buscarPorChaveUnica(matriculaAluno, codigoDisciplina,
                codigoPeriodo, codigoTurma);

        if (matricula == null || matricula.getStatus() != StatusMatricula.CONFIRMADA) {
            throw new Exception("Erro: O aluno não possui matrícula confirmada.");
        }

        Nota nota = notaRepository.buscarPorChaveUnica(matriculaAluno, codigoDisciplina, codigoPeriodo, codigoTurma);

        if (nota == null) {
            throw new Exception("Erro: Nenhuma nota lançada para este aluno.");
        }

        nota.setEtapa1(etapa1);
        nota.setEtapa2(etapa2);
        notaRepository.atualizar(nota);

        double media = (nota.getEtapa1() + nota.getEtapa2()) / 2.0;
        String situacao = media >= 7.0 ? "APROVADO" : media >= 4.0 ? "RECUPERACAO" : "REPROVADO POR NOTA";
        historicoService.registrarHistorico(matriculaAluno, codigoDisciplina, media,
                "APROVADO".equalsIgnoreCase(situacao));
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
}
