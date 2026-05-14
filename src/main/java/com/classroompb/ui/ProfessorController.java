package com.classroompb.ui;

import java.util.Arrays;
import java.util.List;

import com.classroompb.model.Usuario;
import com.classroompb.service.UsuarioService;

/**
 * Controlador da interface do Professor.
 * Responsável pelo menu e todas as ações disponíveis para esse perfil.
 * As funcionalidades serão implementadas nas releases seguintes (RF27–RF35).
 */
public class ProfessorController {

    private final UsuarioService service;

    public ProfessorController(UsuarioService service) {
        this.service = service;
    }

    /** Exibe o menu principal do professor e permanece em loop até logout. */
    public void exibirMenu(Usuario usuario) {
        while (true) {
            List<String> opcoes = Arrays.asList(
                "Visualizar turmas",
                "Registrar frequência",
                "Lançar notas",
                "Acompanhar alunos",
                "Alterar notas (antes do fechamento)",
                "Logout"
            );
            int escolha = ConsoleUI.exibirMenuInterativo("MENU PROFESSOR", opcoes);

            if (escolha == -1 || escolha == opcoes.size() - 1) break;

            // Funcionalidades implementadas nas próximas releases
            ConsoleUI.exibirMensagem("Funcionalidade disponível na próxima release.", false);
        }
    }
}