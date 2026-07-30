package com.classroompb.ui;

import java.time.LocalDate;
//import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.classroompb.model.Aula;
import com.classroompb.model.Diario;
import com.classroompb.model.MatriculaTurma;
import com.classroompb.model.RegistroFrequencia;
import com.classroompb.model.StatusFrequencia;
import com.classroompb.model.TipoUsuario;
import com.classroompb.model.Turma;
import com.classroompb.model.Usuario;
import com.classroompb.service.FrequenciaService;
import com.classroompb.service.NotaService;
import com.classroompb.service.PerfilAcessoService;
import com.classroompb.service.UsuarioService;
import com.classroompb.service.AulaService;
import com.classroompb.service.DiarioService;

/**
 * Controlador da interface do Professor. Responsavel pelo menu e pelas acoes disponiveis para esse perfil.
 */
public class ProfessorController {

    @SuppressWarnings("unused")
    private final UsuarioService service;
    private final FrequenciaService frequenciaService;
    private final NotaService notaService;
    private final DiarioService diarioService;
    private final AulaService aulaService;

    public ProfessorController(UsuarioService service, FrequenciaService frequenciaService, NotaService notaService,
            DiarioService diarioService, AulaService aulaService) {
        this.service = service;
        this.frequenciaService = frequenciaService;
        this.notaService = notaService;
        this.diarioService = diarioService;
        this.aulaService = aulaService;
    }

    /** Exibe o menu principal do professor e permanece em loop ate logout. */
    public void exibirMenu(Usuario usuario) {
        try {
            PerfilAcessoService.validarPerfil(usuario, TipoUsuario.PROFESSOR);
        } catch (Exception e) {
            ConsoleUI.exibirMensagem(e.getMessage(), true);
            return;
        }

        while (true) {
            List<String> opcoes = Arrays.asList("Visualizar turmas", "Registrar frequencia", "Lancar notas",
                    "Acompanhar alunos", "Alterar notas (antes do fechamento)", "Cadastrar aula", "Logout");
            int escolha = ConsoleUI.exibirMenuInterativo("MENU PROFESSOR", opcoes);

            if (escolha == -1 || escolha == opcoes.size() - 1) {
                break;
            }

            switch (escolha) {
            case 0:
                visualizarTurmas(usuario);
                break;
            case 1:
                registrarFrequencia(usuario);
                break;
            case 2:
                lancarNotas(usuario);
                break;
            case 4:
                alterarNotas(usuario);
                break;
            case 5:
                cadastrarAula(usuario);
                break;
            default:
                ConsoleUI.exibirMensagem("Funcionalidade disponivel na proxima release.", false);
                break;
            }
        }
    }

    private void visualizarTurmas(Usuario professor) {
        try {
            List<Turma> turmas = frequenciaService.listarTurmasDoProfessor(professor);
            ConsoleUI.limparTela();
            ConsoleUI.exibirCabecalho("MINHAS TURMAS");

            if (turmas.isEmpty()) {
                ConsoleUI.exibirMensagem("Nenhuma turma vinculada a este professor.", false);
                return;
            }

            ConsoleUI.exibirTabela(new String[] { "Disciplina", "Periodo", "Turma", "Horario", "Sala" },
                    turmas.stream()
                            .map(t -> new String[] { t.getCodigoDisciplina(), t.getCodigoPeriodo(), t.getCodigo(),
                                    valorOuTraco(t.getHorario()), valorOuTraco(t.getSala()) })
                            .collect(Collectors.toList()));
            System.out.println("Pressione ENTER para continuar...");
            ConsoleUI.aguardarEnter();
        } catch (Exception e) {
            ConsoleUI.exibirMensagem(e.getMessage(), true);
        }
    }

