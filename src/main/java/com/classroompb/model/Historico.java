package com.classroompb.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Historico implements Serializable {

    private static final long serialVersionUID = 1L;

    private String matriculaAluno;
    private String codigoPeriodo;
    private String codigoDisciplina;
    private String nomeDisciplina;
    private String codigoTurma;
    private String matriculaProfessor;
    private String nomeProfessor;
    private double notaFinal;
    private double frequencia;
    private String situacao;
    private boolean aprovado;

    public Historico() {
    }

    public Historico(String matriculaAluno, String codigoDisciplina, double notaFinal, boolean aprovado) {
        this.matriculaAluno = matriculaAluno;
        this.codigoDisciplina = codigoDisciplina;
        this.notaFinal = notaFinal;
        this.aprovado = aprovado;
        this.situacao = aprovado ? "APROVADO" : "REPROVADO POR NOTA";
    }

    @SuppressWarnings("checkstyle:ParameterNumber")
    public Historico(String matriculaAluno, String codigoPeriodo, String codigoDisciplina, String nomeDisciplina,
            String codigoTurma, String matriculaProfessor, String nomeProfessor, double notaFinal, double frequencia,
            String situacao) {
        this.matriculaAluno = matriculaAluno;
        this.codigoPeriodo = codigoPeriodo;
        this.codigoDisciplina = codigoDisciplina;
        this.nomeDisciplina = nomeDisciplina;
        this.codigoTurma = codigoTurma;
        this.matriculaProfessor = matriculaProfessor;
        this.nomeProfessor = nomeProfessor;
        this.notaFinal = notaFinal;
        this.frequencia = frequencia;
        setSituacao(situacao);
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

    public String getCodigoPeriodo() {
        return codigoPeriodo;
    }

    public void setCodigoPeriodo(String codigoPeriodo) {
        this.codigoPeriodo = codigoPeriodo;
    }

    public String getNomeDisciplina() {
        return nomeDisciplina;
    }

    public void setNomeDisciplina(String nomeDisciplina) {
        this.nomeDisciplina = nomeDisciplina;
    }

    public String getCodigoTurma() {
        return codigoTurma;
    }

    public void setCodigoTurma(String codigoTurma) {
        this.codigoTurma = codigoTurma;
    }

    public String getMatriculaProfessor() {
        return matriculaProfessor;
    }

    public void setMatriculaProfessor(String matriculaProfessor) {
        this.matriculaProfessor = matriculaProfessor;
    }

    public String getNomeProfessor() {
        return nomeProfessor;
    }

    public void setNomeProfessor(String nomeProfessor) {
        this.nomeProfessor = nomeProfessor;
    }

    public void setCodigoDisciplina(String codigoDisciplina) {
        this.codigoDisciplina = codigoDisciplina;
    }

    public double getNotaFinal() {
        return notaFinal;
    }

    public void setNotaFinal(double notaFinal) {
        this.notaFinal = notaFinal;
    }

    public double getFrequencia() {
        return frequencia;
    }

    public void setFrequencia(double frequencia) {
        this.frequencia = frequencia;
    }

    public String getSituacao() {
        return situacao;
    }

    public void setSituacao(String situacao) {
        this.situacao = situacao;
        this.aprovado = "APROVADO".equalsIgnoreCase(situacao);
    }

    public boolean isAprovado() {
        return aprovado;
    }

    public void setAprovado(boolean aprovado) {
        this.aprovado = aprovado;
    }

    @JsonIgnore
    public String getChaveUnica() {
        return matriculaAluno + "_" + codigoDisciplina + "_" + codigoPeriodo + "_" + codigoTurma;
    }
}
