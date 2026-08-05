package com.classroompb.model;

import java.io.Serializable;

public class Nota implements Serializable {

    private static final long serialVersionUID = 1L;

    private String matriculaAluno;
    private String codigoDisciplina;
    private String codigoPeriodo;
    private String codigoTurma;

    private String codigoDiario;
    private String codigoAvaliacao;
    private Double valor;
    private String matriculaProfessor;

    private Double etapa1;
    private Double etapa2;

    public Nota() {
    }

    public Nota(String matriculaAluno, String codigoDisciplina, String codigoPeriodo, String codigoTurma) {
        setMatriculaAluno(matriculaAluno);
        setCodigoDisciplina(codigoDisciplina);
        setCodigoPeriodo(codigoPeriodo);
        setCodigoTurma(codigoTurma);
    }

    @SuppressWarnings("checkstyle:ParameterNumber")
    public Nota(String matriculaAluno, String codigoDisciplina, String codigoPeriodo, String codigoTurma,
            String codigoDiario, String codigoAvaliacao, Double valor, String matriculaProfessor) {
        this(matriculaAluno, codigoDisciplina, codigoPeriodo, codigoTurma);
        this.codigoDiario = codigoDiario;
        this.codigoAvaliacao = codigoAvaliacao;
        this.valor = valor;
        this.matriculaProfessor = matriculaProfessor;
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

    public Double getEtapa1() {
        return etapa1;
    }

    public void setEtapa1(Double etapa1) {
        this.etapa1 = etapa1;
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

    public String getCodigoAvaliacao() {
        return codigoAvaliacao;
    }

    public void setCodigoAvaliacao(String codigoAvaliacao) {
        this.codigoAvaliacao = codigoAvaliacao;
    }

    public Double getValor() {
        return valor;
    }

    public void setValor(Double valor) {
        this.valor = valor;
    }

    public String getMatriculaProfessor() {
        return matriculaProfessor;
    }

    public void setMatriculaProfessor(String matriculaProfessor) {
        this.matriculaProfessor = matriculaProfessor;
    }

    public Double getEtapa2() {
        return etapa2;
    }

    public void setEtapa2(Double etapa2) {
        this.etapa2 = etapa2;
    }

    public String getChaveUnica() {
        if (codigoAvaliacao != null && !codigoAvaliacao.isBlank()) {
            return matriculaAluno + "_" + codigoAvaliacao;
        }
        return matriculaAluno + "_" + codigoDisciplina + "_" + codigoPeriodo + "_" + codigoTurma;
    }
}
