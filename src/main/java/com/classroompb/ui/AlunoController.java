package com.classroompb.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.classroompb.model.Disciplina;
import com.classroompb.model.Historico;
import com.classroompb.model.MatriculaTurma;
import com.classroompb.model.Nota;
import com.classroompb.model.PeriodoLetivo;
import com.classroompb.model.RegistroFrequencia;
import com.classroompb.model.StatusMatricula;
import com.classroompb.model.TipoUsuario;
import com.classroompb.model.Turma;
import com.classroompb.model.Usuario;
import com.classroompb.service.DisciplinaService;
import com.classroompb.service.FrequenciaService;
import com.classroompb.service.HistoricoService;
import com.classroompb.service.ConsultaAcademicaService;
import com.classroompb.service.MatriculaTurmaService;
import com.classroompb.service.NotaService;
import com.classroompb.service.PerfilAcessoService;
import com.classroompb.service.PeriodoLetivoService;
import com.classroompb.service.TurmaService;
import com.classroompb.service.UsuarioService;

/**
 * Controlador da interface do Aluno. Responsável pelo menu e todas as ações disponíveis para esse perfil.
 */
public class AlunoController {

    @SuppressWarnings("unused")
    private final UsuarioService service;
    private final DisciplinaService disciplinaService;
    private final PeriodoLetivoService periodoLetivoService;
    private final TurmaService turmaService;
    private final MatriculaTurmaService matriculaService;
    private final FrequenciaService freqService;
    private final NotaService notaService;
    private final HistoricoService historicoService;
    private final ConsultaAcademicaService consultaAcademicaService;

    public AlunoController(UsuarioService service) {
        this(service, null, null, null, null, null, null, null, null);
    }

    public AlunoController(UsuarioService service, DisciplinaService disciplinaService,
            PeriodoLetivoService periodoLetivoService, TurmaService turmaService) {
        this(service, disciplinaService, periodoLetivoService, turmaService, null, null, null, null, null);
    }

    public AlunoController(UsuarioService service, DisciplinaService disciplinaService,
            PeriodoLetivoService periodoLetivoService, TurmaService turmaService,
            MatriculaTurmaService matriculaService, FrequenciaService freqService, NotaService notaService) {
        this(service, disciplinaService, periodoLetivoService, turmaService, matriculaService, freqService, notaService,
                null, null);
    }

    public AlunoController(UsuarioService service, DisciplinaService disciplinaService,
            PeriodoLetivoService periodoLetivoService, TurmaService turmaService,
            MatriculaTurmaService matriculaService, FrequenciaService freqService, NotaService notaService,
            HistoricoService historicoService) {
        this(service, disciplinaService, periodoLetivoService, turmaService, matriculaService, freqService, notaService,
                historicoService, null);
    }

