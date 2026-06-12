package com.classroompb.model;

/**
 * Representa um usuário do tipo Administrador. Herda todos os atributos de Usuario e define o tipo como ADMINISTRADOR
 * automaticamente.
 */

public class Administrador extends Usuario {
    public Administrador() {
        super();
    }

    /** Construtor completo — define o tipo como ADMINISTRADOR automaticamente. */
    public Administrador(String matricula, String nome, String email, String senha) {
        super(matricula, nome, email, senha, TipoUsuario.ADMINISTRADOR);
    }
}
