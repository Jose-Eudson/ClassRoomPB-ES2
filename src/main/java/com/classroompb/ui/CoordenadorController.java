package com.classroompb.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.classroompb.model.Disciplina;
import com.classroompb.model.Historico;
import com.classroompb.model.TipoUsuario;
import com.classroompb.model.Turma;
import com.classroompb.model.Usuario;
import com.classroompb.service.DisciplinaService;
import com.classroompb.service.HistoricoService;
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
    private final com.classroompb.service.MatriculaTurmaService matriculaService;
    private final HistoricoService historicoService;

    public CoordenadorController(UsuarioService service, DisciplinaService disciplinaService,
            PeriodoLetivoService periodoService, TurmaService turmaService,
            com.classroompb.service.MatriculaTurmaService matriculaService) {
        this(service, disciplinaService, periodoService, turmaService, matriculaService, null);
    }

    public CoordenadorController(UsuarioService service, DisciplinaService disciplinaService,
            PeriodoLetivoService periodoService, TurmaService turmaService,
            com.classroompb.service.MatriculaTurmaService matriculaService, HistoricoService historicoService) {
        this.service = service;
        this.disciplinaService = disciplinaService;
        this.periodoService = periodoService;
        this.turmaService = turmaService;
        this.matriculaService = matriculaService;
        this.historicoService = historicoService;
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
            List<String> opcoes = Arrays.asList("Gerenciar disciplinas", "Gerenciar período letivo", "Gerenciar turmas",
                    "Gerenciar solicitações de matrícula", "Relatórios", "Consultar histórico do aluno", "Logout");
            int escolha = ConsoleUI.exibirMenuInterativo("MENU COORDENADOR", opcoes);

            if (escolha == -1 || escolha == opcoes.size() - 1) {
                break;
            }

            switch (escolha) {
            case 0:
                gerenciarDisciplinas(usuario);
                break;
            case 1:
                gerenciarPeriodoLetivo(usuario);
                break;
            case 2:
                gerenciarTurmas(usuario);
                break;
            case 3:
                gerenciarSolicitacoesMatricula(usuario);
                break;
            case 4:
                exibirMenuRelatorios(usuario);
                break;
            case 5:
                consultarHistoricoAluno(usuario);
                break;
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
            List<String> opcoes = Arrays.asList("Ofertar turma", "Editar turma", "Excluir turma",
                    "Listar turmas por período", "Listar turmas por disciplina e período", "Voltar");
            int escolha = ConsoleUI.exibirMenuInterativo("GERENCIAR TURMAS", opcoes);

            if (escolha == 5 || escolha == -1) {
                break;
            }

            switch (escolha) {
            case 0:
                ofertarTurma(usuario);
                break;
            case 1:
                editarTurma(usuario);
                break;
            case 2:
                excluirTurma(usuario);
                break;
            case 3:
                listarTurmasPorPeriodo();
                break;
            case 4:
                listarTurmasPorDisciplinaEPeriodo();
                break;
            }
        }
    }

    private void ofertarTurma(Usuario usuario) {
        ConsoleUI.limparTela();
        ConsoleUI.exibirCabecalho("OFERTAR TURMA");
        try {
            String codigoDisciplina = ConsoleUI.lerEntrada("Código da disciplina: ");
            String codigoPeriodo = ConsoleUI.lerEntrada("Código do período letivo (ex: 2026.1): ");
            String codigoTurma = ConsoleUI.lerEntrada("Código da turma (ex: T01): ");
            int vagas = Integer.parseInt(ConsoleUI.lerEntrada("Número de vagas: "));
            String horario = ConsoleUI.lerEntrada("Horário (ex: Seg/Qua 10h-12h): ");
            String sala = ConsoleUI.lerEntrada("Sala (ex: Bloco A - 101): ");
            String professor = ConsoleUI.lerEntrada("Matrícula do professor responsável: ");

            turmaService.ofertarTurma(usuario, codigoDisciplina, codigoPeriodo, codigoTurma, vagas, horario, sala,
                    professor);

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
            String codigoPeriodo = ConsoleUI.lerEntrada("Código do período letivo (ex: 2026.1): ");
            String codigoTurma = ConsoleUI.lerEntrada("Código da turma (ex: T01): ");

            Turma atual = turmaService.buscarTurma(codigoDisciplina, codigoPeriodo, codigoTurma);

            System.out.println("\nDados atuais:");
            System.out.println("  Vagas  : " + atual.getVagas());
            System.out.println("  Horário: " + atual.getHorario());
            System.out.println("  Sala   : " + atual.getSala());
            System.out.println("  Prof   : "
                    + (atual.getMatriculaProfessor() == null ? "sem professor" : atual.getMatriculaProfessor()));

            String vagasTexto = ConsoleUI.lerEntrada("\nNovas vagas (vazio para manter): ");
            int novasVagas = vagasTexto.trim().isEmpty() ? 0 : Integer.parseInt(vagasTexto);

            String novoHorario = ConsoleUI.lerEntrada("Novo horário (vazio para manter): ");
            String novaSala = ConsoleUI.lerEntrada("Nova sala (vazio para manter): ");
            String novoProf = ConsoleUI.lerEntrada("Nova matrícula do professor (vazio para manter): ");

            // Vazio (ou só espaços) = manter o professor atual
            String matriculaProf = (novoProf == null || novoProf.trim().isEmpty()) ? null : novoProf.trim();

            turmaService.editarTurma(usuario, codigoDisciplina, codigoPeriodo, codigoTurma, novasVagas, novoHorario,
                    novaSala, matriculaProf);

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
            String codigoPeriodo = ConsoleUI.lerEntrada("Código do período letivo (ex: 2026.1): ");
            String codigoTurma = ConsoleUI.lerEntrada("Código da turma (ex: T01): ");

            Turma turma = turmaService.buscarTurma(codigoDisciplina, codigoPeriodo, codigoTurma);

            System.out.println("\n" + turma);
            int escolha = ConsoleUI.exibirMenuInterativo("Tem certeza que deseja excluir esta turma?",
                    Arrays.asList("Sim, excluir", "Não, cancelar"));

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
            String codigoPeriodo = ConsoleUI.lerEntrada("Código do período letivo (ex: 2026.1): ");
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
        String[] colunas = { "Código", "Disciplina", "Período", "Vagas", "Horário", "Sala", "Professor" };
        List<String[]> linhas = new ArrayList<String[]>();
        for (Turma t : turmas) {
            linhas.add(new String[] { t.getCodigo(), t.getCodigoDisciplina(), t.getCodigoPeriodo(),
                    String.valueOf(t.getVagas()), t.getHorario(),
                    (t.getSala() == null || t.getSala().isEmpty()) ? "-" : t.getSala(),
                    (t.getMatriculaProfessor() == null || t.getMatriculaProfessor().isEmpty()) ? "-"
                            : t.getMatriculaProfessor() });
        }
        ConsoleUI.exibirTabela(colunas, linhas);
    }

    // -------------------------------------------------------------------------
    // Submenu de disciplinas
    // -------------------------------------------------------------------------

    private void gerenciarDisciplinas(Usuario usuario) {
        while (true) {
            List<String> opcoes = Arrays.asList("Cadastrar disciplina", "Editar disciplina", "Listar disciplinas",
                    "Deletar disciplina", "Voltar");
            int escolha = ConsoleUI.exibirMenuInterativo("GERENCIAR DISCIPLINAS", opcoes);

            if (escolha == 4 || escolha == -1) {
                break;
            }

            switch (escolha) {
            case 0:
                cadastrarDisciplina(usuario);
                break;
            case 1:
                editarDisciplina(usuario);
                break;
            case 2:
                listarDisciplinas();
                break;
            case 3:
                deletarDisciplina(usuario);
                break;
            }
        }
    }

    private void cadastrarDisciplina(Usuario usuario) {
        ConsoleUI.limparTela();
        ConsoleUI.exibirCabecalho("CADASTRAR DISCIPLINA");
        try {
            String codigo = ConsoleUI.lerEntrada("Codigo da disciplina: ");
            String nome = ConsoleUI.lerEntrada("Nome da disciplina: ");
            int cargaHoraria = Integer.parseInt(ConsoleUI.lerEntrada("Carga horaria (horas): "));
            int creditos = Integer.parseInt(ConsoleUI.lerEntrada("Creditos: "));
            String preReqTexto = ConsoleUI.lerEntrada("Pre-requisitos (separados por virgula, ou vazio): ");

            List<String> preReq = preReqTexto.trim().isEmpty() ? new ArrayList<String>()
                    : new ArrayList<String>(Arrays.asList(preReqTexto.split(",")));

            disciplinaService.cadastrarDisciplina(usuario, codigo, nome, cargaHoraria, creditos, preReq);
            ConsoleUI.exibirMensagem("Disciplina cadastrada com sucesso!", false);
        } catch (NumberFormatException e) {
            ConsoleUI.exibirMensagem("Erro: Carga horaria e creditos devem ser numeros inteiros.", true);
        } catch (Exception e) {
            ConsoleUI.exibirMensagem(e.getMessage(), true);
        }
    }

    private void editarDisciplina(Usuario usuario) {
        ConsoleUI.limparTela();
        ConsoleUI.exibirCabecalho("EDITAR DISCIPLINA");
        try {
            String codigo = ConsoleUI.lerEntrada("Codigo da disciplina: ");
            Disciplina atual = disciplinaService.buscarPorCodigo(codigo);

            System.out.println("\nDados atuais: " + atual.getNome() + " | " + atual.getCargaHoraria() + "h" + " | "
                    + atual.getCreditos() + " creditos" + " | Pre-req: " + atual.getPreRequisitos());

            String novoNome = ConsoleUI.lerEntrada("Novo nome (vazio para manter): ");
            if (novoNome.trim().isEmpty()) {
                novoNome = atual.getNome();
            }

            String cargaTexto = ConsoleUI.lerEntrada("Nova carga horaria (vazio para manter): ");
            int novaCarga = cargaTexto.trim().isEmpty() ? atual.getCargaHoraria() : Integer.parseInt(cargaTexto);

            String credTexto = ConsoleUI.lerEntrada("Novos creditos (vazio para manter): ");
            int novosCreditos = credTexto.trim().isEmpty() ? atual.getCreditos() : Integer.parseInt(credTexto);

            String preReqTexto = ConsoleUI.lerEntrada("Novos pre-requisitos (vazio para manter): ");
            List<String> novosPreReq = preReqTexto.trim().isEmpty() ? atual.getPreRequisitos()
                    : new ArrayList<String>(Arrays.asList(preReqTexto.split(",")));

            disciplinaService.editarDisciplina(usuario, codigo, novoNome, novaCarga, novosCreditos, novosPreReq);
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

        String[] colunas = { "Codigo", "Nome", "Carga Horaria", "Creditos", "Pre-requisitos" };
        List<String[]> linhas = new ArrayList<String[]>();
        for (Disciplina d : disciplinas) {
            linhas.add(new String[] { d.getCodigo(), d.getNome(), d.getCargaHoraria() + "h",
                    String.valueOf(d.getCreditos()),
                    d.getPreRequisitos().isEmpty() ? "-" : d.getPreRequisitos().toString() });
        }

        ConsoleUI.exibirTabela(colunas, linhas);
        System.out.println("\nTotal: " + disciplinas.size());
        ConsoleUI.exibirMensagem("Fim da lista.", false);
    }

    private void deletarDisciplina(Usuario usuario) {
        ConsoleUI.limparTela();
        ConsoleUI.exibirCabecalho("DELETAR DISCIPLINA");
        try {
            String codigo = ConsoleUI.lerEntrada("Codigo da disciplina: ");
            Disciplina d = disciplinaService.buscarPorCodigo(codigo);

            System.out.println("\nDisciplina: " + d.getNome() + " [" + d.getCodigo() + "]");
            int escolha = ConsoleUI.exibirMenuInterativo("Tem certeza?",
                    Arrays.asList("Sim, deletar", "Nao, cancelar"));

            if (escolha == 0) {
                disciplinaService.deletarDisciplina(usuario, codigo);
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
            List<String> opcoes = Arrays.asList("Cadastrar período letivo", "Listar períodos letivos",
                    "Editar período letivo", "Ativar período", "Encerrar período", "Voltar");
            int escolha = ConsoleUI.exibirMenuInterativo("GERENCIAR PERÍODO LETIVO", opcoes);

            if (escolha == 5 || escolha == -1) {
                break;
            }

            switch (escolha) {
            case 0:
                cadastrarPeriodoLetivo(usuario);
                break;
            case 1:
                listarPeriodos();
                break;
            case 2:
                editarPeriodoLetivo(usuario);
                break;
            case 3:
                ativarPeriodo(usuario);
                break;
            case 4:
                encerrarPeriodo(usuario);
                break;
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

        String[] colunas = { "Código", "Ano", "Semestre", "Início", "Fim", "Ativo" };
        List<String[]> linhas = new ArrayList<String[]>();
        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (com.classroompb.model.PeriodoLetivo p : periodos) {
            linhas.add(new String[] { p.getCodigo(), String.valueOf(p.getAno()), String.valueOf(p.getSemestre()),
                    p.getDataInicio().format(fmt), p.getDataFim().format(fmt), p.isAtivo() ? "Sim" : "Não" });
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
            System.out.println("\nDados atuais: " + atual.getCodigo() + " | " + atual.getDataInicio().format(fmt)
                    + " até " + atual.getDataFim().format(fmt) + " | Ativo: " + (atual.isAtivo() ? "Sim" : "Não"));

            String inicioTexto = ConsoleUI.lerEntrada("Nova data início (dd/mm/aaaa, vazio para manter): ");
            java.time.LocalDate novoInicio = inicioTexto.trim().isEmpty() ? atual.getDataInicio()
                    : java.time.LocalDate.parse(inicioTexto.trim(), fmt);

            String fimTexto = ConsoleUI.lerEntrada("Nova data fim (dd/mm/aaaa, vazio para manter): ");
            java.time.LocalDate novoFim = fimTexto.trim().isEmpty() ? atual.getDataFim()
                    : java.time.LocalDate.parse(fimTexto.trim(), fmt);

            periodoService.editarPeriodo(codigo, novoInicio, novoFim);
            ConsoleUI.exibirMensagem("Período letivo atualizado com sucesso!", false);
        } catch (Exception e) {
            ConsoleUI.exibirMensagem(e.getMessage(), true);
        }
    }

    private void cadastrarPeriodoLetivo(Usuario usuario) {
        ConsoleUI.limparTela();

        ConsoleUI.exibirCabecalho("CADASTRAR PERIODO LETIVO");

        try {

            int ano = Integer.parseInt(ConsoleUI.lerEntrada("Ano: "));

            int semestre = Integer.parseInt(ConsoleUI.lerEntrada("Semestre (1 ou 2): "));

            String codigo = ano + "." + semestre;

            java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");

            java.time.LocalDate dataInicio = java.time.LocalDate
                    .parse(ConsoleUI.lerEntrada("Data inicio (dd/mm/aaaa): "), fmt);

            java.time.LocalDate dataFim = java.time.LocalDate.parse(ConsoleUI.lerEntrada("Data fim (dd/mm/aaaa): "),
                    fmt);

            String respostaAtivo = ConsoleUI.lerEntrada("Periodo ativo? (S/N): ").trim().toUpperCase();

            boolean ativo = respostaAtivo.equals("S");

            periodoService.cadastrarPeriodo(codigo, ano, semestre, dataInicio, dataFim, ativo);

            ConsoleUI.exibirMensagem("Periodo cadastrado com sucesso! Codigo: " + codigo, false);

        } catch (Exception e) {

            ConsoleUI.exibirMensagem(e.getMessage(), true);
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

    // -------------------------------------------------------------------------
    // Gestão de Solicitações de Matrícula (RF16)
    // -------------------------------------------------------------------------

    private void gerenciarSolicitacoesMatricula(Usuario usuario) {
        while (true) {
            List<String> opcoes = Arrays.asList("Listar solicitações pendentes", "Listar todas as solicitações",
                    "Listar solicitações por status", "Listar solicitações de uma turma",
                    "Listar lista de espera de uma turma", "Voltar");

            int escolha = ConsoleUI.exibirMenuInterativo("GERENCIAR SOLICITAÇÕES DE MATRÍCULA", opcoes);

            if (escolha == 5 || escolha == -1) {
                break;
            }

            switch (escolha) {
            case 0:
                listarSolicitacoesPendentes(usuario);
                break;
            case 1:
                listarTodasSolicitacoes(usuario);
                break;
            case 2:
                listarSolicitacoesPorStatus(usuario);
                break;
            case 3:
                listarSolicitacoesPorTurma(usuario);
                break;
            case 4:
                listarListaEsperaPorTurma(usuario);
                break;
            }
        }
    }

    private void listarSolicitacoesPendentes(Usuario usuario) {
        ConsoleUI.limparTela();
        ConsoleUI.exibirCabecalho("SOLICITAÇÕES PENDENTES");

        try {
            List<com.classroompb.model.MatriculaTurma> pendentes = matriculaService
                    .listarSolicitacoesPendentes(usuario);

            if (pendentes.isEmpty()) {
                ConsoleUI.exibirMensagem("Não há solicitações pendentes no momento.", false);
                return;
            }

            exibirTabelaSolicitacoes(pendentes);
            processarSolicitacaoPendente(usuario, pendentes);

        } catch (Exception e) {
            ConsoleUI.exibirMensagem(e.getMessage(), true);
        }
    }

    private void listarTodasSolicitacoes(Usuario usuario) {
        ConsoleUI.limparTela();
        ConsoleUI.exibirCabecalho("TODAS AS SOLICITAÇÕES DE MATRÍCULA");

        try {
            List<com.classroompb.model.MatriculaTurma> todas = matriculaService.listarTodasSolicitacoes(usuario);

            if (todas.isEmpty()) {
                ConsoleUI.exibirMensagem("Não há solicitações de matrícula no sistema.", false);
                return;
            }

            exibirTabelaSolicitacoesCompleta(todas);
            System.out.println("\nTotal de solicitações: " + todas.size());
            ConsoleUI.exibirMensagem("Fim da lista.", false);

        } catch (Exception e) {
            ConsoleUI.exibirMensagem(e.getMessage(), true);
        }
    }

    private void listarSolicitacoesPorStatus(Usuario usuario) {
        ConsoleUI.limparTela();
        ConsoleUI.exibirCabecalho("SOLICITAÇÕES POR STATUS");

        try {
            List<com.classroompb.model.StatusMatricula> statusDisponiveis = Arrays.asList(
                    com.classroompb.model.StatusMatricula.PENDENTE, com.classroompb.model.StatusMatricula.CONFIRMADA,
                    com.classroompb.model.StatusMatricula.LISTA_ESPERA, com.classroompb.model.StatusMatricula.CANCELADA,
                    com.classroompb.model.StatusMatricula.REJEITADA);

            List<String> statusOpcoes = Arrays.asList("PENDENTE", "CONFIRMADA", "LISTA_ESPERA", "CANCELADA",
                    "REJEITADA");

            int escolhaStatus = ConsoleUI.exibirMenuInterativo("Selecione o status", statusOpcoes);

            if (escolhaStatus == -1) {
                return;
            }

            com.classroompb.model.StatusMatricula status = statusDisponiveis.get(escolhaStatus);

            List<com.classroompb.model.MatriculaTurma> solicitacoes = matriculaService
                    .listarSolicitacoesPorStatus(usuario, status);

            if (solicitacoes.isEmpty()) {
                ConsoleUI.exibirMensagem("Não há solicitações com o status " + status + ".", false);
                return;
            }

            exibirTabelaSolicitacoesCompleta(solicitacoes);
            System.out.println("\nTotal de solicitações com status " + status + ": " + solicitacoes.size());
            ConsoleUI.exibirMensagem("Fim da lista.", false);

        } catch (Exception e) {
            ConsoleUI.exibirMensagem(e.getMessage(), true);
        }
    }

    private void listarSolicitacoesPorTurma(Usuario usuario) {
        ConsoleUI.limparTela();
        ConsoleUI.exibirCabecalho("SOLICITAÇÕES POR TURMA");

        try {
            String codigoDisciplina = ConsoleUI.lerEntrada("Código da disciplina: ").trim();
            String codigoPeriodo = ConsoleUI.lerEntrada("Código do período letivo (ex: 2026.1): ").trim();
            String codigoTurma = ConsoleUI.lerEntrada("Código da turma (ex: T01): ").trim();

            // Valida se a turma realmente existe
            turmaService.buscarTurma(codigoDisciplina, codigoPeriodo, codigoTurma);

            List<com.classroompb.model.MatriculaTurma> solicitacoes = matriculaService
                    .listarSolicitacoesPorTurma(usuario, codigoDisciplina, codigoPeriodo, codigoTurma);

            if (solicitacoes.isEmpty()) {
                ConsoleUI.exibirMensagem("A turma existe, mas ainda não possui solicitações.", false);
                return;
            }

            exibirTabelaSolicitacoesCompleta(solicitacoes);
            System.out.println("\nTotal de solicitações: " + solicitacoes.size());
            ConsoleUI.exibirMensagem("Fim da lista.", false);

        } catch (Exception e) {
            ConsoleUI.exibirMensagem(e.getMessage(), true);
        }
    }

    private void listarListaEsperaPorTurma(Usuario usuario) {
        ConsoleUI.limparTela();
        ConsoleUI.exibirCabecalho("LISTA DE ESPERA POR TURMA");

        try {
            String codigoDisciplina = ConsoleUI.lerEntrada("Código da disciplina: ").trim();
            String codigoPeriodo = ConsoleUI.lerEntrada("Código do período letivo (ex: 2026.1): ").trim();
            String codigoTurma = ConsoleUI.lerEntrada("Código da turma (ex: T01): ").trim();

            // Valida se a turma realmente existe
            turmaService.buscarTurma(codigoDisciplina, codigoPeriodo, codigoTurma);

            List<com.classroompb.model.MatriculaTurma> listaEspera = matriculaService.listarListaEsperaPorTurma(usuario,
                    codigoDisciplina, codigoPeriodo, codigoTurma);

            if (listaEspera.isEmpty()) {
                ConsoleUI.exibirMensagem("A turma existe, mas não possui alunos em lista de espera.", false);
                return;
            }

            exibirTabelaSolicitacoesCompleta(listaEspera);
            System.out.println("\nTotal de alunos em lista de espera: " + listaEspera.size());
            ConsoleUI.exibirMensagem("Fim da lista.", false);

        } catch (Exception e) {
            ConsoleUI.exibirMensagem(e.getMessage(), true);
        }
    }

    private void exibirTabelaSolicitacoes(List<com.classroompb.model.MatriculaTurma> solicitacoes) {
        String[] colunas = { "#", "Aluno", "Disciplina", "Período", "Turma", "Data" };
        List<String[]> linhas = new ArrayList<>();
        for (int i = 0; i < solicitacoes.size(); i++) {
            com.classroompb.model.MatriculaTurma m = solicitacoes.get(i);
            linhas.add(new String[] { String.valueOf(i + 1), m.getMatriculaAluno(), m.getCodigoDisciplina(),
                    m.getCodigoPeriodo(), m.getCodigoTurma(),
                    m.getDataSolicitacao().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) });
        }
        ConsoleUI.exibirTabela(colunas, linhas);
    }

    private void exibirTabelaSolicitacoesCompleta(List<com.classroompb.model.MatriculaTurma> solicitacoes) {
        String[] colunas = { "#", "Aluno", "Disciplina", "Período", "Turma", "Status", "Data" };
        List<String[]> linhas = new ArrayList<>();
        for (int i = 0; i < solicitacoes.size(); i++) {
            com.classroompb.model.MatriculaTurma m = solicitacoes.get(i);
            linhas.add(new String[] { String.valueOf(i + 1), m.getMatriculaAluno(), m.getCodigoDisciplina(),
                    m.getCodigoPeriodo(), m.getCodigoTurma(), m.getStatus().toString(),
                    m.getDataSolicitacao().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) });
        }
        ConsoleUI.exibirTabela(colunas, linhas);
    }

    // -------------------------------------------------------------------------
    // Relatórios (RF40–RF43)
    // -------------------------------------------------------------------------

    private void exibirMenuRelatorios(Usuario usuario) {
        while (true) {
            List<String> opcoes = Arrays.asList("RF40 - Alunos matriculados por turma",
                    "RF41 - Ocupação de vagas por período", "RF42 - Reprovados por disciplina",
                    "Consultar histórico acadêmico de aluno", "Voltar");
            int escolha = ConsoleUI.exibirMenuInterativo("RELATÓRIOS", opcoes);

            if (escolha == 4 || escolha == -1) {
                break;
            }

            switch (escolha) {
            case 0:
                relatorioAlunosMatriculados(usuario);
                break;
            case 1:
                relatorioOcupacaoVagas(usuario);
                break;
            case 2:
                relatorioReprovadosPorDisciplina(usuario);
                break;
            case 3:
                consultarHistoricoAluno(usuario);
                break;
            default:
                break;
            }
        }
    }

    private void relatorioOcupacaoVagas(Usuario usuario) {
        ConsoleUI.limparTela();
        ConsoleUI.exibirCabecalho("RF41 - OCUPAÇÃO DE VAGAS POR PERÍODO");
        try {
            String codigoPeriodo = ConsoleUI.lerEntrada("Código do período letivo (ex: 2026.1): ").trim();
            List<Turma> turmas = matriculaService.listarTurmasComOcupacaoPorPeriodo(usuario, codigoPeriodo);

            if (turmas.isEmpty()) {
                ConsoleUI.exibirMensagem("Nenhuma turma ofertada neste período.", false);
                return;
            }

            String[] colunas = { "Turma", "Disciplina", "Total Vagas", "Ocupadas", "Disponíveis" };
            List<String[]> linhas = new ArrayList<>();
            for (Turma t : turmas) {
                long disponiveis = matriculaService.vagasDisponiveis(t);
                long ocupadas = t.getVagas() - disponiveis;
                linhas.add(new String[] { t.getCodigo(), t.getCodigoDisciplina(), String.valueOf(t.getVagas()),
                        String.valueOf(ocupadas), String.valueOf(disponiveis) });
            }

            ConsoleUI.exibirTabela(colunas, linhas);
            System.out.println("\nTotal de turmas: " + turmas.size());
            ConsoleUI.exibirMensagem("Fim do relatório.", false);
        } catch (Exception e) {
            ConsoleUI.exibirMensagem(e.getMessage(), true);
        }
    }

    private void relatorioReprovadosPorDisciplina(Usuario usuario) {
        ConsoleUI.limparTela();
        ConsoleUI.exibirCabecalho("RF42 - REPROVADOS POR DISCIPLINA");
        try {
            if (historicoService == null) {
                ConsoleUI.exibirMensagem("Funcionalidade disponível na próxima release.", false);
                return;
            }
            String codigoDisciplina = ConsoleUI.lerEntrada("Código da disciplina: ").trim();
            List<Historico> reprovados = historicoService.listarReprovadosPorDisciplina(usuario, codigoDisciplina);

            if (reprovados.isEmpty()) {
                ConsoleUI.exibirMensagem("Nenhum aluno reprovado nesta disciplina.", false);
                return;
            }

            String[] colunas = { "Aluno", "Período", "Nota Final", "Frequência", "Situação" };
            List<String[]> linhas = new ArrayList<>();
            for (Historico h : reprovados) {
                linhas.add(new String[] { h.getMatriculaAluno(), h.getCodigoPeriodo(),
                        String.format("%.1f", h.getNotaFinal()), String.format("%.1f%%", h.getFrequencia()),
                        h.getSituacao() });
            }

            ConsoleUI.exibirTabela(colunas, linhas);
            System.out.println("\nTotal de reprovados: " + reprovados.size());
            ConsoleUI.exibirMensagem("Fim do relatório.", false);
        } catch (Exception e) {
            ConsoleUI.exibirMensagem(e.getMessage(), true);
        }
    }

    private void consultarHistoricoAluno(Usuario coordenador) {
        ConsoleUI.limparTela();
        ConsoleUI.exibirCabecalho("HISTÓRICO ACADÊMICO DE ALUNO");
        try {
            if (historicoService == null) {
                ConsoleUI.exibirMensagem("Funcionalidade disponível na próxima release.", false);
                return;
            }
            String matricula = ConsoleUI.lerEntrada("Matrícula do aluno: ");
            List<Historico> historicos = historicoService.consultarHistoricoAlunoPeloCoordenador(coordenador,
                    matricula);
            if (historicos.isEmpty()) {
                ConsoleUI.exibirMensagem("O aluno ainda não possui histórico acadêmico.", false);
                return;
            }

            String[] colunas = { "Período", "Disciplina", "Professor", "Nota final", "Frequência", "Situação" };
            List<String[]> linhas = new ArrayList<>();
            for (Historico item : historicos) {
                linhas.add(new String[] { item.getCodigoPeriodo(), item.getNomeDisciplina(), item.getNomeProfessor(),
                        String.format("%.1f", item.getNotaFinal()), String.format("%.1f%%", item.getFrequencia()),
                        item.getSituacao() });
            }
            ConsoleUI.exibirTabela(colunas, linhas);
            ConsoleUI.aguardarEnter();
        } catch (Exception e) {
            ConsoleUI.exibirMensagem(e.getMessage(), true);
        }
    }

    private void relatorioAlunosMatriculados(Usuario usuario) {
        ConsoleUI.limparTela();
        ConsoleUI.exibirCabecalho("ALUNOS MATRICULADOS POR TURMA");
        try {
            String codigoDisciplina = ConsoleUI.lerEntrada("Código da disciplina: ").trim();
            String codigoPeriodo = ConsoleUI.lerEntrada("Código do período letivo (ex: 2026.1): ").trim();
            String codigoTurma = ConsoleUI.lerEntrada("Código da turma (ex: T01): ").trim();

            List<com.classroompb.model.MatriculaTurma> alunos = matriculaService
                    .listarAlunosMatriculadosPorTurma(usuario, codigoDisciplina, codigoPeriodo, codigoTurma);

            if (alunos.isEmpty()) {
                ConsoleUI.exibirMensagem("Nenhum aluno matriculado nesta turma.", false);
                return;
            }

            String[] colunas = { "#", "Matrícula do Aluno", "Disciplina", "Período", "Turma", "Data de Matrícula" };
            List<String[]> linhas = new ArrayList<>();
            java.time.format.DateTimeFormatter fmtRel = java.time.format.DateTimeFormatter
                    .ofPattern("dd/MM/yyyy HH:mm");
            for (int i = 0; i < alunos.size(); i++) {
                com.classroompb.model.MatriculaTurma m = alunos.get(i);
                linhas.add(new String[] { String.valueOf(i + 1), m.getMatriculaAluno(), m.getCodigoDisciplina(),
                        m.getCodigoPeriodo(), m.getCodigoTurma(), m.getDataSolicitacao().format(fmtRel) });
            }

            ConsoleUI.exibirTabela(colunas, linhas);
            System.out.println("\nTotal de alunos matriculados: " + alunos.size());
            ConsoleUI.exibirMensagem("Fim do relatório.", false);
        } catch (Exception e) {
            ConsoleUI.exibirMensagem(e.getMessage(), true);
        }
    }

    private void processarSolicitacaoPendente(Usuario usuario, List<com.classroompb.model.MatriculaTurma> pendentes) {
        try {
            List<String> opcoes = Arrays.asList("Aprovar solicitação", "Negar solicitação", "Voltar");
            int escolha = ConsoleUI.exibirMenuInterativo("AÇÕES", opcoes);

            if (escolha == 2 || escolha == -1) {
                return;
            }

            String indiceStr = ConsoleUI.lerEntrada("Digite o número da solicitação: ");
            int indice = Integer.parseInt(indiceStr) - 1;

            if (indice < 0 || indice >= pendentes.size()) {
                ConsoleUI.exibirMensagem("Índice inválido.", true);
                return;
            }

            com.classroompb.model.MatriculaTurma selecionada = pendentes.get(indice);

            if (escolha == 0) {
                matriculaService.aprovarMatricula(usuario, selecionada.getMatriculaAluno(),
                        selecionada.getCodigoDisciplina(), selecionada.getCodigoPeriodo(),
                        selecionada.getCodigoTurma());
                ConsoleUI.exibirMensagem("Solicitação aprovada com sucesso!", false);
            } else {
                matriculaService.negarMatricula(usuario, selecionada.getMatriculaAluno(),
                        selecionada.getCodigoDisciplina(), selecionada.getCodigoPeriodo(),
                        selecionada.getCodigoTurma());
                ConsoleUI.exibirMensagem("Solicitação negada com sucesso!", false);
            }

        } catch (NumberFormatException e) {
            ConsoleUI.exibirMensagem("Erro: Digite um número válido.", true);
        } catch (Exception e) {
            ConsoleUI.exibirMensagem(e.getMessage(), true);
        }
    }
}
