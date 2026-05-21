package com.classroompb.repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.classroompb.model.PeriodoLetivo;
import com.classroompb.util.JsonUtil;

public class PeriodoLetivoRepository {

    private static final String CAMINHO_PADRAO = "periodos_letivos.json";

    private final String caminhoArquivo;

    private List<PeriodoLetivo> periodos;

    public PeriodoLetivoRepository() {
        this(CAMINHO_PADRAO);
    }

    public PeriodoLetivoRepository(String caminhoArquivo) {

        this.caminhoArquivo = caminhoArquivo;

        carregarDados();
    }

    private void carregarDados() {

        try {

            this.periodos =
                    JsonUtil.carregarLista(
                            caminhoArquivo,
                            PeriodoLetivo.class
                    );

        } catch (IOException e) {

            System.err.println(
                    "Erro ao carregar periodos: "
                            + e.getMessage()
            );

            this.periodos = new ArrayList<>();
        }
    }

    public void atualizarDados() {

        salvarDados();
    }

    private void salvarDados() {

        try {

            JsonUtil.salvar(
                    caminhoArquivo,
                    periodos
            );

        } catch (IOException e) {

            System.err.println(
                    "Erro ao salvar periodos: "
                            + e.getMessage()
            );
        }
    }

    public void salvar(PeriodoLetivo periodo) {

        periodos.add(periodo);

        salvarDados();
    }

    public List<PeriodoLetivo> listarTodos() {

        return new ArrayList<>(periodos);
    }

    public boolean existePorCodigo(String codigo) {

        return periodos.stream()
                .anyMatch(
                        p -> p.getCodigo()
                                .equalsIgnoreCase(codigo)
                );
    }

    public PeriodoLetivo buscarPorCodigo(String codigo) {

        return periodos.stream()
                .filter(
                        p -> p.getCodigo()
                                .equalsIgnoreCase(codigo)
                )
                .findFirst()
                .orElse(null);
    }
}