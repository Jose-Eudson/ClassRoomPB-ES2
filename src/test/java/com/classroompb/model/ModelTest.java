package com.classroompb.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Testes dos modelos de dominio")
public class ModelTest {

    @Nested
    @DisplayName("Aluno")
    class AlunoTest {

        @Test
        @DisplayName("Deve criar Aluno com construtor padrao")
        void deveCriarAlunoPadrao() {
            Aluno aluno = new Aluno();
            assertNotNull(aluno);
            assertNull(aluno.getMatricula());
        }

        @Test
        @DisplayName("Deve criar Aluno com construtor completo e tipo ALUNO")
        void deveCriarAlunoComConstrutorCompleto() {
            Aluno aluno = new Aluno("A0001", "Carlos", "carlos@teste.com", "123");
            assertEquals("A0001", aluno.getMatricula());
            assertEquals("Carlos", aluno.getNome());
            assertEquals("carlos@teste.com", aluno.getEmail());
            assertEquals("123", aluno.getSenha());
            assertEquals(TipoUsuario.ALUNO, aluno.getTipo());
        }

        @Test
        @DisplayName("toString de Aluno deve conter tipo, nome, matricula e email")
        void toStringDeveConterDados() {
            Aluno aluno = new Aluno("A0001", "Carlos", "carlos@teste.com", "123");
            String str = aluno.toString();
            assertTrue(str.contains("ALUNO"));
            assertTrue(str.contains("Carlos"));
            assertTrue(str.contains("A0001"));
            assertTrue(str.contains("carlos@teste.com"));
        }

        @Test
        @DisplayName("Deve atualizar campos via setters")
        void deveAtualizarCamposViaSetters() {
            Aluno aluno = new Aluno();
            aluno.setNome("Novo Nome");
            aluno.setEmail("novo@teste.com");
            aluno.setSenha("novaSenha");
            aluno.setMatricula("A9999");
            aluno.setTipo(TipoUsuario.ALUNO);

            assertEquals("Novo Nome", aluno.getNome());
            assertEquals("novo@teste.com", aluno.getEmail());
            assertEquals("novaSenha", aluno.getSenha());
            assertEquals("A9999", aluno.getMatricula());
            assertEquals(TipoUsuario.ALUNO, aluno.getTipo());
        }
    }

    @Nested
    @DisplayName("Professor")
    class ProfessorTest {

        @Test
        @DisplayName("Deve criar Professor com construtor padrao")
        void deveCriarProfessorPadrao() {
            assertNotNull(new Professor());
        }

        @Test
        @DisplayName("Deve criar Professor com tipo PROFESSOR")
        void deveCriarProfessorComTipo() {
            Professor prof = new Professor("P0001", "Maria", "maria@teste.com", "456");
            assertEquals(TipoUsuario.PROFESSOR, prof.getTipo());
            assertEquals("P0001", prof.getMatricula());
        }

        @Test
        @DisplayName("toString de Professor deve conter dados esperados")
        void toStringProfessor() {
            Professor prof = new Professor("P0001", "Maria", "maria@teste.com", "456");
            String str = prof.toString();
            assertTrue(str.contains("PROFESSOR"));
            assertTrue(str.contains("Maria"));
        }
    }

    @Nested
    @DisplayName("Coordenador")
    class CoordenadorTest {

        @Test
        @DisplayName("Deve criar Coordenador com construtor padrao")
        void deveCriarCoordenadorPadrao() {
            assertNotNull(new Coordenador());
        }

        @Test
        @DisplayName("Deve criar Coordenador com tipo COORDENADOR")
        void deveCriarCoordenadorComTipo() {
            Coordenador coord = new Coordenador("C0001", "Ana", "ana@teste.com", "789");
            assertEquals(TipoUsuario.COORDENADOR, coord.getTipo());
            assertEquals("C0001", coord.getMatricula());
        }

        @Test
        @DisplayName("toString de Coordenador deve conter dados esperados")
        void toStringCoordenador() {
            Coordenador coord = new Coordenador("C0001", "Ana", "ana@teste.com", "789");
            String str = coord.toString();
            assertTrue(str.contains("COORDENADOR"));
            assertTrue(str.contains("Ana"));
        }
    }

    @Nested
    @DisplayName("Administrador")
    class AdministradorTest {

        @Test
        @DisplayName("Deve criar Administrador com construtor padrao")
        void deveCriarAdministradorPadrao() {
            assertNotNull(new Administrador());
        }

        @Test
        @DisplayName("Deve criar Administrador com tipo ADMINISTRADOR")
        void deveCriarAdministradorComTipo() {
            Administrador admin = new Administrador("AD0001", "Root", "root@teste.com", "000");
            assertEquals(TipoUsuario.ADMINISTRADOR, admin.getTipo());
            assertEquals("AD0001", admin.getMatricula());
        }

        @Test
        @DisplayName("toString de Administrador deve conter dados esperados")
        void toStringAdministrador() {
            Administrador admin = new Administrador("AD0001", "Root", "root@teste.com", "000");
            String str = admin.toString();
            assertTrue(str.contains("ADMINISTRADOR"));
            assertTrue(str.contains("Root"));
        }
    }

    @Nested
    @DisplayName("TipoUsuario enum")
    class TipoUsuarioTest {

        @Test
        @DisplayName("Deve ter os quatro tipos de usuario")
        void deveConterQuatroTipos() {
            TipoUsuario[] tipos = TipoUsuario.values();
            assertEquals(4, tipos.length);
            assertTrue(Arrays.asList(tipos).contains(TipoUsuario.ALUNO));
            assertTrue(Arrays.asList(tipos).contains(TipoUsuario.PROFESSOR));
            assertTrue(Arrays.asList(tipos).contains(TipoUsuario.COORDENADOR));
            assertTrue(Arrays.asList(tipos).contains(TipoUsuario.ADMINISTRADOR));
        }

