package com.classroompb.service;

import java.util.List;

import com.classroompb.model.Diario;
import com.classroompb.model.TipoUsuario;
import com.classroompb.model.SituacaoDiario;
import com.classroompb.model.Turma;
import com.classroompb.model.Usuario;
import com.classroompb.repository.DiarioRepository;
import com.classroompb.repository.TurmaRepository;
import com.classroompb.repository.UsuarioRepository;

public class DiarioService {

    private final DiarioRepository diarioRepository;
    private final TurmaRepository turmaRepository;
    private final UsuarioRepository usuarioRepository;

    public DiarioService(DiarioRepository diarioRepository, TurmaRepository turmaRepository,
            UsuarioRepository usuarioRepository) {

        this.diarioRepository = diarioRepository;
        this.turmaRepository = turmaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public void cadastrarDiario(

            String codigo, String codigoTurma, String codigoDisciplina, String codigoPeriodo, String descricao,
            String matriculaProfessor, String horario, String sala, int cargaHoraria) throws Exception {

        if (codigo == null || codigo.isBlank()) {

            throw new Exception("Erro: código obrigatório.");
        }

        if (diarioRepository.buscarPorCodigo(codigo) != null) {

            throw new Exception("Erro: já existe um diário com esse código.");
        }

        Turma turma = turmaRepository.buscarPorChaveUnica(codigoDisciplina, codigoPeriodo, codigoTurma);

        if (turma == null) {

            throw new Exception("Erro: turma inexistente.");
        }

        Usuario professor = usuarioRepository.buscarPorMatricula(matriculaProfessor).orElseThrow(
                () -> new Exception("Erro: Professor com matrícula '" + matriculaProfessor + "' não encontrado."));
        if (professor.getTipo() != TipoUsuario.PROFESSOR) {
            throw new Exception("Erro: O usuário '" + matriculaProfessor + "' não é um professor.");
        }

        if (professor.getTipo() != TipoUsuario.PROFESSOR) {

            throw new Exception("Erro: o usuário informado não é professor.");
        }

        if (cargaHoraria <= 0) {

            throw new Exception("Erro: carga horária inválida.");
        }

        Diario diario = new Diario(

                codigo, codigoTurma, descricao, matriculaProfessor, horario, sala, cargaHoraria, SituacaoDiario.ATIVO);

        diarioRepository.salvar(diario);
    }

    public void editarDiario(Diario diario) throws Exception {

        if (diarioRepository.buscarPorCodigo(diario.getCodigo()) == null) {

            throw new Exception("Erro: diário não encontrado.");
        }

        diarioRepository.atualizar(diario);
    }

    public Diario buscarPorCodigo(String codigo) {

        return diarioRepository.buscarPorCodigo(codigo);
    }

    public List<Diario> listarDiarios() {

        return diarioRepository.listarTodos();
    }

    public List<Diario> listarPorTurma(String codigoTurma) {

        return diarioRepository.buscarPorTurma(codigoTurma);
    }

    public List<Diario> listarPorProfessor(String matriculaProfessor) {

        return diarioRepository.buscarPorProfessor(matriculaProfessor);
    }
}
