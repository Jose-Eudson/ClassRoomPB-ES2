package com.classroompb.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.classroompb.model.Curso;
import com.classroompb.model.TipoUsuario;
import com.classroompb.model.Usuario;
import com.classroompb.service.CursoService;
import com.classroompb.service.PerfilAcessoService;
import com.classroompb.service.UsuarioService;

/**
 * Controlador da interface do Administrador. Responsável pelo menu e todas as ações disponíveis para esse perfil.
 */
public class AdminController {

    private final UsuarioService service;
    private final CursoService cursoService;

    public AdminController(UsuarioService service, CursoService cursoService) {
        this.service = service;
        this.cursoService = cursoService;
    }

    /** Exibe o menu principal do administrador e permanece em loop até logout. */
    public void exibirMenu(Usuario usuario) {
        try {
            PerfilAcessoService.validarPerfil(usuario, TipoUsuario.ADMINISTRADOR);
        } catch (Exception e) {
            ConsoleUI.exibirMensagem(e.getMessage(), true);
            return;
        }

        while (true) {
            List<String> opcoes = Arrays.asList("Gerenciar usuários", "Gerenciar cursos", "Logout");
            int escolha = ConsoleUI.exibirMenuInterativo("MENU ADMINISTRADOR", opcoes);

            if (escolha == -1 || escolha == opcoes.size() - 1) {
                break;
            }

            switch (escolha) {
            case 0:
                gerenciarUsuarios();
                break;
            case 1:
                gerenciarCursos(usuario);
                break;
            }
        }
    }

    // -------------------------------------------------------------------------
    // Submenu de gerenciamento
    // -------------------------------------------------------------------------

    private void gerenciarUsuarios() {
        while (true) {
            List<String> opcoes = Arrays.asList("Cadastrar novo usuário", "Editar usuário", "Deletar usuário",
                    "Listar usuários", "Voltar");
            int escolha = ConsoleUI.exibirMenuInterativo("GERENCIAR USUÁRIOS", opcoes);

            if (escolha == 4 || escolha == -1) {
                break;
            }

            switch (escolha) {
            case 0:
                cadastrarUsuario();
                break;
            case 1:
                editarUsuario();
                break;
            case 2:
                deletarUsuario();
                break;
            case 3:
                listarUsuarios();
                break;
            }
        }
    }

