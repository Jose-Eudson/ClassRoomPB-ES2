package com.classroompb.service;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.classroompb.model.TipoUsuario;
import com.classroompb.model.Usuario;
import com.classroompb.repository.UsuarioRepository;

@DisplayName("RF43 - Relatório geral de usuários cadastrados")
public class UsuarioServiceRF43Test {

    @TempDir
    Path tempDir;

    private UsuarioService service;

    @BeforeEach
    void setUp() {
        UsuarioRepository repository = new UsuarioRepository(tempDir.resolve("usuarios.json").toString());
        service = new UsuarioService(repository);
    }

    @Nested
    @DisplayName("obterTodosUsuarios()")
    class ObterTodosUsuarios {

        @Test
        @DisplayName("Deve retornar lista vazia quando não há usuários cadastrados")
        void deveRetornarVaziaParaSistemaVazio() {
            List<Usuario> resultado = service.obterTodosUsuarios();
            assertTrue(resultado.isEmpty());
        }

        @Test
        @DisplayName("Deve retornar todos os usuários cadastrados")
        void deveRetornarTodosOsUsuarios() throws Exception {
            service.cadastrarUsuario("A001", "Aluno Um", "aluno@test.com", "senha", TipoUsuario.ALUNO);
            service.cadastrarUsuario("P001", "Prof Um", "prof@test.com", "senha", TipoUsuario.PROFESSOR);
            service.cadastrarUsuario("AD001", "Admin", "admin@test.com", "senha", TipoUsuario.ADMINISTRADOR);

            List<Usuario> resultado = service.obterTodosUsuarios();

            assertEquals(3, resultado.size());
        }

        @Test
        @DisplayName("Deve incluir usuários de todos os tipos")
        void deveIncluirTodosOsTipos() throws Exception {
            service.cadastrarUsuario("A001", "Aluno", "aluno@test.com", "senha", TipoUsuario.ALUNO);
            service.cadastrarUsuario("C001", "Coord", "coord@test.com", "senha", TipoUsuario.COORDENADOR);
            service.cadastrarUsuario("P001", "Prof", "prof@test.com", "senha", TipoUsuario.PROFESSOR);
            service.cadastrarUsuario("AD001", "Admin", "admin@test.com", "senha", TipoUsuario.ADMINISTRADOR);

            List<Usuario> resultado = service.obterTodosUsuarios();

            assertEquals(4, resultado.size());
            assertTrue(resultado.stream().anyMatch(u -> u.getTipo() == TipoUsuario.ALUNO));
            assertTrue(resultado.stream().anyMatch(u -> u.getTipo() == TipoUsuario.COORDENADOR));
            assertTrue(resultado.stream().anyMatch(u -> u.getTipo() == TipoUsuario.PROFESSOR));
            assertTrue(resultado.stream().anyMatch(u -> u.getTipo() == TipoUsuario.ADMINISTRADOR));
        }

        @Test
        @DisplayName("Deve refletir usuários deletados")
        void deveRefletirDelecao() throws Exception {
            service.cadastrarUsuario("A001", "Aluno Um", "aluno@test.com", "senha", TipoUsuario.ALUNO);
            service.cadastrarUsuario("P001", "Prof Um", "prof@test.com", "senha", TipoUsuario.PROFESSOR);
            service.deletarUsuario("A001");

            List<Usuario> resultado = service.obterTodosUsuarios();

            assertEquals(1, resultado.size());
            assertEquals("P001", resultado.get(0).getMatricula());
        }
    }
}
