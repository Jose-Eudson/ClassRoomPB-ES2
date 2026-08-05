package com.classroompb.ui;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.classroompb.model.Aula;
import com.classroompb.model.Avaliacao;
import com.classroompb.model.Diario;
import com.classroompb.model.MatriculaTurma;
import com.classroompb.model.RegistroFrequencia;
import com.classroompb.model.StatusFrequencia;
import com.classroompb.model.TipoUsuario;
import com.classroompb.model.Usuario;
import com.classroompb.service.AulaService;
import com.classroompb.service.AvaliacaoService;
import com.classroompb.service.DiarioService;
import com.classroompb.service.FrequenciaService;
import com.classroompb.service.NotaService;
import com.classroompb.service.PerfilAcessoService;
import com.classroompb.service.UsuarioService;

public class ProfessorController {
    @SuppressWarnings("unused")
    private final UsuarioService usuarioService;
    private final FrequenciaService frequenciaService;
    private final NotaService notaService;
    private final DiarioService diarioService;
    private final AulaService aulaService;
    private final AvaliacaoService avaliacaoService;

    public ProfessorController(UsuarioService usuarioService, FrequenciaService frequenciaService,
            NotaService notaService, DiarioService diarioService, AulaService aulaService) {
        this(usuarioService, frequenciaService, notaService, diarioService, aulaService, null);
    }

    public ProfessorController(UsuarioService usuarioService, FrequenciaService frequenciaService,
            NotaService notaService, DiarioService diarioService, AulaService aulaService,
            AvaliacaoService avaliacaoService) {
        this.usuarioService = usuarioService;
        this.frequenciaService = frequenciaService;
        this.notaService = notaService;
        this.diarioService = diarioService;
        this.aulaService = new AulaService(aulaService);
        this.avaliacaoService = avaliacaoService == null ? null : new AvaliacaoService(avaliacaoService);
    }

    public void exibirMenu(Usuario professor) {
        try {
            PerfilAcessoService.validarPerfil(professor, TipoUsuario.PROFESSOR);
        } catch (Exception e) {
            ConsoleUI.exibirMensagem(e.getMessage(), true);
            return;
        }
        while (true) {
            List<String> opcoes = Arrays.asList("Visualizar meus diarios", "Registrar frequencia", "Lancar notas",
                    "Consultar media parcial", "Alterar notas", "Cadastrar aula", "Editar aula",
                    "Gerenciar avaliacoes", "Fechar diario", "Logout");
            int escolha = ConsoleUI.exibirMenuInterativo("MENU PROFESSOR", opcoes);
            if (escolha < 0 || escolha == opcoes.size() - 1) {
                return;
            }
            executarOpcao(professor, escolha);
        }
    }

    private void executarOpcao(Usuario professor, int escolha) {
        switch (escolha) {
        case 0:
            visualizarDiarios(professor);
            break;
        case 1:
            registrarFrequencia(professor);
            break;
        case 2:
            lancarOuAlterarNotas(professor, false);
            break;
        case 3:
            consultarMedia(professor);
            break;
        case 4:
            lancarOuAlterarNotas(professor, true);
            break;
        case 5:
            cadastrarAula(professor);
            break;
        case 6:
            editarAula(professor);
            break;
        case 7:
            gerenciarAvaliacoes(professor);
            break;
        case 8:
            fecharDiario(professor);
            break;
        default:
            break;
        }
    }

    private void visualizarDiarios(Usuario professor) {
        try {
            List<Diario> diarios = diarioService.listarPorProfessor(professor);
            if (diarios.isEmpty()) {
                ConsoleUI.exibirMensagem("Nenhum diario vinculado a este professor.", false);
                return;
            }
            ConsoleUI.exibirTabela(new String[] { "Codigo", "Descricao", "Turma", "Horario", "Sala", "Situacao" },
                    diarios.stream().map(d -> new String[] { d.getCodigo(), d.getDescricao(), d.getCodigoTurma(),
                            d.getHorario(), d.getSala(), d.getSituacao().name() }).collect(Collectors.toList()));
            ConsoleUI.aguardarEnter();
        } catch (Exception e) {
            ConsoleUI.exibirMensagem(e.getMessage(), true);
        }
    }

