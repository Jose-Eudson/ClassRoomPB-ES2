package com.classroompb.model;

import java.time.LocalDate;

public class Aula {
    private String codigo;
    private String codigoDiario;
    private LocalDate data;
    private String conteudo;
    private int numero;

    public Aula(String codigo, String codigoDiario, LocalDate data, String conteudo, int numero) {
        this.codigo = codigo;
        this.codigoDiario = codigoDiario;
        this.data = data;
        this.conteudo = conteudo;
        this.numero = numero;
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

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public String getConteudo() {
        return conteudo;
    }

    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }
}
