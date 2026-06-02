package com.classroompb.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.classroompb.model.Disciplina;
import com.classroompb.model.PeriodoLetivo;
import com.classroompb.model.TipoUsuario;
import com.classroompb.model.Turma;
import com.classroompb.model.Usuario;
import com.classroompb.service.DisciplinaService;
import com.classroompb.service.PerfilAcessoService;
import com.classroompb.service.PeriodoLetivoService;
import com.classroompb.service.TurmaService;
import com.classroompb.service.UsuarioService;

/**
 * Controlador da interface do Aluno.
 * Responsável pelo menu e todas as ações disponíveis para esse perfil.
 */
public class AlunoController {

    @SuppressWarnings("unused")
    private final UsuarioService service;
    private final DisciplinaService disciplinaService;
    private final PeriodoLetivoService periodoLetivoService;
    private final TurmaService turmaService;

    public AlunoController(UsuarioService service) {
        this(service, null, null, null);
    }

    public AlunoController(
            UsuarioService service,
            DisciplinaService disciplinaService,
            PeriodoLetivoService periodoLetivoService,
            TurmaService turmaService
    ) {
        this.service = service;
        this.disciplinaService = disciplinaService;
        this.periodoLetivoService = periodoLetivoService;
        this.turmaService = turmaService;
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
                "Logout"
            );
            int escolha = ConsoleUI.exibirMenuInterativo("MENU ALUNO", opcoes);

            if (escolha == -1 || escolha == opcoes.size() - 1) break;

            switch (escolha) {
                case 0: consultarDisciplinasETurmas(); break;
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
     * Fluxo principal de consulta de disciplinas e turmas.
     * Permite ao aluno visualizar disciplinas cadastradas e as turmas
     * ofertadas no período letivo ativo.
     */
    private void consultarDisciplinasETurmas() {
        if (disciplinaService == null || periodoLetivoService == null || turmaService == null) {
            ConsoleUI.exibirMensagem("Funcionalidade disponível na próxima release.", false);
            return;
        }

        while (true) {
            List<String> opcoes = Arrays.asList(
                "Listar todas as disciplinas",
                "Listar turmas do período ativo",
                "Buscar turmas por disciplina",
                "Voltar"
            );
            int escolha = ConsoleUI.exibirMenuInterativo("CONSULTAR DISCIPLINAS E TURMAS", opcoes);

            if (escolha == -1 || escolha == opcoes.size() - 1) return;

            switch (escolha) {
                case 0: listarTodasDisciplinas();          break;
                case 1: listarTurmasDoPeriodoAtivo();      break;
                case 2: buscarTurmasPorDisciplina();       break;
            }
        }
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
            String preReqs = (d.getPreRequisitos() == null || d.getPreRequisitos().isEmpty())
                    ? "Nenhum"
                    : String.join(", ", d.getPreRequisitos());
            linhas.add(new String[]{
                d.getCodigo(),
                d.getNome(),
                String.valueOf(d.getCargaHoraria()),
                String.valueOf(d.getCreditos()),
                preReqs
            });
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

        System.out.println("  Período: " + periodoAtivo.getCodigo()
                + " | " + periodoAtivo.getDataInicio()
                + " até " + periodoAtivo.getDataFim() + "\n");

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
     * Permite ao aluno buscar turmas de uma disciplina específica
     * dentro do período letivo ativo.
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
        System.out.println("  Carga Horária: " + disciplina.getCargaHoraria() + "h"
                + " | Créditos: " + disciplina.getCreditos());

        String preReqs = (disciplina.getPreRequisitos() == null || disciplina.getPreRequisitos().isEmpty())
                ? "Nenhum"
                : String.join(", ", disciplina.getPreRequisitos());
        System.out.println("  Pré-requisitos: " + preReqs);
        System.out.println("  Período: " + periodoAtivo.getCodigo() + "\n");

        List<Turma> turmas = turmaService.listarTurmasPorDisciplinaEPeriodo(
                codigoDisciplina, periodoAtivo.getCodigo());

        if (turmas.isEmpty()) {
            ConsoleUI.exibirMensagem(
                    "Nenhuma turma ofertada para esta disciplina no período " + periodoAtivo.getCodigo() + ".",
                    false);
            return;
        }

        exibirTabelaTurmas(turmas);
        System.out.println("Total: " + turmas.size() + " turma(s).");
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
            if (p.isAtivo()) return p;
        }
        return null;
    }

    /** Exibe uma tabela formatada com os dados das turmas fornecidas. */
    private void exibirTabelaTurmas(List<Turma> turmas) {
        String[] colunas = { "Código", "Disciplina", "Horário", "Sala", "Vagas", "Professor" };
        List<String[]> linhas = new ArrayList<>();
        for (Turma t : turmas) {
            String prof = (t.getMatriculaProfessor() == null || t.getMatriculaProfessor().trim().isEmpty())
                    ? "A definir"
                    : t.getMatriculaProfessor();
            linhas.add(new String[]{
                t.getCodigo(),
                t.getCodigoDisciplina(),
                t.getHorario(),
                t.getSala(),
                String.valueOf(t.getVagas()),
                prof
            });
        }
        ConsoleUI.exibirTabela(colunas, linhas);
    }

    /** Lê uma linha da entrada sem exibir prompt, usado apenas para pausar a tela. */
    private void lerEntradaSilenciosa() {
        try {
            //noinspection ResultOfMethodCallIgnored
            System.in.read();
            // Descarta restante do buffer
            while (System.in.available() > 0) System.in.read();
        } catch (java.io.IOException ignored) {}
    }
}