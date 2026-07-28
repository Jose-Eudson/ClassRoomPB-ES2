package com.classroompb.ui;

import com.classroompb.model.Diario;

import com.classroompb.service.DiarioService;

import java.util.List;

public class DiarioController {

    private final DiarioService diarioService;

    public DiarioController(DiarioService diarioService) {
        this.diarioService = diarioService;
    }

    public void cadastrarDiario(String codigo, String codigoTurma, String codigoDisciplina, String codigoPeriodo,
            String descricao, String matriculaProfessor, String horario, String sala, int cargaHoraria)
            throws Exception {

        diarioService.cadastrarDiario(codigo, codigoTurma, codigoDisciplina, codigoPeriodo, descricao,
                matriculaProfessor, horario, sala, cargaHoraria);
    }

    public List<Diario> listarTodos() {
        return diarioService.listarDiarios();
    }

    public List<Diario> buscarPorTurma(String codigoTurma) {
        return diarioService.buscarPorTurma(codigoTurma);
    }

    public List<Diario> listarPorProfessor(String matricula) {
        return diarioService.listarPorProfessor(matricula);
    }
}
