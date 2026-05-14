package com.classroompb.model;

/**
 * Representa um usuário do tipo Professor.
 * Herda todos os atributos de Usuario e define o tipo como PROFESSOR automaticamente.
 */

public class Professor extends Usuario {
    public Professor() { super(); }

    /** Construtor completo — define o tipo como PROFESSOR automaticamente. */
    public Professor(String matricula, String nome, String email, String senha) {
        super(matricula, nome, email, senha, TipoUsuario.PROFESSOR);
    }
}
