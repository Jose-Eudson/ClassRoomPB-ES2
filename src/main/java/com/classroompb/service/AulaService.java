package com.classroompb.service;

import java.time.LocalDate;
import java.util.List;

import com.classroompb.model.Aula;
import com.classroompb.model.Diario;
import com.classroompb.repository.AulaRepository;
import com.classroompb.repository.DiarioRepository;

public class AulaService {

    private final AulaRepository aulaRepository;
    private final DiarioRepository diarioRepository;

    public AulaService(AulaRepository aulaRepository, DiarioRepository diarioRepository) {

        this.aulaRepository = aulaRepository;
        this.diarioRepository = diarioRepository;
    }

    // Cadastra uma nova aula pertencente a um diário.
    public void cadastrarAula(String codigo, String codigoDiario, LocalDate data, String conteudo) throws Exception {

        if (codigo == null || codigo.isBlank()) {
            throw new Exception("Erro: código da aula obrigatório.");
        }

        if (codigoDiario == null || codigoDiario.isBlank()) {
            throw new Exception("Erro: diário obrigatório.");
        }

        if (data == null) {
            throw new Exception("Erro: data da aula obrigatória.");
        }

        if (conteudo == null || conteudo.isBlank()) {
            throw new Exception("Erro: conteúdo da aula obrigatório.");
        }

        if (aulaRepository.buscarPorCodigo(codigo) != null) {
            throw new Exception("Erro: já existe uma aula com esse código.");
        }

        Diario diario = diarioRepository.buscarPorCodigo(codigoDiario);

        if (diario == null) {
            throw new Exception("Erro: diário inexistente.");
        }

        int numero = aulaRepository.buscarPorDiario(codigoDiario).size() + 1;

        Aula aula = new Aula(codigo, codigoDiario, data, conteudo, numero);

        aulaRepository.salvar(aula);
    }

    // Lista todas as aulas cadastradas.
    public List<Aula> listarAulas() {
        return aulaRepository.listarTodas();
    }

    // Busca uma aula pelo código.
    public Aula buscarPorCodigo(String codigo) {
        return aulaRepository.buscarPorCodigo(codigo);
    }

    // Lista todas as aulas de um diário.
    public List<Aula> listarPorDiario(String codigoDiario) {
        return aulaRepository.buscarPorDiario(codigoDiario);
    }

    // Atualiza uma aula.
    public void atualizarAula(Aula aula) throws Exception {

        if (aula == null) {
            throw new Exception("Erro: aula inválida.");
        }

        Aula existente = aulaRepository.buscarPorCodigo(aula.getCodigo());

        if (existente == null) {
            throw new Exception("Erro: aula inexistente.");
        }

        aulaRepository.atualizar(aula);
    }

    // Remove uma aula.
    public void removerAula(String codigo) throws Exception {

        Aula aula = aulaRepository.buscarPorCodigo(codigo);

        if (aula == null) {
            throw new Exception("Erro: aula inexistente.");
        }

        aulaRepository.deletar(codigo);
    }

    // Verifica se um diário possui pelo menos uma aula.
    public boolean diarioPossuiAulas(String codigoDiario) {
        return !aulaRepository.buscarPorDiario(codigoDiario).isEmpty();
    }

    // Retorna a quantidade de aulas cadastradas em um diário.
    public int quantidadeAulas(String codigoDiario) {
        return aulaRepository.buscarPorDiario(codigoDiario).size();
    }
}
