package com.classroompb.model;

import java.io.Serializable;

public class Avaliacao implements Serializable {
    private static final long serialVersionUID = 1L;

    private String codigo;
    private String codigoDiario;
    private String codigoDisciplina;
    private String codigoPeriodo;
    private String codigoTurma;
    private String descricao;
    private String etapa;
    private double peso;
    private double notaMaxima;

    public Avaliacao() {
    }

    @SuppressWarnings("checkstyle:ParameterNumber")
    public Avaliacao(String codigo, String codigoDiario, String codigoDisciplina, String codigoPeriodo,
            String codigoTurma, String descricao, String etapa, double peso, double notaMaxima) {
        this.codigo = codigo;
        this.codigoDiario = codigoDiario;
        this.codigoDisciplina = codigoDisciplina;
        this.codigoPeriodo = codigoPeriodo;
        this.codigoTurma = codigoTurma;
        this.descricao = descricao;
        this.etapa = etapa;
        this.peso = peso;
        this.notaMaxima = notaMaxima;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
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

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
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

    public String getEtapa() {
        return etapa;
    }

    public void setEtapa(String etapa) {
        this.etapa = etapa;
    }

    public double getPeso() {
        return peso;
    }

    public void setPeso(double peso) {
        this.peso = peso;
    }

    public double getNotaMaxima() {
        return notaMaxima;
    }

    public void setNotaMaxima(double notaMaxima) {
        this.notaMaxima = notaMaxima;
    }
}
