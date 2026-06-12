package com.classroompb.model;

/**
 * RF16/RF21: Status possíveis para uma solicitação de matrícula em turma.
 */
public enum StatusMatricula {
    /** Solicitação registrada, aguardando processamento. */
    PENDENTE,

    /** Matrícula efetivada — vaga reservada para o aluno. */
    CONFIRMADA,

    /** Aluno aguardando vaga na turma. */
    LISTA_ESPERA,

    /** Solicitação cancelada pelo aluno. */
    CANCELADA,

    /** Solicitação rejeitada pelo coordenador. */
    REJEITADA
}
