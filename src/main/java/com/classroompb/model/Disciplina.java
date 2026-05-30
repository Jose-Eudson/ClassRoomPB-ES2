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
    private List<String> pre_requisitos;
    
    /** Construtor padrao necessario para desserializacao. */
    public Disciplina() {
        this.pre_requisitos = new ArrayList<>();
    }

    /** Construtor completo para criacao de disciplinas. */
    public Disciplina(
            String codigo,
            String nome,
            int cargaHoraria,
            int creditos,
            List<String> pre_requisitos
    ) {
        this.codigo = codigo;
        this.nome = nome;
        this.cargaHoraria = cargaHoraria;
        this.creditos = creditos;
        this.pre_requisitos = pre_requisitos;
    }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public int getCargaHoraria() { return cargaHoraria; }
    public void setCargaHoraria(int cargaHoraria) { this.cargaHoraria = cargaHoraria; }

    public int getCreditos() {return creditos;}
    public void setCreditos(int creditos) {this.creditos = creditos;}
    
    
    public List<String> getPreRequisitos() {
        return pre_requisitos;
    }

    public void setPreRequisitos(List<String> pre_requisitos) {
        this.pre_requisitos = pre_requisitos;
    }
    
    @Override
    public String toString() {
        return String.format("[DISCIPLINA] %s - %s (%dh, %d créditos) pré-requisitos: %s",
                codigo,
                nome,
                cargaHoraria,
                creditos,
                pre_requisitos
        );
    }
}