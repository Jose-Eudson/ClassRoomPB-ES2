package com.classroompb.repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.classroompb.model.Disciplina;
import com.classroompb.util.JsonUtil;

/**
 * Repositorio responsavel pela persistencia de disciplinas em arquivo JSON.
 */
public class DisciplinaRepository {

    private static final String CAMINHO_PADRAO = "disciplinas.json";

    private final String caminhoArquivo;

    private List<Disciplina> disciplinas;

    public DisciplinaRepository() {
        this(CAMINHO_PADRAO);
    }

    public DisciplinaRepository(String caminhoArquivo) {

        this.caminhoArquivo = caminhoArquivo;

        carregarDados();
    }

    private void carregarDados() {

        try {

            this.disciplinas = JsonUtil.carregarLista(caminhoArquivo, Disciplina.class);

        } catch (IOException e) {

            System.err.println("Erro ao carregar disciplinas: " + e.getMessage());

            this.disciplinas = new ArrayList<>();
        }
    }

    private void salvarDados() {

        try {

            JsonUtil.salvar(caminhoArquivo, disciplinas);

        } catch (IOException e) {

            System.err.println("Erro ao salvar disciplinas: " + e.getMessage());
        }
    }

    public void salvar(Disciplina disciplina) {

        disciplinas.add(disciplina);

        salvarDados();
    }

    public List<Disciplina> listarTodos() {
        return new ArrayList<>(disciplinas);
    }

    public boolean existePorCodigo(String codigo) {

        return disciplinas.stream().anyMatch(d -> d.getCodigo().equalsIgnoreCase(codigo));
    }

    public Disciplina buscarPorCodigo(String codigo) {

        return disciplinas.stream().filter(d -> d.getCodigo().equalsIgnoreCase(codigo)).findFirst().orElse(null);
    }

    public void atualizar(Disciplina atualizada) {

        for (int i = 0; i < disciplinas.size(); i++) {
            if (disciplinas.get(i).getCodigo().equalsIgnoreCase(atualizada.getCodigo())) {
                disciplinas.set(i, atualizada);
                salvarDados();
                return;
            }
        }
        throw new IllegalArgumentException("Disciplina com codigo " + atualizada.getCodigo() + " nao encontrada.");
    }

    public void deletar(String codigo) {

        boolean removida = disciplinas.removeIf(d -> d.getCodigo().equalsIgnoreCase(codigo));

        if (removida) {
            salvarDados();
        } else {
            throw new IllegalArgumentException("Disciplina com codigo " + codigo + " nao encontrada.");
        }
    }
}
