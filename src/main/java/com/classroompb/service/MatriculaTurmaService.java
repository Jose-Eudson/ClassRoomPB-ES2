package com.classroompb.service;

import java.util.List;

import com.classroompb.model.MatriculaTurma;
import com.classroompb.model.PeriodoLetivo;
import com.classroompb.model.StatusMatricula;
import com.classroompb.model.TipoUsuario;
import com.classroompb.model.Turma;
import com.classroompb.model.Usuario;
import com.classroompb.repository.MatriculaTurmaRepository;
import com.classroompb.repository.PeriodoLetivoRepository;
import com.classroompb.repository.TurmaRepository;

/**
 * RF16: Serviço responsável pela solicitação e gestão de matrículas de alunos
 * em turmas.
 *
 * Regras de negócio aplicadas:
 *   - Apenas alunos podem solicitar matrícula.
 *   - A turma informada deve existir.
 *   - O período letivo da turma deve estar ativo.
 *   - Não é permitido ao aluno solicitar matrícula na mesma turma mais de uma vez
 *     (solicitações PENDENTE ou CONFIRMADA bloqueiam nova solicitação).
 *   - Não é possível solicitar matrícula se não houver vagas disponíveis
 *     (vagas - confirmadas <= 0).
 *   - O aluno só pode cancelar suas próprias solicitações.
 *   - Apenas solicitações com status PENDENTE podem ser canceladas pelo aluno.
 */
public class MatriculaTurmaService {

    private final MatriculaTurmaRepository matriculaRepository;
    private final TurmaRepository turmaRepository;
    private final PeriodoLetivoRepository periodoRepository;

    public MatriculaTurmaService(
            MatriculaTurmaRepository matriculaRepository,
            TurmaRepository turmaRepository,
            PeriodoLetivoRepository periodoRepository
    ) {
        this.matriculaRepository = matriculaRepository;
        this.turmaRepository     = turmaRepository;
        this.periodoRepository   = periodoRepository;
    }

    // =========================================================================
    // RF16 — Solicitar matrícula
    // =========================================================================

    /**
     * Registra uma solicitação de matrícula de um aluno em uma turma.
     *
     * @param aluno            usuário que está realizando a operação (deve ser ALUNO)
     * @param codigoDisciplina código da disciplina da turma
     * @param codigoPeriodo    código do período letivo
     * @param codigoTurma      código da turma
     * @throws Exception se qualquer regra de negócio for violada
     */
    public void solicitarMatricula(
            Usuario aluno,
            String codigoDisciplina,
            String codigoPeriodo,
            String codigoTurma
    ) throws Exception {

        // Permissão: apenas alunos
        if (aluno == null || aluno.getTipo() != TipoUsuario.ALUNO) {
            throw new Exception("Erro: Apenas alunos podem solicitar matrícula em turmas.");
        }

        // Validação: campos obrigatórios
        if (codigoDisciplina == null || codigoDisciplina.trim().isEmpty()) {
            throw new Exception("Erro: Código da disciplina não pode ser vazio.");
        }
        if (codigoPeriodo == null || codigoPeriodo.trim().isEmpty()) {
            throw new Exception("Erro: Código do período letivo não pode ser vazio.");
        }
        if (codigoTurma == null || codigoTurma.trim().isEmpty()) {
            throw new Exception("Erro: Código da turma não pode ser vazio.");
        }

        String discNorm   = codigoDisciplina.trim();
        String periodoNorm = codigoPeriodo.trim();
        String turmaNorm  = codigoTurma.trim();

        // Regra: turma deve existir
        Turma turma = turmaRepository.buscarPorChaveUnica(discNorm, periodoNorm, turmaNorm);
        if (turma == null) {
            throw new Exception(
                    "Erro: Turma '" + turmaNorm
                    + "' da disciplina '" + discNorm
                    + "' no período '" + periodoNorm + "' não encontrada.");
        }

        // Regra: período letivo deve estar ativo
        PeriodoLetivo periodo = periodoRepository.buscarPorCodigo(periodoNorm);
        if (periodo == null || !periodo.isAtivo()) {
            throw new Exception(
                    "Erro: Não é possível solicitar matrícula em turmas de um período letivo inativo.");
        }

        // Regra: aluno não pode se matricular na mesma turma duas vezes
        if (matriculaRepository.existeSolicitacaoAtiva(
                aluno.getMatricula(), discNorm, periodoNorm, turmaNorm)) {
            throw new Exception(
                    "Erro: Você já possui uma solicitação ativa para a turma '"
                    + turmaNorm + "' da disciplina '" + discNorm + "'.");
        }

        // Regra: verificar disponibilidade de vagas
        long vagasOcupadas = matriculaRepository.contarConfirmadasPorTurma(discNorm, periodoNorm, turmaNorm);
        if (vagasOcupadas >= turma.getVagas()) {
            throw new Exception(
                    "Erro: Não há vagas disponíveis na turma '" + turmaNorm
                    + "' da disciplina '" + discNorm + "'. Vagas esgotadas ("
                    + turma.getVagas() + "/" + turma.getVagas() + ").");
        }

        MatriculaTurma solicitacao = new MatriculaTurma(
                aluno.getMatricula(),
                discNorm,
                periodoNorm,
                turmaNorm
        );

        matriculaRepository.salvar(solicitacao);
    }

