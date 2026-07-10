package com.classroompb.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.classroompb.model.Historico;

public class HistoricoRepository {
    private final List<Historico> historicos = new ArrayList<>();

    public void salvar(Historico historico) {

        if (historico == null) {
            return;
        }

        historicos.add(historico);
    }

    public void atualizar(Historico historico) {

        if (historico == null) {
            return;
        }

        for (int i = 0; i < historicos.size(); i++) {
            Historico atual = historicos.get(i);
            if (atual.getMatriculaAluno().equalsIgnoreCase(historico.getMatriculaAluno())
                    && atual.getCodigoDisciplina().equalsIgnoreCase(historico.getCodigoDisciplina())) {
                historicos.set(i, historico);
                return;
            }
        }

        historicos.add(historico);
    }

    public List<Historico> buscarPorAluno(String matriculaAluno) {

        return historicos.stream().filter(h -> h.getMatriculaAluno().equals(matriculaAluno))
                .collect(Collectors.toList());
    }
}
