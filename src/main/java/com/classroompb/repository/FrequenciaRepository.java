package com.classroompb.repository;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.classroompb.model.RegistroFrequencia;
import com.classroompb.util.JsonUtil;

/**
 * RF27: Repositorio responsavel pela persistencia dos registros de frequencia em arquivo JSON.
 */
public class FrequenciaRepository {

    private static final String CAMINHO_PADRAO = "frequencias.json";

    private final String caminhoArquivo;
    private List<RegistroFrequencia> frequencias;

    public FrequenciaRepository() {
        this(CAMINHO_PADRAO);
    }

    public FrequenciaRepository(String caminhoArquivo) {
        this.caminhoArquivo = caminhoArquivo;
        carregarDados();
    }

    private void carregarDados() {
        try {
            this.frequencias = JsonUtil.carregarLista(caminhoArquivo, RegistroFrequencia.class);
        } catch (IOException e) {
            System.err.println("Erro ao carregar frequencias: " + e.getMessage());
            this.frequencias = new ArrayList<>();
        }
    }

    private void salvarDados() {
        try {
            JsonUtil.salvar(caminhoArquivo, frequencias);
        } catch (IOException e) {
            System.err.println("Erro ao salvar frequencias: " + e.getMessage());
        }
    }

    /** Persiste um novo registro de frequencia. */
    public void salvar(RegistroFrequencia frequencia) {
        frequencias.add(frequencia);
        salvarDados();
    }

    /** Retorna copia defensiva de todos os registros. */
    public List<RegistroFrequencia> listarTodas() {
        return new ArrayList<>(frequencias);
    }

    /** Busca um registro pela chave unica composta. */
    public RegistroFrequencia buscarPorChaveUnica(String matriculaAluno, String codigoDisciplina, String codigoPeriodo,
            String codigoTurma, LocalDate dataAula) {
        String chave = matriculaAluno + "_" + codigoDisciplina + "_" + codigoPeriodo + "_" + codigoTurma + "_"
                + dataAula;
        return frequencias.stream().filter(f -> f.getChaveUnica().equalsIgnoreCase(chave)).findFirst().orElse(null);
    }

    /** Lista os registros de uma turma em uma aula especifica. */
    public List<RegistroFrequencia> listarPorTurmaEData(String codigoDisciplina, String codigoPeriodo,
            String codigoTurma, LocalDate dataAula) {
        return frequencias.stream()
                .filter(f -> f.getCodigoDisciplina().equalsIgnoreCase(codigoDisciplina)
                        && f.getCodigoPeriodo().equalsIgnoreCase(codigoPeriodo)
                        && f.getCodigoTurma().equalsIgnoreCase(codigoTurma) && f.getDataAula().equals(dataAula))
                .collect(Collectors.toList());
    }

    /** Lista todos os registros de frequencia de um aluno. */
    public List<RegistroFrequencia> listarPorAluno(String matriculaAluno) {
        return frequencias.stream().filter(f -> f.getMatriculaAluno().equalsIgnoreCase(matriculaAluno))
                .collect(Collectors.toList());
    }

    public List<RegistroFrequencia> listarPorAlunoETurma(String matriculaAluno, String codigoDisciplina,
            String codigoPeriodo, String codigoTurma) {
        return frequencias.stream()
                .filter(f -> f.getMatriculaAluno().equalsIgnoreCase(matriculaAluno)
                        && f.getCodigoDisciplina().equalsIgnoreCase(codigoDisciplina)
                        && f.getCodigoPeriodo().equalsIgnoreCase(codigoPeriodo)
                        && f.getCodigoTurma().equalsIgnoreCase(codigoTurma))
                .collect(Collectors.toList());
    }

    /** Atualiza um registro existente identificado pela chave unica composta. */
    public void atualizar(RegistroFrequencia atualizada) {
        for (int i = 0; i < frequencias.size(); i++) {
            if (frequencias.get(i).getChaveUnica().equalsIgnoreCase(atualizada.getChaveUnica())) {
                frequencias.set(i, atualizada);
                salvarDados();
                return;
            }
        }
        throw new IllegalArgumentException(
                "Registro de frequencia com chave " + atualizada.getChaveUnica() + " nao encontrado.");
    }
}
