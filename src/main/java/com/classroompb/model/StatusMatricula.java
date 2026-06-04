package com.classroompb.model;

/**
 * RF16: Status possíveis para uma solicitação de matrícula em turma.
 */
public enum StatusMatricula {
    /** Solicitação registrada, aguardando processamento. */
    PENDENTE,

    /** Matrícula efetivada — vaga reservada para o aluno. */
    CONFIRMADA,

    /** Solicitação cancelada pelo aluno. */
    CANCELADA
}
