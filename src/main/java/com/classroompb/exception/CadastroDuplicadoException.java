package com.classroompb.exception;

/**
 * RF04: O sistema deve impedir cadastro duplicado por matrícula ou e-mail.
 *
 * Exceção lançada quando uma tentativa de cadastro viola a unicidade
 * de matrícula ou e-mail. Estende RuntimeException para não obrigar
 * os controladores de UI a declararem checked exceptions desnecessariamente,
 * mantendo a compatibilidade com o tratamento existente via catch (Exception e).
 */
public class CadastroDuplicadoException extends RuntimeException {

    /** Identifica qual campo gerou o conflito. */
    public enum Campo {
        MATRICULA, EMAIL
    }

    private final Campo campo;
    private final String valorDuplicado;

    /**
     * @param campo          campo que violou a unicidade (MATRICULA ou EMAIL)
     * @param valorDuplicado valor que já existe no sistema
     */
    public CadastroDuplicadoException(Campo campo, String valorDuplicado) {
        super(mensagem(campo, valorDuplicado));
        this.campo = campo;
        this.valorDuplicado = valorDuplicado;
    }

    /** Retorna o campo que gerou o conflito. */
    public Campo getCampo() {
        return campo;
    }

    /** Retorna o valor duplicado que causou a exceção. */
    public String getValorDuplicado() {
        return valorDuplicado;
    }

    private static String mensagem(Campo campo, String valor) {
        if (campo == Campo.MATRICULA) {
            return "Erro: Já existe um usuário com esta matrícula.";
        }
        return "Erro: Já existe um usuário com este e-mail.";
    }
}
