package com.classroompb.repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.classroompb.model.Historico;
import com.classroompb.util.JsonUtil;

public class HistoricoRepository {
    private static final String CAMINHO_PADRAO = "historicos.json";
    private final String caminhoArquivo;
    private List<Historico> historicos;

    public HistoricoRepository() {
        this(CAMINHO_PADRAO);
    }

    public HistoricoRepository(String caminhoArquivo) {
        this.caminhoArquivo = caminhoArquivo;
        carregarDados();
    }

    private void carregarDados() {
        try {
            historicos = JsonUtil.carregarLista(caminhoArquivo, Historico.class);
        } catch (IOException e) {
            System.err.println("Erro ao carregar históricos: " + e.getMessage());
            historicos = new ArrayList<>();
        }
    }

    private void salvarDados() {
        try {
            JsonUtil.salvar(caminhoArquivo, historicos);
        } catch (IOException e) {
            System.err.println("Erro ao salvar históricos: " + e.getMessage());
        }
    }

    public void salvar(Historico historico) {

        if (historico == null) {
            return;
        }

        atualizar(historico);
    }

    public void atualizar(Historico historico) {

        if (historico == null) {
            return;
        }

        for (int i = 0; i < historicos.size(); i++) {
            Historico atual = historicos.get(i);
            if (mesmaChave(atual, historico)) {
                historicos.set(i, historico);
                salvarDados();
                return;
            }
        }

        historicos.add(historico);
        salvarDados();
    }

    public List<Historico> listarTodos() {
        return new ArrayList<>(historicos);
    }

    public List<Historico> buscarPorAluno(String matriculaAluno) {

        if (matriculaAluno == null) {
            return new ArrayList<>();
        }
        Comparator<String> texto = Comparator.nullsFirst(String.CASE_INSENSITIVE_ORDER);
        return historicos.stream().filter(h -> matriculaAluno.equalsIgnoreCase(h.getMatriculaAluno()))
                .sorted(Comparator.comparing(Historico::getCodigoPeriodo, texto)
                        .thenComparing(Historico::getNomeDisciplina, texto)
                        .thenComparing(Historico::getCodigoDisciplina, texto))
                .collect(Collectors.toList());
    }

    private boolean mesmaChave(Historico primeiro, Historico segundo) {
        return iguais(primeiro.getMatriculaAluno(), segundo.getMatriculaAluno())
                && iguais(primeiro.getCodigoDisciplina(), segundo.getCodigoDisciplina())
                && iguais(primeiro.getCodigoPeriodo(), segundo.getCodigoPeriodo())
                && iguais(primeiro.getCodigoTurma(), segundo.getCodigoTurma());
    }

    private boolean iguais(String primeiro, String segundo) {
        return primeiro == null ? segundo == null : segundo != null && primeiro.equalsIgnoreCase(segundo);
    }
}
