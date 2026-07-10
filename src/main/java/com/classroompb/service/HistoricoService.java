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

    public List<Historico> listarHistoricoDoAluno(String matriculaAluno) {

        String matricula = validarCampoObrigatorio(matriculaAluno, "matrícula do aluno");
        return repository.buscarPorAluno(matricula);
    }

    public void registrarHistorico(String matriculaAluno, String codigoDisciplina, double notaFinal, boolean aprovado) {

        final String matricula = validarCampoObrigatorio(matriculaAluno, "matrícula do aluno");
        final String disciplina = validarCampoObrigatorio(codigoDisciplina, "código da disciplina");

        List<Historico> historicos = repository.buscarPorAluno(matricula);

        Historico existente = historicos.stream().filter(h -> h.getCodigoDisciplina().equalsIgnoreCase(disciplina))
                .findFirst().orElse(null);

        if (existente != null) {
            existente.setNotaFinal(notaFinal);
            existente.setAprovado(aprovado);
            repository.atualizar(existente);
            return;
        }

        repository.salvar(new Historico(matricula, disciplina, notaFinal, aprovado));
    }

    private String validarCampoObrigatorio(String valor, String campo) {

        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException("Erro: " + campo + " não pode ser vazio.");
        }

        return valor.trim();
    }
}
