package com.classroompb.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.classroompb.model.TipoUsuario;
import com.classroompb.model.Usuario;
import com.classroompb.service.UsuarioService;

/**
 * Controlador da interface do Administrador.
 * Responsável pelo menu e todas as ações disponíveis para esse perfil.
 */
public class AdminController {

    private final UsuarioService service;

    public AdminController(UsuarioService service) {
        this.service = service;
    }

    /** Exibe o menu principal do administrador e permanece em loop até logout. */
    public void exibirMenu(Usuario usuario) {
        while (true) {
            List<String> opcoes = Arrays.asList(
                "Gerenciar usuários",
                "Listar todos os usuários",
                "Logout"
            );
            int escolha = ConsoleUI.exibirMenuInterativo("MENU ADMINISTRADOR", opcoes);

            if (escolha == -1 || escolha == opcoes.size() - 1) break;

            switch (escolha) {
                case 0: gerenciarUsuarios(); break;
                case 1: listarUsuarios();    break;
            }
        }
    }

    // -------------------------------------------------------------------------
    // Submenu de gerenciamento
    // -------------------------------------------------------------------------

    private void gerenciarUsuarios() {
        while (true) {
            List<String> opcoes = Arrays.asList(
                "Cadastrar novo usuário",
                "Editar usuário",
                "Deletar usuário",
                "Listar usuários",
                "Voltar"
            );
            int escolha = ConsoleUI.exibirMenuInterativo("GERENCIAR USUÁRIOS", opcoes);

            if (escolha == 4 || escolha == -1) break;

            switch (escolha) {
                case 0: cadastrarUsuario(); break;
                case 1: editarUsuario();    break;
                case 2: deletarUsuario();   break;
                case 3: listarUsuarios();   break;
            }
        }
    }

    // -------------------------------------------------------------------------
    // Ações
    // -------------------------------------------------------------------------

    /** Coleta dados e cadastra um novo usuário de qualquer tipo. */
    private void cadastrarUsuario() {
        ConsoleUI.limparTela();
        ConsoleUI.exibirCabecalho("CADASTRAR NOVO USUÁRIO");
        try {
            String nome  = ConsoleUI.lerEntrada("Nome: ");
            String email = ConsoleUI.lerEntrada("E-mail: ");
            String senha = ConsoleUI.lerSenha("Senha: ");

            List<String> tipos = Arrays.asList("Aluno", "Professor", "Coordenador", "Administrador");
            int tipoEscolha = ConsoleUI.exibirMenuInterativo("Tipo de Usuário", tipos);
            if (tipoEscolha == -1) return;

            TipoUsuario tipo = TipoUsuario.values()[tipoEscolha];
            String matricula = service.cadastrarUsuarioComMatriculaAutomatica(nome, email, senha, tipo);
            ConsoleUI.exibirMensagem("Usuário cadastrado! Matrícula: " + matricula, false);
        } catch (Exception e) {
            ConsoleUI.exibirMensagem(e.getMessage(), true);
        }
    }

    /**
     * Edita os dados de um usuário existente.
     * Campos deixados em branco mantêm o valor atual.
     * Permite alterar o cargo, o que gera nova matrícula automaticamente.
     */
    private void editarUsuario() {
        ConsoleUI.limparTela();
        ConsoleUI.exibirCabecalho("EDITAR USUÁRIO");
        try {
            String matricula = ConsoleUI.lerEntrada("Matrícula do usuário: ");
            Usuario usuario = service.buscarUsuarioPorMatricula(matricula);

            System.out.println("\nDados atuais: " + usuario.getNome() + " (" + usuario.getTipo() + ")");

            String novoNome = ConsoleUI.lerEntrada("Novo nome (vazio para manter): ");
            if (novoNome.isEmpty()) novoNome = usuario.getNome();

            String novoEmail = ConsoleUI.lerEntrada("Novo e-mail (vazio para manter): ");
            if (novoEmail.isEmpty()) novoEmail = usuario.getEmail();

            String novaSenha = ConsoleUI.lerSenha("Nova senha (vazio para manter): ");
            if (novaSenha.isEmpty()) novaSenha = usuario.getSenha();

            int mudarCargo = ConsoleUI.exibirMenuInterativo("Alterar cargo?", Arrays.asList("Sim", "Não"));
            if (mudarCargo == 0) {
                List<String> tipos = Arrays.asList("Aluno", "Professor", "Coordenador", "Administrador");
                int tipoEscolha = ConsoleUI.exibirMenuInterativo("Novo Cargo", tipos);
                if (tipoEscolha != -1) {
                    service.editarUsuarioComTipo(matricula, novoNome, novoEmail, novaSenha, TipoUsuario.values()[tipoEscolha]);
                }
            } else {
                service.editarUsuario(matricula, novoNome, novoEmail, novaSenha);
            }

            ConsoleUI.exibirMensagem("Usuário atualizado com sucesso!", false);
        } catch (Exception e) {
            ConsoleUI.exibirMensagem(e.getMessage(), true);
        }
    }

    /** Solicita matrícula, exibe o usuário e pede confirmação antes de deletar. */
    private void deletarUsuario() {
        ConsoleUI.limparTela();
        ConsoleUI.exibirCabecalho("DELETAR USUÁRIO");
        try {
            String matricula = ConsoleUI.lerEntrada("Matrícula do usuário: ");
            Usuario usuario = service.buscarUsuarioPorMatricula(matricula);

            System.out.println("\nUsuário: " + usuario.getNome() + " [" + usuario.getTipo() + "]");
            int escolha = ConsoleUI.exibirMenuInterativo("Tem certeza?", Arrays.asList("Sim, deletar", "Não, cancelar"));

            if (escolha == 0) {
                service.deletarUsuario(matricula);
                ConsoleUI.exibirMensagem("Usuário deletado!", false);
            } else {
                ConsoleUI.exibirMensagem("Operação cancelada.", false);
            }
        } catch (Exception e) {
            ConsoleUI.exibirMensagem(e.getMessage(), true);
        }
    }

    /** Exibe todos os usuários em tabela formatada. */
    private void listarUsuarios() {
        ConsoleUI.limparTela();
        ConsoleUI.exibirCabecalho("LISTA DE USUÁRIOS");
        List<Usuario> usuarios = service.obterTodosUsuarios();

        if (usuarios.isEmpty()) {
            ConsoleUI.exibirMensagem("Nenhum usuário cadastrado.", true);
            return;
        }

        String[] colunas = {"Matrícula", "Nome", "E-mail", "Cargo"};
        List<String[]> linhas = new ArrayList<>();
        for (Usuario u : usuarios) {
            linhas.add(new String[]{u.getMatricula(), u.getNome(), u.getEmail(), u.getTipo().toString()});
        }

        ConsoleUI.exibirTabela(colunas, linhas);
        System.out.println("\nTotal: " + usuarios.size());
        ConsoleUI.exibirMensagem("Fim da lista.", false);
    }
}