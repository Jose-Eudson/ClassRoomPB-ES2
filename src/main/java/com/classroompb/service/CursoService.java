package com.classroompb.service;

import java.util.List;

import com.classroompb.model.Curso;
import com.classroompb.repository.CursoRepository;

/**
 * Camada de serviço com regras de negócio para cadastro de cursos.
 */
public class CursoService {
    private final CursoRepository repository;

    public CursoService(CursoRepository repository) {
        this.repository = repository;
    }

    public void cadastrarCurso(String codigo, String nome, int cargaHoraria) throws Exception {
        validarCamposObrigatorios(codigo, nome);
        if (cargaHoraria <= 0) {
            throw new Exception("Erro: Carga horária deve ser maior que zero.");
        }
        if (repository.existePorCodigo(codigo)) {
            throw new Exception("Erro: Já existe um curso com este código.");
        }

        Curso novoCurso = new Curso(codigo, nome, cargaHoraria);
        repository.salvar(novoCurso);
        System.out.println("Curso cadastrado com sucesso: " + nome);
    }

    public List<Curso> obterTodosCursos() {
        return repository.listarTodos();
    }

    private void validarCamposObrigatorios(String codigo, String nome) throws Exception {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new Exception("Erro: Código do curso não pode ser vazio.");
        }
        if (nome == null || nome.trim().isEmpty()) {
            throw new Exception("Erro: Nome do curso não pode ser vazio.");
        }
    }
}
