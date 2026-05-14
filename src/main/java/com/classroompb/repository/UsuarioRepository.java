package com.classroompb.repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.classroompb.model.Usuario;
import com.classroompb.util.JsonUtil;

/**
 * Repositório responsável pela persistência de usuários em arquivo JSON.
 * Mantém a lista em memória e sincroniza com o disco a cada operação de escrita.
 */

public class UsuarioRepository {
    private static final String CAMINHO_PADRAO = "usuarios.json";
    private final String caminhoArquivo;
    private List<Usuario> usuarios;

    /**
     * Construtor padrão — usa o arquivo real "usuarios.json" em produção.
     */
    public UsuarioRepository() {
        this(CAMINHO_PADRAO);
    }

    /**
     * Construtor para testes — permite injetar um caminho de arquivo temporário,
     * evitando que os testes apaguem ou sobrescrevam o "usuarios.json" de produção.
     */
    public UsuarioRepository(String caminhoArquivo) {
        this.caminhoArquivo = caminhoArquivo;
        carregarDados();
    }

    /** Carrega a lista de usuários do arquivo JSON para a memória ao inicializar. */
    private void carregarDados() {
        try {
            this.usuarios = JsonUtil.carregarLista(caminhoArquivo, Usuario.class);
        } catch (IOException e) {
            System.err.println("Erro ao carregar usuários: " + e.getMessage());
            this.usuarios = new ArrayList<>();
        }
    }

    /** Persiste a lista atual de usuários no arquivo JSON. */
    private void salvarDados() {
        try {
            JsonUtil.salvar(caminhoArquivo, usuarios);
        } catch (IOException e) {
            System.err.println("Erro ao salvar usuários: " + e.getMessage());
        }
    }

    /** Adiciona um novo usuário à lista e persiste no arquivo. */
    public void salvar(Usuario usuario) {
        usuarios.add(usuario);
        salvarDados();
    }

    /** Retorna uma cópia da lista de todos os usuários cadastrados. */
    public List<Usuario> listarTodos() {
        return new ArrayList<>(usuarios);
    }

    /** Busca um usuário pela matrícula. Retorna Optional vazio se não encontrar. */
    public Optional<Usuario> buscarPorMatricula(String matricula) {
        return usuarios.stream()
                .filter(u -> u.getMatricula().equals(matricula))
                .findFirst();
    }

    /** Busca um usuário pelo e-mail, ignorando maiúsculas/minúsculas. */
    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarios.stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    /**
     * Substitui o usuário existente com a mesma matrícula pelos novos dados e persiste.
     * Lança exceção se a matrícula não for encontrada.
     */
    public void atualizar(Usuario usuarioAtualizado) {
        for (int i = 0; i < usuarios.size(); i++) {
            if (usuarios.get(i).getMatricula().equals(usuarioAtualizado.getMatricula())) {
                usuarios.set(i, usuarioAtualizado);
                salvarDados();
                return;
            }
        }
        throw new IllegalArgumentException("Usuário com matrícula " + usuarioAtualizado.getMatricula() + " não encontrado.");
    }

    /**
     * Remove o usuário com a matrícula informada e persiste.
     * Lança exceção se a matrícula não for encontrada.
     */
    public void deletar(String matricula) {
        boolean removido = usuarios.removeIf(u -> u.getMatricula().equals(matricula));
        if (removido) {
            salvarDados();
        } else {
            throw new IllegalArgumentException("Usuário com matrícula " + matricula + " não encontrado.");
        }
    }
}
