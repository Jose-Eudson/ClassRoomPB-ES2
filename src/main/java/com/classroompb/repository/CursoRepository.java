package com.classroompb.repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.classroompb.model.Curso;
import com.classroompb.util.JsonUtil;

/**
 * Repositório responsável pela persistência de cursos em arquivo JSON.
 */
public class CursoRepository {
    private static final String CAMINHO_PADRAO = "cursos.json";
    private final String caminhoArquivo;
    private List<Curso> cursos;

    public CursoRepository() {
        this(CAMINHO_PADRAO);
    }

    public CursoRepository(String caminhoArquivo) {
        this.caminhoArquivo = caminhoArquivo;
        carregarDados();
    }

    private void carregarDados() {
        try {
            this.cursos = JsonUtil.carregarLista(caminhoArquivo, Curso.class);
        } catch (IOException e) {
            System.err.println("Erro ao carregar cursos: " + e.getMessage());
            this.cursos = new ArrayList<>();
        }
    }

    private void salvarDados() {
        try {
            JsonUtil.salvar(caminhoArquivo, cursos);
        } catch (IOException e) {
            System.err.println("Erro ao salvar cursos: " + e.getMessage());
        }
    }

    public void salvar(Curso curso) {
        cursos.add(curso);
        salvarDados();
    }

    public List<Curso> listarTodos() {
        return new ArrayList<>(cursos);
    }

    public boolean existePorCodigo(String codigo) {
        return cursos.stream().anyMatch(c -> c.getCodigo().equalsIgnoreCase(codigo));
    }

    public Curso buscarPorCodigo(String codigo) {
        return cursos.stream()
                .filter(c -> c.getCodigo().equalsIgnoreCase(codigo))
                .findFirst()
                .orElse(null);
    }

    public void atualizar(Curso atualizado) {
        for (int i = 0; i < cursos.size(); i++) {
            if (cursos.get(i).getCodigo().equalsIgnoreCase(atualizado.getCodigo())) {
                cursos.set(i, atualizado);
                salvarDados();
                return;
            }
        }
        throw new IllegalArgumentException(
                "Curso com codigo " + atualizado.getCodigo() + " nao encontrado."
        );
    }

    public void deletar(String codigo) {
        boolean removido = cursos.removeIf(c -> c.getCodigo().equalsIgnoreCase(codigo));
        if (removido) {
            salvarDados();
        } else {
            throw new IllegalArgumentException(
                    "Curso com codigo " + codigo + " nao encontrado."
            );
        }
    }
}