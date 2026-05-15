package com.classroompb.service;

import java.util.List;

import com.classroompb.exception.CadastroDuplicadoException;
import com.classroompb.model.Administrador;
import com.classroompb.model.Aluno;
import com.classroompb.model.Coordenador;
import com.classroompb.model.Professor;
import com.classroompb.model.TipoUsuario;
import com.classroompb.model.Usuario;
import com.classroompb.repository.UsuarioRepository;
import com.classroompb.util.MatriculaGenerator;

/**
 * Camada de serviço que contém as regras de negócio para gerenciamento de usuários.
 * Valida os dados recebidos antes de delegar as operações ao repositório.
 */

public class UsuarioService {
    private UsuarioRepository repository;

    /** Injeta o repositório via construtor, facilitando o uso de mocks nos testes. */
    public UsuarioService(UsuarioRepository repository) {
        this.repository = repository;
    }

    /**
     * Cadastra um usuário com matrícula fornecida manualmente.
     * Valida campos obrigatórios e garante que matrícula e e-mail são únicos.
     */
    public void cadastrarUsuario(String matricula, String nome, String email, String senha, TipoUsuario tipo) throws Exception {
        validarCamposObrigatorios(matricula, nome, email, senha);
        if (tipo == null) {
            throw new Exception("Erro: Tipo de usuário não pode ser nulo.");
        }

        if (repository.existePorMatricula(matricula)) {
            throw new CadastroDuplicadoException(CadastroDuplicadoException.Campo.MATRICULA, matricula);
        }
        if (repository.existePorEmail(email)) {
            throw new CadastroDuplicadoException(CadastroDuplicadoException.Campo.EMAIL, email);
        }

        Usuario novoUsuario = criarUsuario(matricula, nome, email, senha, tipo);
        repository.salvar(novoUsuario);
        System.out.println("Usuário cadastrado com sucesso: " + nome);
    }

    /**
     * Cadastra um usuário gerando a matrícula automaticamente via MatriculaGenerator.
     * Retorna a matrícula gerada para exibição ao usuário.
     */
    public String cadastrarUsuarioComMatriculaAutomatica(String nome, String email, String senha, TipoUsuario tipo) throws Exception {
        validarCamposObrigatoriosSeMatricula(nome, email, senha);
        if (tipo == null) {
            throw new Exception("Erro: Tipo de usuário não pode ser nulo.");
        }
        if (repository.existePorEmail(email)) {
            throw new CadastroDuplicadoException(CadastroDuplicadoException.Campo.EMAIL, email);
        }

        String matricula = MatriculaGenerator.gerarMatricula(tipo, repository.listarTodos());
        Usuario novoUsuario = criarUsuario(matricula, nome, email, senha, tipo);
        repository.salvar(novoUsuario);
        System.out.println("Usuário cadastrado com sucesso: " + nome + " (Matrícula: " + matricula + ")");
        return matricula;
    }

    /**
     * Edita nome, e-mail e senha de um usuário existente, mantendo o mesmo tipo/matrícula.
     * Valida unicidade do novo e-mail caso tenha sido alterado.
     */
    public void editarUsuario(String matricula, String novoNome, String novoEmail, String novaSenha) throws Exception {
        validarCamposObrigatorios(matricula, novoNome, novoEmail, novaSenha);

        Usuario usuario = repository.buscarPorMatricula(matricula)
                .orElseThrow(() -> new Exception("Erro: Usuário com matrícula " + matricula + " não encontrado."));

        if (!usuario.getEmail().equalsIgnoreCase(novoEmail) && repository.existePorEmail(novoEmail)) {
            throw new CadastroDuplicadoException(CadastroDuplicadoException.Campo.EMAIL, novoEmail);
        }

        usuario.setNome(novoNome);
        usuario.setEmail(novoEmail);
        usuario.setSenha(novaSenha);

        repository.atualizar(usuario);
        System.out.println("Usuário atualizado com sucesso: " + novoNome);
    }

    /**
     * Edita um usuário incluindo possível mudança de tipo/cargo.
     * Se o tipo mudar, remove o usuário antigo e recria com nova matrícula gerada automaticamente,
     * pois cada tipo tem seu próprio prefixo de matrícula.
     */
    public void editarUsuarioComTipo(String matricula, String novoNome, String novoEmail, String novaSenha, TipoUsuario novoTipo) throws Exception {
        validarCamposObrigatorios(matricula, novoNome, novoEmail, novaSenha);
        if (novoTipo == null) {
            throw new Exception("Erro: Tipo de usuário não pode ser nulo.");
        }

        Usuario usuarioAntigo = repository.buscarPorMatricula(matricula)
                .orElseThrow(() -> new Exception("Erro: Usuário com matrícula " + matricula + " não encontrado."));

        if (!usuarioAntigo.getEmail().equalsIgnoreCase(novoEmail) && repository.existePorEmail(novoEmail)) {
            throw new CadastroDuplicadoException(CadastroDuplicadoException.Campo.EMAIL, novoEmail);
        }

        String novaMatricula = matricula;
        boolean tipoMudou = usuarioAntigo.getTipo() != novoTipo;

        if (tipoMudou) {
            repository.deletar(matricula);
            novaMatricula = MatriculaGenerator.gerarMatricula(novoTipo, repository.listarTodos());
        }

        Usuario usuarioAtualizado = criarUsuario(novaMatricula, novoNome, novoEmail, novaSenha, novoTipo);

        if (tipoMudou) {
            repository.salvar(usuarioAtualizado);
            System.out.println("Cargo alterado! Matrícula antiga (" + matricula + ") removida. Nova matrícula: " + novaMatricula);
        } else {
            repository.atualizar(usuarioAtualizado);
        }

        System.out.println("Usuário atualizado com sucesso: " + novoNome + " (Cargo: " + novoTipo + ")");
    }

