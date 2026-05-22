package com.classroompb.repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.classroompb.model.Turma;
import com.classroompb.util.JsonUtil;

/**
 * RF10: Repositório responsável pela persistência de turmas em arquivo JSON.
 * Segue o mesmo padrão dos demais repositórios do sistema.
 */
public class TurmaRepository {

    private static final String CAMINHO_PADRAO = "turmas.json";

    private final String caminhoArquivo;

    private List<Turma> turmas;

    public TurmaRepository() {
        this(CAMINHO_PADRAO);
    }

    public TurmaRepository(String caminhoArquivo) {
        this.caminhoArquivo = caminhoArquivo;
        carregarDados();
    }

    private void carregarDados() {
        try {
            this.turmas = JsonUtil.carregarLista(caminhoArquivo, Turma.class);
        } catch (IOException e) {
            System.err.println("Erro ao carregar turmas: " + e.getMessage());
            this.turmas = new ArrayList<>();
        }
    }

    private void salvarDados() {
        try {
            JsonUtil.salvar(caminhoArquivo, turmas);
        } catch (IOException e) {
            System.err.println("Erro ao salvar turmas: " + e.getMessage());
        }
    }

    /** Persiste uma nova turma. */
    public void salvar(Turma turma) {
        turmas.add(turma);
        salvarDados();
    }

    /** Retorna cópia defensiva de todas as turmas. */
    public List<Turma> listarTodos() {
        return new ArrayList<>(turmas);
    }

    /**
     * Verifica se já existe uma turma com a mesma chave única
     * (codigoDisciplina + codigoPeriodo + codigo).
     */
    public boolean existePorChaveUnica(String codigoDisciplina, String codigoPeriodo, String codigo) {
        String chave = codigoDisciplina + "_" + codigoPeriodo + "_" + codigo;
        return turmas.stream()
                .anyMatch(t -> t.getChaveUnica().equalsIgnoreCase(chave));
    }

    /** Retorna todas as turmas de uma disciplina em um determinado período. */
    public List<Turma> listarPorDisciplinaEPeriodo(String codigoDisciplina, String codigoPeriodo) {
        return turmas.stream()
                .filter(t -> t.getCodigoDisciplina().equalsIgnoreCase(codigoDisciplina)
                          && t.getCodigoPeriodo().equalsIgnoreCase(codigoPeriodo))
                .collect(Collectors.toList());
    }

    /** Retorna todas as turmas de um determinado período. */
    public List<Turma> listarPorPeriodo(String codigoPeriodo) {
        return turmas.stream()
                .filter(t -> t.getCodigoPeriodo().equalsIgnoreCase(codigoPeriodo))
                .collect(Collectors.toList());
    }

    /**
     * Busca uma turma pela chave única composta.
     * Retorna null se não encontrada.
     */
    public Turma buscarPorChaveUnica(String codigoDisciplina, String codigoPeriodo, String codigo) {
        String chave = codigoDisciplina + "_" + codigoPeriodo + "_" + codigo;
        return turmas.stream()
                .filter(t -> t.getChaveUnica().equalsIgnoreCase(chave))
                .findFirst()
                .orElse(null);
    }
}