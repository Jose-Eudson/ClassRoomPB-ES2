package com.classroompb.ui;

import java.util.Arrays;
import java.util.List;

import com.classroompb.model.TipoUsuario;
import com.classroompb.model.Usuario;
import com.classroompb.service.PerfilAcessoService;
import com.classroompb.service.UsuarioService;

/**
 * Controlador da interface do Aluno.
 * Responsável pelo menu e todas as ações disponíveis para esse perfil.
 * As funcionalidades serão implementadas nas releases seguintes (RF15–RF30).
 */
public class AlunoController {

    private final UsuarioService service;

    public AlunoController(UsuarioService service) {
        this.service = service;
    }

    /** Exibe o menu principal do aluno e permanece em loop até logout. */
    public void exibirMenu(Usuario usuario) {
        try {
            PerfilAcessoService.validarPerfil(usuario, TipoUsuario.ALUNO);
        } catch (Exception e) {
            ConsoleUI.exibirMensagem(e.getMessage(), true);
            return;
        }

        while (true) {
            List<String> opcoes = Arrays.asList(
                "Consultar disciplinas e turmas",
                "Solicitar matrícula",
                "Acompanhar matrícula e lista de espera",
                "Consultar frequência e notas",
                "Consultar histórico acadêmico",
                "Cancelar matrícula",
                "Logout"
            );
            int escolha = ConsoleUI.exibirMenuInterativo("MENU ALUNO", opcoes);

            if (escolha == -1 || escolha == opcoes.size() - 1) break;

            // Funcionalidades implementadas nas próximas releases
            ConsoleUI.exibirMensagem("Funcionalidade disponível na próxima release.", false);
        }
    }
}
