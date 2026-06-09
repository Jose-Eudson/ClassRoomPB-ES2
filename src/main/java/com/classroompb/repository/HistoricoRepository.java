package com.classroompb.repository;

import java.util.ArrayList;
import java.util.List;

import com.classroompb.model.Historico;

public class HistoricoRepository {
        private final List<Historico> historicos =  new ArrayList<>();

        public void salvar(Historico historico) {

                historicos.add(historico);
        }

        public List<Historico> buscarPorAluno(String matriculaAluno) {

                return historicos.stream()
                        .filter(h ->
                                h.getMatriculaAluno()
                                        .equals(matriculaAluno))
                        .toList();
        }
}