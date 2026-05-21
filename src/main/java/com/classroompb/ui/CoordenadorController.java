package com.classroompb.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.classroompb.model.Disciplina;
import com.classroompb.model.TipoUsuario;
import com.classroompb.model.Usuario;
import com.classroompb.service.DisciplinaService;
import com.classroompb.service.PerfilAcessoService;
import com.classroompb.service.PeriodoLetivoService;
import com.classroompb.service.UsuarioService;

/**
 * Controlador da interface do Coordenador.
 */
public class CoordenadorController {

    @SuppressWarnings("unused")
    private final UsuarioService service;
    private final DisciplinaService disciplinaService;
    private final PeriodoLetivoService periodoService;

    public CoordenadorController(UsuarioService service, DisciplinaService disciplinaService, PeriodoLetivoService periodoService) {
        this.service = service;
        this.disciplinaService = disciplinaService;
        this.periodoService = periodoService;
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
                "Gerenciar disciplinas",
                "Cadastrar período letivo",
                "Ativar período",
                "Encerrar período",
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
                case 0: gerenciarDisciplinas(); break;
                case 1: cadastrarPeriodoLetivo(usuario); break;
                case 2: ativarPeriodo(usuario); break;
                case 3: encerrarPeriodo(usuario); break;
                default:
                    ConsoleUI.exibirMensagem("Funcionalidade disponível na próxima release.", false);
                    break;
            }
        }
    }

    // -------------------------------------------------------------------------
    // Submenu de disciplinas
    // -------------------------------------------------------------------------

    private void gerenciarDisciplinas() {
        while (true) {
            List<String> opcoes = Arrays.asList(
                "Cadastrar disciplina",
                "Editar disciplina",
                "Listar disciplinas",
                "Deletar disciplina",
                "Voltar"
            );
            int escolha = ConsoleUI.exibirMenuInterativo("GERENCIAR DISCIPLINAS", opcoes);

            if (escolha == 4 || escolha == -1) break;

            switch (escolha) {
                case 0: cadastrarDisciplina(); break;
                case 1: editarDisciplina();    break;
                case 2: listarDisciplinas();   break;
                case 3: deletarDisciplina();   break;
            }
        }
    }

    private void cadastrarDisciplina() {
        ConsoleUI.limparTela();
        ConsoleUI.exibirCabecalho("CADASTRAR DISCIPLINA");
        try {
            String codigo = ConsoleUI.lerEntrada("Codigo da disciplina: ");
            String nome   = ConsoleUI.lerEntrada("Nome da disciplina: ");
            int cargaHoraria = Integer.parseInt(ConsoleUI.lerEntrada("Carga horaria (horas): "));
            int creditos     = Integer.parseInt(ConsoleUI.lerEntrada("Creditos: "));
            String preReqTexto = ConsoleUI.lerEntrada("Pre-requisitos (separados por virgula, ou vazio): ");

            List<String> preReq = preReqTexto.trim().isEmpty()
                    ? new ArrayList<String>()
                    : new ArrayList<String>(Arrays.asList(preReqTexto.split(",")));

            disciplinaService.cadastrarDisciplina(codigo, nome, cargaHoraria, creditos, preReq);
            ConsoleUI.exibirMensagem("Disciplina cadastrada com sucesso!", false);
        } catch (NumberFormatException e) {
            ConsoleUI.exibirMensagem("Erro: Carga horaria e creditos devem ser numeros inteiros.", true);
        } catch (Exception e) {
            ConsoleUI.exibirMensagem(e.getMessage(), true);
        }
    }

    private void editarDisciplina() {
        ConsoleUI.limparTela();
        ConsoleUI.exibirCabecalho("EDITAR DISCIPLINA");
        try {
            String codigo = ConsoleUI.lerEntrada("Codigo da disciplina: ");
            Disciplina atual = disciplinaService.buscarPorCodigo(codigo);

            System.out.println("\nDados atuais: " + atual.getNome()
                    + " | " + atual.getCargaHoraria() + "h"
                    + " | " + atual.getCreditos() + " creditos"
                    + " | Pre-req: " + atual.getPreRequisitos());

            String novoNome = ConsoleUI.lerEntrada("Novo nome (vazio para manter): ");
            if (novoNome.trim().isEmpty()) novoNome = atual.getNome();

            String cargaTexto = ConsoleUI.lerEntrada("Nova carga horaria (vazio para manter): ");
            int novaCarga = cargaTexto.trim().isEmpty()
                    ? atual.getCargaHoraria()
                    : Integer.parseInt(cargaTexto);

            String credTexto = ConsoleUI.lerEntrada("Novos creditos (vazio para manter): ");
            int novosCreditos = credTexto.trim().isEmpty()
                    ? atual.getCreditos()
                    : Integer.parseInt(credTexto);

            String preReqTexto = ConsoleUI.lerEntrada("Novos pre-requisitos (vazio para manter): ");
            List<String> novosPreReq = preReqTexto.trim().isEmpty()
                    ? atual.getPreRequisitos()
                    : new ArrayList<String>(Arrays.asList(preReqTexto.split(",")));

            disciplinaService.editarDisciplina(codigo, novoNome, novaCarga, novosCreditos, novosPreReq);
            ConsoleUI.exibirMensagem("Disciplina atualizada com sucesso!", false);
        } catch (NumberFormatException e) {
            ConsoleUI.exibirMensagem("Erro: Valores numericos invalidos.", true);
        } catch (Exception e) {
            ConsoleUI.exibirMensagem(e.getMessage(), true);
        }
    }

    private void listarDisciplinas() {
        ConsoleUI.limparTela();
        ConsoleUI.exibirCabecalho("LISTA DE DISCIPLINAS");
        List<Disciplina> disciplinas = disciplinaService.listarDisciplinas();

        if (disciplinas.isEmpty()) {
            ConsoleUI.exibirMensagem("Nenhuma disciplina cadastrada.", true);
            return;
        }

        String[] colunas = {"Codigo", "Nome", "Carga Horaria", "Creditos", "Pre-requisitos"};
        List<String[]> linhas = new ArrayList<String[]>();
        for (Disciplina d : disciplinas) {
            linhas.add(new String[]{
                d.getCodigo(),
                d.getNome(),
                d.getCargaHoraria() + "h",
                String.valueOf(d.getCreditos()),
                d.getPreRequisitos().isEmpty() ? "-" : d.getPreRequisitos().toString()
            });
        }

        ConsoleUI.exibirTabela(colunas, linhas);
        System.out.println("\nTotal: " + disciplinas.size());
        ConsoleUI.exibirMensagem("Fim da lista.", false);
    }

    private void deletarDisciplina() {
        ConsoleUI.limparTela();
        ConsoleUI.exibirCabecalho("DELETAR DISCIPLINA");
        try {
            String codigo = ConsoleUI.lerEntrada("Codigo da disciplina: ");
            Disciplina d = disciplinaService.buscarPorCodigo(codigo);

            System.out.println("\nDisciplina: " + d.getNome() + " [" + d.getCodigo() + "]");
            int escolha = ConsoleUI.exibirMenuInterativo("Tem certeza?",
                    Arrays.asList("Sim, deletar", "Nao, cancelar"));

            if (escolha == 0) {
                disciplinaService.deletarDisciplina(codigo);
                ConsoleUI.exibirMensagem("Disciplina deletada com sucesso!", false);
            } else {
                ConsoleUI.exibirMensagem("Operacao cancelada.", false);
            }
        } catch (Exception e) {
            ConsoleUI.exibirMensagem(e.getMessage(), true);
        }
    }

    private void cadastrarPeriodoLetivo(Usuario usuario) {

        ConsoleUI.limparTela();

        ConsoleUI.exibirCabecalho(
                "CADASTRAR PERIODO LETIVO"
        );

        try {

            int ano =
                    Integer.parseInt(
                            ConsoleUI.lerEntrada(
                                    "Ano: "
                            )
                    );

            int semestre =
                    Integer.parseInt(
                            ConsoleUI.lerEntrada(
                                    "Semestre (1 ou 2): "
                            )
                    );

            String codigo = ano + "." + semestre;

            java.time.format.DateTimeFormatter fmt =
                    java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");

            java.time.LocalDate dataInicio =
                    java.time.LocalDate.parse(
                            ConsoleUI.lerEntrada(
                                    "Data inicio (dd/mm/aaaa): "
                            ),
                            fmt
                    );

            java.time.LocalDate dataFim =
                    java.time.LocalDate.parse(
                            ConsoleUI.lerEntrada(
                                    "Data fim (dd/mm/aaaa): "
                            ),
                            fmt
                    );

            String respostaAtivo =
                    ConsoleUI.lerEntrada(
                            "Periodo ativo? (S/N): "
                    ).trim().toUpperCase();

            boolean ativo = respostaAtivo.equals("S");

            periodoService.cadastrarPeriodo(
                    codigo,
                    ano,
                    semestre,
                    dataInicio,
                    dataFim,
                    ativo
            );

            ConsoleUI.exibirMensagem(
                    "Periodo cadastrado com sucesso! Codigo: " + codigo,
                    false
            );

        } catch (Exception e) {

            ConsoleUI.exibirMensagem(
                    e.getMessage(),
                    true
            );
        }
    }

    private void ativarPeriodo(Usuario usuario) {

        try {

            String codigo = ConsoleUI.lerEntrada("Codigo do periodo: ");

            periodoService.ativarPeriodo(usuario, codigo);

            ConsoleUI.exibirMensagem("Periodo ativado com sucesso!", false);

        } catch (Exception e) {

            ConsoleUI.exibirMensagem(e.getMessage(), true);
        }
    }

    private void encerrarPeriodo(Usuario usuario) {

        try {

            String codigo = ConsoleUI.lerEntrada("Codigo do periodo: ");

            periodoService.encerrarPeriodo(usuario, codigo);

            ConsoleUI.exibirMensagem("Periodo encerrado com sucesso!", false);

        } catch (Exception e) {

            ConsoleUI.exibirMensagem(e.getMessage(), true);
        }
    }
}