package com.classroompb.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * RF16: Entidade de domínio que representa a solicitação de matrícula
 * de um aluno em uma turma.
 *
 * Uma solicitação é identificada pela combinação única
 * (matriculaAluno + codigoDisciplina + codigoPeriodo + codigoTurma).
 *
 * Status possíveis:
 *   - PENDENTE  : solicitação registrada, aguardando processamento
 *   - CONFIRMADA: matrícula efetivada (vaga reservada)
 *   - CANCELADA : solicitação cancelada pelo aluno
 */
public class MatriculaTurma implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Matrícula do aluno solicitante. */
    private String matriculaAluno;

    /** Código da disciplina da turma solicitada. */
    private String codigoDisciplina;

    /** Código do período letivo da turma solicitada. */
    private String codigoPeriodo;

    /** Código da turma solicitada. */
    private String codigoTurma;

    /** Status atual da solicitação. */
    private StatusMatricula status;

    /** Data e hora em que a solicitação foi registrada. */
    private LocalDateTime dataSolicitacao;

    /** Construtor padrão necessário para desserialização JSON. */
    public MatriculaTurma() {}

    /**
     * Construtor completo para criação de uma nova solicitação.
     * O status inicial é sempre PENDENTE.
     *
     * @param matriculaAluno    matrícula do aluno
     * @param codigoDisciplina  código da disciplina
     * @param codigoPeriodo     código do período letivo
     * @param codigoTurma       código da turma
     */
    public MatriculaTurma(
            String matriculaAluno,
            String codigoDisciplina,
            String codigoPeriodo,
            String codigoTurma
    ) {
        this.matriculaAluno   = matriculaAluno;
        this.codigoDisciplina = codigoDisciplina;
        this.codigoPeriodo    = codigoPeriodo;
        this.codigoTurma      = codigoTurma;
        this.status           = StatusMatricula.PENDENTE;
        this.dataSolicitacao  = LocalDateTime.now();
    }

    // -------------------------------------------------------------------------
    // Getters e Setters
    // -------------------------------------------------------------------------

    public String getMatriculaAluno() { return matriculaAluno; }
    public void setMatriculaAluno(String matriculaAluno) { this.matriculaAluno = matriculaAluno; }

    public String getCodigoDisciplina() { return codigoDisciplina; }
    public void setCodigoDisciplina(String codigoDisciplina) { this.codigoDisciplina = codigoDisciplina; }

    public String getCodigoPeriodo() { return codigoPeriodo; }
    public void setCodigoPeriodo(String codigoPeriodo) { this.codigoPeriodo = codigoPeriodo; }

    public String getCodigoTurma() { return codigoTurma; }
    public void setCodigoTurma(String codigoTurma) { this.codigoTurma = codigoTurma; }

    public StatusMatricula getStatus() { return status; }
    public void setStatus(StatusMatricula status) { this.status = status; }

    public LocalDateTime getDataSolicitacao() { return dataSolicitacao; }
    public void setDataSolicitacao(LocalDateTime dataSolicitacao) { this.dataSolicitacao = dataSolicitacao; }

    // -------------------------------------------------------------------------
    // Identificador composto
    // -------------------------------------------------------------------------

    /**
     * Retorna a chave única composta que identifica esta solicitação.
     * Formato: "{matriculaAluno}_{codigoDisciplina}_{codigoPeriodo}_{codigoTurma}"
     */
    @JsonIgnore
    public String getChaveUnica() {
        return matriculaAluno + "_" + codigoDisciplina + "_" + codigoPeriodo + "_" + codigoTurma;
    }

    @Override
    public String toString() {
        return String.format(
                "[MATRICULA] Aluno: %s | Turma: %s | Disciplina: %s | Período: %s | Status: %s | Solicitado em: %s",
                matriculaAluno, codigoTurma, codigoDisciplina, codigoPeriodo, status,
                dataSolicitacao != null ? dataSolicitacao.toString() : "-"
        );
    }
}