    private void registrarFrequencia(Usuario professor) {
        try {
            Diario diario = selecionarDiario(professor);
            if (diario == null) {
                return;
            }
            Aula aula = selecionarAula(diario);
            if (aula == null) {
                return;
            }
            List<MatriculaTurma> matriculas = frequenciaService.listarMatriculasConfirmadasDoDiario(professor,
                    diario.getCodigo());
            for (MatriculaTurma matricula : matriculas) {
                String valor = ConsoleUI.lerEntrada("Aluno " + matricula.getMatriculaAluno() + " [P/F]: ").trim();
                StatusFrequencia status = "P".equalsIgnoreCase(valor) ? StatusFrequencia.PRESENTE
                        : "F".equalsIgnoreCase(valor) ? StatusFrequencia.FALTA : null;
                if (status != null) {
                    frequenciaService.registrarFrequencia(professor, matricula.getMatriculaAluno(),
                            diario.getCodigoDisciplina(), diario.getCodigoPeriodo(), diario.getCodigoTurma(),
                            aula.getCodigo(), aula.getData(), status);
                }
            }
            List<RegistroFrequencia> registros = frequenciaService.listarFrequenciaDaAula(professor,
                    diario.getCodigo(), aula.getCodigo());
            ConsoleUI.exibirMensagem(registros.size() + " frequencias registradas.", false);
        } catch (Exception e) {
            ConsoleUI.exibirMensagem(e.getMessage(), true);
        }
    }

    private void lancarOuAlterarNotas(Usuario professor, boolean alterar) {
        try {
            exigirAvaliacaoService();
            Diario diario = selecionarDiario(professor);
            if (diario == null) {
                return;
            }
            Avaliacao avaliacao = selecionarAvaliacao(diario);
            if (avaliacao == null) {
                return;
            }
            for (MatriculaTurma matricula : frequenciaService.listarMatriculasConfirmadasDoDiario(professor,
                    diario.getCodigo())) {
                double valor = Double.parseDouble(ConsoleUI.lerEntrada("Nota de " + matricula.getMatriculaAluno()
                        + " (maximo " + avaliacao.getNotaMaxima() + "): "));
                if (alterar) {
                    notaService.alterarNotaAvaliacao(professor, matricula.getMatriculaAluno(), avaliacao.getCodigo(),
                            valor);
                } else {
                    notaService.lancarNotaAvaliacao(professor, matricula.getMatriculaAluno(), avaliacao.getCodigo(),
                            valor);
                }
            }
            ConsoleUI.exibirMensagem("Notas processadas com sucesso.", false);
        } catch (Exception e) {
            ConsoleUI.exibirMensagem(e.getMessage(), true);
        }
    }

    private void consultarMedia(Usuario professor) {
        try {
            Diario diario = selecionarDiario(professor);
            if (diario == null) {
                return;
            }
            for (MatriculaTurma matricula : frequenciaService.listarMatriculasConfirmadasDoDiario(professor,
                    diario.getCodigo())) {
                double media = notaService.calcularMediaParcial(matricula.getMatriculaAluno(), diario.getCodigo());
                System.out.printf("%s: %.2f%n", matricula.getMatriculaAluno(), media);
            }
            ConsoleUI.aguardarEnter();
        } catch (Exception e) {
            ConsoleUI.exibirMensagem(e.getMessage(), true);
        }
    }

    private void cadastrarAula(Usuario professor) {
        try {
            Diario diario = selecionarDiario(professor);
            if (diario == null) {
                return;
            }
            String codigo = ConsoleUI.lerEntrada("Codigo da aula: ").trim();
            LocalDate data = LocalDate.parse(ConsoleUI.lerEntrada("Data (AAAA-MM-DD): ").trim());
            String conteudo = ConsoleUI.lerEntrada("Conteudo: ").trim();
            double duracao = Double.parseDouble(ConsoleUI.lerEntrada("Duracao em horas: ").trim());
            aulaService.cadastrarAula(professor, codigo, diario.getCodigo(), data, conteudo, duracao);
            ConsoleUI.exibirMensagem("Aula cadastrada com sucesso.", false);
        } catch (Exception e) {
            ConsoleUI.exibirMensagem(e.getMessage(), true);
        }
    }

    private void editarAula(Usuario professor) {
        try {
            Diario diario = selecionarDiario(professor);
            if (diario == null) {
                return;
            }
            Aula aula = selecionarAula(diario);
            if (aula == null) {
                return;
            }
            aula.setConteudo(ConsoleUI.lerEntrada("Novo conteudo: ").trim());
            aula.setDuracaoHoras(Double.parseDouble(ConsoleUI.lerEntrada("Nova duracao em horas: ").trim()));
            aulaService.atualizarAula(professor, aula);
            ConsoleUI.exibirMensagem("Aula atualizada com sucesso.", false);
        } catch (Exception e) {
            ConsoleUI.exibirMensagem(e.getMessage(), true);
        }
    }

    private void gerenciarAvaliacoes(Usuario professor) {
        try {
            exigirAvaliacaoService();
            Diario diario = selecionarDiario(professor);
            if (diario == null) {
                return;
            }
            List<String> opcoes = Arrays.asList("Listar", "Cadastrar", "Editar", "Excluir", "Voltar");
            int escolha = ConsoleUI.exibirMenuInterativo("AVALIACOES", opcoes);
            if (escolha == 0) {
                listarAvaliacoes(diario);
            } else if (escolha == 1) {
                cadastrarAvaliacao(professor, diario);
            } else if (escolha == 2) {
                editarAvaliacao(professor, diario);
            } else if (escolha == 3) {
                Avaliacao avaliacao = selecionarAvaliacao(diario);
                if (avaliacao != null) {
                    avaliacaoService.remover(professor, avaliacao.getCodigo());
                }
            }
        } catch (Exception e) {
            ConsoleUI.exibirMensagem(e.getMessage(), true);
        }
    }

