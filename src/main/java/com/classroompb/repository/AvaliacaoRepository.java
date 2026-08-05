package com.classroompb.repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.classroompb.model.Avaliacao;
import com.classroompb.util.JsonUtil;

public class AvaliacaoRepository {
    private static final String CAMINHO_PADRAO = "avaliacoes.json";
    private final String caminhoArquivo;
    private List<Avaliacao> avaliacoes;

    public AvaliacaoRepository() {
        this(CAMINHO_PADRAO);
    }

    public AvaliacaoRepository(String caminhoArquivo) {
        this.caminhoArquivo = caminhoArquivo;
        carregarDados();
    }

    public void salvar(Avaliacao avaliacao) {
        if (buscarPorCodigo(avaliacao.getCodigo()) != null) {
            throw new IllegalArgumentException("Avaliação com código " + avaliacao.getCodigo() + " ja existe.");
        }
        avaliacoes.add(avaliacao);
        salvarDados();
    }

    public Avaliacao buscarPorCodigo(String codigo) {
        if (codigo == null) {
            return null;
        }
        return avaliacoes.stream().filter(a -> codigo.equalsIgnoreCase(a.getCodigo())).findFirst().orElse(null);
    }

    public List<Avaliacao> listarTodas() {
        return new ArrayList<>(avaliacoes);
    }

    public List<Avaliacao> listarPorDiario(String codigoDiario) {
        return avaliacoes.stream().filter(a -> a.getCodigoDiario().equalsIgnoreCase(codigoDiario))
                .collect(Collectors.toList());
    }

    public void atualizar(Avaliacao atualizada) {
        for (int i = 0; i < avaliacoes.size(); i++) {
            if (avaliacoes.get(i).getCodigo().equalsIgnoreCase(atualizada.getCodigo())) {
                avaliacoes.set(i, atualizada);
                salvarDados();
                return;
            }
        }
        throw new IllegalArgumentException("Avaliação não encontrada.");
    }

    public void deletar(String codigo) {
        boolean removida = avaliacoes.removeIf(a -> a.getCodigo().equalsIgnoreCase(codigo));
        if (!removida) {
            throw new IllegalArgumentException("Avaliação não encontrada.");
        }
        salvarDados();
    }

    private void carregarDados() {
        try {
            avaliacoes = JsonUtil.carregarLista(caminhoArquivo, Avaliacao.class);
        } catch (IOException e) {
            System.err.println("Erro ao carregar avaliacões: " + e.getMessage());
            avaliacoes = new ArrayList<>();
        }
    }

    private void salvarDados() {
        try {
            JsonUtil.salvar(caminhoArquivo, avaliacoes);
        } catch (IOException e) {
            throw new IllegalStateException("Erro ao salvar avaliacões: " + e.getMessage(), e);
        }
    }
}
