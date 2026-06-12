package com.classroompb.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidade de dominio que representa uma disciplina.
 */
public class Disciplina implements Serializable {
    private static final long serialVersionUID = 1L;

    private String codigo;
    private String nome;
    private int cargaHoraria;
    private int creditos;
    private List<String> preRequisitos;

    /** Construtor padrao necessario para desserializacao. */
    public Disciplina() {
        this.preRequisitos = new ArrayList<>();
    }

    /** Construtor completo para criacao de disciplinas. */
    public Disciplina(String codigo, String nome, int cargaHoraria, int creditos, List<String> preRequisitos) {
        this.codigo = codigo;
        this.nome = nome;
        this.cargaHoraria = cargaHoraria;
        this.creditos = creditos;
        this.preRequisitos = preRequisitos != null ? new ArrayList<>(preRequisitos) : new ArrayList<>();
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

    public int getCreditos() {
        return creditos;
    }

    public void setCreditos(int creditos) {
        this.creditos = creditos;
    }

    public List<String> getPreRequisitos() {
        return new ArrayList<>(preRequisitos);
    }

    public void setPreRequisitos(List<String> preRequisitos) {
        this.preRequisitos = preRequisitos != null ? new ArrayList<>(preRequisitos) : new ArrayList<>();
    }

    @Override
    public String toString() {
        return String.format("[DISCIPLINA] %s - %s (%dh, %d créditos) pré-requisitos: %s", codigo, nome, cargaHoraria,
                creditos, preRequisitos);
    }
}