        @Test
        @DisplayName("Deve retornar tipo pelo nome")
        void deveRetornarTipoPeloNome() {
            assertEquals(TipoUsuario.ALUNO, TipoUsuario.valueOf("ALUNO"));
            assertEquals(TipoUsuario.PROFESSOR, TipoUsuario.valueOf("PROFESSOR"));
            assertEquals(TipoUsuario.COORDENADOR, TipoUsuario.valueOf("COORDENADOR"));
            assertEquals(TipoUsuario.ADMINISTRADOR, TipoUsuario.valueOf("ADMINISTRADOR"));
        }
    }

    @Nested
    @DisplayName("Curso")
    class CursoTest {

        @Test
        @DisplayName("Deve criar Curso com construtor padrao")
        void deveCriarCursoPadrao() {
            Curso curso = new Curso();
            assertNotNull(curso);
            assertNull(curso.getCodigo());
        }

        @Test
        @DisplayName("Deve criar Curso com construtor completo")
        void deveCriarCursoCompleto() {
            Curso curso = new Curso("ADS", "Análise e Desenvolvimento de Sistemas", 3200);
            assertEquals("ADS", curso.getCodigo());
            assertEquals("Análise e Desenvolvimento de Sistemas", curso.getNome());
            assertEquals(3200, curso.getCargaHoraria());
        }

        @Test
        @DisplayName("Deve atualizar Curso via setters")
        void deveAtualizarCursoViaSetters() {
            Curso curso = new Curso();
            curso.setCodigo("SI");
            curso.setNome("Sistemas de Informação");
            curso.setCargaHoraria(3000);

            assertEquals("SI", curso.getCodigo());
            assertEquals("Sistemas de Informação", curso.getNome());
            assertEquals(3000, curso.getCargaHoraria());
        }

        @Test
        @DisplayName("toString de Curso deve conter codigo, nome e carga horaria")
        void toStringCursoDeveConterDados() {
            Curso curso = new Curso("ADS", "Análise e Desenvolvimento de Sistemas", 3200);
            String str = curso.toString();
            assertTrue(str.contains("ADS"));
            assertTrue(str.contains("Análise e Desenvolvimento de Sistemas"));
            assertTrue(str.contains("3200"));
            assertTrue(str.contains("[CURSO]"));
        }
    }

    @Nested
    @DisplayName("Disciplina")
    class DisciplinaTest {

        @Test
        @DisplayName("Deve criar Disciplina com construtor padrao e lista de pre-requisitos vazia")
        void deveCriarDisciplinaPadrao() {
            Disciplina disciplina = new Disciplina();
            assertNotNull(disciplina);
            assertNotNull(disciplina.getPreRequisitos());
            assertTrue(disciplina.getPreRequisitos().isEmpty());
        }

        @Test
        @DisplayName("Deve criar Disciplina com construtor completo")
        void deveCriarDisciplinaCompleta() {
            List<String> preReqs = Arrays.asList("ES1", "POO");
            Disciplina disciplina = new Disciplina("ES2", "Engenharia de Software 2", 60, 4, preReqs);
            assertEquals("ES2", disciplina.getCodigo());
            assertEquals("Engenharia de Software 2", disciplina.getNome());
            assertEquals(60, disciplina.getCargaHoraria());
            assertEquals(4, disciplina.getCreditos());
            assertEquals(preReqs, disciplina.getPreRequisitos());
        }

        @Test
        @DisplayName("Deve atualizar Disciplina via setters incluindo creditos e pre-requisitos")
        void deveAtualizarDisciplinaViaSetters() {
            Disciplina disciplina = new Disciplina();
            disciplina.setCodigo("BD");
            disciplina.setNome("Banco de Dados");
            disciplina.setCargaHoraria(80);
            disciplina.setCreditos(5);
            List<String> preReqs = new ArrayList<>();
            preReqs.add("ALEST");
            disciplina.setPreRequisitos(preReqs);

            assertEquals("BD", disciplina.getCodigo());
            assertEquals("Banco de Dados", disciplina.getNome());
            assertEquals(80, disciplina.getCargaHoraria());
            assertEquals(5, disciplina.getCreditos());
            assertEquals(preReqs, disciplina.getPreRequisitos());
        }

        @Test
        @DisplayName("toString de Disciplina deve conter todos os dados principais")
        void toStringDisciplinaDeveConterDados() {
            Disciplina disciplina = new Disciplina("ES2", "Engenharia de Software 2", 
            		60, 4, Arrays.asList("ES1", "POO"));
            String str = disciplina.toString();
            assertTrue(str.contains("ES2"));
            assertTrue(str.contains("Engenharia de Software 2"));
            assertTrue(str.contains("60"));
            assertTrue(str.contains("4"));
            assertTrue(str.contains("ES1"));
            assertTrue(str.contains("POO"));
            assertTrue(str.contains("[DISCIPLINA]"));
        }
    }

    @Nested
    @DisplayName("Usuario (Abstrato)")
    class UsuarioAbstractTest {
        // Como Usuario é abstrato, testamos através de uma implementação concreta (Aluno já testado)
        // Mas podemos validar o comportamento comum aqui se necessário.
        
        @Test
        @DisplayName("Deve validar que Usuario retem dados comuns")
        void deveValidarDadosComuns() {
            Usuario u = new Aluno("123", "Teste", "teste@email.com", "senha");
            assertEquals("123", u.getMatricula());
            assertEquals("Teste", u.getNome());
            assertEquals("teste@email.com", u.getEmail());
            assertEquals("senha", u.getSenha());
        }
    }
}
