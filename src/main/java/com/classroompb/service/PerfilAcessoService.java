package com.classroompb.service;

import com.classroompb.model.TipoUsuario;
import com.classroompb.model.Usuario;

/**
 * Serviço utilitário para validar autorização por perfil.
 */
public final class PerfilAcessoService {

    private PerfilAcessoService() {
        // Classe utilitária: impedir instanciação.
    }

    /**
     * Garante que o usuário autenticado possui o perfil esperado.
     */
    public static void validarPerfil(Usuario usuario, TipoUsuario perfilEsperado) throws Exception {
        if (perfilEsperado == null) {
            throw new Exception("Erro: Perfil esperado não pode ser nulo.");
        }
        if (usuario == null) {
            throw new Exception("Erro: Usuário não autenticado.");
        }
        if (usuario.getTipo() == null) {
            throw new Exception("Erro: Perfil de acesso inválido.");
        }
        if (usuario.getTipo() != perfilEsperado) {
            throw new Exception("Erro: Acesso negado para este perfil.");
        }
    }
}
