package com.classroompb.repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.classroompb.model.Nota;
import com.classroompb.util.JsonUtil;

public class NotaRepository {

    private static final String CAMINHO_PADRAO = "notas.json";

    private final String caminhoArquivo;
    private List<Nota> notas;

    public NotaRepository() {
        this(CAMINHO_PADRAO);
    }

    public NotaRepository(String caminhoArquivo) {
        this.caminhoArquivo = caminhoArquivo;
        carregarDados();
    }

    private void carregarDados() {
        try {
            notas = JsonUtil.carregarLista(caminhoArquivo, Nota.class);
        } catch (IOException e) {
            notas = new ArrayList<>();
        }
    }

    private void salvarDados() {
        try {
            JsonUtil.salvar(caminhoArquivo, notas);
        } catch (IOException e) {
            System.err.println("Erro ao salvar notas: " + e.getMessage());
        }
    }

    public void salvar(Nota nota) {
        if (notas.stream().anyMatch(n -> n.getChaveUnica().equalsIgnoreCase(nota.getChaveUnica()))) {
            throw new IllegalArgumentException("Nota duplicada.");
        }
        notas.add(nota);
        salvarDados();
    }

    public void atualizar(Nota notaAtualizada) {

        for (int i = 0; i < notas.size(); i++) {

            if (notas.get(i).getChaveUnica().equalsIgnoreCase(notaAtualizada.getChaveUnica())) {

                notas.set(i, notaAtualizada);
                salvarDados();
                return;
            }
        }

        throw new IllegalArgumentException("Nota não encontrada.");
    }

    public Nota buscarPorChaveUnica(String matriculaAluno, String codigoDisciplina, String codigoPeriodo,
            String codigoTurma) {

        String chave = matriculaAluno + "_" + codigoDisciplina + "_" + codigoPeriodo + "_" + codigoTurma;

        return notas.stream().filter(n -> n.getChaveUnica().equalsIgnoreCase(chave)).findFirst().orElse(null);
    }

    public List<Nota> listarTodas() {
        return new ArrayList<>(notas);
    }

    public Nota buscarPorAlunoEAvaliacao(String matriculaAluno, String codigoAvaliacao) {
        return notas.stream().filter(n -> n.getCodigoAvaliacao() != null)
                .filter(n -> n.getMatriculaAluno().equalsIgnoreCase(matriculaAluno)
                        && n.getCodigoAvaliacao().equalsIgnoreCase(codigoAvaliacao))
                .findFirst().orElse(null);
    }

    public List<Nota> listarPorAlunoEDiario(String matriculaAluno, String codigoDiario) {
        return notas.stream().filter(n -> n.getCodigoDiario() != null)
                .filter(n -> n.getMatriculaAluno().equalsIgnoreCase(matriculaAluno)
                        && n.getCodigoDiario().equalsIgnoreCase(codigoDiario))
                .collect(java.util.stream.Collectors.toList());
    }

    public List<Nota> listarPorDiario(String codigoDiario) {
        return notas.stream()
                .filter(n -> n.getCodigoDiario() != null && n.getCodigoDiario().equalsIgnoreCase(codigoDiario))
                .collect(java.util.stream.Collectors.toList());
    }
}
