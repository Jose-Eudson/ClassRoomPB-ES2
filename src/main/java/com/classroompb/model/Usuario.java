package com.classroompb.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Classe abstrata base para todos os usuários do sistema.
 *
 * Usa anotações do Jackson para serialização/deserialização polimórfica em JSON:
 * o campo "tipo" determina qual subclasse concreta instanciar ao carregar do arquivo.
 */

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "tipo",
    visible = true
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = Aluno.class, name = "ALUNO"),
    @JsonSubTypes.Type(value = Professor.class, name = "PROFESSOR"),
    @JsonSubTypes.Type(value = Coordenador.class, name = "COORDENADOR"),
    @JsonSubTypes.Type(value = Administrador.class, name = "ADMINISTRADOR")
})
public abstract class Usuario implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String matricula;
    private String nome;
    private String email;
    private String senha;
    private TipoUsuario tipo;

    /** Construtor padrão necessário para o Jackson deserializar o JSON. */
    public Usuario() {}

    /** Construtor completo usado pelas subclasses ao criar um novo usuário. */
    public Usuario(String matricula, String nome, String email, String senha, TipoUsuario tipo) {
        this.matricula = matricula;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.tipo = tipo;
    }

    // Getters e Setters
    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public TipoUsuario getTipo() { return tipo; }
    public void setTipo(TipoUsuario tipo) { this.tipo = tipo; }

    /** Retorna uma representação textual resumida do usuário para exibição no console. */
    @Override
    public String toString() {
        return String.format("[%s] %s - %s (%s)", tipo, nome, matricula, email);
    }
}
