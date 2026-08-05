package com.classroompb.service;

import java.time.LocalDate;
import java.util.List;

import com.classroompb.model.Aula;
import com.classroompb.model.Diario;
import com.classroompb.model.SituacaoDiario;
import com.classroompb.model.SituacaoTurma;
import com.classroompb.model.TipoUsuario;
import com.classroompb.model.Turma;
import com.classroompb.model.Usuario;
import com.classroompb.repository.AulaRepository;
import com.classroompb.repository.DiarioRepository;
import com.classroompb.repository.TurmaRepository;

public class AulaService {

    private final AulaRepository aulaRepository;
    private final DiarioRepository diarioRepository;
    private final TurmaRepository turmaRepository;

    public AulaService(AulaRepository aulaRepository, DiarioRepository diarioRepository) {
        this(aulaRepository, diarioRepository, null);
    }

    public AulaService(AulaRepository aulaRepository, DiarioRepository diarioRepository,
            TurmaRepository turmaRepository) {
        this.aulaRepository = aulaRepository;
        this.diarioRepository = diarioRepository;
        this.turmaRepository = turmaRepository;
    }

    /** Copia o service mantendo os mesmos repositories compartilhados. */
    public AulaService(AulaService origem) {
        this(origem.aulaRepository, origem.diarioRepository, origem.turmaRepository);
    }

    // Cadastra uma nova aula pertencente a um diário.
    public void cadastrarAula(String codigo, String codigoDiario, LocalDate data, String conteudo) throws Exception {
        cadastrarAula(codigo, codigoDiario, data, conteudo, 1.0);
    }

    public void cadastrarAula(String codigo, String codigoDiario, LocalDate data, String conteudo,
            double duracaoHoras) throws Exception {

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

        validarDuracao(duracaoHoras);

        if (aulaRepository.buscarPorCodigo(codigo) != null) {
            throw new Exception("Erro: já existe uma aula com esse código.");
        }

        Diario diario = diarioRepository.buscarPorCodigo(codigoDiario);

        if (diario == null) {
            throw new Exception("Erro: diário inexistente.");
        }

        int numero = aulaRepository.buscarPorDiario(codigoDiario).size() + 1;

        Aula aula = new Aula(codigo, codigoDiario, data, conteudo, numero, duracaoHoras);

        aulaRepository.salvar(aula);
    }

    public void cadastrarAula(Usuario professor, String codigo, String codigoDiario, LocalDate data, String conteudo)
            throws Exception {
        cadastrarAula(professor, codigo, codigoDiario, data, conteudo, 1.0);
    }

    public void cadastrarAula(Usuario professor, String codigo, String codigoDiario, LocalDate data, String conteudo,
            double duracaoHoras) throws Exception {
        Diario diario = validarOperacao(professor, codigoDiario);
        cadastrarAula(codigo, diario.getCodigo(), data, conteudo, duracaoHoras);
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

    public void atualizarAula(Usuario professor, Aula aula) throws Exception {
        if (aula == null) {
            throw new Exception("Erro: aula inválida.");
        }
        validarOperacao(professor, aula.getCodigoDiario());
        if (aula.getNumero() <= 0 || aula.getData() == null || aula.getConteudo() == null
                || aula.getConteudo().isBlank()) {
            throw new Exception("Erro: dados da aula inválidos.");
        }
        validarDuracao(aula.getDuracaoHoras());
        atualizarAula(aula);
    }

    // Remove uma aula.
    public void removerAula(String codigo) throws Exception {

        Aula aula = aulaRepository.buscarPorCodigo(codigo);

        if (aula == null) {
            throw new Exception("Erro: aula inexistente.");
        }

        aulaRepository.deletar(codigo);
    }

    public void removerAula(Usuario professor, String codigo) throws Exception {
        Aula aula = aulaRepository.buscarPorCodigo(codigo);
        if (aula == null) {
            throw new Exception("Erro: aula inexistente.");
        }
        validarOperacao(professor, aula.getCodigoDiario());
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

    private Diario validarOperacao(Usuario professor, String codigoDiario) throws Exception {
        if (professor == null || professor.getTipo() != TipoUsuario.PROFESSOR) {
            throw new Exception("Erro: apenas professores podem gerenciar aulas.");
        }
        if (codigoDiario == null || codigoDiario.isBlank()) {
            throw new Exception("Erro: diário obrigatório.");
        }
        Diario diario = diarioRepository.buscarPorCodigo(codigoDiario.trim());
        if (diario == null) {
            throw new Exception("Erro: diário inexistente.");
        }
        if (!professor.getMatricula().equalsIgnoreCase(diario.getMatriculaProfessor())) {
            throw new Exception("Erro: apenas o professor responsável pelo diário pode gerenciar aulas.");
        }
        if (diario.getSituacao() == SituacaoDiario.ENCERRADO) {
            throw new Exception("Erro: diário fechado não permite alterar aulas.");
        }
        validarTurmaAberta(diario);
        return diario;
    }

    private void validarDuracao(double duracaoHoras) throws Exception {
        if (duracaoHoras <= 0.0 || Double.isNaN(duracaoHoras) || Double.isInfinite(duracaoHoras)) {
            throw new Exception("Erro: duração da aula deve ser maior que zero.");
        }
    }

    private void validarTurmaAberta(Diario diario) throws Exception {
        if (turmaRepository == null) {
            return;
        }
        Turma turma = turmaRepository.buscarPorChaveUnica(diario.getCodigoDisciplina(), diario.getCodigoPeriodo(),
                diario.getCodigoTurma());
        if (turma != null && turma.getSituacao() == SituacaoTurma.ENCERRADA) {
            throw new Exception("Erro: turma encerrada não permite alterar aulas.");
        }
    }
}
