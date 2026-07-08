package com.classroompb.model;

import java.io.Serializable;

public class Nota implements Serializable {

    private static final long serialVersionUID = 1L;

    private String matriculaAluno;
    private String codigoDisciplina;
    private String codigoPeriodo;
    private String codigoTurma;

    private Double etapa1;
    private Double etapa2;

    public Nota() {
    }

    public Nota(String matriculaAluno, String codigoDisciplina, String codigoPeriodo, String codigoTurma) {

        this.matriculaAluno = matriculaAluno;
        this.codigoDisciplina = codigoDisciplina;
        this.codigoPeriodo = codigoPeriodo;
        this.codigoTurma = codigoTurma;
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

    public Double getEtapa1() {
        return etapa1;
    }

    public void setEtapa1(Double etapa1) {
        this.etapa1 = etapa1;
    }

    public Double getEtapa2() {
        return etapa2;
    }

    public void setEtapa2(Double etapa2) {
        this.etapa2 = etapa2;
    }

    public String getChaveUnica() {
        return matriculaAluno + "_" + codigoDisciplina + "_" + codigoPeriodo + "_" + codigoTurma;
    }
}
