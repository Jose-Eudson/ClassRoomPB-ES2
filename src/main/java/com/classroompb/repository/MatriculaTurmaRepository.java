package com.classroompb.repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.classroompb.model.MatriculaTurma;
import com.classroompb.model.StatusMatricula;
import com.classroompb.util.JsonUtil;

/**
 * RF16: Repositório responsável pela persistência das solicitações de matrícula em turmas, em arquivo JSON.
 */
public class MatriculaTurmaRepository {

    private static final String CAMINHO_PADRAO = "matriculas_turma.json";

    private final String caminhoArquivo;
    private List<MatriculaTurma> matriculas;

    public MatriculaTurmaRepository() {
        this(CAMINHO_PADRAO);
    }

    public MatriculaTurmaRepository(String caminhoArquivo) {
        this.caminhoArquivo = caminhoArquivo;
        carregarDados();
    }

    private void carregarDados() {
        try {
            this.matriculas = JsonUtil.carregarLista(caminhoArquivo, MatriculaTurma.class);
        } catch (IOException e) {
            System.err.println("Erro ao carregar matrículas: " + e.getMessage());
            this.matriculas = new ArrayList<>();
        }
    }

    private void salvarDados() {
        try {
            JsonUtil.salvar(caminhoArquivo, matriculas);
        } catch (IOException e) {
            System.err.println("Erro ao salvar matrículas: " + e.getMessage());
        }
    }

    /** Persiste uma nova solicitação de matrícula. */
    public void salvar(MatriculaTurma matricula) {
        matriculas.add(matricula);
        salvarDados();
    }

    /** Retorna cópia defensiva de todas as solicitações. */
    public List<MatriculaTurma> listarTodas() {
        return new ArrayList<>(matriculas);
    }

    /**
     * Verifica se já existe uma solicitação ativa (PENDENTE ou CONFIRMADA) do mesmo aluno para a mesma turma.
     */
    public boolean existeSolicitacaoAtiva(String matriculaAluno, String codigoDisciplina, String codigoPeriodo,
            String codigoTurma) {
        String chave = matriculaAluno + "_" + codigoDisciplina + "_" + codigoPeriodo + "_" + codigoTurma;
        return matriculas.stream()
                .anyMatch(m -> m.getChaveUnica().equalsIgnoreCase(chave) && m.getStatus() != StatusMatricula.CANCELADA);
    }

    /**
     * Retorna todas as solicitações de um aluno específico.
     *
     * @param matriculaAluno
     *            matrícula do aluno
     *
     * @return lista de solicitações (pode ser vazia)
     */
    public List<MatriculaTurma> listarPorAluno(String matriculaAluno) {
        return matriculas.stream().filter(m -> m.getMatriculaAluno().equalsIgnoreCase(matriculaAluno))
                .collect(Collectors.toList());
    }

    /**
     * Retorna todas as solicitações de um aluno com um status específico.
     *
     * @param matriculaAluno
     *            matrícula do aluno
     * @param status
     *            status desejado
     *
     * @return lista filtrada
     */
    public List<MatriculaTurma> listarPorAlunoEStatus(String matriculaAluno, StatusMatricula status) {
        return matriculas.stream()
                .filter(m -> m.getMatriculaAluno().equalsIgnoreCase(matriculaAluno) && m.getStatus() == status)
                .collect(Collectors.toList());
    }

    /**
     * Retorna todas as solicitações para uma turma específica.
     *
     * @param codigoDisciplina
     *            código da disciplina
     * @param codigoPeriodo
     *            código do período letivo
     * @param codigoTurma
     *            código da turma
     *
     * @return lista de solicitações para a turma
     */
    public List<MatriculaTurma> listarPorTurma(String codigoDisciplina, String codigoPeriodo, String codigoTurma) {
        return matriculas.stream()
                .filter(m -> m.getCodigoDisciplina().equalsIgnoreCase(codigoDisciplina)
                        && m.getCodigoPeriodo().equalsIgnoreCase(codigoPeriodo)
                        && m.getCodigoTurma().equalsIgnoreCase(codigoTurma))
                .collect(Collectors.toList());
    }

    /**
     * Conta quantas solicitações com status CONFIRMADA existem para uma turma. Esse número representa as vagas já
     * ocupadas.
     *
     * @param codigoDisciplina
     *            código da disciplina
     * @param codigoPeriodo
     *            código do período letivo
     * @param codigoTurma
     *            código da turma
     *
     * @return número de matrículas confirmadas
     */
    public long contarOcupadasPorTurma(String codigoDisciplina, String codigoPeriodo, String codigoTurma) {
        return matriculas.stream()
                .filter(m -> m.getCodigoDisciplina().equalsIgnoreCase(codigoDisciplina)
                        && m.getCodigoPeriodo().equalsIgnoreCase(codigoPeriodo)
                        && m.getCodigoTurma().equalsIgnoreCase(codigoTurma)
                        && (m.getStatus() == StatusMatricula.CONFIRMADA || m.getStatus() == StatusMatricula.PENDENTE))
                .count();
    }

    /**
     * Busca uma solicitação pela chave única composta.
     *
     * @param matriculaAluno
     *            matrícula do aluno
     * @param codigoDisciplina
     *            código da disciplina
     * @param codigoPeriodo
     *            código do período letivo
     * @param codigoTurma
     *            código da turma
     *
     * @return a solicitação encontrada, ou null
     */
    public MatriculaTurma buscarPorChaveUnica(String matriculaAluno, String codigoDisciplina, String codigoPeriodo,
            String codigoTurma) {
        String chave = matriculaAluno + "_" + codigoDisciplina + "_" + codigoPeriodo + "_" + codigoTurma;
        return matriculas.stream().filter(m -> m.getChaveUnica().equalsIgnoreCase(chave)).findFirst().orElse(null);
    }

    /**
     * RF23: Retorna os alunos em lista de espera de uma turma, ordenados pela data da solicitação (mais antigo
     * primeiro), respeitando a ordem de chegada.
     *
     * @param codigoDisciplina
     *            código da disciplina
     * @param codigoPeriodo
     *            código do período letivo
     * @param codigoTurma
     *            código da turma
     *
     * @return lista de solicitações com status LISTA_ESPERA, ordenadas por dataSolicitacao ascendente
     */
    public List<MatriculaTurma> listarListaEsperaPorTurmaOrdenada(String codigoDisciplina, String codigoPeriodo,
            String codigoTurma) {
        return matriculas.stream().filter(m -> m.getCodigoDisciplina().equalsIgnoreCase(codigoDisciplina)
                && m.getCodigoPeriodo().equalsIgnoreCase(codigoPeriodo)
                && m.getCodigoTurma().equalsIgnoreCase(codigoTurma) && m.getStatus() == StatusMatricula.LISTA_ESPERA)
                .sorted(Comparator.comparing(MatriculaTurma::getDataSolicitacao,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
    }

    /**
     * Atualiza o status de uma solicitação existente.
     *
     * @param atualizada
     *            solicitação com os dados novos
     *
     * @throws IllegalArgumentException
     *             se não for encontrada
     */
    public void atualizar(MatriculaTurma atualizada) {
        for (int i = 0; i < matriculas.size(); i++) {
            if (matriculas.get(i).getChaveUnica().equalsIgnoreCase(atualizada.getChaveUnica())) {
                matriculas.set(i, atualizada);
                salvarDados();
                return;
            }
        }
        throw new IllegalArgumentException(
                "Solicitação de matrícula com chave " + atualizada.getChaveUnica() + " não encontrada.");
    }
}
