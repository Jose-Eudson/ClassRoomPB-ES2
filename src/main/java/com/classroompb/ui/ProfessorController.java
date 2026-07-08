package com.classroompb.ui;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

/**
 * Controlador da interface do Professor. Responsavel pelo menu e pelas acoes disponiveis para esse perfil.
 */
public class ProfessorController {

    @SuppressWarnings("unused")
    private final UsuarioService service;
    private final FrequenciaService frequenciaService;
    private final NotaService notaService;

    public ProfessorController(UsuarioService service, FrequenciaService frequenciaService, NotaService notaService) {
        this.service = service;
        this.frequenciaService = frequenciaService;
        this.notaService = notaService;
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
                    "Acompanhar alunos", "Alterar notas (antes do fechamento)", "Logout");
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

            LocalDate dataAula = lerDataAula();
            if (dataAula == null) {
                return;
            }

            List<MatriculaTurma> matriculas = frequenciaService.listarMatriculasConfirmadasDaTurma(professor,
                    turma.getCodigoDisciplina(), turma.getCodigoPeriodo(), turma.getCodigo());
            if (matriculas.isEmpty()) {
                ConsoleUI.exibirMensagem("Nao ha alunos com matricula confirmada nesta turma.", false);
                return;
            }

            ConsoleUI.limparTela();
            ConsoleUI.exibirCabecalho("REGISTRAR FREQUENCIA");
            System.out.println("Turma: " + turma.getCodigoDisciplina() + " | " + turma.getCodigoPeriodo() + " | "
                    + turma.getCodigo());
            System.out.println("Data da aula: " + dataAula);
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
                        turma.getCodigoDisciplina(), turma.getCodigoPeriodo(), turma.getCodigo(), dataAula, status);
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

    private LocalDate lerDataAula() {
        while (true) {
            ConsoleUI.limparTela();
            ConsoleUI.exibirCabecalho("DATA DA AULA");
            String entrada = ConsoleUI.lerEntrada("Data da aula (AAAA-MM-DD) ou ENTER para cancelar: ").trim();
            if (entrada.isEmpty()) {
                return null;
            }
            try {
                return LocalDate.parse(entrada);
            } catch (DateTimeParseException e) {
                ConsoleUI.exibirMensagem("Data invalida. Use o formato AAAA-MM-DD.", true);
            }
        }
    }

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
            }

            ConsoleUI.exibirMensagem("Notas lancadas com sucesso!", false);

        } catch (Exception e) {

            ConsoleUI.exibirMensagem(e.getMessage(), true);
        }
    }
}
