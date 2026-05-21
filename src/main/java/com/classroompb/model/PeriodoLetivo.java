package com.classroompb.model;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Entidade de dominio que representa um periodo letivo.
 */
public class PeriodoLetivo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String codigo;
    private int ano;
    private int semestre;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private boolean ativo;

    public PeriodoLetivo() {}

    public PeriodoLetivo(
            String codigo,  //2026.1
            int ano,
            int semestre,
            LocalDate dataInicio,
            LocalDate dataFim,
            boolean ativo 
    ) {
        this.codigo = codigo;
        this.ano = ano;
        this.semestre = semestre;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.ativo = ativo;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public int getAno() {
        return ano;
    }

    public void setAno(int ano) {
        this.ano = ano;
    }

    public int getSemestre() {
        return semestre;
    }

    public void setSemestre(int semestre) {
        this.semestre = semestre;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDate getDataFim() {
        return dataFim;
    }

    public void setDataFim(LocalDate dataFim) {
        this.dataFim = dataFim;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    @Override
    public String toString() {

        String status = ativo ? "ATIVO" : "INATIVO";

        return String.format(
                "[PERIODO LETIVO] %s - %d.%d | %s ate %s | %s",
                codigo,
                ano,
                semestre,
                dataInicio,
                dataFim,
                status
        );
    }
}