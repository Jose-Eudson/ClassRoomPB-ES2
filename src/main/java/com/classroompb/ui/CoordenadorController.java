package com.classroompb.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.classroompb.model.Disciplina;
import com.classroompb.model.TipoUsuario;
import com.classroompb.model.Turma;
import com.classroompb.model.Usuario;
import com.classroompb.service.DisciplinaService;
import com.classroompb.service.PerfilAcessoService;
import com.classroompb.service.PeriodoLetivoService;
import com.classroompb.service.TurmaService;
import com.classroompb.service.UsuarioService;

/**
 * Controlador da interface do Coordenador.
 */
public class CoordenadorController {

    @SuppressWarnings("unused")
    private final UsuarioService service;
    private final DisciplinaService disciplinaService;
    private final PeriodoLetivoService periodoService;
    private final TurmaService turmaService;

    public CoordenadorController(
            UsuarioService service,
            DisciplinaService disciplinaService,
            PeriodoLetivoService periodoService,
            TurmaService turmaService
    ) {
        this.service = service;
        this.disciplinaService = disciplinaService;
        this.periodoService = periodoService;
        this.turmaService = turmaService;
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
                "Gerenciar período letivo",
                "Ofertar turmas",
                "Logout"
            );
            int escolha = ConsoleUI.exibirMenuInterativo("MENU COORDENADOR", opcoes);

            if (escolha == -1 || escolha == opcoes.size() - 1) break;

