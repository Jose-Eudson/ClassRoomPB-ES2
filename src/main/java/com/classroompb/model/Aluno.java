package com.classroompb.model;

/**
 * Representa um usuário do tipo Aluno.
 * Herda todos os atributos de Usuario e define o tipo como ALUNO automaticamente.
 */

public class Aluno extends Usuario {
    public Aluno() { super(); }

    /** Construtor completo — define o tipo como ALUNO automaticamente. */
    public Aluno(String matricula, String nome, String email, String senha) {
        super(matricula, nome, email, senha, TipoUsuario.ALUNO);
    }
}
