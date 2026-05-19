package com.classroompb.ui;

import java.util.Arrays;
import java.util.List;

import com.classroompb.model.TipoUsuario;
import com.classroompb.model.Usuario;
import com.classroompb.service.DisciplinaService;
import com.classroompb.service.PerfilAcessoService;
import com.classroompb.service.UsuarioService;

/**
 * Controlador da interface do Coordenador.
 * Responsável pelo menu e todas as ações disponíveis para esse perfil.
 * As funcionalidades serão implementadas nas releases seguintes (RF05–RF14).
 */
public class CoordenadorController {

@SuppressWarnings("unused")
    private final UsuarioService service;
    private final DisciplinaService disciplinaService;

    public CoordenadorController(UsuarioService service, DisciplinaService disciplinaService) {
        this.service = service;
        this.disciplinaService = disciplinaService;
    }

    /** Exibe o menu principal do coordenador e permanece em loop até logout. */
    public void exibirMenu(Usuario usuario) {
        try {
            PerfilAcessoService.validarPerfil(usuario, TipoUsuario.COORDENADOR);
        } catch (Exception e) {
            ConsoleUI.exibirMensagem(e.getMessage(), true);
            return;
        }

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

            switch (escolha) {
                case 0:
                    cadastrarDisciplina();
                    break;
                default:
                    // Funcionalidades implementadas nas próximas releases
                    ConsoleUI.exibirMensagem("Funcionalidade disponível na próxima release.", false);
                    break;
            }
        }
    }

    private void cadastrarDisciplina() {
        ConsoleUI.limparTela();
        ConsoleUI.exibirCabecalho("CADASTRAR DISCIPLINA");
        try {
            String codigo = ConsoleUI.lerEntrada("Codigo da disciplina: ");
            String nome = ConsoleUI.lerEntrada("Nome da disciplina: ");
            String cargaHorariaTexto = ConsoleUI.lerEntrada("Carga horaria (horas): ");

            int cargaHoraria = Integer.parseInt(cargaHorariaTexto);
            disciplinaService.cadastrarDisciplina(codigo, nome, cargaHoraria);
            ConsoleUI.exibirMensagem("Disciplina cadastrada com sucesso!", false);
        } catch (NumberFormatException e) {
            ConsoleUI.exibirMensagem("Erro: Carga horaria deve ser um numero inteiro.", true);
        } catch (Exception e) {
            ConsoleUI.exibirMensagem(e.getMessage(), true);
        }
    }
}
