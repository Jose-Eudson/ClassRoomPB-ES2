package com.classroompb.model;

public class Diario {
    private String codigo;
    private String codigoTurma;
    private String descricao;
    private String matriculaProfessor;
    private String horario;
    private String sala;
    private int cargaHoraria;
    private SituacaoDiario situacao;

    public Diario() {

    }

    public Diario(String codigo, String codigoTurma, String descricao, String matriculaProfessor, String horario,
            String sala, int cargaHoraria, SituacaoDiario situacao) {
        this.codigo = codigo;
        this.codigoTurma = codigoTurma;
        this.descricao = descricao;
        this.matriculaProfessor = matriculaProfessor;
        this.horario = horario;
        this.sala = sala;
        this.cargaHoraria = cargaHoraria;
        this.situacao = situacao;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getCodigoTurma() {
        return codigoTurma;
    }

    public void setCodigoTurma(String codigoTurma) {
        this.codigoTurma = codigoTurma;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getMatriculaProfessor() {
        return matriculaProfessor;
    }

    public void setMatriculaProfessor(String matriculaProfessor) {
        this.matriculaProfessor = matriculaProfessor;
    }

    public String getHorario() {
        return horario;
    }

    public void setHorario(String horario) {
        this.horario = horario;
    }

    public String getSala() {
        return sala;
    }

    public void setSala(String sala) {
        this.sala = sala;
    }

    public int getCargaHoraria() {
        return cargaHoraria;
    }

    public void setCargaHoraria(int cargaHoraria) {
        this.cargaHoraria = cargaHoraria;
    }

    public SituacaoDiario getSituacao() {
        return situacao;
    }

    public void setSituacao(SituacaoDiario situacao) {
        this.situacao = situacao;
    }
}
