package com.classroompb.ui;

import java.util.Arrays;
import java.util.List;

import com.classroompb.model.TipoUsuario;
import com.classroompb.model.Usuario;
import com.classroompb.repository.CursoRepository;
import com.classroompb.repository.DisciplinaRepository;
import com.classroompb.repository.FrequenciaRepository;
import com.classroompb.repository.HistoricoRepository;
import com.classroompb.repository.MatriculaTurmaRepository;
import com.classroompb.repository.NotaRepository;
import com.classroompb.repository.PeriodoLetivoRepository;
import com.classroompb.repository.TurmaRepository;
import com.classroompb.repository.UsuarioRepository;
import com.classroompb.service.CursoService;
import com.classroompb.service.DisciplinaService;
import com.classroompb.service.FrequenciaService;
import com.classroompb.service.HistoricoService;
import com.classroompb.service.MatriculaTurmaService;
import com.classroompb.service.NotaService;
import com.classroompb.service.PeriodoLetivoService;
import com.classroompb.service.TurmaService;
import com.classroompb.service.UsuarioService;

/**
 * Ponto de entrada da aplicação. Responsável apenas por inicializar os serviços, exibir a tela inicial e rotear o
 * usuário logado para o controlador correto do seu perfil.
 */

public class Main {

    private static UsuarioService service;
    private static CursoService cursoService;
    private static DisciplinaService disciplinaService;
    private static PeriodoLetivoService periodoLetivoService;
    private static TurmaService turmaService;
    private static FrequenciaService freqService;
    private static NotaService notaService;
    private static MatriculaTurmaService matriculaService;

    private static AlunoController alunoController;
    private static ProfessorController professorController;
    private static CoordenadorController coordenadorController;
    private static AdminController adminController;

    public static void main(String[] args) {
        UsuarioRepository repository = new UsuarioRepository();
        CursoRepository cursoRepository = new CursoRepository();
        DisciplinaRepository disciplinaRepository = new DisciplinaRepository();
        PeriodoLetivoRepository periodoLetivoRepository = new PeriodoLetivoRepository();
        TurmaRepository turmaRepository = new TurmaRepository();
        HistoricoRepository historicoRepository = new HistoricoRepository();
        MatriculaTurmaRepository matriculaRepository = new MatriculaTurmaRepository();
        FrequenciaRepository frequenciaRepository = new FrequenciaRepository();
        NotaRepository notaRepository = new NotaRepository();
        service = new UsuarioService(repository, cursoRepository);
        cursoService = new CursoService(cursoRepository);
        disciplinaService = new DisciplinaService(disciplinaRepository);
        periodoLetivoService = new PeriodoLetivoService(periodoLetivoRepository);
        turmaService = new TurmaService(turmaRepository, disciplinaRepository, periodoLetivoRepository);
        freqService = new FrequenciaService(frequenciaRepository, turmaRepository, matriculaRepository,
                historicoRepository, notaRepository, disciplinaRepository, repository);
        notaService = new NotaService(notaRepository, turmaRepository, matriculaRepository, historicoRepository,
                frequenciaRepository, disciplinaRepository, repository);
        HistoricoService historicoService = new HistoricoService(historicoRepository, repository);
        matriculaService = new MatriculaTurmaService(matriculaRepository, turmaRepository, periodoLetivoRepository,
                disciplinaRepository, historicoRepository);

        alunoController = new AlunoController(service, disciplinaService, periodoLetivoService, turmaService,
                matriculaService, freqService, notaService, historicoService);
        professorController = new ProfessorController(service, freqService, notaService);
        coordenadorController = new CoordenadorController(service, disciplinaService, periodoLetivoService,
                turmaService, matriculaService, historicoService);
        adminController = new AdminController(service, cursoService);

        while (true) {
            List<String> opcoes = Arrays.asList("Login", "Cadastrar-se (Apenas Alunos)", "Sair");
            int escolha = ConsoleUI.exibirMenuInterativo("ClassRoomPB - Sistema Academico", opcoes);

            if (escolha == 2 || escolha == -1) {
                break;
            }
            switch (escolha) {
            case 0:
                realizarLogin();
                break;
            case 1:
                cadastrarAutoAluno();
                break;
            }
        }

        ConsoleUI.limparTela();
        System.out.println("Sistema encerrado. Ate logo!");
    }

    // -------------------------------------------------------------------------
    // Login e cadastro inicial
    // -------------------------------------------------------------------------

    /** Captura matricula ou e-mail e senha, autentica e redireciona para o controller do perfil. */
    private static void realizarLogin() {
        ConsoleUI.limparTela();
        ConsoleUI.exibirCabecalho("LOGIN");
        String identificador = ConsoleUI.lerEntrada("Matricula ou E-mail: ");
        String senha = ConsoleUI.lerSenha("Senha: ");

        try {
            Usuario usuario = service.login(identificador, senha);
            ConsoleUI.exibirMensagem("Bem-vindo, " + usuario.getNome() + "!", false);
            rotearPorPerfil(usuario);
        } catch (Exception e) {
            ConsoleUI.exibirMensagem(e.getMessage(), true);
        }
    }

    /** Permite que um novo aluno se cadastre informando nome, e-mail e senha. Matricula gerada automaticamente. */
    private static void cadastrarAutoAluno() {
        ConsoleUI.limparTela();
        ConsoleUI.exibirCabecalho("CADASTRO DE ALUNO");
        try {
            String nome = ConsoleUI.lerEntrada("Nome: ");
            String email = ConsoleUI.lerEntrada("E-mail: ");
            String senha = ConsoleUI.lerSenha("Senha: ");
            String codigoCurso = ConsoleUI.lerEntrada("Código do curso: ");

            String matricula = service.cadastrarUsuarioComMatriculaAutomatica(nome, email, senha, TipoUsuario.ALUNO,
                    codigoCurso);
            ConsoleUI.exibirMensagem("Cadastro realizado! Sua matricula: " + matricula, false);
        } catch (Exception e) {
            ConsoleUI.exibirMensagem(e.getMessage(), true);
        }
    }

    // -------------------------------------------------------------------------
    // Roteamento por perfil
    // -------------------------------------------------------------------------

    /**
     * Direciona o usuario logado para o controller correspondente ao seu tipo. Cada controller e responsavel pelo
     * proprio menu e loop de navegacao.
     */
    private static void rotearPorPerfil(Usuario usuario) {
        if (usuario == null || usuario.getTipo() == null) {
            ConsoleUI.exibirMensagem("Erro: Perfil de acesso inválido.", true);
            return;
        }

        switch (usuario.getTipo()) {
        case ALUNO:
            alunoController.exibirMenu(usuario);
            break;
        case PROFESSOR:
            professorController.exibirMenu(usuario);
            break;
        case COORDENADOR:
            coordenadorController.exibirMenu(usuario);
            break;
        case ADMINISTRADOR:
            adminController.exibirMenu(usuario);
            break;
        default:
            ConsoleUI.exibirMensagem("Erro: Perfil de acesso inválido.", true);
            break;
        }
    }
}
