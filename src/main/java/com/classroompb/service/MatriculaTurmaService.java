package com.classroompb.service;

import java.util.List;

import com.classroompb.model.Aluno;
import com.classroompb.model.Disciplina;
import com.classroompb.model.MatriculaTurma;
import com.classroompb.model.PeriodoLetivo;
import com.classroompb.model.StatusMatricula;
import com.classroompb.model.TipoUsuario;
import com.classroompb.model.Turma;
import com.classroompb.model.Usuario;
import com.classroompb.repository.DisciplinaRepository;
import com.classroompb.repository.HistoricoRepository;
import com.classroompb.repository.MatriculaTurmaRepository;

import com.classroompb.repository.PeriodoLetivoRepository;
import com.classroompb.repository.TurmaRepository;
import java.time.LocalDate;

/**
 * RF16: Serviço responsável pela solicitação e gestão de matrículas de alunos
 * em turmas.
 *
 * Regras de negócio aplicadas:
 * - Apenas alunos podem solicitar matrícula.
 * - A turma informada deve existir.
 * - O período letivo da turma deve estar ativo.
 * - Não é permitido ao aluno solicitar matrícula na mesma turma mais de uma vez
 * (solicitações PENDENTE ou CONFIRMADA bloqueiam nova solicitação).
 * - Não é possível solicitar matrícula se não houver vagas disponíveis
 * (vagas - confirmadas <= 0).
 * - O aluno só pode cancelar suas próprias solicitações.
 * - Apenas solicitações com status PENDENTE podem ser canceladas pelo aluno.
 */
public class MatriculaTurmaService {

    private final MatriculaTurmaRepository matriculaRepository;
    private final TurmaRepository turmaRepository;
    private final PeriodoLetivoRepository periodoRepository;
    private final DisciplinaRepository disciplinaRepository;
    private final HistoricoRepository historicoRepository;

    private HistoricoService historicoService;

    public MatriculaTurmaService(
            MatriculaTurmaRepository matriculaRepository,
            TurmaRepository turmaRepository,
            PeriodoLetivoRepository periodoRepository,
            DisciplinaRepository disciplinaRepository,
            HistoricoRepository historicoRepository) {
        this.matriculaRepository = matriculaRepository;
        this.turmaRepository = turmaRepository;
        this.periodoRepository = periodoRepository;
        this.disciplinaRepository = disciplinaRepository;
        this.historicoRepository = historicoRepository;

        this.historicoService = new HistoricoService(historicoRepository);
    }

    // =========================================================================
    // RF16 — Solicitar matrícula
    // =========================================================================