    private void registrarFrequencia(Usuario professor) {
        try {

            Turma turma = selecionarTurmaDoProfessor(professor);

            if (turma == null) {
                return;
            }

            // RN15 - A turma deve possuir pelo menos um diário
            if (!diarioService.turmaPossuiDiario(turma.getCodigo())) {
                throw new Exception("Erro: a turma deve possuir pelo menos um diário cadastrado.");
            }

            List<Diario> diarios = diarioService.buscarPorTurma(turma.getCodigo());

            if (diarios.isEmpty()) {
                ConsoleUI.exibirMensagem("Nao ha diario cadastrado para esta turma.", false);
                return;
            }

            Diario diario = diarios.get(0);

            List<Aula> aulas = aulaService.listarPorDiario(diario.getCodigo());

            if (aulas.isEmpty()) {
                ConsoleUI.exibirMensagem("Nao ha aulas cadastradas neste diario.", false);
                return;
            }

            ConsoleUI.limparTela();
            ConsoleUI.exibirCabecalho("REGISTRAR FREQUENCIA");

            System.out.println("Turma: " + turma.getCodigoDisciplina() + " | " + turma.getCodigoPeriodo() + " | "
                    + turma.getCodigo());

            System.out.println();
            System.out.println("Aulas cadastradas:");

            for (Aula aula : aulas) {
                System.out.println(aula.getNumero() + " - " + aula.getCodigo() + " | " + aula.getData() + " | "
                        + aula.getConteudo());
            }

            System.out.println();

            String codigoAula = ConsoleUI.lerEntrada("Codigo da aula: ").trim();

            Aula aula = aulaService.buscarPorCodigo(codigoAula);

            if (aula == null) {
                ConsoleUI.exibirMensagem("Aula inexistente.", true);
                return;
            }

            LocalDate dataAula = aula.getData();

            List<MatriculaTurma> matriculas = frequenciaService.listarMatriculasConfirmadasDaTurma(professor,
                    turma.getCodigoDisciplina(), turma.getCodigoPeriodo(), turma.getCodigo());

            if (matriculas.isEmpty()) {
                ConsoleUI.exibirMensagem("Nao ha alunos com matricula confirmada nesta turma.", false);
                return;
            }

            System.out.println();
            System.out.println("Digite P para presente, F para falta ou ENTER para pular o aluno.");
            System.out.println();

            int lancados = 0;

            for (MatriculaTurma matricula : matriculas) {

                String entrada = ConsoleUI.lerEntrada("Aluno " + matricula.getMatriculaAluno() + " [P/F/ENTER]: ")
                        .trim();

                if (entrada.isEmpty()) {
                    continue;
                }

                StatusFrequencia status = parseStatus(entrada);

                if (status == null) {
                    System.out.println("Entrada ignorada. Use P, PRESENTE, F ou FALTA.");
                    continue;
                }

                frequenciaService.registrarFrequencia(professor, matricula.getMatriculaAluno(),
                        turma.getCodigoDisciplina(), turma.getCodigoPeriodo(), turma.getCodigo(), codigoAula, dataAula,
                        status);

                lancados++;
            }

            exibirFrequenciaLancada(professor, turma, dataAula, lancados);

        } catch (Exception e) {
            ConsoleUI.exibirMensagem(e.getMessage(), true);
        }
    }

    private Turma selecionarTurmaDoProfessor(Usuario professor) throws Exception {
        List<Turma> turmas = frequenciaService.listarTurmasDoProfessor(professor);
        if (turmas.isEmpty()) {
            ConsoleUI.exibirMensagem("Nenhuma turma vinculada a este professor.", false);
            return null;
        }

        List<String> opcoes = turmas.stream().map(t -> t.getCodigoDisciplina() + " | " + t.getCodigoPeriodo() + " | "
                + t.getCodigo() + " | " + valorOuTraco(t.getHorario())).collect(Collectors.toList());
        opcoes.add("Cancelar");

        int escolha = ConsoleUI.exibirMenuInterativo("SELECIONAR TURMA", opcoes);
        if (escolha == -1 || escolha == opcoes.size() - 1) {
            return null;
        }
        return turmas.get(escolha);
    }

