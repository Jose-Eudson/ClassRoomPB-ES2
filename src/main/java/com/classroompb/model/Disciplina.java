package com.classroompb.model;

import java.io.Serializable;

/**
 * Entidade de dominio que representa uma disciplina.
 */
public class Disciplina implements Serializable {
    private static final long serialVersionUID = 1L;

    private String codigo;
    private String nome;
    private int cargaHoraria;

    /** Construtor padrao necessario para desserializacao. */
    public Disciplina() {}

    /** Construtor completo para criacao de disciplinas. */
    public Disciplina(String codigo, String nome, int cargaHoraria) {
        this.codigo = codigo;
        this.nome = nome;
        this.cargaHoraria = cargaHoraria;
    }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public int getCargaHoraria() { return cargaHoraria; }
    public void setCargaHoraria(int cargaHoraria) { this.cargaHoraria = cargaHoraria; }

    @Override
    public String toString() {
        return String.format("[DISCIPLINA] %s - %s (%dh)", codigo, nome, cargaHoraria);
    }
}