    @SuppressWarnings("checkstyle:ParameterNumber")
    public AlunoController(UsuarioService service, DisciplinaService disciplinaService,
            PeriodoLetivoService periodoLetivoService, TurmaService turmaService,
            MatriculaTurmaService matriculaService, FrequenciaService freqService, NotaService notaService,
            HistoricoService historicoService, ConsultaAcademicaService consultaAcademicaService) {
        this.service = service;
        this.disciplinaService = disciplinaService;
        this.periodoLetivoService = periodoLetivoService;
        this.turmaService = turmaService;
        this.matriculaService = matriculaService;
        this.freqService = freqService;
        this.notaService = notaService;
        this.historicoService = historicoService;
        this.consultaAcademicaService = consultaAcademicaService;
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
            List<String> opcoes = Arrays.asList("Consultar disciplinas e turmas", "Solicitar matrícula em turma",
                    "Cancelar matrícula", "Minhas solicitações de matrícula", "Consultar frequência por disciplina",
                    "Consultar notas", "Consultar histórico de disciplinas", "Consultar meus diários", "Logout");
            int escolha = ConsoleUI.exibirMenuInterativo("MENU ALUNO", opcoes);

            if (escolha == -1 || escolha == opcoes.size() - 1) {
                break;
            }

            switch (escolha) {
            case 0:
                consultarDisciplinasETurmas();
                break;
            case 1:
                solicitarMatricula(usuario);
                break;
            case 2:
                cancelarSolicitacaoMatricula(usuario);
                break;
            case 3:
                listarMinhasSolicitacoes(usuario);
                break;
            case 4:
                consultarFrequenciaPorDisciplina(usuario);
                break;
            case 5:
                consultarNotas(usuario);
                break;
            case 6:
                consultarHistorico(usuario);
                break;
            case 7:
                consultarMeusDiarios(usuario);
                break;
            default:
                ConsoleUI.exibirMensagem("Funcionalidade disponível na próxima release.", false);
                break;
            }
        }
    }

    // -------------------------------------------------------------------------
    // RF15: Consulta de disciplinas e turmas disponíveis
    // -------------------------------------------------------------------------

    /**
     * Fluxo principal de consulta de disciplinas e turmas. Permite ao aluno visualizar disciplinas cadastradas e as
     * turmas ofertadas no período letivo ativo.
     */
    private void consultarDisciplinasETurmas() {
        if (disciplinaService == null || periodoLetivoService == null || turmaService == null) {
            ConsoleUI.exibirMensagem("Funcionalidade disponível na próxima release.", false);
            return;
        }

        while (true) {
            List<String> opcoes = Arrays.asList("Listar todas as disciplinas", "Listar turmas do período ativo",
                    "Buscar turmas por disciplina", "Voltar");
            int escolha = ConsoleUI.exibirMenuInterativo("CONSULTAR DISCIPLINAS E TURMAS", opcoes);

            if (escolha == -1 || escolha == opcoes.size() - 1) {
                return;
            }

            switch (escolha) {
            case 0:
                listarTodasDisciplinas();
                break;
            case 1:
                listarTurmasDoPeriodoAtivo();
                break;
            case 2:
                buscarTurmasPorDisciplina();
                break;
            }
        }
    }

    private void consultarNotas(Usuario aluno) {

        try {

            MatriculaTurma matricula = selecionarTurmaDoAluno(aluno);

            if (matriculaService == null) {
                return;
            }

            Nota nota = notaService.consultarNotas(aluno.getMatricula(), matricula.getCodigoDisciplina(),
                    matricula.getCodigoPeriodo(), matricula.getCodigoTurma());

            ConsoleUI.limparTela();
            ConsoleUI.exibirCabecalho("CONSULTA DE NOTAS");

            System.out.println("Disciplina: " + matricula.getCodigoDisciplina());
            System.out.println();

            System.out.println("Etapa 1: " + nota.getEtapa1());
            System.out.println("Etapa 2: " + nota.getEtapa2());

            double media = (nota.getEtapa1() + nota.getEtapa2()) / 2.0;
            double percentualFrequencia = freqService.calcularPercentualFrequencia(aluno.getMatricula(),
                    matricula.getCodigoDisciplina(), matricula.getCodigoPeriodo(), matricula.getCodigoTurma());

            System.out.printf("Média: %.2f%n", media);
            System.out.println("Situação: "
                    + notaService.calcularSituacaoFinal(aluno.getMatricula(), matricula.getCodigoDisciplina(),
                            matricula.getCodigoPeriodo(), matricula.getCodigoTurma(), percentualFrequencia));

            ConsoleUI.aguardarEnter();

        } catch (Exception e) {
            ConsoleUI.exibirMensagem(e.getMessage(), true);
        }
    }

    private void consultarHistorico(Usuario aluno) {
        try {
            if (historicoService == null) {
                ConsoleUI.exibirMensagem("Funcionalidade disponível na próxima release.", false);
                return;
            }

            List<Historico> historico = historicoService.consultarHistoricoAluno(aluno);

            ConsoleUI.limparTela();
            ConsoleUI.exibirCabecalho("HISTÓRICO DE DISCIPLINAS");

            if (historico.isEmpty()) {
                ConsoleUI.exibirMensagem("Nenhuma disciplina concluída registrada ainda.", false);
                return;
            }

            String[] colunas = { "Período", "Disciplina", "Professor", "Nota final", "Frequência", "Situação" };
            List<String[]> linhas = new ArrayList<>();
            for (Historico item : historico) {
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

    private MatriculaTurma selecionarTurmaDoAluno(Usuario aluno) throws Exception {

        List<MatriculaTurma> matriculas = matriculaService.listarTurmasDoAluno(aluno);

        if (matriculas.isEmpty()) {
            ConsoleUI.exibirMensagem("Voce nao possui matriculas confirmadas.", false);
            return null;
        }

        List<String> opcoes = matriculas.stream()
                .map(m -> m.getCodigoDisciplina() + " | " + m.getCodigoPeriodo() + " | " + m.getCodigoTurma())
                .collect(Collectors.toList());

        opcoes.add("Cancelar");

        int escolha = ConsoleUI.exibirMenuInterativo("SELECIONAR TURMA", opcoes);

        if (escolha == -1 || escolha == opcoes.size() - 1) {
            return null;
        }

        return matriculas.get(escolha);
    }

    /** Lista todas as disciplinas cadastradas no sistema em formato de tabela. */
    private void listarTodasDisciplinas() {
        List<Disciplina> disciplinas = disciplinaService.listarDisciplinas();

        ConsoleUI.limparTela();
        ConsoleUI.exibirCabecalho("DISCIPLINAS CADASTRADAS");

        if (disciplinas.isEmpty()) {
            ConsoleUI.exibirMensagem("Nenhuma disciplina cadastrada no sistema.", false);
            return;
        }

        String[] colunas = { "Código", "Nome", "C.H. (h)", "Créditos", "Pré-requisitos" };
        List<String[]> linhas = new ArrayList<>();
        for (Disciplina d : disciplinas) {
            String preReqs = (d.getPreRequisitos() == null || d.getPreRequisitos().isEmpty()) ? "Nenhum"
                    : String.join(", ", d.getPreRequisitos());
            linhas.add(new String[] { d.getCodigo(), d.getNome(), String.valueOf(d.getCargaHoraria()),
                    String.valueOf(d.getCreditos()), preReqs });
        }

        ConsoleUI.exibirTabela(colunas, linhas);
        System.out.println("Total: " + disciplinas.size() + " disciplina(s).");
        System.out.println("\nPressione ENTER para continuar...");
        lerEntradaSilenciosa();
    }

    /** Lista todas as turmas ofertadas no período letivo atualmente ativo. */
    private void listarTurmasDoPeriodoAtivo() {
        ConsoleUI.limparTela();
        ConsoleUI.exibirCabecalho("TURMAS DO PERÍODO ATIVO");

        // Localiza o período ativo
        PeriodoLetivo periodoAtivo = encontrarPeriodoAtivo();
        if (periodoAtivo == null) {
            ConsoleUI.exibirMensagem("Nenhum período letivo ativo no momento.", false);
            return;
        }

        System.out.println("  Período: " + periodoAtivo.getCodigo() + " | " + periodoAtivo.getDataInicio() + " até "
                + periodoAtivo.getDataFim() + "\n");

        List<Turma> turmas = turmaService.listarTurmasPorPeriodo(periodoAtivo.getCodigo());

        if (turmas.isEmpty()) {
            ConsoleUI.exibirMensagem("Nenhuma turma ofertada neste período.", false);
            return;
        }

        exibirTabelaTurmas(turmas);
        System.out.println("Total: " + turmas.size() + " turma(s).");
        System.out.println("\nPressione ENTER para continuar...");
        lerEntradaSilenciosa();
    }

    /**
     * Permite ao aluno buscar turmas de uma disciplina específica dentro do período letivo ativo.
     */
    private void buscarTurmasPorDisciplina() {
        ConsoleUI.limparTela();
        ConsoleUI.exibirCabecalho("BUSCAR TURMAS POR DISCIPLINA");

        // Localiza o período ativo
        PeriodoLetivo periodoAtivo = encontrarPeriodoAtivo();
        if (periodoAtivo == null) {
            ConsoleUI.exibirMensagem("Nenhum período letivo ativo no momento.", false);
            return;
        }

        String codigoDisciplina = ConsoleUI.lerEntrada("Código da disciplina: ").trim();
        if (codigoDisciplina.isEmpty()) {
            ConsoleUI.exibirMensagem("Código da disciplina não pode ser vazio.", true);
            return;
        }

        // Valida se a disciplina existe
        Disciplina disciplina;
        try {
            disciplina = disciplinaService.buscarPorCodigo(codigoDisciplina);
        } catch (Exception e) {
            ConsoleUI.exibirMensagem(e.getMessage(), true);
            return;
        }

        ConsoleUI.limparTela();
        ConsoleUI.exibirCabecalho("TURMAS DE " + disciplina.getNome().toUpperCase());

        System.out.println("  Disciplina : " + disciplina.getCodigo() + " - " + disciplina.getNome());
        System.out.println(
                "  Carga Horária: " + disciplina.getCargaHoraria() + "h" + " | Créditos: " + disciplina.getCreditos());

        String preReqs = (disciplina.getPreRequisitos() == null || disciplina.getPreRequisitos().isEmpty()) ? "Nenhum"
                : String.join(", ", disciplina.getPreRequisitos());
        System.out.println("  Pré-requisitos: " + preReqs);
        System.out.println("  Período: " + periodoAtivo.getCodigo() + "\n");

        List<Turma> turmas = turmaService.listarTurmasPorDisciplinaEPeriodo(codigoDisciplina, periodoAtivo.getCodigo());

        if (turmas.isEmpty()) {
            ConsoleUI.exibirMensagem(
                    "Nenhuma turma ofertada para esta disciplina no período " + periodoAtivo.getCodigo() + ".", false);
            return;
        }

        exibirTabelaTurmas(turmas);
        System.out.println("Total: " + turmas.size() + " turma(s).");
        System.out.println("\nPressione ENTER para continuar...");
        lerEntradaSilenciosa();
    }

    // -------------------------------------------------------------------------
    // RF16: Solicitar matrícula em turma
    // -------------------------------------------------------------------------

    /**
     * Fluxo de solicitação de matrícula em turma para o aluno. Lista as turmas do período ativo e permite ao aluno
     * escolher uma.
     */
    private void solicitarMatricula(Usuario aluno) {
        if (matriculaService == null || turmaService == null || periodoLetivoService == null) {
            ConsoleUI.exibirMensagem("Funcionalidade disponível na próxima release.", false);
            return;
        }

        ConsoleUI.limparTela();
        ConsoleUI.exibirCabecalho("SOLICITAR MATRÍCULA EM TURMA");

        PeriodoLetivo periodoAtivo = encontrarPeriodoAtivo();
        if (periodoAtivo == null) {
            ConsoleUI.exibirMensagem("Nenhum período letivo ativo no momento.", false);
            return;
        }

        System.out.println("  Período ativo: " + periodoAtivo.getCodigo() + " | " + periodoAtivo.getDataInicio()
                + " até " + periodoAtivo.getDataFim() + "\n");

        String codigoDisciplina = ConsoleUI.lerEntrada("Código da disciplina: ").trim();
        if (codigoDisciplina.isEmpty()) {
            ConsoleUI.exibirMensagem("Código da disciplina não pode ser vazio.", true);
            return;
        }

        List<Turma> turmas = turmaService.listarTurmasPorDisciplinaEPeriodo(codigoDisciplina, periodoAtivo.getCodigo());

        if (turmas.isEmpty()) {
            ConsoleUI.exibirMensagem("Nenhuma turma ofertada para a disciplina '" + codigoDisciplina + "' no período "
                    + periodoAtivo.getCodigo() + ".", false);
            return;
        }

        ConsoleUI.limparTela();
        ConsoleUI.exibirCabecalho("TURMAS DISPONÍVEIS — " + codigoDisciplina.toUpperCase());
        exibirTabelaTurmas(turmas);
        System.out.println();

        String codigoTurma = ConsoleUI.lerEntrada("Código da turma desejada: ").trim();
        if (codigoTurma.isEmpty()) {
            ConsoleUI.exibirMensagem("Código da turma não pode ser vazio.", true);
            return;
        }

        try {
            StatusMatricula status = matriculaService.solicitarMatricula(aluno, codigoDisciplina,
                    periodoAtivo.getCodigo(), codigoTurma);

            if (status == StatusMatricula.LISTA_ESPERA) {
                ConsoleUI.exibirMensagem("Turma cheia. Você foi adicionado à lista de espera. " + "Turma: "
                        + codigoTurma + " | Disciplina: " + codigoDisciplina + " | Período: " + periodoAtivo.getCodigo()
                        + ".", false);
            } else if (status == StatusMatricula.CONFIRMADA) {
                ConsoleUI.exibirMensagem("Matrícula confirmada com sucesso! " + "Turma: " + codigoTurma
                        + " | Disciplina: " + codigoDisciplina + " | Período: " + periodoAtivo.getCodigo() + ".",
                        false);
            } else {
                ConsoleUI.exibirMensagem("Solicitação de matrícula registrada com status: " + status + ".", false);
            }

        } catch (Exception e) {
            ConsoleUI.exibirMensagem(e.getMessage(), true);
        }
    }

    // -------------------------------------------------------------------------
    // RF16: Cancelar solicitação de matrícula
    // -------------------------------------------------------------------------

    /** Fluxo de cancelamento de uma solicitação PENDENTE do aluno. */
    private void cancelarSolicitacaoMatricula(Usuario aluno) {
        if (matriculaService == null) {
            ConsoleUI.exibirMensagem("Funcionalidade disponível na próxima release.", false);
            return;
        }

        ConsoleUI.limparTela();
        ConsoleUI.exibirCabecalho("CANCELAR SOLICITAÇÃO DE MATRÍCULA");

        List<MatriculaTurma> solicitacoes;
        try {
            solicitacoes = matriculaService.listarMinhasSolicitacoes(aluno);
        } catch (Exception e) {
            ConsoleUI.exibirMensagem(e.getMessage(), true);
            return;
        }

        if (solicitacoes.isEmpty()) {
            ConsoleUI.exibirMensagem("Você não possui solicitações de matrícula.", false);
            return;
        }

        exibirTabelaSolicitacoes(solicitacoes);
        System.out.println();

        String codigoDisciplina = ConsoleUI.lerEntrada("Código da disciplina: ").trim();
        String codigoTurma = ConsoleUI.lerEntrada("Código da turma: ").trim();

        if (codigoDisciplina.isEmpty() || codigoTurma.isEmpty()) {
            ConsoleUI.exibirMensagem("Disciplina e turma são obrigatórios.", true);
            return;
        }

        // Determina o período a partir das solicitações existentes do aluno
        String codigoPeriodo = solicitacoes.stream()
                .filter(m -> m.getCodigoDisciplina().equalsIgnoreCase(codigoDisciplina)
                        && m.getCodigoTurma().equalsIgnoreCase(codigoTurma))
                .map(MatriculaTurma::getCodigoPeriodo).findFirst().orElse(null);

        if (codigoPeriodo == null) {
            ConsoleUI.exibirMensagem("Nenhuma solicitação encontrada para a turma '" + codigoTurma + "' da disciplina '"
                    + codigoDisciplina + "'.", true);
            return;
        }

        try {
            matriculaService.cancelarMatricula(aluno, codigoDisciplina, codigoPeriodo, codigoTurma);
            ConsoleUI.exibirMensagem("Matrícula cancelada com sucesso.", false);
        } catch (Exception e) {
            ConsoleUI.exibirMensagem(e.getMessage(), true);
        }
    }

    // -------------------------------------------------------------------------
    // RF16: Listar minhas solicitações
    // -------------------------------------------------------------------------

    /** Exibe todas as solicitações de matrícula do aluno logado. */
    private void listarMinhasSolicitacoes(Usuario aluno) {
        if (matriculaService == null) {
            ConsoleUI.exibirMensagem("Funcionalidade disponível na próxima release.", false);
            return;
        }

        ConsoleUI.limparTela();
        ConsoleUI.exibirCabecalho("MINHAS SOLICITAÇÕES DE MATRÍCULA");

        List<MatriculaTurma> solicitacoes;
        try {
            solicitacoes = matriculaService.listarMinhasSolicitacoes(aluno);
        } catch (Exception e) {
            ConsoleUI.exibirMensagem(e.getMessage(), true);
            return;
        }

        if (solicitacoes.isEmpty()) {
            ConsoleUI.exibirMensagem("Você não possui solicitações de matrícula.", false);
            return;
        }

        exibirTabelaSolicitacoes(solicitacoes);
        System.out.println("Total: " + solicitacoes.size() + " solicitação(ões).");
        System.out.println("\nPressione ENTER para continuar...");
        lerEntradaSilenciosa();
    }

    // -------------------------------------------------------------------------
    // RF29: O aluno deve poder consultar sua frequência por disciplina
    // -------------------------------------------------------------------------

    /**
     * Permite ao aluno consultar seu percentual de presença e histórico de chamadas de uma disciplina do período atual
     * na qual ele esteja matriculado.
     */
    private void consultarFrequenciaPorDisciplina(Usuario aluno) {
        if (freqService == null || matriculaService == null) {
            ConsoleUI.exibirMensagem("Funcionalidade disponível na próxima release.", false);
            return;
        }

        ConsoleUI.limparTela();
        ConsoleUI.exibirCabecalho("CONSULTAR FREQUÊNCIA POR DISCIPLINA");

        List<MatriculaTurma> matriculas;
        try {
            // Filtra apenas as matrículas com status CONFIRMADA do aluno
            matriculas = matriculaService.listarMinhasSolicitacoes(aluno).stream()
                    .filter(m -> m.getStatus() == StatusMatricula.CONFIRMADA).collect(Collectors.toList());
        } catch (Exception e) {
            ConsoleUI.exibirMensagem(e.getMessage(), true);
            return;
        }

        if (matriculas.isEmpty()) {
            ConsoleUI.exibirMensagem("Você não possui nenhuma matrícula CONFIRMADA para consultar frequência.", false);
            return;
        }

        // Exibe as disciplinas em que ele está de fato matriculado para ele escolher
        System.out.println("Suas disciplinas ativas:\n");
        exibirTabelaSolicitacoes(matriculas);
        System.out.println();

        String codigoDisciplina = ConsoleUI.lerEntrada("Código da disciplina desejada: ").trim();

        // Localiza a matrícula correspondente para extrair o período e a turma automaticamente
        MatriculaTurma matriculaSelecionada = matriculas.stream()
                .filter(m -> m.getCodigoDisciplina().equalsIgnoreCase(codigoDisciplina)).findFirst().orElse(null);

        if (matriculaSelecionada == null) {
            ConsoleUI.exibirMensagem("Você não está matriculado na disciplina '" + codigoDisciplina + "'.", true);
            return;
        }

        try {
            // Busca o histórico de frequência usando os parâmetros que o Service exige
            List<RegistroFrequencia> registros = freqService.obterFrequenciaAluno(aluno.getMatricula(),
                    matriculaSelecionada.getCodigoDisciplina(), matriculaSelecionada.getCodigoPeriodo(),
                    matriculaSelecionada.getCodigoTurma());

            // Calcula o percentual usando a lógica do seu próprio Service
            double percentual = freqService.calcularPercentualFrequencia(aluno.getMatricula(),
                    matriculaSelecionada.getCodigoDisciplina(), matriculaSelecionada.getCodigoPeriodo(),
                    matriculaSelecionada.getCodigoTurma());

            ConsoleUI.limparTela();
            ConsoleUI.exibirCabecalho("RELATÓRIO DE FREQUÊNCIA — " + codigoDisciplina.toUpperCase());
            System.out.println("  Disciplina: " + matriculaSelecionada.getCodigoDisciplina());
            System.out.println("  Turma     : " + matriculaSelecionada.getCodigoTurma());
            System.out.println("  Período   : " + matriculaSelecionada.getCodigoPeriodo());
            System.out.println("  Frequência: " + String.format("%.1f", percentual) + "%");
            System.out.println("  Situação  : " + (percentual >= 75.0 ? "REGULAR" : "REPROVADO POR FALTA"));
            System.out.println();

            // Monta a tabela do histórico detalhado dia a dia
            String[] colunas = { "Data da Aula", "Status" };
            List<String[]> linhas = new ArrayList<>();
            for (RegistroFrequencia rf : registros) {
                linhas.add(new String[] { rf.getDataAula().toString(), rf.getStatus().toString() });
            }
            ConsoleUI.exibirTabela(colunas, linhas);

            long totalFaltas = registros.stream()
                    .filter(r -> r.getStatus() == com.classroompb.model.StatusFrequencia.FALTA).count();
            System.out.println("Total de aulas dadas: " + registros.size());
            System.out.println("Total de faltas: " + totalFaltas);

            String alerta = freqService.obterAlertaFrequencia(percentual);

            if (alerta != null) {
                ConsoleUI.exibirMensagem(alerta, percentual < 75.0);
            }
        } catch (Exception e) {
            ConsoleUI.exibirMensagem(e.getMessage(), true);
        }

        System.out.println("\nPressione ENTER para continuar...");
        lerEntradaSilenciosa();
    }

    // -------------------------------------------------------------------------
    // Métodos auxiliares
    // -------------------------------------------------------------------------

    /** Retorna o período letivo ativo, ou null se não existir nenhum. */
    private PeriodoLetivo encontrarPeriodoAtivo() {
        List<PeriodoLetivo> periodos = periodoLetivoService.listarPeriodos();
        for (PeriodoLetivo p : periodos) {
            if (p.isAtivo()) {
                return p;
            }
        }
        return null;
    }

    /** Exibe uma tabela formatada com os dados das turmas fornecidas. */
    private void exibirTabelaTurmas(List<Turma> turmas) {
        String[] colunas = { "Código", "Disciplina", "Horário", "Sala", "Vagas", "Professor" };
        List<String[]> linhas = new ArrayList<>();
        for (Turma t : turmas) {
            String prof = (t.getMatriculaProfessor() == null || t.getMatriculaProfessor().trim().isEmpty())
                    ? "A definir" : t.getMatriculaProfessor();
            linhas.add(new String[] { t.getCodigo(), t.getCodigoDisciplina(), t.getHorario(), t.getSala(),
                    String.valueOf(t.getVagas()), prof });
        }
        ConsoleUI.exibirTabela(colunas, linhas);
    }

    /** Exibe uma tabela formatada com as solicitações de matrícula. */
    private void exibirTabelaSolicitacoes(List<MatriculaTurma> solicitacoes) {
        String[] colunas = { "Disciplina", "Turma", "Período", "Status", "Data Solicitação" };
        List<String[]> linhas = new ArrayList<>();
        for (MatriculaTurma m : solicitacoes) {
            String data = m.getDataSolicitacao() != null
                    ? m.getDataSolicitacao().toString().replace("T", " ").substring(0, 16) : "-";
            linhas.add(new String[] { m.getCodigoDisciplina(), m.getCodigoTurma(), m.getCodigoPeriodo(),
                    m.getStatus().toString(), data });
        }
        ConsoleUI.exibirTabela(colunas, linhas);
    }

    private void consultarMeusDiarios(Usuario aluno) {
        if (consultaAcademicaService == null) {
            ConsoleUI.exibirMensagem("Consulta de diarios indisponivel.", true);
            return;
        }
        try {
            List<com.classroompb.model.Diario> diarios = consultaAcademicaService.listarDiariosDoAluno(aluno);
            if (diarios.isEmpty()) {
                ConsoleUI.exibirMensagem("Voce nao possui diarios vinculados.", false);
                return;
            }
            for (com.classroompb.model.Diario diario : diarios) {
                System.out.println("\n" + diario.getCodigo() + " - " + diario.getDescricao());
                System.out.println("Frequencias: "
                        + consultaAcademicaService.listarFrequencia(aluno, diario.getCodigo()).size());
                for (com.classroompb.model.Avaliacao avaliacao : consultaAcademicaService.listarAvaliacoes(aluno,
                        diario.getCodigo())) {
                    com.classroompb.model.Nota nota = consultaAcademicaService.listarNotas(aluno, diario.getCodigo())
                            .stream().filter(n -> avaliacao.getCodigo().equalsIgnoreCase(n.getCodigoAvaliacao()))
                            .findFirst().orElse(null);
                    System.out.println(avaliacao.getDescricao() + ": " + (nota == null ? "-" : nota.getValor()));
                }
                try {
                    System.out.printf("Media parcial: %.2f%n",
                            consultaAcademicaService.calcularMediaParcial(aluno, diario.getCodigo()));
                } catch (Exception e) {
                    System.out.println("Media parcial: ainda nao disponivel");
                }
            }
            ConsoleUI.aguardarEnter();
        } catch (Exception e) {
            ConsoleUI.exibirMensagem(e.getMessage(), true);
        }
    }

    /**
     * Lê uma linha da entrada sem exibir prompt, usado apenas para pausar a tela.
     */
    private void lerEntradaSilenciosa() {
        ConsoleUI.aguardarEnter();
    }
}
