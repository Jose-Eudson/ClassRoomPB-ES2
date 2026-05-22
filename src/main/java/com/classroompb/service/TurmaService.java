package com.classroompb.service;

import java.util.List;

import com.classroompb.model.Disciplina;
import com.classroompb.model.PeriodoLetivo;
import com.classroompb.model.TipoUsuario;
import com.classroompb.model.Turma;
import com.classroompb.model.Usuario;
import com.classroompb.repository.DisciplinaRepository;
import com.classroompb.repository.PeriodoLetivoRepository;
import com.classroompb.repository.TurmaRepository;

/**
 * RF10: Serviço responsável pela oferta de turmas.
 *
 * Regras de negócio aplicadas:
 *   - Apenas coordenadores podem ofertar turmas.
 *   - A disciplina informada deve existir.
 *   - O período letivo informado deve existir.
 *   - Não é permitido cadastrar turmas em períodos inativos.
 *   - O código da turma não pode ser vazio.
 *   - O número de vagas deve ser positivo.
 *   - O horário não pode ser vazio.
 *   - Não pode existir outra turma com o mesmo código para a mesma
 *     disciplina no mesmo período (unicidade pela chave composta).
 */
public class TurmaService {

    private final TurmaRepository turmaRepository;
    private final DisciplinaRepository disciplinaRepository;
    private final PeriodoLetivoRepository periodoRepository;

    public TurmaService(
            TurmaRepository turmaRepository,
            DisciplinaRepository disciplinaRepository,
            PeriodoLetivoRepository periodoRepository
    ) {
        this.turmaRepository = turmaRepository;
        this.disciplinaRepository = disciplinaRepository;
        this.periodoRepository = periodoRepository;
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

        Turma turma = new Turma(
                codigoTurma.trim(),
                codigoDisciplina.trim(),
                codigoPeriodo.trim(),
                vagas,
                horario.trim(),
                (matriculaProfessor == null || matriculaProfessor.trim().isEmpty())
                        ? null
                        : matriculaProfessor.trim()
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
}