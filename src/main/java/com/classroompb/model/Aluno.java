package com.classroompb.model;

/**
 * Representa um usuário do tipo Aluno. Herda todos os atributos de Usuario e define o tipo como ALUNO automaticamente.
 */

public class Aluno extends Usuario {
    private String codigoCurso;

    public Aluno() {
        super();
    }

    /** Construtor completo — define o tipo como ALUNO automaticamente. */
    public Aluno(String matricula, String nome, String email, String senha) {
        super(matricula, nome, email, senha, TipoUsuario.ALUNO);
    }

    public Aluno(String matricula, String nome, String email, String senha, String codigoCurso) {
        this(matricula, nome, email, senha);
        this.codigoCurso = codigoCurso;
    }

    public String getCodigoCurso() {
        return codigoCurso;
    }

    public void setCodigoCurso(String codigoCurso) {
        this.codigoCurso = codigoCurso;
    }
}
