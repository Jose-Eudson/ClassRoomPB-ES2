package com.classroompb.ui;

import java.util.Arrays;
import java.util.List;

import com.classroompb.model.Usuario;
import com.classroompb.service.UsuarioService;

/**
 * Controlador da interface do Coordenador.
 * Responsável pelo menu e todas as ações disponíveis para esse perfil.
 * As funcionalidades serão implementadas nas releases seguintes (RF05–RF14).
 */
public class CoordenadorController {

    private final UsuarioService service;

    public CoordenadorController(UsuarioService service) {
        this.service = service;
    }

    /** Exibe o menu principal do coordenador e permanece em loop até logout. */
    public void exibirMenu(Usuario usuario) {
        while (true) {
            List<String> opcoes = Arrays.asList(
                "Cadastrar disciplinas",
                "Ofertar turmas",
                "Gerenciar vagas e horários",
                "Aprovar/cancelar matrículas",
                "Visualizar listas de espera",
                "Gerar relatórios acadêmicos",
                "Logout"
            );
            int escolha = ConsoleUI.exibirMenuInterativo("MENU COORDENADOR", opcoes);

            if (escolha == -1 || escolha == opcoes.size() - 1) break;

            // Funcionalidades implementadas nas próximas releases
            ConsoleUI.exibirMensagem("Funcionalidade disponível na próxima release.", false);
        }
    }
}