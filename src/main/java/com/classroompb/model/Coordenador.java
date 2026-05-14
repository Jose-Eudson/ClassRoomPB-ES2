package com.classroompb.model;

/**
 * Representa um usuário do tipo Coordenador.
 * Herda todos os atributos de Usuario e define o tipo como COORDENADOR automaticamente.
 */

public class Coordenador extends Usuario {
    public Coordenador() { super(); }

    /** Construtor completo — define o tipo como COORDENADOR automaticamente. */
    public Coordenador(String matricula, String nome, String email, String senha) {
        super(matricula, nome, email, senha, TipoUsuario.COORDENADOR);
    }
}