    /**
     * Registra uma solicitação de matrícula de um aluno em uma turma.
     *
     * @param aluno            usuário que está realizando a operação (deve ser
     *                         ALUNO)
     * @param codigoDisciplina código da disciplina da turma
     * @param codigoPeriodo    código do período letivo
     * @param codigoTurma      código da turma
     * @throws Exception se qualquer regra de negócio for violada
     */
    public StatusMatricula solicitarMatricula(
            Usuario aluno,
            String codigoDisciplina,
            String codigoPeriodo,
            String codigoTurma) throws Exception {

        // Permissão: apenas alunos
        if (aluno == null || aluno.getTipo() != TipoUsuario.ALUNO) {
            throw new Exception("Erro: Apenas alunos podem solicitar matrícula em turmas.");
        }

        // Validação: campos obrigatórios
        if (codigoDisciplina == null || codigoDisciplina.trim().isEmpty()) {
            throw new Exception("Erro: Código da disciplina não pode ser vazio.");
        }
        if (codigoPeriodo == null || codigoPeriodo.trim().isEmpty()) {
            throw new Exception("Erro: Código do período letivo não pode ser vazio.");
        }
        if (codigoTurma == null || codigoTurma.trim().isEmpty()) {
            throw new Exception("Erro: Código da turma não pode ser vazio.");
        }

        String discNorm = codigoDisciplina.trim();
        String periodoNorm = codigoPeriodo.trim();
        String turmaNorm = codigoTurma.trim();

        // Regra: turma deve existir
        Turma turma = turmaRepository.buscarPorChaveUnica(discNorm, periodoNorm, turmaNorm);
        if (turma == null) {
            throw new Exception(
                    "Erro: Turma '" + turmaNorm
                            + "' da disciplina '" + discNorm
                            + "' no período '" + periodoNorm + "' não encontrada.");
        }

        // Regra: período letivo deve estar ativo
        PeriodoLetivo periodo = periodoRepository.buscarPorCodigo(periodoNorm);
        if (periodo == null || !periodo.isAtivo()) {
            throw new Exception(
                    "Erro: Não é possível solicitar matrícula em turmas de um período letivo inativo.");
        }

        // Regra: aluno não pode solicitar a mesma turma mais de uma vez
        if (matriculaRepository.existeSolicitacaoAtiva(
                aluno.getMatricula(), discNorm, periodoNorm, turmaNorm)) {
            throw new Exception(
                    "Erro: Você já possui uma solicitação ativa para a turma '"
                            + turmaNorm + "' da disciplina '" + discNorm + "'.");
        }

        // RF18 - verificar disciplina e pré-requisitos
        Disciplina disciplina = disciplinaRepository.buscarPorCodigo(discNorm);

        if (disciplina == null) {
            throw new Exception(
                    "Erro: Disciplina '" + discNorm + "' não encontrada.");
        }

        validarPreRequisitos((Aluno) aluno, disciplina);

        // RF19 - impedir choque de horário
        validarChoqueHorario((Aluno) aluno, turma);

        // RF21 - caso não haja vaga, aluno entra em lista de espera
        long vagasOcupadas = matriculaRepository.contarOcupadasPorTurma(
                discNorm,
                periodoNorm,
                turmaNorm);

        MatriculaTurma solicitacao = new MatriculaTurma(
                aluno.getMatricula(),
                discNorm,
                periodoNorm,
                turmaNorm);

        if (vagasOcupadas >= turma.getVagas()) {
            solicitacao.setStatus(StatusMatricula.LISTA_ESPERA);
        } else {
            solicitacao.setStatus(StatusMatricula.CONFIRMADA);
        }

        matriculaRepository.salvar(solicitacao);
        return solicitacao.getStatus();
    }

    // =========================================================================
    // Cancelar solicitação
    // =========================================================================

    /**
     * Cancela uma solicitação de matrícula PENDENTE do aluno.
     *
     * @param aluno            usuário que está realizando a operação (deve ser
     *                         ALUNO)
     * @param codigoDisciplina código da disciplina da turma
     * @param codigoPeriodo    código do período letivo
     * @param codigoTurma      código da turma
     * @throws Exception se a solicitação não existir, não pertencer ao aluno
     *                   ou não estiver com status PENDENTE
     */
    public void cancelarSolicitacao(
            Usuario aluno,
            String codigoDisciplina,
            String codigoPeriodo,
            String codigoTurma) throws Exception {

        if (aluno == null || aluno.getTipo() != TipoUsuario.ALUNO) {
            throw new Exception("Erro: Apenas alunos podem cancelar suas solicitações de matrícula.");
        }

        String discNorm = codigoDisciplina != null ? codigoDisciplina.trim() : "";
        String periodoNorm = codigoPeriodo != null ? codigoPeriodo.trim() : "";
        String turmaNorm = codigoTurma != null ? codigoTurma.trim() : "";

        MatriculaTurma solicitacao = matriculaRepository.buscarPorChaveUnica(
                aluno.getMatricula(), discNorm, periodoNorm, turmaNorm);

        if (solicitacao == null) {
            throw new Exception(
                    "Erro: Nenhuma solicitação encontrada para a turma '" + turmaNorm
                            + "' da disciplina '" + discNorm + "'.");
        }

        if (solicitacao.getStatus() != StatusMatricula.PENDENTE) {
            throw new Exception(
                    "Erro: Apenas solicitações com status PENDENTE podem ser canceladas. "
                            + "Status atual: " + solicitacao.getStatus() + ".");
        }

        solicitacao.setStatus(StatusMatricula.CANCELADA);
        matriculaRepository.atualizar(solicitacao);
    }

