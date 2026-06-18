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
    private LocalDate dataAula;
    private StatusFrequencia status;
    private String matriculaProfessor;
    private LocalDateTime dataRegistro;

    /** Construtor padrao necessario para desserializacao JSON. */
    public RegistroFrequencia() {
    }

    /** Cria um registro de frequencia para uma aula. */
    public RegistroFrequencia(String matriculaAluno, String codigoDisciplina, String codigoPeriodo, String codigoTurma,
            LocalDate dataAula, StatusFrequencia status, String matriculaProfessor) {
        this.matriculaAluno = matriculaAluno;
        this.codigoDisciplina = codigoDisciplina;
        this.codigoPeriodo = codigoPeriodo;
        this.codigoTurma = codigoTurma;
        this.dataAula = dataAula;
        this.status = status;
        this.matriculaProfessor = matriculaProfessor;
        this.dataRegistro = LocalDateTime.now();
    }

    public String getMatriculaAluno() {
        return matriculaAluno;
    }

    public void setMatriculaAluno(String matriculaAluno) {
        this.matriculaAluno = matriculaAluno;
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
        return matriculaAluno + "_" + codigoDisciplina + "_" + codigoPeriodo + "_" + codigoTurma + "_" + dataAula;
    }

    @Override
    public String toString() {
        return String.format("[FREQUENCIA] Aluno: %s | Turma: %s | Disciplina: %s | Periodo: %s | Aula: %s | %s",
                matriculaAluno, codigoTurma, codigoDisciplina, codigoPeriodo, dataAula, status);
    }
}
