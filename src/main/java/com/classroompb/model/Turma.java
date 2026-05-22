package com.classroompb.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * RF10: Entidade de domínio que representa uma turma ofertada
 * para uma disciplina em um período letivo.
 *
 * Uma turma é identificada de forma única pela combinação
 * (codigoDisciplina + codigoPeriodo + codigo), permitindo que
 * a mesma disciplina tenha múltiplas turmas no mesmo período.
 */
public class Turma implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Identificador único da turma (ex: "T01"). */
    private String codigo;

    /** Código da disciplina associada (FK lógica para Disciplina). */
    private String codigoDisciplina;

    /** Código do período letivo associado (FK lógica para PeriodoLetivo). */
    private String codigoPeriodo;

    /** Número máximo de alunos que podem se matricular. */
    private int vagas;

    /** Horário das aulas (ex: "Seg/Qua 10h-12h"). */
    private String horario;

    /** Matrícula do professor responsável pela turma (pode ser nulo). */
    private String matriculaProfessor;

    /** Construtor padrão necessário para desserialização JSON. */
    public Turma() {}

    /**
     * Construtor completo para criação de turmas.
     *
     * @param codigo             identificador da turma (ex: "T01")
     * @param codigoDisciplina   código da disciplina ofertada
     * @param codigoPeriodo      código do período letivo (ex: "2026.1")
     * @param vagas              número máximo de vagas
     * @param horario            descrição do horário das aulas
     * @param matriculaProfessor matrícula do professor (nullable)
     */
    public Turma(
            String codigo,
            String codigoDisciplina,
            String codigoPeriodo,
            int vagas,
            String horario,
            String matriculaProfessor
    ) {
        this.codigo = codigo;
        this.codigoDisciplina = codigoDisciplina;
        this.codigoPeriodo = codigoPeriodo;
        this.vagas = vagas;
        this.horario = horario;
        this.matriculaProfessor = matriculaProfessor;
    }

    // -------------------------------------------------------------------------
    // Getters e Setters
    // -------------------------------------------------------------------------

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getCodigoDisciplina() { return codigoDisciplina; }
    public void setCodigoDisciplina(String codigoDisciplina) { this.codigoDisciplina = codigoDisciplina; }

    public String getCodigoPeriodo() { return codigoPeriodo; }
    public void setCodigoPeriodo(String codigoPeriodo) { this.codigoPeriodo = codigoPeriodo; }

    public int getVagas() { return vagas; }
    public void setVagas(int vagas) { this.vagas = vagas; }

    public String getHorario() { return horario; }
    public void setHorario(String horario) { this.horario = horario; }

    public String getMatriculaProfessor() { return matriculaProfessor; }
    public void setMatriculaProfessor(String matriculaProfessor) { this.matriculaProfessor = matriculaProfessor; }

    // -------------------------------------------------------------------------
    // Identificador composto: disciplina + período + código
    // -------------------------------------------------------------------------

    /**
     * Retorna a chave única composta que identifica esta turma no sistema.
     * Formato: "{codigoDisciplina}_{codigoPeriodo}_{codigo}"
     * Exemplo: "MAT001_2026.1_T01"
     */
    @JsonIgnore
    public String getChaveUnica() {
        return codigoDisciplina + "_" + codigoPeriodo + "_" + codigo;
    }

    @Override
    public String toString() {
        String prof = (matriculaProfessor == null || matriculaProfessor.trim().isEmpty())
                ? "sem professor"
                : matriculaProfessor;
        return String.format(
                "[TURMA] %s | Disciplina: %s | Período: %s | %d vagas | %s | Prof: %s",
                codigo, codigoDisciplina, codigoPeriodo, vagas, horario, prof
        );
    }
}