    // =========================================================================
    // Cancelar solicitação
    // =========================================================================

    /**
     * Cancela uma solicitação de matrícula PENDENTE do aluno.
     *
     * @param aluno            usuário que está realizando a operação (deve ser ALUNO)
     * @param codigoDisciplina código da disciplina da turma
     * @param codigoPeriodo    código do período letivo
     * @param codigoTurma      código da turma
     * @throws Exception se a solicitação não existir, não pertencer ao aluno
     *                   ou não estiver com status PENDENTE
     */
    public void cancelarSolicitacao(
            Usuario aluno,
            String codigoDisciplina,
            String codigoPeriodo,
            String codigoTurma
    ) throws Exception {

        if (aluno == null || aluno.getTipo() != TipoUsuario.ALUNO) {
            throw new Exception("Erro: Apenas alunos podem cancelar suas solicitações de matrícula.");
        }

        String discNorm    = codigoDisciplina  != null ? codigoDisciplina.trim()  : "";
        String periodoNorm = codigoPeriodo     != null ? codigoPeriodo.trim()     : "";
        String turmaNorm   = codigoTurma       != null ? codigoTurma.trim()       : "";

        MatriculaTurma solicitacao = matriculaRepository.buscarPorChaveUnica(
                aluno.getMatricula(), discNorm, periodoNorm, turmaNorm);

        if (solicitacao == null) {
            throw new Exception(
                    "Erro: Nenhuma solicitação encontrada para a turma '" + turmaNorm
                    + "' da disciplina '" + discNorm + "'.");
        }

        if (solicitacao.getStatus() != StatusMatricula.PENDENTE) {
            throw new Exception(
                    "Erro: Apenas solicitações com status PENDENTE podem ser canceladas. "
                    + "Status atual: " + solicitacao.getStatus() + ".");
        }

        solicitacao.setStatus(StatusMatricula.CANCELADA);
        matriculaRepository.atualizar(solicitacao);
    }

    // =========================================================================
    // Consultas
    // =========================================================================

    /**
     * Lista todas as solicitações de matrícula de um aluno.
     *
     * @param aluno usuário autenticado (deve ser ALUNO)
     * @return lista de solicitações (pode ser vazia)
     * @throws Exception se o usuário não for aluno
     */
    public List<MatriculaTurma> listarMinhasSolicitacoes(Usuario aluno) throws Exception {
        if (aluno == null || aluno.getTipo() != TipoUsuario.ALUNO) {
            throw new Exception("Erro: Apenas alunos podem consultar suas solicitações de matrícula.");
        }
        return matriculaRepository.listarPorAluno(aluno.getMatricula());
    }

    /**
     * Retorna o número de vagas disponíveis (não confirmadas) em uma turma.
     *
     * @param turma a turma a ser consultada
     * @return vagas disponíveis (>= 0)
     */
    public long vagasDisponiveis(Turma turma) {
        if (turma == null) return 0;
        long ocupadas = matriculaRepository.contarConfirmadasPorTurma(
                turma.getCodigoDisciplina(),
                turma.getCodigoPeriodo(),
                turma.getCodigo()
        );
        return Math.max(0, turma.getVagas() - ocupadas);
    }
}