    // =========================================================================
    // RF22 — Cancelar matrícula dentro do período permitido
    // =========================================================================

    /**
     * RF22: Permite que o aluno cancele uma matrícula dentro do período permitido.
     *
     * Regra adotada:
     * - Apenas alunos podem cancelar matrícula.
     * - A matrícula precisa existir.
     * - Status permitidos para cancelamento: CONFIRMADA, LISTA_ESPERA ou PENDENTE.
     * - O período letivo precisa estar ativo.
     * - A data atual precisa estar entre dataInicio e dataFim do período.
     */
    public void cancelarMatricula(
            Usuario aluno,
            String codigoDisciplina,
            String codigoPeriodo,
            String codigoTurma) throws Exception {

        if (aluno == null || aluno.getTipo() != TipoUsuario.ALUNO) {
            throw new Exception("Erro: Apenas alunos podem cancelar matrícula.");
        }

        if (codigoDisciplina == null || codigoDisciplina.trim().isEmpty()) {
            throw new Exception("Erro: Código da disciplina não pode ser vazio.");
        }

        if (codigoPeriodo == null || codigoPeriodo.trim().isEmpty()) {
            throw new Exception("Erro: Código do período letivo não pode ser vazio.");
        }

        if (codigoTurma == null || codigoTurma.trim().isEmpty()) {
            throw new Exception("Erro: Código da turma não pode ser vazio.");
        }

        String discNorm = codigoDisciplina.trim();
        String periodoNorm = codigoPeriodo.trim();
        String turmaNorm = codigoTurma.trim();

        MatriculaTurma matricula = matriculaRepository.buscarPorChaveUnica(
                aluno.getMatricula(),
                discNorm,
                periodoNorm,
                turmaNorm);

        if (matricula == null) {
            throw new Exception(
                    "Erro: Nenhuma matrícula encontrada para a turma '"
                            + turmaNorm + "' da disciplina '" + discNorm + "'.");
        }

        if (matricula.getStatus() == StatusMatricula.CANCELADA) {
            throw new Exception("Erro: Esta matrícula já está cancelada.");
        }

        if (matricula.getStatus() == StatusMatricula.REJEITADA) {
            throw new Exception("Erro: Matrículas rejeitadas não podem ser canceladas.");
        }

        PeriodoLetivo periodo = periodoRepository.buscarPorCodigo(periodoNorm);

        if (periodo == null) {
            throw new Exception("Erro: Período letivo '" + periodoNorm + "' não encontrado.");
        }

        if (!periodo.isAtivo()) {
            throw new Exception("Erro: Não é possível cancelar matrícula em período letivo inativo.");
        }

        LocalDate hoje = LocalDate.now();

        if (periodo.getDataInicio() != null && hoje.isBefore(periodo.getDataInicio())) {
            throw new Exception("Erro: Cancelamento de matrícula fora do período permitido.");
        }

        if (periodo.getDataFim() != null && hoje.isAfter(periodo.getDataFim())) {
            throw new Exception("Erro: Cancelamento de matrícula fora do período permitido.");
        }

        StatusMatricula statusAnterior = matricula.getStatus();

        matricula.setStatus(StatusMatricula.CANCELADA);
        matriculaRepository.atualizar(matricula);

        // RF23: se uma vaga foi liberada, promove o primeiro aluno da lista de espera
        if (statusAnterior == StatusMatricula.CONFIRMADA
                || statusAnterior == StatusMatricula.PENDENTE) {
            promoverPrimeiroDaListaEsperaSeHouverVaga(discNorm, periodoNorm, turmaNorm);
        }
    }

    // =========================================================================
    // Consultas
    // =========================================================================

    /**
     * Lista todas as solicitações de matrícula de um aluno.
     *
     * @param aluno usuário autenticado (deve ser ALUNO)
     * @return lista de solicitações (pode ser vazia)
     * @throws Exception se o usuário não for aluno
     */
    public List<MatriculaTurma> listarMinhasSolicitacoes(Usuario aluno) throws Exception {
        if (aluno == null || aluno.getTipo() != TipoUsuario.ALUNO) {
            throw new Exception("Erro: Apenas alunos podem consultar suas solicitações de matrícula.");
        }
        return matriculaRepository.listarPorAluno(aluno.getMatricula());
    }

