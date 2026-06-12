package com.classroompb.service;

import java.util.List;

import com.classroompb.model.Historico;
import com.classroompb.repository.HistoricoRepository;

public class HistoricoService {

    private final HistoricoRepository repository;

    public HistoricoService(HistoricoRepository repository) {

        this.repository = repository;
    }

    public boolean alunoFoiAprovado(String matriculaAluno, String codigoDisciplina) {

        List<Historico> historicos = repository.buscarPorAluno(matriculaAluno);

        return historicos.stream()
                .anyMatch(h -> h.getCodigoDisciplina().equalsIgnoreCase(codigoDisciplina) && h.isAprovado());
    }
}