    private void gerenciarCursos(Usuario usuario) {
        while (true) {
            List<String> opcoes = Arrays.asList("Cadastrar novo curso", "Editar curso", "Listar cursos",
                    "Deletar curso", "Voltar");
            int escolha = ConsoleUI.exibirMenuInterativo("GERENCIAR CURSOS", opcoes);

            if (escolha == 4 || escolha == -1) {
                break;
            }

            switch (escolha) {
            case 0:
                cadastrarCurso(usuario);
                break;
            case 1:
                editarCurso(usuario);
                break;
            case 2:
                listarCursos();
                break;
            case 3:
                deletarCurso(usuario);
                break;
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
            String nome = ConsoleUI.lerEntrada("Nome: ");
            String email = ConsoleUI.lerEntrada("E-mail: ");
            String senha = ConsoleUI.lerSenha("Senha: ");

            List<String> tipos = Arrays.asList("Aluno", "Professor", "Coordenador", "Administrador");
            int tipoEscolha = ConsoleUI.exibirMenuInterativo("Tipo de Usuário", tipos);
            if (tipoEscolha == -1) {
                return;
            }

            TipoUsuario tipo = TipoUsuario.values()[tipoEscolha];
            String matricula = service.cadastrarUsuarioComMatriculaAutomatica(nome, email, senha, tipo);
            ConsoleUI.exibirMensagem("Usuário cadastrado! Matrícula: " + matricula, false);
        } catch (Exception e) {
            ConsoleUI.exibirMensagem(e.getMessage(), true);
        }
    }

    /**
     * Edita os dados de um usuário existente. Campos deixados em branco mantêm o valor atual. Permite alterar o cargo,
     * o que gera nova matrícula automaticamente.
     */
    private void editarUsuario() {
        ConsoleUI.limparTela();
        ConsoleUI.exibirCabecalho("EDITAR USUÁRIO");
        try {
            String matricula = ConsoleUI.lerEntrada("Matrícula do usuário: ");
            Usuario usuario = service.buscarUsuarioPorMatricula(matricula);

            System.out.println("\nDados atuais: " + usuario.getNome() + " (" + usuario.getTipo() + ")");

            String novoNome = ConsoleUI.lerEntrada("Novo nome (vazio para manter): ");
            if (novoNome.isEmpty()) {
                novoNome = usuario.getNome();
            }

            String novoEmail = ConsoleUI.lerEntrada("Novo e-mail (vazio para manter): ");
            if (novoEmail.isEmpty()) {
                novoEmail = usuario.getEmail();
            }

            String novaSenha = ConsoleUI.lerSenha("Nova senha (vazio para manter): ");
            if (novaSenha.isEmpty()) {
                novaSenha = usuario.getSenha();
            }

            int mudarCargo = ConsoleUI.exibirMenuInterativo("Alterar cargo?", Arrays.asList("Sim", "Não"));
            if (mudarCargo == 0) {
                List<String> tipos = Arrays.asList("Aluno", "Professor", "Coordenador", "Administrador");
                int tipoEscolha = ConsoleUI.exibirMenuInterativo("Novo Cargo", tipos);
                if (tipoEscolha != -1) {
                    service.editarUsuarioComTipo(matricula, novoNome, novoEmail, novaSenha,
                            TipoUsuario.values()[tipoEscolha]);
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
            int escolha = ConsoleUI.exibirMenuInterativo("Tem certeza?",
                    Arrays.asList("Sim, deletar", "Não, cancelar"));

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

        String[] colunas = { "Matrícula", "Nome", "E-mail", "Cargo" };
        List<String[]> linhas = new ArrayList<>();
        for (Usuario u : usuarios) {
            linhas.add(new String[] { u.getMatricula(), u.getNome(), u.getEmail(), u.getTipo().toString() });
        }

        ConsoleUI.exibirTabela(colunas, linhas);
        System.out.println("\nTotal: " + usuarios.size());
        ConsoleUI.exibirMensagem("Fim da lista.", false);
    }

    /** Coleta dados e cadastra um novo curso. */
    private void cadastrarCurso(Usuario usuario) {
        ConsoleUI.limparTela();
        ConsoleUI.exibirCabecalho("CADASTRAR NOVO CURSO");
        try {
            String codigo = ConsoleUI.lerEntrada("Código do curso: ");
            String nome = ConsoleUI.lerEntrada("Nome do curso: ");
            String cargaHorariaTexto = ConsoleUI.lerEntrada("Carga horária (horas): ");

            int cargaHoraria = Integer.parseInt(cargaHorariaTexto);
            cursoService.cadastrarCurso(usuario, codigo, nome, cargaHoraria);
            ConsoleUI.exibirMensagem("Curso cadastrado com sucesso!", false);
        } catch (NumberFormatException e) {
            ConsoleUI.exibirMensagem("Erro: Carga horária deve ser um número inteiro.", true);
        } catch (Exception e) {
            ConsoleUI.exibirMensagem(e.getMessage(), true);
        }
    }

    /** Edita nome e carga horária de um curso existente. Campos em branco mantêm o valor atual. */
    private void editarCurso(Usuario usuario) {
        ConsoleUI.limparTela();
        ConsoleUI.exibirCabecalho("EDITAR CURSO");
        try {
            String codigo = ConsoleUI.lerEntrada("Código do curso: ");
            com.classroompb.model.Curso atual = cursoService.buscarPorCodigo(codigo);

            System.out.println("\nDados atuais: " + atual.getNome() + " | " + atual.getCargaHoraria() + "h");

            String novoNome = ConsoleUI.lerEntrada("Novo nome (vazio para manter): ");
            if (novoNome.trim().isEmpty()) {
                novoNome = atual.getNome();
            }

            String cargaTexto = ConsoleUI.lerEntrada("Nova carga horária (vazio para manter): ");
            int novaCarga = cargaTexto.trim().isEmpty() ? atual.getCargaHoraria() : Integer.parseInt(cargaTexto);

            cursoService.editarCurso(usuario, codigo, novoNome, novaCarga);
            ConsoleUI.exibirMensagem("Curso atualizado com sucesso!", false);
        } catch (NumberFormatException e) {
            ConsoleUI.exibirMensagem("Erro: Carga horária deve ser um número inteiro.", true);
        } catch (Exception e) {
            ConsoleUI.exibirMensagem(e.getMessage(), true);
        }
    }

    /** Solicita código, exibe o curso e pede confirmação antes de deletar. */
    private void deletarCurso(Usuario usuario) {
        ConsoleUI.limparTela();
        ConsoleUI.exibirCabecalho("DELETAR CURSO");
        try {
            String codigo = ConsoleUI.lerEntrada("Código do curso: ");
            com.classroompb.model.Curso curso = cursoService.buscarPorCodigo(codigo);

            System.out.println("\nCurso: " + curso.getNome() + " [" + curso.getCodigo() + "]");
            int escolha = ConsoleUI.exibirMenuInterativo("Tem certeza?",
                    Arrays.asList("Sim, deletar", "Não, cancelar"));

            if (escolha == 0) {
                cursoService.deletarCurso(usuario, codigo);
                ConsoleUI.exibirMensagem("Curso deletado com sucesso!", false);
            } else {
                ConsoleUI.exibirMensagem("Operação cancelada.", false);
            }
        } catch (Exception e) {
            ConsoleUI.exibirMensagem(e.getMessage(), true);
        }
    }

    /** Exibe todos os cursos em tabela formatada. */
    private void listarCursos() {
        ConsoleUI.limparTela();
        ConsoleUI.exibirCabecalho("LISTA DE CURSOS");
        List<Curso> cursos = cursoService.obterTodosCursos();

        if (cursos.isEmpty()) {
            ConsoleUI.exibirMensagem("Nenhum curso cadastrado.", true);
            return;
        }

        String[] colunas = { "Código", "Nome", "Carga Horária" };
        List<String[]> linhas = new ArrayList<>();
        for (Curso c : cursos) {
            linhas.add(new String[] { c.getCodigo(), c.getNome(), c.getCargaHoraria() + "h" });
        }

        ConsoleUI.exibirTabela(colunas, linhas);
        System.out.println("\nTotal: " + cursos.size());
        ConsoleUI.exibirMensagem("Fim da lista.", false);
    }
}