    /**
     * Retorna o número de vagas disponíveis (não confirmadas) em uma turma.
     *
     * @param turma a turma a ser consultada
     * @return vagas disponíveis (>= 0)
     */
    public long vagasDisponiveis(Turma turma) {
        if (turma == null)
            return 0;
        long ocupadas = matriculaRepository.contarOcupadasPorTurma(
                turma.getCodigoDisciplina(),
                turma.getCodigoPeriodo(),
                turma.getCodigo());
        return Math.max(0, turma.getVagas() - ocupadas);
    }

    // =========================================================================
    // RF23 — Manutenção da lista de espera por turma
    // =========================================================================

    /**
     * RF23: Lista os alunos em lista de espera de uma turma específica.
     *
     * A lista é ordenada pela data de solicitação, mantendo a ordem de chegada.
     */
    public List<MatriculaTurma> listarListaEsperaPorTurma(
            Usuario coordenador,
            String codigoDisciplina,
            String codigoPeriodo,
            String codigoTurma) throws Exception {

        validarCoordenador(coordenador);

        if (codigoDisciplina == null || codigoDisciplina.trim().isEmpty()) {
            throw new Exception("Erro: Código da disciplina não pode ser vazio.");
        }

        if (codigoPeriodo == null || codigoPeriodo.trim().isEmpty()) {
            throw new Exception("Erro: Código do período letivo não pode ser vazio.");
        }

        if (codigoTurma == null || codigoTurma.trim().isEmpty()) {
            throw new Exception("Erro: Código da turma não pode ser vazio.");
        }

        String discNorm = codigoDisciplina.trim();
        String periodoNorm = codigoPeriodo.trim();
        String turmaNorm = codigoTurma.trim();

        Turma turma = turmaRepository.buscarPorChaveUnica(discNorm, periodoNorm, turmaNorm);

        if (turma == null) {
            throw new Exception(
                    "Erro: Turma '" + turmaNorm
                            + "' da disciplina '" + discNorm
                            + "' no período '" + periodoNorm + "' não encontrada.");
        }

        return obterListaEsperaOrdenada(discNorm, periodoNorm, turmaNorm);
    }

    /**
     * RF23: Promove automaticamente o primeiro aluno da lista de espera
     * quando surgir vaga em uma turma.
     */
    private void promoverPrimeiroDaListaEsperaSeHouverVaga(
            String codigoDisciplina,
            String codigoPeriodo,
            String codigoTurma) {
        Turma turma = turmaRepository.buscarPorChaveUnica(
                codigoDisciplina,
                codigoPeriodo,
                codigoTurma);

        if (turma == null) {
            return;
        }

        long ocupadas = matriculaRepository.contarOcupadasPorTurma(
                codigoDisciplina,
                codigoPeriodo,
                codigoTurma);

        if (ocupadas >= turma.getVagas()) {
            return;
        }

        List<MatriculaTurma> listaEspera = obterListaEsperaOrdenada(
                codigoDisciplina,
                codigoPeriodo,
                codigoTurma);

        if (listaEspera.isEmpty()) {
            return;
        }

        MatriculaTurma proximo = listaEspera.get(0);
        proximo.setStatus(StatusMatricula.CONFIRMADA);
        matriculaRepository.atualizar(proximo);
    }