    /*
     * private LocalDate lerDataAula() { while (true) { ConsoleUI.limparTela();
     * ConsoleUI.exibirCabecalho("DATA DA AULA"); String entrada =
     * ConsoleUI.lerEntrada("Data da aula (AAAA-MM-DD) ou ENTER para cancelar: ").trim(); if (entrada.isEmpty()) {
     * return null; } try { return LocalDate.parse(entrada); } catch (DateTimeParseException e) {
     * ConsoleUI.exibirMensagem("Data invalida. Use o formato AAAA-MM-DD.", true); } } }
     */

    private void exibirFrequenciaLancada(Usuario professor, Turma turma, LocalDate dataAula, int lancados)
            throws Exception {
        List<RegistroFrequencia> registros = frequenciaService.listarFrequenciaDaAula(professor,
                turma.getCodigoDisciplina(), turma.getCodigoPeriodo(), turma.getCodigo(), dataAula);

        ConsoleUI.limparTela();
        ConsoleUI.exibirCabecalho("FREQUENCIA DA AULA");
        System.out.println("Lancamentos realizados nesta operacao: " + lancados);
        System.out.println();

        if (registros.isEmpty()) {
            System.out.println("Nenhum registro de frequencia para esta aula.");
        } else {
            ConsoleUI
                    .exibirTabela(
                            new String[] { "Aluno", "Status", "Registrado por",
                                    "Data registro" },
                            registros.stream()
                                    .map(r -> new String[] { r.getMatriculaAluno(), r.getStatus().name(),
                                            valorOuTraco(r.getMatriculaProfessor()),
                                            r.getDataRegistro() != null ? r.getDataRegistro().toString() : "-" })
                                    .collect(Collectors.toList()));
        }

        System.out.println("Pressione ENTER para continuar...");
        ConsoleUI.aguardarEnter();
    }

    private StatusFrequencia parseStatus(String entrada) {
        String normalizada = entrada.trim().toUpperCase();
        if ("P".equals(normalizada) || "PRESENTE".equals(normalizada)) {
            return StatusFrequencia.PRESENTE;
        }
        if ("F".equals(normalizada) || "FALTA".equals(normalizada)) {
            return StatusFrequencia.FALTA;
        }
        return null;
    }

    private String valorOuTraco(String valor) {
        return valor == null || valor.trim().isEmpty() ? "-" : valor.trim();
    }

    private void lancarNotas(Usuario professor) {

        try {

            Turma turma = selecionarTurmaDoProfessor(professor);

            if (turma == null) {
                return;
            }

            List<MatriculaTurma> matriculas = frequenciaService.listarMatriculasConfirmadasDaTurma(professor,
                    turma.getCodigoDisciplina(), turma.getCodigoPeriodo(), turma.getCodigo());

            if (matriculas.isEmpty()) {

                ConsoleUI.exibirMensagem("Nao ha alunos matriculados.", false);

                return;
            }

            ConsoleUI.limparTela();
            ConsoleUI.exibirCabecalho("LANCAR NOTAS");

            for (MatriculaTurma matricula : matriculas) {

                System.out.println();
                System.out.println("Aluno: " + matricula.getMatriculaAluno());

                double etapa1 = Double.parseDouble(ConsoleUI.lerEntrada("Etapa 1: "));

                double etapa2 = Double.parseDouble(ConsoleUI.lerEntrada("Etapa 2: "));

                notaService.lancarNotas(

                        professor,

                        matricula.getMatriculaAluno(),

                        turma.getCodigoDisciplina(),

                        turma.getCodigoPeriodo(),

                        turma.getCodigo(),

                        etapa1,

                        etapa2);

                double media = (etapa1 + etapa2) / 2.0;
                double percentualFrequencia = frequenciaService.calcularPercentualFrequencia(
                        matricula.getMatriculaAluno(), turma.getCodigoDisciplina(), turma.getCodigoPeriodo(),
                        turma.getCodigo());
                String situacao = notaService.calcularSituacaoFinal(matricula.getMatriculaAluno(),
                        turma.getCodigoDisciplina(), turma.getCodigoPeriodo(), turma.getCodigo(), percentualFrequencia);

                System.out.printf("Média final: %.2f%n", media);
                System.out.println("Situação: " + situacao);
            }

            ConsoleUI.exibirMensagem("Notas lancadas com sucesso!", false);

        } catch (Exception e) {

            ConsoleUI.exibirMensagem(e.getMessage(), true);
        }
    }

