package com.classroompb.util;

import java.util.List;

import com.classroompb.model.TipoUsuario;
import com.classroompb.model.Usuario;

/**
 * Gerador automático de matrículas sequenciais por tipo de usuário.
 *
 * Formato das matrículas: - ALUNO: A{NNNN} (ex: A0001) - PROFESSOR: P{NNNN} (ex: P0001) - COORDENADOR: C{NNNN} (ex:
 * C0001) - ADMINISTRADOR: AD{NNNN} (ex: AD0001)
 */

public class MatriculaGenerator {

    /**
     * Gera uma matrícula única baseada no tipo de usuário. Percorre os usuários existentes para determinar o próximo
     * número sequencial.
     *
     * @param tipo
     *            Tipo de usuário
     * @param usuariosExistentes
     *            Lista de usuários já cadastrados para cálculo do sequencial
     *
     * @return Matrícula gerada no formato apropriado
     */
    public static String gerarMatricula(TipoUsuario tipo, List<Usuario> usuariosExistentes) {
        String prefixo = obterPrefixo(tipo);
        int proximoSequencial = calcularProximoSequencial(usuariosExistentes, prefixo);
        return prefixo + String.format("%04d", proximoSequencial);
    }

    /**
     * Retorna o prefixo de matrícula correspondente ao tipo de usuário. Usa if-else para evitar geração de classe
     * anônima sintética pelo compilador.
     */
    private static String obterPrefixo(TipoUsuario tipo) {
        if (tipo == null) {
            throw new NullPointerException("Tipo de usuário não pode ser nulo");
        }
        if (tipo == TipoUsuario.ALUNO) {
            return "A";
        }
        if (tipo == TipoUsuario.PROFESSOR) {
            return "P";
        }
        if (tipo == TipoUsuario.COORDENADOR) {
            return "C";
        }
        if (tipo == TipoUsuario.ADMINISTRADOR) {
            return "AD";
        }
        throw new IllegalArgumentException("Tipo de usuário inválido: " + tipo);
    }

    /**
     * Percorre os usuários existentes, filtra os que têm o mesmo prefixo, extrai a parte numérica e retorna o maior
     * valor encontrado + 1.
     *
     * Usa prefixo.length() para extrair a parte numérica corretamente, evitando erro com o prefixo "AD" que tem 2
     * caracteres.
     */
    private static int calcularProximoSequencial(List<Usuario> usuariosExistentes, String prefixo) {
        int maiorSequencial = 0;

        for (Usuario usuario : usuariosExistentes) {
            String matricula = usuario.getMatricula();
            if (matricula != null && matricula.startsWith(prefixo) && matricula.length() > prefixo.length()) {
                String sequencialStr = matricula.substring(prefixo.length());
                if (sequencialStr.matches("\\d+")) {
                    int sequencial = Integer.parseInt(sequencialStr);
                    if (sequencial > maiorSequencial) {
                        maiorSequencial = sequencial;
                    }
                }
            }
        }

        return maiorSequencial + 1;
    }
}
