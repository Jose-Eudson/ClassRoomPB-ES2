package com.classroompb.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * RF10/RF11: Entidade de domínio que representa uma turma ofertada para uma disciplina em um período letivo.
 *
 * Uma turma é identificada de forma única pela combinação (codigoDisciplina + codigoPeriodo + codigo), permitindo que a
 * mesma disciplina tenha múltiplas turmas no mesmo período.
 *
 * RF11: Cada turma possui professor responsável, limite de vagas, horário e sala.
 */
public class Turma implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Identificador único da turma (ex: "T01"). */
    private String codigo;

    /** Código da disciplina associada (FK lógica para Disciplina). */
    private String codigoDisciplina;

    /** Código do período letivo associado (FK lógica para PeriodoLetivo). */
    private String codigoPeriodo;

    /** Número máximo de alunos que podem se matricular. RF11. */
    private int vagas;

    /** Horário das aulas (ex: "Seg/Qua 10h-12h"). RF11. */
    private String horario;

    /** Sala onde as aulas serão realizadas (ex: "Bloco A - 101"). RF11. */
    private String sala;

    /** Matrícula do professor responsável pela turma (pode ser nulo). RF11. */
    private String matriculaProfessor;

    /** Situação acadêmica da oferta. Ausente em JSONs antigos, permanece ABERTA. */
    private SituacaoTurma situacao = SituacaoTurma.ABERTA;

    /** Construtor padrão necessário para desserialização JSON. */
    public Turma() {
    }

    /**
     * Construtor completo para criação de turmas (RF11).
     *
     * @param codigo
     *            identificador da turma (ex: "T01")
     * @param codigoDisciplina
     *            código da disciplina ofertada
     * @param codigoPeriodo
     *            código do período letivo (ex: "2026.1")
     * @param vagas
     *            número máximo de vagas
     * @param horario
     *            descrição do horário das aulas
     * @param sala
     *            sala onde as aulas serão realizadas
     * @param matriculaProfessor
     *            matrícula do professor (nullable)
     */
    public Turma(String codigo, String codigoDisciplina, String codigoPeriodo, int vagas, String horario, String sala,
            String matriculaProfessor) {
        this.codigo = codigo;
        this.codigoDisciplina = codigoDisciplina;
        this.codigoPeriodo = codigoPeriodo;
        this.vagas = vagas;
        this.horario = horario;
        this.sala = sala;
        this.matriculaProfessor = matriculaProfessor;
        this.situacao = SituacaoTurma.ABERTA;
    }

    // -------------------------------------------------------------------------
    // Getters e Setters
    // -------------------------------------------------------------------------

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getCodigoDisciplina() {
        return codigoDisciplina;
    }

    public void setCodigoDisciplina(String codigoDisciplina) {
        this.codigoDisciplina = codigoDisciplina;
    }

    public String getCodigoPeriodo() {
        return codigoPeriodo;
    }

    public void setCodigoPeriodo(String codigoPeriodo) {
        this.codigoPeriodo = codigoPeriodo;
    }

    public int getVagas() {
        return vagas;
    }

    public void setVagas(int vagas) {
        this.vagas = vagas;
    }

    public String getHorario() {
        return horario;
    }

    public void setHorario(String horario) {
        this.horario = horario;
    }

    public String getSala() {
        return sala;
    }

    public void setSala(String sala) {
        this.sala = sala;
    }

    public String getMatriculaProfessor() {
        return matriculaProfessor;
    }

    public void setMatriculaProfessor(String matriculaProfessor) {
        this.matriculaProfessor = matriculaProfessor;
    }

    public SituacaoTurma getSituacao() {
        return situacao == null ? SituacaoTurma.ABERTA : situacao;
    }

    public void setSituacao(SituacaoTurma situacao) {
        this.situacao = situacao == null ? SituacaoTurma.ABERTA : situacao;
    }

    // -------------------------------------------------------------------------
    // Identificador composto: disciplina + período + código
    // -------------------------------------------------------------------------

    /**
     * Retorna a chave única composta que identifica esta turma no sistema. Formato:
     * "{codigoDisciplina}_{codigoPeriodo}_{codigo}" Exemplo: "MAT001_2026.1_T01"
     */
    @JsonIgnore
    public String getChaveUnica() {
        return codigoDisciplina + "_" + codigoPeriodo + "_" + codigo;
    }

    @Override
    public String toString() {
        String prof = matriculaProfessor == null || matriculaProfessor.trim().isEmpty() ? "sem professor"
                : matriculaProfessor;
        String salaStr = sala == null || sala.trim().isEmpty() ? "sem sala" : sala;
        return String.format("[TURMA] %s | Disciplina: %s | Período: %s | %d vagas | %s | Sala: %s | Prof: %s", codigo,
                codigoDisciplina, codigoPeriodo, vagas, horario, salaStr, prof);
    }
}