    private void listarAvaliacoes(Diario diario) {
        for (Avaliacao avaliacao : avaliacaoService.listarPorDiario(diario.getCodigo())) {
            System.out.println(avaliacao.getCodigo() + " | " + avaliacao.getDescricao() + " | "
                    + avaliacao.getEtapa() + " | peso " + avaliacao.getPeso() + " | max "
                    + avaliacao.getNotaMaxima());
        }
        ConsoleUI.aguardarEnter();
    }

    private void cadastrarAvaliacao(Usuario professor, Diario diario) throws Exception {
        String codigo = ConsoleUI.lerEntrada("Codigo: ").trim();
        String descricao = ConsoleUI.lerEntrada("Descricao: ").trim();
        String etapa = ConsoleUI.lerEntrada("Etapa: ").trim();
        double peso = Double.parseDouble(ConsoleUI.lerEntrada("Peso: "));
        double maxima = Double.parseDouble(ConsoleUI.lerEntrada("Nota maxima: "));
        avaliacaoService.cadastrar(professor, codigo, diario.getCodigo(), descricao, etapa, peso, maxima);
    }

    private void editarAvaliacao(Usuario professor, Diario diario) throws Exception {
        Avaliacao avaliacao = selecionarAvaliacao(diario);
        if (avaliacao == null) {
            return;
        }
        avaliacao.setDescricao(ConsoleUI.lerEntrada("Nova descricao: ").trim());
        avaliacao.setEtapa(ConsoleUI.lerEntrada("Nova etapa: ").trim());
        avaliacao.setPeso(Double.parseDouble(ConsoleUI.lerEntrada("Novo peso: ")));
        avaliacao.setNotaMaxima(Double.parseDouble(ConsoleUI.lerEntrada("Nova nota maxima: ")));
        avaliacaoService.editar(professor, avaliacao);
    }

    private void fecharDiario(Usuario professor) {
        try {
            Diario diario = selecionarDiario(professor);
            if (diario != null) {
                diarioService.fecharDiario(professor, diario.getCodigo());
                ConsoleUI.exibirMensagem("Diario fechado com sucesso.", false);
            }
        } catch (Exception e) {
            ConsoleUI.exibirMensagem(e.getMessage(), true);
        }
    }

    private Diario selecionarDiario(Usuario professor) throws Exception {
        List<Diario> diarios = diarioService.listarPorProfessor(professor);
        if (diarios.isEmpty()) {
            ConsoleUI.exibirMensagem("Voce nao possui diarios cadastrados.", false);
            return null;
        }
        List<String> opcoes = diarios.stream().map(d -> d.getCodigo() + " | " + d.getDescricao() + " | "
                + d.getSituacao()).collect(Collectors.toList());
        opcoes.add("Cancelar");
        int escolha = ConsoleUI.exibirMenuInterativo("SELECIONAR DIARIO", opcoes);
        return escolha < 0 || escolha == opcoes.size() - 1 ? null : diarios.get(escolha);
    }

    private Aula selecionarAula(Diario diario) {
        List<Aula> aulas = aulaService.listarPorDiario(diario.getCodigo());
        if (aulas.isEmpty()) {
            ConsoleUI.exibirMensagem("Nao ha aulas neste diario.", false);
            return null;
        }
        List<String> opcoes = aulas.stream().map(a -> a.getCodigo() + " | " + a.getData() + " | " + a.getConteudo())
                .collect(Collectors.toList());
        opcoes.add("Cancelar");
        int escolha = ConsoleUI.exibirMenuInterativo("SELECIONAR AULA", opcoes);
        return escolha < 0 || escolha == opcoes.size() - 1 ? null : aulas.get(escolha);
    }

    private Avaliacao selecionarAvaliacao(Diario diario) {
        List<Avaliacao> avaliacoes = avaliacaoService.listarPorDiario(diario.getCodigo());
        if (avaliacoes.isEmpty()) {
            ConsoleUI.exibirMensagem("Nao ha avaliacoes neste diario.", false);
            return null;
        }
        List<String> opcoes = avaliacoes.stream()
                .map(a -> a.getCodigo() + " | " + a.getDescricao() + " | " + a.getEtapa())
                .collect(Collectors.toList());
        opcoes.add("Cancelar");
        int escolha = ConsoleUI.exibirMenuInterativo("SELECIONAR AVALIACAO", opcoes);
        return escolha < 0 || escolha == opcoes.size() - 1 ? null : avaliacoes.get(escolha);
    }

    private void exigirAvaliacaoService() {
        if (avaliacaoService == null) {
            throw new IllegalStateException("Funcionalidade de avaliacao indisponivel.");
        }
    }
}