            switch (escolha) {
                case 0: gerenciarDisciplinas(); break;
                case 1: gerenciarPeriodoLetivo(usuario); break;
                case 2: gerenciarTurmas(usuario); break;
                default:
                    ConsoleUI.exibirMensagem("Funcionalidade disponível na próxima release.", false);
                    break;
            }
        }
    }

    // -------------------------------------------------------------------------
    // Submenu de turmas (RF10)
    // -------------------------------------------------------------------------

    private void gerenciarTurmas(Usuario usuario) {
        while (true) {
            List<String> opcoes = Arrays.asList(
                "Ofertar turma",
                "Editar turma",
                "Excluir turma",
                "Listar turmas por período",
                "Listar turmas por disciplina e período",
                "Voltar"
            );
            int escolha = ConsoleUI.exibirMenuInterativo("GERENCIAR TURMAS", opcoes);

            if (escolha == 5 || escolha == -1) break;

            switch (escolha) {
                case 0: ofertarTurma(usuario);                    break;
                case 1: editarTurma(usuario);                     break;
                case 2: excluirTurma(usuario);                    break;
                case 3: listarTurmasPorPeriodo();                 break;
                case 4: listarTurmasPorDisciplinaEPeriodo();      break;
            }
        }
    }

    private void ofertarTurma(Usuario usuario) {
        ConsoleUI.limparTela();
        ConsoleUI.exibirCabecalho("OFERTAR TURMA");
        try {
            String codigoDisciplina = ConsoleUI.lerEntrada("Código da disciplina: ");
            String codigoPeriodo    = ConsoleUI.lerEntrada("Código do período letivo (ex: 2026.1): ");
            String codigoTurma      = ConsoleUI.lerEntrada("Código da turma (ex: T01): ");
            int vagas               = Integer.parseInt(ConsoleUI.lerEntrada("Número de vagas: "));
            String horario          = ConsoleUI.lerEntrada("Horário (ex: Seg/Qua 10h-12h): ");
            String sala             = ConsoleUI.lerEntrada("Sala (ex: Bloco A - 101): ");
            String professor        = ConsoleUI.lerEntrada("Matrícula do professor responsável: ");

            turmaService.ofertarTurma(
                    usuario,
                    codigoDisciplina,
                    codigoPeriodo,
                    codigoTurma,
                    vagas,
                    horario,
                    sala,
                    professor
            );

            ConsoleUI.exibirMensagem("Turma ofertada com sucesso!", false);
        } catch (NumberFormatException e) {
            ConsoleUI.exibirMensagem("Erro: O número de vagas deve ser um inteiro.", true);
        } catch (Exception e) {
            ConsoleUI.exibirMensagem(e.getMessage(), true);
        }
    }

    private void editarTurma(Usuario usuario) {
        ConsoleUI.limparTela();
        ConsoleUI.exibirCabecalho("EDITAR TURMA");
        try {
            String codigoDisciplina = ConsoleUI.lerEntrada("Código da disciplina: ");
            String codigoPeriodo    = ConsoleUI.lerEntrada("Código do período letivo (ex: 2026.1): ");
            String codigoTurma      = ConsoleUI.lerEntrada("Código da turma (ex: T01): ");

            Turma atual = turmaService.buscarTurma(codigoDisciplina, codigoPeriodo, codigoTurma);

            System.out.println("\nDados atuais:");
            System.out.println("  Vagas  : " + atual.getVagas());
            System.out.println("  Horário: " + atual.getHorario());
            System.out.println("  Sala   : " + atual.getSala());
            System.out.println("  Prof   : " + (atual.getMatriculaProfessor() == null
                    ? "sem professor" : atual.getMatriculaProfessor()));

            String vagasTexto = ConsoleUI.lerEntrada("\nNovas vagas (vazio para manter): ");
            int novasVagas = vagasTexto.trim().isEmpty() ? 0 : Integer.parseInt(vagasTexto);

            String novoHorario = ConsoleUI.lerEntrada("Novo horário (vazio para manter): ");
            String novaSala    = ConsoleUI.lerEntrada("Nova sala (vazio para manter): ");
            String novoProf    = ConsoleUI.lerEntrada(
                    "Nova matrícula do professor (vazio para manter): ");

            // Vazio (ou só espaços) = manter o professor atual
            String matriculaProf = (novoProf == null || novoProf.trim().isEmpty()) ? null : novoProf.trim();

            turmaService.editarTurma(
                    usuario,
                    codigoDisciplina,
                    codigoPeriodo,
                    codigoTurma,
                    novasVagas,
                    novoHorario,
                    novaSala,
                    matriculaProf
            );

            ConsoleUI.exibirMensagem("Turma atualizada com sucesso!", false);
        } catch (NumberFormatException e) {
            ConsoleUI.exibirMensagem("Erro: O número de vagas deve ser um inteiro.", true);
        } catch (Exception e) {
            ConsoleUI.exibirMensagem(e.getMessage(), true);
        }
    }

    private void excluirTurma(Usuario usuario) {
        ConsoleUI.limparTela();
        ConsoleUI.exibirCabecalho("EXCLUIR TURMA");
        try {
            String codigoDisciplina = ConsoleUI.lerEntrada("Código da disciplina: ");
            String codigoPeriodo    = ConsoleUI.lerEntrada("Código do período letivo (ex: 2026.1): ");
            String codigoTurma      = ConsoleUI.lerEntrada("Código da turma (ex: T01): ");

            Turma turma = turmaService.buscarTurma(codigoDisciplina, codigoPeriodo, codigoTurma);

            System.out.println("\n" + turma);
            int escolha = ConsoleUI.exibirMenuInterativo(
                    "Tem certeza que deseja excluir esta turma?",
                    Arrays.asList("Sim, excluir", "Não, cancelar")
            );

            if (escolha == 0) {
                turmaService.excluirTurma(usuario, codigoDisciplina, codigoPeriodo, codigoTurma);
                ConsoleUI.exibirMensagem("Turma excluída com sucesso!", false);
            } else {
                ConsoleUI.exibirMensagem("Operação cancelada.", false);
            }
        } catch (Exception e) {
            ConsoleUI.exibirMensagem(e.getMessage(), true);
        }
    }

    private void listarTurmasPorPeriodo() {
        ConsoleUI.limparTela();
        ConsoleUI.exibirCabecalho("TURMAS POR PERÍODO");
        try {
            String codigoPeriodo = ConsoleUI.lerEntrada("Código do período letivo (ex: 2026.1): ");
            List<Turma> turmas = turmaService.listarTurmasPorPeriodo(codigoPeriodo);

            if (turmas.isEmpty()) {
                ConsoleUI.exibirMensagem("Nenhuma turma ofertada neste período.", true);
                return;
            }

            exibirTabelaTurmas(turmas);
            System.out.println("\nTotal: " + turmas.size());
            ConsoleUI.exibirMensagem("Fim da lista.", false);
        } catch (Exception e) {
            ConsoleUI.exibirMensagem(e.getMessage(), true);
        }
    }

    private void listarTurmasPorDisciplinaEPeriodo() {
        ConsoleUI.limparTela();
        ConsoleUI.exibirCabecalho("TURMAS POR DISCIPLINA E PERÍODO");
        try {
            String codigoDisciplina = ConsoleUI.lerEntrada("Código da disciplina: ");
            String codigoPeriodo    = ConsoleUI.lerEntrada("Código do período letivo (ex: 2026.1): ");
            List<Turma> turmas = turmaService.listarTurmasPorDisciplinaEPeriodo(codigoDisciplina, codigoPeriodo);

            if (turmas.isEmpty()) {
                ConsoleUI.exibirMensagem("Nenhuma turma encontrada para esta disciplina neste período.", true);
                return;
            }

            exibirTabelaTurmas(turmas);
            System.out.println("\nTotal: " + turmas.size());
            ConsoleUI.exibirMensagem("Fim da lista.", false);
        } catch (Exception e) {
            ConsoleUI.exibirMensagem(e.getMessage(), true);
        }
    }

    private void exibirTabelaTurmas(List<Turma> turmas) {
        String[] colunas = {"Código", "Disciplina", "Período", "Vagas", "Horário", "Sala", "Professor"};
        List<String[]> linhas = new ArrayList<String[]>();
        for (Turma t : turmas) {
            linhas.add(new String[]{
                t.getCodigo(),
                t.getCodigoDisciplina(),
                t.getCodigoPeriodo(),
                String.valueOf(t.getVagas()),
                t.getHorario(),
                (t.getSala() == null || t.getSala().isEmpty()) ? "-" : t.getSala(),
                (t.getMatriculaProfessor() == null || t.getMatriculaProfessor().isEmpty())
                        ? "-"
                        : t.getMatriculaProfessor()
            });
        }
        ConsoleUI.exibirTabela(colunas, linhas);
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

    // -------------------------------------------------------------------------
    // Submenu de período letivo
    // -------------------------------------------------------------------------

    private void gerenciarPeriodoLetivo(Usuario usuario) {
        while (true) {
            List<String> opcoes = Arrays.asList(
                "Cadastrar período letivo",
                "Listar períodos letivos",
                "Editar período letivo",
                "Ativar período",
                "Encerrar período",
                "Voltar"
            );
            int escolha = ConsoleUI.exibirMenuInterativo("GERENCIAR PERÍODO LETIVO", opcoes);

            if (escolha == 5 || escolha == -1) break;

            switch (escolha) {
                case 0: cadastrarPeriodoLetivo(usuario); break;
                case 1: listarPeriodos();                break;
                case 2: editarPeriodoLetivo(usuario);    break;
                case 3: ativarPeriodo(usuario);          break;
                case 4: encerrarPeriodo(usuario);        break;
            }
        }
    }

    private void listarPeriodos() {
        ConsoleUI.limparTela();
        ConsoleUI.exibirCabecalho("LISTA DE PERÍODOS LETIVOS");
        List<com.classroompb.model.PeriodoLetivo> periodos = periodoService.listarPeriodos();

        if (periodos.isEmpty()) {
            ConsoleUI.exibirMensagem("Nenhum período letivo cadastrado.", true);
            return;
        }

        String[] colunas = {"Código", "Ano", "Semestre", "Início", "Fim", "Ativo"};
        List<String[]> linhas = new ArrayList<String[]>();
        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (com.classroompb.model.PeriodoLetivo p : periodos) {
            linhas.add(new String[]{
                p.getCodigo(),
                String.valueOf(p.getAno()),
                String.valueOf(p.getSemestre()),
                p.getDataInicio().format(fmt),
                p.getDataFim().format(fmt),
                p.isAtivo() ? "Sim" : "Não"
            });
        }

        ConsoleUI.exibirTabela(colunas, linhas);
        System.out.println("\nTotal: " + periodos.size());
        ConsoleUI.exibirMensagem("Fim da lista.", false);
    }

    private void editarPeriodoLetivo(Usuario usuario) {
        ConsoleUI.limparTela();
        ConsoleUI.exibirCabecalho("EDITAR PERÍODO LETIVO");
        try {
            String codigo = ConsoleUI.lerEntrada("Código do período (ex: 2025.1): ");
            com.classroompb.model.PeriodoLetivo atual = periodoService.buscarPorCodigo(codigo);

            java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
            System.out.println("\nDados atuais: " + atual.getCodigo()
                    + " | " + atual.getDataInicio().format(fmt)
                    + " até " + atual.getDataFim().format(fmt)
                    + " | Ativo: " + (atual.isAtivo() ? "Sim" : "Não"));

            String inicioTexto = ConsoleUI.lerEntrada("Nova data início (dd/mm/aaaa, vazio para manter): ");
            java.time.LocalDate novoInicio = inicioTexto.trim().isEmpty()
                    ? atual.getDataInicio()
                    : java.time.LocalDate.parse(inicioTexto.trim(), fmt);

            String fimTexto = ConsoleUI.lerEntrada("Nova data fim (dd/mm/aaaa, vazio para manter): ");
            java.time.LocalDate novoFim = fimTexto.trim().isEmpty()
                    ? atual.getDataFim()
                    : java.time.LocalDate.parse(fimTexto.trim(), fmt);

            periodoService.editarPeriodo(codigo, novoInicio, novoFim);
            ConsoleUI.exibirMensagem("Período letivo atualizado com sucesso!", false);
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