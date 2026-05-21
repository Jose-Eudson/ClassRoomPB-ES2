package com.classroompb.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.classroompb.model.TipoUsuario;
import com.classroompb.model.Usuario;
import com.classroompb.service.DisciplinaService;
import com.classroompb.service.PerfilAcessoService;
import com.classroompb.service.PeriodoLetivoService;
import com.classroompb.service.UsuarioService;

/**
 * Controlador da interface do Coordenador.
 * Responsável pelo menu e todas as ações disponíveis para esse perfil.
 * As funcionalidades serão implementadas nas releases seguintes (RF05–RF14).
 */
public class CoordenadorController {

    @SuppressWarnings("unused")
    private final UsuarioService service;
    private final DisciplinaService disciplinaService;
    private final PeriodoLetivoService periodoService;


    public CoordenadorController(UsuarioService service, DisciplinaService disciplinaService) {
        this.service = service;
        this.disciplinaService = disciplinaService;
        
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
                "Cadastrar disciplinas",
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
                case 0:
                    cadastrarDisciplina();
                    break;
                case 1:
                    cadastrarPeriodoLetivo(usuario);
                    break;
                case 2:
                    ativarPeriodo(usuario);
                    break;
                case 3:
                    encerrarPeriodo(usuario);
                    break;
                default:
                    // Funcionalidades implementadas nas próximas releases
                    ConsoleUI.exibirMensagem("Funcionalidade disponível na próxima release.", false);
                    break;
            }
        }
    }

    private void cadastrarDisciplina() {
        ConsoleUI.limparTela();
        ConsoleUI.exibirCabecalho("CADASTRAR DISCIPLINA");
        try {
            String codigo = ConsoleUI.lerEntrada("Codigo da disciplina: ");
            String nome = ConsoleUI.lerEntrada("Nome da disciplina: ");
            String cargaHorariaTexto = ConsoleUI.lerEntrada("Carga horaria (horas): ");
            String creditosTexto = ConsoleUI.lerEntrada("Créditos da disciplina: ");
            String preRequisitosTexto = ConsoleUI.lerEntrada("Pré-requisitos da disciplina: ");
            
            String[] partes = preRequisitosTexto.split(",");
            
            int cargaHoraria = Integer.parseInt(cargaHorariaTexto);
            int creditos = Integer.parseInt(creditosTexto);
            List<String> preReq = new ArrayList<>(Arrays.asList(partes));
            
            disciplinaService.cadastrarDisciplina(codigo, nome, cargaHoraria, creditos, preReq);
            ConsoleUI.exibirMensagem("Disciplina cadastrada com sucesso!", false);
        } catch (NumberFormatException e) {
            ConsoleUI.exibirMensagem("Erro: Carga horaria deve ser um numero inteiro.", true);
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

            String codigo =
                    ConsoleUI.lerEntrada(
                            "Codigo do periodo (ex: 2026.2): "
                    );

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

            java.time.LocalDate dataInicio =
                    java.time.LocalDate.parse(
                            ConsoleUI.lerEntrada(
                                    "Data inicio (AAAA-MM-DD): "
                            )
                    );

            java.time.LocalDate dataFim =
                    java.time.LocalDate.parse(
                            ConsoleUI.lerEntrada(
                                    "Data fim (AAAA-MM-DD): "
                            )
                    );

            boolean ativo =
                    Boolean.parseBoolean(
                            ConsoleUI.lerEntrada(
                                    "Periodo ativo? (true/false): "
                            )
                    );

            periodoService.cadastrarPeriodo(
                    codigo,
                    ano,
                    semestre,
                    dataInicio,
                    dataFim,
                    ativo
            );

            ConsoleUI.exibirMensagem(
                    "Periodo cadastrado com sucesso!",
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
