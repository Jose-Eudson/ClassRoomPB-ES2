package com.classroompb.repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.classroompb.model.Diario;
import com.classroompb.util.JsonUtil;

/**
 * Repositório responsável pela persistência de diários em arquivo JSON.
 */
public class DiarioRepository {

    private static final String CAMINHO_PADRAO = "diarios.json";

    private final String caminhoArquivo;

    private List<Diario> diarios;

    public DiarioRepository() {
        this(CAMINHO_PADRAO);
    }

    public DiarioRepository(String caminhoArquivo) {
        this.caminhoArquivo = caminhoArquivo;
        carregarDados();
    }

    private void carregarDados() {
        try {
            this.diarios = JsonUtil.carregarLista(caminhoArquivo, Diario.class);
        } catch (IOException e) {
            System.err.println("Erro ao carregar diarios: " + e.getMessage());
            this.diarios = new ArrayList<>();
        }
    }

    private void salvarDados() {
        try {
            JsonUtil.salvar(caminhoArquivo, diarios);
        } catch (IOException e) {
            System.err.println("Erro ao salvar diarios: " + e.getMessage());
        }
    }

    public void salvar(Diario diario) {
        diarios.add(diario);
        salvarDados();
    }

    public List<Diario> listarTodos() {
        return new ArrayList<>(diarios);
    }

    public boolean existePorCodigo(String codigo) {
        return diarios.stream().anyMatch(d -> d.getCodigo().equalsIgnoreCase(codigo));
    }

    public Diario buscarPorCodigo(String codigo) {
        return diarios.stream().filter(d -> d.getCodigo().equalsIgnoreCase(codigo)).findFirst().orElse(null);
    }

    public List<Diario> buscarPorTurma(String codigoTurma) {
        return diarios.stream().filter(d -> d.getCodigoTurma().equalsIgnoreCase(codigoTurma))
                .collect(Collectors.toList());
    }

    public List<Diario> buscarPorProfessor(String matriculaProfessor) {
        return diarios.stream().filter(d -> d.getMatriculaProfessor().equalsIgnoreCase(matriculaProfessor))
                .collect(Collectors.toList());
    }

    public void atualizar(Diario atualizado) {
        for (int i = 0; i < diarios.size(); i++) {

            if (diarios.get(i).getCodigo().equalsIgnoreCase(atualizado.getCodigo())) {

                diarios.set(i, atualizado);
                salvarDados();
                return;
            }
        }

        throw new IllegalArgumentException("Diario com codigo " + atualizado.getCodigo() + " nao encontrado.");
    }

    public void deletar(String codigo) {

        boolean removido = diarios.removeIf(d -> d.getCodigo().equalsIgnoreCase(codigo));

        if (removido) {
            salvarDados();
        } else {
            throw new IllegalArgumentException("Diario com codigo " + codigo + " nao encontrado.");
        }
    }
}
