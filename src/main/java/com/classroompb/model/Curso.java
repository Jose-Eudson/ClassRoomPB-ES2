package com.classroompb.model;

import java.io.Serializable;

/**
 * Entidade de domínio que representa um curso da instituição.
 */
public class Curso implements Serializable {
    private static final long serialVersionUID = 1L;

    private String codigo;
    private String nome;
    private int cargaHoraria;

    /** Construtor padrão necessário para desserialização. */
    public Curso() {
    }

    /** Construtor completo para criação de cursos. */
    public Curso(String codigo, String nome, int cargaHoraria) {
        this.codigo = codigo;
        this.nome = nome;
        this.cargaHoraria = cargaHoraria;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getCargaHoraria() {
        return cargaHoraria;
    }

    public void setCargaHoraria(int cargaHoraria) {
        this.cargaHoraria = cargaHoraria;
    }

    @Override
    public String toString() {
        return String.format("[CURSO] %s - %s (%dh)", codigo, nome, cargaHoraria);
    }
}