    /**
     * Remove um usuário pelo número de matrícula.
     * Lança exceção se a matrícula não for encontrada.
     */
    public void deletarUsuario(String matricula) throws Exception {
        if (matricula == null || matricula.trim().isEmpty()) {
            throw new Exception("Erro: Matrícula não pode ser vazia.");
        }

        Usuario usuario = repository.buscarPorMatricula(matricula)
                .orElseThrow(() -> new Exception("Erro: Usuário com matrícula " + matricula + " não encontrado."));

        repository.deletar(matricula);
        System.out.println("Usuário deletado com sucesso: " + usuario.getNome());
    }

    /**
     * Busca e retorna um usuário pela matrícula.
     * Lança exceção se não encontrado ou se a matrícula for vazia.
     */
    public Usuario buscarUsuarioPorMatricula(String matricula) throws Exception {
        if (matricula == null || matricula.trim().isEmpty()) {
            throw new Exception("Erro: Matrícula não pode ser vazia.");
        }
        return repository.buscarPorMatricula(matricula)
                .orElseThrow(() -> new Exception("Erro: Usuário com matrícula " + matricula + " não encontrado."));
    }

    /**
     * Autentica um usuário pelo identificador (matrícula ou e-mail) e senha.
     * Se o identificador contiver '@', é tratado como e-mail; caso contrário, como matrícula.
     * O e-mail é buscado de forma case-insensitive; a matrícula é buscada de forma exata.
     * A senha é sempre verificada de forma case-sensitive.
     */
    public Usuario login(String identificador, String senha) throws Exception {
        if (identificador == null || identificador.trim().isEmpty()) {
            throw new Exception("Erro: Matrícula ou e-mail não pode ser vazio.");
        }
        if (senha == null || senha.trim().isEmpty()) {
            throw new Exception("Erro: Senha não pode ser vazia.");
        }

        Usuario usuario;
        if (identificador.contains("@")) {
            usuario = repository.buscarPorEmail(identificador)
                    .orElseThrow(() -> new Exception("Erro: Usuário não encontrado."));
        } else {
            usuario = repository.buscarPorMatricula(identificador)
                    .orElseThrow(() -> new Exception("Erro: Usuário não encontrado."));
        }

        if (!usuario.getSenha().equals(senha)) {
            throw new Exception("Erro: Senha incorreta.");
        }
        if (!matriculaCompativelComTipo(usuario.getMatricula(), usuario.getTipo())) {
            throw new Exception("Erro: Dados de acesso inválidos para o perfil do usuário.");
        }

        return usuario;
    }

    /** Imprime no console a lista de todos os usuários cadastrados. */
    public void listarUsuarios() {
        System.out.println("\n--- Lista de Usuários Cadastrados ---");
        repository.listarTodos().forEach(System.out::println);
    }

    /** Retorna a lista de todos os usuários para uso programático (ex: exibição em tabela na UI). */
    public List<Usuario> obterTodosUsuarios() {
        return repository.listarTodos();
    }

    // -------------------------------------------------------------------------
    // Helpers privados
    // -------------------------------------------------------------------------

    /** Valida que matrícula, nome, e-mail e senha não são nulos nem em branco. */
    private void validarCamposObrigatorios(String matricula, String nome, String email, String senha) throws Exception {
        if (matricula == null || matricula.trim().isEmpty()) {
            throw new Exception("Erro: Matrícula não pode ser vazia.");
        }
        validarCamposObrigatoriosSeMatricula(nome, email, senha);
    }

    /** Valida nome, e-mail e senha (sem matrícula — usada no cadastro com matrícula automática). */
    private void validarCamposObrigatoriosSeMatricula(String nome, String email, String senha) throws Exception {
        if (nome == null || nome.trim().isEmpty()) {
            throw new Exception("Erro: Nome não pode ser vazio.");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new Exception("Erro: E-mail não pode ser vazio.");
        }
        if (senha == null || senha.trim().isEmpty()) {
            throw new Exception("Erro: Senha não pode ser vazia.");
        }
    }

    /** Instancia a subclasse concreta de Usuario de acordo com o tipo informado. */
    private Usuario criarUsuario(String matricula, String nome, String email, String senha, TipoUsuario tipo) throws Exception {
        switch (tipo) {
            case ALUNO:         return new Aluno(matricula, nome, email, senha);
            case PROFESSOR:     return new Professor(matricula, nome, email, senha);
            case COORDENADOR:   return new Coordenador(matricula, nome, email, senha);
            case ADMINISTRADOR: return new Administrador(matricula, nome, email, senha);
            default:            throw new Exception("Erro: Tipo de usuário inválido.");
        }
    }

    /**
     * Garante consistência entre tipo de usuário e formato da matrícula.
     * ALUNO: A0001 | PROFESSOR: P0001 | COORDENADOR: C0001 | ADMINISTRADOR: AD0001
     */
    private boolean matriculaCompativelComTipo(String matricula, TipoUsuario tipo) {
        if (matricula == null || tipo == null) {
            return false;
        }
        String prefixoEsperado;
        switch (tipo) {
            case ALUNO:
                prefixoEsperado = "A";
                break;
            case PROFESSOR:
                prefixoEsperado = "P";
                break;
            case COORDENADOR:
                prefixoEsperado = "C";
                break;
            case ADMINISTRADOR:
                prefixoEsperado = "AD";
                break;
            default:
                return false;
        }

        if (!matricula.startsWith(prefixoEsperado)) {
            return false;
        }

        String parteNumerica = matricula.substring(prefixoEsperado.length());
        return !parteNumerica.isEmpty() && parteNumerica.matches("\\d+");
    }
}
