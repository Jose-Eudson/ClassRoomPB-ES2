package com.classroompb.service;

import java.time.LocalDate;

import com.classroompb.model.PeriodoLetivo;
import com.classroompb.model.TipoUsuario;
import com.classroompb.model.Usuario;
import com.classroompb.repository.PeriodoLetivoRepository;

public class PeriodoLetivoService {

    private final PeriodoLetivoRepository repository;

    public PeriodoLetivoService(PeriodoLetivoRepository repository) {
        this.repository = repository;
    }

    public void cadastrarPeriodo(String codigo, int ano, int semestre, LocalDate dataInicio, LocalDate dataFim,
            boolean ativo) throws Exception {

        if (codigo == null || codigo.trim().isEmpty()) {

            throw new Exception("Erro: Codigo do periodo nao pode ser vazio.");
        }

        if (semestre != 1 && semestre != 2) {

            throw new Exception("Erro: Semestre deve ser 1 ou 2.");
        }

        if (dataInicio.isAfter(dataFim)) {

            throw new Exception("Erro: Data inicial nao pode ser maior que data final.");
        }

        if (repository.existePorCodigo(codigo)) {

            throw new Exception("Erro: Ja existe um periodo com este codigo.");
        }

        PeriodoLetivo periodo = new PeriodoLetivo(codigo, ano, semestre, dataInicio, dataFim, ativo);

        repository.salvar(periodo);
    }

    public void ativarPeriodo(Usuario usuario, String codigo) throws Exception {

        if (usuario.getTipo() != TipoUsuario.COORDENADOR) {
            throw new Exception("Erro: Apenas coordenadores podem ativar periodos.");
        }

        PeriodoLetivo periodo = repository.buscarPorCodigo(codigo);

        if (periodo == null) {

            throw new Exception("Erro: Periodo nao encontrado.");
        }

        /*
         * Regra: apenas um periodo pode ficar ativo
         */
        for (PeriodoLetivo p : repository.listarTodos()) {

            p.setAtivo(false);
        }

        periodo.setAtivo(true);

        repository.atualizarDados();
    }

    public void encerrarPeriodo(Usuario usuario, String codigo) throws Exception {

        if (usuario.getTipo() != TipoUsuario.COORDENADOR) {
            throw new Exception("Erro: Apenas coordenadores podem encerrar periodos.");
        }

        PeriodoLetivo periodo = repository.buscarPorCodigo(codigo);

        if (periodo == null) {
            throw new Exception("Erro: Periodo nao encontrado.");
        }

        periodo.setAtivo(false);

        repository.atualizarDados();
    }

    public java.util.List<PeriodoLetivo> listarPeriodos() {
        return repository.listarTodos();
    }

    public PeriodoLetivo buscarPorCodigo(String codigo) throws Exception {
        PeriodoLetivo periodo = repository.buscarPorCodigo(codigo);
        if (periodo == null) {
            throw new Exception("Erro: Periodo nao encontrado.");
        }
        return periodo;
    }

    public void editarPeriodo(String codigo, LocalDate novaDataInicio, LocalDate novaDataFim) throws Exception {
        PeriodoLetivo periodo = repository.buscarPorCodigo(codigo);
        if (periodo == null) {
            throw new Exception("Erro: Periodo nao encontrado.");
        }
        if (novaDataInicio.isAfter(novaDataFim)) {
            throw new Exception("Erro: Data inicial nao pode ser maior que data final.");
        }
        periodo.setDataInicio(novaDataInicio);
        periodo.setDataFim(novaDataFim);
        repository.atualizarDados();
    }
}
