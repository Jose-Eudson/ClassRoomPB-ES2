package com.classroompb.service;

import java.util.ArrayList;
import java.util.List;

import com.classroompb.model.Disciplina;
import com.classroompb.model.TipoUsuario;
import com.classroompb.model.Usuario;
import com.classroompb.repository.DisciplinaRepository;

public class DisciplinaService {

    private final DisciplinaRepository repository;

    public DisciplinaService(DisciplinaRepository repository) {
        this.repository = repository;
    }

    public void cadastrarDisciplina(Usuario coordenador, String codigo, String nome, int cargaHoraria, int creditos,
            List<String> preRequisitos) throws Exception {

        if (coordenador == null || coordenador.getTipo() != TipoUsuario.COORDENADOR) {
            throw new Exception("Erro: Apenas coordenadores podem cadastrar disciplinas.");
        }

        if (codigo == null || codigo.trim().isEmpty()) {

            throw new Exception("Erro: Codigo da disciplina nao pode ser vazio.");
        }

        if (nome == null || nome.trim().isEmpty()) {

            throw new Exception("Erro: Nome da disciplina nao pode ser vazio.");
        }

        if (cargaHoraria <= 0) {

            throw new Exception("Erro: Carga horaria deve ser maior que zero.");
        }

        if (creditos <= 0) {

            throw new Exception("Erro: Creditos devem ser maiores que zero.");
        }

        if (repository.existePorCodigo(codigo)) {

            throw new Exception("Erro: Ja existe uma disciplina com este codigo.");
        }

        // RF07/RN04: valida que cada pré-requisito informado existe no sistema
        // (aceita tanto o código quanto o nome da disciplina) e normaliza para o código
        List<String> preRequisitosNormalizados = resolverPreRequisitos(preRequisitos);

        Disciplina disciplina = new Disciplina(codigo, nome, cargaHoraria, creditos, preRequisitosNormalizados);

        repository.salvar(disciplina);
    }

    /**
     * Resolve a lista de pré-requisitos informados (pelo código OU pelo nome da disciplina) validando que cada um já
     * existe cadastrado no sistema. Retorna a lista normalizada usando sempre o código da disciplina, para manter a
     * consistência dos dados persistidos.
     */
    private List<String> resolverPreRequisitos(List<String> preRequisitos) throws Exception {

        if (preRequisitos == null) {
            return null;
        }

        List<String> normalizados = new ArrayList<>();

        for (String entrada : preRequisitos) {

            if (entrada == null) {
                continue;
            }

            String valorTrimmed = entrada.trim();

            if (valorTrimmed.isEmpty()) {
                continue;
            }

            Disciplina encontrada = repository.buscarPorCodigo(valorTrimmed);

            if (encontrada == null) {
                final String valorBusca = valorTrimmed;
                encontrada = repository.listarTodos().stream()
                        .filter(d -> d.getNome() != null && d.getNome().equalsIgnoreCase(valorBusca)).findFirst()
                        .orElse(null);
            }

            if (encontrada == null) {
                throw new Exception("Erro: Pré-requisito '" + valorTrimmed
                        + "' não encontrado. Cadastre a disciplina antes de usá-la como pré-requisito "
                        + "(informe o código ou o nome exato da disciplina).");
            }

            normalizados.add(encontrada.getCodigo());
        }

        return normalizados;
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

    public void editarDisciplina(Usuario coordenador, String codigo, String novoNome, int novaCargaHoraria,
            int novosCreditos, List<String> novosPreRequisitos) throws Exception {

        if (coordenador == null || coordenador.getTipo() != TipoUsuario.COORDENADOR) {
            throw new Exception("Erro: Apenas coordenadores podem editar disciplinas.");
        }

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
        // (aceita tanto o código quanto o nome da disciplina) e normaliza para o código
        if (novosPreRequisitos != null) {
            for (String codigoPreReq : novosPreRequisitos) {
                // Um pré-requisito não pode ser a própria disciplina
                if (codigoPreReq.trim().equalsIgnoreCase(codigo)) {
                    throw new Exception("Erro: Uma disciplina não pode ser pré-requisito de si mesma.");
                }
            }
        }
        List<String> novosPreRequisitosNormalizados = resolverPreRequisitos(novosPreRequisitos);

        existente.setNome(novoNome);
        existente.setCargaHoraria(novaCargaHoraria);
        existente.setCreditos(novosCreditos);
        existente.setPreRequisitos(novosPreRequisitosNormalizados);

        repository.atualizar(existente);
    }

    public void deletarDisciplina(Usuario coordenador, String codigo) throws Exception {

        if (coordenador == null || coordenador.getTipo() != TipoUsuario.COORDENADOR) {
            throw new Exception("Erro: Apenas coordenadores podem deletar disciplinas.");
        }

        buscarPorCodigo(codigo);
        repository.deletar(codigo);
    }
}