    /**
     * Retorna apenas matrículas com status LISTA_ESPERA de uma turma,
     * ordenadas pela data da solicitação.
     */
    private List<MatriculaTurma> obterListaEsperaOrdenada(
            String codigoDisciplina,
            String codigoPeriodo,
            String codigoTurma) {
        return matriculaRepository
                .listarPorTurma(codigoDisciplina, codigoPeriodo, codigoTurma)
                .stream()
                .filter(m -> m.getStatus() == StatusMatricula.LISTA_ESPERA)
                .sorted(java.util.Comparator.comparing(
                        MatriculaTurma::getDataSolicitacao,
                        java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())))
                .collect(java.util.stream.Collectors.toList());
    }
    // =========================================================================
    // Gestão pelo Coordenador
    // =========================================================================

    /**
     * Lista todas as solicitações pendentes no sistema.
     *
     * @param coordenador usuário que está realizando a operação (deve ser
     *                    COORDENADOR)
     * @return lista de solicitações pendentes
     * @throws Exception se o usuário não for coordenador
     */
    public List<MatriculaTurma> listarSolicitacoesPendentes(Usuario coordenador) throws Exception {
        if (coordenador == null || coordenador.getTipo() != TipoUsuario.COORDENADOR) {
            throw new Exception("Erro: Apenas coordenadores podem listar solicitações de matrícula.");
        }
        return matriculaRepository.listarTodas().stream()
                .filter(m -> m.getStatus() == StatusMatricula.PENDENTE)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Lista todas as solicitações de matrícula do sistema, independente do status.
     *
     * @param coordenador usuário que está realizando a operação (deve ser
     *                    COORDENADOR)
     * @return lista de todas as solicitações
     * @throws Exception se o usuário não for coordenador
     */
    public List<MatriculaTurma> listarTodasSolicitacoes(Usuario coordenador) throws Exception {
        if (coordenador == null || coordenador.getTipo() != TipoUsuario.COORDENADOR) {
            throw new Exception("Erro: Apenas coordenadores podem listar solicitações de matrícula.");
        }
        return matriculaRepository.listarTodas();
    }

    /**
     * Lista todas as solicitações de matrícula de um status específico.
     *
     * @param coordenador usuário que está realizando a operação (deve ser
     *                    COORDENADOR)
     * @param status      o status desejado
     * @return lista de solicitações com o status especificado
     * @throws Exception se o usuário não for coordenador
     */
    public List<MatriculaTurma> listarSolicitacoesPorStatus(Usuario coordenador, StatusMatricula status)
            throws Exception {
        if (coordenador == null || coordenador.getTipo() != TipoUsuario.COORDENADOR) {
            throw new Exception("Erro: Apenas coordenadores podem listar solicitações de matrícula.");
        }
        return matriculaRepository.listarTodas().stream()
                .filter(m -> m.getStatus() == status)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Lista todas as solicitações de matrícula de uma turma específica.
     *
     * @param coordenador      usuário que está realizando a operação (deve ser
     *                         COORDENADOR)
     * @param codigoDisciplina código da disciplina
     * @param codigoPeriodo    código do período letivo
     * @param codigoTurma      código da turma
     * @return lista de solicitações da turma
     * @throws Exception se o usuário não for coordenador
     */
    public List<MatriculaTurma> listarSolicitacoesPorTurma(
            Usuario coordenador,
            String codigoDisciplina,
            String codigoPeriodo,
            String codigoTurma) throws Exception {
        if (coordenador == null || coordenador.getTipo() != TipoUsuario.COORDENADOR) {
            throw new Exception("Erro: Apenas coordenadores podem listar solicitações de matrícula.");
        }
        return matriculaRepository.listarPorTurma(codigoDisciplina, codigoPeriodo, codigoTurma);
    }

    /**
     * Aprova uma solicitação de matrícula.
     *
     * @param coordenador      usuário coordenador
     * @param matriculaAluno   matrícula do aluno
     * @param codigoDisciplina código da disciplina
     * @param codigoPeriodo    código do período letivo
     * @param codigoTurma      código da turma
     * @throws Exception se não for coordenador ou solicitação não
     *                   encontrada/pendente
     */
    public void aprovarMatricula(
            Usuario coordenador,
            String matriculaAluno,
            String codigoDisciplina,
            String codigoPeriodo,
            String codigoTurma) throws Exception {
        validarCoordenador(coordenador);

        MatriculaTurma solicitacao = buscarSolicitacaoPendete(
                matriculaAluno, codigoDisciplina, codigoPeriodo, codigoTurma);

        solicitacao.setStatus(StatusMatricula.CONFIRMADA);
        matriculaRepository.atualizar(solicitacao);
    }

    /**
     * Nega uma solicitação de matrícula.
     *
     * @param coordenador      usuário coordenador
     * @param matriculaAluno   matrícula do aluno
     * @param codigoDisciplina código da disciplina
     * @param codigoPeriodo    código do período letivo
     * @param codigoTurma      código da turma
     * @throws Exception se não for coordenador ou solicitação não
     *                   encontrada/pendente
     */
    public void negarMatricula(
            Usuario coordenador,
            String matriculaAluno,
            String codigoDisciplina,
            String codigoPeriodo,
            String codigoTurma) throws Exception {
        validarCoordenador(coordenador);

        MatriculaTurma solicitacao = buscarSolicitacaoPendete(
                matriculaAluno, codigoDisciplina, codigoPeriodo, codigoTurma);

        solicitacao.setStatus(StatusMatricula.REJEITADA);
        matriculaRepository.atualizar(solicitacao);
    }

    private void validarCoordenador(Usuario coordenador) throws Exception {
        if (coordenador == null || coordenador.getTipo() != TipoUsuario.COORDENADOR) {
            throw new Exception("Erro: Apenas coordenadores podem realizar esta operação.");
        }
    }

    private MatriculaTurma buscarSolicitacaoPendete(
            String matriculaAluno,
            String codigoDisciplina,
            String codigoPeriodo,
            String codigoTurma) throws Exception {
        MatriculaTurma solicitacao = matriculaRepository.buscarPorChaveUnica(
                matriculaAluno, codigoDisciplina, codigoPeriodo, codigoTurma);

        if (solicitacao == null) {
            throw new Exception("Erro: Solicitação de matrícula não encontrada.");
        }

        if (solicitacao.getStatus() != StatusMatricula.PENDENTE) {
            throw new Exception("Erro: Apenas solicitações PENDENTES podem ser aprovadas ou negadas.");
        }

        return solicitacao;
    }

    public void validarPreRequisitos(Aluno aluno, Disciplina disciplina) throws Exception {

        List<String> preRequisitos = disciplina.getPreRequisitos();

        if (preRequisitos == null || preRequisitos.isEmpty())
            return;

        for (String codigoPreReq : preRequisitos) {

            boolean aprovado = historicoService.alunoFoiAprovado(aluno.getMatricula(), codigoPreReq);
            if (!aprovado) {

                throw new Exception("Erro: O aluno nao foi aprovado no pre-requisito " + codigoPreReq);
            }
        }
    }

    private void validarChoqueHorario(Aluno aluno, Turma novaTurma) throws Exception {

        List<MatriculaTurma> matriculas = matriculaRepository.listarPorAluno(aluno.getMatricula());

        for (MatriculaTurma matricula : matriculas) {

            if (matricula.getStatus() == StatusMatricula.CANCELADA
                    || matricula.getStatus() == StatusMatricula.REJEITADA) {
                continue;
            }

            Turma turmaExistente = turmaRepository.buscarPorChaveUnica(
                    matricula.getCodigoDisciplina(),
                    matricula.getCodigoPeriodo(),
                    matricula.getCodigoTurma());

            if (turmaExistente == null) {
                continue;
            }

            if (existeChoqueHorario(turmaExistente.getHorario(), novaTurma.getHorario())) {

                throw new Exception("Erro: Existe choque de horário com a turma " + turmaExistente.getCodigo());
            }
        }
    }

    private boolean existeChoqueHorario(String horario1, String horario2) {
        if (horario1 == null || horario2 == null)
            return false;

        String[] partes1 = horario1.split(" ");
        String[] partes2 = horario2.split(" ");

        String dias1 = partes1[0];
        String dias2 = partes2[0];

        String hora1 = partes1[1];
        String hora2 = partes2[1];

        // verifica se existe algum dia em comum
        String[] listaDias1 = dias1.split("/");
        String[] listaDias2 = dias2.split("/");

        boolean mesmoDia = false;

        for (String d1 : listaDias1) {
            for (String d2 : listaDias2) {
                if (d1.equalsIgnoreCase(d2))
                    mesmoDia = true;
            }
        }

        if (!mesmoDia)
            return false;

        return hora1.equalsIgnoreCase(hora2);
    }
}
