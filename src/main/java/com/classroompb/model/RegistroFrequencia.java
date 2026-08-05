package com.classroompb.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * RF27: Entidade de dominio que representa a presenca ou falta de um aluno em uma aula especifica.
 *
 * Um registro e identificado pela combinacao unica (matriculaAluno + codigoDisciplina + codigoPeriodo + codigoTurma +
 * dataAula), permitindo um unico lancamento por aluno em cada aula.
 */
public class RegistroFrequencia implements Serializable {

    private static final long serialVersionUID = 1L;

    private String matriculaAluno;
    private String codigoDisciplina;
    private String codigoPeriodo;
    private String codigoTurma;
    private String codigoDiario;
    private String codigoAula;
    private LocalDate dataAula;
    private StatusFrequencia status;
    private String matriculaProfessor;
    private LocalDateTime dataRegistro;

    /** Construtor padrao necessario para desserializacao JSON. */
    public RegistroFrequencia() {
    }

    /** Cria um registro de frequencia para uma aula. */
    public RegistroFrequencia(String matriculaAluno, String codigoDisciplina, String codigoPeriodo, String codigoTurma,
            String codigoAula, LocalDate dataAula, StatusFrequencia status, String matriculaProfessor) {
        this(matriculaAluno, codigoDisciplina, codigoPeriodo, codigoTurma, null, codigoAula, dataAula, status,
                matriculaProfessor);
    }

    @SuppressWarnings("checkstyle:ParameterNumber")
    public RegistroFrequencia(String matriculaAluno, String codigoDisciplina, String codigoPeriodo, String codigoTurma,
            String codigoDiario, String codigoAula, LocalDate dataAula, StatusFrequencia status,
            String matriculaProfessor) {
        setMatriculaAluno(matriculaAluno);
        setCodigoDisciplina(codigoDisciplina);
        setCodigoPeriodo(codigoPeriodo);
        setCodigoTurma(codigoTurma);
        setCodigoDiario(codigoDiario);
        setCodigoAula(codigoAula);
        setDataAula(dataAula);
        setStatus(status);
        setMatriculaProfessor(matriculaProfessor);
        this.dataRegistro = LocalDateTime.now();
    }

    public String getMatriculaAluno() {
        return matriculaAluno;
    }

    public void setMatriculaAluno(String matriculaAluno) {
        this.matriculaAluno = matriculaAluno;
    }

    public String getCodigoDiario() {
        return codigoDiario;
    }

    public void setCodigoDiario(String codigoDiario) {
        this.codigoDiario = codigoDiario;
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

    public String getCodigoTurma() {
        return codigoTurma;
    }

    public String getCodigoAula() {
        return codigoAula;
    }

    public void setCodigoAula(String codigoAula) {
        this.codigoAula = codigoAula;
    }

    public void setCodigoTurma(String codigoTurma) {
        this.codigoTurma = codigoTurma;
    }

    public LocalDate getDataAula() {
        return dataAula;
    }

    public void setDataAula(LocalDate dataAula) {
        this.dataAula = dataAula;
    }

    public StatusFrequencia getStatus() {
        return status;
    }

    public void setStatus(StatusFrequencia status) {
        this.status = status;
    }

    public String getMatriculaProfessor() {
        return matriculaProfessor;
    }

    public void setMatriculaProfessor(String matriculaProfessor) {
        this.matriculaProfessor = matriculaProfessor;
    }

    public LocalDateTime getDataRegistro() {
        return dataRegistro;
    }

    public void setDataRegistro(LocalDateTime dataRegistro) {
        this.dataRegistro = dataRegistro;
    }

    /**
     * Retorna a chave unica composta que identifica a frequencia do aluno em uma aula.
     */
    @JsonIgnore
    public String getChaveUnica() {
        if (codigoDiario != null && !codigoDiario.isBlank()) {
            return matriculaAluno + "_" + codigoDiario + "_" + codigoAula;
        }
        return matriculaAluno + "_" + codigoDisciplina + "_" + codigoPeriodo + "_" + codigoTurma + "_" + dataAula
                + "_" + codigoAula;
    }

    @Override
    public String toString() {
        return String.format("[FREQUENCIA] Aluno: %s | Turma: %s | Disciplina: %s | Periodo: %s | Aula: %s | %s",
                matriculaAluno, codigoTurma, codigoDisciplina, codigoPeriodo, codigoAula + "@" + dataAula, status);
    }
}
