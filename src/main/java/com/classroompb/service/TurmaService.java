package com.classroompb.service;

import java.time.LocalDate;
import java.util.List;

import com.classroompb.model.Disciplina;
import com.classroompb.model.PeriodoLetivo;
import com.classroompb.model.TipoUsuario;
import com.classroompb.model.Turma;
import com.classroompb.model.Usuario;
import com.classroompb.repository.DisciplinaRepository;
import com.classroompb.repository.PeriodoLetivoRepository;
import com.classroompb.repository.TurmaRepository;
import com.classroompb.repository.UsuarioRepository;

/**
 * RF10/RF11: Serviço responsável pela oferta e gestão de turmas.
 *
 * Regras de negócio aplicadas:
 *   - Apenas coordenadores podem ofertar, editar ou excluir turmas.
 *   - A disciplina informada deve existir.
 *   - O período letivo informado deve existir.
 *   - Não é permitido cadastrar turmas em períodos inativos.
 *   - O código da turma não pode ser vazio.
 *   - O número de vagas deve ser positivo.
 *   - O horário não pode ser vazio.
 *   - A sala não pode ser vazia. (RF11)
 *   - Não pode existir outra turma com o mesmo código para a mesma
 *     disciplina no mesmo período (unicidade pela chave composta).
 *   - RF12: O professor não pode ministrar duas turmas no mesmo horário.
 *   - RF13: Não é permitido ofertar turma sem professor responsável.
 *   - RF14: Edição/cancelamento de turma só é permitido antes do início das aulas.
 */
public class TurmaService {

    private final TurmaRepository turmaRepository;
    private final DisciplinaRepository disciplinaRepository;
    private final PeriodoLetivoRepository periodoRepository;
    private final UsuarioRepository usuarioRepository;

    public TurmaService(
            TurmaRepository turmaRepository,
            DisciplinaRepository disciplinaRepository,
            PeriodoLetivoRepository periodoRepository
    ) {
        this(turmaRepository, disciplinaRepository, periodoRepository, new UsuarioRepository());
    }

