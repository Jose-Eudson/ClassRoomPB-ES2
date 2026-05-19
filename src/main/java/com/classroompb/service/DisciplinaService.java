package com.classroompb.service;

import java.util.List;

import com.classroompb.model.Disciplina;
import com.classroompb.repository.DisciplinaRepository;

/**
 * Camada de servico com regras de negocio para cadastro de disciplinas.
 */
public class DisciplinaService {
    private final DisciplinaRepository repository;

    public DisciplinaService(DisciplinaRepository repository) {
        this.repository = repository;
    }

    public void cadastrarDisciplina(String codigo, String nome, int cargaHoraria) throws Exception {
        validarCamposObrigatorios(codigo, nome);
        if (cargaHoraria <= 0) {
            throw new Exception("Erro: Carga horaria deve ser maior que zero.");
        }
        if (repository.existePorCodigo(codigo)) {
            throw new Exception("Erro: Ja existe uma disciplina com este codigo.");
        }

        Disciplina novaDisciplina = new Disciplina(codigo, nome, cargaHoraria);
        repository.salvar(novaDisciplina);
        System.out.println("Disciplina cadastrada com sucesso: " + nome);
    }

    public List<Disciplina> obterTodasDisciplinas() {
        return repository.listarTodos();
    }

    private void validarCamposObrigatorios(String codigo, String nome) throws Exception {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new Exception("Erro: Codigo da disciplina nao pode ser vazio.");
        }
        if (nome == null || nome.trim().isEmpty()) {
            throw new Exception("Erro: Nome da disciplina nao pode ser vazio.");
        }
    }
}
