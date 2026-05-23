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

        // RF07/RN04: valida que cada pré-requisito informado existe no sistema
        if (preRequisitos != null) {
            for (String codigoPreReq : preRequisitos) {
                String codigoTrimmed = codigoPreReq.trim();
                if (!codigoTrimmed.isEmpty() && repository.buscarPorCodigo(codigoTrimmed) == null) {
                    throw new Exception(
                            "Erro: Pré-requisito '" + codigoTrimmed + "' não encontrado. Cadastre a disciplina antes de usá-la como pré-requisito."
                    );
                }
            }
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

    public Disciplina buscarPorCodigo(String codigo) throws Exception {

        if (codigo == null || codigo.trim().isEmpty()) {
            throw new Exception("Erro: Codigo nao pode ser vazio.");
        }

        Disciplina d = repository.buscarPorCodigo(codigo);

        if (d == null) {
            throw new Exception("Erro: Disciplina com codigo " + codigo + " nao encontrada.");
        }

        return d;
    }

    public void editarDisciplina(
            String codigo,
            String novoNome,
            int novaCargaHoraria,
            int novosCreditos,
            List<String> novosPreRequisitos
    ) throws Exception {

        Disciplina existente = buscarPorCodigo(codigo);

        if (novoNome == null || novoNome.trim().isEmpty()) {
            throw new Exception("Erro: Nome da disciplina nao pode ser vazio.");
        }
        if (novaCargaHoraria <= 0) {
            throw new Exception("Erro: Carga horaria deve ser maior que zero.");
        }
        if (novosCreditos <= 0) {
            throw new Exception("Erro: Creditos devem ser maiores que zero.");
        }

        // RF07/RN04: valida que cada pré-requisito informado existe no sistema
        if (novosPreRequisitos != null) {
            for (String codigoPreReq : novosPreRequisitos) {
                String codigoTrimmed = codigoPreReq.trim();
                // Um pré-requisito não pode ser a própria disciplina
                if (codigoTrimmed.equalsIgnoreCase(codigo)) {
                    throw new Exception(
                            "Erro: Uma disciplina não pode ser pré-requisito de si mesma.");
                }
                if (!codigoTrimmed.isEmpty() && repository.buscarPorCodigo(codigoTrimmed) == null) {
                    throw new Exception(
                            "Erro: Pré-requisito '" + codigoTrimmed + "' não encontrado.");
                }
            }
        }

        existente.setNome(novoNome);
        existente.setCargaHoraria(novaCargaHoraria);
        existente.setCreditos(novosCreditos);
        existente.setPreRequisitos(novosPreRequisitos);

        repository.atualizar(existente);
    }

    public void deletarDisciplina(String codigo) throws Exception {

        buscarPorCodigo(codigo);
        repository.deletar(codigo);
    }
}