    public TurmaService(
            TurmaRepository turmaRepository,
            DisciplinaRepository disciplinaRepository,
            PeriodoLetivoRepository periodoRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.turmaRepository = turmaRepository;
        this.disciplinaRepository = disciplinaRepository;
        this.periodoRepository = periodoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Oferta (cadastra) uma nova turma para uma disciplina em um período letivo.
     *
     * @param coordenador        usuário que está realizando a operação (deve ser COORDENADOR)
     * @param codigoDisciplina   código da disciplina a ser ofertada
     * @param codigoPeriodo      código do período letivo (ex: "2026.1")
     * @param codigoTurma        identificador da turma (ex: "T01")
     * @param vagas              número máximo de vagas disponíveis
     * @param horario            descrição do horário das aulas
     * @param sala               sala onde as aulas serão realizadas (RF11)
     * @param matriculaProfessor matrícula do professor responsável (pode ser null ou vazio)
     * @throws Exception se qualquer regra de negócio for violada
     */
    public void ofertarTurma(
            Usuario coordenador,
            String codigoDisciplina,
            String codigoPeriodo,
            String codigoTurma,
            int vagas,
            String horario,
            String sala,
            String matriculaProfessor
    ) throws Exception {

        // Permissão: apenas coordenadores
        if (coordenador == null || coordenador.getTipo() != TipoUsuario.COORDENADOR) {
            throw new Exception("Erro: Apenas coordenadores podem ofertar turmas.");
        }

        // Validação: código da turma obrigatório
        if (codigoTurma == null || codigoTurma.trim().isEmpty()) {
            throw new Exception("Erro: Código da turma não pode ser vazio.");
        }

        // Validação: código da disciplina obrigatório
        if (codigoDisciplina == null || codigoDisciplina.trim().isEmpty()) {
            throw new Exception("Erro: Código da disciplina não pode ser vazio.");
        }

        // Validação: código do período obrigatório
        if (codigoPeriodo == null || codigoPeriodo.trim().isEmpty()) {
            throw new Exception("Erro: Código do período letivo não pode ser vazio.");
        }

        // Validação: vagas positivas
        if (vagas <= 0) {
            throw new Exception("Erro: O número de vagas deve ser maior que zero.");
        }

        // Validação: horário obrigatório
        if (horario == null || horario.trim().isEmpty()) {
            throw new Exception("Erro: Horário da turma não pode ser vazio.");
        }

        // Validação: sala obrigatória (RF11)
        if (sala == null || sala.trim().isEmpty()) {
            throw new Exception("Erro: Sala da turma não pode ser vazia.");
        }

        // Regra: disciplina deve existir
        Disciplina disciplina = disciplinaRepository.buscarPorCodigo(codigoDisciplina);
        if (disciplina == null) {
            throw new Exception(
                    "Erro: Disciplina com código '" + codigoDisciplina + "' não encontrada."
            );
        }

        // Regra: período letivo deve existir
        PeriodoLetivo periodo = periodoRepository.buscarPorCodigo(codigoPeriodo);
        if (periodo == null) {
            throw new Exception(
                    "Erro: Período letivo '" + codigoPeriodo + "' não encontrado."
            );
        }

        // Regra: não ofertar em período inativo
        if (!periodo.isAtivo()) {
            throw new Exception(
                    "Erro: Não é possível ofertar turmas em um período letivo inativo."
            );
        }

        // Regra: unicidade da turma no contexto disciplina + período
        if (turmaRepository.existePorChaveUnica(codigoDisciplina, codigoPeriodo, codigoTurma)) {
            throw new Exception(
                    "Erro: Já existe uma turma '" + codigoTurma
                    + "' para a disciplina '" + codigoDisciplina
                    + "' no período '" + codigoPeriodo + "'."
            );
        }

        // RF13: Professor responsável é obrigatório para ofertar turma
        if (matriculaProfessor == null || matriculaProfessor.trim().isEmpty()) {
            throw new Exception("Erro: RF13 - Não é possível ofertar turma sem professor responsável.");
        }

        // Verifica se o professor existe e é do tipo PROFESSOR
        Usuario prof = usuarioRepository.buscarPorMatricula(matriculaProfessor.trim())
                .orElseThrow(() -> new Exception(
                        "Erro: Professor com matrícula '" + matriculaProfessor.trim() + "' não encontrado."));
        if (prof.getTipo() != TipoUsuario.PROFESSOR) {
            throw new Exception(
                    "Erro: O usuário '" + matriculaProfessor.trim() + "' não é um professor.");
        }

        // RF12: Professor não pode ministrar duas turmas no mesmo horário (RN06)
        List<Turma> turmasExistentes = turmaRepository.listarPorPeriodo(codigoPeriodo.trim());
        for (Turma t : turmasExistentes) {
            if (matriculaProfessor.trim().equals(t.getMatriculaProfessor())
                    && horario.trim().equalsIgnoreCase(t.getHorario())) {
                throw new Exception(
                        "Erro: RF12/RN06 - O professor '" + matriculaProfessor.trim()
                        + "' já ministra uma turma no horário '" + horario.trim()
                        + "' neste período.");
            }
        }

        Turma turma = new Turma(
                codigoTurma.trim(),
                codigoDisciplina.trim(),
                codigoPeriodo.trim(),
                vagas,
                horario.trim(),
                sala.trim(),
                matriculaProfessor.trim()
        );

        turmaRepository.salvar(turma);
    }

    /**
     * Lista todas as turmas ofertadas em um determinado período letivo.
     *
     * @param codigoPeriodo código do período letivo
     * @return lista de turmas do período (pode ser vazia)
     */
    public List<Turma> listarTurmasPorPeriodo(String codigoPeriodo) {
        return turmaRepository.listarPorPeriodo(codigoPeriodo);
    }

    /**
     * Lista todas as turmas de uma disciplina em um período letivo específico.
     *
     * @param codigoDisciplina código da disciplina
     * @param codigoPeriodo    código do período letivo
     * @return lista de turmas encontradas (pode ser vazia)
     */
    public List<Turma> listarTurmasPorDisciplinaEPeriodo(String codigoDisciplina, String codigoPeriodo) {
        return turmaRepository.listarPorDisciplinaEPeriodo(codigoDisciplina, codigoPeriodo);
    }

    /**
     * Lista todas as turmas cadastradas no sistema.
     *
     * @return lista completa de turmas
     */
    public List<Turma> listarTodasTurmas() {
        return turmaRepository.listarTodos();
    }

    /**
     * Busca uma turma pela chave composta e a retorna.
     *
     * @param codigoDisciplina código da disciplina
     * @param codigoPeriodo    código do período letivo
     * @param codigoTurma      código da turma
     * @return a turma encontrada
     * @throws Exception se a turma não for encontrada
     */
    public Turma buscarTurma(String codigoDisciplina, String codigoPeriodo, String codigoTurma)
            throws Exception {
        if (codigoDisciplina == null || codigoDisciplina.trim().isEmpty()
                || codigoPeriodo == null || codigoPeriodo.trim().isEmpty()
                || codigoTurma == null || codigoTurma.trim().isEmpty()) {
            throw new Exception("Erro: Disciplina, período e código da turma são obrigatórios.");
        }
        Turma turma = turmaRepository.buscarPorChaveUnica(codigoDisciplina, codigoPeriodo, codigoTurma);
        if (turma == null) {
            throw new Exception(
                    "Erro: Turma '" + codigoTurma
                    + "' da disciplina '" + codigoDisciplina
                    + "' no período '" + codigoPeriodo + "' não encontrada."
            );
        }
        return turma;
    }

    /**
     * Edita os atributos de uma turma existente.
     * O código da turma, disciplina e período são a chave de identificação
     * e não podem ser alterados nesta operação.
     *
     * @param coordenador        usuário que está realizando a operação (deve ser COORDENADOR)
     * @param codigoDisciplina   código da disciplina da turma a editar
     * @param codigoPeriodo      código do período letivo da turma a editar
     * @param codigoTurma        código da turma a editar
     * @param novasVagas         novo número de vagas (mantém o atual se zero ou negativo)
     * @param novoHorario        novo horário (mantém o atual se vazio ou nulo)
     * @param novaSala           nova sala (mantém a atual se vazio ou nulo)
     * @param novaMatriculaProf  nova matrícula do professor (null ou vazio = sem professor)
     * @throws Exception se qualquer regra de negócio for violada
     */
    public void editarTurma(
            Usuario coordenador,
            String codigoDisciplina,
            String codigoPeriodo,
            String codigoTurma,
            int novasVagas,
            String novoHorario,
            String novaSala,
            String novaMatriculaProf
    ) throws Exception {

        // Permissão: apenas coordenadores
        if (coordenador == null || coordenador.getTipo() != TipoUsuario.COORDENADOR) {
            throw new Exception("Erro: Apenas coordenadores podem editar turmas.");
        }

        // Localiza a turma — lança exceção se não existir
        Turma turma = buscarTurma(codigoDisciplina, codigoPeriodo, codigoTurma);

        // RF14: Só é possível editar antes do início das aulas
        PeriodoLetivo periodoEditar = periodoRepository.buscarPorCodigo(codigoPeriodo);
        if (periodoEditar != null && periodoEditar.getDataInicio() != null
                && !LocalDate.now().isBefore(periodoEditar.getDataInicio())) {
            throw new Exception(
                    "Erro: RF14 - Não é possível editar uma turma após o início das aulas (início: "
                    + periodoEditar.getDataInicio() + ").");
        }

        // Atualiza vagas somente se um valor positivo foi fornecido
        if (novasVagas > 0) {
            turma.setVagas(novasVagas);
        } else if (novasVagas < 0) {
            throw new Exception("Erro: O número de vagas não pode ser negativo.");
        }

        // Atualiza horário somente se fornecido
        if (novoHorario != null && !novoHorario.trim().isEmpty()) {
            turma.setHorario(novoHorario.trim());
        }

        // Atualiza sala somente se fornecida
        if (novaSala != null && !novaSala.trim().isEmpty()) {
            turma.setSala(novaSala.trim());
        }

        // RF13/RN13: não permite remover o professor de uma turma existente
        if (novaMatriculaProf != null && novaMatriculaProf.trim().isEmpty()) {
            throw new Exception("Erro: RF13 - Não é possível remover o professor de uma turma. Informe outro professor.");
        }

        // Atualiza professor se informado, validando existência e tipo PROFESSOR
        if (novaMatriculaProf != null && !novaMatriculaProf.trim().isEmpty()) {
            Usuario novoProf = usuarioRepository.buscarPorMatricula(novaMatriculaProf.trim())
                    .orElseThrow(() -> new Exception(
                            "Erro: Professor com matrícula '" + novaMatriculaProf.trim() + "' não encontrado."));
            if (novoProf.getTipo() != TipoUsuario.PROFESSOR) {
                throw new Exception(
                        "Erro: O usuário '" + novaMatriculaProf.trim() + "' não é um professor.");
            }
            turma.setMatriculaProfessor(novaMatriculaProf.trim());
        }
        // novaMatriculaProf == null significa "não alterar o professor"

        // RF12/RN06: professor não pode ministrar duas turmas no mesmo horário
        String profFinal = turma.getMatriculaProfessor();
        String horarioFinal = turma.getHorario();
        if (profFinal != null && !profFinal.trim().isEmpty()) {
            List<Turma> turmasNoPeriodo = turmaRepository.listarPorPeriodo(codigoPeriodo);
            for (Turma t : turmasNoPeriodo) {
                // ignora a própria turma
                if (t.getCodigo().equals(codigoTurma)
                        && t.getCodigoDisciplina().equals(codigoDisciplina)
                        && t.getCodigoPeriodo().equals(codigoPeriodo)) {
                    continue;
                }
                if (profFinal.equals(t.getMatriculaProfessor())
                        && horarioFinal.equalsIgnoreCase(t.getHorario())) {
                    throw new Exception(
                            "Erro: RF12/RN06 - O professor '" + profFinal
                            + "' já ministra uma turma no horário '" + horarioFinal
                            + "' neste período.");
                }
            }
        }

        turmaRepository.atualizar(turma);
    }

    /**
     * Exclui uma turma do sistema.
     *
     * @param coordenador      usuário que está realizando a operação (deve ser COORDENADOR)
     * @param codigoDisciplina código da disciplina da turma a excluir
     * @param codigoPeriodo    código do período letivo da turma a excluir
     * @param codigoTurma      código da turma a excluir
     * @throws Exception se o usuário não tiver permissão ou a turma não existir
     */
    public void excluirTurma(
            Usuario coordenador,
            String codigoDisciplina,
            String codigoPeriodo,
            String codigoTurma
    ) throws Exception {

        // Permissão: apenas coordenadores
        if (coordenador == null || coordenador.getTipo() != TipoUsuario.COORDENADOR) {
            throw new Exception("Erro: Apenas coordenadores podem excluir turmas.");
        }

        // Confirma existência antes de deletar
        buscarTurma(codigoDisciplina, codigoPeriodo, codigoTurma);

        // RF14: Só é possível cancelar antes do início das aulas
        PeriodoLetivo periodoExcluir = periodoRepository.buscarPorCodigo(codigoPeriodo);
        if (periodoExcluir != null && periodoExcluir.getDataInicio() != null
                && !LocalDate.now().isBefore(periodoExcluir.getDataInicio())) {
            throw new Exception(
                    "Erro: RF14 - Não é possível cancelar uma turma após o início das aulas (início: "
                    + periodoExcluir.getDataInicio() + ").");
        }

        turmaRepository.deletar(codigoDisciplina, codigoPeriodo, codigoTurma);
    }
}