    private void alterarNotas(Usuario professor) {

        try {

            Turma turma = selecionarTurmaDoProfessor(professor);

            if (turma == null) {
                return;
            }

            List<MatriculaTurma> matriculas = frequenciaService.listarMatriculasConfirmadasDaTurma(professor,
                    turma.getCodigoDisciplina(), turma.getCodigoPeriodo(), turma.getCodigo());

            if (matriculas.isEmpty()) {
                ConsoleUI.exibirMensagem("Nao ha alunos matriculados.", false);
                return;
            }

            ConsoleUI.limparTela();
            ConsoleUI.exibirCabecalho("ALTERAR NOTAS");

            for (MatriculaTurma matricula : matriculas) {

                System.out.println();
                System.out.println("Aluno: " + matricula.getMatriculaAluno());

                double etapa1 = Double.parseDouble(ConsoleUI.lerEntrada("Nova Etapa 1: "));
                double etapa2 = Double.parseDouble(ConsoleUI.lerEntrada("Nova Etapa 2: "));

                notaService.alterarNotas(professor, matricula.getMatriculaAluno(), turma.getCodigoDisciplina(),
                        turma.getCodigoPeriodo(), turma.getCodigo(), etapa1, etapa2);

                double media = (etapa1 + etapa2) / 2.0;
                double percentualFrequencia = frequenciaService.calcularPercentualFrequencia(
                        matricula.getMatriculaAluno(), turma.getCodigoDisciplina(), turma.getCodigoPeriodo(),
                        turma.getCodigo());
                String situacao = notaService.calcularSituacaoFinal(matricula.getMatriculaAluno(),
                        turma.getCodigoDisciplina(), turma.getCodigoPeriodo(), turma.getCodigo(), percentualFrequencia);

                System.out.printf("Média final: %.2f%n", media);
                System.out.println("Situação: " + situacao);
            }

            ConsoleUI.exibirMensagem("Notas alteradas com sucesso!", false);

        } catch (Exception e) {
            ConsoleUI.exibirMensagem(e.getMessage(), true);
        }
    }

    private void cadastrarAula(Usuario professor) {

        try {

            List<Diario> diarios = diarioService.listarPorProfessor(professor.getMatricula());

            if (diarios.isEmpty()) {
                ConsoleUI.exibirMensagem("Voce nao possui diarios cadastrados.", false);
                return;
            }

            ConsoleUI.limparTela();
            ConsoleUI.exibirCabecalho("CADASTRAR AULA");

            System.out.println("Diarios:");

            for (int i = 0; i < diarios.size(); i++) {

                Diario d = diarios.get(i);

                System.out.println(
                        (i + 1) + " - " + d.getCodigo() + " | Turma: " + d.getCodigoTurma() + " | " + d.getDescricao());
            }

            int opcao = Integer.parseInt(ConsoleUI.lerEntrada("Escolha um diario: "));

            if (opcao < 1 || opcao > diarios.size()) {

                ConsoleUI.exibirMensagem("Opcao invalida.", true);
                return;
            }

            Diario diario = diarios.get(opcao - 1);

            String codigo = ConsoleUI.lerEntrada("Codigo da aula: ").trim();

            LocalDate data = LocalDate.parse(ConsoleUI.lerEntrada("Data (AAAA-MM-DD): ").trim());

            String conteudo = ConsoleUI.lerEntrada("Conteudo ministrado: ").trim();

            aulaService.cadastrarAula(codigo, diario.getCodigo(), data, conteudo);

            ConsoleUI.exibirMensagem("Aula cadastrada com sucesso!", false);

        } catch (Exception e) {

            ConsoleUI.exibirMensagem(e.getMessage(), true);
        }
    }
}
