package com.classroompb.repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.classroompb.model.Aula;
import com.classroompb.util.JsonUtil;

public class AulaRepository {

    private static final String CAMINHO_PADRAO = "aulas.json";
    private final String caminhoArquivo;
    private List<Aula> aulas;

    public AulaRepository() {
        this(CAMINHO_PADRAO);
    }

    public AulaRepository(String caminhoArquivo) {
        this.caminhoArquivo = caminhoArquivo;
        carregarDados();
    }

    private void carregarDados() {
        try {
            this.aulas = JsonUtil.carregarLista(caminhoArquivo, Aula.class);
        } catch (IOException e) {
            System.err.println("Erro ao carregar aulas: " + e.getMessage());
            this.aulas = new ArrayList<>();
        }
    }

    public void salvar(Aula aula) {
        aulas.add(aula);
        salvarDados();
    }

    public List<Aula> listarTodas() {
        return new ArrayList<>(aulas);
    }

    public boolean existePorCodigo(String codigo) {
        return aulas.stream().anyMatch(a -> a.getCodigo().equalsIgnoreCase(codigo));
    }

    public Aula buscarPorCodigo(String codigo) {
        return aulas.stream().filter(a -> a.getCodigo().equalsIgnoreCase(codigo)).findFirst().orElse(null);
    }

    public void atualizar(Aula aulaAtualizada) {
        for (int i = 0; i < aulas.size(); i++) {
            if (aulas.get(i).getCodigo().equalsIgnoreCase(aulaAtualizada.getCodigo())) {
                aulas.set(i, aulaAtualizada);
                salvarDados();
                return;
            }
        }
        throw new IllegalArgumentException("Aula com codigo " + aulaAtualizada.getCodigo() + " nao encontrada.");
    }

    public void deletar(String codigo) {
        aulas.removeIf(a -> a.getCodigo().equalsIgnoreCase(codigo));
        salvarDados();
    }

    public List<Aula> buscarPorDiario(String codigoDiario) {

        return aulas.stream().filter(a -> a.getCodigoDiario().equalsIgnoreCase(codigoDiario)).toList();
    }

    public Aula buscarPorDiarioENumero(String codigoDiario, int numero) {

        return aulas.stream().filter(a -> a.getCodigoDiario().equalsIgnoreCase(codigoDiario))
                .filter(a -> a.getNumero() == numero).findFirst().orElse(null);
    }

    private void salvarDados() {
        try {
            JsonUtil.salvar(caminhoArquivo, aulas);
        } catch (IOException e) {
            System.err.println("Erro ao salvar aulas: " + e.getMessage());
        }
    }
}
