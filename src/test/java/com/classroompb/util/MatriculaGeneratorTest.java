package com.classroompb.util;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.classroompb.model.Administrador;
import com.classroompb.model.Aluno;
import com.classroompb.model.Coordenador;
import com.classroompb.model.Professor;
import com.classroompb.model.TipoUsuario;
import com.classroompb.model.Usuario;

/**
 * Testes unitários para MatriculaGenerator. Verifica a geração correta de matrículas para cada tipo de usuário,
 * incluindo sequenciamento, independência entre tipos e tratamento de edge cases.
 */

@DisplayName("Testes de MatriculaGenerator")
public class MatriculaGeneratorTest {

    @Test
    @DisplayName("Deve gerar primeira matrícula A0001 para Aluno sem usuários existentes")
    void deveGerarPrimeiraMatriculaParaAluno() {
        String matricula = MatriculaGenerator.gerarMatricula(TipoUsuario.ALUNO, new ArrayList<>());
        assertEquals("A0001", matricula);
    }

    @Test
    @DisplayName("Deve gerar A0002 quando já existe A0001")
    void deveGerarProximaMatriculaParaAluno() {
        List<Usuario> usuarios = new ArrayList<>();
        usuarios.add(new Aluno("A0001", "João", "joao@email.com", "123"));

        String matricula = MatriculaGenerator.gerarMatricula(TipoUsuario.ALUNO, usuarios);
        assertEquals("A0002", matricula);
    }

    @Test
    @DisplayName("Deve gerar primeira matrícula P0001 para Professor")
    void deveGerarPrimeiraMatriculaParaProfessor() {
        String matricula = MatriculaGenerator.gerarMatricula(TipoUsuario.PROFESSOR, new ArrayList<>());
        assertEquals("P0001", matricula);
    }

    @Test
    @DisplayName("Deve gerar P0002 quando já existe P0001")
    void deveGerarProximaMatriculaParaProfessor() {
        List<Usuario> usuarios = new ArrayList<>();
        usuarios.add(new Professor("P0001", "Maria", "maria@email.com", "123"));

        String matricula = MatriculaGenerator.gerarMatricula(TipoUsuario.PROFESSOR, usuarios);
        assertEquals("P0002", matricula);
    }

    @Test
    @DisplayName("Deve gerar primeira matrícula C0001 para Coordenador")
    void deveGerarPrimeiraMatriculaParaCoordenador() {
        String matricula = MatriculaGenerator.gerarMatricula(TipoUsuario.COORDENADOR, new ArrayList<>());
        assertEquals("C0001", matricula);
    }

    @Test
    @DisplayName("Deve gerar C0002 quando já existe C0001")
    void deveGerarProximaMatriculaParaCoordenador() {
        List<Usuario> usuarios = new ArrayList<>();
        usuarios.add(new Coordenador("C0001", "Carlos", "carlos@email.com", "123"));

        String matricula = MatriculaGenerator.gerarMatricula(TipoUsuario.COORDENADOR, usuarios);
        assertEquals("C0002", matricula);
    }

    @Test
    @DisplayName("Deve gerar primeira matrícula AD0001 para Administrador")
    void deveGerarPrimeiraMatriculaParaAdministrador() {
        String matricula = MatriculaGenerator.gerarMatricula(TipoUsuario.ADMINISTRADOR, new ArrayList<>());
        assertEquals("AD0001", matricula);
    }

    @Test
    @DisplayName("Deve gerar AD0002 quando já existe AD0001")
    void deveGerarProximaMatriculaParaAdministrador() {
        List<Usuario> usuarios = new ArrayList<>();
        usuarios.add(new Administrador("AD0001", "Admin", "admin@email.com", "123"));

        String matricula = MatriculaGenerator.gerarMatricula(TipoUsuario.ADMINISTRADOR, usuarios);
        assertEquals("AD0002", matricula);
    }

    @Test
    @DisplayName("Não deve interferir entre tipos — alunos não afetam numeração de professores")
    void deveManterSequencialIndependentePorTipo() {
        List<Usuario> usuarios = new ArrayList<>();
        usuarios.add(new Aluno("A0001", "João", "joao@email.com", "123"));
        usuarios.add(new Aluno("A0002", "Maria", "maria@email.com", "456"));

        // Com 2 alunos existentes, professor ainda deve começar em P0001
        String matriculaProfessor = MatriculaGenerator.gerarMatricula(TipoUsuario.PROFESSOR, usuarios);
        assertEquals("P0001", matriculaProfessor);
    }

    @Test
    @DisplayName("Deve ignorar matrículas fora do padrão esperado")
    void deveIgnorarMatriculasForaDoPadrao() {
        List<Usuario> usuarios = new ArrayList<>();
        usuarios.add(new Aluno("ALUNO_ANTIGO", "João", "joao@email.com", "123"));

        // Matrícula fora do formato não deve influenciar o sequencial
        String matricula = MatriculaGenerator.gerarMatricula(TipoUsuario.ALUNO, usuarios);
        assertEquals("A0001", matricula);
    }

    @Test
    @DisplayName("Deve gerar matrícula sequencial correta com lista mista de tipos")
    void deveGerarMatriculaComListaMista() {
        List<Usuario> usuarios = new ArrayList<>();
        usuarios.add(new Aluno("A0001", "Alice", "alice@email.com", "1"));
        usuarios.add(new Professor("P0001", "Bob", "bob@email.com", "2"));
        usuarios.add(new Aluno("A0002", "Carol", "carol@email.com", "3"));

        String proximoAluno = MatriculaGenerator.gerarMatricula(TipoUsuario.ALUNO, usuarios);
        String proximoProfessor = MatriculaGenerator.gerarMatricula(TipoUsuario.PROFESSOR, usuarios);

        assertEquals("A0003", proximoAluno);
        assertEquals("P0002", proximoProfessor);
    }

    @Test
    @DisplayName("Deve lançar exceção para tipo nulo")
    void deveLancarExcecaoParaTipoNulo() {
        // O switch do obterPrefixo recebe null e lança NullPointerException antes do default
        assertThrows(NullPointerException.class, () -> MatriculaGenerator.gerarMatricula(null, new ArrayList<>()));
    }
}
