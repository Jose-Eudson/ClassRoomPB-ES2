package com.classroompb.model;

import java.io.Serializable;

public class Historico implements Serializable {

    private static final long serialVersionUID = 1L;

    private String matriculaAluno;

    private String codigoDisciplina;

    private double notaFinal;

    private boolean aprovado;

    public Historico() {
    }

    public Historico(String matriculaAluno, String codigoDisciplina, double notaFinal, boolean aprovado) {

        this.matriculaAluno = matriculaAluno;
        this.codigoDisciplina = codigoDisciplina;
        this.notaFinal = notaFinal;
        this.aprovado = aprovado;
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

    public double getNotaFinal() {
        return notaFinal;
    }

    public void setNotaFinal(double notaFinal) {
        this.notaFinal = notaFinal;
    }

    public boolean isAprovado() {
        return aprovado;
    }

    public void setAprovado(boolean aprovado) {
        this.aprovado = aprovado;
    }
}
