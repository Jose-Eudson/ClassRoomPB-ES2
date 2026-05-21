package com.classroompb.service;

import java.util.List;

import com.classroompb.model.Disciplina;
import com.classroompb.repository.DisciplinaRepository;

public class DisciplinaService {

    private final DisciplinaRepository repository;

    public DisciplinaService(
            DisciplinaRepository repository
    ) {
        this.repository = repository;
    }

    public void cadastrarDisciplina(
            String codigo,
            String nome,
            int cargaHoraria,
            int creditos,
            List<String> preRequisitos
    ) throws Exception {

        if (codigo == null || codigo.trim().isEmpty()) {

            throw new Exception(
                    "Erro: Codigo da disciplina nao pode ser vazio."
            );
        }

        if (nome == null || nome.trim().isEmpty()) {

            throw new Exception(
                    "Erro: Nome da disciplina nao pode ser vazio."
            );
        }

        if (cargaHoraria <= 0) {

            throw new Exception(
                    "Erro: Carga horaria deve ser maior que zero."
            );
        }

        if (creditos <= 0) {

            throw new Exception(
                    "Erro: Creditos devem ser maiores que zero."
            );
        }

        if (repository.existePorCodigo(codigo)) {

            throw new Exception(
                    "Erro: Ja existe uma disciplina com este codigo."
            );
        }

        Disciplina disciplina =
                new Disciplina(
                        codigo,
                        nome,
                        cargaHoraria,
                        creditos,
                        preRequisitos
                );

        repository.salvar(disciplina);
    }

    public List<Disciplina> listarDisciplinas() {

        return repository.listarTodos();
    }
}
